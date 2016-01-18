package tv.ismar.daisy.core.client;

import com.squareup.okhttp.ResponseBody;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Path;

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
}
