package tv.ismar.daisy.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.squareup.otto.Bus;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import tv.ismar.daisy.R;
import tv.ismar.daisy.core.advertisement.AdvertisementManager;
import tv.ismar.daisy.core.initialization.InitializeProcess;
import tv.ismar.daisy.core.logger.AdvertisementLogger;
import tv.ismar.daisy.core.preferences.LogSharedPrefs;
import tv.ismar.daisy.core.service.PosterUpdateService;
import tv.ismar.daisy.data.LaunchAdvertisementEntity;
import tv.ismar.daisy.utils.CountDownTimer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;

/**
 * Created by huaijie on 3/10/15.
 */
public class AdvertisementActivity extends Activity {
    private static final String TAG = "AdvertisementActivity";
    private static final int MSG_ADVER_TIMER = 0x0001;

    private static final int[] secondsResId = {R.drawable.second_1, R.drawable.second_2,
            R.drawable.second_3, R.drawable.second_4, R.drawable.second_5};

    /**
     * View
     */
    private ImageView adverPic;
    private ImageView timerText;

    private Handler messageHandler;

    private volatile boolean flag = true;

    private File posterFile;

    private Bus mBus;

    private CountDownTimer mCountDownTimer;

    private AdvertisementManager mAdvertisementManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        flag = true;
        setContentView(R.layout.activity_advertisement);
        initViews();
        posterFile = new File(getFilesDir(), PosterUpdateService.POSTER_NAME);
        placeAdvertisementPic();
        messageHandler = new MessageHandler();
        new Thread(new InitializeProcess(this)).start();

        mBus = new Bus();
    }

    private void advertiseCountDown() {
        mCountDownTimer = new CountDownTimer();
        mCountDownTimer.countDown(5, new CountDownTimer.OnTimeChangedCallback() {
            @Override
            public void onTimeChange() {

            }

            @Override
            public void onTimeEnd(Timer timer) {

            }
        });

    }

    private void initViews() {
        adverPic = (ImageView) findViewById(R.id.advertisement_pic);
        timerText = (ImageView) findViewById(R.id.adver_timer);
    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    private void placeAdvertisementPic() {


        mAdvertisementManager = new AdvertisementManager();
        String path = mAdvertisementManager.getAppLaunchAdvertisement();

        Log.d(TAG, "fetch advertisement path: " + path);
        Picasso.with(AdvertisementActivity.this)
                .load(path)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                .into(adverPic, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        timerCountDown();
                        String launchAppAdvEntityStr = LogSharedPrefs.getInstance().getSharedPrefs(LogSharedPrefs.LAUNCH_APP_ADV_ENTITY);
                        LaunchAdvertisementEntity.AdvertisementData[] advertisementDatas = new Gson().fromJson(launchAppAdvEntityStr, LaunchAdvertisementEntity.AdvertisementData[].class);
                        if (null != advertisementDatas){
                            LaunchAdvertisementEntity.AdvertisementData data = advertisementDatas[0];
                            AdvertisementLogger.bootAdvPlay(data.getTitle(), data.getMedia_id(), data.getMedia_url(), data.getDuration());
                        }
                    }

                    @Override
                    public void onError() {
                        Picasso.with(AdvertisementActivity.this).load("file:///android_asset/poster.png").into(adverPic);
                        timerCountDown();
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
        Intent intent = new Intent(this, TVGuideActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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


    private void showCountTime(int second) {
        timerText.setImageResource(secondsResId[second]);
        if (second == 0 && flag) {
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


    @Override
    protected void onDestroy() {
        flag = false;
        adverPic = null;
        mAdvertisementManager = null;
        super.onDestroy();

    }
}
