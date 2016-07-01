package tv.ismar.daisy.core.client;


import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;
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
                @Query("item_id") String itemId,
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


    public interface OrderCheck {
        @FormUrlEncoded
        @POST("api/play/check/")
        Call<ResponseBody> doRequest(
                @Field("item") String item,
                @Field("package") String pkg,
                @Field("subitem") String subItem,
                @Field("device_token") String deviceToken,
                @Field("access_token") String accessToken
        );
    }

    public interface PlayCheck {
        @FormUrlEncoded
        @POST("api/play/check/")
        Call<ResponseBody> doRequest(
                @Field("item") String item,
                @Field("package") String pkg,
                @Field("subitem") String subItem,
                @Field("device_token") String deviceToken,
                @Field("access_token") String accessToken
        );
    }

    public interface OrderPurchase {
        @FormUrlEncoded
        @POST("api/order/purchase/")
        Call<ResponseBody> doRequest(
                @Field("item") String item,
                @Field("package") String pkg,
                @Field("subitem") String subItem,
                @Field("device_token") String deviceToken,
                @Field("access_token") String accessToken
        );
    }
}
