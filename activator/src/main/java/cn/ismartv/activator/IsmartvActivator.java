package cn.ismartv.activator;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import cn.ismartv.activator.core.Md5;
import cn.ismartv.activator.core.http.HttpClientAPI;
import cn.ismartv.activator.core.rsa.RSACoder;
import cn.ismartv.activator.core.rsa.SkyAESTool2;
import cn.ismartv.activator.data.Result;
import cn.ismartv.log.interceptor.HttpLoggingInterceptor;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.GsonConverterFactory;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by huaijie on 5/17/16.
 */
public class IsmartvActivator {
    static {
        System.loadLibrary("mac_address");
    }

    private static final String TAG = "IsmartvActivator";
    private static final String DEFAULT_HOST = "http://peachtest.tvxio.com";
    private static final String SIGN_FILE_NAME = "sign";
    private static final int DEFAULT_CONNECT_TIMEOUT = 2;
    private static final int DEFAULT_READ_TIMEOUT = 5;

    private String manufacture;
    private String kind;
    private String version;
    private String location;
    private String sn;
    private Context mContext;
    private String fingerprint;
    private Callback mCallback;
    private Retrofit SKY_Retrofit;
    private String deviceId;

    private static IsmartvActivator mInstance;

    public void setManufacture(String manufacture) {
        this.manufacture = manufacture;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setLocation(String location) {
        this.location = location;
    }


    public static IsmartvActivator getInstance(Context context, String host) {
        if (mInstance == null) {
            mInstance = new IsmartvActivator(context, host);
        }
        return mInstance;
    }

    public static IsmartvActivator getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new IsmartvActivator(context, DEFAULT_HOST);
        }
        return mInstance;
    }

    public IsmartvActivator(Context context, String host) {
        mContext = context;
        manufacture = Build.BRAND;
        kind = Build.PRODUCT.replaceAll(" ", "_").toLowerCase();
        version = String.valueOf(getAppVersionCode());
        deviceId = getMacAddress();
        sn = Md5.md5((deviceId + Build.SERIAL).trim());
        fingerprint = Md5.md5(sn);

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_READ_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(interceptor)
                .build();

        SKY_Retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(host)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }


    public void execute(Callback callback) {
        mCallback = callback;
        if (isSignFileExists()) {
            active();
        } else {
            getLicence(mCallback);
        }
    }

    private int getAppVersionCode() {
        PackageManager packageManager = mContext.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(mContext.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private String getAppVersionName() {
        String appVersionName = new String();
        try {
            PackageManager pm = mContext.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(), 0);
            appVersionName = pi.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return appVersionName;
    }

    private boolean isSignFileExists() {
        return mContext.getFileStreamPath(SIGN_FILE_NAME).exists();
    }


    private void getLicence(final Callback callback) {
        HttpClientAPI.GetLicence client = SKY_Retrofit.create(HttpClientAPI.GetLicence.class);
        client.excute(fingerprint, sn, manufacture, "1").enqueue(new retrofit2.Callback<ResponseBody>() {
            @Override
            public void onResponse(Response<ResponseBody> response) {
                try {
                    if (response.body() != null) {
                        writeToSign(response.body().bytes());
                        active();
                    } else {
                        callback.onFailure("get licence: " + response.errorBody().string());
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                callback.onFailure("get licence: " + t.getMessage());
            }

        });

    }

    private void active() {
        String signPath = mContext.getFileStreamPath(SIGN_FILE_NAME).getAbsolutePath();
        String result = decryptSign(sn, signPath);
        String publicKey = result.split("\\$\\$\\$")[1];
        String sign = "ismartv=201415&kind=" + kind + "&sn=" + sn;
        String rsaEncryptResult = encryptWithPublic(sign, publicKey);

        HttpClientAPI.ExcuteActivator activator = SKY_Retrofit.create(HttpClientAPI.ExcuteActivator.class);
        activator.excute(sn, manufacture, kind, version, rsaEncryptResult, fingerprint, "v3_0", getAndroidDevicesInfo()).enqueue(new retrofit2.Callback<Result>() {
            @Override
            public void onResponse(Response<Result> response) {
                if (response.errorBody() != null) {
                    try {
                        mCallback.onFailure("active: " + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    mCallback.onSuccess(response.body());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                mCallback.onFailure("active: " + t.getMessage());
            }
        });
    }


    private String getAndroidDevicesInfo() {
        try {
            JSONObject json = new JSONObject();
            String versionName = getAppVersionName();
            String serial = Build.SERIAL;
            String hh = Build.ID + "//" + Build.SERIAL;
            Md5.md5(Build.SERIAL + Build.ID);
            json.put("fingerprintE", Md5.md5(Build.SERIAL + Build.ID));
            json.put("fingerprintD", hh);
            json.put("versionName", versionName);
            json.put("serial", serial);
            json.put("deviceId", deviceId);
            return json.toString() + "///" + this.location;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void writeToSign(byte[] bytes) {
        FileOutputStream fs;
        try {
            fs = mContext.openFileOutput(SIGN_FILE_NAME, Context.MODE_WORLD_READABLE);
            fs.write(bytes);
            fs.flush();
            fs.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String decryptSign(String key, String ContentPath) {
        String decryptResult = new String();
        File file = new File(ContentPath);
        if (file.exists()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                int count = fileInputStream.available();
                byte[] bytes = new byte[count];
                fileInputStream.read(bytes);
                fileInputStream.close();
                decryptResult = SkyAESTool2.decrypt(key.substring(0, 16), Base64.decode(bytes, Base64.URL_SAFE));
            } catch (Exception e) {
                file.delete();
                mCallback.onFailure("decryptSign: " + e.getMessage());
            }
        }
        return decryptResult;
    }


    private String encryptWithPublic(String string, String publicKey) {
        try {
            String input = Md5.md5(string);
            byte[] rsaResult = RSACoder.encryptByPublicKey(input.getBytes(), publicKey);
            return Base64.encodeToString(rsaResult, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public interface Callback {
        void onSuccess(Result result);

        void onFailure(String msg);
    }

    public native String getMacAddress();


    public String PayRsaEncode(String content) {
        String rsaEnResult = "";
        FileInputStream inputStream = null;
        try {
            inputStream = mContext.openFileInput("sn");
            int length = inputStream.available();
            byte[] bytes = new byte[length];
            inputStream.read(bytes);
            String mac = new String(bytes, "UTF-8");
            inputStream.close();
            this.sn = mac;

            this.fingerprint = this.sn;

            String key = new String(this.fingerprint);
            String result = decryptSign(key, mContext.getFileStreamPath(SIGN_FILE_NAME).getAbsolutePath());
            String publicKey;
            publicKey = result.split("\\$\\$\\$")[1];
            Log.d(TAG, "zjq public key is ---> " + publicKey);
            rsaEnResult = encryptWithPublic(content, publicKey);
            Log.d(TAG, "zjq RSA 加密" + rsaEnResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rsaEnResult;
    }
}