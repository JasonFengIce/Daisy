package tv.ismar.daisy.core.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import tv.ismar.daisy.update.AppUpdateUtils;

/**
 * Created by huaijie on 3/10/15.
 */
public class PlayCompleteReveiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        AppUpdateUtils.getInstance().installApk(context);
    }
}
