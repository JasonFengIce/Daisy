package tv.ismar.daisy.core.cache.image_loader;

import android.app.Activity;
import android.content.Context;

/**
 * Created by huaijie on 11/25/15.
 */
public class ImageLoader {

    public static RequestManager with(Activity activity) {
        RequestManager requestManager = new RequestManager(activity);
        return requestManager;
    }


    public interface LoadCallback {
        void onSuccess();

        void onFailure();
    }

    public enum CacheStrategy {
        DISK_NONE,
        MEMORY_NODE,
        DISK_CACHE,
        MEMORY_CACHE
    }

    enum CacheType {
        PICASSO,
        GLIDE
    }
}
