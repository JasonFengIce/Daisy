package tv.ismar.daisy.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import cn.ismartv.speedtester.core.CdnCacheManager;
import cn.ismartv.speedtester.core.ClientApi;
import cn.ismartv.speedtester.data.json.CdnChangeTagEntity;
import cn.ismartv.speedtester.data.json.CdnListEntity;
import com.squareup.picasso.Picasso;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import tv.ismar.daisy.AppConstant;
import tv.ismar.daisy.R;
import tv.ismar.daisy.core.advertisement.AdvertisementInfoEntity;
import tv.ismar.daisy.utils.DeviceUtils;

/**
 * Created by huaijie on 3/10/15.
 */
public class AdvertisementActivity extends Activity {
    private static final String TAG = "AdvertisementActivity";

    private ImageView adverPic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fetchAdvertisementInfo();
        setContentView(R.layout.activity_advertisement);
        adverPic = (ImageView) findViewById(R.id.advertisement_pic);


//        Intent intent = new Intent(this, LauncherActivity.class);
//        startActivity(intent);
//        fetchCdnChangeTag();
    }


    private void fetchCdnChangeTag() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(AppConstant.LOG_LEVEL)
                .setEndpoint(AppConstant.CDN_API_HOST)
                .build();
        ClientApi.CdnChangeTag client = restAdapter.create(ClientApi.CdnChangeTag.class);
        client.excute(new Callback<CdnChangeTagEntity>() {
            @Override
            public void success(CdnChangeTagEntity cdnChangeTagEntity, Response response) {
                fetchCdnList();
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.e(TAG, "fetchCdnChangeTag failed !!! ");
                Log.e(TAG, retrofitError.getMessage());
            }
        });
    }

    private void fetchCdnList() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(AppConstant.LOG_LEVEL)
                .setEndpoint(AppConstant.CDN_API_HOST)
                .build();
        ClientApi.CdnList client = restAdapter.create(ClientApi.CdnList.class);
        client.excute(new Callback<CdnListEntity>() {
            @Override
            public void success(CdnListEntity cdnListEntity, Response response) {
                CdnCacheManager.getInstance().saveCdnList(cdnListEntity);
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.e(TAG, "fetchCdnList failed !!! ");
                Log.e(TAG, retrofitError.getMessage());
            }
        });
    }

//    Picasso.with(AdvertisementActivity.this)
//            .load(ad)
//    .placeholder(R.drawable.preview).error(R.drawable.preview)
//    .into(homeImages[i]);

    private void fetchAdvertisementInfo() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(AppConstant.LOG_LEVEL)
                .setEndpoint(tv.ismar.daisy.core.client.ClientApi.ADVERTISEMENT_HOST)
                .build();
        tv.ismar.daisy.core.client.ClientApi.AdvertisementInfo client =
                restAdapter.create(tv.ismar.daisy.core.client.ClientApi.AdvertisementInfo.class);
        String deviceId = DeviceUtils.getDeviceSN();

        client.excute(deviceId, new Callback<AdvertisementInfoEntity>() {
            @Override
            public void success(AdvertisementInfoEntity advertisementInfoEntity, Response response) {
                String url = advertisementInfoEntity.getUrl();
                Log.i(TAG, "fetchAdvertisementInfo: adver pic url ---> " + url);
                Picasso.with(AdvertisementActivity.this)
                        .load(url)
                        .placeholder(R.drawable.preview)
                        .error(R.drawable.preview)
                        .into(adverPic);
            }

            @Override
            public void failure(RetrofitError retrofitError) {

            }
        });
    }
}
