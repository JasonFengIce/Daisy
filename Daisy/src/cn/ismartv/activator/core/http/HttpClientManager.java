package cn.ismartv.activator.core.http;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;

import java.util.concurrent.TimeUnit;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

/**
 * Created by huaijie on 1/14/16.
 */
public class HttpClientManager {
    private static final int DEFAULT_TIMEOUT = 2;
    private static final String SKY_HOST = "http://sky.tvxio.com";

    public Retrofit SKY_Retrofit;

    private static HttpClientManager ourInstance = new HttpClientManager();

    public static HttpClientManager getInstance() {
        return ourInstance;
    }

    private HttpClientManager() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient();
        client.setConnectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        client.interceptors().add(interceptor);
        SKY_Retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(SKY_HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}
