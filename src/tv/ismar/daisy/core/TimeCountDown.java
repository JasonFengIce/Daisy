package tv.ismar.daisy.core;

import android.os.Handler;
import android.os.Message;

/**
 * Created by huaijie on 7/12/15.
 */
public class TimeCountDown {
    private int from;
    private int to;


    public interface OnTimeChangeListener {
        void onChange();
    }

    public void start(int from, int to) {
        this.from = from;
        this.to = to;
    }


    private void pauseCount() {

    }

    private void continueCount() {

    }

    private void restartCount(){

    }

    private Handler messageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

        }
    };
}
