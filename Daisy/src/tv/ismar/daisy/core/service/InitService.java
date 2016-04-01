package tv.ismar.daisy.core.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import cn.ismartv.activator.Activator;
import cn.ismartv.activator.data.Result;
import tv.ismar.daisy.VodApplication;
import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.VodUserAgent;
import tv.ismar.daisy.core.initialization.InitializeProcess;
import tv.ismar.daisy.core.preferences.AccountSharedPrefs;

public class InitService extends Service implements Activator.OnComplete {

    private ConnectionChangeReceiver mConnectivityReceiver;
    private String product;
    private String mode;
    private Activator activator;
    private String localInfo;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        activator = Activator.getInstance(this);
        activator.setOnCompleteListener(this);
        localInfo = DaisyUtils.getVodApplication(this).getPreferences()
                .getString(VodApplication.LOCATION_INFO, "");
        product = Build.BRAND.replace(" ", "_");
        mode = VodUserAgent.getModelName();
        getHardInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            activator.active(product, mode,
                    String.valueOf(SimpleRestClient.appVersion), localInfo);
            Log.v("InitService", "InitService started");
            new Thread(new InitializeProcess(this)).start();
        } else {
            registerNetStateReceiver();
        }
    }

    @Override
    public void onFailed(String arg0) {

    }

    @Override
    public void onSuccess(Result result) {
        saveActivedInfo(result);
        saveSimpleRestClientPreferences(this, result);
        DaisyUtils.getVodApplication(this).getNewContentModel();
    }

    private void saveActivedInfo(Result result) {
        AccountSharedPrefs accountSharedPrefs = AccountSharedPrefs
                .getInstance();
        accountSharedPrefs.setSharedPrefs(AccountSharedPrefs.APP_UPDATE_DOMAIN,
                result.getUpgrade_domain());
        accountSharedPrefs.setSharedPrefs(AccountSharedPrefs.LOG_DOMAIN,
                result.getLog_Domain());
        accountSharedPrefs.setSharedPrefs(AccountSharedPrefs.API_DOMAIN,
                result.getDomain());
        accountSharedPrefs.setSharedPrefs(
                AccountSharedPrefs.ADVERTISEMENT_DOMAIN, result.getAd_domain());

        accountSharedPrefs.setSharedPrefs(AccountSharedPrefs.DEVICE_TOKEN,
                result.getDevice_token());
        accountSharedPrefs.setSharedPrefs(AccountSharedPrefs.SN_TOKEN,
                result.getSn_Token());
    }

    private void saveSimpleRestClientPreferences(Context context, Result result) {
        SimpleRestClient.root_url = "http://" + result.getDomain();
        SimpleRestClient.sRoot_url = "http://" + result.getDomain();
        SimpleRestClient.ad_domain = "http://" + result.getAd_domain();
        SimpleRestClient.log_domain = "http://" + result.getLog_Domain();
        SimpleRestClient.device_token = result.getDevice_token();
        SimpleRestClient.upgrade_domain = result.getUpgrade_domain();
        DaisyUtils
                .getVodApplication(context)
                .getEditor()
                .putString(VodApplication.DEVICE_TOKEN,
                        SimpleRestClient.device_token);
        DaisyUtils.getVodApplication(context).save();
        SimpleRestClient.sn_token = result.getSn_Token();
        SimpleRestClient.mobile_number = DaisyUtils.getVodApplication(this)
                .getPreferences().getString(VodApplication.MOBILE_NUMBER, "");
        SimpleRestClient.access_token = DaisyUtils.getVodApplication(this)
                .getPreferences().getString(VodApplication.AUTH_TOKEN, "");
    }

    /*
     * 手动注册网络状态变化
     */
    private void registerNetStateReceiver() {
        mConnectivityReceiver = new ConnectionChangeReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(android.net.ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mConnectivityReceiver, filter);
    }

    /*
     * 注销网络监听
     */
	private void unregisterNetStateReceiver() {
		try {
			unregisterReceiver(mConnectivityReceiver);
		} catch (java.lang.IllegalArgumentException e) {
		}
	}

    public class ConnectionChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo networkInfo = connectivityManager
                    .getActiveNetworkInfo();

            if (networkInfo != null) {
                unregisterNetStateReceiver();
                if (networkInfo.isConnected()) {
                    activator.active(product, mode,
                            String.valueOf(SimpleRestClient.appVersion),
                            localInfo);
                    Log.v("InitService", "InitService started");
                    new Thread(new InitializeProcess(InitService.this)).start();
                }
            } else {
            }
        }
    }

    private void getHardInfo() {
        PackageManager manager = getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
            SimpleRestClient.appVersion = info.versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}
