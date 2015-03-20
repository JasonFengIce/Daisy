
package tv.ismar.daisy.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import tv.ismar.daisy.core.update.AppUpdateUtils;

/**
 * Created by huaijie on 3/19/15.
 */
public class SelectorActivty extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent();
        if (AppUpdateUtils.getInstance().getUpdatePreferences(this)) {
            intent.setClass(this, AppUpdateActivity.class);
        } else {
            intent.setClass(this, AdvertisementActivity.class);
        }
        startActivity(intent);
        finish();
    }


}
