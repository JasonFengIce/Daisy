package tv.ismar.daisy.core.vlc;

import android.content.Context;
import android.util.Log;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.util.VLCUtil;

/**
 * Created by huaijie on 8/12/15.
 */
public class VLCInstance {

    public final static String TAG = "VLC/Util/VLCInstance";

    private static LibVLC sLibVLC = null;

    /**
     * A set of utility functions for the VLC application
     */
    public synchronized static LibVLC get() throws IllegalStateException {
        if (sLibVLC == null) {
            Thread.setDefaultUncaughtExceptionHandler(new VLCCrashHandler());

            final Context context = SampleApplication.getAppContext();
            if (!VLCUtil.hasCompatibleCPU(context)) {
                Log.e(TAG, VLCUtil.getErrorMsg());
                throw new IllegalStateException("LibVLC initialisation failed: " + VLCUtil.getErrorMsg());
            }

            sLibVLC = new LibVLC(VLCOptions.getLibOptions(context));
            LibVLC.setOnNativeCrashListener(new LibVLC.OnNativeCrashListener() {
                @Override
                public void onNativeCrash() {
                    Log.e(TAG, "onNativeCrash");
//                    Intent i = new Intent(context, NativeCrashActivity.class);
//                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    i.putExtra("PID", android.os.Process.myPid());
//                    context.startActivity(i);
                }
            });
        }
        return sLibVLC;
    }

    public static synchronized void restart(Context context) throws IllegalStateException {
        if (sLibVLC != null) {
            sLibVLC.release();
            sLibVLC = new LibVLC(VLCOptions.getLibOptions(context));
        }
    }

    public static synchronized boolean testCompatibleCPU(Context context) {
        if (sLibVLC == null && !VLCUtil.hasCompatibleCPU(context)) {
//            final Intent i = new Intent(context, CompatErrorActivity.class);
//            context.startActivity(i);
            Log.e(TAG, "CompatibleCPU Error");
            return false;
        } else
            return true;
    }

}
