package tv.ismar.sakura.core;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.http.*;
import tv.ismar.daisy.AppConstant;
import tv.ismar.sakura.data.http.*;

import java.util.List;

/**
 * Created by huaijie on 2015/4/7.
 */
public class SakuraClientAPI {
    public static final RestAdapter restAdapter_WX_API_TVXIO;
    public static final RestAdapter restAdapter_IRIS_TVXIO;
    public static final RestAdapter restAdapter_SPEED_CALLA_TVXIO;
    public static final RestAdapter restAdapter_LILY_TVXIO_HOST;

    public static final String API_HOST = "http://wx.api.tvxio.com/";
    private static final String IRIS_TVXIO_HOST = "http://iris.tvxio.com";
    private static final String SPEED_CALLA_TVXIO_HOST = "http://speed.calla.tvxio.com";
    private static final String LILY_TVXIO_HOST = "http://lily.tvxio.com";


    static {
        restAdapter_WX_API_TVXIO = new RestAdapter.Builder()
                .setLogLevel(AppConstant.LOG_LEVEL)
                .setEndpoint(SakuraClientAPI.API_HOST)
                .build();
        restAdapter_IRIS_TVXIO = new RestAdapter.Builder()
                .setLogLevel(AppConstant.LOG_LEVEL)
                .setEndpoint(SakuraClientAPI.IRIS_TVXIO_HOST)
                .build();
        restAdapter_SPEED_CALLA_TVXIO = new RestAdapter.Builder()
                .setLogLevel(AppConstant.LOG_LEVEL)
                .setEndpoint(SPEED_CALLA_TVXIO_HOST)
                .build();
        restAdapter_LILY_TVXIO_HOST = new RestAdapter.Builder()
                .setLogLevel(AppConstant.LOG_LEVEL)
                .setEndpoint(LILY_TVXIO_HOST)
                .build();

    }

    public interface Problems {
        @GET("/customer/points/")
        void excute(
                Callback<List<ProblemEntity>> callback
        );
    }

    public interface Feedback {
        @GET("/customer/getfeedback/")
        void excute(
                @Query("sn") String sn,
                @Query("topn") String topn,
                Callback<ChatMsgEntity> callback
        );
    }


    public interface GetBindCdn {
        @GET("/shipinkefu/getCdninfo?actiontype=getBindcdn")
        void excute(
                @Query("sn") String snCode,
                Callback<BindedCdnEntity> callback
        );
    }

    public interface BindCdn {
        @GET("/shipinkefu/getCdninfo?actiontype=bindecdn")
        void excute(
                @Query("sn") String snCode,
                @Query("cdn") int cdnId,
                Callback<Empty> callback
        );
    }

    /**
     * UnbindNode
     */
    public interface UnbindNode {
        @GET("/shipinkefu/getCdninfo?actiontype=unbindCdn")
        void excute(
                @Query("sn") String sn,
                Callback<Empty> callback
        );
    }


    public interface FetchTel {
        public String ACTION = "getContact";

        @GET("/shipinkefu/getCdninfo")
        void excute(
                @Query("actiontype") String actiontype,
                @Query("ModeName") String modeName,
                @Query("sn") String sn,
                Callback<List<TeleEntity>> callback
        );
    }


    public interface DeviceLog {
        @GET("/log")
        void execute(
                @Query("data") String data,
                @Query("sn") String sn,
                @Query("modelname") String modelName,
                Callback<Empty> callback
        );
    }

    public interface UploadResult {
        public static final String ACTION_TYPE = "submitTestData";

        @FormUrlEncoded
        @POST("/shipinkefu/getCdninfo")
        void excute(
                @Field("actiontype") String actionType,
                @Field("snCode") String snCode,
                @Field("nodeId") String nodeId,
                @Field("nodeSpeed") String nodeSpeed,
                Callback<Empty> callback
        );
    }

}
