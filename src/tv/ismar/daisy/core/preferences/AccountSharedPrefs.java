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

    public static final String APP_UPDATE_DOMAIN = "app_update_domain";
    public static final String LOG_DOMAIN = "log_domain";
    public static final String API_DOMAIN = "api_domain";
    public static final String ADVERTISEMENT_DOMAIN = "advertisement_domain";

    public static final String DEVICE_TOKEN = "device_token";
    public static final String SN_TOKEN = "sn_token";

    public static final String PACKAGE_INFO = "package_info";
    public static final String EXPIRY_DATE = "expiry_date";

    public static final String PROVINCE = "province";
    public static final String CITY = "city";
    public static final String PROVINCE_PY = "province_py";
    public static final String ISP = "isp";
    public static final String IP = "ip";


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
