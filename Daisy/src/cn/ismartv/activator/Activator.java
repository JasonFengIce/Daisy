package cn.ismartv.activator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.http.util.EncodingUtils;
import org.json.JSONException;
import org.json.JSONObject;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;
import cn.ismartv.activator.core.http.HttpClientAPI;
import cn.ismartv.activator.core.http.HttpClientAPI.ExcuteActivator;
import cn.ismartv.activator.core.http.HttpClientManager;
import cn.ismartv.activator.core.mnative.NativeManager;
import cn.ismartv.activator.data.Result;
import cn.ismartv.activator.utils.MD5Utils;

import com.squareup.okhttp.ResponseBody;

public class Activator {
    public static final String SIGN_FILE_NAME = "sign1";
    public static final String APP_NAME = "activator";
    public static boolean DEBUG = true;
    private static final String TAG = "Activator";
    private static Context mContext;
    private static Activator ourInstance = new Activator();
    private String sn, manufacture, kind, version, fingerprint, locationInfo;
    private OnComplete mOnComplete;
    public boolean iswaiting;
    private int actvieTryTime = 0;

    public interface OnComplete {
        void onSuccess(Result result);

        void onFailed(String message);
    }

    public void setOnCompleteListener(OnComplete onComplete) {
        mOnComplete = onComplete;
    }


    private Activator() {

    }

    public static Activator getInstance(Context context) {
        mContext = context;
        return ourInstance;
    }


    public void active(String manufacture, String kind, String version, String locationInfo) {
        actvieTryTime = actvieTryTime + 1;
		iswaiting = true;
		if (actvieTryTime > 2) {
			iswaiting = false;
			mOnComplete.onFailed("激活失败!!!");
		}

        NativeManager nativeManager = new NativeManager();
        this.locationInfo = locationInfo;
        try {
            this.sn = nativeManager.GetEtherentMac();
            if ("noaddress".equals(this.sn)) {
                this.sn = MD5Utils.encryptByMD5(getDeviceId() + Build.SERIAL);
            }
            this.manufacture = manufacture;
            this.kind = kind.toLowerCase();
            this.version = version;
            this.fingerprint = MD5Utils.encryptByMD5(this.sn);
            if (!mContext.getFileStreamPath(SIGN_FILE_NAME).exists()) {
                FileOutputStream fs = mContext.openFileOutput("sn", Context.MODE_WORLD_READABLE);
                fs.write(this.sn.getBytes());
                fs.flush();
                fs.close();
                getLicence();
            } else {
            	String content;
            	File snfile = mContext.getFileStreamPath("sn");
            	if(!snfile.exists()){
            		content = sn;
            	}else{
            		FileInputStream inputStream = mContext.openFileInput("sn");
                    int length = inputStream.available();
                    byte[] bytes = new byte[length];
                    inputStream.read(bytes);
                    content = EncodingUtils.getString(bytes, "UTF-8");
                    inputStream.close();       		
            	}
                this.sn = content;
                this.fingerprint = MD5Utils.encryptByMD5(this.sn);
                activator(sn, manufacture, kind, version, fingerprint);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getLicence() {
        Retrofit retrofit = HttpClientManager.getInstance().SKY_Retrofit;
        HttpClientAPI.GetLicence client = retrofit.create(HttpClientAPI.GetLicence.class);
        client.excute(fingerprint, sn, manufacture, "1").enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Response<ResponseBody> response, Retrofit retrofit) {
                try {
                	if(response.body() != null){
                    FileOutputStream fs = mContext.openFileOutput(SIGN_FILE_NAME, Context.MODE_WORLD_READABLE);
                    fs.write(response.body().bytes());
                    fs.flush();
                    fs.close();
                    activator(sn, manufacture, kind, version, fingerprint);
                	}else{
                		mOnComplete.onFailed("get licence failure!!!");
                		}
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
						iswaiting = false;
                mOnComplete.onFailed("get licence failure!!!");
            }
        });
    }


    private void activator(String sn, final String manufacture, final String kind, final String version, String fingerprint) {
        NativeManager nativeManager = new NativeManager();
        String result = nativeManager.decrypt(sn, mContext.getFileStreamPath(SIGN_FILE_NAME).getAbsolutePath(), new NativeManager.DecryptCallback() {
            @Override
            public void onFailure() {
                active(manufacture, kind, version, locationInfo);
            }
        });
        String publicKey;
        try {
            publicKey = result.split("\\$\\$\\$")[1];
        } catch (Exception e) {
			iswaiting = false;
            mOnComplete.onFailed(e.getMessage());
            return;
        }

        String sign = "ismartv=201415&kind=" + kind + "&sn=" + sn;
        String rsaEnResult = nativeManager.RSAEncrypt(publicKey, sign);

        Retrofit retrofit = HttpClientManager.getInstance().SKY_Retrofit;
        ExcuteActivator activator = retrofit.create(ExcuteActivator.class);
        activator.excute(sn, manufacture, kind, version, rsaEnResult, fingerprint, "v2_0", getAndroidDevicesInfo()).enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Response<Result> response, Retrofit retrofit) {
                Result result = response.body();
                if (response.errorBody() != null) {
							iswaiting = false;
                    mOnComplete.onFailed("激活失败");
                    Log.e(TAG, response.errorBody().toString());
                } else {
							iswaiting = false;
                    mOnComplete.onSuccess(result);
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
            	iswaiting = false;
                mOnComplete.onFailed("激活失败");
            }
        });
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
            String result = nativeManagers.decrypt(key, mContext.getFileStreamPath(SIGN_FILE_NAME).getAbsolutePath(), new NativeManager.DecryptCallback() {
                @Override
                public void onFailure() {
                    active(manufacture, kind, version, locationInfo);
                }
            });
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

    private String getDeviceId() {
        String deviceId = null;
        try {
            TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            deviceId = tm.getDeviceId();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return deviceId;
    }

}
