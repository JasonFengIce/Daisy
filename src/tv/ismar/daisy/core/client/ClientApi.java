package tv.ismar.daisy.core.client;

import com.ismartv.launcher.data.ChannelEntity;
import com.ismartv.launcher.data.VideoEntity;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.Path;
import retrofit.http.Query;
import tv.ismar.daisy.AppConstant;
import tv.ismar.daisy.core.advertisement.AdvertisementInfoEntity;
import tv.ismar.daisy.core.update.VersionInfoEntity;
import tv.ismar.daisy.models.launcher.*;
import tv.ismar.daisy.data.HomePagerEntity;

import java.util.ArrayList;

/**
 * Created by huaijie on 3/9/15.
 */
public class ClientApi {

    public static final String APP_UPDATE_HOST = "http://client.tvxio.com";

    public static final String SKYTEST_TVXIO_HOST = "http://skytest.tvxio.com";

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
                @Query("access_token") String accessToken,
                @Query("device_token") String deviceToken,
                Callback<HomePagerEntity> callback
        );
    }

    public interface OverSeas {
        @GET("/api/tv/homepage/overseas/")
        void excute(
                @Query("device_token") String deviceToken,
                Callback<HomePagerEntity> callback
        );
    }


    public interface Child {
        @GET("/api/tv/homepage/comic/")
        void excute(
                @Query("device_token") String deviceToken,
                Callback<HomePagerEntity> callback
        );
    }

    public interface Chinese {

    }

    public interface Drama {

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
     * frontpage
     */
    public interface Frontpage {
        @Headers({"Accpt: application/json", "Cache-Control: no-cache"})
        @GET("/api/tv/frontpage/")
        void excute(@Query("device_token") String deviceToken,
                    Callback<FrontPageEntity> callback
        );
    }

    /**
     * linkedvideo
     */
    public interface Linkedvideo {
        @Headers({"Accpt: application/json", "Cache-Control: no-cache"})
        @GET("/api/tv/linkedvideo/{video_id}/")
        void excute(
                @Path("video_id") long videoId,
                @Query("device_token") String deviceToken,
                Callback<ArrayList<AttributeEntity>> callback
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

    /**
     * HorizontalGuide
     */
    public interface HorizontalGuide {
        @Headers({"Accpt: application/json", "Cache-Control: no-cache"})
        @GET("/api/tv/section/tvhome/")
        void excute(
                @Query("device_token") String deviceToken,
                Callback<VideoEntity> callback
        );

    }


    public interface GeoId {
        public static final String HOST = "http://media.lily.tvxio.com";

        @GET("/geoid.json")
        void excute(Callback<ArrayList<GeoIdEntity>> callback);

    }

    public interface IpLookup {
        public static final String HOST = "http://lily.tvxio.com";

        @GET("/iplookup")
        void excute(Callback<IpLookupEntity> callback);

    }

    public interface Weather {
        public static final String HOST = "http://media.lily.tvxio.com";

        @GET("/{geoid}.json")
        void excute(
                @Path("geoid") String geoid,
                Callback<WeatherEntity> callback
        );
    }


}
