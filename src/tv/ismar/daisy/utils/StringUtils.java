package tv.ismar.daisy.utils;

import android.util.Log;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by huaijie on 8/3/15.
 */
public class StringUtils {
    public static String getMd5Code(String string) {
        String value = null;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(string.getBytes());
            value = new BigInteger(1, messageDigest.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return value;
    }
}
