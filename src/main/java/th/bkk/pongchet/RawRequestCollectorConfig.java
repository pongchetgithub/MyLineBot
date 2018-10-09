package th.bkk.pongchet;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SuppressWarnings({"unchecked","rawtypes"})
public class RawRequestCollectorConfig {
	
	@Bean
	public FilterRegistrationBean rawRequestFilterBean() {
		final FilterRegistrationBean filterRegBean = new FilterRegistrationBean();
		filterRegBean.setFilter(new RawRequestCollectorFilter());
		filterRegBean.addUrlPatterns("/*");
		filterRegBean.setEnabled(Boolean.TRUE);
		filterRegBean.setName(RawRequestCollectorFilter.class.getSimpleName());
		filterRegBean.setAsyncSupported(Boolean.TRUE);
		return filterRegBean;
	}
}
