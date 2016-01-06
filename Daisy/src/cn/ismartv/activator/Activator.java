package cn.ismartv.activator;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;

import org.apache.http.util.EncodingUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import cn.ismartv.activator.core.http.AppSharedPreferences;
import cn.ismartv.activator.core.http.HttpClient;
import cn.ismartv.activator.core.mnative.NativeManager;
import cn.ismartv.activator.core.rsa.RSACoder;
import cn.ismartv.activator.data.Result;
import cn.ismartv.activator.utils.MD5Utils;
import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.Headers;
import retrofit.http.POST;

public class Activator {
    //public static final String HOST = "http://peach.t.tvxio.com";
    //  public static final String HOST = "http://peach.tvxio.com";
    // public static final String HOST = "http://192.168.1.207:53948";
    public static final String HOST = "http://sky.tvxio.com";

    public static final String SIGN_FILE_NAME = "sign1";
    public static final String APP_NAME = "activator";
    public static boolean DEBUG = true;
    private static final String TAG = "Activator";

    private static Context mContext;


    private static Activator ourInstance = new Activator();

    private String sn, manufacture, kind, version, fingerprint, locationInfo;

    public MessageHandler messageHandler;
    private int getLicenceTryCount = 0;
    public static interface OnComplete {
        void onSuccess(Result result);

        void onFailed(String message);
    }

    private OnComplete mOnComplete;


    public static Activator getInstance(Context context) {
        mContext = context;
        return ourInstance;
    }

    public void setOnCompleteListener(OnComplete onComplete) {
        mOnComplete = onComplete;
    }


    private Activator() {

    }

    public String getDeviceId() {
        String deviceId = null;
        try {
            TelephonyManager tm = (TelephonyManager) mContext
                    .getSystemService(Context.TELEPHONY_SERVICE);
            deviceId = tm.getDeviceId();

        } catch (Exception e) {

            e.printStackTrace();
        }
        return deviceId;
    }

    public void active(String manufacture, String kind, String version, String locationInfo) {
        NativeManager nativeManager = new NativeManager();
        this.locationInfo = locationInfo;
        try {
            this.sn = nativeManager.GetEtherentMac();
            if ("noaddress".equals(this.sn)) {
                this.sn = MD5Utils.encryptByMD5(getDeviceId() + Build.SERIAL);
            }
            Log.i("zjqtestismartv", "sn==" + this.sn);
            this.manufacture = manufacture;
            this.kind = kind.toLowerCase();
            this.version = version;
            this.fingerprint = MD5Utils.encryptByMD5(this.sn);
            messageHandler = new MessageHandler();
            if (!mContext.getFileStreamPath(SIGN_FILE_NAME).exists()) {
                FileOutputStream fs = mContext.openFileOutput(
                        "sn", Context.MODE_WORLD_READABLE);
                byte[] buffer = new byte[1024];
                fs.write(this.sn.getBytes());
                fs.flush();
                fs.close();
                getLicence(messageHandler);
            } else {
                FileInputStream inputStream = mContext.openFileInput("sn");
                int length = inputStream.available();
                byte[] bytes = new byte[length];
                inputStream.read(bytes);
                String content = EncodingUtils.getString(bytes, "UTF-8");
                inputStream.close();
                this.sn = content;
                this.fingerprint = MD5Utils.encryptByMD5(this.sn);
                Log.i("qazwsx", "sn==" + this.sn + "//fingerprint==" + this.fingerprint);
                messageHandler.sendEmptyMessage(HttpClient.SUCCESS);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getLicence(final MessageHandler messageHandler) {
        new Thread() {
            @Override
            public void run() {
            	getLicenceTryCount++;
				if (getLicenceTryCount > 0)
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
                Map map = new HashMap();
                map.put("fingerprint", fingerprint);
                map.put("sn", sn);
                map.put("manufacture", manufacture);
                map.put("code", "1");
                HttpClient.postDatas(messageHandler, mContext, HOST + "/trust/get_licence/", map);
            }
        }.start();
    }

    static {
        System.loadLibrary("activator");
    }

    interface ExcuteActivator {
        @Headers({
                "Pragma: no-cache",
                "Cache-Control: no-cache"
        })
        @FormUrlEncoded
        @POST("/trust/security/active/")
        Call<Result> excute(
                @Field("sn") String sn,
                @Field("manufacture") String manufacture,
                @Field("kind") String kind,
                @Field("version") String version,
                @Field("sign") String sign,
                @Field("fingerprint") String fingerprint,
                @Field("api_version") String api_version,
                @Field("info") String deviceInfo
        );
    }

    private void activator(String sn, String manufacture, String kind, String version, String fingerprint, String sign, String publicKey, String rsa) {
        OkHttpClient client = new OkHttpClient();
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        client.interceptors().add(interceptor);
        Retrofit restAdapter = new Retrofit.Builder()
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(HOST)
                .build();
        ExcuteActivator activator = restAdapter.create(ExcuteActivator.class);
        //ecodeWithPublic(sign,publicKey);
        String a = getAndroidDevicesInfo();
        activator.excute(sn, manufacture, kind, version, rsa, fingerprint, "v2_0", getAndroidDevicesInfo()).enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Response<Result> response, Retrofit retrofit) {
                Map<String, String> map = AppSharedPreferences.getInstance(mContext).getPackageInfo();
                Result result = response.body();
                if (result != null) {
                    result.setPackageInfo(map.get("package"));
                    result.setExpiry_date(map.get("exrpiry_date"));
                    mOnComplete.onSuccess(result);
                } else {
                    if (response.errorBody() != null) {
                        mOnComplete.onFailed("激活失败");
                        Log.e(TAG, response.errorBody().toString());
                    }
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                mOnComplete.onFailed("激活失败");
            }
        });

    }


    private String ecodeWithPublic(String string, String publicKey) {
        try {
            String input = MD5Utils.encryptByMD5(string);
            byte[] rsaResult = RSACoder.encryptByPublicKey(input.getBytes(), publicKey);
            if (DEBUG)
                Log.d(TAG, "md5 == ---> " + input);
            return Base64.encodeToString(rsaResult, Base64.URL_SAFE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String PayRsaEncode(String content) {

        String rsaEnResult = "";
        FileInputStream inputStream = null;
        try {
            inputStream = mContext.openFileInput("sn");
            int length = inputStream.available();
            byte[] bytes = new byte[length];
            inputStream.read(bytes);
            String mac = EncodingUtils.getString(bytes, "UTF-8");
            inputStream.close();
            this.sn = mac;

            this.fingerprint = this.sn;

            String key = new String(this.fingerprint);
            NativeManager nativeManagers = new NativeManager();
            String result = nativeManagers.decrypt(key, mContext.getFileStreamPath("sign1").getAbsolutePath());
            String publicKey;
            publicKey = result.split("\\$\\$\\$")[1];
            Log.d(TAG, "zjq public key is ---> " + publicKey);
            rsaEnResult = nativeManagers.RSAEncrypt(publicKey, content);


            Log.d(TAG, "zjq RSA 加密" + rsaEnResult);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rsaEnResult;
    }

    public String PayRsaEncodeByJava(String content) {
        this.fingerprint = MD5Utils.encryptByMD5(Build.SERIAL + Build.ID);
        String key = new String(this.fingerprint);
        NativeManager nativeManagers = new NativeManager();
        String result = nativeManagers.decrypt(key, mContext.getFileStreamPath("sign1").getAbsolutePath());
        String publicKey;
        publicKey = result.split("\\$\\$\\$")[1];
        Log.d(TAG, "zjq public key is ---> " + publicKey);
        String rsaEnResult = ecodeWithPublic(content, publicKey);


        Log.d(TAG, "zjq RSA 加密" + rsaEnResult);
        return rsaEnResult;
    }

    class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HttpClient.SUCCESS:
                    String key = new String(sn);
                    if (key.length() > 16)
                        key.substring(0, 16);
                    NativeManager nativeManager = new NativeManager();
                    Log.d(TAG, "key is :" + key);
                    String result = nativeManager.decrypt(key, mContext.getFileStreamPath("sign1").getAbsolutePath());
                    if("error".equals(result) && getLicenceTryCount <= 2){
                    	getLicence(messageHandler);
                    	return;
                    }
                    String publicKey;
                    try {
                        publicKey = result.split("\\$\\$\\$")[1];
                    } catch (Exception e) {
                        mOnComplete.onFailed(e.getMessage());
                        return;
                    }

                    if (Activator.DEBUG) {
                        Log.d(TAG, "native result is ---> " + result);
                        Log.d(TAG, "public key is ---> " + publicKey);
                    }
                    String sign = "ismartv=201415&kind=" + kind + "&sn=" + sn;
                    Log.d(TAG, "RSA 签名" + sign);
                    String rsaEnResult = nativeManager.RSAEncrypt(publicKey, sign);
                    Log.d(TAG, "RSA 加密" + rsaEnResult);

                    activator(sn, manufacture, kind, version, fingerprint, sign, publicKey, rsaEnResult);
                    break;
                case HttpClient.FAILED:
				if (getLicenceTryCount > 2)
                    mOnComplete.onFailed((String) msg.obj);
                    break;
                default:
                    break;
            }
        }
    }

    private String getAndroidDevicesInfo() {
        try {
            JSONObject json = new JSONObject();
            String versionName = getAppVersionName();
            // String densityDpi = getDensityDpi();
            //  String heightPixels = getheightPixels();
            String serial = Build.SERIAL;
            String deviceId = getDeviceId();
            String ID = Build.ID;
            String hh = Build.ID + "//" + Build.SERIAL;
            Log.i("zjqtestismartv", "hh");
            MD5Utils.encryptByMD5(Build.SERIAL + Build.ID);
            json.put("fingerprintE", MD5Utils.encryptByMD5(Build.SERIAL + Build.ID));
            json.put("fingerprintD", hh);
            json.put("versionName", versionName);
            //json.put("densityDpi", densityDpi);
            //json.put("heightPixels", heightPixels);
            json.put("serial", serial);
            json.put("deviceId", deviceId);
            return json.toString() + "///" + this.locationInfo;
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "";
    }

    private String getDensityDpi() {
        DisplayMetrics metric = new DisplayMetrics();
        android.view.Display display = ((Activity) mContext).getWindowManager().getDefaultDisplay();
        display.getMetrics(metric);
        int densityDpi = metric.densityDpi;
        return String.valueOf(densityDpi);
    }

    private String getAppVersionName() {
        String versionName = "";
        try {
            PackageManager pm = mContext.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(), 0);
            versionName = pi.versionName;
            if (versionName == null || versionName.length() <= 0) {
                return "";
            }
        } catch (Exception e) {
        }
        return versionName;
    }

    public String getheightPixels() {
        int H = 0;
//		   DisplayMetrics mDisplayMetrics = new DisplayMetrics();
//		   ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
//		   H = mDisplayMetrics.heightPixels;
        int ver = Build.VERSION.SDK_INT;
        DisplayMetrics dm = new DisplayMetrics();
        android.view.Display display = ((Activity) mContext).getWindowManager().getDefaultDisplay();
        display.getMetrics(dm);
        if (ver < 13) {
            H = dm.heightPixels;
        } else if (ver == 13) {
            try {
                Method mt = display.getClass().getMethod("getRealHeight");
                H = (Integer) mt.invoke(display);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                H = dm.heightPixels;
                e.printStackTrace();
            }
        } else if (ver > 13) {
            try {
                Method mt = display.getClass().getMethod("getRawHeight");
                H = (Integer) mt.invoke(display);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                H = dm.heightPixels;
                e.printStackTrace();
            }
        }
        return String.valueOf(H);
    }

    public void removeCallback() {
        if (messageHandler != null)
            messageHandler.removeCallbacksAndMessages(null);
    }
}
