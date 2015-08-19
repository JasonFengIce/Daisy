package tv.ismar.daisy.core.vlc;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

/**
 * Created by huaijie on 8/17/15.
 */
public class SampleApplication extends Application {

    public final static String SLEEP_INTENT = "org.videolan.vlc.SleepIntent";
    public final static String INCOMING_CALL_INTENT = "org.videolan.vlc.IncomingCallIntent";
    public final static String CALL_ENDED_INTENT = "org.videolan.vlc.CallEndedIntent";

    private static SampleApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }


    public static Context getAppContext() {
        return instance;
    }

    /**
     * @return the main resources from the Application
     */
    public static Resources getAppResources() {
        return instance.getResources();
    }
}
