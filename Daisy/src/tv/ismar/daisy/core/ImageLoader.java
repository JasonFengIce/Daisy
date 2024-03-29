package tv.ismar.daisy.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class ImageLoader {

	private static final String TAG = "ImageLoader";
	
	/**
     * @author Cyril Mottier
     */
    public static interface ImageLoaderCallback {

        void onImageLoadingStarted(ImageLoader loader);

        void onImageLoadingEnded(ImageLoader loader, Bitmap bitmap);

        void onImageLoadingFailed(ImageLoader loader, Throwable exception);
    }
    
    private static final int ON_START = 0x100;
    private static final int ON_FAIL = 0x101;
    private static final int ON_END = 0x102;
    
    private static ExecutorService sExecutor;
    private static BitmapFactory.Options sDefaultOptions;
    private static ImageCache sImageCache;
    
    public ImageLoader(Context context) {
    	if (sImageCache == null) {
            sImageCache = DaisyUtils.getImageCache(context);
        }
        if (sExecutor == null) {
            sExecutor = DaisyUtils.getExecutor(context);
        }
        if (sDefaultOptions == null) {
        	sDefaultOptions = new BitmapFactory.Options();
        	sDefaultOptions.inDither = true;
        	sDefaultOptions.inScaled = true;
        	sDefaultOptions.inDensity = DisplayMetrics.DENSITY_MEDIUM;
        	sDefaultOptions.inTargetDensity = context.getResources().getDisplayMetrics().densityDpi;
        }
    }
    
    public Future<?> loadImage(String url, ImageLoaderCallback callback) {
        return loadImage(url, callback, null);
    }

    public Future<?> loadImage(String url, ImageLoaderCallback callback, ImageProcessor bitmapProcessor) {
        return loadImage(url, callback, bitmapProcessor, null);
    }
    
    public Future<?> loadImage(String url, ImageLoaderCallback callback, ImageProcessor bitmapProcessor, BitmapFactory.Options options) {
        return sExecutor.submit(new ImageFetcher(url, callback, bitmapProcessor, options));
    }
    
    private class ImageFetcher implements Runnable {

        private String mUrl;
        private ImageHandler mHandler;
        private ImageProcessor mBitmapProcessor;
        private BitmapFactory.Options mOptions;

        public ImageFetcher(String url, ImageLoaderCallback callback, ImageProcessor bitmapProcessor, BitmapFactory.Options options) {
            mUrl = url;
            mHandler = new ImageHandler(url, callback);
            mBitmapProcessor = bitmapProcessor;
            mOptions = options;
        }

        public void run() {

            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

            final Handler h = mHandler;
            Bitmap bitmap = null;
            Throwable throwable = null;

            h.sendMessage(Message.obtain(h, ON_START));

            try {

                if (TextUtils.isEmpty(mUrl)) {
                    throw new Exception("The given URL cannot be null or empty");
                }
                
                InputStream inputStream = null;
                
                inputStream = new URL(mUrl).openStream();

                // TODO Cyril: Use a AndroidHttpClient?
                bitmap = BitmapFactory.decodeStream(inputStream, null, (mOptions == null) ? sDefaultOptions : mOptions);
                
                if (mBitmapProcessor != null && bitmap != null) {
                    final Bitmap processedBitmap = mBitmapProcessor.processImage(bitmap);
                    if (processedBitmap != null) {
                        bitmap = processedBitmap;
                    }
                }

            } catch (Exception e) {
                // An error occured while retrieving the image
            	e.printStackTrace();
                throwable = e;
            }

            if (bitmap == null) {
                if (throwable == null) {
                    // Skia returned a null bitmap ... that's usually because
                    // the given url wasn't pointing to a valid image
                    throwable = new Exception("Skia image decoding failed");
                }
                h.sendMessage(Message.obtain(h, ON_FAIL, throwable));
            } else {
                h.sendMessage(Message.obtain(h, ON_END, bitmap));
            }
        }
    }
    
    private class ImageHandler extends Handler {

        private String mUrl;
        private ImageLoaderCallback mCallback;

        private ImageHandler(String url, ImageLoaderCallback callback) {
            mUrl = url;
            mCallback = callback;
        }

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {

                case ON_START:
                    if (mCallback != null) {
                        mCallback.onImageLoadingStarted(ImageLoader.this);
                    }
                    break;

                case ON_FAIL:
                    if (mCallback != null) {
                        mCallback.onImageLoadingFailed(ImageLoader.this, (Throwable) msg.obj);
                    }
                    break;

                case ON_END:

                    final Bitmap bitmap = (Bitmap) msg.obj;
                    sImageCache.put(mUrl, bitmap);

                    if (mCallback != null) {
                        mCallback.onImageLoadingEnded(ImageLoader.this, bitmap);
                    }
                    break;

                default:
                    super.handleMessage(msg);
                    break;
            }
        };
    }
}
