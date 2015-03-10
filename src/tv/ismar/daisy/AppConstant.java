package tv.ismar.daisy;

import android.util.Log;
import retrofit.RestAdapter;

/**
 * Created by huaijie on 3/9/15.
 */
public class AppConstant {

    public static final String API_HOST = "http://wx.api.tvxio.com/";

    public static final boolean DEBUG = true;

    public static final RestAdapter.LogLevel LOG_LEVEL = DEBUG ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE;

}
