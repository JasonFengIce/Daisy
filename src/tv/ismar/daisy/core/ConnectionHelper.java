package tv.ismar.daisy.core;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectionHelper {

	/**
	 * 判断当前网络是否连接上
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isNetWorkAvailable(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) {
			NetworkInfo netWorkInfo = connectivity.getActiveNetworkInfo();
			return netWorkInfo != null && netWorkInfo.isAvailable();
		}
		return false;
	}
}
