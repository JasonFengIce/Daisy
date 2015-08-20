package tv.ismar.daisy.qiyimediaplayer;

import android.util.Log;
import com.qiyi.video.utils.LogUtils;

public class MyLogUtils {
    private static boolean mIsDebug = LogUtils.mIsDebug;

    public static void setDebug(boolean isDebug) {
        mIsDebug = true;
        if (isDebug) {
            i("###", "log is open");
        } else {
            i("###", "log is close");
        }
        mIsDebug = isDebug;
    }

    public static void d(String tag, Object object) {
        try {
//          if (!mIsDebug) {
//              return;
//          }
            Log.d(tag, object.toString());
        } catch (Exception e) {
        }
    }

    public static void i(String tag, Object object) {
        try {
//          if (!mIsDebug) {
//              return;
//          }
            Log.i(tag, object.toString());
        } catch (Exception e) {
        }
    }

    public static void w(String tag, Object object) {
        try {
            if (!mIsDebug) {
                return;
            }
            Log.w(tag, object.toString());
        } catch (Exception e) {
        }
    }

    public static void e(String tag, Object object) {
        try {
            if (!mIsDebug) {
                return;
            }
            Log.e(tag, object.toString());
        } catch (Exception e) {
        }
    }
    
    public static void d(String tag, Object object, Throwable t) {
        try {
//          if (!mIsDebug) {
//              return;
//          }
            Log.d(tag, object.toString(), t);
        } catch (Exception e) {
        }
    }

    public static void i(String tag, Object object, Throwable t) {
        try {
//          if (!mIsDebug) {
//              return;
//          }
            Log.i(tag, object.toString(), t);
        } catch (Exception e) {
        }
    }

    public static void w(String tag, Object object, Throwable t) {
        try {
            if (!mIsDebug) {
                return;
            }
            Log.w(tag, object.toString(), t);
        } catch (Exception e) {
        }
    }

    public static void e(String tag, Object object, Throwable t) {
        try {
            if (!mIsDebug) {
                return;
            }
            Log.e(tag, object.toString(), t);
        } catch (Exception e) {
        }
    }
}
