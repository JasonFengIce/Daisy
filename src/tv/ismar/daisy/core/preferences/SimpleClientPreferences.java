package tv.ismar.daisy.core.preferences;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by huaijie on 6/16/15.
 */
public class SimpleClientPreferences {
    private static final String TAG = "SimpleClientPreferences";

    private static final String PREFERENCES_NAME = "simpleclient_preferences";

    private static final String API_DOMAIN = "api_domain";
    private static final String ADVERTISEMENT_DOMAIN = "advertisement_domain";
    private static final String LOG_DOMAIN = "log_domain";
    private static final String AUTH_TOKEN = "auth_token";
    private static final String MOBILE_NUMBER = "mobile_number";
    private static final String DEVICE_TOKEN = "device_token";
    private static final String SN_TOKEN = "sn_token";
    private static final String LOCATION_INFO = "location_info";


    private Context context;

    private static SimpleClientPreferences instance;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private SimpleClientPreferences(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public static SimpleClientPreferences getInstance(Context context) {
        if (instance == null) {
            instance = new SimpleClientPreferences(context);
        }
        return instance;
    }

    public String getApiDomain() {
        return sharedPreferences.getString(API_DOMAIN, "");
    }

    public void setApiDomain(String apiDomain) {
        editor.putString(API_DOMAIN, apiDomain).apply();
    }

    public String getAdvertisementDomain() {
        return sharedPreferences.getString(ADVERTISEMENT_DOMAIN, "");
    }

    public void setAdvertisementDomain(String advertisementDomain) {
        editor.putString(ADVERTISEMENT_DOMAIN, advertisementDomain).apply();
    }

    public String getLogDomain() {
        return sharedPreferences.getString(LOG_DOMAIN, "");
    }

    public void setLogDomain(String logDomain) {
        editor.putString(LOG_DOMAIN, logDomain).apply();
    }

    public String getAuthToken() {
        return sharedPreferences.getString(AUTH_TOKEN, "");
    }

    public void setAuthToken(String authToken) {
        editor.putString(AUTH_TOKEN, authToken).apply();
    }

    public String getMobileNumber() {
        return sharedPreferences.getString(MOBILE_NUMBER, "");
    }

    public void setMobileNumber(String mobileNumber) {
        editor.putString(MOBILE_NUMBER, mobileNumber).apply();
    }

    public String getDeviceToken() {
        return sharedPreferences.getString(DEVICE_TOKEN, "");
    }

    public void setDeviceToken(String deviceToken) {
        editor.putString(DEVICE_TOKEN, deviceToken).apply();
    }



    public String getSnToken() {
        return sharedPreferences.getString(SN_TOKEN, "");
    }

    public void setSnToken(String snToken) {
        editor.putString(SN_TOKEN, snToken).apply();
    }

    public String getLocationInfo() {
        return sharedPreferences.getString(LOCATION_INFO, "");
    }

    public void setLocationInfo(String locationInfo) {
        editor.putString(LOCATION_INFO, locationInfo).apply();
    }
}
