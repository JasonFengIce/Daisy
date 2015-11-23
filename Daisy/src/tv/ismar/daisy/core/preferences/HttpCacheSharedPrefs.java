package tv.ismar.daisy.core.preferences;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by huaijie on 11/23/15.
 */
public class HttpCacheSharedPrefs {
    public static final String SHARED_PREFS_NAME = "http_cache";

    private static Context mContext;

    public static void initialize(Context context) {
        mContext = context;
    }

    public static String getSharedPrefs(String key) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, "");
    }

    public static void setSharedPrefs(String key, String value) {
        SharedPreferences.Editor editor = mContext.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(key, value);
        editor.apply();
    }
}
