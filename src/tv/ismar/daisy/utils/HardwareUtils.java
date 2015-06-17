package tv.ismar.daisy.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;

/**
 * Created by huaijie on 3/12/15.
 */
public class HardwareUtils {

    public static String getCachePath(Context context) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return new File(Environment.getExternalStorageDirectory(), "/Daisy").getAbsolutePath();
        } else {
            long internalMemorySize = getAvailableInternalMemorySize() / 1024 / 1024 / 1024;
            if (internalMemorySize >= 1)
                return context.getCacheDir().getAbsolutePath();
            else
                return "";
        }
    }

    public static String getSDCardCachePath() {
        return new File(Environment.getExternalStorageDirectory(), "/Daisy").getAbsolutePath();
    }


    public static long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }


    public static String getMd5ByFile(File file) {
        String value = null;
        FileInputStream in;
        try {
            in = new FileInputStream(file);
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[1024 * 1024];
            int length;
            while ((length = in.read(buffer)) >0){
                messageDigest.update(buffer, 0, length);
            }
            BigInteger bi = new BigInteger(1, messageDigest.digest());
            value = bi.toString(16);
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public static String getFileNameWithoutSuffix(String fileName) {
        if (!TextUtils.isEmpty(fileName)) {
            return fileName.split("\\.")[0];
        } else {
            return fileName;
        }
    }

    public static boolean isExternalStorageMounted() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ? true : false;
    }

    public int getheightPixels(Context context) {
        int H = 0;
        int ver = Build.VERSION.SDK_INT;
        DisplayMetrics dm = new DisplayMetrics();
        android.view.Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
        display.getMetrics(dm);
        if (ver < 13) {
            H = dm.heightPixels;
        } else if (ver == 13) {
            try {
                Method mt = display.getClass().getMethod("getRealHeight");
                H = (Integer) mt.invoke(display);
            } catch (Exception e) {
                H = dm.heightPixels;
                e.printStackTrace();
            }
        } else if (ver > 13) {
            try {
                Method mt = display.getClass().getMethod("getRawHeight");
                H = (Integer) mt.invoke(display);
            } catch (Exception e) {
                H = dm.heightPixels;
                e.printStackTrace();
            }
        }
        return H;
    }


}
