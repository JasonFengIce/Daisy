package cn.ismartv.activator.core.http;


import cn.ismartv.activator.data.Result;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by huaijie on 1/14/16.
 */
public class HttpClientAPI {


    public interface ExcuteActivator {
        @Headers({
                "Pragma: no-cache",
                "Cache-Control: no-cache"
        })
        @FormUrlEncoded
        @POST("/trust/security/active/")
        Call<Result> excute(
                @Field("sn") String sn,
                @Field("manufacture") String manufacture,
                @Field("kind") String kind,
                @Field("version") String version,
                @Field("sign") String sign,
                @Field("fingerprint") String fingerprint,
                @Field("api_version") String api_version,
                @Field("info") String deviceInfo
        );
    }

    public interface GetLicence {
        @FormUrlEncoded
        @POST("/trust/get_licence/")
        Call<ResponseBody> excute(
                @Field("fingerprint") String fingerprint,
                @Field("sn") String sn,
                @Field("manufacture") String manufacture,
                @Field("code") String code
        );
    }

}
