package ismartv.test;

import android.test.AndroidTestCase;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;

import java.io.IOException;

import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;
import retrofit.http.GET;
import tv.ismar.sakura.data.http.BindedCdnEntity;

/**
 * Created by huaijie on 1/15/16.
 */
public class HttpTest extends AndroidTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        testUrl();
    }

    public void testUrl() {
        OkHttpClient client = new OkHttpClient();
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        client.interceptors().add(interceptor);
        Retrofit restAdapter_WX_API_TVXIO = new Retrofit.Builder()
                .client(client)
                .baseUrl("http://www.baidu.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        Test t = restAdapter_WX_API_TVXIO.create(Test.class);
        try {
            Response<BindedCdnEntity> responseBodyResponse = t.test().execute();
            Log.i("t", String.valueOf(responseBodyResponse.body().getSncdn()));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    interface Test {
        @GET("/")
        Call<BindedCdnEntity> test(

        );

    }
}
