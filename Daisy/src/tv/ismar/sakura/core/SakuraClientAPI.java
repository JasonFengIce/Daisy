package tv.ismar.sakura.core;


import java.util.List;

import cn.ismartv.log.interceptor.HttpLoggingInterceptor;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import tv.ismar.sakura.data.http.BindedCdnEntity;
import tv.ismar.sakura.data.http.ChatMsgEntity;
import tv.ismar.sakura.data.http.Empty;
import tv.ismar.sakura.data.http.ProblemEntity;
import tv.ismar.sakura.data.http.TeleEntity;

/**
 * Created by huaijie on 2015/4/7.
 */
public class SakuraClientAPI {
    public static final Retrofit restAdapter_WX_API_TVXIO;
    public static final Retrofit restAdapter_IRIS_TVXIO;
    public static final Retrofit restAdapter_SPEED_CALLA_TVXIO;
    public static final Retrofit restAdapter_LILY_TVXIO_HOST;

    public static final String API_HOST = "http://wx.api.tvxio.com/";
    private static final String IRIS_TVXIO_HOST = "http://iris.tvxio.com";
    private static final String SPEED_CALLA_TVXIO_HOST = "http://speed.calla.tvxio.com";
    private static final String LILY_TVXIO_HOST = "http://lily.tvxio.com";


    static {
        OkHttpClient client = new OkHttpClient();
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        client.interceptors().add(interceptor);
        restAdapter_WX_API_TVXIO = new Retrofit.Builder()
                .client(client)
                .baseUrl(SakuraClientAPI.API_HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        restAdapter_IRIS_TVXIO = new Retrofit.Builder()
                .client(client)
                .baseUrl(SakuraClientAPI.IRIS_TVXIO_HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        restAdapter_SPEED_CALLA_TVXIO = new Retrofit.Builder()
                .client(client)
                .baseUrl(SakuraClientAPI.SPEED_CALLA_TVXIO_HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        restAdapter_LILY_TVXIO_HOST = new Retrofit.Builder()
                .client(client)
                .baseUrl(SakuraClientAPI.LILY_TVXIO_HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

    }

    public interface Problems {
        @GET("/customer/points/")
        Call<List<ProblemEntity>> excute(

        );
    }

    public interface Feedback {
        @GET("/customer/getfeedback/")
        Call<ChatMsgEntity> excute(
                @Query("sn") String sn,
                @Query("topn") String topn

        );
    }


    public interface GetBindCdn {
        @GET("/shipinkefu/getCdninfo?actiontype=getBindcdn")
        Call<BindedCdnEntity> excute(
                @Query("sn") String snCode
        );
    }

    public interface BindCdn {
        @GET("/shipinkefu/getCdninfo?actiontype=bindecdn")
        Call<Empty> excute(
                @Query("sn") String snCode,
                @Query("cdn") int cdnId
        );
    }

    /**
     * UnbindNode
     */
    public interface UnbindNode {
        @GET("/shipinkefu/getCdninfo?actiontype=unbindCdn")
        Call<Empty> excute(
                @Query("sn") String sn

        );
    }


    public interface FetchTel {
        public String ACTION = "getContact";

        @GET("/shipinkefu/getCdninfo")
        Call<List<TeleEntity>> excute(
                @Query("actiontype") String actiontype,
                @Query("ModeName") String modeName,
                @Query("sn") String sn

        );
    }


    public interface DeviceLog {
        @GET("/log")
        Call<Empty> execute(
                @Query("data") String data,
                @Query("sn") String sn,
                @Query("modelname") String modelName

        );
    }

    public interface UploadResult {
        String ACTION_TYPE = "submitTestData";

        @FormUrlEncoded
        @POST("/shipinkefu/getCdninfo")
        Call<Empty> excute(
                @Field("actiontype") String actionType,
                @Field("snCode") String snCode,
                @Field("nodeId") String nodeId,
                @Field("nodeSpeed") String nodeSpeed
        );
    }

}
