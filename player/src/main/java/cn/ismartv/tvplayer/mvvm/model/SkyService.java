package cn.ismartv.tvplayer.mvvm.model;

import android.net.Uri;

import cn.ismartv.log.interceptor.HttpLoggingInterceptor;
import okhttp3.OkHttpClient;
import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;
import retrofit2.RxJavaCallAdapterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by huibin on 7/15/16.
 */
public interface SkyService {

    @GET("api/clip/{pk}/")
    Observable<ClipInfoEntity> apiClip(
            @Path("pk") String pk,
            @Query("device_token") String device_token,
            @Query("sign") String sign,
            @Query("code") String code
    );


    class Factory {
        public static SkyService create(String baseUrl) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    .build();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(appendProtocol(baseUrl))
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .client(client)
                    .build();
            return retrofit.create(SkyService.class);
        }

        private static String appendProtocol(String host) {
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
}
