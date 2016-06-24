package tv.ismar.daisy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import tv.ismar.daisy.core.preferences.AccountSharedPrefs;
import tv.ismar.daisy.player.InitPlayerTool;

public class ReactiveActivity extends BaseActivity {
    private LinearLayout layout;
    private PowerManager.WakeLock mWakelock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        winParams.flags |= (WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.dimAmount = 0.75f;
        win.setAttributes(lp);
        win.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        setContentView(R.layout.reactive);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                AccountSharedPrefs accountSharedPrefs = AccountSharedPrefs.getInstance();
                String lock_url = accountSharedPrefs.getSharedPreferences().getString("lock_url", null);
                lock_url = "http://sky.tvxio.bestv.com.cn/v3_0/UF30/tou/api/item/701893/";
                String lock_content_model = accountSharedPrefs.getSharedPreferences().getString("lock_content_model", null);
                if (lock_url != null && lock_content_model != null) {
                    Intent intent = new Intent();
                    if (("variety".equals(lock_content_model) || "entertainment".equals(lock_content_model))) {
                        intent.setAction("tv.ismar.daisy.EntertainmentItem");
                        intent.putExtra("title", "娱乐综艺");
                    } else if ("movie".equals(lock_content_model)) {
                        intent.setAction("tv.ismar.daisy.PFileItem");
                        intent.putExtra("title", "电影");
                    } else {
                        intent.setAction("tv.ismar.daisy.Item");
                    }
                    intent.putExtra("url", lock_url);
                    intent.putExtra("fromPage", "lockscreen");
//					startActivity(intent);
                    InitPlayerTool tool = new InitPlayerTool(ReactiveActivity.this);
//                    tool.channel=channelEntity.getChannel();
                    tool.fromPage = "lockscreen";
                    tool.initClipInfo("http://sky.tvxio.bestv.com.cn/v3_0/SKY2/tou/api/item/702040/", InitPlayerTool.FLAG_URL);
//					finish();
                }
            }
        }, 10);
    }

    @Override
    protected void onResume() {
        super.onResume();
        acquireWakeLock();
     }

    @Override
    protected void onPause() {
        super.onPause();
//        finish();
        releaseWakeLock();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        finish();
        return true;
    }

    public void exitbutton1(View v) {
        this.finish();
    }

    public void exitbutton0(View v) {
        this.finish();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if (resultCode == 1010) {
           finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void acquireWakeLock() {

        if (mWakelock == null) {

            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

            mWakelock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, this.getClass().getCanonicalName());

            mWakelock.acquire();

        }
    }

    private void releaseWakeLock() {

        if (mWakelock != null && mWakelock.isHeld()) {

            mWakelock.release();

            mWakelock = null;

        }
    }
}
