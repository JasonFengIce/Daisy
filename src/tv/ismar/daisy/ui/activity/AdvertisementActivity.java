package tv.ismar.daisy.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import cn.ismartv.speedtester.core.CdnCacheManager;
import cn.ismartv.speedtester.core.ClientApi;
import cn.ismartv.speedtester.data.json.CdnChangeTagEntity;
import cn.ismartv.speedtester.data.json.CdnListEntity;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import tv.ismar.daisy.AppConstant;
import tv.ismar.daisy.LauncherActivity;
import tv.ismar.daisy.R;
import tv.ismar.daisy.update.AppUpdateUtils;

/**
 * Created by huaijie on 3/10/15.
 */
public class AdvertisementActivity extends Activity {
    private static final String TAG = "AdvertisementActivity";
    private RestAdapter restAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advertisement);
        restAdapter = new RestAdapter.Builder()
                .setLogLevel(AppConstant.LOG_LEVEL)
                .setEndpoint(AppConstant.CDN_API_HOST)
                .build();
//        AppUpdateUtils.getInstance().fetchUpdate(this);

        Intent intent = new Intent(this, LauncherActivity.class);
        startActivity(intent);
//        fetchCdnChangeTag();
    }


    private void fetchCdnChangeTag() {
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
}
