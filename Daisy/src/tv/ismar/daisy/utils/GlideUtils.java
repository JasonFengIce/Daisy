package tv.ismar.daisy.utils;

import android.app.Activity;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

/**
 * Created by huaijie on 11/24/15.
 */
public class GlideUtils {

    public static void load(Activity activity, String path, ImageView target) {
        Glide.with(activity)
                .load(path)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(true)
                .into(target);
    }

    public static void load(Activity activity, String path, ImageView target, RequestListener listener) {
        Glide.with(activity)
                .load(path)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(true)
                .listener(listener)
                .into(target);
    }
}
