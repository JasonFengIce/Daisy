package tv.ismar.daisy.core.client;

import retrofit.Callback;
import retrofit.http.GET;
import tv.ismar.daisy.core.update.VersionInfoEntity;

/**
 * Created by huaijie on 3/9/15.
 */
public class ClientApi {
    public interface AppVersionInfo {
        @GET("/api/upgrade/application/ismartvod/")
        void excute(
                Callback<VersionInfoEntity> callback
        );
    }
}
