package cn.ismartv.activator.core.mnative;

import android.util.Base64;
import android.util.Log;
import cn.ismartv.activator.core.rsa.SkyAESTool2;

import java.io.File;
import java.io.FileInputStream;

/**
 * Created by huaijie on 14-10-17.
 */
public class NativeManager {
    private static final String TAG = "NativeManager";

    static {
        System.loadLibrary("activator");
    }

    public native String AESdecrypt(String key, byte[] content);

    public native String encrypt(String key, String content);

    public String decrypt(String key, String ContentPath) {
        File file = new File(ContentPath);
        if (file.exists()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                int count = fileInputStream.available();
                byte[] bytes = new byte[count];
                fileInputStream.read(bytes);
                fileInputStream.close();
                return SkyAESTool2.decrypt(key.substring(0, 16), Base64.decode(bytes, Base64.URL_SAFE));
            } catch (Exception e) {
                file.delete();
                Log.e(TAG, "NativeManager decrypt Exception");
                return "error"; 
            }
        }
        return "";
    }

    public native String RSAEncrypt(String key, String content);

    public native String GetEtherentMac();

    public native String PayRSAEncrypt(String key, String content);

    private static class SingleNativeManager {
        private static NativeManager instance = new NativeManager();
    }

    public static NativeManager getInstance() {
        return SingleNativeManager.instance;
    }
}
