package tv.ismar.daisy.utils;

import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by huaijie on 11/9/15.
 */
public class CountDownTimer {
    private static final String TAG = "CountDownTimer";

    private int mCurrentTime;


    public void countDown(final int totalTime, final OnTimeChangedCallback onTimeChangedCallback) {
        final Timer timer = new Timer();
        final TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                mCurrentTime = mCurrentTime + 1;
                if (mCurrentTime >= totalTime) {
                    onTimeChangedCallback.onTimeEnd(timer);
                }

            }
        };
        timer.schedule(timerTask, 0, 1000);
    }


    public interface OnTimeChangedCallback {
        void onTimeChange();

        void onTimeEnd(Timer timer);
    }
}
