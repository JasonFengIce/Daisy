package tv.ismar.daisy.update;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
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
public class UpdateUtils {

    private static final String TAG = "UpdateUtils";
    private static final String SELF_APP_NAME = "Daisy.apk";

    private static UpdateUtils instance;
    private static Context mContext;

    private UpdateUtils() {

    }

    public static UpdateUtils getInstance(Context context) {
        if (instance == null) {
            instance = new UpdateUtils();
            mContext = context;
        }
        return instance;
    }


    public void fetchUpdate() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(AppConstant.LOG_LEVEL)
                .setEndpoint(AppConstant.API_HOST)
                .build();

        ClientApi.AppVersionInfo client = restAdapter.create(ClientApi.AppVersionInfo.class);
        client.excute(new Callback<VersionInfoEntity>() {
            @Override
            public void success(VersionInfoEntity versionInfoEntity, Response response) {
                PackageInfo packageInfo = null;
                try {
                    packageInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e(TAG, "can't find this application!!!");
                }
                if (packageInfo.versionCode < Integer.parseInt(versionInfoEntity.getVersion())) {
                    if (AppConstant.DEBUG) {
                        Log.d(TAG, "download url: " + versionInfoEntity.getDownloadurl());
                        Log.d(TAG, "local app version ---> " + packageInfo.versionCode);
                        Log.d(TAG, "server app version ---> " + versionInfoEntity.getVersion());
                    }

                    ////////////////////////////////////////////////////////////////////////////////////
                    //If Local Version Less
                    ////////////////////////////////////////////////////////////////////////////////////
                    downloadAPK(versionInfoEntity.getDownloadurl(), versionInfoEntity.getMd5());
                }
            }

            @Override
            public void failure(RetrofitError retrofitError) {

            }
        });
    }

    private void downloadAPK(final String downloadUrl, final String md5) {
        new Thread() {
            @Override
            public void run() {
                File fileName = null;
                try {
                    int byteread;
                    URL url = new URL(downloadUrl);
                    fileName = new File(getSDPath(), SELF_APP_NAME);
                    if (!fileName.exists())
                        fileName.createNewFile();
                    URLConnection conn = url.openConnection();
                    InputStream inStream = conn.getInputStream();
                    FileOutputStream fs = mContext.openFileOutput(SELF_APP_NAME, Context.MODE_WORLD_WRITEABLE);
                    byte[] buffer = new byte[1024];
                    while ((byteread = inStream.read(buffer)) != -1) {
                        fs.write(buffer, 0, byteread);
                    }
                    fs.flush();
                    fs.close();
                    inStream.close();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String MD5Value = getMd5ByFile(fileName);
                if (md5.equals(MD5Value)) {
                    Log.d(TAG, "md5: " + md5 + " | " + "md5: " + MD5Value);
                    installApk();
                }
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


    private void installApk() {


        Uri uri = Uri.fromFile(new File(getSDPath(), SELF_APP_NAME));
        if (AppConstant.DEBUG)
            Log.d(TAG, uri.toString());

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        mContext.startActivity(intent);


//        File apkfile = new File(mContext.getFilesDir(), SELF_APP_NAME);
//        if (!apkfile.exists()) {
//            return;
//        }
//        Intent i = new Intent(Intent.ACTION_VIEW);
//        i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
//        mContext.startActivity(i);


    }


//    private void deleteApk(final Context context) {
//        new Thread() {
//            @Override
//            public void run() {
//                try {
//                    sleep(120000);
//                    File file = new File(context.getFilesDir(), BootInstallTask.SELF_APP_NAME);
//                    if (null != file)
//                        file.delete();
//                    file = new File(context.getFilesDir(), BootInstallTask.VOD_APP_NAME);
//                    if (null != file)
//                        file.delete();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }.start();


//    }

}
