package tv.ismar.daisy.core.cache.image_loader;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

/**
 * Created by huaijie on 11/25/15.
 */
public class RequestBuilder {
    private String mPath;
    private Context context;
    private ImageLoader.LoadCallback mCallback;
    private ImageLoader.CacheStrategy mDiskCacheStrategy;
    private ImageLoader.CacheStrategy mMemoryCacheStrategy;
    private ImageLoader.CacheType mCacheType = ImageLoader.CacheType.PICASSO;

    public RequestBuilder load(Context context, String path) {
        this.context = context;
        mPath = path;
        return this;
    }

    public RequestBuilder diskCacheStrategy(ImageLoader.CacheStrategy cacheStrategy) {
        mDiskCacheStrategy = cacheStrategy;
        return this;
    }

    public RequestBuilder memoryCacheStrategy(ImageLoader.CacheStrategy cacheStrategy) {
        mMemoryCacheStrategy = cacheStrategy;
        return this;
    }

    public RequestBuilder cacheStrategy(ImageLoader.CacheType cacheType) {
        mCacheType = cacheType;
        return this;
    }

    public RequestBuilder addCallback(ImageLoader.LoadCallback callback) {
        mCallback = callback;
        return this;
    }


    public void into(ImageView imageView) {
        if (mDiskCacheStrategy == ImageLoader.CacheStrategy.DISK_NONE && mMemoryCacheStrategy == ImageLoader.CacheStrategy.MEMORY_NODE) {
            loadByNoCache(imageView);
        } else {

        }
    }

    private void loadByNoCache(ImageView imageView) {
        switch (mCacheType) {
            case PICASSO:
                loadByPicassoByNoCache(imageView);
                break;
            case GLIDE:
                loadByGlideByNoCache(imageView);
                break;
        }
    }

    public void loadByPicassoByNoCache(ImageView imageView) {
        Picasso.with(context).load(mPath).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE).into(imageView, new Callback() {
            @Override
            public void onSuccess() {
                if (mCallback != null) {
                    mCallback.onSuccess();
                }

            }

            @Override
            public void onError() {
                if (mCallback != null) {
                    mCallback.onFailure();
                }

            }
        });
    }


    public void loadByGlideByNoCache(ImageView imageView) {
        Glide.with(context)
                .load(mPath)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        if (mCallback != null) {
                            mCallback.onFailure();
                        }
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        if (mCallback != null) {
                            mCallback.onSuccess();
                        }
                        return false;
                    }
                })
                .apply(RequestOptions.skipMemoryCacheOf(true))
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                .into(imageView);
    }
}
