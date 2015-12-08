package tv.ismar.daisy.core.logger;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.HEAD;
import retrofit.http.Header;
import retrofit.http.POST;
import tv.ismar.daisy.core.VodUserAgent;
import tv.ismar.daisy.core.preferences.AccountSharedPrefs;
import tv.ismar.daisy.utils.HardwareUtils;
import tv.ismar.sakura.data.http.Empty;

/**
 * Created by huaijie on 11/16/15.
 */
public class AppLogger {

    static Retrofit buildRetrofit() {
        String advHost = "http://" + AccountSharedPrefs.getInstance().getSharedPrefs(AccountSharedPrefs.LOG_DOMAIN);
//        String advHost = "http://10.254.0.100:8080";
        OkHttpClient client = new OkHttpClient();
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        client.interceptors().add(interceptor);
        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(advHost)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit;
    }

    static void log(String data) {
        AccountSharedPrefs accountSharedPrefs = AccountSharedPrefs.getInstance();
        String sn = accountSharedPrefs.getSharedPrefs(AccountSharedPrefs.SN_TOKEN);
        String deviceToken = accountSharedPrefs.getSharedPrefs(AccountSharedPrefs.DEVICE_TOKEN);
        String accessToken = accountSharedPrefs.getSharedPrefs(AccountSharedPrefs.ACESS_TOKEN);
        String modelName = HardwareUtils.getModelName();
        String userAgent = VodUserAgent.getUserAgent(VodUserAgent.getMACAddress());
        String contentEncoding = "gzip";

        Retrofit retrofit = buildRetrofit();
        LogRequest logRequest = retrofit.create(LogRequest.class);
        Call<Empty> call = logRequest.doRequest(userAgent, contentEncoding,sn, deviceToken, accessToken, modelName, data);
        call.enqueue(new Callback<Empty>() {
            @Override
            public void onResponse(Response<Empty> response, Retrofit retrofit) {

            }

            @Override
            public void onFailure(Throwable throwable) {

            }
        });
    }

    interface LogRequest {
        @FormUrlEncoded
        @POST("/log")
        Call<Empty> doRequest(
                @Header("User-Agent") String userAgent,
                @Header("Content-Encoding") String contentEncoding,
                @Field("sn") String sn,
                @Field("deviceToken") String deviceToken,
                @Field("acessToken") String acessToken,
                @Field("modelname") String modelName,
                @Field("data") String data
        );
    }
}
