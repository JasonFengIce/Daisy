package cn.ismartv.activator.core.mnative;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import android.util.Base64;
import android.util.Log;

import org.apache.http.util.EncodingUtils;

import cn.ismartv.activator.core.rsa.SkyAESTool2;
import cn.ismartv.activator.utils.MD5Utils;

/**
 * Created by huaijie on 14-10-17.
 */
public class NativeManager {
    private static final String TAG = "NativeManager";

//    static {
//        System.loadLibrary("activator");
//    }

    public interface DecryptCallback {
        void onFailure();
    }

//    public native String AESdecrypt(String key, byte[] content);

//    public native String encrypt(String key, String content);

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

//    public native String RSAEncrypt(String key, String content);
public static String RSAEncrypt(String publicKeyString, String content){
    try {
        byte[] keyBytes = Base64.decode(publicKeyString.getBytes(), Base64.URL_SAFE);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(keySpec);

        byte[] byteFina = null;
        Cipher cipher;

        try {
            // 采用了填充为空的方式，最终每次加密后的串都是一致的，保密性差
            cipher = Cipher.getInstance("RSA/None/NoPadding", "BC");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byteFina = cipher.doFinal(content.getBytes());
        } finally {
            cipher = null;
        }
        return new String(Base64.encode(byteFina, Base64.URL_SAFE));
    }catch (Exception e){
        return "";
    }

}

//    public native String GetEtherentMac();
    public String GetEtherentMac(){
    	String content = null;
    	try {
			FileInputStream inputStream = new FileInputStream("sys/class/net/eth0/address");
            int length = inputStream.available();
            byte[] bytes = new byte[length];
            inputStream.read(bytes);
            content = EncodingUtils.getString(bytes, "UTF-8");
            inputStream.close();  
		} catch (FileNotFoundException e) {
            content ="noaddress";
			e.printStackTrace();
		} catch (IOException e) {
            content ="noaddress";
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	content = MD5Utils.encryptByMD5(content);
    	return content;
    }

//    public native String PayRSAEncrypt(String key, String content);

    private static class SingleNativeManager {
        private static NativeManager instance = new NativeManager();
    }

    public static NativeManager getInstance() {
        return SingleNativeManager.instance;
    }
}
