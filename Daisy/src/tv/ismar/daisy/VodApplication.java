package tv.ismar.daisy;

import android.app.Activity;
import android.content.*;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.IBinder;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;

import cn.ismar.daisy.pad.lockscreenservice.Setlockscreenservice;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import cn.ismartv.injectdb.library.ActiveAndroid;
import cn.ismartv.injectdb.library.app.Application;
import tv.ismar.daisy.core.ImageCache;
import tv.ismar.daisy.core.MessageQueue;
import tv.ismar.daisy.core.NetworkUtils;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.advertisement.AdvertisementManager;
import tv.ismar.daisy.core.cache.CacheManager;
import tv.ismar.daisy.core.client.HttpManager;
import tv.ismar.daisy.core.client.IsmartvUrlClient;
import tv.ismar.daisy.core.preferences.AccountSharedPrefs;
import tv.ismar.daisy.core.preferences.AppSharedPrefs;
import tv.ismar.daisy.dao.DBHelper;
import tv.ismar.daisy.models.ContentModel;
import tv.ismar.daisy.models.ContentModelList;
import tv.ismar.daisy.persistence.FavoriteManager;
import tv.ismar.daisy.persistence.HistoryManager;
import tv.ismar.daisy.persistence.LocalFavoriteManager;
import tv.ismar.daisy.persistence.LocalHistoryManager;


public class VodApplication extends Application {
    private static final String TAG = "VodApplication";

    public final static String SLEEP_INTENT = "org.videolan.vlc.SleepIntent";
    public final static String INCOMING_CALL_INTENT = "org.videolan.vlc.IncomingCallIntent";
    public final static String CALL_ENDED_INTENT = "org.videolan.vlc.CallEndedIntent";


    public static final String domain = "";
    public static final String ad_domain = "ad_domain";
    public ContentModel[] mContentModel;
    public static final String LOGIN_STATE = "loginstate";
    public static String AUTH_TOKEN = "auth_token";
    public static String MOBILE_NUMBER = "mobile_number";
    public static String DEVICE_TOKEN = "device_token";
    public static String SN_TOKEN = "sntoken";
    public static String DOMAIN = "domain";
    public static String LOG_DOMAIN = "logmain";
    public static String LOCATION_INFO = "location_info";
    public static String LOCATION_PROVINCE = "location_province";
    public static String LOCATION_CITY = "location_city";
    public static String LOCATION_DISTRICT = "location_district";
    public static String BESTTV_AUTH_BIND_FLAG = "besttv_auth_bind_flag";
    private static final int CORE_POOL_SIZE = 5;
    public static String NEWEST_ENTERTAINMENT = "newestentertainment";
    public static String OPENID = "openid";
    public static final String CACHED_LOG = "cached_log";
    private ExecutorService mExecutorService;
    public static String apiDomain = "http://skytest.tvxio.com";
    // public static float rate = 1;
    /**
     * Use to cache the AsyncImageView's bitmap in memory, When application memory is low, the cache will be recovered.
     */
    private ImageCache mImageCache;
    private ArrayList<WeakReference<OnLowMemoryListener>> mLowMemoryListeners;
    private static SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;

    public static final String PREFERENCE_FILE_NAME = "Daisy";

    public SharedPreferences getPreferences() {
        return mPreferences;
    }

    public SharedPreferences.Editor getEditor() {
        return mEditor;
    }


    private static VodApplication instance;
    private Setlockscreenservice nativeservice;
    private Random random=new Random();
    public static YogaBroadcastReceiver receiver;
    public void load(Context a) {
        try {
            mPreferences = a.getSharedPreferences(PREFERENCE_FILE_NAME, 0);
            mEditor = mPreferences.edit();
            Set<String> cached_log = mPreferences.getStringSet(CACHED_LOG, null);
            mEditor.remove(CACHED_LOG).commit();
            if (!isFinish) {
                new Thread(mUpLoadLogRunnable).start();
                isFinish = true;
            }
            if (cached_log != null) {
                Iterator<String> it = cached_log.iterator();
                while (it.hasNext()) {
                    MessageQueue.addQueue(it.next());
                }
            }
        } catch (Exception e) {
            System.out.println("load(Activity a)=" + e);
        }
    }

    public boolean save() {
        return mEditor.commit();
    }

    public static void setDevice_Token() {
        SimpleRestClient.device_token = mPreferences.getString(VodApplication.DEVICE_TOKEN, "");
    }

    public VodApplication() {
        mLowMemoryListeners = new ArrayList<WeakReference<OnLowMemoryListener>>();
        mActivityPool = new ConcurrentHashMap<String, Activity>();
    }

    private HistoryManager mHistoryManager;

    private FavoriteManager mFavoriteManager;

    private DBHelper mDBHelper;

    private ConcurrentHashMap<String, Activity> mActivityPool;

    private boolean isFinish = true;

    public void removeActivtyFromPool(String tag) {
        Activity a = mActivityPool.remove(tag);
        Log.d(TAG, "remove activity: " + a);
        if (mActivityPool.size() == 0) {
            isFinish = false;
        }
    }

    public void addActivityToPool(String tag, Activity activity) {
        Log.d(TAG, "add activity: " + activity);
        mActivityPool.put(tag, activity);
        if (!isFinish) {
            new Thread(mUpLoadLogRunnable).start();
            isFinish = true;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        HttpManager.initialize(this);
        ActiveAndroid.initialize(this, true);
        IsmartvUrlClient.initializeWithContext(this);
        CacheManager.initialize(this);
        AccountSharedPrefs.initialize(this);
        AppSharedPrefs.initialize(this);


        AdvertisementManager.initialize(this);
        getContentModelFromAssets();
        load(this);
        registerReceiver(mCloseReceiver, new IntentFilter("com.amlogic.dvbplayer.homekey"));
        registerReceiver(mSleepReceiver, new IntentFilter("com.alpha.lenovo.powerKey"));
        SharedPreferences sharedPreferences = getSharedPreferences("account", Context.MODE_WORLD_READABLE);
        apiDomain = sharedPreferences.getString("api_domain", "http://skytest.tvxio.com");
        Intent intent = new Intent("cn.ismar.daisy.pad.setlockscreenservice");
        intent.setPackage("cn.ismar.daisy.pad");
        bindService(intent, mConnection, BIND_AUTO_CREATE);
        receiver = new YogaBroadcastReceiver();
        IntentFilter filter=new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(receiver,filter);
    }


    public static Context getAppContext() {
        return instance;
    }

    /**
     * @return the main resources from the Application
     */
    public static Resources getAppResources() {
        return instance.getResources();
    }

    public void getContentModelFromAssets() {
        AssetManager assetManager = getAssets();
        SimpleRestClient restClient = new SimpleRestClient();
        try {
            InputStream in = assetManager.open("content_model.json");
            ContentModelList contentModelList = restClient.getContentModelList(in);
            if (contentModelList != null) {
                mContentModel = contentModelList.zh_CN;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getDeviceId(Context context) {
        String deviceId = null;
        try {
            TelephonyManager tm = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            deviceId = tm.getDeviceId();

        } catch (Exception e) {

            e.printStackTrace();
        }
        return deviceId;
    }

    String sn;


    public void getNewContentModel() {

//		new Thread(mGetNewContentModelTask).start();
        new Thread(mUpLoadLogRunnable).start();
    }


    private Runnable mUpLoadLogRunnable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            while (isFinish) {
                try {
                    Thread.sleep(10000);
//                    synchronized (MessageQueue.async) {
                    // Thread.sleep(900000);


                    ArrayList<String> list = MessageQueue.getQueueList();
                    int i;
                    JSONArray s = new JSONArray();
                    if (list.size() > 0) {
                        for (i = 0; i < list.size(); i++) {
                            JSONObject obj;
                            try {
                                Log.i("qazwsx", "json item==" + list.get(i).toString());
                                obj = new JSONObject(list.get(i).toString());
                                s.put(obj);
                            } catch (JSONException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }

                        }
                        if (i == list.size()) {
                            MessageQueue.remove();
                            NetworkUtils.LogSender(s.toString());
                            Log.i("qazwsx", "json array==" + s.toString());
                            Log.i("qazwsx", "remove");
                        }
                    } else {
                        Log.i("qazwsx", "queue is no elements");
                    }
//                    }

                    //NetworkUtils.LogUpLoad(getApplicationContext());
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            Log.i("qazwsx", "Thread is finished!!!");
        }

    };

    /**
     * Return this application {@link DBHelper}
     *
     * @return The application {@link DBHelper}
     */
    public DBHelper getDBHelper() {
        if (mDBHelper == null) {
            mDBHelper = new DBHelper(this);
        }
        return mDBHelper;
    }

    /**
     * Return this application {@link HistoryManager}
     *
     * @return The application {@link HistoryManager}
     */
    public HistoryManager getHistoryManager() {
        if (mHistoryManager == null) {
            mHistoryManager = new LocalHistoryManager(this);
        }
        return mHistoryManager;
    }

    public FavoriteManager getFavoriteManager() {
        if (mFavoriteManager == null) {
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
        ConcurrentHashMap<String, Activity> activityPool = (ConcurrentHashMap<String, Activity>) mActivityPool;
        for (String tag : activityPool.keySet()) {
            Activity activity = activityPool.get(tag);
            if (activity != null) {
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


    public float getRate(Context context) {
        DisplayMetrics metric = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(metric);
        int densityDpi = metric.densityDpi; // 屏幕密度DPI（120 / 160 / 240）
        float rate = (float) densityDpi / (float) 160;
        return rate;
    }
    public  ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            nativeservice = Setlockscreenservice.Stub.asInterface(service);
        }

        public void onServiceDisconnected(ComponentName className) {
        }
    };
    private void testLock() {
        new Thread() {
            public void run() {
                Log.e("yoga==","testlock");
                HttpURLConnection httpConn = null;
                InputStreamReader inputStreamReader = null;
                try {
                    URL connURL = new URL(SimpleRestClient.root_url+"/api/tv/homepage/lockscreen/"+Settings.System.getString(getContentResolver(),"push_messages_favors_id").split("\\|").length+"/");
                    httpConn = (HttpURLConnection) connURL.openConnection();
                    httpConn.setRequestProperty("Accept", "application/json");
//                    httpConn.setRequestProperty("User-Agent", userAgent);
                    httpConn.setConnectTimeout(10000);
                    httpConn.setReadTimeout(10000);
                    httpConn.connect();
                    int code = httpConn.getResponseCode();
                    inputStreamReader = new InputStreamReader(
                            httpConn.getInputStream(), "UTF-8");
                    StringBuffer json = new StringBuffer();
                    String line = null;
                    try {
                        BufferedReader reader = new BufferedReader(inputStreamReader);
                        while ((line = reader.readLine()) != null)
                            json.append(line);
                    } catch (Exception e) {
                        System.out.println(e.toString());
                    }
                    inputStreamReader.close();
                    httpConn.disconnect();
                    JSONArray object = new JSONArray(json.toString());
                    ArrayList<String> prefrence = getPreferenceChannels(getContentResolver());
                    String channel=prefrence.get(random.nextInt(prefrence.size()));
                    for(int i =0;i<object.length();i++){
                        JSONObject element = object.getJSONObject(i);
                        String url = element.getString("url");
                        String image = element.getString("image");
                        String content_model = element.getString("content_model");
                        Log.e("yoga==",channel+"&"+content_model);
                        if(channel.equals(content_model)) {
                            Log.e("yoga==",url);
                            AccountSharedPrefs accountSharedPrefs = AccountSharedPrefs.getInstance();
                            accountSharedPrefs.setSharedPrefs("lock_url", url);
                            accountSharedPrefs.setSharedPrefs("lock_content_model", content_model);
                            nativeservice.setLockScreen(image);
                            break;
                        }
                    }
                } catch (Exception e) {
                    Log.e("Exception", e.toString());
                }
            }
        }.start();
    }
    private ArrayList<String> getPreferenceChannels(ContentResolver resolver) {
        ArrayList<String> prefrence = new ArrayList<String>();
        String[] content_model=Settings.System.getString(resolver,"push_messages_favors_content_model").split("\\|");
        String[] ischecked=Settings.System.getString(resolver,"push_messages_favors_ischecked").split("\\|");
        for (int i = 0; i <ischecked.length ; i++) {
           if(Integer.parseInt(ischecked[i])==1){
               prefrence.add(content_model[i]);
           }

        }
        return prefrence;
    }
    public class YogaBroadcastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            testLock();
        }
    }

}
