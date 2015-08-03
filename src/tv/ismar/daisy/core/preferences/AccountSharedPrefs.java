package tv.ismar.daisy.core.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import java.security.Key;

/**
 * Created by huaijie on 8/3/15.
 */
public class AccountSharedPrefs {
    private static final String TAG = "AccountSharedPrefs";

    private static final String SHARED_PREFS_NAME = "account";

    public static final String PROVINCE = "province";
    public static final String CITY = "city";
    public static final String PROVINCE_PY = "province_py";

    private static AccountSharedPrefs instance;
    private Context mContext;

    private SharedPreferences mSharedPreferences;

    private AccountSharedPrefs(Context context) {
        this.mContext = context;
        mSharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static AccountSharedPrefs getInstance(Context context) {
        if (instance == null) {
            instance = new AccountSharedPrefs(context);
        }
        return instance;
    }

    public SharedPreferences getSharedPreferences() {
        return mSharedPreferences;
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
