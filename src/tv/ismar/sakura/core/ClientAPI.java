package tv.ismar.sakura.core;

import retrofit.Callback;
import retrofit.http.GET;
import tv.ismar.sakura.data.http.CdnListEntity;

/**
 * Created by huaijie on 2015/4/7.
 */
public class ClientAPI {

    public static final String API_HOST = "http://wx.api.tvxio.com/";

    /**
     * cdn list
     */
    public interface CdnList {
        @GET("/shipinkefu/getCdninfo?actiontype=getcdnlist")
        void execute(Callback<CdnListEntity> callback);
    }


}
