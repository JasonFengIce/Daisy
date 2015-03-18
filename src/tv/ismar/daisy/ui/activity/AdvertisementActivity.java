package tv.ismar.daisy.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import com.squareup.picasso.Picasso;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import tv.ismar.daisy.AppConstant;
import tv.ismar.daisy.LauncherActivity;
import tv.ismar.daisy.R;
import tv.ismar.daisy.core.advertisement.AdvertisementInfoEntity;
import tv.ismar.daisy.core.update.AppUpdateUtils;
import tv.ismar.daisy.utils.DeviceUtils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

/**
 * Created by huaijie on 3/10/15.
 */
public class AdvertisementActivity extends Activity {
    private static final String TAG = "AdvertisementActivity";


    private String advertisePicCacheDir;
    private String ADVERTISE_POSTER_NAME = "poster.png";

    private static final int MSG_ADVER_TIMER = 0x0001;

    private ImageView adverPic;
    private ImageView timerText;

    private Handler messageHandler;

    int[] secondsResId = {R.drawable.second_1, R.drawable.second_2,
            R.drawable.second_3, R.drawable.second_4, R.drawable.second_5};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advertisement);
        initViews();
        File apkFile = new File(getFilesDir().getAbsolutePath(), AppUpdateUtils.SELF_APP_NAME);
        if (AppUpdateUtils.getInstance().getUpdatePreferences(this) && apkFile.exists()) {
            AppUpdateUtils.getInstance().modifyUpdatePreferences(this, false);
            Picasso.with(AdvertisementActivity.this)
                    .load(R.drawable.bg_update_prompt)
                    .into(adverPic);
            AppUpdateUtils.getInstance().execCmd("adb connect 127.0.0.1");
            AppUpdateUtils.getInstance().execCmd("adb install -r " + apkFile.getAbsolutePath());
        } else {
            advertisePicCacheDir = getFilesDir().getAbsolutePath();
            placeAdvertisementPic(new File(advertisePicCacheDir, ADVERTISE_POSTER_NAME).getAbsolutePath());
//        fetchCdnChangeTag();
            messageHandler = new MessageHandler();
        }

    }

    private void initViews() {
        adverPic = (ImageView) findViewById(R.id.advertisement_pic);
        timerText = (ImageView) findViewById(R.id.adver_timer);
    }

//    private void fetchCdnChangeTag() {
//        RestAdapter restAdapter = new RestAdapter.Builder()
//                .setLogLevel(AppConstant.LOG_LEVEL)
//                .setEndpoint(AppConstant.CDN_API_HOST)
//                .build();
//        ClientApi.CdnChangeTag client = restAdapter.create(ClientApi.CdnChangeTag.class);
//        client.excute(new Callback<CdnChangeTagEntity>() {
//            @Override
//            public void success(CdnChangeTagEntity cdnChangeTagEntity, Response response) {
//                fetchCdnList();
//            }
//
//            @Override
//            public void failure(RetrofitError retrofitError) {
//                Log.e(TAG, "fetchCdnChangeTag failed !!! ");
//                Log.e(TAG, retrofitError.getMessage());
//            }
//        });
//    }

//    private void fetchCdnList() {
//        RestAdapter restAdapter = new RestAdapter.Builder()
//                .setLogLevel(AppConstant.LOG_LEVEL)
//                .setEndpoint(AppConstant.CDN_API_HOST)
//                .build();
//        ClientApi.CdnList client = restAdapter.create(ClientApi.CdnList.class);
//        client.excute(new Callback<CdnListEntity>() {
//            @Override
//            public void success(CdnListEntity cdnListEntity, Response response) {
//                CdnCacheManager.getInstance().saveCdnList(cdnListEntity);
//            }
//
//            @Override
//            public void failure(RetrofitError retrofitError) {
//                Log.e(TAG, "fetchCdnList failed !!! ");
//                Log.e(TAG, retrofitError.getMessage());
//            }
//        });
//    }

    private void placeAdvertisementPic(String path) {
        Picasso.with(AdvertisementActivity.this)
                .load("file://" + path)
                .error(getImageFromAssetsFile("poster.png"))
                .into(adverPic, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        timerCountDown();
                    }

                    @Override
                    public void onError() {
                        fetchAdvertisementInfo();
                        adverPic.setImageDrawable(getImageFromAssetsFile("poster.png"));
                        timerCountDown();
                    }
                });
    }


    private void fetchAdvertisementInfo() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(AppConstant.LOG_LEVEL)
                .setEndpoint(tv.ismar.daisy.core.client.ClientApi.ADVERTISEMENT_HOST)
                .build();
        tv.ismar.daisy.core.client.ClientApi.AdvertisementInfo client =
                restAdapter.create(tv.ismar.daisy.core.client.ClientApi.AdvertisementInfo.class);
        String deviceId = DeviceUtils.getDeviceSN();
        client.excute(deviceId, new Callback<ArrayList<AdvertisementInfoEntity>>() {
            @Override
            public void success(ArrayList<AdvertisementInfoEntity> advertisementInfoEntities, Response response) {
                String url = advertisementInfoEntities.get(0).getUrl();
                Log.i(TAG, "fetchAdvertisementInfo: adver pic url ---> " + url);
                downloadPic(AdvertisementActivity.this, advertisePicCacheDir, ADVERTISE_POSTER_NAME, url);
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.e(TAG, "fetchAdvertisementInfo failed!!!");
            }
        });
    }

    private void timerCountDown() {
        new Thread() {
            @Override
            public void run() {
                for (int i = 4; i >= 0; i--) {
                    Message message = messageHandler.obtainMessage(MSG_ADVER_TIMER, i);
                    messageHandler.sendMessage(message);
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    private void intentToLauncher() {
        Intent intent = new Intent(this, LauncherActivity.class);
        startActivity(intent);
    }

    /**
     * get bitmap from assert directory
     *
     * @param fileName
     * @return
     */
    private Drawable getImageFromAssetsFile(String fileName) {
        Drawable image = null;
        AssetManager am = getResources().getAssets();
        try {
            InputStream is = am.open(fileName);
            image = BitmapDrawable.createFromStream(is, "post");
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }


    private Bitmap getBitmapFromAssetsFile(String fileName) {
        Bitmap image = null;
        AssetManager am = getResources().getAssets();
        try {
            InputStream is = am.open(fileName);
            image = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    private void showCountTime(int second) {
        timerText.setImageResource(secondsResId[second]);
        if (second == 0) {
            intentToLauncher();
            finish();
        }
    }

    class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_ADVER_TIMER:
                    int second = Integer.parseInt(String.valueOf(msg.obj));
                    showCountTime(second);
                    break;
                default:
                    break;
            }
        }
    }

    private void downloadPic(final Context mContext, final String path, final String name, final String downloadUrl) {
        new Thread() {
            @Override
            public void run() {
                Log.d(TAG, "downloadPic is running...");
                File fileName = null;
                try {
                    int byteread;
                    URL url = new URL(downloadUrl);
                    fileName = new File(path, name);
                    if (!fileName.exists())
                        fileName.createNewFile();
                    URLConnection conn = url.openConnection();
                    InputStream inStream = conn.getInputStream();
                    FileOutputStream fs = mContext.openFileOutput(name, Context.MODE_PRIVATE);
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
                Log.d(TAG, "downloadPic is end...");
            }
        }.start();
    }

    public void installApk(Context mContext, String path) {
        if (new File(path).exists()) {
            Log.i(TAG, "install update apk ...");
            Uri uri = Uri.parse("file://" + path);
            Log.i(TAG, uri.toString());
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        }
    }

    private int execInstallCmd(final String params) {
        int result = -1;
        try {
            Process localProcess = Runtime.getRuntime().exec(params);
            Object localObject = localProcess.getOutputStream();
            DataOutputStream localDataOutputStream = new DataOutputStream(
                    (OutputStream) localObject);
            localDataOutputStream.writeBytes("exit\n");
            localDataOutputStream.flush();
            localProcess.waitFor();
            result = localProcess.exitValue();
        } catch (Exception localException) {
            localException.printStackTrace();
        }
        return result;
    }
}
