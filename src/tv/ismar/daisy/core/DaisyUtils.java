package tv.ismar.daisy.core;

import android.content.Context;
import android.content.Intent;
import tv.ismar.daisy.VodApplication;
import tv.ismar.daisy.dao.DBHelper;
import tv.ismar.daisy.persistence.FavoriteManager;
import tv.ismar.daisy.persistence.HistoryManager;

import java.util.concurrent.ExecutorService;

public class DaisyUtils {

	private DaisyUtils() {
	}

	/**
	 * Return the current {@link VodApplication}
	 * 
	 * @param context
	 *            The calling context
	 * @return The {@link VodApplication} the given context is linked to.
	 */
	public static VodApplication getVodApplication(Context context) {
		return (VodApplication) context.getApplicationContext();
	}

	/**
	 * Return the {@link VodApplication} image cache
	 * 
	 * @param context
	 *            The calling context
	 * @return The image cache of the current {@link VodApplication}
	 */
	public static ImageCache getImageCache(Context context) {
		return getVodApplication(context).getImageCache();
	}

	/**
	 * Return the {@link VodApplication} executors pool.
	 * 
	 * @param context
	 *            The calling context
	 * @return The executors pool of the current {@link VodApplication}
	 */
	public static ExecutorService getExecutor(Context context) {
		return getVodApplication(context).getExecutor();
	}

	public static DBHelper getDBHelper(Context context) {
		return getVodApplication(context).getDBHelper();
	}

	public static HistoryManager getHistoryManager(Context context) {
		return getVodApplication(context).getHistoryManager();
	}

	public static FavoriteManager getFavoriteManager(Context context) {
		return getVodApplication(context).getFavoriteManager();
	}

	public static void gotoSpecialPage(Context context, String contentMode,String url,String from) {
		Intent intent = new Intent();
		if ("variety".equals(contentMode)||"entertainment".equals(contentMode)) {
			intent.setAction("tv.ismar.daisy.EntertainmentItem");
			intent.putExtra("title", "娱乐综艺");
		} else if ("movie".equals(contentMode)) {
			intent.setAction("tv.ismar.daisy.PFileItem");
			intent.putExtra("title", "电影");
		}
        else if("package".equals(contentMode)){
            intent.setAction("tv.ismar.daisy.packageitem");
            intent.putExtra("title", "礼包详情");
        }else {
			intent.setAction("tv.ismar.daisy.Item");
		}
        intent.putExtra("url", url);
        intent.putExtra("fromPage",from);
		context.startActivity(intent);
	}

}
