package th.bkk.pongchet;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import org.apache.commons.lang3.math.NumberUtils;
import org.json.JSONException;
import org.json.JSONObject;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings({ "rawtypes", "unchecked", "deprecation" })
public class RawRequestCollectorFilter implements Filter {

	@Override
	public void destroy() {
		log.info(">> destroy: " + RawRequestCollectorFilter.class.getSimpleName());
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		
		if (request instanceof HttpServletRequest) {
			HttpServletRequest wrappedRequest = new ICASHttpRequestWrapper((HttpServletRequest)request);
			log.info("http method: "+wrappedRequest.getMethod()+ " , uri:"+wrappedRequest.getRequestURI());
			
			StringBuilder b = new StringBuilder();
			String rawRq = "";
			Boolean multiPartData = Boolean.FALSE;
			if (wrappedRequest.getContentType() != null &&
					wrappedRequest.getContentType().toLowerCase().contains("multipart/form-data")){
				multiPartData = Boolean.TRUE;
				log.debug("request is multipart/form-data");
			}
			
			// ----- Header -----
			b.append("{ root: {")
				.append("\"header\":").append("{\n")
					.append(buildHeaderJson(wrappedRequest))
				.append("}\n").append(",")
			// ----- Body Parameter -----
					.append("\"body\":").append("{\n")
						.append("\"parameters\":").append("{\n");
							if(multiPartData) {
//									wrappedRequest.get
								Map m = new HashMap();
								m.put("description", wrappedRequest.getParameter("description"));
							    Part filePart = wrappedRequest.getPart("file"); // Retrieves <input type="file" name="file">
							    m.put("content-type", filePart.getContentType());
							    m.put("size", filePart.getSize());
							    m.put("name", filePart.getName());
							    m.put("filename", filePart.getSubmittedFileName());
							    b.append(buildMapJson(m));
							} else {
								b.append(buildParameterJson(wrappedRequest));
							}
						b.append("},\n")
			// ----- Body JSON -----
						.append("\"json\":");
						if (multiPartData){
							b.append("{ \"multipart_form-data\" : true }");
						} else {
							b.append(buildBodyDataJson(wrappedRequest));
						}
					b.append("}\n")
				.append("} }\n");
//				log.debug("before format: \n"+b);

			try {
				// ----- JSON String to XML String -----
				JSONObject json = new JSONObject(b.toString());
				rawRq = json.toString(4);
			} catch (JSONException e) {
				log.warn("Error while collect raw request as json format",e);
			}				
			log.info("### raw REQUEST json format: \n"+rawRq);
			
			StringBuffer url = ((HttpServletRequest) request).getRequestURL();
			if (((HttpServletRequest) request).getQueryString() != null) url.append("?").append(((HttpServletRequest) request).getQueryString());
			chain.doFilter(wrappedRequest, response);
		} else {
			chain.doFilter(request, response);
		}
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		log.info(">> init " + RawRequestCollectorFilter.class.getSimpleName());
	}

	private String getFullURL(HttpServletRequest request) {
		StringBuffer requestURL = request.getRequestURL();
		String queryString = request.getQueryString();

		if (queryString == null) {
			return requestURL.toString();
		} else {
			return requestURL.append('?').append(queryString).toString();
		}
	}
	
	private StringBuilder buildHeaderJson(HttpServletRequest request) {
		StringBuilder b = new StringBuilder();
		Enumeration<String> h = request.getHeaderNames();
		while (h.hasMoreElements()) {
			String key = h.nextElement();
			String value = null;
			if (NumberUtils.isNumber(request.getHeader(key))) {
				value = (new BigDecimal(request.getHeader(key))).toString();
			} else {
				value = "\""+request.getHeader(key)+"\"";
			}
			b.append("\"").append(key.toLowerCase()).append("\":").append(value).append(",\n");
		}
			b.append("\"url\":\"").append(getFullURL(request)).append("\",\n")
			.append("\"method\":\"").append(request.getMethod()).append("\"\n");
		return b;
	}
	
	private StringBuilder buildMapJson(Map m) {
		StringBuilder b = new StringBuilder();
		java.util.Iterator<String> iter = m.keySet().iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			Object value = m.get(key);
			b.append("\"").append(key).append("\":");
			if (value == null) {
				b.append("\"\",\n");
			} else if (value instanceof Number) {
				b.append(value).append(",\n");
			} else {
				b.append("\"").append(value).append("\",\n");
			}
		}
		b.append("\"data\":\"\"");
		return b;
	}
	
	private StringBuilder buildParameterJson(HttpServletRequest request) {
		StringBuilder b = new StringBuilder();
		Enumeration<String> p = request.getParameterNames();
		while (p.hasMoreElements()) {
			String key = p.nextElement();
			String value = null;
			if (NumberUtils.isNumber(request.getParameter(key))) {
				log.debug("raw value: "+request.getParameter(key));
//				value = (new BigDecimal(request.getParameter(key))).toString();
				BigDecimal bd = new BigDecimal(request.getParameter(key));
				log.debug("bigdecimal value: "+bd);
				DecimalFormat df = new DecimalFormat("##########.##");
				df.setMaximumFractionDigits(2);
				df.setMinimumFractionDigits(0);
				df.setGroupingUsed(false);
				value = df.format(bd);
				if (value.endsWith(".00")) value = (value.substring(0, value.indexOf(".00")));
				log.debug("format value: "+value);
			} else {
				value = "\""+request.getParameter(key)+"\"";
			}
			b.append("\"").append(key).append("\":").append(value).append("\n");
			if (p.hasMoreElements()) b.append(",");
		}

		log.debug("json value: "+b);
		return b;
	}
	
	private StringBuilder buildBodyDataJson(HttpServletRequest request) throws IOException{
		String s;
		BufferedReader br = request.getReader();
		StringBuilder b = new StringBuilder();
		while ((s = br.readLine()) != null) {
			b.append(s);
		}
		if (b.length() == 0) {
			return b.append("{}");
		}
			return b;
	}

}
