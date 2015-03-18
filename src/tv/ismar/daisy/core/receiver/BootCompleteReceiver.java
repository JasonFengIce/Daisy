package tv.ismar.daisy.core.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import tv.ismar.daisy.core.update.AppUpdateUtils;

/**
 * Created by huaijie on 3/12/15.
 */
public class BootCompleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
//        AppUpdateUtils.getInstance().modifyUpdatePreferences(context, true);
    }
}
