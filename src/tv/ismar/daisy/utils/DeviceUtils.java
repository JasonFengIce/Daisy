package tv.ismar.daisy.utils;

import android.os.Build;

/**
 * Created by huaijie on 3/12/15.
 */
public class DeviceUtils {
    public static final String getDeviceSN() {
        return Build.SERIAL;
    }
}
