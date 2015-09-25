package tv.ismar.daisy.jni;

/**
 * Created by huaijie on 9/25/15.
 */
public class HttpClient {
    static {
        System.loadLibrary("HttpClient");
    }


    public native String doGet(String host, String page, String parameters);


    public native String doPost(String host, String page, String parameters);

}
