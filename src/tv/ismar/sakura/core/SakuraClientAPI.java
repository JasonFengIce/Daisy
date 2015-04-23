package tv.ismar.sakura.core;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Query;
import tv.ismar.daisy.AppConstant;
import tv.ismar.sakura.data.http.*;

import java.util.List;

/**
 * Created by huaijie on 2015/4/7.
 */
public class SakuraClientAPI {
    public static final RestAdapter restAdapter_WX_API_TVXIO;
    public static final RestAdapter restAdapter_IRIS_TVXIO;

    public static final String API_HOST = "http://wx.api.tvxio.com/";
    private static final String IRIS_TVXIO_HOST = "http://iris.tvxio.com";


    static {
        restAdapter_WX_API_TVXIO = new RestAdapter.Builder()
                .setLogLevel(AppConstant.LOG_LEVEL)
                .setEndpoint(SakuraClientAPI.API_HOST)
                .build();
        restAdapter_IRIS_TVXIO = new RestAdapter.Builder()
                .setLogLevel(AppConstant.LOG_LEVEL)
                .setEndpoint(SakuraClientAPI.IRIS_TVXIO_HOST)
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


    /**
     * cdn list
     */
    public interface CdnList {
        @GET("/shipinkefu/getCdninfo?actiontype=getcdnlist")
        void execute(Callback<CdnListEntity> callback);
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

}
