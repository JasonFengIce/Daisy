package tv.ismar.sakura.core;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Query;
import tv.ismar.daisy.AppConstant;
import tv.ismar.sakura.data.http.BindedCdnEntity;
import tv.ismar.sakura.data.http.CdnListEntity;
import tv.ismar.sakura.data.http.Empty;

/**
 * Created by huaijie on 2015/4/7.
 */
public class SakuraClientAPI {
    public static final RestAdapter restAdapter_WX_API_TVXIO;

    static {
        restAdapter_WX_API_TVXIO = new RestAdapter.Builder()
                .setLogLevel(AppConstant.LOG_LEVEL)
                .setEndpoint(SakuraClientAPI.API_HOST)
                .build();
    }

    public static final String API_HOST = "http://wx.api.tvxio.com/";

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
                @Query("cdn") String cdnNumber,
                Callback<Empty> callback
        );
    }

}
