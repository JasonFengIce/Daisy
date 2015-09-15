package tv.ismar.daisy.core.service;

import static tv.ismar.daisy.AppConstant.KIND;
import static tv.ismar.daisy.AppConstant.MANUFACTURE;
import tv.ismar.daisy.VodApplication;
import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.initialization.InitializeProcess;
import tv.ismar.daisy.core.preferences.AccountSharedPrefs;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import cn.ismartv.activator.Activator;
import cn.ismartv.activator.data.Result;

public class InitService extends Service implements Activator.OnComplete {

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Activator activator = Activator.getInstance(this);
		activator.setOnCompleteListener(this);
		String localInfo = DaisyUtils.getVodApplication(this).getPreferences()
				.getString(VodApplication.LOCATION_INFO, "");
		activator.active(MANUFACTURE, KIND,
				String.valueOf(SimpleRestClient.appVersion), localInfo);
		Log.v("InitService", "InitService started");
		new Thread(new InitializeProcess(this)).start();
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
				.getInstance(this);
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

		accountSharedPrefs.setSharedPrefs(AccountSharedPrefs.PACKAGE_INFO,
				result.getPackageInfo());
		accountSharedPrefs.setSharedPrefs(AccountSharedPrefs.EXPIRY_DATE,
				result.getExpiry_date());

	}

	private void saveSimpleRestClientPreferences(Context context, Result result) {
		SimpleRestClient.root_url = "http://" + result.getDomain();
		SimpleRestClient.sRoot_url = "http://" + result.getDomain();
		SimpleRestClient.ad_domain = "http://" + result.getAd_domain();
		SimpleRestClient.log_domain = "http://" + result.getLog_Domain();
		SimpleRestClient.device_token = result.getDevice_token();
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
}
