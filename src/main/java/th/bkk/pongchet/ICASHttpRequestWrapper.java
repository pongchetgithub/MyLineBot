package th.bkk.pongchet;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class ICASHttpRequestWrapper extends HttpServletRequestWrapper {
    private HttpServletRequest original;
    private byte[] reqBytes;
    private boolean firstTime = true;

    public ICASHttpRequestWrapper(HttpServletRequest request) {
        super(request);
        original = request;
    }

    @Override
    public BufferedReader getReader() throws IOException{
        if(firstTime)
            firstTime();
        InputStreamReader isr = new InputStreamReader(new ByteArrayInputStream(reqBytes));
        return new BufferedReader(isr);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {

        if(firstTime)
            firstTime();

        ServletInputStream sis = new ServletInputStream() {
            private int lastIndexRetrieved = -1;
            private ReadListener readListener = null;

            @Override
            public boolean isFinished() {
                return (lastIndexRetrieved == reqBytes.length-1);
            }

            @Override
            public boolean isReady() {
                return isFinished();
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                this.readListener = readListener;
                if (!isFinished()) {
                    try {
                        readListener.onDataAvailable();
                    } catch (IOException e) {
                        readListener.onError(e);
                    }
                } else {
                    try {
                        readListener.onAllDataRead();
                    } catch (IOException e) {
                        readListener.onError(e);
                    }
                }
            }

            @Override
            public int read() throws IOException {
                int i;
                if (!isFinished()) {
                    i = reqBytes[lastIndexRetrieved+1];
                    lastIndexRetrieved++;
                    if (isFinished() && (readListener != null)) {
                        try {
                            readListener.onAllDataRead();
                        } catch (IOException ex) {
                            readListener.onError(ex);
                            throw ex;
                        }
                    }
                    return i;
                } else {
                    return -1;
                }
            }
        };
        return sis;
    }


    private void firstTime() throws IOException{
        firstTime = false;
        byte[] buffer = new byte[4096];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ServletInputStream inputStream = original.getInputStream();
        int readLength = 0;
        while((readLength = inputStream.read(buffer)) != -1) {
            baos.write(buffer, 0, readLength);
        }
        reqBytes = baos.toByteArray();
    }
}
