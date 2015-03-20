package tv.ismar.daisy;

import retrofit.RestAdapter;

/**
 * Created by huaijie on 3/9/15.
 */
public class AppConstant {
    public static final String VOD_PLAYER_COMPLETE_ACTION = "cn.ismartv.vod.action.PLAY_COMPLETE";
    public static final String APP_UPDATE_ACTION = "cn.ismartv.vod.action.app_update";

   

    public static final String CDN_API_HOST = "http://wx.api.tvxio.com";

    public static final boolean DEBUG = true;

    public static final RestAdapter.LogLevel LOG_LEVEL = DEBUG ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE;

}