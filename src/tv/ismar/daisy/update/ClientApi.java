package tv.ismar.daisy.update;

import retrofit.Callback;
import retrofit.http.GET;

/**
 * Created by huaijie on 3/9/15.
 */
public class ClientApi {

    public interface AppVersionInfo {
        @GET("/shipinkefu/getCdninfo?actiontype=getLatestAppVersion")
        void excute(
                Callback<VersionInfoEntity> callback
        );
    }
}
