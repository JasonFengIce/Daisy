package cn.ismartv.speedtester.core;

import cn.ismartv.speedtester.data.json.CdnChangeTagEntity;
import cn.ismartv.speedtester.data.json.CdnListEntity;
import retrofit.Callback;
import retrofit.http.GET;

/**
 * Created by huaijie on 3/10/15.
 */
public class ClientApi {

    public interface CdnChangeTag {
        @GET("/shipinkefu/getCdninfo?actiontype=gettag")
        void excute(
                Callback<CdnChangeTagEntity> callback
        );
    }

    public interface CdnList {

        @GET("/shipinkefu/getCdninfo?actiontype=getcdnlist")
        void excute(
                Callback<CdnListEntity> callback
        );
    }
}
