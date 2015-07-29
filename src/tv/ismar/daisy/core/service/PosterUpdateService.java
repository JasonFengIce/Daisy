package tv.ismar.daisy.core.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import tv.ismar.daisy.AppConstant;
import tv.ismar.daisy.VodApplication;
import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.NetworkUtils;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.advertisement.AdvertisementInfoEntity;
import tv.ismar.daisy.core.client.IsmartvUrlClient;
import tv.ismar.daisy.models.AdElement;
import tv.ismar.daisy.utils.AppUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.util.*;

/**
 * Created by huaijie on 3/19/15.
 */
public class PosterUpdateService extends Service {
    private static final String TAG = "PosterUpdateService";

    private static final int UPDATE_ADVERTISEMENT = 0x0001;

    public static final String POSTER_NAME = "poster.png";
    private static final String POSTER_TMP_NAME = "poster_tmp.png";

    private File posterFile;
    private File posterTmpFile;

    private Handler messageHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_ADVERTISEMENT:
                    fetchAdvertisementInfo();
                    break;
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        posterFile = new File(getFilesDir(), POSTER_NAME);
        posterTmpFile = new File(getFilesDir(), POSTER_TMP_NAME);
        posterUpdateTask();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void posterUpdateTask() {
        final Timer timer = new Timer();
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                Log.i(TAG, "posterUpdateTask every 15 min");
                if (isPosterExpire() && posterFile.exists()) {
                    posterFile.delete();
                }

                messageHandler.sendEmptyMessage(UPDATE_ADVERTISEMENT);
            }
        };
        timer.schedule(tt, 3000, 30 * 1000);
// ／       timer.schedule(tt, 3000, 15 * 60 * 1000);
    }

    private void fetchAdvertisementInfo() {
        String api = SimpleRestClient.ad_domain + "/api/get/ad/";
        String adpId = "['" + "kaipingguanggao" + "']";
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("adpid", adpId);

        new IsmartvUrlClient().doAdvertisementRequest(IsmartvUrlClient.Method.POST, api, params, new IsmartvUrlClient.CallBack() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "fetchAdvertisementInfo:" + result);
            }

            @Override
            public void onFailed(Exception exception) {
                Log.e(TAG, exception.getMessage());
            }
        });
    }


    private void downloadPic(final AdvertisementInfoEntity advertisementInfoEntity) {
        new Thread() {
            @Override
            public void run() {
                Log.d(TAG, "downloadPic is running...");
                try {
                    int byteread;
                    URL url = new URL(advertisementInfoEntity.getUrl());
                    Log.i(TAG, "downloadPic ---> " + url);
                    if (!posterTmpFile.exists())
                        posterTmpFile.createNewFile();
                    URLConnection conn = url.openConnection();
                    InputStream inStream = conn.getInputStream();
                    FileOutputStream fs = openFileOutput(POSTER_TMP_NAME, Context.MODE_PRIVATE);
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
                Log.i(TAG, "downloadPic is end...");
                updateLocalPoster(advertisementInfoEntity);
            }
        }.start();


    }

    private void updateLocalPoster(AdvertisementInfoEntity advertisementInfoEntity) {
        Log.i(TAG, "updateLocalPoster is running...");
        String cacheFileMd5 = AppUtils.getMd5ByFile(posterTmpFile);
        if (posterTmpFile.exists() &&
                cacheFileMd5.equalsIgnoreCase(advertisementInfoEntity.getMd5())) {
            Log.i(TAG, "replace local poster png");
            posterTmpFile.renameTo(posterFile);
            modifyPosterPreference(advertisementInfoEntity.getEndTimeStamp());
        }
    }

    private boolean isPosterExpire() {
        SharedPreferences preferences = getSharedPreferences("poster", Context.MODE_PRIVATE);
        String posterDateStr = preferences.getString("end_time", "1000-01-01 00:00:00");
        Timestamp posterExpireTimeStamp = Timestamp.valueOf(posterDateStr);
        Timestamp currentTimeStamp = new Timestamp(new Date().getTime());
        if (posterExpireTimeStamp.after(currentTimeStamp))
            return false;
        else
            return true;
    }

    private void modifyPosterPreference(Timestamp timestamp) {
        SharedPreferences preferences = getSharedPreferences("poster", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("end_time", timestamp.toString());
        editor.apply();
    }


    private File getLocalPosterFile() {
        return getFileStreamPath(POSTER_NAME);
    }
}