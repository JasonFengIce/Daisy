package cn.ismartv.activator.core.http;


import java.util.concurrent.TimeUnit;

import android.net.Uri;
import cn.ismartv.log.interceptor.HttpLoggingInterceptor;
import okhttp3.OkHttpClient;
import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;

/**
 * Created by huaijie on 1/14/16.
 */
public class HttpClientManager {
    private static final int DEFAULT_CONNECT_TIMEOUT = 2;
    private static final int DEFAULT_READ_TIMEOUT = 5;
    private static final String SKY_HOST = "http://sky.tvxio.com";
    private static final String SKY_HOST_TEST = "http://peachtest.tvxio.com";

    public Retrofit SKY_Retrofit;

    private static HttpClientManager ourInstance = new HttpClientManager();

    public static HttpClientManager getInstance() {
        return ourInstance;
    }

    private HttpClientManager() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_READ_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(interceptor)
                .build();
        SKY_Retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(appendProtocol(SKY_HOST))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    private String appendProtocol(String host) {
        Uri uri = Uri.parse(host);
        String url = uri.toString();
        if (!uri.toString().startsWith("http://") && !uri.toString().startsWith("https://")) {
            url = "http://" + host;
        }

        if (!url.endsWith("/")) {
            url = url + "/";
        }
        return url;
    }
}
