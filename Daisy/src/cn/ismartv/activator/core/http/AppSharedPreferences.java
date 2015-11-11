package cn.ismartv.activator.core.http;

import android.content.Context;
import android.content.SharedPreferences;
import cn.ismartv.activator.Activator;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by huaijie on 14-10-23.
 */
public class AppSharedPreferences {
    private static final String TAG = "AppSharedPreferences";
    private static AppSharedPreferences instance;
    private static Context mContext;

    private AppSharedPreferences() {

    }

    public static AppSharedPreferences getInstance(Context context) {
        if (null == instance || null == context) {
            instance = new AppSharedPreferences();
            mContext = context;
        }
        return instance;
    }


    public void setPackageInfo(String string) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(Activator.APP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String[] strs = string.split("&");
        try {
            editor.putString("expiry_date", URLDecoder.decode(strs[0].split("=")[1], "utf-8"));
            editor.putString("package", URLDecoder.decode((strs[1].split("=")[1]), "utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        editor.apply();
    }

    public Map<String, String> getPackageInfo() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(Activator.APP_NAME, Context.MODE_PRIVATE);
        Map<String, String> map = new HashMap<String, String>();
        map.put("expiry_date", sharedPreferences.getString("expiry_date", ""));
        map.put("package", sharedPreferences.getString("package", ""));
        return map;
    }

}
