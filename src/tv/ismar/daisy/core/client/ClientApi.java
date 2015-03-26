package tv.ismar.daisy.core.client;

import com.ismartv.launcher.data.ChannelEntity;
import com.ismartv.launcher.data.VideoEntity;
import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.Path;
import retrofit.http.Query;
import tv.ismar.daisy.core.advertisement.AdvertisementInfoEntity;
import tv.ismar.daisy.core.update.VersionInfoEntity;
import tv.ismar.daisy.models.launcher.AttributeEntity;
import tv.ismar.daisy.models.launcher.FrontPageEntity;

import java.util.ArrayList;

/**
 * Created by huaijie on 3/9/15.
 */
public class ClientApi {

    public static final String APP_UPDATE_HOST = "http://client.tvxio.com";


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
        @GET("/api/tv/frontpage/")
        void excute(@Query("device_token") String deviceToken,
                    Callback<FrontPageEntity> callback
        );
    }

    /**
     * linkedvideo
     */

    public interface Linkedvideo {
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
        @GET("/api/tv/section/tvhome/")
        void excute(
                @Query("device_token") String deviceToken,
                Callback<VideoEntity> callback
        );

    }

}
