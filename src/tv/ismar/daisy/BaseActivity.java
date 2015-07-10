package tv.ismar.daisy;

import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.SimpleRestClient;
import android.app.Activity;
import android.os.Bundle;

public class BaseActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (SimpleRestClient.root_url.equals("")) {
            SimpleRestClient.root_url = DaisyUtils.getVodApplication(this).getPreferences().getString(VodApplication.DOMAIN, "");
            SimpleRestClient.sRoot_url = DaisyUtils.getVodApplication(this).getPreferences().getString(VodApplication.DOMAIN, "");
            SimpleRestClient.ad_domain = DaisyUtils.getVodApplication(this).getPreferences().getString(VodApplication.ad_domain, "");
            SimpleRestClient.log_domain = DaisyUtils.getVodApplication(this).getPreferences().getString(VodApplication.LOG_DOMAIN, "");
            SimpleRestClient.device_token = DaisyUtils.getVodApplication(this).getPreferences().getString(VodApplication.DEVICE_TOKEN, "");
            SimpleRestClient.sn_token = DaisyUtils.getVodApplication(this).getPreferences().getString(VodApplication.SN_TOKEN, "");
            SimpleRestClient.mobile_number = DaisyUtils.getVodApplication(this).getPreferences().getString(VodApplication.MOBILE_NUMBER, "");
            SimpleRestClient.access_token = DaisyUtils.getVodApplication(this).getPreferences().getString(VodApplication.AUTH_TOKEN, "");
        }
    }
}
