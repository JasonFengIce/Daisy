package tv.ismar.daisy;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import tv.ismar.daisy.core.ImageCache;
import tv.ismar.daisy.core.NetworkUtils;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.VodUserAgent;
import tv.ismar.daisy.dao.DBHelper;
import tv.ismar.daisy.models.ContentModel;
import tv.ismar.daisy.models.ContentModelList;
import tv.ismar.daisy.persistence.FavoriteManager;
import tv.ismar.daisy.persistence.HistoryManager;
import tv.ismar.daisy.persistence.LocalFavoriteManager;
import tv.ismar.daisy.persistence.LocalHistoryManager;
import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;


public class VodApplication extends Application {

	private static final String TAG = "VodApplication";
	
	public static final String content_model_api = "/static/meta/content_model.json";
	public ContentModel[] mContentModel;
	
	private static final int CORE_POOL_SIZE = 5;
	private ExecutorService mExecutorService;
	
	/**
	 * Use to cache the AsyncImageView's bitmap in memory, When application memory is low, the cache will be recovered.
	 */
	private ImageCache mImageCache;
	private ArrayList<WeakReference<OnLowMemoryListener>> mLowMemoryListeners;
	
	public VodApplication() {
		mLowMemoryListeners = new ArrayList<WeakReference<OnLowMemoryListener>>();
		mActivityPool = new ConcurrentHashMap<String, Activity>();
	}
	
	private HistoryManager mHistoryManager;
	
	private FavoriteManager mFavoriteManager;
	
	private DBHelper mDBHelper;
	
	private ConcurrentHashMap<String, Activity> mActivityPool;
	
	private boolean isFinish = true;
	private TelephonyManager tm;
	public void removeActivtyFromPool(String tag) {
		Activity a = mActivityPool.remove(tag);
		Log.d(TAG, "remove activity: "+a);
		if(mActivityPool.size()==0){
			isFinish = false;
		}
	}
	
	public void addActivityToPool(String tag, Activity activity) {
		Log.d(TAG, "add activity: "+activity);
		mActivityPool.put(tag, activity);
		if(!isFinish){
			new Thread(mUpLoadLogRunnable).start();
			isFinish = true;
		}
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		getContentModelFromAssets();
		getNewContentModel();
		registerReceiver(mCloseReceiver, new IntentFilter("com.amlogic.dvbplayer.homekey"));
		registerReceiver(mSleepReceiver, new IntentFilter("com.alpha.lenovo.powerKey"));
	}

	public void getContentModelFromAssets() {
		AssetManager assetManager = getAssets();
		SimpleRestClient restClient = new SimpleRestClient();
		try {
			InputStream in = assetManager.open("content_model.json");
			ContentModelList contentModelList = restClient.getContentModelList(in);
			if(contentModelList!=null) {
				mContentModel = contentModelList.zh_CN;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public void getNewContentModel(){
		
		new Thread(mGetNewContentModelTask).start();
		new Thread(mUpLoadLogRunnable).start();
	}
	
	private Runnable mGetNewContentModelTask = new Runnable() {
		
		@Override
		public void run() {
			SimpleRestClient restClient = new SimpleRestClient();
			
			ContentModelList contentModelList = restClient.getContentModelLIst(content_model_api);
			if(contentModelList!=null){
				mContentModel = contentModelList.zh_CN;
			}
			
		}
	};
	
	
	private Runnable mUpLoadLogRunnable = new Runnable(){

		@Override
		public void run() {
			// TODO Auto-generated method stub			
			while(isFinish){		
					try {
						Thread.sleep(10000);
						Log.i("zhangjiqiang", "upload123");
						NetworkUtils.LogUpLoad(getApplicationContext());
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
				}				
			}
		}
		
	};

	/**
	 * Return this application {@link DBHelper}
	 * @return The application {@link DBHelper}
	 */
	public DBHelper getDBHelper() {
		if(mDBHelper==null) {
			mDBHelper = new DBHelper(this);
		}
		return mDBHelper;
	}
	
	/**
	 * Return this application {@link HistoryManager}
	 * @return The application {@link HistoryManager}
	 */
	public HistoryManager getHistoryManager() {
		if(mHistoryManager==null) {
			mHistoryManager = new LocalHistoryManager(this);
		}
		return mHistoryManager;
	}
	
	public FavoriteManager getFavoriteManager() {
		if(mFavoriteManager == null) {
			mFavoriteManager = new LocalFavoriteManager(this);
		}
		return mFavoriteManager;
	}
	
	private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "GreenDroid thread #" + mCount.getAndIncrement());
        }
    };
	/**
     * Return an ExecutorService (global to the entire application) that may be
     * used by clients when running long tasks in the background.
     * 
     * @return An ExecutorService to used when processing long running tasks
     */
    public ExecutorService getExecutor() {
        if (mExecutorService == null) {
            mExecutorService = Executors.newFixedThreadPool(CORE_POOL_SIZE, sThreadFactory);
        }
        return mExecutorService;
    }
    
    /**
     * Return this application {@link ImageCache}.
     * 
     * @return The application {@link ImageCache}
     */
    public ImageCache getImageCache() {
        if (mImageCache == null) {
            mImageCache = new ImageCache(this);
        }
        return mImageCache;
    }
    /**
     * Used for receiving low memory system notification. You should definitely
     * use it in order to clear caches and not important data every time the
     * system needs memory.
     * 
     * @author Cyril Mottier
     * @see GDApplication#registerOnLowMemoryListener(OnLowMemoryListener)
     * @see GDApplication#unregisterOnLowMemoryListener(OnLowMemoryListener)
     */
    public static interface OnLowMemoryListener {
        
        /**
         * Callback to be invoked when the system needs memory.
         */
        public void onLowMemoryReceived();
    }
    
    /**
     * Add a new listener to registered {@link OnLowMemoryListener}.
     * 
     * @param listener The listener to unregister
     * @see OnLowMemoryListener
     */
    public void registerOnLowMemoryListener(OnLowMemoryListener listener) {
        if (listener != null) {
            mLowMemoryListeners.add(new WeakReference<OnLowMemoryListener>(listener));
        }
    }

    /**
     * Remove a previously registered listener
     * 
     * @param listener The listener to unregister
     * @see OnLowMemoryListener
     */
    public void unregisterOnLowMemoryListener(OnLowMemoryListener listener) {
        if (listener != null) {
            int i = 0;
            while (i < mLowMemoryListeners.size()) {
                final OnLowMemoryListener l = mLowMemoryListeners.get(i).get();
                if (l == null || l == listener) {
                    mLowMemoryListeners.remove(i);
                } else {
                    i++;
                }
            }
        }
    }
	@Override
	public void onTerminate() {
		super.onTerminate();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		int i = 0;
        while (i < mLowMemoryListeners.size()) {
            final OnLowMemoryListener listener = mLowMemoryListeners.get(i).get();
            if (listener == null) {
                mLowMemoryListeners.remove(i);
            } else {
                listener.onLowMemoryReceived();
                i++;
            }
        }
	}

	@Override
	public void onTrimMemory(int level) {
		// TODO Auto-generated method stub
		super.onTrimMemory(level);
	}
	
	private BroadcastReceiver mCloseReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "Home key is pressed!");
			finishVOD();
		}
		
	};
	
	private void finishVOD() {
		ConcurrentHashMap<String, Activity> activityPool =(ConcurrentHashMap<String, Activity>)mActivityPool;
		for(String tag: activityPool.keySet()) {
			Activity activity = activityPool.get(tag);
			if(activity!=null) {
				activity.finish();
			}
		}
		activityPool.clear();
	}
	
	private BroadcastReceiver mSleepReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			finishVOD();
		}
	};
   public int getheightPixels(Context context){
	   int H = 0;
//	   DisplayMetrics mDisplayMetrics = new DisplayMetrics();
//	   ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
//	   H = mDisplayMetrics.heightPixels;
	   int ver = Build.VERSION.SDK_INT;
	   DisplayMetrics dm = new DisplayMetrics(); 
	   android.view.Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
	   display.getMetrics(dm);
	   if(ver<13){
		   H = dm.heightPixels;
	   }
	   else if(ver==13){
		   try {
			Method mt = display.getClass().getMethod("getRealHeight");
			 H = (Integer) mt.invoke(display);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			H = dm.heightPixels;
			e.printStackTrace();
		} 
	   }
	   else if(ver>13){
           try {
			Method mt = display.getClass().getMethod("getRawHeight");
	        H = (Integer) mt.invoke(display); 
		} catch (Exception e) {
			// TODO Auto-generated catch block
			H = dm.heightPixels;
			e.printStackTrace();
		}   
	   }
	   return H;
   }
   

}
