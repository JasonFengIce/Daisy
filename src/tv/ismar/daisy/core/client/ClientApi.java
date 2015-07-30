package tv.ismar.daisy.core.client;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.Query;
import tv.ismar.daisy.AppConstant;
import tv.ismar.daisy.core.update.VersionInfoEntity;
import tv.ismar.daisy.data.ChannelEntity;
import tv.ismar.daisy.data.HomePagerEntity;

/**
 * Created by huaijie on 3/9/15.
 */
public class ClientApi {

    public static final String APP_UPDATE_HOST = "http://client.tvxio.com";

    public static final String SKYTEST_TVXIO_HOST = "http://v2.sky.tvxio.com/";

    public static final RestAdapter restAdapter_SKYTEST_TVXIO;

    static {
        restAdapter_SKYTEST_TVXIO = new RestAdapter.Builder()
                .setLogLevel(AppConstant.LOG_LEVEL)
                .setEndpoint(SKYTEST_TVXIO_HOST)
                .build();
    }

    public interface AppVersionInfo {
        @GET("/api/upgrade/application/ismartvod/")
        void excute(
                Callback<VersionInfoEntity> callback
        );
    }
}
