package tv.ismar.daisy.utils;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;

/**
 * Created by huaijie on 3/12/15.
 */
public class DeviceUtils {

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


    public static long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
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

    public static String getFileNameWithoutSuffix(String fileName) {
        if (!TextUtils.isEmpty(fileName)) {
            return fileName.split("\\.")[0];
        } else {
            return fileName;
        }
    }
}
