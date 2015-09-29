package tv.ismar.daisy.jni;

import java.util.IdentityHashMap;

/**
 * Created by huaijie on 9/28/15.
 */
public class HttpResponseEntity {
    private int code;
    private IdentityHashMap<String, String> headers;
    private String body;


    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public IdentityHashMap<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(IdentityHashMap<String, String> headers) {
        this.headers = headers;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
