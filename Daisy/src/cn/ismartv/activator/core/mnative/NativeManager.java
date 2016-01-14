package cn.ismartv.activator.core.mnative;

import android.util.Base64;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;

import cn.ismartv.activator.core.rsa.SkyAESTool2;

/**
 * Created by huaijie on 14-10-17.
 */
public class NativeManager {
    private static final String TAG = "NativeManager";

    static {
        System.loadLibrary("activator");
    }

    public interface DecryptCallback {
        void onFailure();
    }

    public native String AESdecrypt(String key, byte[] content);

    public native String encrypt(String key, String content);

    public String decrypt(String key, String ContentPath, DecryptCallback callback) {
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
                callback.onFailure();
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
