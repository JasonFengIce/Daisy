package tv.ismar.daisy.core;


import java.util.concurrent.ExecutorService;

import tv.ismar.daisy.VodApplication;

import android.content.Context;

public class DaisyUtils {

	private DaisyUtils() {
    }

    /**
     * Return the current {@link VodApplication}
     * 
     * @param context The calling context
     * @return The {@link VodApplication} the given context is linked to.
     */
    public static VodApplication getVodApplication(Context context) {
        return (VodApplication) context.getApplicationContext();
    }

    /**
     * Return the {@link VodApplication} image cache
     * 
     * @param context The calling context
     * @return The image cache of the current {@link VodApplication}
     */
    public static ImageCache getImageCache(Context context) {
        return getVodApplication(context).getImageCache();
    }

    /**
     * Return the {@link VodApplication} executors pool.
     * 
     * @param context The calling context
     * @return The executors pool of the current {@link VodApplication}
     */
    public static ExecutorService getExecutor(Context context) {
        return getVodApplication(context).getExecutor();
    }

}
