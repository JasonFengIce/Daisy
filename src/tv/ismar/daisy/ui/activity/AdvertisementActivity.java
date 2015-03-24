package tv.ismar.daisy.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;
import com.squareup.picasso.Picasso;
import tv.ismar.daisy.LauncherActivity;
import tv.ismar.daisy.R;
import tv.ismar.daisy.core.service.PosterUpdateService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by huaijie on 3/10/15.
 */
public class AdvertisementActivity extends Activity {
    private static final String TAG = "AdvertisementActivity";
    private static final int MSG_ADVER_TIMER = 0x0001;
    private String advertisePicCacheDir;

    private static final int[] secondsResId = {R.drawable.second_1, R.drawable.second_2,
            R.drawable.second_3, R.drawable.second_4, R.drawable.second_5};

    /**
     * View
     */
    private ImageView adverPic;
    private ImageView timerText;

    private Handler messageHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advertisement);
        initViews();

        File posterFile = new File(getFilesDir(), PosterUpdateService.POSTER_NAME);
        placeAdvertisementPic(posterFile.getAbsolutePath());
//        fetchCdnChangeTag();
        messageHandler = new MessageHandler();


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
                .skipMemoryCache()
                .into(adverPic, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        timerCountDown();
                    }

                    @Override
                    public void onError() {
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
}
