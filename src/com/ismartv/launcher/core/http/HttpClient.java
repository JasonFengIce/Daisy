package com.ismartv.launcher.core.http;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by <huaijiefeng@gmail.com> on 9/2/14.
 */
public class HttpClient {
    private static final String TAG = "HttpClient";
    private static HttpClient instance;

    private HttpClient() {
    }

    public static HttpClient getInstance() {
        if (null == instance) {
            instance = new HttpClient();
        }
        return instance;
    }


    public ImageLoader getImageLoader(Context context) {
        RequestQueue mQueue = Volley.newRequestQueue(context);
        ImageLoader imageLoader = new ImageLoader(mQueue, new BitmapCache());
        return imageLoader;
    }

    public class BitmapCache implements ImageLoader.ImageCache {
        private LruCache<String, Bitmap> mCache;

        public BitmapCache() {
            int maxSize = 100 * 1024 * 1024;
            mCache = new LruCache<String, Bitmap>(maxSize) {
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    return bitmap.getRowBytes() * bitmap.getHeight();
                }
            };
        }

        @Override
        public Bitmap getBitmap(String url) {
            return mCache.get(url);
        }

        @Override
        public void putBitmap(String url, Bitmap bitmap) {
            mCache.put(url, bitmap);
        }
    }

    public Bitmap getURLimage(String url) {
        Bitmap bmp = null;
        try {
            URL myurl = new URL(url);
            // 获得连接
            HttpURLConnection conn = (HttpURLConnection) myurl.openConnection();
            conn.setConnectTimeout(6000);//设置超时
            conn.setDoInput(true);
            conn.setUseCaches(false);//不缓存
            conn.connect();
            InputStream is = conn.getInputStream();//获得图片的数据流
            bmp = BitmapFactory.decodeStream(is);
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bmp;
    }


}
