package tv.ismar.daisy.core.client;


import java.util.concurrent.TimeUnit;

import cn.ismartv.log.interceptor.HttpLoggingInterceptor;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;


/**
 * Created by huaijie on 1/18/16.
 */
public class HttpManager {
    private static final int DEFAULT_CONNECT_TIMEOUT = 2;
    private static final int DEFAULT_READ_TIMEOUT = 5;

    private static final String SKY_HOST = "http://media.lily.tvxio.com";

    public Retrofit media_lily_Retrofit;

    private static HttpManager ourInstance = new HttpManager();

    public static HttpManager getInstance() {
        return ourInstance;
    }

    public OkHttpClient mClient;

    private HttpManager() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        mClient = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_READ_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(interceptor)
                .build();
        media_lily_Retrofit = new Retrofit.Builder()
                .client(mClient)
                .baseUrl(SKY_HOST)
                .build();
    }
}
