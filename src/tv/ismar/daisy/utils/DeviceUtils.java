package tv.ismar.daisy.utils;

import android.content.Context;
import android.os.Build;
import android.os.Environment;

import java.io.File;

/**
 * Created by huaijie on 3/12/15.
 */
public class DeviceUtils {
    public static final String getDeviceSN() {
        return Build.SERIAL;
    }

    public static final File getCacheDirectory(Context context) {
        File cacheFile = null;

        boolean exist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if (exist) {
            cacheFile = new File(Environment.getExternalStorageDirectory() + File.separator + context.getPackageName());
        } else {
            cacheFile = context.getFilesDir();
        }
        return cacheFile;
    }
}
