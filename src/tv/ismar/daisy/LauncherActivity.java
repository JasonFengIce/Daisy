package tv.ismar.daisy;

import android.app.Activity;
import android.app.Dialog;
import android.content.*;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.*;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.URLUtil;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import cn.ismartv.activator.Activator;
import cn.ismartv.activator.data.Result;
import com.google.gson.Gson;
import com.ismartv.launcher.data.ChannelEntity;
import com.ismartv.launcher.data.FrontPageEntity;
import com.ismartv.launcher.data.VideoEntity;
import com.ismartv.launcher.data.WeatherEntity;
import com.ismartv.launcher.ui.widget.IsmartvVideoView;
import com.squareup.picasso.Picasso;
import org.json.JSONException;
import org.json.JSONObject;
import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.SystemFileUtil;
import tv.ismar.daisy.core.service.PosterUpdateService;
import tv.ismar.daisy.core.update.AppUpdateUtils;
import tv.ismar.daisy.player.InitPlayerTool;
import tv.ismar.daisy.ui.adapter.ChannelAdapter;
import tv.ismar.daisy.ui.widget.DaisyButton;
import tv.ismar.daisy.ui.widget.DaisyImageView;
import tv.ismar.daisy.views.CustomDialog;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class LauncherActivity extends Activity implements View.OnClickListener, Activator.OnComplete, OnItemClickListener {
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
    private IsmartvVideoView videoView;

    private ImageView[] homeImages;
    private TextView[] homeTitles;


    private RelativeLayout[] channelImtes;

    private ImageView[] recomends;

    private ImageView weatherIcon;
    private TextView weatherTmp;
    private TextView todayWeatherDetail;

    private TextView tomorrowDetail;

    private RelativeLayout[] homeItems;
    private String mLocalPath;
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
    PopupWindow popupWindow;

    private GridView channelGrid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerUpdateReceiver();
        AppUpdateUtils.getInstance().checkUpdate(this);
        String path = getFilesDir().getAbsolutePath();
        SystemFileUtil.appPath = path;
        DaisyUtils.getVodApplication(this).addActivityToPool(this.toString(), this);
        mView = LayoutInflater.from(this).inflate(R.layout.activity_launcher, null);
        setContentView(mView);
        setContentView(R.layout.activity_launcher);
        initViews();
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        int height = metric.heightPixels; // 屏幕高度（像素）
        int densityDpi = metric.densityDpi; // 屏幕密度DPI（120 / 160 / 240）
        float rate = (float) densityDpi / (float) 160;
        VodApplication.rate = rate;
        String domain = DaisyUtils.getVodApplication(this).getPreferences().getString("domain", "");
//		if("".equals(domain))
//		    register();
//		else{
//			SimpleRestClient.root_url = domain;
//			SimpleRestClient.sRoot_url = domain;
//			mainHandler.sendEmptyMessage(GETDOMAIN);
//		}
        activator = Activator.getInstance(this);
        activator.setOnCompleteListener(this);
        activator.active(MANUFACTURE, KIND, VERSION);
    }

    Dialog dialog = null;
    private DialogInterface.OnClickListener mPositiveListener;
    private DialogInterface.OnClickListener mNegativeListener;
    public static final int NETWORK_ACTIVE_FAILED = 1;

    private void showDialog() {
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
                    .setMessage(R.string.active_fail)
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
            showDialog();
        }
    }

    private void initViews() {
        weatherIcon = (ImageView) findViewById(R.id.weather_icon);
        weatherTmp = (TextView) findViewById(R.id.weather_tmp);
        todayWeatherDetail = (TextView) findViewById(R.id.today_weather_detail);
        tomorrowDetail = (TextView) findViewById(R.id.tomorrow_detail);
        videoView = (IsmartvVideoView) findViewById(R.id.video_view);
        btn_search = (DaisyImageView)findViewById(R.id.search);
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
        homeItems = new RelativeLayout[]{
                (RelativeLayout) findViewById(R.id.home_item_1),
                (RelativeLayout) findViewById(R.id.home_item_2),
                (RelativeLayout) findViewById(R.id.home_item_3),
                (RelativeLayout) findViewById(R.id.home_item_4),
                (RelativeLayout) findViewById(R.id.home_item_5),
                (RelativeLayout) findViewById(R.id.home_item_6)};

        homeImages = new ImageView[]{(ImageView) findViewById(R.id.home_1),
                (ImageView) findViewById(R.id.home_2),
                (ImageView) findViewById(R.id.home_3),
                (ImageView) findViewById(R.id.home_4),
                (ImageView) findViewById(R.id.home_5),
                (ImageView) findViewById(R.id.home_6)};

        homeTitles = new TextView[]{
                (TextView) findViewById(R.id.home_title_1),
                (TextView) findViewById(R.id.home_title_2),
                (TextView) findViewById(R.id.home_title_3),
                (TextView) findViewById(R.id.home_title_4),
                (TextView) findViewById(R.id.home_title_5),
                (TextView) findViewById(R.id.home_title_6)};

        channelGrid = (GridView) findViewById(R.id.channel_grid);
        channelGrid.setOnItemClickListener(this);


        recomends = new ImageView[]{(ImageView) findViewById(R.id.image_1),
                (ImageView) findViewById(R.id.image_2),
                (ImageView) findViewById(R.id.image_3)};
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

    public void pickHomeItem(RelativeLayout view) {
        intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        String values[] = view.getTag().toString().split(",");
        if (values[1].equals("item")) {
            intent.setClassName("tv.ismar.daisy",
                    "tv.ismar.daisy.ItemDetailActivity");
            intent.putExtra("url", values[0]);
            startActivity(intent);
        } else {
            InitPlayerTool tool = new InitPlayerTool(LauncherActivity.this);
            tool.initClipInfo(InitPlayerTool.FLAG_URL, values[0]);
        }
    }

    private Intent intent;

    private void setFrontPage(String content) {
        try {
            Gson gson = new Gson();
            FrontPageEntity frontBeans = gson.fromJson(content.toString(),
                    FrontPageEntity.class);
            final Uri uri = Uri.parse(frontBeans.getVideos().get(0)
                    .getVideo_url());
            mRemoteUrl = frontBeans.getVideos().get(0).getVideo_url();
            // mRemoteUrl = "http://192.168.1.185:8099/shipinkefu/22.mp4";
            int position = mRemoteUrl.lastIndexOf("/");
            String fileName = mRemoteUrl.substring(position + 1,
                    mRemoteUrl.length());
            String realname = fileName.substring(0, fileName.lastIndexOf("?"));
            mLocalPath = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + "/VideoCache/" + realname;
            // videoView.setVideoPath(mLocalPath);
            // videoView.start();
            //mRemoteUrl = mRemoteUrl.substring(0, mRemoteUrl.lastIndexOf("?"));
            //mRemoteUrl  = mRemoteUrl + "?" + "sn=" + Build.SERIAL;
            playvideo();
            videoView.setKeepScreenOn(true);

            videoView
                    .setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mp.start();
                            mp.setLooping(true);
                        }
                    });
            videoView
                    .setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            // videoView.setVideoURI(uri);
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
                        String mLocalDir = Environment
                                .getExternalStorageDirectory()
                                .getAbsolutePath()
                                + "/VideoCache";
                        File Dir = new File(mLocalDir);
                        for (String s : Dir.list()) {
                            File f = new File(s);
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
                    if (iserror) {
                        videoView.setVideoPath(mLocalPath);
                        videoView.start();
                        iserror = false;
                    }
                    break;

                case CACHE_VIDEO_END:
                    if (iserror || !videoView.isPlaying()) {
                        videoView.setVideoPath(mLocalPath);
                        videoView.start();
                        iserror = false;
                    }
                    break;
            }

            // super.handleMessage(msg);
        }
    };

    private void startPlayVideo(String path) {
        try {
            videoView.setVideoPath(mLocalPath);
            videoView.start();
        } catch (Exception e) {

        }
    }

    private void setChannels(String content) {
        Gson gson = new Gson();
        ChannelEntity[] channelBeans = gson.fromJson(content.toString(),
                ChannelEntity[].class);
        ChannelAdapter channelAdapter = new ChannelAdapter(this, channelBeans);
        channelGrid.setAdapter(channelAdapter);

//        for (int i = 0; i < channelBeans.length; i++) {
//            channelTexts[i].setText(channelBeans[i].getName());
//            channelTexts[i].setTextColor(Color.WHITE);
//            TextPaint tp = channelTexts[i].getPaint();
//            tp.setFakeBoldText(true);
//            JSONObject jsonObject = new JSONObject();
//            try {
//                jsonObject.put("name", channelBeans[i].getName());
//                jsonObject.put("url", channelBeans[i].getUrl());
//                jsonObject.put("channel", channelBeans[i].getChannel());
//            } catch (JSONException e) {
//                if (e != null)
//                    e.printStackTrace();
//            }
//            channelImtes[i].setTag(jsonObject.toString());
//            channelImtes[i].setOnClickListener(LauncherActivity.this);
//        }
    }

	private void setTvHome(String content) {
		try{
			Gson gson = new Gson();
			VideoEntity tvHome = gson.fromJson(content.toString(),
					VideoEntity.class);
			for (int i = 0; i < 6; i++) {
				homeTitles[i].setText(tvHome.getObjects().get(i).getTitle());
				Picasso.with(LauncherActivity.this)
						.load(tvHome.getObjects().get(i).getImage())
						.placeholder(R.drawable.preview).error(R.drawable.preview)
						.into(homeImages[i]);
				boolean is_complex = tvHome.getObjects().get(i).isIs_complex();
				if (is_complex)
					homeItems[i].setTag(tvHome.getObjects().get(i).getItem_url()
							+ "," + "item");
				else
					homeItems[i].setTag(tvHome.getObjects().get(i).getItem_url()
							+ "," + "play");
				homeItems[i].setOnClickListener(viewItemClickListener);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

    private void setLatest(String content) {
        Gson gson = new Gson();
        VideoEntity videoEntity = gson.fromJson(content.toString(),
                VideoEntity.class);
        for (int i = 0; i < 3; i++) {

            Picasso.with(LauncherActivity.this)
                    .load(videoEntity.getObjects().get(i).getImage())
                    .placeholder(R.drawable.preview).error(R.drawable.preview)
                    .into(recomends[i]);
            boolean is_complex = videoEntity.getObjects().get(i).isIs_complex();
            if (is_complex)
                recomends[i].setTag(videoEntity.getObjects().get(i)
                        .getItem_url()
                        + ",item");
            else
                recomends[i].setTag(videoEntity.getObjects().get(i)
                        .getItem_url()
                        + ",play");

            recomends[i].setOnClickListener(viewItemClickListener);
        }
    }

    private void setWeather(String content) {
        Gson gson = new Gson();
        WeatherEntity weatherEntity = gson.fromJson(content.toString(),
                WeatherEntity.class);
        weatherTmp.setText(weatherEntity.getName() + "  "
                + weatherEntity.getToday().getTemperature() + " ℃");
        todayWeatherDetail.setText(weatherEntity.getToday().getPhenomenon()
                + "  " + weatherEntity.getToday().getWind_direction());
        tomorrowDetail.setText(weatherEntity.getTomorrow().getTemperature()
                + " ℃ " + weatherEntity.getTomorrow().getPhenomenon() + " "
                + weatherEntity.getTomorrow().getWind_direction());

    }

    View.OnClickListener viewItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            String[] values = view.getTag().toString().split(",");
            if (values[1].equals("item")) {
                intent.setClassName("tv.ismar.daisy",
                        "tv.ismar.daisy.ItemDetailActivity");
                intent.putExtra("url", values[0]);
                startActivity(intent);
            } else {
                InitPlayerTool tool = new InitPlayerTool(LauncherActivity.this);
                tool.initClipInfo(values[0], InitPlayerTool.FLAG_URL);
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
                    setChannels(dataBundle.getString("content"));
                    break;
                case FETCHFRONTPAGE:
                    setFrontPage(dataBundle.getString("content"));
                    break;
                case FETCHWEATHER:
                    setWeather(dataBundle.getString("content"));
                    break;
                case FETCHLATEST:
                    setLatest(dataBundle.getString("content"));
                    break;
                case FETCHTVHOME:
                    setTvHome(dataBundle.getString("content"));
                    break;
                case GETDOMAIN:
                    DaisyUtils.getVodApplication(LauncherActivity.this).getNewContentModel();
                    updatePoster();
                    getFrontPage();
                    getTvHome();
                    getChannels();
                    getLatest();
                    fetchWeather();
                    break;
            }
        }
    };

    private void getChannels() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                StringBuffer content = new StringBuffer();
                try {
                    URL getUrl = new URL(SimpleRestClient.root_url
                            + "/api/tv/channels/" + "?device_token=" + SimpleRestClient.device_token);
                    HttpURLConnection connection = (HttpURLConnection) getUrl
                            .openConnection();
                    connection.setReadTimeout(4000);
					connection.setUseCaches(false);
					connection.connect();
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(connection.getInputStream(),"UTF-8"));
					String lines;
					while ((lines = reader.readLine()) != null) {
						content.append(lines);
					}
					Message message = new Message();
					Bundle data = new Bundle();
					data.putString("content", content.toString());
					message.setData(data);
					message.what = FETCHCHANNEL;
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

    private void getTvHome() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                StringBuffer content = new StringBuffer();
                try {
                    URL getUrl = new URL(SimpleRestClient.root_url
                            + "/api/tv/section/tvhome/" + "?device_token=" + SimpleRestClient.device_token);
                    HttpURLConnection connection = (HttpURLConnection) getUrl
                            .openConnection();
                    connection.setReadTimeout(9000);
					connection.setUseCaches(false);
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
                    message.what = FETCHTVHOME;
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

    private void getLatest() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                StringBuffer content = new StringBuffer();
                try {
                    URL getUrl = new URL(SimpleRestClient.root_url
                            + "/api/tv/section/xinpianshangxian/" + "?device_token=" + SimpleRestClient.device_token);
                    HttpURLConnection connection = (HttpURLConnection) getUrl
                            .openConnection();
					connection.setUseCaches(false);
                    connection.setReadTimeout(19000);
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
                    message.what = FETCHLATEST;
                    mainHandler.sendMessage(message);
                } catch (MalformedURLException e) {
                    if (e != null)
                        System.err.println(e.getMessage());
                } catch (IOException e) {
                    // System.err.println(e.getMessage());
                }
            }

        }.start();
    }

    private void fetchWeather() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                StringBuffer content = new StringBuffer();
                try {
                    URL getUrl = new URL(
                            "http://media.lily.tvxio.com/101010100.json");
                    HttpURLConnection connection = (HttpURLConnection) getUrl
                            .openConnection();
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
                    message.what = FETCHWEATHER;
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            isfinished = true;
            videoView.stopPlayback();
            mHandler.removeCallbacksAndMessages(null);
            finish();
        }
        return super.onKeyDown(keyCode, event);
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
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void onFailed(String erro) {
        Log.d(TAG, erro);
        showDialog();
    }

    @Override
    protected void onDestroy() {
    	DaisyUtils.getVodApplication(this).removeActivtyFromPool(this.toString());
        unregisterReceiver(appUpdateReceiver);
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }

        super.onDestroy();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        String content = (String) view.findViewById(R.id.channel_img).getTag();
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
        contentView.setBackgroundResource(R.drawable.background);
        popupWindow = new PopupWindow(null, 1400, 500);
        popupWindow.setContentView(contentView);
        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        DaisyButton updateNow = (DaisyButton) contentView.findViewById(R.id.update_now_bt);
        DaisyButton updateNext = (DaisyButton) contentView.findViewById(R.id.update_next_bt);

        TextView updateTitle = (TextView) contentView.findViewById(R.id.update_title);
        LinearLayout updateMsgLayout = (LinearLayout) contentView.findViewById(R.id.update_msg_layout);

        final String path = bundle.getString("path");
        String title = bundle.getString("title");
        updateTitle.setText(title);

        ArrayList<String> msgs = bundle.getStringArrayList("msgs");

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
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
                popupWindow.dismiss();
                installApk(context, path);
            }
        });

        updateNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppUpdateUtils.getInstance().modifyUpdatePreferences(context, true);
                popupWindow.dismiss();
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

    private void updatePoster(){
        Intent intent = new Intent();
        intent.setClass(this, PosterUpdateService.class);
        startService(intent);
    }
}
