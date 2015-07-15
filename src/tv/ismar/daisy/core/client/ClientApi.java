package tv.ismar.daisy.core.client;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.Query;
import tv.ismar.daisy.AppConstant;
import tv.ismar.daisy.core.advertisement.AdvertisementInfoEntity;
import tv.ismar.daisy.core.update.VersionInfoEntity;
import tv.ismar.daisy.data.ChannelEntity;
import tv.ismar.daisy.data.HomePagerEntity;

import java.util.ArrayList;

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

    public interface Homepage {
        @GET("/api/tv/homepage/top/")
        void excute(
                @Query("device_token") String deviceToken,
                Callback<HomePagerEntity> callback
        );
    }

    public interface ChineseMovie {
        @GET("/api/tv/homepage/chinesemovie/")
        void excute(
                @Query("access_token") String accessToken,
                @Query("device_token") String deviceToken,
                Callback<HomePagerEntity> callback
        );
    }

    public interface Overseas {
        @GET("/api/tv/homepage/overseas/")
        void excute(
                @Query("access_token") String accessToken,
                @Query("device_token") String deviceToken,
                Callback<HomePagerEntity> callback
        );
    }

    public interface Teleplay {
        @GET("/api/tv/homepage/teleplay/")
        void excute(
                @Query("access_token") String accessToken,
                @Query("device_token") String deviceToken,
                Callback<HomePagerEntity> callback
        );
    }

    public interface Child {
        @GET("/api/tv/homepage/comic/")
        void excute(
                @Query("access_token") String accessToken,
                @Query("device_token") String deviceToken,
                Callback<HomePagerEntity> callback
        );
    }

    public interface AppVersionInfo {
        @GET("/api/upgrade/application/ismartvod/")
        void excute(
                Callback<VersionInfoEntity> callback
        );
    }

    /**
     * advertisement
     */
    public interface AdvertisementInfo {
        @Headers("Accpt: application/json")
        @GET("/api/power/")
        void excute(
                @Query("device_id") String deviceId,
                Callback<ArrayList<AdvertisementInfoEntity>> callback
        );
    }

    /**
     * channel
     */

    public interface Channels {
        @Headers({"Accpt: application/json", "Cache-Control: no-cache"})
        @GET("/api/tv/channels/")
        void excute(
                @Query("device_token") String deviceToken,
                Callback<ChannelEntity[]> callback
        );
    }

}
