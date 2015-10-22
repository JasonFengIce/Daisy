package tv.ismar.daisy.core.update;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.nfc.Tag;

import com.activeandroid.util.Log;

import java.util.HashMap;

import tv.ismar.daisy.AppConstant;
import tv.ismar.daisy.core.client.IsmartvHttpClient;
import tv.ismar.daisy.core.client.IsmartvUrlClient;
import tv.ismar.daisy.utils.HardwareUtils;

/**
 * Created by huaijie on 10/22/15.
 */
public class AppUpdateUtilsV2 {
    private static final String TAG = "AppUpdateUtilsV2";
    private static final String APP_UPDATE_API_V2 = "/api/v2/upgrade/";


    private static AppUpdateUtilsV2 mInstance;
    private Context mContext;

    private AppUpdateUtilsV2(Context context) {
        mContext = context;
    }

    public static AppUpdateUtilsV2 getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new AppUpdateUtilsV2(context);
        }
        return mInstance;
    }


    public void checkAppUpdate(String host, String sn, String manu, String location) {


        //当前apk版本号
        int currentApkVersionCode = fetchVersionCode();

        //机器型号
        String modelName = HardwareUtils.getModelName();

        //请求qpi
        String api = host + APP_UPDATE_API_V2;

        //请求参数
        HashMap<String, String> paramters = new HashMap<>();
        paramters.put("sn", sn);
        paramters.put("manu", manu);
        paramters.put("app", AppConstant.APP_NAME);
        paramters.put("modalname", modelName);
        paramters.put("loc", location);
        paramters.put("ver", String.valueOf(currentApkVersionCode));
//


//        new IsmartvHttpClient(mContext).doRequest(IsmartvHttpClient.Method.GET, api, paramters, new IsmartvHttpClient.CallBack() {
//            @Override
//            public void onSuccess(String result) {
//                Log.i(TAG, "checkAppUpdate: " + result);
//            }
//
//            @Override
//            public void onFailed(Exception exception) {
//
//            }
//        });


        new IsmartvUrlClient().doNormalRequest(IsmartvUrlClient.Method.GET, api, paramters, new IsmartvUrlClient.CallBack() {
            @Override
            public void onSuccess(String result) {
                Log.i(TAG, "checkAppUpdate: " + result);
            }

            @Override
            public void onFailed(Exception exception) {

            }
        });
    }

    private int fetchVersionCode() {
        int versionCode = 0;

        try {
            PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            versionCode = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            android.util.Log.e(TAG, "can't find this application!!!");
        }
        return versionCode;
    }


}
