package tv.ismar.daisy.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import tv.ismar.daisy.R;
import tv.ismar.daisy.core.update.AppUpdateUtils;

import java.io.File;

/**
 * Created by huaijie on 3/19/15.
 */
public class AppUpdateActivity extends Activity {
    private static final String TAG = "AppUpdateActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_update);
        File apkFile = new File(getFilesDir().getAbsolutePath(), AppUpdateUtils.SELF_APP_NAME);
        if (AppUpdateUtils.getInstance().getUpdatePreferences(this) && apkFile.exists()) {
            AppUpdateUtils.getInstance().modifyUpdatePreferences(this, false);
            AppUpdateUtils.getInstance().execCmd("adb connect 127.0.0.1:5555");
            AppUpdateUtils.getInstance().execCmd("adb -s 127.0.0.1:5555 install -r " + apkFile.getAbsolutePath());
        }
    }
}
