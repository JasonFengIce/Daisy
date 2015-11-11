package cn.ismartv.activator.utils;

import android.util.Log;
import cn.ismartv.activator.Activator;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by huaijie on 14-10-15.
 */
public class MD5Utils {
    private static final String TAG = "MD5Utils";

    public static String encryptByMD5(String string) {
        if (Activator.DEBUG)
            Log.d(TAG, "not md5 ---> " + string);
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Huh, MD5 should be supported?", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Huh, UTF-8 should be supported?", e);
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        if (Activator.DEBUG)
            Log.d(TAG, "md5 ---> " + hex.toString());
        return hex.toString();
    }


    public static String bcd2Str(byte[] bytes) {

        StringBuffer temp = new StringBuffer(bytes.length * 2);

        for (int i = 0; i < bytes.length; i++) {

            temp.append((byte) ((bytes[i] & 0xf0) >>> 4));

            temp.append((byte) (bytes[i] & 0x0f));

        }

        return temp.toString().substring(0, 1).equalsIgnoreCase("0") ? temp.toString().substring(1) : temp.toString();

    }

}
