package tv.ismar.daisy.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Func1;
import tv.ismar.daisy.R;
import tv.ismar.daisy.core.advertisement.AdvertisementManager;
import tv.ismar.daisy.core.initialization.InitializeProcess;
import tv.ismar.daisy.core.logger.AdvertisementLogger;
import tv.ismar.daisy.core.preferences.LogSharedPrefs;
import tv.ismar.daisy.data.LaunchAdvertisementEntity;
import tv.ismar.daisy.player.CallaPlay;

public class AdvertisementActivity extends Activity {
    private static final String TAG = "AdvertisementActivity";
    private static final String DEFAULT_ADV_PICTURE = "file:///android_asset/poster.png";

    private static final int[] secondsResId = {R.drawable.second_1, R.drawable.second_2,
            R.drawable.second_3, R.drawable.second_4, R.drawable.second_5};
    private ImageView adverPic;
    private ImageView timerText;
    private AdvertisementManager mAdvertisementManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advertisement);
        initViews();
        placeAdvertisementPic();
        new Thread(new InitializeProcess(this)).start();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initViews() {
        adverPic = (ImageView) findViewById(R.id.advertisement_pic);
        timerText = (ImageView) findViewById(R.id.adver_timer);
    }

    private void placeAdvertisementPic() {
        mAdvertisementManager = new AdvertisementManager();
        String path = mAdvertisementManager.getAppLaunchAdvertisement();
        Log.d(TAG, "fetch advertisement path: " + path);
        Picasso.with(this)
                .load(path)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_CACHE)
                .into(adverPic, new Callback() {
                    @Override
                    public void onSuccess() {
                        String launchAppAdvEntityStr = LogSharedPrefs.getSharedPrefs(LogSharedPrefs.SHARED_PREFS_NAME);
                        LaunchAdvertisementEntity.AdvertisementData[] advertisementDatas = new Gson().fromJson(launchAppAdvEntityStr, LaunchAdvertisementEntity.AdvertisementData[].class);
                        if (null != advertisementDatas) {
                            LaunchAdvertisementEntity.AdvertisementData data = advertisementDatas[0];
                            new CallaPlay().boot_ad_play(data.getTitle(), data.getMedia_id(), data.getMedia_url(), data.getDuration());
                        }
                    }

                    @Override
                    public void onError() {
                        new CallaPlay().bootAdvExcept(AdvertisementLogger.BOOT_ADV_PLAY_EXCEPTION_CODE, AdvertisementLogger.BOOT_ADV_PLAY_EXCEPTION_STRING);
                        Picasso.with(AdvertisementActivity.this)
                                .load(DEFAULT_ADV_PICTURE)
                                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                                .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_CACHE)
                                .into(adverPic);

                    }
                });
        timerCountDown();
    }

    private void timerCountDown() {
        Observable<Integer> observable = countdown(5);
        observable.doOnSubscribe(new Action0() {
            @Override
            public void call() {
                Log.i(TAG, "开始计时");
            }
        }).subscribe(new Subscriber<Integer>() {
            @Override
            public void onCompleted() {
                Log.i(TAG, "计时完成");
                intentToLauncher();
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "计时出错: " + e.getMessage());
            }

            @Override
            public void onNext(Integer integer) {
                Log.i(TAG, "当前计时：" + integer);
                timerText.setImageResource(secondsResId[integer - 1]);
            }
        });
    }

    private void intentToLauncher() {
        Intent intent = new Intent(this, TVGuideActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public Observable<Integer> countdown(int time) {
        if (time < 0) time = 0;

        final int countTime = time;
        return Observable.interval(0, 1, TimeUnit.SECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<Long, Integer>() {
                    @Override
                    public Integer call(Long increaseTime) {
                        return countTime - increaseTime.intValue();
                    }
                })
                .take(countTime);
    }
}
