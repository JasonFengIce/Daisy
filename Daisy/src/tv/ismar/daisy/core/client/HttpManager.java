package tv.ismar.daisy.core.client;


import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.net.Uri;
import cn.ismartv.log.interceptor.HttpLoggingInterceptor;
import okhttp3.*;
import retrofit2.Retrofit;


/**
 * Created by huaijie on 1/18/16.
 */
public class HttpManager {
    private static final int DEFAULT_CONNECT_TIMEOUT = 2;
    private static final int DEFAULT_READ_TIMEOUT = 5;

    private static final String SKY_HOST = "http://media.lily.tvxio.com";

    public Retrofit media_lily_Retrofit;

    private static HttpManager ourInstance;

    public static HttpManager getInstance() {
        if (ourInstance == null) {
            ourInstance = new HttpManager();
        }
        return ourInstance;
    }

    public OkHttpClient mClient;

    public OkHttpClient mCacheClient;

    private static Context mContext;

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


        File cacheFile = new File(mContext.getCacheDir(), "okhttp_cache");
        Cache cache = new Cache(cacheFile, 1024 * 1024 * 100); //100Mb
        mCacheClient = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_READ_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(interceptor)
                .addInterceptor(CACHE_CONTROL_INTERCEPTOR)
                .addNetworkInterceptor(CACHE_CONTROL_INTERCEPTOR)
                .cache(cache)
                .build();
    }

    public static void initialize(Context context) {
        mContext = context;
    }

    private final Interceptor CACHE_CONTROL_INTERCEPTOR = new Interceptor() {
        @Override
        public okhttp3.Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            if (!NetUtils.isConnected(mContext)) {
                request = request.newBuilder()
                        .cacheControl(CacheControl.FORCE_CACHE)
                        .build();
            }
            okhttp3.Response originalResponse = chain.proceed(request);
            if (NetUtils.isConnected(mContext)) {
                //有网的时候读接口上的@Headers里的配置，你可以在这里进行统一的设置
                String cacheControl = request.cacheControl().toString();
                return originalResponse.newBuilder()
                        .header("Cache-Control", cacheControl)
                        .removeHeader("Pragma")
                        .build();
            } else {
                return originalResponse.newBuilder()
                        .header("Cache-Control", "public, only-if-cached, max-stale=2419200")
                        .removeHeader("Pragma")
                        .build();
            }
        }
    };

    public static String appendProtocol(String host) {
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
