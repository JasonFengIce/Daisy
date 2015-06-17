package tv.ismar.daisy.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import com.squareup.picasso.Picasso;
import tv.ismar.daisy.AppConstant;
import tv.ismar.daisy.R;
import tv.ismar.daisy.core.service.PosterUpdateService;

import java.io.*;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        flag = true;
        setContentView(R.layout.activity_advertisement);
        initViews();
        posterFile = new File(getFilesDir(), PosterUpdateService.POSTER_NAME);
        placeAdvertisementPic(posterFile.getAbsolutePath());
        messageHandler = new MessageHandler();
        parsePackage();
    }

    private void initViews() {
        adverPic = (ImageView) findViewById(R.id.advertisement_pic);
        timerText = (ImageView) findViewById(R.id.adver_timer);
    }


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
        Intent intent = new Intent(this, TVGuideActivity.class);
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
        super.onDestroy();
        flag = false;
    }

    private void parsePackage() {
        try {
            getPackageManager().getApplicationInfo(
                    "cn.ismartv.speedtester", 0);
        } catch (PackageManager.NameNotFoundException e) {
            parseAsset(this);
        }
    }

    private void parseAsset(final Context context) {
        new Thread() {
            @Override
            public void run() {
                String apkName = "Sakura.apk";
                if (AppConstant.DEBUG)
                    Log.d(TAG, "parse asset invoke...");
                try {
                    InputStream inputStream = context.getAssets().open(apkName);
                    ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
                    int ch;
                    while ((ch = inputStream.read()) != -1) {
                        bytestream.write(ch);
                    }
                    byte imgdata[] = bytestream.toByteArray();
                    bytestream.close();
                    File cacheDir = context.getFilesDir();
                    File temfileName = new File(cacheDir.getAbsolutePath(), apkName);
                    if (!temfileName.exists())
                        temfileName.createNewFile();
                    FileOutputStream fout = context.openFileOutput(apkName, Context.MODE_WORLD_READABLE|Context.MODE_WORLD_WRITEABLE);
                    fout.write(imgdata);
                    fout.flush();
                    fout.close();
                } catch (IOException e) {
                    Log.e(TAG, "parse assert exception");
                }
            }
        }.start();

    }
}
