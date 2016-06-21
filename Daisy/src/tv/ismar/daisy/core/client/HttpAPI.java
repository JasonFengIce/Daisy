package tv.ismar.daisy.core.client;


import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;
import tv.ismar.daisy.data.ChannelEntity;
import tv.ismar.daisy.data.HomePagerEntity;
import tv.ismar.daisy.data.http.ItemEntity;

/**
 * Created by huaijie on 1/18/16.
 */
public class HttpAPI {

    public interface WeatherAPI {
        @GET("/{geoId}.xml")
        Call<ResponseBody> doRequest(
                @Path("geoId") String geoId
        );
    }

    public interface ApiItem {
        @GET("api/item/{itemId}/")
        Call<ItemEntity> doRequest(
                @Path("itemId") String itemId
        );
    }

    public interface OrderCreate {
        @FormUrlEncoded
        @POST("api/order/create/")
        Call<ResponseBody> doRequest(
                @Field("device_token") String deviceToken,
                @Field("wares_id") String waresId,
                @Field("wares_type") String waresType,
                @Field("source") String source
        );
    }

    public interface TvChannels {
        @Headers("Cache-Control: public, max-age=3600")
        @GET("api/tv/channels/")
        Call<ChannelEntity[]> doRequest();
    }


    public interface TvHomepageTop {
        @Headers("Cache-Control: public, max-age=3600")
        @GET("api/tv/homepage/top/")
        Call<HomePagerEntity> doRequest();
    }

}
