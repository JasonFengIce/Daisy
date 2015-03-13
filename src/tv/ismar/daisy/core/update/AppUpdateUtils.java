package tv.ismar.daisy.core.update;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import tv.ismar.daisy.AppConstant;
import tv.ismar.daisy.core.client.ClientApi;

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

    private String path;

    private AppUpdateUtils() {

    }

    public static AppUpdateUtils getInstance() {
        if (instance == null) {
            instance = new AppUpdateUtils();
        }
        return instance;
    }

    public void checkUpdate(final Context mContext) {
        path = mContext.getFilesDir().getAbsolutePath();
//        path = "/sdcard";
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(AppConstant.LOG_LEVEL)
                .setEndpoint(ClientApi.APP_UPDATE_HOST)
                .build();

        ClientApi.AppVersionInfo client = restAdapter.create(ClientApi.AppVersionInfo.class);
        client.excute(new Callback<VersionInfoEntity>() {
            @Override
            public void success(VersionInfoEntity versionInfoEntity, Response response) {
                File apkFile = new File(path, SELF_APP_NAME);
                PackageInfo packageInfo = null;
                try {
                    packageInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e(TAG, "can't find this application!!!");
                }
                if (packageInfo.versionCode < Integer.parseInt(versionInfoEntity.getVersion())) {
                    if (apkFile.exists()) {
                        String serverMd5Code = versionInfoEntity.getMd5();
                        String localMd5Code = getMd5ByFile(apkFile);
                        String currentActivityName = getCurrentActivityName(mContext);

                        int apkVersionCode = getApkVersionCode(mContext, apkFile.getAbsolutePath());
                        int serverVersionCode = Integer.parseInt(versionInfoEntity.getVersion());
                        if (serverMd5Code.equals(localMd5Code) && !currentActivityName.equals(PLAYER_ACTIVITY_NAME)
                                && apkVersionCode == serverVersionCode) {
                            Bundle bundle = new Bundle();
                            bundle.putString("title", versionInfoEntity.getUpdate_title());
                            bundle.putStringArrayList("msgs", versionInfoEntity.getUpdate_msg());
                            bundle.putString("path", apkFile.getAbsolutePath());
                            sendUpdateBroadcast(mContext, bundle);
                        }
                    } else {
                        String downloadUrl = versionInfoEntity.getDownloadurl();
                        String serverMd5 = versionInfoEntity.getMd5();
                        downloadAPK(mContext, downloadUrl);
                    }
                } else {
                    if (apkFile.exists()) {
                        apkFile.delete();
                    }
                }
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.e(TAG, "checkUpdate failed !!!");
            }
        });
    }

    private void downloadAPK(final Context mContext, final String downloadUrl) {
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
                    FileOutputStream fs = mContext.openFileOutput(SELF_APP_NAME, Context.MODE_WORLD_READABLE);
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

    private int getApkVersionCode(Context context, String path) {
        final PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(path, 0);
        return info.versionCode;
    }


}
