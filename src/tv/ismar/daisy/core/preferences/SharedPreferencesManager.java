package tv.ismar.daisy.core.preferences;

import android.content.Context;

/**
 * Created by huaijie on 8/3/15.
 */
public class SharedPreferencesManager {
    private static final String TAG = "SharedPreferencesManager";

    private static final String ACCOUNT_PREFS = "account_prefs";

    private static Context mContext;


    public static void initialize(Context context) {
        mContext = context;
    }


}
