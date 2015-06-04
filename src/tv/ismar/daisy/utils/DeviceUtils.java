package tv.ismar.daisy.utils;

import android.content.Context;
import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;

/**
 * Created by huaijie on 3/12/15.
 */
public class DeviceUtils {
//    public static final String getDeviceSN() {
//        return Build.SERIAL;
//    }
//
//    public static final File getCacheDirectory(Context context) {
//        File cacheFile = null;
//
//        boolean exist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
//        if (exist) {
//            cacheFile = new File(Environment.getExternalStorageDirectory() + File.separator + context.getPackageName());
//        } else {
//            cacheFile = context.getFilesDir();
//        }
//        return cacheFile;
//    }


    public static String getCachePath() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return new File(Environment.getExternalStorageDirectory(), "/Daisy").getPath();
        } else {
            return null;
        }
    }

    public static String getMd5ByFile(File file) {
        String value = null;
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            MappedByteBuffer byteBuffer = in.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(byteBuffer);
            BigInteger bi = new BigInteger(1, md5.digest());
            value = bi.toString(16);
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }
}
