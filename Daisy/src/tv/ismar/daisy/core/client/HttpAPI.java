package tv.ismar.daisy.core.client;


import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
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

}
