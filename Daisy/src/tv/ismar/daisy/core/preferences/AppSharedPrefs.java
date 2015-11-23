package tv.ismar.daisy.core.preferences;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by huaijie on 11/16/15.
 */
public class AppSharedPrefs {
    public static void initialize(Context context) {
        HttpCacheSharedPrefs.initialize(context);
        LogSharedPrefs.initialize(context);
    }

}
