package tv.ismar.daisy.core.client;


import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import tv.ismar.daisy.data.http.newvip.paylayer.PayLayerEntity;
import tv.ismar.daisy.data.http.newvip.paylayerpackage.PayLayerPackageEntity;
import tv.ismar.daisy.data.http.newvip.paylayervip.PayLayerVipEntity;

/**
 * Created by huaijie on 4/11/16.
 */
public class NewVipHttpApi {
    public interface PayLayer {
        @GET("api/paylayer/{item_id}/")
        Call<PayLayerEntity> doRequest(
                @Path("item_id") String itemId,
                @Query("device_token") String deviceToken
        );
    }

    public interface PayLayerVip {
        @GET("api/paylayer/vip/{cpid}/")
        Call<PayLayerVipEntity> doRequest(
                @Path("cpid") String cpid,
                @Query("device_token") String deviceToken
        );
    }

    public interface PayLayerPack {
        @GET("api/paylayer/package/{package_id}/")
        Call<PayLayerPackageEntity> doRequest(
                @Path("package_id") String packageId,
                @Query("device_token") String deviceToken
        );
    }
}
