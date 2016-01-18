package tv.ismar.daisy.core.client;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;

import java.util.concurrent.TimeUnit;

import retrofit.Retrofit;

/**
 * Created by huaijie on 1/18/16.
 */
public class HttpManager {

    private static final int DEFAULT_TIMEOUT = 2;
    private static final String SKY_HOST = "http://media.lily.tvxio.com";

    public Retrofit media_lily_Retrofit;

    private static HttpManager ourInstance = new HttpManager();

    public static HttpManager getInstance() {
        return ourInstance;
    }

    private HttpManager() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient();
        client.setConnectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        client.interceptors().add(interceptor);
        media_lily_Retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(SKY_HOST)
                .build();
    }
}
