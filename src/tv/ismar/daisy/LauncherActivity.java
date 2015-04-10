package tv.ismar.daisy;

import android.app.Activity;
import android.app.Dialog;
import android.content.*;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.*;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import cn.ismartv.activator.Activator;
import cn.ismartv.activator.data.Result;
import com.google.gson.Gson;
import com.ismartv.launcher.data.ChannelEntity;
import com.ismartv.launcher.data.VideoEntity;
import com.ismartv.launcher.ui.widget.IsmartvVideoView;
import org.json.JSONException;
import org.json.JSONObject;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.SystemFileUtil;
import tv.ismar.daisy.core.client.ClientApi;
import tv.ismar.daisy.core.service.PosterUpdateService;
import tv.ismar.daisy.core.update.AppUpdateUtils;
import tv.ismar.daisy.models.launcher.*;
import tv.ismar.daisy.ui.widget.*;
import tv.ismar.daisy.views.CustomDialog;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class LauncherActivity extends Activity implements View.OnClickListener, Activator.OnComplete {
    private static final String TAG = "LauncherActivity";
    private AppUpdateReceiver appUpdateReceiver;
    private static final int NAME = 1;
    private static final int URL = 2;
    private static final int CHANNEL = 3;
    private static final int FETCHCHANNEL = 0x01;
    private static final int FETCHFRONTPAGE = 0x02;
    private static final int FETCHWEATHER = 0x03;
    private static final int FETCHLATEST = 0x04;
    private static final int FETCHTVHOME = 0x05;
    private static final int GETDOMAIN = 0x06;

    /**
     * view
     */
    private IsmartvVideoView videoView;

    private ImageView weatherIcon;
    private TextView weatherTmp;
    private TextView todayWeatherDetail;

    private TextView tomorrowDetail;

    private String mLocalPath;
    private String mLocalDir;
    private String mRemoteUrl;
    private long readSize = 0;
    private static final int READY_BUFF = 2000 * 1024;
    private static final int CACHE_BUFF = 500 * 1024;

    private boolean isready = false;
    private boolean iserror = false;
    private int errorCnt = 0;
    private int curPosition = 0;
    private long mediaLength = 0;

    private final static int VIDEO_STATE_UPDATE = 0;
    private final static int CACHE_VIDEO_READY = 1;
    private final static int CACHE_VIDEO_UPDATE = 2;
    private final static int CACHE_VIDEO_END = 3;
    private boolean isfinished = false;
    String url = "cord.tvxio.com/v2_0/A21/dto";
    private SimpleRestClient simpleRestClient;

    private static final String KIND = "sky";
    private static final String VERSION = "1.0";
    private static final String MANUFACTURE = "sky";
    private static final String userid = "333333";
    String sn;
    private View mView;
    Activator activator;
    private DaisyImageView btn_personcenter;
    private DaisyImageView btn_search;

    private volatile boolean videoCacheIsComplete = false;

    /**
     * PopupWindow
     */
    PopupWindow updatePopupWindow;
    PopupWindow exitPopupWindow;
    PopupWindow netErrorPopupWindow;
    /**
     * views
     */
    private ChannelGridView channelGrid;
    private VerticalGuideListView linkedvideoGrid;
    private HorizontalGuideListView horizontalGuideListView;

    ArrayList<AttributeEntity> attributeDatas;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerUpdateReceiver();
        AppUpdateUtils.getInstance().checkUpdate(this);
        fetchIpLookup();
        String path = getFilesDir().getAbsolutePath();
        SystemFileUtil.appPath = path;
        DaisyUtils.getVodApplication(this).addActivityToPool(this.toString(), this);
        mView = LayoutInflater.from(this).inflate(R.layout.activity_launcher, null);
        setContentView(mView);
        initViews();
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        int height = metric.heightPixels; // 屏幕高度（像素）
        int densityDpi = metric.densityDpi; // 屏幕密度DPI（120 / 160 / 240）
        float rate = (float) densityDpi / (float) 160;
        VodApplication.rate = rate;
        String domain = DaisyUtils.getVodApplication(this).getPreferences().getString("domain", "");
        activator = Activator.getInstance(this);
        activator.setOnCompleteListener(this);
        activator.active(MANUFACTURE, KIND, VERSION);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    Dialog dialog = null;
    private DialogInterface.OnClickListener mPositiveListener;
    private DialogInterface.OnClickListener mNegativeListener;
    public static final int NETWORK_ACTIVE_FAILED = 1;

    private void showDialog(String str) {
        if (dialog == null) {
            mPositiveListener = new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    // TODO Auto-generated method stub
                    dialog.dismiss();
                    //register();
                    activator.active(MANUFACTURE, KIND, VERSION);
                }
            };
            mNegativeListener = new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    // TODO Auto-generated method stub
                    dialog.dismiss();
                    LauncherActivity.this.finish();
                }
            };
            dialog = new CustomDialog.Builder(this)
                    .setMessage(str)
                    .setPositiveButton(R.string.vod_retry, mPositiveListener)
                    .setNegativeButton(R.string.vod_ok, mNegativeListener).create();
        }
        dialog.show();
    }

    private void register() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                sn = Build.SERIAL;
                if (sn == null || (sn != null && sn.equals("unknown"))) {
                    sn = VodApplication.getDeviceId(LauncherActivity.this);
                }
                String responseCode = SimpleRestClient.readContentFromPost("register", sn);
                if (responseCode != null && responseCode.equals("200")) {
                    active();
                }
            }
        }).start();
    }

    private void active() {
        String content = SimpleRestClient.readContentFromPost("active", sn);
        if (!"".equals(content)) {
            try {
                JSONObject json = new JSONObject(content);
                String domain = json.getString("domain");
                SimpleRestClient.root_url = "http://" + domain;
                SimpleRestClient.sRoot_url = "http://" + domain;
                SimpleRestClient.ad_domain = "http://" + json.getString("ad_domain");
                DaisyUtils.getVodApplication(LauncherActivity.this).getEditor().putString("domain", SimpleRestClient.root_url);
                DaisyUtils.getVodApplication(LauncherActivity.this).getEditor().putString("ad_domain", SimpleRestClient.ad_domain);
                DaisyUtils.getVodApplication(LauncherActivity.this).save();
                mainHandler.sendEmptyMessage(GETDOMAIN);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            showDialog(getResources().getString(R.string.active_fail));
        }
    }

    private void initViews() {
        weatherIcon = (ImageView) findViewById(R.id.weather_icon);
        weatherTmp = (TextView) findViewById(R.id.weather_tmp);
        todayWeatherDetail = (TextView) findViewById(R.id.today_weather_detail);
        tomorrowDetail = (TextView) findViewById(R.id.tomorrow_detail);
        videoView = (IsmartvVideoView) findViewById(R.id.video_view);
        btn_search = (DaisyImageView) findViewById(R.id.search);
        btn_personcenter = (DaisyImageView) findViewById(R.id.user_center);
        btn_search.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent searchIntent = new Intent();
                searchIntent.setClass(LauncherActivity.this, SearchActivity.class);
                startActivity(searchIntent);
            }
        });
        btn_personcenter.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent();
                intent.setClass(LauncherActivity.this, PersonCenterActivity.class);
                startActivity(intent);
            }
        });


        channelGrid = (ChannelGridView) findViewById(R.id.channel_grid);
        linkedvideoGrid = (VerticalGuideListView) findViewById(R.id.grid_linkedvideo);
        horizontalGuideListView = (HorizontalGuideListView) findViewById(R.id.h_guide_View);

        //set listener for view
        setListener();
    }

    private void setListener() {
        linkedvideoGrid.setItemClickListener(new VerticalGuideListView.OnItemClickListener() {
            @Override
            public void onItemClick(View view) {
                Intent intent = new Intent();
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                AttributeEntity attributeEntity = (AttributeEntity) view.getTag();
                intent.setClassName("tv.ismar.daisy", "tv.ismar.daisy.ItemDetailActivity");
                intent.putExtra("url", attributeEntity.getAttributes().getUrl());
                startActivity(intent);
            }
        });
        linkedvideoGrid.setItemHoverListener(new VerticalGuideListView.OnItemHoverListener() {
            @Override
            public void onItemHover(View view) {
                AttributeEntity attributeEntity = (AttributeEntity) view.getTag();
                if (videoView.isPlaying() && videoCacheIsComplete) {
                    playVideoByTime((int) attributeEntity.getStart_time() * 1000, (int) attributeEntity.getEnd_time() * 1000);
                }

            }
        });

        videoView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (attributeDatas != null) {
                    for (AttributeEntity attributeEntity : attributeDatas) {
                        if (videoView.getCurrentPosition() > attributeEntity.getStart_time() * 1000 &&
                                videoView.getCurrentPosition() < attributeEntity.getEnd_time() * 1000) {
                            Intent intent = new Intent();
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setClassName("tv.ismar.daisy", "tv.ismar.daisy.ItemDetailActivity");
                            intent.putExtra("url", attributeEntity.getAttributes().getUrl());
                            startActivity(intent);
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        String content = (String) view.getTag();
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(content);
            intent.putExtra("title", jsonObject.getString("name"));
            intent.putExtra("url", jsonObject.getString("url"));
            intent.putExtra("channel", jsonObject.getString("channel"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        intent.setClassName("tv.ismar.daisy",
                "tv.ismar.daisy.ChannelListActivity");
        this.startActivity(intent);
    }


    private Intent intent;

    private void setFrontPage(String content) {
        try {
            Gson gson = new Gson();
            FrontPageEntity frontBeans = gson.fromJson(content.toString(),
                    FrontPageEntity.class);

            /**
             * linkedvideo
             */
            String videoId = frontBeans.getVideos().get(0).getVideo_id();
            fetchLinkedvideo(Long.parseLong(videoId));

            final Uri uri = Uri.parse(frontBeans.getVideos().get(0)
                    .getVideo_url());
            mRemoteUrl = frontBeans.getVideos().get(0).getVideo_url();
            int position = mRemoteUrl.lastIndexOf("/");
            String fileName = mRemoteUrl.substring(position + 1,
                    mRemoteUrl.length());
            String realname = fileName.substring(0, fileName.lastIndexOf("?"));
            if (SystemFileUtil.isCanWriteSD()) {
                mLocalPath = Environment.getExternalStorageDirectory()
                        .getAbsolutePath() + "/VideoCache/" + realname;
                mLocalDir = Environment.getExternalStorageDirectory()
                        .getAbsolutePath() + "/VideoCache/";
            } else {
                String path = getFilesDir().getAbsolutePath();
                mLocalPath = path + "/VideoCache/" + realname;
                mLocalDir = path + "/VideoCache";
            }

            playvideo();
            //  videoView.setVideoPath(mRemoteUrl);
            videoView.start();
            videoView.setKeepScreenOn(true);

            videoView
                    .setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mp.start();
                            // mp.setLooping(true);
                        }
                    });
            videoView
                    .setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                        @Override
                        public void onCompletion(MediaPlayer mp) {
                             videoView.setVideoPath(mLocalPath);
                            // videoView.start();
                        }
                    });
            videoView.setOnErrorListener(new OnErrorListener() {

                public boolean onError(MediaPlayer mediaplayer, int i, int j) {
                    iserror = true;
                    errorCnt++;
                    videoView.pause();
                    return true;
                }
            });
        } catch (Exception e) {

        }

    }

    FileOutputStream out = null;
    InputStream is = null;

    private void playvideo() {
        if (!URLUtil.isNetworkUrl(this.mRemoteUrl)) {
            videoView.setVideoPath(this.mRemoteUrl);
            videoView.start();
            return;
        }
        new Thread(new Runnable() {

            @Override
            public void run() {

                try {
                    File cacheFile = new File(mLocalPath);
                    if (!cacheFile.exists()) {
                        cacheFile.getParentFile().mkdirs();
//                        String mLocalDir = Environment
//                                .getExternalStorageDirectory()
//                                .getAbsolutePath()
//                                + "/VideoCache";
                        File Dir = new File(mLocalDir);
                        for (String s : Dir.list()) {
                            File f = new File(mLocalDir, s);
                            if (f.exists()) {
                                f.delete();
                            }
                        }
                        cacheFile.createNewFile();
                    } else {
                        long TotalFile = DaisyUtils
                                .getVodApplication(LauncherActivity.this)
                                .getPreferences().getLong("TotalSize", 0);
                        long filesize = cacheFile.length();
                        if (filesize >= TotalFile && filesize > 0) {
                            mHandler.sendEmptyMessage(CACHE_VIDEO_END);
                            return;
                        }
                    }
                    readSize = cacheFile.length();
                    URL url = new URL(mRemoteUrl + "&device_token=" + SimpleRestClient.device_token);
                    HttpURLConnection httpConnection = (HttpURLConnection) url
                            .openConnection();
                    System.out.println("localUrl: " + mLocalPath);
                    out = new FileOutputStream(cacheFile, true);

                    httpConnection.setRequestProperty("User-Agent", "NetFox");
                    httpConnection.setRequestProperty("RANGE", "bytes="
                            + readSize + "-");
                    is = httpConnection.getInputStream();
                    String contentrange = httpConnection
                            .getHeaderField("Content-Range");
                    long Totalsize = Long.parseLong(contentrange.substring(
                            contentrange.lastIndexOf("/") + 1,
                            contentrange.length()));
                    DaisyUtils.getVodApplication(LauncherActivity.this)
                            .getEditor().putLong("TotalSize", Totalsize);
                    DaisyUtils.getVodApplication(LauncherActivity.this).save();
                    mediaLength = httpConnection.getContentLength();
                    if (mediaLength == -1) {
                        return;
                    }

                    mediaLength += readSize;

                    byte buf[] = new byte[4 * 1024];
                    int size = 0;
                    long lastReadSize = 0;
                    while ((size = is.read(buf)) != -1 && !isfinished) {
                        try {
                            out.write(buf, 0, size);
                            Log.i("zjq", "write byte==" + size);
                            readSize += size;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (!isready) {
                            if ((readSize - lastReadSize) > READY_BUFF) {
                                lastReadSize = readSize;
                                mHandler.sendEmptyMessage(CACHE_VIDEO_READY);
                            }
                        } else {
                            if ((readSize - lastReadSize) > CACHE_BUFF
                                    * (errorCnt + 1)) {
                                lastReadSize = readSize;
                                mHandler.sendEmptyMessage(CACHE_VIDEO_UPDATE);
                            }
                        }
                    }
                    Log.i("zjq", "download finish");
                    if (!isfinished)
                        mHandler.sendEmptyMessage(CACHE_VIDEO_END);
                } catch (Exception e) {
                    if (!isfinished) {
                        iserror = true;
                        mHandler.sendEmptyMessage(CACHE_VIDEO_END);
                    }
                    e.printStackTrace();
                } finally {
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e) {
                            //
                        }
                    }

                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                            //
                        }
                    }
                }

            }
        }).start();
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case VIDEO_STATE_UPDATE:

                    break;

                case CACHE_VIDEO_READY:
                    isready = true;
                    videoView.setVideoPath(mLocalPath);
                    videoView.start();
                    break;

                case CACHE_VIDEO_UPDATE:
                    if (iserror && !videoView.isPlaying()) {
                        videoView.setVideoPath(mLocalPath);
                        videoView.start();
                        iserror = false;
                    }
                    break;

                case CACHE_VIDEO_END:
                    videoCacheIsComplete = true;
                    if (iserror || !videoView.isPlaying()) {
                        videoView.setVideoPath(mLocalPath);
                        videoView.start();
                        iserror = false;

                    }
                    break;
            }

        }
    };

    private Handler mainHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int type = msg.what;
            Bundle dataBundle = msg.getData();
            switch (type) {
                case FETCHCHANNEL:
                    break;
                case FETCHFRONTPAGE:
                    setFrontPage(dataBundle.getString("content"));
                    break;
                case FETCHWEATHER:
//                    setWeather(dataBundle.getString("content"));
                    break;
                case FETCHLATEST:
                    break;
                case FETCHTVHOME:
//                    setTvHome(dataBundle.getString("content"));
                    break;
                case GETDOMAIN:
                    DaisyUtils.getVodApplication(LauncherActivity.this).getNewContentModel();
                    updatePoster();
                    getFrontPage();
                    fetchHorizontalGuide();
                    fetchChannels();
//                    fetchWeather();
                    break;
            }
        }
    };


    /**
     * fetch channel
     */
    private void fetchChannels() {
        String deviceToken = SimpleRestClient.device_token;
        String host = SimpleRestClient.root_url;
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(AppConstant.LOG_LEVEL)
                .setEndpoint(host)
                .build();
        ClientApi.Channels client = restAdapter.create(ClientApi.Channels.class);
        client.excute(deviceToken, new Callback<ChannelEntity[]>() {
            @Override
            public void success(ChannelEntity[] channelEntities, Response response) {
                channelGrid.setAdapter(channelEntities);
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.e(TAG, retrofitError.getMessage());
            }
        });
    }


    private void getFrontPage() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                StringBuffer content = new StringBuffer();
                try {
                    URL getUrl = new URL(SimpleRestClient.root_url
                            + "/api/tv/frontpage/" + "?device_token=" + SimpleRestClient.device_token);
                    HttpURLConnection connection = (HttpURLConnection) getUrl
                            .openConnection();
                    connection.setUseCaches(false);
                    connection.setReadTimeout(4000);
                    connection.connect();
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream(), "UTF-8"));
                    String lines;
                    while ((lines = reader.readLine()) != null) {
                        content.append(lines);
                    }
                    Message message = new Message();
                    Bundle data = new Bundle();
                    data.putString("content", content.toString());
                    message.setData(data);
                    message.what = FETCHFRONTPAGE;
                    mainHandler.sendMessage(message);
                } catch (MalformedURLException e) {
                    if (e != null)
                        System.err.println(e.getMessage());
                } catch (IOException e) {
                    if (e != null)
                        System.err.println(e.getMessage());
                }
            }

        }.start();
    }


    private void fetchHorizontalGuide() {
        String deviceToken = SimpleRestClient.device_token;
        String host = SimpleRestClient.root_url;
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(AppConstant.LOG_LEVEL)
                .setEndpoint(host)
                .build();
        ClientApi.HorizontalGuide client = restAdapter.create(ClientApi.HorizontalGuide.class);
        client.excute(deviceToken, new Callback<VideoEntity>() {
            @Override
            public void success(VideoEntity videoEntity, Response response) {
                horizontalGuideListView.setAdapter(videoEntity);
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.e(TAG, retrofitError.getMessage());
            }
        });

    }


    private void fetchLinkedvideo(long videoId) {
        String deviceToken = SimpleRestClient.device_token;
        String host = SimpleRestClient.root_url;
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(AppConstant.LOG_LEVEL)
                .setEndpoint(host)
                .build();
        ClientApi.Linkedvideo client = restAdapter.create(ClientApi.Linkedvideo.class);
        client.excute(videoId, deviceToken, new Callback<ArrayList<AttributeEntity>>() {
            @Override
            public void success(ArrayList<AttributeEntity> attributeEntities, Response response) {
                if (AppConstant.DEBUG)
                    Log.d(TAG, "fetchLinkedvideo attributeEntities size ---> " + attributeEntities.size());
                attributeDatas = attributeEntities;
                linkedvideoGrid.setAdapter(attributeEntities);
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.e(TAG, retrofitError.getMessage());
            }
        });

    }


    @Override
    public void onSuccess(Result result) {
        Log.d(TAG, result.toString());
        try {
            SimpleRestClient.root_url = "http://" + result.getDomain();
            SimpleRestClient.sRoot_url = "http://" + result.getDomain();
            SimpleRestClient.ad_domain = "http://" + result.getAd_domain();
            SimpleRestClient.log_domain = "http://" + result.getLog_Domain();
            SimpleRestClient.device_token = result.getDevice_token();
            SimpleRestClient.sn_token = result.getSn_Token();
            DaisyUtils.getVodApplication(LauncherActivity.this).getEditor().putString(VodApplication.ad_domain, SimpleRestClient.ad_domain);
//			DaisyUtils.getVodApplication(LauncherActivity.this).getEditor().putString("domain", SimpleRestClient.root_url);
//			DaisyUtils.getVodApplication(LauncherActivity.this).getEditor().putString("ad_domain", SimpleRestClient.ad_domain);
            DaisyUtils.getVodApplication(LauncherActivity.this).save();
            SimpleRestClient.mobile_number = DaisyUtils.getVodApplication(this).getPreferences().getString(VodApplication.MOBILE_NUMBER, "");
            SimpleRestClient.access_token = DaisyUtils.getVodApplication(this).getPreferences().getString(VodApplication.AUTH_TOKEN, "");
            mainHandler.sendEmptyMessage(GETDOMAIN);
            Log.i("zjqactivator", "device_token=" + SimpleRestClient.device_token + "///"
                    + "access_token=" + SimpleRestClient.access_token + "ad_domain==" + SimpleRestClient.ad_domain + "domain==" + SimpleRestClient.root_url);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void onFailed(String erro) {
//        Log.d(TAG, erro);
       // showDialog(erro);
          checkNetWork(erro);
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG, "onBackPressed");
        showExitPopup(mView);
    }

    public void superOnbackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        DaisyUtils.getVodApplication(this).removeActivtyFromPool(this.toString());
        unregisterReceiver(appUpdateReceiver);


        if (!(updatePopupWindow == null)) {
            updatePopupWindow.dismiss();
        }
        if (exitPopupWindow != null) {
            exitPopupWindow.dismiss();
        }

        super.onDestroy();
    }

    /**
     * receive app update broadcast, and show update popup window
     */
    class AppUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getBundleExtra("data");
            showUpdatePopup(mView, bundle);
        }
    }

    private void registerUpdateReceiver() {
        appUpdateReceiver = new AppUpdateReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AppConstant.APP_UPDATE_ACTION);
        registerReceiver(appUpdateReceiver, intentFilter);
    }


    /**
     * show update popup, now update app or next time update
     *
     * @param view   popup window location
     * @param bundle update data
     */
    private void showUpdatePopup(View view, Bundle bundle) {
        final Context context = this;
        View contentView = LayoutInflater.from(context)
                .inflate(R.layout.popup_update, null);
        contentView.setBackgroundResource(R.drawable.popup_bg_yellow);
        updatePopupWindow = new PopupWindow(null, 1400, 500);
        updatePopupWindow.setContentView(contentView);
        updatePopupWindow.setFocusable(true);
        updatePopupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        DaisyButton updateNow = (DaisyButton) contentView.findViewById(R.id.update_now_bt);

        TextView updateTitle = (TextView) contentView.findViewById(R.id.update_title);
        LinearLayout updateMsgLayout = (LinearLayout) contentView.findViewById(R.id.update_msg_layout);

        final String path = bundle.getString("path");
        String title = bundle.getString("title");
        updateTitle.setText(title);

        ArrayList<String> msgs = bundle.getStringArrayList("msgs");

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = 200;
        layoutParams.topMargin = 20;

        for (String msg : msgs) {
            TextView textView = new TextView(this);
            textView.setTextSize(getResources().getDimensionPixelSize(R.dimen.update_msg_textsize));
            textView.setLayoutParams(layoutParams);
            textView.setText(msg);
            updateMsgLayout.addView(textView);
        }

        updateNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePopupWindow.dismiss();
                installApk(context, path);
            }
        });
    }

    public void installApk(Context mContext, String path) {
        Uri uri = Uri.parse("file://" + path);
        if (AppConstant.DEBUG)
            Log.d(TAG, uri.toString());
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    private void updatePoster() {
        Intent intent = new Intent();
        intent.setClass(this, PosterUpdateService.class);
        startService(intent);
    }

    /**
     * showExitPopup
     *
     * @param view
     */
    private void showExitPopup(View view) {
        final Context context = this;
        View contentView = LayoutInflater.from(context)
                .inflate(R.layout.popup_exit, null);
        exitPopupWindow = new PopupWindow(null, 740, 341);
        exitPopupWindow.setContentView(contentView);
        exitPopupWindow.setFocusable(true);
        exitPopupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        DaisyButton confirmExit = (DaisyButton) contentView.findViewById(R.id.confirm_exit);
        DaisyButton cancelExit = (DaisyButton) contentView.findViewById(R.id.cancel_exit);

        confirmExit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                isfinished = true;
                videoView.stopPlayback();
                mHandler.removeCallbacksAndMessages(null);
                exitPopupWindow.dismiss();
                superOnbackPressed();
            }
        });

        cancelExit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                exitPopupWindow.dismiss();
            }
        });
    }


    private void playVideoByTime(int startTime, int endTime) {
        int currnet = videoView.getCurrentPosition();
        Log.d(TAG, "current ---> " + currnet);
        if (currnet > endTime || currnet < startTime) {
            Log.d(TAG, "start time ---> " + startTime + "  end time ---> " + endTime);
            isfinished = true;
            videoView.seekTo(startTime);
        }
    }


    public void fetchIpLookup() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(AppConstant.LOG_LEVEL)
                .setEndpoint(ClientApi.IpLookup.HOST)
                .build();
        ClientApi.IpLookup client = restAdapter.create(ClientApi.IpLookup.class);
        client.excute(new Callback<IpLookupEntity>() {
            @Override
            public void success(IpLookupEntity ipLookupEntity, Response response) {
                String city = ipLookupEntity.getCity();
                fetchGeoId(city);
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.e(TAG, retrofitError.getMessage());
            }
        });

    }

    public void fetchGeoId(final String city) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(AppConstant.LOG_LEVEL)
                .setEndpoint(ClientApi.GeoId.HOST)
                .build();
        ClientApi.GeoId client = restAdapter.create(ClientApi.GeoId.class);
        client.excute(new Callback<ArrayList<GeoIdEntity>>() {
            @Override
            public void success(ArrayList<GeoIdEntity> geoIdEntities, Response response) {
                String geoId = null;
                for (GeoIdEntity geoIdEntity : geoIdEntities) {
                    if (geoIdEntity.getName().equals(city)) {
                        geoId = geoIdEntity.getId();
                    }
                }
                if (!TextUtils.isEmpty(geoId))
                    fetchWeather(geoId);
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.e(TAG, retrofitError.getMessage());
            }
        });

    }


    private void fetchWeather(final String geoId) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(AppConstant.LOG_LEVEL)
                .setEndpoint(ClientApi.Weather.HOST)
                .build();
        ClientApi.Weather client = restAdapter.create(ClientApi.Weather.class);
        client.excute(geoId, new Callback<WeatherEntity>() {
            @Override
            public void success(WeatherEntity weatherEntity, Response response) {
                setWeather(weatherEntity);
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.e(TAG, retrofitError.getMessage());
            }
        });
    }

    private void setWeather(WeatherEntity weatherEntity) {
        weatherTmp.setText(weatherEntity.getName() + "  "
                + weatherEntity.getToday().getTemperature() + " ℃");
        todayWeatherDetail.setText(weatherEntity.getToday().getPhenomenon()
                + "  " + weatherEntity.getToday().getWind_direction());
        tomorrowDetail.setText(weatherEntity.getTomorrow().getTemperature()
                + " ℃   " + weatherEntity.getTomorrow().getPhenomenon() + "   "
                + weatherEntity.getTomorrow().getWind_direction());
    }
    
    private void showNetErrorPopup() {
        //final Context context = this;
        View contentView = LayoutInflater.from(mView.getContext())
                .inflate(R.layout.popup_net_error, null);
        netErrorPopupWindow = new PopupWindow(null, 1400, 500);
        netErrorPopupWindow.setContentView(contentView);
        netErrorPopupWindow.setFocusable(true);
        netErrorPopupWindow.showAtLocation(mView, Gravity.CENTER, 0, 0);
        DaisyButton confirmExit = (DaisyButton) contentView.findViewById(R.id.confirm_exit);

        confirmExit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
            	netErrorPopupWindow.dismiss();
                superOnbackPressed();
            }
        });
    }



    private void checkNetWork(String error) {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo.State wifiState = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
        if(cm.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET)==null){
        	if(wifiState != NetworkInfo.State.CONNECTED){
        		showNetErrorPopup();
        	}
        	else{
        		showDialog(error);
        	}
        }
        else{
            NetworkInfo.State ethernetState = cm.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET).getState();
            if (wifiState != NetworkInfo.State.CONNECTED && ethernetState != NetworkInfo.State.CONNECTED) {
                showNetErrorPopup();
            }
            else{
            	showDialog(error);
            }
        }
    }
}
