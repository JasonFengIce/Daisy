package tv.ismar.daisy.update;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import tv.ismar.daisy.AppConstant;

import java.io.*;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;

/**
 * Created by huaijie on 3/9/15.
 */
public class AppUpdateUtils {
    private static final String TAG = "AppUpdateUtils";
    private static final String SELF_APP_NAME = "Daisy.apk";

    private static final String PLAYER_ACTIVITY_NAME = "tv.ismar.daisy.PlayerActivity";

    private static AppUpdateUtils instance;

    private final String path;

    private AppUpdateUtils() {
        path = "/sdcard";
    }

    public static AppUpdateUtils getInstance() {
        if (instance == null) {
            instance = new AppUpdateUtils();
        }
        return instance;
    }

    public void checkUpdate(final Context mContext) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(AppConstant.LOG_LEVEL)
                .setEndpoint(AppConstant.APP_UPDATE_HOST)
                .build();

        ClientApi.AppVersionInfo client = restAdapter.create(ClientApi.AppVersionInfo.class);
        client.excute(new Callback<VersionInfoEntity>() {
            @Override
            public void success(VersionInfoEntity versionInfoEntity, Response response) {
                File apkFile = new File(path, SELF_APP_NAME);
                if (apkFile.exists()) {
                    String serverMd5Code = versionInfoEntity.getMd5();
                    String localMd5Code = getMd5ByFile(apkFile);
                    String currentActivityName = getCurrentActivityName(mContext);
                    if (serverMd5Code.equals(localMd5Code) && !currentActivityName.equals(PLAYER_ACTIVITY_NAME)) {
                        Bundle bundle = new Bundle();
                        bundle.putString("title", versionInfoEntity.getUpdate_title());
                        bundle.putStringArrayList("msgs", versionInfoEntity.getUpdate_msg());
                        bundle.putString("path", apkFile.getAbsolutePath());
                        sendUpdateBroadcast(mContext, bundle);
                    }
                } else {
                    PackageInfo packageInfo = null;
                    try {
                        packageInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
                    } catch (PackageManager.NameNotFoundException e) {
                        Log.e(TAG, "can't find this application!!!");
                    }
                    if (packageInfo.versionCode < Integer.parseInt(versionInfoEntity.getVersion())) {
                        String downloadUrl = versionInfoEntity.getDownloadurl();
                        String serverMd5 = versionInfoEntity.getMd5();
                        downloadAPK(mContext, downloadUrl, serverMd5);
                    }
                }
            }

            @Override
            public void failure(RetrofitError retrofitError) {

            }
        });
    }

    private void downloadAPK(final Context mContext, final String downloadUrl, final String md5) {
        new Thread() {
            @Override
            public void run() {
                Log.d(TAG, "downloadAPK is running...");
                File fileName = null;
                try {
                    int byteread;
                    URL url = new URL(downloadUrl);
                    fileName = new File(path, SELF_APP_NAME);
                    if (!fileName.exists())
                        fileName.createNewFile();
                    URLConnection conn = url.openConnection();
                    InputStream inStream = conn.getInputStream();
                    FileOutputStream fs = new FileOutputStream(new File(path, SELF_APP_NAME));
                    byte[] buffer = new byte[1024];
                    while ((byteread = inStream.read(buffer)) != -1) {
                        fs.write(buffer, 0, byteread);
                    }
                    inStream.close();
                    fs.flush();
                    fs.close();

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                checkUpdate(mContext);

            }
        }.start();
    }


    private static String getMd5ByFile(File file) {
        String value = null;
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            MappedByteBuffer byteBuffer = in.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(byteBuffer);
            BigInteger bi = new BigInteger(1, md5.digest());
            value = bi.toString(16);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return value;
    }

    public String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(Environment.MEDIA_MOUNTED);   //判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        }
        Log.d(TAG, sdDir.toString());
        return sdDir.toString();
    }

    private void sendUpdateBroadcast(Context context, Bundle bundle) {
        Intent intent = new Intent();
        intent.setAction(AppConstant.APP_UPDATE_ACTION);
        intent.putExtra("data", bundle);
        context.sendBroadcast(intent);
    }

    /**
     * get current activity task the top activity
     *
     * @param context
     * @return
     */
    public String getCurrentActivityName(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        Log.i(TAG, "getCurrentActivityName : pkg --->" + cn.getPackageName());
        Log.i(TAG, "getCurrentActivityName : cls ---> " + cn.getClassName());
        return cn.getClassName();
    }

    public void modifyUpdatePreferences(Context context, boolean update) {
        SharedPreferences.Editor editor = context.getSharedPreferences("app_update", Context.MODE_PRIVATE).edit();
        editor.putBoolean("update", update);
        editor.apply();
    }

    public boolean getUpdatePreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("app_update", Context.MODE_PRIVATE);
        boolean update = sharedPreferences.getBoolean("update", false);
        return update;
    }
}
