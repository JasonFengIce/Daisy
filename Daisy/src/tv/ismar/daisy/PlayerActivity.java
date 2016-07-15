package tv.ismar.daisy;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnHoverListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.gson.JsonSyntaxException;
import com.ismartv.api.AESDemo;
import com.ismartv.api.t.AccessProxy;
import com.ismartv.bean.ClipInfo;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import cn.ismartv.tvplayer.MainActivity;
import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;
import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.EventProperty;
import tv.ismar.daisy.core.ImageUtils;
import tv.ismar.daisy.core.NetworkUtils;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.SimpleRestClient.HttpPostRequestInterface;
import tv.ismar.daisy.core.VodUserAgent;
import tv.ismar.daisy.core.client.HttpAPI;
import tv.ismar.daisy.core.client.HttpManager;
import tv.ismar.daisy.core.client.PlayCheckManager;
import tv.ismar.daisy.core.preferences.AccountSharedPrefs;
import tv.ismar.daisy.data.http.ItemEntity;
import tv.ismar.daisy.exception.ItemOfflineException;
import tv.ismar.daisy.exception.NetworkException;
import tv.ismar.daisy.models.AdElement;
import tv.ismar.daisy.models.Attribute;
import tv.ismar.daisy.models.Clip;
import tv.ismar.daisy.models.History;
import tv.ismar.daisy.models.Item;
import tv.ismar.daisy.models.Quality;
import tv.ismar.daisy.persistence.HistoryManager;
import tv.ismar.daisy.player.CallaPlay;
import tv.ismar.daisy.player.ISTVVodMenu;
import tv.ismar.daisy.player.ISTVVodMenuItem;
import tv.ismar.daisy.ui.fragment.EpisodeFragment;
import tv.ismar.daisy.ui.fragment.EpisodeFragment.OnItemSelectedListener;
import tv.ismar.daisy.utils.HardwareUtils;
import tv.ismar.daisy.views.IsmatvVideoView;
import tv.ismar.daisy.views.MarqueeView;
import tv.ismar.daisy.views.PaymentDialog;
import tv.ismar.player.SmartPlayer;

import static tv.ismar.daisy.DramaListActivity.ORDER_CHECK_BASE_URL;

public class PlayerActivity extends VodMenuAction implements OnItemSelectedListener {

    @SuppressWarnings("unused")
    private static final String SAMPLE = "http://114.80.0.33/qyrrs?url=http%3A%2F%2Fjq.v.tvxio.com%2Fcdn%2F0%2F7b%2F78fadc2ffa42309bda633346871f26%2Fhigh%2Fslice%2Findex.m3u8&quality=high&sn=weihongchang_s52&clipid=779521&sid=85d3f919a918460d9431136d75db17f03&sign=08a868ad3c4e3b37537a13321a6f9d4b";
    private static final String PREFS_NAME = "tv.ismar.daisy";
    private static final String TAG = "PLAYER";
    private static final String BUFFERCONTINUE = " 上次放映：";
    private static final String PlAYSTART = " 即将放映：";
    private static final String EXTOCLOSE = " 网络数据异常，即将退出播放器";
    @SuppressWarnings("unused")
    private static final String HOST = "cord.tvxio.com";

    private static final int SHORT_STEP = 1;
    private static final int DIALOG_OK_CANCEL = 0;
    private final int MOVE_LEFT = 1011;
    private final int MOVE_RIGHT = 1012;
    private final int MOVE_TOP = 1013;
    private final int MOVE_BOTTOM = 1014;
    private boolean paused = false;
    private boolean isSeekBuffer = false;
    private boolean panelShow = false;
    private int currQuality = 0;
    private String urls[] = new String[6];
    private Animation panelShowAnimation;
    private Animation panelHideAnimation;
    private Animation top_to_down, top_to_up;
    // private Animation bufferHideAnimation;
    private ImageView logoImage;
    private LinearLayout panelLayout;
    private RelativeLayout top_panel;
    private MarqueeView titleText;
    private TextView qualityText;
    private TextView timeText, endTimetext;
    private ImageView playPauseImage;
    private ImageView ffImage;
    private ImageView fbImage;
    private TextView ad_count_view;
    private int clipLength = 0;
    private int currPosition = 0;
    private boolean isContinue = true;
    private ISTVVodMenu menu = null;
    private ClipInfo urlInfo = new ClipInfo();
    private Handler mCheckHandler = new Handler();
    private int tempOffset = 0;
    private Item serialItem; // 电视剧剧集父类
    private Item item;
    private Clip clip;
    private Bundle bundle;
    private SeekBar timeBar;
    private IsmatvVideoView mVideoView;
    private Dialog popupDlg = null;
    private InputStream logoInputStream;
    private HistoryManager historyManager;
    private History mHistory;
    private SimpleRestClient simpleRestClient;
    private String itemUrl;
    private String subItemUrl;
    private int seekPostion = 0;
    private boolean isSeek = false;
    private List<Item> listItems = new ArrayList<Item>();
    private List<Integer> payedItemspk = new ArrayList<Integer>();
    private int currNum = 0;
    private int offsets = 0;
    private int offn = 1;
    private long bufferDuration = 0;
    private long startDuration = 0;
    private CallaPlay callaPlay = new CallaPlay();
    AudioManager am;
    private String mSection;
    private String sid = "";
    private String mediaip;
    private boolean live_video = false;
    private boolean isPreview = false;
    private boolean paystatus = false;
    private boolean ismedialplayerinit = false;
    private Stack<AdElement> adElement;
    private AdImageDialog adimageDialog;
    private int adsumtime;
    private boolean isadvideoplaying = false;
    private int speed;
    private AccountSharedPrefs shardpref;
    private ImageView gesture_tipview;
    private boolean isPaymentdialogShow = false;
    private static final String ACTION = "com.android.hoperun.screensave";
    private ScreenSaveBrocast saveScreenbroad;
    private boolean isneedpause = true;
    private HashMap<String, Integer> adlog = new HashMap<String, Integer>();
    private String adurl;
    private TextView anthology;
    /**
     * 左右滑动的最短距离
     */
    private int distance = 100;
    /**
     * 左右滑动的最大速度
     */
    private int velocity = 200;
    private PopupWindow popupWindow, itemPopWindow, EntertainmentPop;
    private View bright;
    private ProgressBar progress_bright;
    private View volumn;
    private ProgressBar progress_volumn;
    private int currentVolumn;
    private int currentBright;
    private int maxVolumn;
    private AudioManager mAudioManager;
    private TextView progress_time;
    private ImageView player_back;
    private View play_progress;
    private TextView current_play_progress;
    private ProgressBar progress_play;
    private PowerManager.WakeLock mWakelock;
    private boolean isAnthology = false;

    public float getScreenWidth() {
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        int width = metric.widthPixels;     // 屏幕宽度（像素）
        int height = metric.heightPixels;   // 屏幕高度（像素）
        float density = metric.density;      // 屏幕密度（0.75 / 1.0 / 1.5）
        int densityDpi = metric.densityDpi;  // 屏幕密度DPI（120 / 160 / 240）
        return width / density;
    }

    private class ScreenSaveBrocast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION)) {
                System.out.println("receiver message --->>>>");
                abortBroadcast();
            }
        }

    }

    private EpisodeFragment mEpisodeFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        bundle = intent.getExtras();
        item = (Item) bundle.get("item");


        Intent newIntent = new Intent();
        newIntent.setClass(this, MainActivity.class);
        newIntent.putExtra("base_url", SimpleRestClient.root_url);
        newIntent.putExtra("pk", String.valueOf(item.pk));
        newIntent.putExtra("device_token", SimpleRestClient.device_token);
        newIntent.putExtra("sign", getAES(SimpleRestClient.access_token, ""));
        startActivity(newIntent);


//        Window win = getWindow();
//        WindowManager.LayoutParams winParams = win.getAttributes();
//        winParams.flags |= (WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
//                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
//                | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
//                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
////		WindowManager.LayoutParams lp = getWindow().getAttributes();
////		lp.dimAmount = 0.75f;
////		win.setAttributes(lp);
////		win.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
//        activityTag = "BaseActivity";
//        IntentFilter intentFilter = new IntentFilter(ACTION);
//        saveScreenbroad = new ScreenSaveBrocast();
//        intentFilter.setPriority(119110);
//        registerReceiver(saveScreenbroad, intentFilter);
//        shardpref = AccountSharedPrefs.getInstance();
//        setView();
//        initData();
//        DisplayMetrics metric = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(metric);
//
//        mEpisodeFragment = new EpisodeFragment();
//        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
//        fragmentTransaction.add(R.id.episode_layout, mEpisodeFragment);
//        fragmentTransaction.hide(mEpisodeFragment);
//        fragmentTransaction.commit();
//        mEpisodeFragment.setOnItemSelectedListener(this);
    }

    private static String getAES(String sn, String access_token) {
        String keyCrypt = "smartvdefaultkey";
        String result = null;
        String contents = (new StringBuilder(String.valueOf((new Date())
                .getTime()))).append(sn).toString();
        if (access_token != null && access_token.length() > 0) {
            if (access_token.length() > 15) {
                result = AESDemo.encrypttoStr(contents,
                        access_token.substring(0, 16));
            } else {
                int leng = 16 - access_token.length();
                for (int i = 0; i < leng; i++)
                    access_token = (new StringBuilder(
                            String.valueOf(access_token))).append("0")
                            .toString();

                result = AESDemo.encrypttoStr(contents,
                        access_token.substring(0, 16));
            }
        } else {
            result = AESDemo.encrypttoStr(contents, keyCrypt);// 1422928853725001122334455
        }
        return result;
    }

    @Override
    public void onResume() {
        super.onResume();
//		if(item == null){
//			String testitemurl = " http://sky.tvxio.bestv.com.cn/v3_0/UF30/tou/api/item/701893/";
//				GetItemTask mGetItemTask = new GetItemTask();
//				mGetItemTask.execute(testitemurl);
//		}else {
        if (needOnresume && isneedpause) {
            isBuffer = true;
            showBuffer();
            initPlayer();
        }
//		}
        isneedpause = true;
        needOnresume = true;
    }

    private void setView() {
        panelShowAnimation = AnimationUtils.loadAnimation(this,
                R.anim.fly_up);
        panelHideAnimation = AnimationUtils.loadAnimation(this,
                R.anim.fly_down);
        top_to_down = AnimationUtils.loadAnimation(this, R.anim.top_fly_down);
        top_to_up = AnimationUtils.loadAnimation(this, R.anim.top_fly_up);
        // bufferHideAnimation =
        // AnimationUtils.loadAnimation(this,R.drawable.fade_out);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.vod_player);

        mVideoView = (IsmatvVideoView) findViewById(R.id.video_view);
        panelLayout = (LinearLayout) findViewById(R.id.PanelLayout);
        top_panel = (RelativeLayout) findViewById(R.id.top_panelayout);
        player_back = (ImageView) findViewById(R.id.player_back);
        player_back.setOnClickListener(listener);
        titleText = (MarqueeView) findViewById(R.id.TitleText);
        anthology = (TextView) findViewById(R.id.anthology);
        anthology.setOnClickListener(listener);
        qualityText = (TextView) findViewById(R.id.QualityText);
        qualityText.setOnClickListener(listener);
        timeText = (TextView) findViewById(R.id.TimeText);
        endTimetext = (TextView) findViewById(R.id.endTimeText);
        progress_time = (TextView) findViewById(R.id.progress_time);
        timeBar = (SeekBar) findViewById(R.id.TimeSeekBar);
        timeBar.setOnSeekBarChangeListener(new SeekBarChangeEvent());
        playPauseImage = (ImageView) findViewById(R.id.PlayPauseImage);
        ffImage = (ImageView) findViewById(R.id.FFImage);
        //fbImage = (ImageView) findViewById(R.id.FBImage);
        bufferLayout = (LinearLayout) findViewById(R.id.BufferLayout);
        bufferText = (TextView) findViewById(R.id.BufferText);
        logoImage = (ImageView) findViewById(R.id.logo_image);
        ad_count_view = (TextView) findViewById(R.id.ad_count_view);
        panelLayout.setVisibility(View.GONE);
        top_panel.setVisibility(View.GONE);
        bufferLayout.setVisibility(View.GONE);
        qualityText.setVisibility(View.GONE);
        gesture_tipview = (ImageView) findViewById(R.id.gesture_tipview);
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        bright = findViewById(R.id.bright);
        progress_bright = (ProgressBar) findViewById(R.id.progress_bright);
        volumn = findViewById(R.id.volumn);
        progress_volumn = (ProgressBar) findViewById(R.id.progress_volumn);
        play_progress = findViewById(R.id.play_progress);
        current_play_progress = (TextView) findViewById(R.id.current_play_progress);
        progress_play = (ProgressBar) findViewById(R.id.progress_play);
        playPauseImage.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent keycode) {
                switch (keycode.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // if (mVideoView.getDuration() > 0) {
                        if (!paused) {
                            pauseItem();
                            playPauseImage
                                    .setImageResource(R.drawable.paus);
                        } else {
                            resumeItem();
                            playPauseImage
                                    .setImageResource(R.drawable.play);
                        }

                        // }
                        break;

                    default:
                        break;
                }
                return false;
            }
        });
        gesture_tipview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gesture_tipview.setVisibility(View.GONE);
                shardpref.setSharedPrefs(AccountSharedPrefs.FIRST_USE, "false");
                initClipInfo();
            }
        });
//	//	fbImage.setOnTouchListener(new OnTouchListener() {
//
//			@Override
//			public boolean onTouch(View v, MotionEvent keycode) {
//				// TODO Auto-generated method stub
//				switch (keycode.getAction()) {
//					case MotionEvent.ACTION_DOWN:
//						if (clipLength > 0 && !live_video) {
//							isSeek = true;
//							showPanel();
//							isBuffer = true;
//							showBuffer();
//							fastBackward(SHORT_STEP);
//							mVideoView.seekTo(currPosition);
//							isSeekBuffer = true;
//							Log.d(TAG, "LEFT seek to "
//									+ getTimeString(currPosition));
//							isSeek = false;
//							offsets = 0;
//							offn = 1;
//						}
//						break;
//
//					default:
//						break;
//				}
//				return false;
//			}
//		});
        ffImage.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent keycode) {
                // TODO Auto-generated method stub
//				switch (keycode.getAction()) {
//					case MotionEvent.ACTION_DOWN:
//						if (clipLength > 0 && !live_video) {
//							isSeek = true;
//							showPanel();
//							isBuffer = true;
//							showBuffer();
//							fastForward(SHORT_STEP);
//							mVideoView.seekTo(currPosition);
//							isSeekBuffer = true;
//							Log.d(TAG, "RIGHT seek to"
//									+ getTimeString(currPosition));
//							isSeek = false;
//							offsets = 0;
//							offn = 1;
//						}
//						break;
//
//					default:
//						break;

                //}
                if (listItems.size() != 0) {
                    gotoFinishPage();
                }
                return false;
            }
        });

        mVideoView.setOnHoverListener(onhoverlistener);
        setVideoActionListener();
        if ("false".equals(shardpref.getSharedPrefs(AccountSharedPrefs.FIRST_USE))) {
            Intent intent = getIntent();
            String testitemurl = intent.getStringExtra("testitemurl");
//			String testitemurl = " http://sky.tvxio.bestv.com.cn/v3_0/UF30/tou/api/item/701893/";
//			if(testitemurl != null && !"".equals(testitemurl)){
//				GetItemTask mGetItemTask = new GetItemTask();
//				mGetItemTask.execute(testitemurl);
//			}else{
            initClipInfo();
//			}
        } else {
            gesture_tipview.setVisibility(View.VISIBLE);
            gesture_tipview.setAlpha(81);
            //setGesturebackground(gesture_tipview, R.drawable.frist_play);
            gesture_tipview.setBackgroundResource(R.drawable.frist_play);
        }
    }

    private void initData() {
        progress_bright.setMax(100);
        progress_volumn.setMax(100);
        // 获取系统音量
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        maxVolumn = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        currentVolumn = getVolumn();
        progress_volumn.setProgress(currentVolumn);
        //获取系统亮度
        currentBright = getsystemBright();
        progress_bright.setProgress(currentBright);
    }

    protected void initClipInfo() {
        simpleRestClient = new SimpleRestClient();
        bufferText.setText(BUFFERING);
        if (mVideoView != null) {
            mVideoView.setAlpha(0);
        }
        showBuffer();
        Log.d(TAG, " initClipInfo ");
        Intent intent = getIntent();
        if (intent != null) {
            mSection = intent.getStringExtra(EventProperty.SECTION);
            bundle = intent.getExtras();
            item = (Item) bundle.get("item");
            if (item != null) {
                clip = item.clip;
                live_video = item.live_video;
                isPreview = item.isPreview;
                if (isPreview && live_video)
                    live_video = false;
                section = item.section;
                channel = item.channel;
                slug = item.slug;
                fromPage = item.fromPage;
            }
            // use to get mUrl, and registerActivity
            DaisyUtils.getVodApplication(this).addActivityToPool(this.toString(), this);
            String info = bundle.getString("ismartv");
            urlInfo = AccessProxy.getIsmartvClipInfo(info);
            if (item.item_pk == item.pk)
                itemUrl = SimpleRestClient.root_url + "/api/item/" + item.pk + "/";
            else
                itemUrl = SimpleRestClient.root_url + "/api/item/" + item.item_pk + "/";

            PlayCheckManager.getInstance().check(String.valueOf(item.item_pk), new PlayCheckManager.Callback() {
                @Override
                public void onSuccess(boolean isBuy) {
                    if (isBuy) {
                        isPreview = false;
                        new FetchClipTask().execute();
                    } else {
                        if (isPreview) {
                            serialItem = (Item) bundle.get("seraItem");
                            initPlayer();
                        } else {
                            getAdInfo("qiantiepian");
                        }
                    }
                }

                @Override
                public void onFailure() {

                }
            });
        }
    }

    private HashMap<String, Integer> adTimeMap = new HashMap<String, Integer>();

    protected void showAd(ArrayList<AdElement> result, String adpid) {
        adElement = new Stack<AdElement>();
        for (int i = 0; i < result.size(); i++) {
            AdElement element = result.get(i);
            if (element.getRoot_retcode() != 200)
                break;
            if (element.getRetcode() != 200) {
                // report error log
            } else {
                if ("video".equals(element.getMedia_type())) {
                    adsumtime += element.getDuration();
                }
                adElement.push(element);
            }
        }
        if ("zanting".equals(adpid) && adElement.isEmpty())
            return;
        playAdElement();
    }

    private String[] paths = null;

    private void playAdElement() {
        hidePanel();
        if (!adElement.isEmpty()) {
            int count = adElement.size();
            if (count > 0) {
                if (!(adElement.size() == 1 && !"video".equals(adElement.get(0).getMedia_type()))) {
                    paths = new String[count + 1];
                }

                AdElement element;
                int i = 0;

                while (!adElement.empty()) {
                    // if ("video".equals(element.getMedia_type())) {
                    // currPosition = 0;
                    element = adElement.pop();
                    if ("video".equals(element.getMedia_type())) {
                        paths[i] = element.getMedia_url();
                        adlog.put(paths[i], element.getMedia_id());
                        i++;
                    } else {
                        adimageDialog = new AdImageDialog(this,
                                R.style.UserinfoDialog, element.getMedia_url(), element.getTitle(), element.getMedia_id());

                        adimageDialog.getWindow().clearFlags(
                                WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                        try {
                            adimageDialog.show();
                        } catch (android.view.WindowManager.BadTokenException e) {
                        }
                        //paths = null;
                        return;
                    }
                    // }
                }
                isadvideoplaying = true;
                ad_count_view.setVisibility(View.VISIBLE);
                ad_count_view.setText("广告倒计时" + adsumtime);
            }
        } else {
            ad_count_view.setVisibility(View.GONE);
            isadvideoplaying = false;
        }
        playMainVideo();
    }

    protected void playMainVideo() {
        if (item.expense != null && !item.ispayed && !isPreview) {
            orderCheck();
        } else {
            if (urlInfo != null && item.pk == item.item_pk) {// 单集节目
                initPlayer();
            } else {// 电视剧单集,需要反查父类
                fetchItemInfo(String.valueOf(item.item_pk));
                new FetchSeriallTask().execute();
            }
        }
    }

    // 根据电视剧单集获取电视剧全部信息
    class FetchSeriallTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPostExecute(Boolean result) {
            if (result) { // 免费或已经付费的电视剧单集直接播放
                initPlayer();
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean result = false;
            try {
                currNum = item.position;
                Log.d(TAG, "currNum ===" + currNum);
                if (listItems.size() == 0) { // 剧集列表为空，第一次请求需要将所有剧集添加
                    String seriaUrl = SimpleRestClient.root_url + "/api/item/"
                            + item.item_pk + "/";
                    serialItem = simpleRestClient.getItem(seriaUrl);
                    if (serialItem != null && serialItem.subitems != null) {
                        listItems = new ArrayList<Item>();
                        for (int i = 0; i < serialItem.subitems.length; i++) {
                            serialItem.subitems[i].content_model = serialItem.content_model;
                            serialItem.subitems[i].expense = serialItem.expense;
                            listItems.add(serialItem.subitems[i]);
                        }
                    }
                    result = true;
                } else { // 电视剧联播，非第一集需要重新获取clip
                    clip = item.clip;
                    urlInfo = AccessProxy.parse(SimpleRestClient.root_url
                                    + "/api/clip/" + clip.pk + "/", VodUserAgent
                                    .getAccessToken(SimpleRestClient.sn_token),
                            PlayerActivity.this);

                    if (urlInfo.getIqiyi_4_0().length() > 0) {
                        Intent intent = new Intent();
                        intent.setAction("tv.ismar.daisy.qiyiPlay");
                        intent.putExtra("iqiyi", urlInfo.getIqiyi_4_0());
                        intent.putExtra("item", item);
                        startActivity(intent);
                        PlayerActivity.this.finish();
                    } else {
                        result = true;
                    }
                }
//				}
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
                result = false;
            } catch (ItemOfflineException e) {
                e.printStackTrace();
                result = false;
            } catch (NetworkException e) {
                e.printStackTrace();
                result = false;
            }
            if (serialItem != null && serialItem.expense != null
                    && !payedItemspk.contains(item.pk)) {
                orderCheck();
                return false;
            }
            return result;
        }
    }

    class FetchClipTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPostExecute(Boolean result) {
            if (result)
                initPlayer();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Boolean result = false;
            if (item != null) {
                clip = item.clip;
                urlInfo = AccessProxy.parse(SimpleRestClient.root_url
                                + "/api/clip/" + clip.pk + "/",
                        VodUserAgent.getAccessToken(SimpleRestClient.sn_token),
                        PlayerActivity.this);
                if (urlInfo.getIqiyi_4_0().length() > 0) {
                    Intent intent = new Intent();
                    intent.setAction("tv.ismar.daisy.qiyiPlay");
                    intent.putExtra("iqiyi", urlInfo.getIqiyi_4_0());
                    intent.putExtra("item", item);
                    startActivity(intent);
                    PlayerActivity.this.finish();
                } else {
                    result = true;
                }
            }
            return result;
        }
    }

    long lastTouchTime = 0;
    boolean doubleTouch = true;
    private int preX, upX;
    private int preY, upY;
    private int moveX, moveY;

    private void setVideoActionListener() {

        mVideoView.setOnTouchListener(new OnTouchListener() {
            float downY;
            float downX;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        downY = event.getY();
                        downX = event.getX();
                        preX = (int) event.getX();
                        preY = (int) event.getY();
                        long currentTime = System.currentTimeMillis();
                        if (isadvideoplaying)
                            return false;
                        if (doubleTouch == true && lastTouchTime != 0 && currentTime - lastTouchTime < 500) {
                            if (!paused) {
                                pauseItem();
                                playPauseImage
                                        .setImageResource(R.drawable.paus);
                            } else {
                                resumeItem();
                                playPauseImage
                                        .setImageResource(R.drawable.play);
                            }
                            doubleTouch = false;
                        } else {
                            if (panelShow) {
                                //				hideMenuHandler.post(hideMenuRunnable);
                                hidePanelHandler.removeCallbacks(hidePanelRunnable);
                                hidePanelHandler.postDelayed(hidePanelRunnable, 3000);
                            } else {
                                showPanel();

                            }
//							getSupportFragmentManager().beginTransaction().hide(mEpisodeFragment).commit();
                        }
                        lastTouchTime = currentTime;
                        doubleTouch = true;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float moveY = event.getY() - downY;
                        float moveX = event.getX() - downX;
                        int halfScreenH = getWindowManager().getDefaultDisplay().getHeight() / 2;
                        int halfScreenW = getWindowManager().getDefaultDisplay().getWidth() / 2;
                        float movePercent = -moveY / halfScreenH;
                        if (Math.abs(moveY) > 100) {
                            if (event.getX() < halfScreenW && downX < halfScreenW) {
                                if (volumn.getVisibility() == View.INVISIBLE)
                                    changeBright(movePercent);
                            } else if (downX > halfScreenW) {
                                if (bright.getVisibility() == View.INVISIBLE)
                                    changeVolumn(movePercent);
                            }
                            downY = event.getY();
                            downX = event.getX();
                        }
                        if (Math.abs(moveX) > 100) {
                            changePlayProgress(moveX);
                            downX = event.getX();
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        upX = (int) event.getX();
                        upY = (int) event.getY();
                        spearEvent(compare(upX, upY), event);
                        bright.setVisibility(View.INVISIBLE);
                        volumn.setVisibility(View.INVISIBLE);
                        play_progress.setVisibility(View.INVISIBLE);
                        break;
                }
                return true;
            }
        });
        mVideoView
                .setOnPreparedListenerUrl(new SmartPlayer.OnPreparedListenerUrl() {
                    @Override
                    public void onPrepared(SmartPlayer mp, String url) {
                        if (paths != null
                                && paths[paths.length - 1].equals(url)) {

                            if (mVideoView != null) {
                                if (live_video) {
                                    timeBar.setEnabled(false);
                                    mp.start();
                                } else {
                                    clipLength = mp.getDuration();
                                    timeBar.setMax(clipLength);
                                    progress_play.setMax(clipLength);
                                    if (paths != null) {
                                        initPlayerRelatedUI();
                                    }
                                    if (currPosition == 0)
                                        mp.start();
                                    else
                                        mp.seekTo(currPosition);
                                    // isBuffer = true;
                                    // showBuffer();
                                    timeBar.setProgress(currPosition);
                                    progress_play.setProgress(currPosition);
                                    timeBar.setEnabled(true);
                                    isadvideoplaying = false;
                                }
                                checkTaskStart(0);
                                timeTaskStart(0);
                            }

                        } else {
                            // 广告
//							if (mHandler.hasMessages(AD_COUNT_ACTION))
//								mHandler.removeMessages(AD_COUNT_ACTION);
//							mHandler.sendEmptyMessageDelayed(AD_COUNT_ACTION,
//									1000);
                            mp.start();
                            checkTaskStart(0);
                            adurl = url;
                            callaPlay.ad_play_load(
                                    item.fromPage,
                                    item.channel,
                                    item.slug,
                                    (System.currentTimeMillis() - bufferDuration),
                                    VodUserAgent.getMediaIp(url),
                                    item.pk,
                                    adlog.get(url), "bestv");
                        }
                        bufferDuration = System.currentTimeMillis();
                    }
                });
        mVideoView
                .setOnCompletionListenerUrl(new SmartPlayer.OnCompletionListenerUrl() {
                    @Override
                    public void onCompletion(SmartPlayer smartPlayer, String url) {
                        if (paths != null && url != null
                                && paths[paths.length - 1].equals(url)) {
                            if (item.isPreview) {
                                if (item.expense == null) {
                                    if (item.pk != item.item_pk && serialItem != null) {
                                        Intent intent = new Intent();
                                        intent.setClass(PlayerActivity.this,
                                                DramaListActivity.class);
                                        intent.putExtra("item", serialItem);
                                        startActivity(intent);
                                    }
                                    finish();
                                    return;
                                }
                                mVideoView.stopPlayback();
                                if (item.isPreview && "sport".equals(item.content_model) && item.live_video) {
                                    finish();
                                } else {
                                    PaymentDialog dialog = new PaymentDialog(
                                            PlayerActivity.this,
                                            R.style.PaymentDialog,
                                            ordercheckListener);
                                    item.model_name = "item";
                                    dialog.setItem(item);
                                    dialog.show();
                                    isPaymentdialogShow = true;
                                }
                            } else {
                                gotoFinishPage();
                            }

                        } else if (paths != null) {
                            callaPlay.ad_play_exit(
                                    item.fromPage,
                                    item.channel,
                                    item.slug,
                                    (System.currentTimeMillis() - bufferDuration),
                                    VodUserAgent.getMediaIp(url), item.pk,
                                    adlog.get(url), "bestv");
                            adsumtime -= (currPosition / 1000);
                            if (paths[paths.length - 2].equals(url)) {
                                if (mHandler.hasMessages(AD_COUNT_ACTION))
                                    mHandler.removeMessages(AD_COUNT_ACTION);
                                ad_count_view.setVisibility(View.GONE);
                                initPlayerRelatedUI();
                                isadvideoplaying = false;
                            }
                        } else {
                            Log.i("zhangjiqiangtest", "playerActivity onCompletion else");
                        }
                    }
                });
        mVideoView.setOnErrorListener(new SmartPlayer.OnErrorListener() {
            @Override
            public boolean onError(SmartPlayer mp, int what, int extra) {
                Log.d(TAG, "mVideoView  Error setVideoPath urls[currQuality] ");
                addHistory(currPosition);
                ExToClosePlayer("error", what + " " + extra);
                return false;
            }
        });

        mVideoView
                .setOnSeekCompleteListener(new SmartPlayer.OnSeekCompleteListener() {

                    @Override
                    public void onSeekComplete(SmartPlayer mp) {
                        // TODO Auto-generated method stub
                        if (!mp.isPlaying()) {
                            mp.start();
                            playPauseImage.setImageResource(R.drawable.play);
                            paused = false;
                        }

//						if (paused) {
//							mp.pause();
//						}
                        isBuffer = false;
                        isSeek = false;
//						hideBuffer();
                        checkTaskStart(500);
                        timeTaskStart(500);
                    }
                });
        mVideoView
                .setOnBufferingUpdateListener(new SmartPlayer.OnBufferingUpdateListener() {

                    @Override
                    public void onBufferingUpdate(SmartPlayer mp, int percent) {
                    }

                });
        mVideoView.setOnInfoListener(new SmartPlayer.OnInfoListener() {

            @Override
            public boolean onInfo(SmartPlayer smartplayer, int i, int j) {
                Log.v("@@@@", "i =" + i + ",.,.j=" + j);
                if (i == SmartPlayer.MEDIA_INFO_BUFFERING_START || i == 809) {
                    isBuffer = true;
                    bufferText.setText(BUFFERING + " " + 0 + "%");
                    showBuffer();
                } else if (i == 704) {
                    bufferText.setText(BUFFERING + " " + j + "%");
                } else if (i == SmartPlayer.MEDIA_INFO_BUFFERING_END || i == 1002 || i == 705
                        || i == 3) {
                    if (i == 3 && !isadvideoplaying) {
                        if (item.pk != item.pk) {
                            callaPlay.videoPlayLoad(
                                    item.item_pk,
                                    item.pk,
                                    item.title,
                                    clip.pk,
                                    currQuality,
                                    (System.currentTimeMillis() - startDuration),
                                    speed, mediaip, sid, urls[currQuality], "bestv");
                            callaPlay.videoPlayStart(item.item_pk, item.pk,
                                    item.title, clip.pk, currQuality, speed, sid,
                                    "bestv");
                        } else {
                            callaPlay.videoPlayLoad(
                                    item.pk,
                                    null,
                                    item.title,
                                    clip.pk,
                                    currQuality,
                                    (System.currentTimeMillis() - startDuration),
                                    speed, mediaip, sid, urls[currQuality], "bestv");
                            callaPlay.videoPlayStart(item.pk, null, item.title,
                                    clip.pk, currQuality, speed, sid, "bestv");
                        }
                    }
                    bufferText.setText(BUFFERING + " " + 100 + "%");
                    isBuffer = false;
                    hideBuffer();
                }
                return false;
            }
        });
        mVideoView.setOnTsInfoListener(new SmartPlayer.OnTsInfoListener() {
            @Override
            public void onTsInfo(SmartPlayer smartPlayer, Map<String, String> stringStringMap) {
                String spd = stringStringMap.get("TsDownLoadSpeed");
                speed = Integer.parseInt(spd);
                speed = speed / (1024 * 8);
                mediaip = stringStringMap.get(SmartPlayer.DownLoadTsInfo.TsIpAddr);
            }
        });

        mVideoView.setOnM3u8IpListener(new SmartPlayer.OnM3u8IpListener() {

            @Override
            public void onM3u8TsInfo(SmartPlayer mp, String ip) {
                // TODO Auto-generated method stub
                Log.e("MediaCodec", "m3u8ip test ip = " + ip);
                mediaip = ip;
            }
        });
    }

    private float screenWidth = 2560;

    private void spearEvent(int compare, MotionEvent event) {
        switch (compare) {
            case MOVE_LEFT:
//				if (clipLength > 0 && !live_video) {
//					isSeek = true;
//					showPanel();
//					isBuffer = true;
//					showBuffer();
//					fastForward(SHORT_STEP);
//					mVideoView.seekTo(currPosition);
//					isSeekBuffer = true;
//					Log.d(TAG, "RIGHT seek to" + getTimeString(currPosition));
//					isSeek = false;
//					offsets = 0;
//					offn = 1;
//				}
                break;
            case MOVE_RIGHT:
//				if (clipLength > 0 && !live_video) {
//					isSeek = true;
//					showPanel();
//					isBuffer = true;
//					showBuffer();
//					fastForward(SHORT_STEP);
//					mVideoView.seekTo(currPosition);
//					isSeekBuffer = true;
//					Log.d(TAG, "RIGHT seek to" + getTimeString(currPosition));
//					isSeek = false;
//					offsets = 0;
//					offn = 1;
//				}
                break;
            case MOVE_TOP:
//
                break;
            case MOVE_BOTTOM:
//				if (upX<=screenWidth/2){
//
//				}else{
//
//				}
//				break;
            default:
                break;
        }
    }

    private void onVolumnSlide(float percentage) {


    }

    private void onBrightnessSlide(float percentage) {

    }

    private int compare(int x, int y) {
        int dltX = x - preX, dltY = y - preY;
        if (Math.abs(dltX) > Math.abs(dltY)) {
            return dltX > 0 ? MOVE_RIGHT : MOVE_LEFT;
        } else {
            return dltY > 0 ? MOVE_TOP : MOVE_BOTTOM;
        }
    }

    private void getAdInfo(String adpid) {
        StringBuffer directorsBuffer = new StringBuffer();
        StringBuffer actorsBuffer = new StringBuffer();
        StringBuffer genresBuffer = new StringBuffer();
        try {
            Attribute.Info[] directorarray = (Attribute.Info[]) item.attributes.map
                    .get("director");
            Attribute.Info[] actorarray = (Attribute.Info[]) item.attributes.map
                    .get("actor");
            Attribute.Info[] genrearray = (Attribute.Info[]) item.attributes.map
                    .get("genre");
            if (directorarray != null) {
                for (int i = 0; i < directorarray.length; i++) {
                    if (i == 0)
                        directorsBuffer.append("[");
                    directorsBuffer.append(directorarray[i].id);
                    if (i >= 0 && i != directorarray.length - 1)
                        directorsBuffer.append(",");
                    if (i == directorarray.length - 1)
                        directorsBuffer.append("]");
                }
            }
            if (actorarray != null) {
                for (int i = 0; i < actorarray.length; i++) {
                    if (i == 0)
                        actorsBuffer.append("[");
                    actorsBuffer.append(actorarray[i].id);
                    if (i >= 0 && i != actorarray.length - 1)
                        actorsBuffer.append(",");
                    if (i == actorarray.length - 1)
                        actorsBuffer.append("]");
                }
            }
            if (genrearray != null) {
                for (int i = 0; i < genrearray.length; i++) {
                    if (i == 0)
                        genresBuffer.append("[");
                    genresBuffer.append(genrearray[i].id);
                    if (i >= 0 && i != genrearray.length - 1)
                        genresBuffer.append(",");
                    if (i == genrearray.length - 1)
                        genresBuffer.append("]");
                }
            }
        } catch (NullPointerException e) {
        }


        String channelSection = "channel=" + "" + "&section=" + "";
        if (StringUtils.isNotEmpty(channel)) {
            if (StringUtils.isNotEmpty(slug)) {
                channelSection = "channel=" + channel + "&section="
                        + slug;
            } else {
                channelSection = "channel=" + channel + "&section="
                        + "";
            }

        }

        String params = channelSection
                + "&itemid="
                + item.pk
                + "&topic="
                + ""
                + "&source="
                + fromPage
                + "&genre="
                + genresBuffer.toString()
                + "&content_model="
                + item.content_model
                + "&director="
                + directorsBuffer.toString()
                + "&actor="
                + actorsBuffer.toString()
                + "&clipid="
                + (item.clip == null ? "" : item.clip.pk)
                + "&live_video="
                + item.live_video
                + "&vendor="
                + Base64.encodeToString(item.vendor == null ? "".getBytes()
                : item.vendor.getBytes(), Base64.URL_SAFE)
                + "&expense=" + (item.expense == null ? false : true)
                + "&length=" + item.clip.length;
        adAsyncTask = new GetAdDataTask();
        adAsyncTask.execute(adpid, params);
    }

    // 初始化logo图片
    private class LogoImageTask extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                logoImage.setImageBitmap(result);
            }
        }

        @Override
        protected Bitmap doInBackground(String... arg0) {
            if (item.logo != null && item.logo.length() > 0)
                if (NetworkUtils.getInputStream(item.logo) != null) {
                    Log.d(TAG, "item.logo ===" + item.logo);
                    logoInputStream = NetworkUtils.getInputStream(item.logo);
                    if (logoInputStream != null) {
                        Bitmap bitmap = ImageUtils.getBitmapFromInputStream(
                                logoInputStream, 160, 50);
                        return bitmap;
                    }
                }
            return null;

        }
    }

    private void initPlayer() {
        if (item.pk != item.item_pk) {
            subItemUrl = SimpleRestClient.root_url + "/api/subitem/" + item.pk
                    + "/";
        }
        try {
            if (urlInfo != null) {
                urls[0] = urlInfo.getNormal();
                urls[1] = urlInfo.getMedium();
                urls[2] = urlInfo.getHigh();
                urls[3] = urlInfo.getAdaptive();
                historyManager = DaisyUtils.getHistoryManager(this);
                Quality quality = historyManager.getQuality();
                if (quality != null) {
                    currQuality = quality.quality;
                } else {
                    for (int i = urls.length - 1; i > -1; i--) {
                        if (urls[i] != null && !urls[i].isEmpty()) {
                            currQuality = i;
                            break;
                        }
                    }
                }
                if (urls[currQuality] == null || urls[currQuality].isEmpty()) {
                    for (int i = urls.length - 1; i > -1; i--) {
                        if (urls[i] != null && !urls[i].isEmpty()) {
                            currQuality = i;
                            break;
                        }
                    }
                }
                Log.d(TAG, "currQuality =====" + currQuality);
                if (item != null) {
                    if (item.pk != item.item_pk) {
                        if (SimpleRestClient.isLogin()) {
                            mHistory = historyManager.getHistoryByUrl(itemUrl,
                                    "yes");
                        } else {
                            mHistory = historyManager.getHistoryByUrl(itemUrl,
                                    "no");
                        }
                        mTitle = item.title;
                    } else {
                        if (SimpleRestClient.isLogin()) {
                            mHistory = historyManager.getHistoryByUrl(itemUrl,
                                    "yes");
                        } else {
                            mHistory = historyManager.getHistoryByUrl(itemUrl,
                                    "no");
                        }
                        mTitle = item.title;
                    }
                    if (mHistory != null) {
                        if (mHistory.is_continue) {
                            if (mHistory.sub_url != null
                                    && mHistory.sub_url.equals(subItemUrl)) {
                                isContinue = mHistory.is_continue;
                                tempOffset = (int) mHistory.last_position;
                                if (urls[mHistory.last_quality] != null
                                        && !urls[mHistory.last_quality]
                                        .isEmpty()) {
                                    currQuality = mHistory.last_quality;
                                }
                            } else if (mHistory.sub_url == null
                                    && mHistory.url != null) {
                                isContinue = mHistory.is_continue;
                                tempOffset = (int) mHistory.last_position;
                                if (urls[mHistory.last_quality] != null
                                        && !urls[mHistory.last_quality]
                                        .isEmpty()) {
                                    currQuality = mHistory.last_quality;
                                }
                            }
                        }
                    } else {
                        if (!isPreview) {
//							tempOffset = 0;
                            isContinue = true;
                        }
                    }

                    // initQualtiyText();
                    // titleText.setText(subItem.title);
                    // qualityText.setVisibility(View.VISIBLE);
                    // if (tempOffset > 0 && isContinue == true && !live_video)
                    // {
                    // bufferText.setText("  " + BUFFERCONTINUE
                    // + getTimeString(tempOffset));
                    // } else {
                    // bufferText.setText(PlAYSTART + "《"
                    // + titleText.getText() + "》");
                    // }
                    // new LogoImageTask().execute();
                }
                // if (tempOffset > 0 && isContinue) {
                // currPosition = tempOffset;
                // seekPostion = tempOffset;
                // } else {
                // currPosition = 0;
                // seekPostion = 0;
                // }
                if (urls != null && mVideoView != null) {
                    // TaskStart();// cmstest.tvxio.com
                    sid = HardwareUtils.getMd5ByString(SimpleRestClient.sn_token + System.currentTimeMillis());
                    startDuration = System.currentTimeMillis();
                    isBuffer = true;
                    showBuffer();
                    if (!isadvideoplaying) {
                        paths = new String[1];
                        paths[0] = urls[currQuality];
                        mVideoView.setVideoPaths(paths);
                        initPlayerRelatedUI();
                    } else {
                        if (paths != null) {
                            startDuration = System.currentTimeMillis();
                            paths[paths.length - 1] = urls[currQuality];
                            mVideoView.setVideoPaths(paths);
                        }
                    }
                    ismedialplayerinit = false;
                }
                if (item != null)
                    callaPlay.videoStart(item, item.pk, item.title,
                            currQuality, null, speed, mSection, sid, "bestv");
                else
                    callaPlay.videoStart(item, null, item.title, currQuality,
                            null, speed, mSection, sid, "bestv");
            } else {
                ExToClosePlayer("url", "m3u8 content error ,");
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            ExToClosePlayer("url", e.toString());
        }
    }

    private String mTitle;

    private void initPlayerRelatedUI() {

        titleText.setText(mTitle);
        initQualtiyText();
        qualityText.setVisibility(View.VISIBLE);
        if (tempOffset > 0 && isContinue == true && !live_video) {
            bufferText.setText("  " + BUFFERCONTINUE
                    + getTimeString(tempOffset));
        } else {
            bufferText.setText(PlAYSTART + "《" + titleText.getText() + "》");
        }
        new LogoImageTask().execute();

        if (tempOffset > 0 && isContinue) {
            currPosition = tempOffset;
            seekPostion = tempOffset;
        } else {
            currPosition = 0;
            seekPostion = 0;
        }
        showPanel();
    }

    private void timeTaskStart(int delay) {
        mHandler.removeCallbacks(mUpdateTimeTask);
        if (delay > 0) {
            mHandler.postDelayed(mUpdateTimeTask, delay);
        } else {
            mHandler.post(mUpdateTimeTask);
        }
    }

    private void timeTaskPause() {
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    private Runnable mUpdateTimeTask = new Runnable() {
        @Override
        public void run() {
            if (mVideoView != null) {
                if (mVideoView.isPlaying()) {
                    seekPostion = mVideoView.getCurrentPosition();
                }
                mHandler.postDelayed(mUpdateTimeTask, 500);
            } else {
                Log.d(TAG, "mVideoView ======= null or err");
                timeTaskPause();
            }
        }
    };

    private void ExToClosePlayer(String code, String content) {
        if (bufferText != null) {
            bufferText.setText(EXTOCLOSE);
            if (code == "url") {
                try {
                    if (item != null)
                        callaPlay.videoExcept("noplayaddress", content,
                                item.item_pk, item.pk, item.title, clip.pk,
                                currQuality, speed, "bestv");
                    else
                        callaPlay.videoExcept("noplayaddress", content,
                                item.pk, null, item.title, clip.pk,
                                currQuality, speed, "bestv");
                } catch (Exception e) {
                    Log.e(TAG,
                            " Sender log videoExcept noplayaddress "
                                    + e.toString());
                }
            }
            if (code == "error") {
                try {
                    if (item != null)
                        callaPlay.videoExcept("mediaexception", content,
                                item.item_pk, item.pk, item.title, clip.pk,
                                currQuality, currPosition, "bestv");
                    else
                        callaPlay.videoExcept("mediaexception", content,
                                item.pk, null, item.title, clip.pk,
                                currQuality, currPosition, "bestv");
                } catch (Exception e) {
                    Log.e(TAG,
                            " Sender log videoExcept noplayaddress "
                                    + e.toString());
                }
            }

        }
        mHandler.postDelayed(finishPlayerActivity, 3000);
    }

    private Runnable finishPlayerActivity = new Runnable() {
        public void run() {
            mHandler.removeCallbacks(finishPlayerActivity);
            PlayerActivity.this.finish();

        }
    };

    private void checkTaskStart(int delay) {
        mCheckHandler.removeCallbacks(checkStatus);
        if (delay > 0) {
            mCheckHandler.postDelayed(checkStatus, delay);
        } else {
            mCheckHandler.post(checkStatus);
        }
    }

    private void checkTaskPause() {
        mCheckHandler.removeCallbacks(checkStatus);
    }

    private Runnable checkStatus = new Runnable() {
        public void run() {
            if (mVideoView != null) {
                if (mVideoView.isPlaying()) {
//					if (bufferLayout.isShown()) {
//						isBuffer = false;
//						hideBuffer();
//					}
                    // if (isadvideoplaying && bufferLayout.isShown()) {
                    // isBuffer = false;
                    // hideBuffer();
                    // }
                    if (mVideoView.getAlpha() < 1) {
                        mVideoView.setAlpha(1);
                        bufferText.setText(BUFFERING);
                    }
                    if (!isSeek && !isBuffer && !live_video) {
                        currPosition = mVideoView.getCurrentPosition();
                        timeBar.setProgress(currPosition);
                    }
                    if (isadvideoplaying) {
                        ad_count_view.setText("广告倒计时" + (adsumtime - (currPosition / 1000)));
                    }
                } else {
                    if (!paused && !isBuffer) {
                        seekPostion = mVideoView.getCurrentPosition();
                    }
                }
                mCheckHandler.postDelayed(checkStatus, 300);
            } else {
                Log.d(TAG, "mVideoView ====== null or err");
                checkTaskPause();
            }

        }

    };

    private void gotoFinishPage() {
        timeTaskPause();
        checkTaskPause();
        try {
            if (item != null)
                callaPlay
                        .videoExit(
                                item.item_pk,
                                item.pk,
                                item.title,
                                clip.pk,
                                currQuality,
                                0,
                                "end",
                                currPosition,
                                (System.currentTimeMillis() - startDuration),
                                item.slug, sid, "list",
                                item.content_model, "bestv");// String
                // section,String
                // sid,String
                // source,String
                // channel
            else
                callaPlay
                        .videoExit(
                                item.pk,
                                null,
                                item.title,
                                clip.pk,
                                currQuality,
                                0,
                                "end",
                                currPosition,
                                (System.currentTimeMillis() - startDuration),
                                item.slug, sid, "list",
                                item.content_model, "bestv");
        } catch (Exception e) {
            Log.e(TAG, " log Sender videoExit end " + e.toString());
        }
        if (mVideoView != null) {
            if (listItems != null && listItems.size() > 0
                    && currNum < (listItems.size() - 1)) {

                if (menu == null) {
                    createWindow();
                    menu = new ISTVVodMenu(this);
                    createMenu(menu);
                }
                ISTVVodMenuItem ii1 = menu.findItem(item.pk);
                if (ii1 != null) {
                    ii1.unselect();
                }
                item = listItems.get(currNum + 1);
                ISTVVodMenuItem ii2 = menu.findItem(item.pk);
                if (ii2 != null) {
                    ii2.select();
                }
                subItemUrl = SimpleRestClient.root_url + "/api/subitem/"
                        + item.pk + "/";
                bundle.remove("url");
                bundle.putString("url", subItemUrl);
                addHistory(0);
                if (mVideoView != null) {
                    mVideoView.setAlpha(0);
                }
                checkContinueOrPay(item.pk);
            } else {
                if (paystatus) {
                    Intent data = new Intent();
                    data.putExtra("result", paystatus);
                    setResult(20, data);
                }
                Intent intent = new Intent("tv.ismar.daisy.PlayFinished");
                intent.putExtra("item", item);
                startActivity(intent);
                seekPostion = 0;
                currPosition = 0;
                mVideoView = null;
                addHistory(0);
                PlayerActivity.this.finish();

            }
        }

    }

    private void checkContinueOrPay(final int pk) {
        if (payedItemspk.contains(pk) || item.expense == null) { // 已经付费或免费
            isBuffer = true;
            showBuffer();
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    subItemUrl = SimpleRestClient.root_url + "/api/subitem/"
                            + pk + "/";
                    try {
                        item = simpleRestClient.getItem(subItemUrl);
                        item.channel = channel;
                        item.section = section;
                        item.slug = slug;
                        item.fromPage = fromPage;
                        getAdInfo("qiantiepian");
                    } catch (JsonSyntaxException e) {
                        e.printStackTrace();
                    } catch (ItemOfflineException e) {
                        e.printStackTrace();
                    } catch (NetworkException e) {
                        e.printStackTrace();
                    }
                }

            }.start();
        } else {
            mVideoView.stopPlayback();
            isPaymentdialogShow = true;
            PaymentDialog dialog = new PaymentDialog(PlayerActivity.this,
                    R.style.PaymentDialog, ordercheckListener);
            item.model_name = "subitem";
            dialog.setItem(item);
            dialog.show();
        }
    }

    private void addHistory(int last_position) {
        if (item != null && historyManager != null) {
            Log.d(TAG, "historyManager title ==" + item.title);
            Log.d(TAG, "historyManager itemUrl ==" + itemUrl);
            Log.d(TAG, "historyManager subItemUrl ==" + subItemUrl);
            Log.d(TAG, "historyManager last_position ==" + last_position);
            Log.d(TAG, "historyManager isContinue ==" + isContinue);
            History history = new History();
            if (item != null && item.title != null) {
                history.title = item.title;
            } else {
                history.title = item.title;
            }
            if (item.expense != null) {
                history.price = (int) item.expense.price;
                history.paytype = item.expense.pay_type;
                history.cptitle = item.expense.cptitle;
                history.cpid = item.expense.cpid;
                history.cpname = item.expense.cpname;
            } else
                history.price = 0;
            history.adlet_url = item.adlet_url;
            history.content_model = item.content_model;
            history.is_complex = item.is_complex;
            history.last_position = last_position;
            history.last_quality = currQuality;
            history.url = itemUrl;
            history.sub_url = subItemUrl;
            history.is_continue = isContinue;
            if (SimpleRestClient.isLogin())
                historyManager.addHistory(history, "yes");
            else
                historyManager.addHistory(history, "no");
        }
    }

    private void createHistory(int length) {
        if ("".equals(SimpleRestClient.access_token)) {
            return;// 不登录不必上传
        }
        int offset = length;
        if (length == clipLength) {
            offset = -1;
        }
        String params = "access_token=" + SimpleRestClient.access_token
                + "&device_token=" + SimpleRestClient.device_token + "&offset="
                + offset;
        if (item.pk != item.item_pk) {
            params = params + "&subitem=" + item.pk;
        } else {
            params = params + "&item=" + item.item_pk;
        }
        simpleRestClient.doSendRequest("/api/histories/create/", "post",
                params, new HttpPostRequestInterface() {

                    @Override
                    public void onSuccess(String info) {
                    }

                    @Override
                    public void onPrepare() {
                    }

                    @Override
                    public void onFailed(String error) {
                    }
                });
    }

    private void showPanel() {
        if (mVideoView == null || isadvideoplaying) {
            return;
        }
        if (isVodMenuVisible()
                || isadvideoplaying
                || !mVideoView.isPlaying()
                && (mVideoView.getmCurrentState() != IsmatvVideoView.STATE_PAUSED))
            return;
        if (!panelShow) {
            panelLayout.startAnimation(panelShowAnimation);
            panelLayout.setVisibility(View.VISIBLE);
            top_panel.startAnimation(top_to_down);
            top_panel.setVisibility(View.VISIBLE);
            panelShow = true;
            hidePanelHandler.postDelayed(hidePanelRunnable, 3000);
        } else {
            hidePanelHandler.removeCallbacks(hidePanelRunnable);
            hidePanelHandler.postDelayed(hidePanelRunnable, 3000);
        }


    }

    private void hidePanel() {
        if (panelShow) {
//			if(EntertainmentPop.isShowing()||popupWindow.isShowing()||itemPopWindow.isShowing()){
//				return;
//			}else {
            panelLayout.startAnimation(panelHideAnimation);
            panelLayout.setVisibility(View.GONE);

            top_panel.startAnimation(top_to_up);
            top_panel.setVisibility(View.GONE);
            panelShow = false;
//			}
        }
        getSupportFragmentManager().beginTransaction().hide(mEpisodeFragment).commitAllowingStateLoss();
    }

    private void pauseItem() {
        if (paused || mVideoView == null)
            return;
        // showBuffer();
        Log.d(TAG, "pause");
        hideBuffer();
        mVideoView.pause();
        paused = true;
        if (!(popupDlg != null && popupDlg.isShowing()))
            getAdInfo("zanting");
        if (item.pk != item.item_pk)
            callaPlay.videoPlayPause(item.item_pk, item.pk, item.title,
                    clip.pk, currQuality, speed, currPosition, sid, "bestv");
        else
            callaPlay.videoPlayPause(item.pk, null, item.title, clip.pk,
                    currQuality, speed, currPosition, sid, "bestv");

    }

    protected void resumeItem() {
        if (!paused || mVideoView == null)
            return;
        // hideBuffer();
        Log.d(TAG, "resume");
        mVideoView.start();

        if (item.pk != item.item_pk)
            callaPlay.videoPlayContinue(item.item_pk, item.pk, item.title,
                    clip.pk, currQuality, speed, currPosition, sid, "bestv");
        else
            callaPlay.videoPlayContinue(item.pk, null, item.title, clip.pk,
                    currQuality, speed, currPosition, sid, "bestv");
        if (!isBuffer) {
            timeTaskStart(0);
        }
        paused = false;
    }

    private void fastForward(int step) {
        if (currPosition > clipLength)
            return;
        if (offsets != 1 && offsets % 5 != 0) {
            offsets += step;
            Log.i("zhnagjiqiang", "offsets != 1 && offsets % 5 != 0");
        } else {
            if (offsets > 0) {
                offn = offsets / 5;
                Log.i("zhnagjiqiang", "offsets > 0");
            }
        }
        if (clipLength > 1000000) {
            if (offn < 11) {
                Log.i("zhnagjiqiang", "offn < 11");
                currPosition += clipLength * offn * 0.01;
            } else {
                Log.i("zhnagjiqiang", "offn >=11");
                currPosition += clipLength * 0.1;
            }
        } else {
            Log.i("zhnagjiqiang", "clipLength  <= 1000000");
            currPosition += 10000;
        }

        if (currPosition > clipLength) {
            currPosition = clipLength - 3000;
        }
        timeBar.setProgress(currPosition);
        Log.d(TAG, "seek Forward " + currPosition);
    }

    private void fastBackward(int step) {
        if (currPosition < 0)
            return;
        if (offsets != 1 && offsets % 5 != 0) {
            offsets += step;
        } else {
            if (offsets > 0) {
                offn = offsets / 5;
            }
        }
        if (clipLength > 1000000) {
            if (offn < 11) {
                currPosition -= clipLength * offn * 0.01;
            } else {
                currPosition -= clipLength * 0.1;
            }
        } else {
            currPosition -= 10000;
        }
        if (currPosition < 0)
            currPosition = 0;
        timeBar.setProgress(currPosition);
        Log.d(TAG, "seek Backward " + currPosition);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean ret = false;

        if (keyCode == KeyEvent.KEYCODE_BACK && item != null && "lockscreen".equals(item.fromPage)) {
            setResult(1010);
            finish();
            return false;
        }

        if (keyCode == KeyEvent.KEYCODE_BACK && !"false".equals(shardpref.getSharedPrefs(AccountSharedPrefs.FIRST_USE))) {
            gesture_tipview.setVisibility(View.GONE);
            shardpref.setSharedPrefs(AccountSharedPrefs.FIRST_USE, "false");
            initClipInfo();
            return false;
        }
        if (!"false".equals(shardpref.getSharedPrefs(AccountSharedPrefs.FIRST_USE))) {
            return false;
        }
        if ("lcd_s3a01".equals(VodUserAgent.getModelName())) {
            if (keyCode == 707 || keyCode == 774 || keyCode == 253) {
                isneedpause = false;
            }
        } else if ("lx565ab".equals(VodUserAgent.getModelName())) {
            if (keyCode == 82 || keyCode == 707 || keyCode == 253) {
                isneedpause = false;
            }
        } else {
            if (keyCode == 223 || keyCode == 499 || keyCode == 480) {
                isneedpause = false;
            }
        }
        if (isadvideoplaying) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                mVideoView.stopPlayback();
                finish();
            }
            return true;
        }

        if (!isVodMenuVisible() && mVideoView != null) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_LEFT:
                case KeyEvent.KEYCODE_MEDIA_REWIND:
                    mHandler.removeCallbacks(mUpdateTimeTask);
                    mHandler.removeCallbacks(checkStatus);
                    if (clipLength > 0 && !live_video) {
                        mVideoView.pause();
                        isSeek = true;
                        showPanel();
                        fastBackward(SHORT_STEP);
                        ret = true;
                        if (mHandler.hasMessages(MSG_SEK_ACTION))
                            mHandler.removeMessages(MSG_SEK_ACTION);
                        mHandler.sendEmptyMessageDelayed(MSG_SEK_ACTION, 1000);
                    }
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                    mHandler.removeCallbacks(mUpdateTimeTask);
                    mHandler.removeCallbacks(checkStatus);
                    if (clipLength > 0 && !live_video) {
                        mVideoView.pause();
                        isSeek = true;
                        showPanel();
                        fastForward(SHORT_STEP);
                        ret = true;
                        if (mHandler.hasMessages(MSG_SEK_ACTION))
                            mHandler.removeMessages(MSG_SEK_ACTION);
                        mHandler.sendEmptyMessageDelayed(MSG_SEK_ACTION, 1000);
                    }
                    break;
                case KeyEvent.KEYCODE_DPAD_CENTER:
                case KeyEvent.KEYCODE_ENTER:
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    if (clipLength > 0) {
                        showPanel();
                        if (!paused) {
                            pauseItem();
                            playPauseImage
                                    .setImageResource(R.drawable.paus);
                        } else {
                            resumeItem();
                            playPauseImage
                                    .setImageResource(R.drawable.play);
                        }

                        ret = true;
                    }
                    break;
                case KeyEvent.KEYCODE_A:
                case KeyEvent.KEYCODE_F1:
                case KeyEvent.KEYCODE_PROG_RED:

                    if (panelShow) {
                        hidePanel();
                    } else {
                        showPanel();
                    }
                    ret = true;

                    break;
                case KeyEvent.KEYCODE_DPAD_UP:

//				am.adjustStreamVolume(AudioManager.STREAM_MUSIC,
//						AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
//				showPanel();
                    if (menu != null && menu.isVisible())
                        return false;
                    if (menu == null) {
                        createWindow();
                        menu = new ISTVVodMenu(this);
                        ret = createMenu(menu);
                    }
                    if (onVodMenuOpened(menu)) {
                        menu.show();
                        hideMenuHandler.postDelayed(hideMenuRunnable, 60000);
                    }
                    //ret = true;

                    break;
                case KeyEvent.KEYCODE_DPAD_DOWN:
//				am.adjustStreamVolume(AudioManager.STREAM_MUSIC,
//						AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);

                    if (menu != null && menu.isVisible())
                        return false;
                    if (menu == null) {
                        createWindow();
                        menu = new ISTVVodMenu(this);
                        ret = createMenu(menu);
                    }
                    if (onVodMenuOpened(menu)) {
                        menu.show();
                        hideMenuHandler.postDelayed(hideMenuRunnable, 60000);
                    }
                    //ret = true;
                    break;
                case KeyEvent.KEYCODE_BACK:
                    if (isadvideoplaying)
                        finish();
                    showPopupDialog(
                            DIALOG_OK_CANCEL,
                            getResources().getString(
                                    R.string.vod_player_exit_dialog));
                    ret = true;
                    if (!paused) {
                        pauseItem();
                        playPauseImage
                                .setImageResource(R.drawable.paus);
                    }
                    break;
                case KeyEvent.KEYCODE_MENU:
                    if (menu != null && menu.isVisible())
                        return false;
                    if (menu == null) {
                        createWindow();
                        menu = new ISTVVodMenu(this);
                        ret = createMenu(menu);
                    }
                    if (onVodMenuOpened(menu)) {
                        menu.show();
                        hideMenuHandler.postDelayed(hideMenuRunnable, 60000);
                    }
                    break;
                case KeyEvent.KEYCODE_MEDIA_STOP:
                    finish();
                default:
                    break;
            }
        }
        if (mVideoView != null && ret == false) {
            ret = super.onKeyDown(keyCode, event);
        }
        return ret;
    }

    public void showPopupDialog(int type, String msg) {
        if (isadvideoplaying) {
            mHandler.removeMessages(AD_COUNT_ACTION);
        }
        if (type == DIALOG_OK_CANCEL) {
            popupDlg = new Dialog(this, R.style.PopupDialog) {
                @Override
                public void onBackPressed() {
                    super.onBackPressed();
                    if (paused) {
                        resumeItem();
                        if (isadvideoplaying) {
                            mHandler.removeMessages(AD_COUNT_ACTION);
                            mHandler.sendEmptyMessageDelayed(AD_COUNT_ACTION,
                                    1000);
                        }
                        playPauseImage
                                .setImageResource(R.drawable.play);
                    }
                }

            };
            View view;
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.popup_2btn, null);

            int width = getResources().getDimensionPixelSize(
                    R.dimen.popup_dialog_width);
            int height = getResources().getDimensionPixelSize(
                    R.dimen.popup_dialog_height);

            popupDlg.addContentView(view, new ViewGroup.LayoutParams(width,
                    height));
            Button btn1 = null, btn2 = null;
            btn1 = (Button) view.findViewById(R.id.confirm_exit);
            btn1.setText(R.string.vod_ok);
            btn2 = (Button) view.findViewById(R.id.cancel_exit);
            btn2.setText(R.string.vod_cancel);
            btn1.setOnHoverListener(new OnHoverListener() {

                @Override
                public boolean onHover(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_HOVER_ENTER || event.getAction() == MotionEvent.ACTION_HOVER_MOVE) {
                        v.requestFocus();
                    }
                    return false;
                }
            });
            btn2.setOnHoverListener(new OnHoverListener() {

                @Override
                public boolean onHover(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_HOVER_ENTER || event.getAction() == MotionEvent.ACTION_HOVER_MOVE) {
                        v.requestFocus();
                    }
                    return false;
                }
            });
            if (btn1 != null) {
                btn1.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if (popupDlg != null && mVideoView != null) {
                            addHistory(seekPostion);
                            checkTaskPause();
                            timeTaskPause();
                            popupDlg.dismiss();
                            try {
                                if (item.pk != item.item_pk)
                                    callaPlay.videoExit(
                                            item.item_pk,
                                            item.pk,
                                            item.title,
                                            clip.pk,
                                            currQuality,
                                            speed,
                                            "detail",
                                            currPosition,
                                            (System.currentTimeMillis() - startDuration),
                                            item.slug, sid, "list",
                                            item.content_model, "bestv");
                                else
                                    callaPlay.videoExit(
                                            item.pk,
                                            null,
                                            item.title,
                                            clip.pk,
                                            currQuality,
                                            speed,
                                            "detail",
                                            currPosition,
                                            (System.currentTimeMillis() - startDuration),
                                            item.slug, sid, "list",
                                            item.content_model, "bestv");
                            } catch (Exception e) {
                                Log.e(TAG,
                                        " Sender log videoPlayStart "
                                                + e.toString());
                            }
                            mVideoView.stopPlayback();
                            Intent data = new Intent();
                            data.putExtra("result", paystatus);
                            setResult(20, data);
                            PlayerActivity.this.finish();
                        }
                    }

                    ;
                });
            }

            if (btn2 != null) {
                btn2.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if (popupDlg != null) {
                            popupDlg.dismiss();
                            if (paused) {
                                resumeItem();
                                playPauseImage
                                        .setImageResource(R.drawable.play);
                            }
                        }
                    }

                    ;
                });
            }
            popupDlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    popupDlg = null;
                }
            });
            popupDlg.show();
        }
    }

    private Handler hidePanelHandler = new Handler();

    private Runnable hidePanelRunnable = new Runnable() {
        @Override
        public void run() {
            hidePanel();
            hidePanelHandler.removeCallbacks(hidePanelRunnable);
        }
    };

    private Handler hideMenuHandler = new Handler();

    private Runnable hideMenuRunnable = new Runnable() {
        @Override
        public void run() {
            if (menu != null) {
                menu.hide();
            }
            hideMenuHandler.removeCallbacks(hideMenuRunnable);
        }
    };

    private boolean isVodMenuVisible() {
        if (menu == null) {
            return false;
        }
        return menu.isVisible();
    }

    protected void showBuffer() {
        bufferDuration = System.currentTimeMillis();
        if (adElement != null && !adElement.isEmpty())
            return;
        bufferLayout.setVisibility(View.VISIBLE);
    }

    protected void hideBuffer() {
        if (mHandler.hasMessages(BUFFER_COUNTDOWN_ACTION)) {
            mHandler.removeMessages(BUFFER_COUNTDOWN_ACTION);
            buffercountDown = 0;
        }
        if (isadvideoplaying && StringUtils.isNotEmpty(adurl)) {
            callaPlay.ad_play_blockend(
                    item.fromPage,
                    item.channel,
                    item.slug,
                    (System.currentTimeMillis() - bufferDuration),
                    VodUserAgent.getMediaIp(adurl),
                    item.pk,
                    adlog.get(adurl), "bestv");
        }
        if (!isBuffer && bufferLayout.isShown()) {
            bufferText.setText(BUFFERING);
            bufferLayout.setVisibility(View.GONE);
            try {
                if (item.pk != item.item_pk)
                    if (isSeekBuffer) {
                        callaPlay
                                .videoPlaySeekBlockend(
                                        item.item_pk,
                                        item.pk,
                                        item.title,
                                        clip.pk,
                                        currQuality,
                                        speed,
                                        currPosition,
                                        (System.currentTimeMillis() - bufferDuration),
                                        mediaip, sid, "bestv");
                    } else {
                        callaPlay
                                .videoPlayBlockend(
                                        item.item_pk,
                                        item.pk,
                                        item.title,
                                        clip.pk,
                                        currQuality,
                                        speed,
                                        currPosition,
                                        (System.currentTimeMillis() - bufferDuration),
                                        mediaip, sid, "bestv");
                    }
                else if (isSeekBuffer) {
                    callaPlay
                            .videoPlaySeekBlockend(
                                    item.pk,
                                    null,
                                    item.title,
                                    clip.pk,
                                    currQuality,
                                    speed,
                                    currPosition,
                                    (System.currentTimeMillis() - bufferDuration),
                                    mediaip, sid, "bestv");

                } else {
                    callaPlay
                            .videoPlayBlockend(
                                    item.item_pk,
                                    item.pk,
                                    item.title,
                                    clip.pk,
                                    currQuality,
                                    speed,
                                    currPosition,
                                    (System.currentTimeMillis() - bufferDuration),
                                    mediaip, sid, "bestv");
                }
                isSeekBuffer = false;
            } catch (Exception e) {
                Log.e(TAG, " Sender log videoPlayBlockend " + e.toString());
            }
        }
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SEK_ACTION:
                    bufferText.setText(BUFFERING + " " + 0 + "%");
                    isBuffer = true;
                    showBuffer();
                    mVideoView.seekTo(currPosition);
                    if (item.pk != item.item_pk)
                        callaPlay.videoPlaySeek(item.item_pk, item.pk, item.title,
                                clip.pk, currQuality, speed, currPosition, sid, "bestv");
                    else
                        callaPlay.videoPlaySeek(item.pk, null, item.title,
                                clip.pk, currQuality, speed, currPosition, sid, "bestv");
                    isSeekBuffer = true;
                    Log.d(TAG, "LEFT seek to " + getTimeString(currPosition));
                    isSeek = false;
                    offsets = 0;
                    offn = 1;
                    break;
                case BUFFER_COUNTDOWN_ACTION:
                    buffercountDown++;
                    if (buffercountDown > 30) {
                        if (mHandler.hasMessages(BUFFER_COUNTDOWN_ACTION)) {
                            mHandler.removeMessages(BUFFER_COUNTDOWN_ACTION);
                            buffercountDown = 0;
                        }
                        showDialog("网络不给力，请检查网络或稍后再试!");
                    } else {
                        // mHandler.sendEmptyMessageDelayed(BUFFER_COUNTDOWN_ACTION,
                        // 1000);
                    }
                    break;
                case DISMISS_AD_DIALOG:
                    if (adimageDialog != null && adimageDialog.isShowing()) {
                        adimageDialog.dismiss();
                    }
                    break;
                case AD_COUNT_ACTION:
                    adsumtime--;
                    ad_count_view.setText("广告倒计时" + adsumtime);
                    if (adsumtime > 0) {
                        sendEmptyMessageDelayed(AD_COUNT_ACTION, 1000);
                    } else {
                        isadvideoplaying = false;
                        // if(mVideoView!=null)
                        // mVideoView.playIndex(paths.length - 1);
                        ad_count_view.setVisibility(View.GONE);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private String getTimeString(int ms) {
        int left = ms;
        int hour = left / 3600000;
        left %= 3600000;
        int min = left / 60000;
        left %= 60000;
        int sec = left / 1000;
        return String.format("%1$02d:%2$02d:%3$02d", hour, min, sec);
    }

    public void onVodMenuClosed(ISTVVodMenu istvVodMenu) {
        // TODO Auto-generated method stub

    }

    public boolean createMenu(ISTVVodMenu menu) {
        ISTVVodMenuItem sub;
        if (listItems != null && listItems.size() > 0) {
            sub = menu.addSubMenu(100,
                    getResources().getString(R.string.serie_switch));
            for (Item i : listItems) {
                String tempurl = SimpleRestClient.root_url + "/api/subitem/"
                        + i.pk + "/";
                if (subItemUrl.equalsIgnoreCase(tempurl)) {
                    sub.addItem(i.pk, i.title, true, true);
                } else {
                    sub.addItem(i.pk, i.title, true, false);
                }
            }
        }

        sub = menu.addSubMenu(0,
                getResources().getString(R.string.vod_player_quality_setting));
        if (urls[0] != null)
            sub.addItem(1,
                    getResources().getString(R.string.vod_player_quality_medium));
        if (urls[1] != null)
            sub.addItem(2,
                    getResources().getString(R.string.vod_player_quality_high));
        if (urls[2] != null)
            sub.addItem(3,
                    getResources().getString(R.string.vod_player_quality_ultra));
        menu.addItem(20, getResources().getString(R.string.kefucentertitle));
        menu.addItem(30, getResources().getString(R.string.playfromstarttitle));

        return true;
    }

    public boolean onVodMenuOpened(ISTVVodMenu menu) {
        for (int i = 0; i < 4; i++) {
            ISTVVodMenuItem item;
            item = menu.findItem(i + 1);
            if (item == null)
                continue;
            if (urls[i] == null) {
                item.disable();
            } else {
                item.enable();
            }
            if (i == currQuality) {
                item.select();
                item.disable();
            } else {
                item.unselect();
            }
        }
        if (panelShow) {
            hidePanel();
        }

        return true;
    }

    class SeekBarChangeEvent implements SeekBar.OnSeekBarChangeListener {

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) progress_time.getLayoutParams();

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            if (!live_video) {
                if (mVideoView.getDuration() > 0) {
                    timeBar.setProgress(progress);
                }
                updataTimeText();
                if (clipLength / 1000 != 0)
                    params.leftMargin = (seekBar.getWidth() - 50) * (progress / 1000) / (clipLength / 1000) - params.width / 2 + getResources().getDimensionPixelOffset(R.dimen.play_seekbar_marginleft) + getResources().getDimensionPixelOffset(R.dimen.seekbar_thumb_w) / 2;
                progress_time.setLayoutParams(params);
                progress_time.setText(getTimeString(progress));
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            Log.d(TAG, "onStartTrackingTouch" + seekBar.getProgress());
            if (!live_video) {
                isSeek = true;
                isBuffer = true;
                showPanel();
                showBuffer();
                params.leftMargin = seekBar.getWidth() * seekBar.getProgress() / clipLength - params.width / 2 + getResources().getDimensionPixelOffset(R.dimen.play_seekbar_marginleft);
                progress_time.setLayoutParams(params);
                progress_time.setVisibility(View.VISIBLE);
                hidePanelHandler.removeCallbacks(hidePanelRunnable);
            }

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            Log.d(TAG, "onStopTrackingTouch" + seekBar.getProgress());
            if (!live_video) {
                mVideoView.seekTo(seekBar.getProgress());
                isBuffer = false;
                isSeekBuffer = true;
                isSeek = false;
                offsets = 0;
                offn = 1;
                hideBuffer();
                progress_time.setVisibility(View.INVISIBLE);
                hidePanelHandler.postDelayed(hidePanelRunnable, 3000);
            }

        }
    }

    private void createWindow() {
        View win;
        ViewGroup root = (ViewGroup) findViewById(Window.ID_ANDROID_CONTENT);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        win = inflater.inflate(R.layout.menu, null);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        win.setLayoutParams(lp);
        root.addView(win);
    }

    /**
     * @param menu
     * @param id
     * @return
     */
    public boolean onVodMenuClicked(ISTVVodMenu menu, int id) {
        if (id > 0 && id < 5) {
            int pos = id - 1;
            if (urls[pos] != null && currQuality != pos) {
                try {
                    timeTaskPause();
                    checkTaskPause();
                    paused = false;
                    playPauseImage
                            .setImageResource(R.drawable.play);
                    isBuffer = true;
                    showBuffer();
                    currQuality = pos;
                    // mVideoView = (IsmatvVideoView)
                    // findViewById(R.id.video_view);
                    paths = new String[1];
                    paths[0] = urls[currQuality];
                    tempOffset = seekPostion;
                    mVideoView.setVideoPaths(paths);
                    // mVideoView.setVideoPath(urls[currQuality]);
                    historyManager.addOrUpdateQuality(new Quality(0,
                            urls[currQuality], currQuality));
                    if (item.pk != item.item_pk)
                        callaPlay.videoSwitchStream(item.item_pk, item.pk,
                                item.title, clip.pk, currQuality, "manual",
                                null, null, mediaip, sid, "bestv");
                    else
                        callaPlay.videoSwitchStream(item.pk, null, item.title,
                                clip.pk, currQuality, "manual", null, null,
                                mediaip, sid, "bestv");
                    initQualtiyText();
                    return true;
                } catch (Exception e) {
                    Log.d(TAG, "Exception change url " + e);
                    return false;
                }
            }
            return true;
        }

        // 客服按钮
        if (id == 20) {
            startSakura();
            return true;
        }
        // 从头播放
        if (id == 30) {
            currPosition = 0;
            mVideoView.seekTo(currPosition);
            showBuffer();
            return true;
        }

        if (id > 100) {
            try {
                if (item != null)
                    callaPlay
                            .videoExit(
                                    item.item_pk,
                                    item.pk,
                                    item.title,
                                    clip.pk,
                                    currQuality,
                                    0,
                                    "end",
                                    currPosition,
                                    (System.currentTimeMillis() - startDuration),
                                    item.slug, sid, "list",
                                    item.content_model, "bestv");// String
                    // section,String
                    // sid,String
                    // source,String
                    // channel
                else
                    callaPlay
                            .videoExit(
                                    item.pk,
                                    null,
                                    item.title,
                                    clip.pk,
                                    currQuality,
                                    0,
                                    "end",
                                    currPosition,
                                    (System.currentTimeMillis() - startDuration),
                                    item.slug, sid, "list",
                                    item.content_model, "bestv");
            } catch (Exception e) {
                Log.e(TAG, " log Sender videoExit end " + e.toString());
            }
            subItemUrl = SimpleRestClient.root_url + "/api/subitem/" + id + "/";
            bundle.remove("url");
            bundle.putString("url", subItemUrl);
            addHistory(0);
            if (mVideoView != null) {
                mVideoView.setAlpha(0);
            }
            for (Item i : listItems) {
                if (i.pk == id) {
                    item = i;
                    break;
                }
            }
            checkContinueOrPay(item.pk);
        }

        return true;
    }

    public void changeQualtiy(int pos) {
        if (urls[pos] != null && currQuality != pos) {
            try {
                timeTaskPause();
                checkTaskPause();
                paused = false;
                playPauseImage
                        .setImageResource(R.drawable.play);
                isBuffer = true;
                showBuffer();
                currQuality = pos;
                // mVideoView = (IsmatvVideoView)
                // findViewById(R.id.video_view);
                paths = new String[1];
                paths[0] = urls[currQuality];
                tempOffset = seekPostion;
                mVideoView.setVideoPaths(paths);
                // mVideoView.setVideoPath(urls[currQuality]);
                historyManager.addOrUpdateQuality(new Quality(0,
                        urls[currQuality], currQuality));
                if (item.pk != item.item_pk)
                    callaPlay.videoSwitchStream(item.item_pk, item.pk,
                            item.title, clip.pk, currQuality, "manual",
                            null, null, mediaip, sid, "bestv");
                else
                    callaPlay.videoSwitchStream(item.pk, null, item.title,
                            clip.pk, currQuality, "manual", null, null,
                            mediaip, sid, "bestv");
                initQualtiyText();
            } catch (Exception e) {
                Log.d(TAG, "Exception change url " + e);
            }
        }
    }

    public void changeItem(int id) {
        try {
            if (item != null)
                callaPlay
                        .videoExit(
                                item.item_pk,
                                item.pk,
                                item.title,
                                clip.pk,
                                currQuality,
                                0,
                                "end",
                                currPosition,
                                (System.currentTimeMillis() - startDuration),
                                item.slug, sid, "list",
                                item.content_model, "bestv");// String
                // section,String
                // sid,String
                // source,String
                // channel
            else
                callaPlay
                        .videoExit(
                                item.pk,
                                null,
                                item.title,
                                clip.pk,
                                currQuality,
                                0,
                                "end",
                                currPosition,
                                (System.currentTimeMillis() - startDuration),
                                item.slug, sid, "list",
                                item.content_model, "bestv");
        } catch (Exception e) {
            Log.e(TAG, " log Sender videoExit end " + e.toString());
        }
        subItemUrl = SimpleRestClient.root_url + "/api/subitem/" + id + "/";
        bundle.remove("url");
        bundle.putString("url", subItemUrl);
        addHistory(0);
        if (mVideoView != null) {
            mVideoView.setAlpha(0);
        }
        for (Item i : listItems) {
            if (i.pk == id) {
                item = i;
                break;
            }
        }
        checkContinueOrPay(item.pk);

    }

    private void updataTimeText() {
        String text = getTimeString(currPosition);
        timeText.setText(text);
        endTimetext.setText(getTimeString(clipLength));
    }

    private void initQualtiyText() {

        switch (currQuality) {
            case 0:
                qualityText.setText("流畅");
                //qualityText.setBackgroundResource(R.drawable.vodplayer_stream_normal);
                break;
            case 1:
                qualityText.setText("高清");
                //qualityText.setBackgroundResource(R.drawable.vodplayer_stream_high);
                break;
            case 2:
                qualityText.setText("超清");
                //qualityText.setBackgroundResource(R.drawable.vodplayer_stream_ultra);
                break;

            case 3:
                qualityText.setText("自适应");
                //qualityText.setBackgroundResource(R.drawable.rounded_edittext);
                break;

            default:
                qualityText.setText("流畅");
                //qualityText.setBackgroundResource(R.drawable.vodplayer_stream_normal);
                break;
        }

    }

    @Override
    protected void onPause() {
        if (!isPaymentdialogShow)
            needOnresume = true;
        if (adAsyncTask != null && !adAsyncTask.isCancelled()) {
            adAsyncTask.cancel(true);
        }
        if (isneedpause) {
            try {
                if (!isadvideoplaying) {
                    createHistory(seekPostion);
                    addHistory(seekPostion);
                }
                checkTaskPause();
                timeTaskPause();
                removeAllHandler();
                mVideoView.stopPlayback();
            } catch (Exception e) {
                Log.d(TAG, "Player close to Home");
            }
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        menu = null;
        urlInfo = null;
        if (mCheckHandler != null)
            mCheckHandler.removeCallbacksAndMessages(null);
        mCheckHandler = null;
        historyManager = null;
        simpleRestClient = null;
        DaisyUtils.getVodApplication(this).removeActivtyFromPool(
                this.toString());
        sendPlayComplete();
        unregisterReceiver(saveScreenbroad);
        super.onDestroy();
    }

    private void removeAllHandler() {
        mHandler.removeMessages(BUFFER_COUNTDOWN_ACTION);
        mHandler.removeCallbacks(mUpdateTimeTask);
        mHandler.removeCallbacks(finishPlayerActivity);
        hideMenuHandler.removeCallbacks(hideMenuRunnable);
        mHandler.removeCallbacks(mUpdateTimeTask);
        mCheckHandler.removeCallbacks(checkStatus);
        hidePanelHandler.removeCallbacks(hidePanelRunnable);
        mHandler.removeCallbacksAndMessages(null);
    }

    private void sendPlayComplete() {
    }

    private PaymentDialog.OrderResultListener ordercheckListener = new PaymentDialog.OrderResultListener() {

        @Override
        public void payResult(boolean result) {
            isPaymentdialogShow = false;
            if (item.item_pk != item.pk) {// 剧集且未付费
                if (result) {
                    isBuffer = true;
                    seekPostion = 0;
                    currPosition = 0;
                    tempOffset = 0;
                    showBuffer();
                    payedItemspk.add(item.pk);
                    new FetchClipTask().execute();
                } else {
                    PlayerActivity.this.finish();
                }
            } else {
                if (result) { // 单集,预告片或着历史记录,收藏进入
                    if (mHistory != null) {
                        mHistory.last_position = item.preview.length * 1000;
                    } else {
                        tempOffset = item.preview.length * 1000;
                    }
                    paystatus = true;
                    item.ispayed = true;
                    item.isPreview = false;
                    isPreview = false;
                    new FetchClipTask().execute();
                } else {
                    if ("lockscreen".equals(item.fromPage))
                        setResult(1010);
                    PlayerActivity.this.finish();
                }
            }
        }
    };

    private OnHoverListener onhoverlistener = new OnHoverListener() {

        @Override
        public boolean onHover(View v, MotionEvent event) {
            int what = event.getAction();
            switch (what) {
                case MotionEvent.ACTION_HOVER_MOVE:
                    if (!isadvideoplaying)
                        showPanel();
                    break;
            }
            return false;
        }

    };

    boolean needOnresume = false;

    private void startSakura() {
        needOnresume = true;
        Intent intent = new Intent();
        intent.setAction("cn.ismar.sakura.launcher");
        startActivity(intent);
    }

    private void orderCheck() {
        SimpleRestClient client = new SimpleRestClient();
        String typePara;
        if (item.item_pk != item.pk) {
            typePara = "&item=" + item.item_pk;
        } else {
            typePara = "&item=" + item.pk;
        }
        client.doSendRequest(ORDER_CHECK_BASE_URL, "post", "device_token="
                + SimpleRestClient.device_token + "&access_token="
                + SimpleRestClient.access_token + typePara, orderCheck);
    }

    private HttpPostRequestInterface orderCheck = new HttpPostRequestInterface() {

        @Override
        public void onPrepare() {
        }

        @Override
        public void onSuccess(String info) {
            if (info != null && "0".equals(info)) {
                if (listItems == null
                        || (listItems != null && listItems.size() == 0)) {
                    PaymentDialog dialog = new PaymentDialog(
                            PlayerActivity.this, R.style.PaymentDialog,
                            ordercheckListener);
                    item.model_name = "item";
                    dialog.setItem(item);
                    dialog.show();
                }
            } else {
                isPreview = false;
                try {
                    JSONArray array = new JSONArray(info);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject seria = array.getJSONObject(i);
                        int pk = seria.getInt("wares_id");
                        payedItemspk.add(pk);
                    }
                } catch (JSONException e) {
                    if (item.item_pk != item.pk) {// 电视剧购买结果
                        for (Item i : listItems) {
                            payedItemspk.add(i.pk);
                        }
                    } else { // 单片购买结果
                        item.ispayed = true;
                    }
                }
                initPlayer();
            }
        }

        @Override
        public void onFailed(String error) {

        }
    };

    public void creatPopWindows() {
        View pop = View.inflate(this, R.layout.quality_pop_item, null);
        popupWindow = new PopupWindow(pop, getResources().getDimensionPixelSize(R.dimen.quality_pop_width), getResources().getDimensionPixelSize(R.dimen.quality_pop_height));
        //popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.quality_pop));
        popupWindow.setBackgroundDrawable(getResources().getDrawable(R.color._202020));
        popupWindow.getBackground().setAlpha(102);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        LinearLayout popMenu = (LinearLayout) pop.findViewById(R.id.pop);
        for (int i = 0; i < urls.length; i++) {
            if (urls[i] != null) {
                View view = View.inflate(this, R.layout.pop_menu_item, null);
                TextView textView = (TextView) view.findViewById(R.id.quality_text);
                ImageView img = (ImageView) view.findViewById(R.id.quality_focus);
                switch (i) {
                    case 0:
                        textView.setText("流畅");
                        break;
                    case 1:
                        textView.setText("高清");
                        break;
                    case 2:
                        textView.setText("超清");
                        break;
                    default:
                        textView.setText("自适应");
                }
                if (i == currQuality) {
                    textView.setTextColor(getResources().getColor(R.color._ff9c3c));
                    img.setBackgroundResource(R.drawable.quality_chosed);
                }
                final int j = i;
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        changeQualtiy(j);
                        popupWindow.dismiss();
                    }
                });
                popMenu.addView(view);
            }
        }
        int[] location = new int[2];
        pop.getLocationOnScreen(location);
        Log.i("Height", panelLayout.getHeight() + "");

        popupWindow.showAtLocation(panelLayout, Gravity.NO_GRAVITY, location[0] + 2325, location[1] + 985);
    }

    View.OnClickListener listener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.QualityText:
                    creatPopWindows();
                    if (!isAnthology) {
                        isAnthology = true;
                        if (!panelShow)
                            showPanel();
                        hidePanelHandler.removeCallbacks(hidePanelRunnable);
                    } else {
                        isAnthology = false;
                        hidePanelHandler.postDelayed(hidePanelRunnable, 3000);
                    }
                    break;
                case R.id.anthology:
                    if (listItems.size() != 0) {
                        if (!isAnthology) {
                            isAnthology = true;
                            if (!panelShow)
                                showPanel();
//						initEntertainmentPop();
                            anthology.setTextColor(getResources().getColor(R.color._ff9c3c));
                            getSupportFragmentManager().beginTransaction().show(mEpisodeFragment).commit();
                            hidePanelHandler.removeCallbacks(hidePanelRunnable);
                        } else {
                            isAnthology = false;
                            anthology.setTextColor(getResources().getColor(R.color._e4e4e4));
                            getSupportFragmentManager().beginTransaction().hide(mEpisodeFragment).commit();
                            hidePanelHandler.postDelayed(hidePanelRunnable, 3000);
                        }
                    }
                    break;
                case R.id.player_back:
                    finish();
                    break;
            }
        }
    };

    public void fetchItemInfo(String itemId) {

        Retrofit retrofit = new Retrofit.Builder()
                .client(HttpManager.getInstance().mClient)
                .baseUrl(appendProtocol(SimpleRestClient.root_url))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        retrofit.create(HttpAPI.ApiItem.class).doRequest(itemId).enqueue(new retrofit2.Callback<ItemEntity>() {
            @Override
            public void onResponse(retrofit2.Response<ItemEntity> response) {
                if (response.body() != null) {
                    ItemEntity itemEntity = response.body();
                    if (itemEntity.getSubitems() != null && itemEntity.getSubitems().length != 0) {
                        switch (itemEntity.getContentModel()) {
                            case "teleplay":
                                mEpisodeFragment.setData(itemEntity, EpisodeFragment.Type.Teleplay);
                                break;
                            case "variety":
                            case "entertainment":
                                mEpisodeFragment.setData(itemEntity, EpisodeFragment.Type.Entertainment);
                                break;
                        }
                        mEpisodeFragment.setCurrentItem(item.position);
                    }
                }
            }

            @Override
            public void onFailure(Throwable t) {

            }
        });
    }

    private String appendProtocol(String host) {
        Uri uri = Uri.parse(host);
        String url = uri.toString();
        if (!uri.toString().startsWith("http://") && !uri.toString().startsWith("https://")) {
            url = "http://" + host;
        }

        if (!url.endsWith("/")) {
            url = url + "/";
        }
        return url;
    }

    @Override
    public void onEpisodeItemSelected(ItemEntity.SubItem subItem) {
        try {
            hidePanel();
            anthology.setTextColor(getResources().getColor(R.color._e4e4e4));
            if (item != null) {
                callaPlay.videoExit(
                        item.item_pk,
                        item.pk,
                        item.title,
                        clip.pk,
                        currQuality,
                        0,
                        "end",
                        currPosition,
                        (System.currentTimeMillis() - startDuration),
                        item.slug, sid, "list",
                        item.content_model, "bestv");
            } else {
                callaPlay.videoExit(
                        item.pk,
                        null,
                        item.title,
                        clip.pk,
                        currQuality,
                        0,
                        "end",
                        currPosition,
                        (System.currentTimeMillis() - startDuration),
                        item.slug, sid, "list",
                        item.content_model, "bestv");
            }
        } catch (Exception e) {
            Log.e(TAG, " log Sender videoExit end " + e.toString());
        }
        subItemUrl = SimpleRestClient.root_url + "/api/subitem/" + subItem.getPk() + "/";
        bundle.remove("url");
        bundle.putString("url", subItemUrl);
        addHistory(0);
        if (mVideoView != null) {
            mVideoView.setAlpha(0);
        }
        for (Item i : listItems) {
            if (i.pk == subItem.getPk()) {
                item = i;
                break;
            }
        }
        checkContinueOrPay(item.pk);
    }

    //滑动改变音量
    private void changeVolumn(float movePercent) {
        if (bright.getVisibility() == View.VISIBLE || play_progress.getVisibility() == View.VISIBLE || isadvideoplaying) {
            return;
        }
        volumn.setVisibility(View.VISIBLE);
        float newVolumn = currentVolumn + movePercent * 100;
        if (newVolumn > 100) {
            newVolumn = 100;
        } else if (newVolumn < 0) {
            newVolumn = 0;
        }
        setVolumn((int) newVolumn);
    }

    //滑动改变亮度
    private void changeBright(float movePercent) {
        if (volumn.getVisibility() == View.VISIBLE || play_progress.getVisibility() == View.VISIBLE || isadvideoplaying) {
            return;
        }
        bright.setVisibility(View.VISIBLE);
        float newBright = currentBright + movePercent * 100;
        if (newBright > 100) {
            newBright = 100;
        } else if (newBright < 0) {
            newBright = 0;
        }
        setBright((int) newBright);
    }

    private void changePlayProgress(float movePercent) {
        if (volumn.getVisibility() == View.VISIBLE || bright.getVisibility() == View.VISIBLE || isadvideoplaying) {
            return;
        }
        if (movePercent > 0) {
            play_progress.setBackgroundResource(R.drawable.moveon);
        } else {
            play_progress.setBackgroundResource(R.drawable.moveback);
        }
        play_progress.setVisibility(View.VISIBLE);
        progress_play.setProgress((int) (currPosition + movePercent * 1000));
        current_play_progress.setText(getTimeString((int) (currPosition + movePercent * 1000) < 0 ? 0 : (int) (currPosition + movePercent * 1000)) + "/" + getTimeString(clipLength));
        mVideoView.seekTo((int) (currPosition + movePercent * 1000));
        timeBar.setProgress(currPosition);
        currPosition = mVideoView.getCurrentPosition();
    }

    public int getVolumn() {
        return mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    public void setVolumn(int volumn) {
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volumn, 0);
        currentVolumn = volumn;
        progress_volumn.setProgress(volumn);
    }


    public void setBright(int bright) {
        currentBright = bright;
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.screenBrightness = bright / 100f;
        window.setAttributes(params);
        progress_bright.setProgress(bright);
    }

    public int getsystemBright() {
        int brightness = 0;
        try {
            /**
             * 返回的亮度值是处于0-255之间的整型数值
             */
            brightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return brightness * 100 / 255;
    }


    class GetItemTask extends AsyncTask<String, Void, Void> {

        int id = 0;
        String url = null;

        @Override
        protected void onPostExecute(Void result) {
            initClipInfo();
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                SimpleRestClient mSimpleRestClient = new SimpleRestClient();
                url = params[0];
                id = SimpleRestClient.getItemId(url, new boolean[1]);
                Item mItem = mSimpleRestClient.getItem(url);
                mItem.isPreview = true;
                getIntent().putExtra("item", mItem);
                urlInfo = AccessProxy.parse(SimpleRestClient.root_url
                                + "/api/clip/" + mItem.preview.pk + "/", VodUserAgent
                                .getAccessToken(SimpleRestClient.sn_token),
                        PlayerActivity.this);
            } catch (ItemOfflineException e) {
                HashMap<String, Object> exceptionProperties = new HashMap<String, Object>();
                exceptionProperties.put(EventProperty.CODE, "nodetail");
                exceptionProperties.put(EventProperty.CONTENT,
                        "no detail error : " + e.getUrl());
                exceptionProperties.put(EventProperty.ITEM, id);
                NetworkUtils.SaveLogToLocal(NetworkUtils.DETAIL_EXCEPT,
                        exceptionProperties);
                e.printStackTrace();
            } catch (JsonSyntaxException e) {
                HashMap<String, Object> exceptionProperties = new HashMap<String, Object>();
                exceptionProperties.put(EventProperty.CODE, "parsejsonerror");
                exceptionProperties.put(EventProperty.CONTENT, e.getMessage()
                        + " : " + url);
                exceptionProperties.put(EventProperty.ITEM, id);
                NetworkUtils.SaveLogToLocal(NetworkUtils.DETAIL_EXCEPT,
                        exceptionProperties);
                e.printStackTrace();
            } catch (NetworkException e) {
                HashMap<String, Object> exceptionProperties = new HashMap<String, Object>();
                exceptionProperties.put(EventProperty.CODE, "networkconnerror");
                exceptionProperties.put(EventProperty.CONTENT, e.getMessage()
                        + " : " + e.getUrl());
                exceptionProperties.put(EventProperty.ITEM, id);
                NetworkUtils.SaveLogToLocal(NetworkUtils.DETAIL_EXCEPT,
                        exceptionProperties);
                e.printStackTrace();
            }
            return null;
        }
    }

    private void acquireWakeLock() {

        if (mWakelock == null) {

            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

            mWakelock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, this.getClass().getCanonicalName());

            mWakelock.acquire();

        }
    }

    private void releaseWakeLock() {

        if (mWakelock != null && mWakelock.isHeld()) {

            mWakelock.release();

            mWakelock = null;

        }
    }
}