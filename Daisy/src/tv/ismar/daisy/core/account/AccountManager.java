package tv.ismar.daisy.core.account;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.ArrayList;

import tv.ismar.daisy.core.SimpleRestClient;

/**
 * Created by huaijie on 10/16/15.
 */
public class AccountManager {
    private static final String TAG = "AccountManager";

    private static final String ACCOUNT_SHARED_PREFS = "Daisy";
    private static final String MOBILE_NUMBER = "mobile_number";
    private static final String AUTH_TOKEN = "auth_token";
    private static AccountManager instance;

    private ArrayList<AccountChangeListener> mAccountChangeListeners = new ArrayList<>();
    private Context mContext;
    private SharedPreferences mAccountSharedPreferences;


    public AccountManager(Context context) {
        mContext = context;
        mAccountSharedPreferences = mContext.getSharedPreferences(ACCOUNT_SHARED_PREFS, Context.MODE_PRIVATE);
        mAccountSharedPreferences.registerOnSharedPreferenceChangeListener(mSharedPreferenceChangeListener);
    }

    public static AccountManager getInstance(Context context) {
        if (instance == null) {
            instance = new AccountManager(context);
        }
        return instance;
    }


    public void commonLogin() {

    }


    public void commonLogout() {
        SharedPreferences.Editor editor = mAccountSharedPreferences.edit();
        editor.putString(MOBILE_NUMBER, "");
        editor.putString(AUTH_TOKEN, "");
        editor.apply();

        SimpleRestClient.access_token = "";
        SimpleRestClient.mobile_number = "";
    }

    public void addAccounChangeListener(AccountChangeListener accountChangeListener) {
        mAccountChangeListeners.add(accountChangeListener);
    }

    public void clearAccounChangeListener() {
        mAccountChangeListeners.clear();
    }

    private SharedPreferences.OnSharedPreferenceChangeListener mSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            String mobileNumber = sharedPreferences.getString("mobile_number", "");
            String authToken = sharedPreferences.getString("auth_token", "");
            if (TextUtils.isEmpty(mobileNumber) && TextUtils.isEmpty(authToken)) {
                for (AccountChangeListener accountChangeListener : mAccountChangeListeners) {
                    accountChangeListener.onLogout();
                }
            } else {
                for (AccountChangeListener accountChangeListener : mAccountChangeListeners) {
                    accountChangeListener.onLogin();
                }
            }
        }
    };

    public boolean isLogin() {
        String mobileNumber = mAccountSharedPreferences.getString("mobile_number", "");
        String authToken = mAccountSharedPreferences.getString("auth_token", "");
        if (TextUtils.isEmpty(mobileNumber) && TextUtils.isEmpty(authToken)) {
            return false;
        } else {
            return true;
        }
    }
}
