package tv.ismar.daisy.core.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import tv.ismar.daisy.ui.activity.AdvertisementActivity;

/**
 * Created by huaijie on 3/18/15.
 */
public class PackageInstallReceiver extends BroadcastReceiver {
    private static final String TAG = "PackageInstallReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "PackageInstallReceiver...");
        Intent activityIntent = new Intent();
        activityIntent.setClass(context, AdvertisementActivity.class);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(activityIntent);
    }
}
