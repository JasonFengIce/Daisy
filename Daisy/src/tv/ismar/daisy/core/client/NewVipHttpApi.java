package tv.ismar.daisy.core.client;


import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import tv.ismar.daisy.data.http.newvip.PayLayerEntity;

/**
 * Created by huaijie on 4/11/16.
 */
public class NewVipHttpApi {
    public interface PayLayer {
        @GET("api/paylayer/{item_id}/")
        Call<PayLayerEntity> doRequest(
                @Path("item_id") String itemId
        );
    }
}
