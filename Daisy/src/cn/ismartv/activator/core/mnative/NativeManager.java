package cn.ismartv.activator.core.mnative;

/**
 * Created by huaijie on 14-10-17.
 */
public class NativeManager {
    static {
        System.loadLibrary("activator");
    }
    
    public native String AESdecrypt(String key,byte[] content);
    
    public native String encrypt(String key, String content);

    public native String decrypt(String key, String ContentPath);
    
    public native String RSAEncrypt(String key,String content);

    public native String GetEtherentMac();
    
    public native String PayRSAEncrypt(String key,String content);
    private static class SingleNativeManager{
    	private static NativeManager instance = new NativeManager();
    }
    
    public static NativeManager getInstance(){
    	return SingleNativeManager.instance;
    }
}
