package tv.ismar.daisy.core.preferences;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by huaijie on 11/16/15.
 */
public class AppSharedPrefs {
    private static final String SHARED_PREFS_NAME = "daisy";

    private static AppSharedPrefs instance;

    private static Context mContext;

    private SharedPreferences mSharedPreferences;


    public AppSharedPrefs(String name) {
        mSharedPreferences = mContext.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    public static AppSharedPrefs getInstance() {
        if (instance == null) {
            instance = new AppSharedPrefs(SHARED_PREFS_NAME);
        }
        return instance;
    }

    public SharedPreferences getSharedPreferences() {
        return mSharedPreferences;
    }

    public static void initialize(Context context) {
        mContext = context;
        HttpCacheSharedPrefs.initialize(context);
    }

    public String getSharedPrefs(String key) {
        return mSharedPreferences.getString(key, "");
    }

    public void setSharedPrefs(String key, String value) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

}
