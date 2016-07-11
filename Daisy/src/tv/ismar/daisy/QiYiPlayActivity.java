package tv.ismar.daisy;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.provider.Settings;
import android.widget.*;
import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;
import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.SimpleRestClient.HttpPostRequestInterface;
import tv.ismar.daisy.core.VodUserAgent;
import tv.ismar.daisy.core.client.HttpAPI;
import tv.ismar.daisy.core.client.HttpManager;
import tv.ismar.daisy.core.preferences.AccountSharedPrefs;
import tv.ismar.daisy.data.http.ItemEntity;
import tv.ismar.daisy.models.Clip;
import tv.ismar.daisy.models.Favorite;
import tv.ismar.daisy.models.History;
import tv.ismar.daisy.models.Item;
import tv.ismar.daisy.persistence.FavoriteManager;
import tv.ismar.daisy.persistence.HistoryManager;
import tv.ismar.daisy.player.CallaPlay;
import tv.ismar.daisy.player.ISTVVodMenu;
import tv.ismar.daisy.player.ISTVVodMenuItem;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.GestureDetector;
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
import cn.ismartv.activator.utils.MD5Utils;
import tv.ismar.daisy.ui.fragment.EpisodeFragment;

import com.ismartv.api.t.AccessProxy;
import com.ismartv.bean.ClipInfo;
import com.qiyi.sdk.player.BitStream;
import com.qiyi.sdk.player.IMedia;
import com.qiyi.sdk.player.IMediaPlayer;
import com.qiyi.sdk.player.IMediaPlayer.OnBitStreamInfoListener;
import com.qiyi.sdk.player.IMediaPlayer.OnBufferChangedListener;
import com.qiyi.sdk.player.IMediaPlayer.OnHeaderTailerInfoListener;
import com.qiyi.sdk.player.IMediaPlayer.OnPreviewInfoListener;
import com.qiyi.sdk.player.IMediaPlayer.OnSeekCompleteListener;
import com.qiyi.sdk.player.IMediaPlayer.OnStateChangedListener;
import com.qiyi.sdk.player.IMediaPlayer.OnVideoSizeChangedListener;
import com.qiyi.sdk.player.ISdkError;
import com.qiyi.sdk.player.IVideoOverlay;
import com.qiyi.sdk.player.Parameter;
import com.qiyi.sdk.player.PlayerSdk;
import com.qiyi.sdk.player.PlayerSdk.OnInitializedListener;
import com.qiyi.sdk.player.SdkVideo;

public class QiYiPlayActivity extends VodMenuAction implements EpisodeFragment.OnItemSelectedListener {
    private static final int MSG_AD_COUNTDOWN = 100;
    private static final int MSG_PLAY_TIME = 101;
    private static final int MSG_INITQUALITYTITLE = 102;
    static final int BUFFER_COUNTDOWN_ACTION = 113;
    private static final int SEEK_STEP = 30000;
    private static final int SHORT_STEP = 1;
    private static final HashMap<BitStream, String> DEFINITION_NAMES;
    private IMediaPlayer mPlayer;
    private static final String TAG = "PLAYER";
    private static final String BUFFERCONTINUE = " 上次放映：";
    private static final String PlAYSTART = " 即将放映：";
    private static final String BUFFERING = " 正在加载 ";
    private static final String EXTOCLOSE = " 网络数据异常，即将退出播放器";
    private static final int DIALOG_OK_CANCEL = 0;

    private boolean paused = false;
    private boolean isBuffer = true;
    private boolean isSeekBuffer = false;
    private boolean panelShow = false;
    private int currQuality = 0;// 0: normal 1: 720P 2:1080P
    private boolean mIsPreview;
    private Animation panelShowAnimation;
    private Animation panelHideAnimation;
    private Animation top_to_down,top_to_up;
    private BitStream currentDefinition;
    // private Animation bufferHideAnimation;
    private LinearLayout panelLayout,bufferLayout;
    private ImageView logoImage;
    private RelativeLayout top_panelayout;
    private tv.ismar.daisy.views.MarqueeView titleText;
    private TextView qualityText;
    private TextView timeText,endtime;
    private ImageView playPauseImage;
    private ImageView ffImage;
    private ImageView fbImage;
    private int clipLength = 0;
    private int currPosition = 0;
    private boolean isContinue = true;
    private ISTVVodMenu menu = null;
    private ClipInfo urlInfo = new ClipInfo();
    private Handler mCheckHandler = new Handler();
    private int tempOffset = 0;
    private Item item;
    private Item subItem;
    private Clip clip;
    private PopupWindow popupWindow;
    private Bundle bundle;
    private SeekBar timeBar;
    private Dialog popupDlg = null;
    private InputStream logoInputStream;
    private HistoryManager historyManager;
    private History mHistory;
    private SimpleRestClient simpleRestClient;
    private String itemUrl;
    private String subItemUrl;
    private int seekPostion = 0;
    private boolean isSeek = false;
    private FavoriteManager favoriteManager;
    private List<Item> listItems;
    private Favorite favorite;
    private int currNum = 0;
    private int offsets = 0;
    private int offn = 1;
    private TextView bufferText;
    private long bufferDuration = 0;
    private long startDuration = 0;
    private CallaPlay callaPlay = new CallaPlay();
    AudioManager am;
    private String mSection;
    private String sid = "";
    private String mediaip;
    private int mCurrentSeed = 0;
    private RelativeLayout mRootLayout;
    private boolean isHideControlPanel = true;
    private GestureDetector mGestureDetector; // 手势监测器
    private boolean live_video = false;
    private List<BitStream> mBitStreamList = new ArrayList<BitStream>();
    private boolean isfinish = false;

    private boolean[] avalibleRate = {false, false, false};
    private AccountSharedPrefs shardpref;
    private ImageView gesture_tipview;
    private boolean isneedpause = true;
    private boolean mQiyiSdkInitialized;
    private FrameLayout frameContainer;
    private IVideoOverlay mVideoOverlay;
    private TextView mTxtAdTimer,anthology;
    private static final int[] SEEK_STEPS = {5000,      10000,      30000,      60000,      300000,     600000};
    private int mSeekStepIndex;
    private  boolean is_vip;
    private int previewLength;
    static {
        DEFINITION_NAMES = new HashMap<BitStream, String>();
        DEFINITION_NAMES.put(BitStream.BITSTREAM_HIGH, "高清");

        DEFINITION_NAMES.put(BitStream.BITSTREAM_720P, "720P");
        DEFINITION_NAMES.put(BitStream.BITSTREAM_720P_DOLBY, "杜比720P");
        DEFINITION_NAMES.put(BitStream.BITSTREAM_720P_H265, "H265_720P");

        DEFINITION_NAMES.put(BitStream.BITSTREAM_1080P, "1080P");
        DEFINITION_NAMES.put(BitStream.BITSTREAM_1080P_DOLBY, "杜比1080P");
        DEFINITION_NAMES.put(BitStream.BITSTREAM_1080P_H265, "H265_1080P");

        DEFINITION_NAMES.put(BitStream.BITSTREAM_4K, "4K");
        DEFINITION_NAMES.put(BitStream.BITSTREAM_4K_DOLBY, "杜比4K");
        DEFINITION_NAMES.put(BitStream.BITSTREAM_4K_H265, "H265_4K");
    }

    private EpisodeFragment mEpisodeFragment;
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
    private String urls[] = new String[6];
    private boolean isAdPlaying;
    private boolean isAnthologyShow=false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        winParams.flags |= (WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        activityTag = "BaseActivity";
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        shardpref = AccountSharedPrefs.getInstance();
        setContentView(R.layout.vod_player);

        mEpisodeFragment = new EpisodeFragment();
        FragmentTransaction fragmentTransaction =  getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.episode_layout, mEpisodeFragment);
        fragmentTransaction.hide(mEpisodeFragment);
        fragmentTransaction.commitAllowingStateLoss();
        mEpisodeFragment.setOnItemSelectedListener(this);

    }

    public void initView() {
        panelShowAnimation = AnimationUtils.loadAnimation(this,
                R.anim.fly_up);
        panelHideAnimation = AnimationUtils.loadAnimation(this,
                R.anim.fly_down);
        top_to_down=AnimationUtils.loadAnimation(this,R.anim.top_fly_down);
        top_to_up=AnimationUtils.loadAnimation(this,R.anim.top_fly_up);
        // bufferHideAnimation =
        // AnimationUtils.loadAnimation(this,R.drawable.fade_out);
        top_panelayout= (RelativeLayout) findViewById(R.id.top_panelayout);
        panelLayout = (LinearLayout) findViewById(R.id.PanelLayout);
        player_back = (ImageView) findViewById(R.id.player_back);
        player_back.setOnClickListener(listener);
        progress_time = (TextView) findViewById(R.id.progress_time);
        play_progress = findViewById(R.id.play_progress);
        current_play_progress = (TextView) findViewById(R.id.current_play_progress);
        progress_play = (ProgressBar) findViewById(R.id.progress_play);
        titleText = (tv.ismar.daisy.views.MarqueeView) findViewById(R.id.TitleText);
        qualityText = (TextView) findViewById(R.id.QualityText);
        anthology= (TextView) findViewById(R.id.anthology);
        anthology.setOnClickListener(listener);
        qualityText.setOnClickListener(listener);
        timeText = (TextView) findViewById(R.id.TimeText);
        endtime= (TextView) findViewById(R.id.endTimeText);
        timeBar = (SeekBar) findViewById(R.id.TimeSeekBar);
        timeBar.setOnSeekBarChangeListener(new SeekBarChangeEvent());
        playPauseImage = (ImageView) findViewById(R.id.PlayPauseImage);
        ffImage = (ImageView) findViewById(R.id.FFImage);
       // fbImage = (ImageView) findViewById(R.id.FBImage);
        bufferLayout = (LinearLayout) findViewById(R.id.BufferLayout);
        bufferText = (TextView) findViewById(R.id.BufferText);
        logoImage = (ImageView) findViewById(R.id.logo_image);
        gesture_tipview = (ImageView)findViewById(R.id.gesture_tipview);
        mTxtAdTimer = (TextView) findViewById(R.id.ad_count_view);
        panelLayout.setVisibility(View.GONE);
        bufferLayout.setVisibility(View.GONE);
        qualityText.setVisibility(View.GONE);
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mRootLayout = (RelativeLayout) findViewById(R.id.RootRelativeLayout);
        bright = findViewById(R.id.bright);
        progress_bright = (ProgressBar) findViewById(R.id.progress_bright);
        volumn = findViewById(R.id.volumn);
        progress_volumn = (ProgressBar) findViewById(R.id.progress_volumn);
        playPauseImage.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent keycode) {
                // TODO Auto-generated method stub
                switch (keycode.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (!paused) {
                            pauseItem();
                            playPauseImage
                                    .setImageResource(R.drawable.paus);
                        } else {
                            resumeItem();
                            playPauseImage
                                    .setImageResource(R.drawable.play);
                        }

                        break;

                    default:
                        break;
                }
                return false;
            }
        });
//        fbImage.setOnTouchListener(new OnTouchListener() {
//
//            @Override
//            public boolean onTouch(View v, MotionEvent keycode) {
//                // TODO Auto-generated method stub
//                switch (keycode.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
//                        if (clipLength > 0 && !live_video) {
//                            isSeek = true;
//                            showPanel();
//                            isBuffer = true;
//                            showBuffer();
//                            int currentPosition = mPlayer.getCurrentPosition();
//                            mPlayer.seekTo(currentPosition + SEEK_STEPS[mSeekStepIndex]);
//                            isSeekBuffer = true;
//                            Log.d(TAG, "LEFT seek to "
//                                    + getTimeString(currPosition));
//                            isSeek = false;
//                            offsets = 0;
//                            offn = 1;
//                        }
//                        break;
//
//                    default:
//                        break;
//                }
//                return false;
//            }
//        });
        ffImage.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent keycode) {
                // TODO Auto-generated method stub
                switch (keycode.getAction()) {
                    case MotionEvent.ACTION_DOWN:
//                        if (clipLength > 0 && !live_video) {
//                            isSeek = true;
//                            showPanel();
//                            isBuffer = true;
//                            showBuffer();
//                            int currentPosition = mPlayer.getCurrentPosition();
//                            mPlayer.seekTo(currentPosition - SEEK_STEPS[mSeekStepIndex]);
//                            mPlayer.seekTo(SEEK_STEP);
//                            isSeekBuffer = true;
//                            Log.d(TAG, "RIGHT seek to"
//                                    + getTimeString(currPosition));
//                            isSeek = false;
//                            offsets = 0;
//                            offn = 1;
 //                       }
                        if(listItems.size()>0){
                            gotoFinishPage();
                        }
                        break;

                    default:
                        break;
                }
                return false;
            }
        });
        DaisyUtils.getVodApplication(this).addActivityToPool(this.toString(),
                this);
        bundle = getIntent().getExtras();
        item = (Item) bundle.get("item");
        section = item.section;
        channel = item.channel;
        slug =  item.slug;
        fromPage = item.fromPage;
        clip = item.clip;
        live_video = item.live_video;
        titleText.setText(item.title);
        simpleRestClient = new SimpleRestClient();
//		if("false".equals(shardpref.getSharedPrefs(AccountSharedPrefs.FIRST_USE))){
        new initPlayTask().execute();

        fetchItemInfo(String.valueOf(item.item_pk));
//		}else{
//			gesture_tipview.setVisibility(View.VISIBLE);
//			setGesturebackground(gesture_tipview, R.drawable.play_gesture);
//		}
    }
    float downY=0;
    float downX=0;
    long lastTouchTime=0;
    boolean doubleTouch=true;
    private int preX,upX;
    private int preY,upY;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downY = event.getY();
                downX = event.getX();
                preX = (int) event.getX();
                preY = (int) event.getY();
                long currentTime=System.currentTimeMillis();
//                if (isadvideoplaying)
//                    return false;
                if(doubleTouch==true&&lastTouchTime!=0&&currentTime-lastTouchTime<500){
                    if (!paused) {
                        pauseItem();
                        playPauseImage
                                .setImageResource(R.drawable.paus);
                    } else {
                        resumeItem();
                        playPauseImage
                                .setImageResource(R.drawable.play);
                    }
                    doubleTouch=false;
                }else{
                    if (panelShow) {
                        //				hideMenuHandler.post(hideMenuRunnable);
                        hidePanelHandler.removeCallbacks(hidePanelRunnable);
                        hidePanelHandler.postDelayed(hidePanelRunnable, 3000);
                    } else {
                        showPanel();

                    }
//                    getSupportFragmentManager().beginTransaction().hide(mEpisodeFragment).commit();
                }
                lastTouchTime=currentTime;
                doubleTouch=true;
                break;
            case MotionEvent.ACTION_MOVE:
                float moveY = event.getY() - downY;
                float moveX=event.getX()-downX;
                int halfScreenH=getWindowManager().getDefaultDisplay().getHeight()/2;
                int halfScreenW=getWindowManager().getDefaultDisplay().getWidth()/2;
                float movePercent=-moveY/halfScreenH;
                if(Math.abs(moveY)>100) {
                    if (event.getX() < halfScreenW&&downX<halfScreenW) {
                        if(volumn.getVisibility()==View.INVISIBLE)
                        changeBright(movePercent);
                    } else if(downX>halfScreenW){
                        if(bright.getVisibility()==View.INVISIBLE)
                        changeVolumn(movePercent);
                    }
                    downY = event.getY();
                    downX=event.getX();
                }
                if(Math.abs(moveX)>100){
                    changePlayProgress(moveX);
                    downX=event.getX();
                }
                break;
            case MotionEvent.ACTION_UP:
                bright.setVisibility(View.INVISIBLE);
                volumn.setVisibility(View.INVISIBLE);
                play_progress.setVisibility(View.INVISIBLE);
                break;
        }
        return super.onTouchEvent(event);
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

    private class initPlayTask extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method stub
            initQiyiVideoPlayer();
        }

        @Override
        protected void onCancelled(Void aVoid) {
            super.onCancelled(aVoid);
        }

        @Override
        protected Void doInBackground(String... params) {
            // TODO Auto-generated method stub
            try {
                if (item != null) {
                    if (item.item_pk != item.pk) {
                        currNum = item.position;
                        Log.d(TAG, "currNum ===" + currNum);
                        subItemUrl = SimpleRestClient.root_url
                                + "/api/subitem/" + item.pk + "/";
                        subItem = simpleRestClient.getItem(subItemUrl);
                        itemUrl = SimpleRestClient.root_url + "/api/item/"
                                + item.item_pk + "/";
                        item = simpleRestClient.getItem(itemUrl);
                        item.channel = channel;
                        item.section = section;
                        item.slug = slug;
                        item.fromPage = fromPage;
                        if (item != null && item.subitems != null) {
                            listItems = new ArrayList<Item>();
                            for (int i = 0; i < item.subitems.length; i++) {
                                listItems.add(item.subitems[i]);
                            }
                        }
                    } else {
                        itemUrl = SimpleRestClient.root_url + "/api/item/"
                                + item.item_pk + "/";
                        // Item item1 = simpleRestClient.getItem(itemUrl);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

    }

    // 初始化播放地址url
    private class ItemByUrlTask extends AsyncTask<String, Void, ClipInfo> {

        @Override
        protected void onPostExecute(ClipInfo result) {
            if (result != null) {
                BitStream definition = getDefinityByQuality(currQuality);
                String[] array = urlInfo.getIqiyi_4_0().split(":");
                SdkVideo qiyiInfo = new SdkVideo(array[0],array[1],false);
                currPosition = seekPostion =0;
                timeBar.setProgress(currPosition);
                startPlayMovie(qiyiInfo);
                getIntent().putExtra("item", subItem);
                titleText.setText(subItem.title);
                mEpisodeFragment.setCurrentItem(currNum);
            } else {
                // ExToClosePlayer("url"," m3u8 quality is null ,or get m3u8 err");
            }
        }

        @Override
        protected ClipInfo doInBackground(String... params) {
            Object obj = bundle.get("url");
            Log.d(TAG, "init player bundle url === " + obj);
            AccessProxy.init(VodUserAgent.getModelName(),
                    ""+SimpleRestClient.appVersion, SimpleRestClient.sn_token);
            try {
                if (obj != null) {
                    subItem = simpleRestClient.getItem((String) obj);
                    currNum = subItem.position;
                    if (subItem != null) {
                        subItem.channel = channel;
                        subItem.section = section;
                        subItem.slug = slug;
                        subItem.fromPage = fromPage;
                        clip = subItem.clip;
                        urlInfo = AccessProxy.parse(SimpleRestClient.root_url
                                        + "/api/clip/" + clip.pk + "/",
                                VodUserAgent.getAccessToken(SimpleRestClient.sn_token),
                                QiYiPlayActivity.this);
                        if (urlInfo.getIqiyi_4_0().length() == 0) {
                            Intent intent = new Intent();
                            intent.setAction("tv.ismar.daisy.Play");
                            intent.putExtra("item", subItem);
                            intent.putExtra("ismartv",
                                    AccessProxy.getvVideoClipInfo());
                            startActivity(intent);
                            QiYiPlayActivity.this.finish();
                            return null;
                        } else {
                            bundle.putString("iqiyi", urlInfo.getIqiyi_4_0());
                            getIntent().putExtra("iqiyi", urlInfo.getIqiyi_4_0());
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, e.toString());
                // ExToClosePlayer("url","m3u8 quality is null ,or get m3u8 err /n"+e.toString());
                return null;
            }
            return urlInfo;

        }
    }

    public void initQiyiVideoPlayer() {
        FrameLayout.LayoutParams flParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER);
        frameContainer = (FrameLayout) findViewById(R.id.fl_videoview_container);
        frameContainer.setVisibility(View.VISIBLE);
        frameContainer.setOnHoverListener(onhoverlistener);
        frameContainer.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isVodMenuVisible()) {
                    hideMenuHandler.post(hideMenuRunnable);
                } else {
//                    if (!paused) {
//                        pauseItem();
//                        playPauseImage
//                                .setImageResource(R.drawable.play);
//                    } else {
//                        resumeItem();
//                        playPauseImage
//                                .setImageResource(R.drawable.paus);
//                    }
                }
                return false;
            }
        });
        favoriteManager = DaisyUtils.getFavoriteManager(this);
        historyManager = DaisyUtils.getHistoryManager(this);
        if (SimpleRestClient.isLogin()) {
            favorite = favoriteManager.getFavoriteByUrl(itemUrl, "yes");
            mHistory = historyManager.getHistoryByUrl(itemUrl, "yes");
        } else {
            favorite = favoriteManager.getFavoriteByUrl(itemUrl, "no");
            mHistory = historyManager.getHistoryByUrl(itemUrl, "no");
        }

        if (mHistory != null)
            currQuality = mHistory.last_quality;
        if (mHistory != null
                && (subItemUrl != null && subItemUrl
                .equalsIgnoreCase(mHistory.sub_url))) {
            isContinue = mHistory.is_continue;
            tempOffset = (int) mHistory.last_position;
        }
        else if (mHistory!=null&&mHistory.sub_url == null
                && mHistory.url != null){
            isContinue = mHistory.is_continue;
            tempOffset = (int) mHistory.last_position;
        }
        if(!mQiyiSdkInitialized) {
            Parameter extraParams = new Parameter();
            //debug code
            extraParams.setInitPlayerSdkAfter(0);  //SDK初始化在调用initialize之后delay一定时间开始执行, 单位为毫秒.
            extraParams.setCustomerAppVersion("" + SimpleRestClient.appVersion);      //传入客户App版本号
            extraParams.setDeviceId(SimpleRestClient.sn_token);   //传入deviceId, VIP项目必传, 登录和鉴权使用
            PlayerSdk.getInstance().initialize(this, extraParams,
                    new OnInitializedListener() {

                        @Override
                        public void onSuccess() {
                            mQiyiSdkInitialized = true;
                            doOnSuccess(); // 初始化成功后做
                        }

                        @Override
                        public void onFailed(int what, int extra) {
                            // TODO
                            Toast.makeText(
                                    QiYiPlayActivity.this,
                                    "QiyiSdk init fail: what=" + what + ", extra="
                                            + extra, Toast.LENGTH_LONG).show();
                        }
                    });
        }else{
            doOnSuccess(); // 初始化成功后做
        }
    }

    private boolean isVodMenuVisible() {
        if (menu == null) {
            return false;
        }
        return menu.isVisible();
    }

    protected void showBuffer() {
        if (isBuffer && !bufferLayout.isShown()) {
            bufferLayout.setVisibility(View.VISIBLE);
            bufferDuration = System.currentTimeMillis();
        }
//        mHandler.sendEmptyMessageDelayed(BUFFER_COUNTDOWN_ACTION, 1000);
    }

    protected void hideBuffer() {
//        if (mHandler.hasMessages(BUFFER_COUNTDOWN_ACTION)) {
//            mHandler.removeMessages(BUFFER_COUNTDOWN_ACTION);
//            buffercountDown = 0;
//        }
        if (!isBuffer && bufferLayout.isShown()) {
            bufferText.setText(BUFFERING);
            bufferLayout.setVisibility(View.GONE);
            try {
                if (subItem != null)
                    if (isSeekBuffer) {
                        callaPlay
                                .videoPlaySeekBlockend(
                                        item.pk,
                                        subItem.pk,
                                        subItem.title,
                                        clip.pk,
                                        currQuality,
                                        0,
                                        currPosition,
                                        (System.currentTimeMillis() - bufferDuration),
                                        mediaip, sid,"qiyi");

                    } else {
                        callaPlay
                                .videoPlayBlockend(
                                        item.pk,
                                        subItem.pk,
                                        subItem.title,
                                        clip.pk,
                                        currQuality,
                                        0,
                                        currPosition,
                                        (System.currentTimeMillis() - bufferDuration),
                                        mediaip, sid,"qiyi");
                    }
                else if (isSeekBuffer) {
                    callaPlay
                            .videoPlaySeekBlockend(
                                    item.pk,
                                    null,
                                    item.title,
                                    clip.pk,
                                    currQuality,
                                    0,
                                    currPosition,
                                    (System.currentTimeMillis() - bufferDuration),
                                    mediaip, sid,"qiyi");

                } else {
                    callaPlay
                            .videoPlayBlockend(
                                    item.pk,
                                    null,
                                    item.title,
                                    clip.pk,
                                    currQuality,
                                    0,
                                    currPosition,
                                    (System.currentTimeMillis() - bufferDuration),
                                    mediaip, sid,"qiyi");
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
                case MSG_AD_COUNTDOWN:
                    mTxtAdTimer.setText("倒计时"+String.valueOf(mPlayer.getAdCountDownTime() / 1000));
                    sendEmptyMessageDelayed(MSG_AD_COUNTDOWN, 1000);
                    break;
                case MSG_PLAY_TIME:
                    updataTimeText();
                    break;
                case MSG_INITQUALITYTITLE:
                    initQualtiyText();
                    break;
                case MSG_SEK_ACTION:
                    mPlayer.seekTo(currPosition);
                    isBuffer = true;
                    showBuffer();
                    if (subItem != null)
                        callaPlay.videoPlaySeek(item.pk, subItem.pk, subItem.title,
                                clip.pk, currQuality, 0, currPosition, sid,"qiyi");
                    else
                        callaPlay.videoPlayContinue(item.pk, null, item.title,
                                clip.pk, currQuality, 0, currPosition, sid,"qiyi");
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
                        mHandler.sendEmptyMessageDelayed(BUFFER_COUNTDOWN_ACTION,
                                1000);
                    }
            }
        }
    };

    public boolean onVodMenuOpened(ISTVVodMenu menu) {

        if (avalibleRate[0]) {
        	if(menu.findItem(1) != null)
            menu.findItem(1).enable();
        } else {
        	if(menu.findItem(1) != null)
            menu.findItem(1).disable();
        }
        if (avalibleRate[1]) {
        	if(menu.findItem(2) != null)
            menu.findItem(2).enable();
        } else {
        	if(menu.findItem(2) != null)
            menu.findItem(2).disable();
        }
        if (avalibleRate[2]) {
        	if(menu.findItem(3) != null)
            menu.findItem(3).enable();
        } else {
        	if(menu.findItem(3) != null)
            menu.findItem(3).disable();
        }
//        menu.findItem(4).disable();
        if (panelShow) {
            hidePanel();
        }

        return true;
    }

    class SeekBarChangeEvent implements SeekBar.OnSeekBarChangeListener {

        RelativeLayout.LayoutParams params= (RelativeLayout.LayoutParams) progress_time.getLayoutParams();
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            if (!live_video) {
                if (clipLength > 0) {
                    timeBar.setProgress(progress);
                    Log.d(TAG, "LEFT seek to " + getTimeString(currPosition));
                }
                updataTimeText();
                if(clipLength/1000!=0)
                params.leftMargin=(seekBar.getWidth()-50)*(progress/1000)/(clipLength/1000)-params.width/2+getResources().getDimensionPixelOffset(R.dimen.play_seekbar_marginleft)+getResources().getDimensionPixelOffset(R.dimen.seekbar_thumb_w)/2;
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
                params.leftMargin=seekBar.getWidth()*seekBar.getProgress()/clipLength-params.width/2+getResources().getDimensionPixelOffset(R.dimen.play_seekbar_marginleft);
                progress_time.setLayoutParams(params);
                progress_time.setVisibility(View.VISIBLE);
                hidePanelHandler.removeCallbacks(hidePanelRunnable);
            }

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            Log.d(TAG, "onStopTrackingTouch" + seekBar.getProgress());
            if (!live_video) {
                mPlayer.seekTo(seekBar.getProgress());
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

    @Override
    public void onResume() {
        super.onResume();
        if(isneedpause){
            initView();
            initData();
        }
        isneedpause = true;
    }

    @Override
    public void onDestroy() {
        menu = null;
        urlInfo = null;
        mCheckHandler = null;
        historyManager = null;
        simpleRestClient = null;
        favoriteManager = null;
        releasePlayer();
        DaisyUtils.getVodApplication(this).removeActivtyFromPool(
                this.toString());
        super.onDestroy();
    }

    private void removeAllHandler() {
        mHandler.removeCallbacks(mUpdateTimeTask);
        mHandler.removeCallbacks(finishPlayerActivity);
        hideMenuHandler.removeCallbacks(hideMenuRunnable);
        mHandler.removeCallbacks(mUpdateTimeTask);
        mCheckHandler.removeCallbacks(checkStatus);
        hidePanelHandler.removeCallbacks(hidePanelRunnable);
    }

    @Override
    public void onPause() {
        if(isneedpause){
            try {
                createHistory(currPosition);
                addHistory(currPosition);
                checkTaskPause();
                timeTaskPause();
                removeAllHandler();
                mPlayer.stop();
                mPlayer.release();
                mPlayer = null;
            } catch (Exception e) {
                Log.d(TAG, "Player close to Home");
            }
        }
        super.onPause();
    }

    private void releasePlayer() {
        mHandler.removeCallbacksAndMessages(null);
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
        }
        mPlayer = null;
    }

    private Handler hidePanelHandler = new Handler();

    private Runnable hidePanelRunnable = new Runnable() {
        @Override
        public void run() {
            hidePanel();
            hidePanelHandler.removeCallbacks(hidePanelRunnable);
        }
    };

    private void hidePanel() {
        if (panelShow) {
            panelLayout.startAnimation(panelHideAnimation);
            panelLayout.setVisibility(View.GONE);
            top_panelayout.startAnimation(top_to_up);
            top_panelayout.setVisibility(View.GONE);
            panelShow = false;
        }
        getSupportFragmentManager().beginTransaction().hide(mEpisodeFragment).commitAllowingStateLoss();
    }

    private void showPanel() {
        if (isVodMenuVisible()||isAdPlaying)
            return;
        if (!panelShow) {
            panelLayout.startAnimation(panelShowAnimation);
            panelLayout.setVisibility(View.VISIBLE);
            top_panelayout.startAnimation(top_to_down);
            top_panelayout.setVisibility(View.VISIBLE);
            panelShow = true;
            hidePanelHandler.postDelayed(hidePanelRunnable, 3000);
        } else {
            hidePanelHandler.removeCallbacks(hidePanelRunnable);
            hidePanelHandler.postDelayed(hidePanelRunnable, 3000);
        }
//        getSupportFragmentManager().beginTransaction().show(mEpisodeFragment).commit();
    }

    private void pauseItem() {
        if (paused)
            return;
        hideBuffer();
        if (mPlayer.isPlaying())
            mPlayer.pause();
        paused = true;
        if (subItem != null)
            callaPlay.videoPlayPause(item.pk, subItem.pk, subItem.title, clip.pk,
                    currQuality, 0, currPosition, sid,"qiyi");
        else
            callaPlay.videoPlayPause(item.pk, null, item.title, clip.pk,
                    currQuality, 0, currPosition, sid,"qiyi");

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

    private void ExToClosePlayer(String code, String content) {
        if (bufferText != null) {
            bufferText.setText(EXTOCLOSE);
            if (code == "url") {
                try {
                    if (subItem != null)
                        callaPlay.videoExcept("noplayaddress", content,
                                item.pk, subItem.pk, subItem.title, clip.pk,
                                currQuality, 0,"qiyi");
                    else
                        callaPlay.videoExcept("noplayaddress", content,
                                item.pk, null, item.title, clip.pk,
                                currQuality, 0,"qiyi");
                } catch (Exception e) {
                    Log.e(TAG,
                            " Sender log videoExcept noplayaddress "
                                    + e.toString());
                }
            }
            if (code == "error") {
                try {
                    if (subItem != null)
                        callaPlay.videoExcept("mediaexception", content,
                                item.pk, subItem.pk, subItem.title, clip.pk,
                                currQuality, currPosition,"qiyi");
                    else
                        callaPlay.videoExcept("mediaexception", content,
                                item.pk, null, item.title, clip.pk,
                                currQuality, currPosition,"qiyi");
                } catch (Exception e) {
                    Log.e(TAG,
                            " Sender log videoExcept noplayaddress "
                                    + e.toString());
                }
            }

        }
        mHandler.postDelayed(finishPlayerActivity, 3000);
    }

    private Runnable mUpdateTimeTask = new Runnable() {
        @Override
        public void run() {
            if (mPlayer != null) {
                if (mPlayer.isPlaying() && !isfinish) {
                    seekPostion = mPlayer.getCurrentPosition();
                    currPosition = seekPostion;
                }
                mHandler.postDelayed(mUpdateTimeTask, 500);
            } else {
                Log.d(TAG, "mVideoView ======= null or err");
                timeTaskPause();
            }
        }
    };

    public void resumeItem() {
        if (!paused)
            return;
        hideBuffer();
        Log.d(TAG, "resume");
        mPlayer.start();
        if (subItem != null)
            callaPlay.videoPlayContinue(item.pk, subItem.pk, subItem.title,
                    clip.pk, currQuality, 0, currPosition, sid,"qiyi");
        else
            callaPlay.videoPlayContinue(item.pk, null, item.title, clip.pk,
                    currQuality, 0, currPosition, sid,"qiyi");
        // if (!isBuffer) {
        // timeTaskStart(0);
        // }
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
//    	if(keyCode == KeyEvent.KEYCODE_BACK && !"false".equals(shardpref.getSharedPrefs(AccountSharedPrefs.FIRST_USE))){
//			gesture_tipview.setVisibility(View.GONE);
//			shardpref.setSharedPrefs(AccountSharedPrefs.FIRST_USE, "false");
//			new initPlayTask().execute();
//			return false;
//		}
        if(keyCode != KeyEvent.KEYCODE_BACK && mTxtAdTimer.getVisibility() == View.VISIBLE)
            return ret;
        if("lcd_s3a01".equals(VodUserAgent.getModelName())){
            if(keyCode == 707 || keyCode == 774 || keyCode ==253){
                isneedpause = false;
            }
        }else if("lx565ab".equals(VodUserAgent.getModelName())){
            if(keyCode == 82 || keyCode == 707 || keyCode ==253){
                isneedpause = false;
            }
        }else{
            if(keyCode == 223 || keyCode == 499 || keyCode ==480){
                isneedpause = false;
            }
        }
        if (!isVodMenuVisible() && mPlayer != null) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_LEFT:
                case KeyEvent.KEYCODE_MEDIA_REWIND:
                    mHandler.removeCallbacks(mUpdateTimeTask);
                    mHandler.removeCallbacks(checkStatus);
                    if (mPlayer.isPlaying())
                        mPlayer.pause();
                    if (mHandler.hasMessages(MSG_SEK_ACTION))
                        mHandler.removeMessages(MSG_SEK_ACTION);
                    mHandler.sendEmptyMessageDelayed(MSG_SEK_ACTION, 1000);
                    isSeek = true;
                    showPanel();
                    fastBackward(SHORT_STEP);
                    ret = true;
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                    mHandler.removeCallbacks(mUpdateTimeTask);
                    mHandler.removeCallbacks(checkStatus);
                    if (mPlayer.isPlaying())
                        mPlayer.pause();
                    if (mHandler.hasMessages(MSG_SEK_ACTION))
                        mHandler.removeMessages(MSG_SEK_ACTION);
                    mHandler.sendEmptyMessageDelayed(MSG_SEK_ACTION, 1000);
                    isSeek = true;
                    showPanel();
                    fastForward(SHORT_STEP);
                    ret = true;
                    break;
                case KeyEvent.KEYCODE_DPAD_CENTER:
                case KeyEvent.KEYCODE_ENTER:
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    if (clipLength > 0) {
                        showPanel();
                        if (!paused) {
                            pauseItem();
                            playPauseImage
                                    .setImageResource(R.drawable.play);
                        } else {
                            resumeItem();
                            playPauseImage
                                    .setImageResource(R.drawable.paus);
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
//				ret = true;
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
                case KeyEvent.KEYCODE_DPAD_DOWN:
//				am.adjustStreamVolume(AudioManager.STREAM_MUSIC,
//						AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
//				ret = true;

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
                case KeyEvent.KEYCODE_BACK:
                    showPopupDialog(
                            DIALOG_OK_CANCEL,
                            getResources().getString(
                                    R.string.vod_player_exit_dialog));
                    ret = true;
                    if (!paused) {
                        pauseItem();
                        playPauseImage
                                .setImageResource(R.drawable.play);
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
        if (mPlayer != null && ret == false) {
            ret = super.onKeyDown(keyCode, event);
        }
        return ret;
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

    public void showPopupDialog(int type, String msg) {
        if (type == DIALOG_OK_CANCEL) {
            popupDlg = new Dialog(this, R.style.PopupDialog) {
                @Override
                public void onBackPressed() {
                    super.onBackPressed();
                    if (paused) {
                        resumeItem();
                        playPauseImage
                                .setImageResource(R.drawable.paus);
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
                    if (event.getAction() == MotionEvent.ACTION_HOVER_ENTER
                            || event.getAction() == MotionEvent.ACTION_HOVER_MOVE) {
                        v.requestFocus();
                    }
                    return false;
                }
            });
            btn2.setOnHoverListener(new OnHoverListener() {

                @Override
                public boolean onHover(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_HOVER_ENTER
                            || event.getAction() == MotionEvent.ACTION_HOVER_MOVE) {
                        v.requestFocus();
                    }
                    return false;
                }
            });
            if (btn1 != null) {
                btn1.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if (popupDlg != null && mPlayer != null) {
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
                                            0,
                                            "detail",
                                            currPosition,
                                            (System.currentTimeMillis() - startDuration),
                                            item.slug, sid, item.fromPage,
                                            item.content_model,"qiyi");
                                else
                                    callaPlay.videoExit(
                                            item.pk,
                                            null,
                                            item.title,
                                            clip.pk,
                                            currQuality,
                                            0,
                                            "detail",
                                            currPosition,
                                            (System.currentTimeMillis() - startDuration),
                                            item.slug, sid, item.fromPage,
                                            item.content_model,"qiyi");
                            } catch (Exception e) {
                                Log.e(TAG,
                                        " Sender log videoPlayStart "
                                                + e.toString());
                            }
                            releasePlayer();
                            finish();
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

    private Handler hideMenuHandler = new Handler();

    private Runnable hideMenuRunnable = new Runnable() {
        @Override
        public void run() {
            if (menu != null) {
                menu.hide();
                menu.clear();
                menu = null;
            }
            hideMenuHandler.removeCallbacks(hideMenuRunnable);
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

    @Override
    public void onVodMenuClosed(ISTVVodMenu menu) {

    }

    public boolean createMenu(ISTVVodMenu menu) {
        ISTVVodMenuItem sub;

        if (listItems != null && listItems.size() > 0) {
            sub = menu.addSubMenu(100,
                    getResources().getString(R.string.serie_switch));
            for (Item i : listItems) {
                String tempurl = simpleRestClient.root_url + "/api/subitem/"
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
        if(avalibleRate[0])
        sub.addItem(1,
                getResources().getString(R.string.vod_player_quality_medium),
                true, currQuality == 0);
        if(avalibleRate[1])
        sub.addItem(2,
                getResources().getString(R.string.vod_player_quality_high),
                true, currQuality == 1);
        if(avalibleRate[2])
        sub.addItem(3,
                getResources().getString(R.string.vod_player_quality_ultra),
                true, currQuality == 2);
//        sub.addItem(4,
//                getResources().getString(R.string.vod_player_quality_adaptive));
        menu.addItem(20, getResources().getString(R.string.kefucentertitle));
        menu.addItem(30, getResources().getString(R.string.playfromstarttitle));

        return true;
    }

    private void updataTimeText() {
        String text = getTimeString(currPosition);
        String text2=getTimeString(clipLength);
        timeText.setText(text);
        endtime.setText(text2);
    }

    private BitStream getDefinityByQuality(int currQuality) {
        switch (currQuality) {
            case 0:
                return BitStream.BITSTREAM_HIGH;
            case 1:
                return BitStream.BITSTREAM_720P;
            case 2:
                return BitStream.BITSTREAM_1080P;
        }
        return BitStream.BITSTREAM_HIGH;
    }

    private void initQualtiyText() {
        switch (currQuality) {
            case 0:
              //  qualityText.setBackgroundResource(R.drawable.vodplayer_stream_normal);
                qualityText.setText("流畅");
                break;
            case 1:
               // qualityText.setBackgroundResource(R.drawable.vodplayer_stream_high);
                qualityText.setText("高清");
                break;
            case 2:
//                qualityText
//                        .setBackgroundResource(R.drawable.vodplayer_stream_ultra);
                qualityText.setText("超清");
                break;
            case 3:
                qualityText.setText("自适应");
               // qualityText.setBackgroundResource(R.drawable.rounded_edittext);
                break;
            default:
//                qualityText
//                        .setBackgroundResource(R.drawable.vodplayer_stream_normal);
                qualityText.setText("流畅");
                break;
        }

    }

    @Override
    public boolean onVodMenuClicked(ISTVVodMenu menu, int id) {
        if (id > 0 && id < 5) {
            int pos = id - 1;
            if (currQuality != pos) {
                try {
                    timeTaskPause();
                    checkTaskPause();
                    paused = false;
                    isBuffer = true;
                    showBuffer();
                    playPauseImage
                            .setImageResource(R.drawable.paus);
                    currQuality = pos;
                    if (currQuality == 0) {
                        mPlayer.switchBitStream(BitStream.BITSTREAM_HIGH);
                    } else if (currQuality == 1) {
                        mPlayer.switchBitStream(BitStream.BITSTREAM_720P);
                    } else {
                        mPlayer.switchBitStream(BitStream.BITSTREAM_1080P);
                    }
                    mPlayer.pause();
                    // historyManager.addOrUpdateQuality(new Quality(0,
                    // urls[currQuality], currQuality));
                    mediaip = "127.0.0.1";
                    if (subItem != null)
                        callaPlay.videoSwitchStream(item.pk, subItem.pk,
                                subItem.title,clip.pk, currQuality, "manual",
                                null, null, mediaip, sid,"qiyi");
                    else
                        callaPlay.videoSwitchStream(item.pk, null, item.title,
                                clip.pk, currQuality, "manual", null, null,
                                mediaip, sid,"qiyi");
                    initQualtiyText();
                    return true;
                } catch (Exception e) {
                    Log.d(TAG, "Exception change url " + e);
                    return false;
                }
            }
            return true;
        }
        if (id == 5) {
            String isnet = "";
            if (SimpleRestClient.isLogin()) {
                isnet = "yes";
            } else {
                isnet = "no";
            }
            if (itemUrl != null && favoriteManager != null
                    && favoriteManager.getFavoriteByUrl(itemUrl, isnet) != null) {
                if (isnet.equals("yes")) {
                    deleteFavoriteByNet();
                }
                favoriteManager.deleteFavoriteByUrl(itemUrl, isnet);
                menu.findItem(5).setTitle(
                        getResources().getString(
                                R.string.vod_player_bookmark_setting));
            } else {
                if (item != null && favoriteManager != null && itemUrl != null) {
                    favorite = new Favorite();
                    favorite.adlet_url = item.adlet_url;
                    favorite.content_model = item.content_model;
                    favorite.is_complex = item.is_complex;
                    favorite.title = item.title;
                    favorite.url = itemUrl;
                    favorite.isnet = isnet;
                    if (isnet.equals("yes")) {
                        createFavoriteByNet();
                    }
                    favoriteManager.addFavorite(favorite, isnet);
                    menu.findItem(5)
                            .setTitle(
                                    getResources()
                                            .getString(
                                                    R.string.vod_bookmark_remove_bookmark_setting));
                }
            }
            return true;
        }
        if (id == 6) {
            gotoRelatePage();
            return true;
        }
        if (id == 8) {
            isContinue = true;
            addHistory(seekPostion);
            return true;
        }
        if (id == 9) {
            isContinue = false;
            addHistory(seekPostion);
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
            mPlayer.seekTo(currPosition);
            showBuffer();
            return true;
        }

        if (id > 100) {
            try {
                if (subItem != null)
                    callaPlay
                            .videoExit(
                                    item.pk,
                                    subItem.pk,
                                    subItem.title,
                                    clip.pk,
                                    currQuality,
                                    0,
                                    "end",
                                    currPosition,
                                    (System.currentTimeMillis() - startDuration),
                                    item.slug, sid, item.fromPage,
                                    item.content_model,"qiyi");// String
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
                                    item.slug, sid, item.fromPage,
                                    item.content_model,"qiyi");
            } catch (Exception e) {
                Log.e(TAG, " log Sender videoExit end " + e.toString());
            }
            subItemUrl = simpleRestClient.root_url + "/api/subitem/" + id + "/";
            bundle.remove("url");
            bundle.putString("url", subItemUrl);
            currPosition = 0;
            tempOffset = 0;
            addHistory(currPosition);
            mPlayer.stop();
            isBuffer = true;
            showBuffer();
            new ItemByUrlTask().execute();
        }

        return true;
    }
    public void changeQuality(int pos){
        if (mBitStreamList.size()!=0 && pos!=currQuality) {
            if (currQuality != pos) {
                try {
                    timeTaskPause();
                    checkTaskPause();
                    paused = false;
                    isBuffer = true;
                    showBuffer();
                    playPauseImage
                            .setImageResource(R.drawable.paus);
                    currQuality = pos;
                    if (currQuality == 0) {
                        mPlayer.switchBitStream(BitStream.BITSTREAM_HIGH);
                    } else if (currQuality == 1) {
                        mPlayer.switchBitStream(BitStream.BITSTREAM_720P);
                    } else {
                        mPlayer.switchBitStream(BitStream.BITSTREAM_1080P);
                    }
                    mPlayer.pause();
                    // historyManager.addOrUpdateQuality(new Quality(0,
                    // urls[currQuality], currQuality));
                    mediaip = "127.0.0.1";
                    if (subItem != null)
                        callaPlay.videoSwitchStream(item.pk, subItem.pk,
                                subItem.title, clip.pk, currQuality, "manual",
                                null, null, mediaip, sid, "qiyi");
                    else
                        callaPlay.videoSwitchStream(item.pk, null, item.title,
                                clip.pk, currQuality, "manual", null, null,
                                mediaip, sid, "qiyi");
                    initQualtiyText();
                } catch (Exception e) {
                    Log.d(TAG, "Exception change url " + e);
                }
            }
        }
    }
    private void deleteFavoriteByNet() {
        simpleRestClient.doSendRequest("/api/bookmark/remove/", "post",
                "access_token=" + SimpleRestClient.access_token
                        + "&device_token=" + SimpleRestClient.device_token
                        + "&item=" + item.pk, new HttpPostRequestInterface() {

                    @Override
                    public void onSuccess(String info) {
                        // TODO Auto-generated method stub
                        if ("200".equals(info)) {

                        }
                    }

                    @Override
                    public void onPrepare() {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onFailed(String error) {
                        // TODO Auto-generated method stub

                    }
                });
    }

    private void createFavoriteByNet() {
        simpleRestClient.doSendRequest("/api/bookmarks/create/", "post",
                "access_token=" + SimpleRestClient.access_token
                        + "&device_token=" + SimpleRestClient.device_token
                        + "&item=" + item.pk, new HttpPostRequestInterface() {

                    @Override
                    public void onSuccess(String info) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onPrepare() {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onFailed(String error) {
                        // TODO Auto-generated method stub

                    }
                });
    }

    private void gotoRelatePage() {
        Intent intent = new Intent();
        intent.setClass(QiYiPlayActivity.this,
                tv.ismar.daisy.RelatedActivity.class);
        intent.putExtra("item", item);
        startActivity(intent);
        addHistory(seekPostion);
        checkTaskPause();
        timeTaskPause();
        mPlayer.stop();
        QiYiPlayActivity.this.finish();
    }

    private void addHistory(int last_position) {
        if (item != null && historyManager != null) {
            History history = new History();
            if (subItem != null && subItem.title != null) {
                history.title = subItem.title;
            } else {
                history.title = item.title;
            }
            history.adlet_url = item.adlet_url;
            history.content_model = item.content_model;
            history.is_complex = item.is_complex;
            history.last_position = last_position;
            history.last_quality = currQuality;
            history.url = itemUrl;
            history.sub_url = subItemUrl;
            history.is_continue = isContinue;
            if(item.expense!=null){
                history.cpid=item.expense.cpid;
                history.cpname=item.expense.cpname;
                history.cptitle=item.expense.cptitle;
                history.paytype=item.expense.pay_type;
            }
            if (!SimpleRestClient.isLogin())
                historyManager.addHistory(history, "no");
            else
                historyManager.addHistory(history, "yes");
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
        if (subItem != null) {
            params = params + "&subitem=" + subItem.pk;
        } else {
            params = params + "&item=" + item.item_pk;
        }
        simpleRestClient.doSendRequest("/api/histories/create/", "post",
                params, new HttpPostRequestInterface() {

                    @Override
                    public void onSuccess(String info) {
                        // TODO Auto-generated method stub
                        Log.i("BAIDU", info);
                    }

                    @Override
                    public void onPrepare() {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onFailed(String error) {
                        // TODO Auto-generated method stub
                        Log.i("BAIDU", error);
                    }
                });
    }

    private Runnable checkStatus = new Runnable() {
        public void run() {
            if (isfinish) {
                currPosition = 0;
                timeBar.setProgress(currPosition);
            } else {
                if (isBuffer || bufferLayout.isShown()) {
                    isBuffer = false;
                    hideBuffer();
                }
                if (live_video && (isBuffer || bufferLayout.isShown())) {
                    isBuffer = false;
                    hideBuffer();
                }
                bufferText.setText(BUFFERING);
                if (!isSeek && !isBuffer && !live_video && mPlayer.isPlaying()) {
                    currPosition = mPlayer.getCurrentPosition();
                    timeBar.setProgress(currPosition);
                }
            }
            mCheckHandler.postDelayed(checkStatus, 400);
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

    private Runnable finishPlayerActivity = new Runnable() {
        public void run() {
            mHandler.removeCallbacks(finishPlayerActivity);
            QiYiPlayActivity.this.finish();
        }
    };

    private void checkTaskPause() {
        mCheckHandler.removeCallbacks(checkStatus);
    }

    private void gotoFinishPage() {
        timeTaskPause();
        checkTaskPause();
        try {
            if (subItem != null)
                callaPlay
                        .videoExit(
                                item.pk,
                                subItem.pk,
                                subItem.title,
                                clip.pk,
                                currQuality,
                                0,
                                "end",
                                currPosition,
                                (System.currentTimeMillis() - startDuration),
                                item.slug, sid, item.fromPage,
                                item.content_model,"qiyi");// String
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
                                item.slug, sid, item.fromPage,
                                item.content_model,"qiyi");
        } catch (Exception e) {
            Log.e(TAG, " log Sender videoExit end " + e.toString());
        }
        if (mPlayer != null) {
            if (listItems != null && listItems.size() > 0
                    && currNum < (listItems.size() - 1)) {
                subItem = listItems.get(currNum + 1);
                subItemUrl = simpleRestClient.root_url + "/api/subitem/"
                        + subItem.pk + "/";
                bundle.remove("url");
                bundle.putString("url", subItemUrl);
                mPlayer.stop();
                addHistory(0);
                isBuffer = true;
                showBuffer();
                seekPostion = 0;
                currPosition = 0;
                tempOffset = 0;
                isfinish = true;
                new ItemByUrlTask().execute();
            } else {
                Intent intent = new Intent("tv.ismar.daisy.PlayFinished");
                intent.putExtra("item", item);
                startActivity(intent);
                seekPostion = 0;
                currPosition = 0;
                mPlayer.stop();
                mPlayer.release();
                mPlayer = null;
                addHistory(0);
                QiYiPlayActivity.this.finish();
            }
        }

    }

    private OnHoverListener onhoverlistener = new OnHoverListener() {

        @Override
        public boolean onHover(View v, MotionEvent event) {
            int what = event.getAction();
            switch (what) {
                case MotionEvent.ACTION_HOVER_MOVE:
                    if (mTxtAdTimer != null
                            && mTxtAdTimer.getVisibility() != View.VISIBLE && clipLength >0)
                        showPanel();
                    break;
            }
            return false;
        }

    };

    private void startPlayMovie(IMedia media) {
        isfinish = false;
        if (!mQiyiSdkInitialized) {//必须SDK初始化成功后调用
            return;
        }
        releasePlayer();
        //创建IVideoOverlay对象, 不支持实现IVideoOverlay接口，必须调用PlaySdk.getInstance().createVideoOverlay创建
        //创建IVideoOverlay对象, 不需创建SurfaceView, 直接传入父容器即可
        mVideoOverlay = PlayerSdk.getInstance().createVideoOverlay(frameContainer);

        //创建IVideoOverlay对象, 如需修改SurfaceView, 请继承VideoSurfaceView
        //mSurfaceView = new MyVideoSurfaceView(getApplicationContext());
        //mVideoOverlay = PlaySdk.getInstance().createVideoOverlay(mWindowedParent, mSurfaceView);

        //IMediaPlayer对象通过QiyiPlayerSdk.getInstance().createVideoPlayer()创建
        mPlayer = PlayerSdk.getInstance().createMediaPlayer();

        //setVideo方法, 更名为setData, 必须调用, 需传入IMedia对象, 起播时间点修改为从IMedia对象获取, 不从setData传参
        mPlayer.setData(media);

        //设置IVideoOverlay对象, 必须调用
        mPlayer.setDisplay(mVideoOverlay);

        //设置播放状态回调监听器, 需要时设置
        mPlayer.setOnStateChangedListener(mStateChangedListener);

        //设置码流信息回调监听器, 需要时设置
        mPlayer.setOnBitStreamInfoListener(mBitStreamInfoListener);

        //设置VIP试看信息回调监听器, 需要时设置
        mPlayer.setOnPreviewInfoListener(mPreviewInfoListener);

        //设置视频分辨率回调监听器, 需要时设置
        mPlayer.setOnVideoSizeChangedListener(mVideoSizeChangedListener);

        //设置seek完成监听器, 需要时设置
        mPlayer.setOnSeekCompleteListener(mSeekCompleteListener);

        //设置片头片尾信息监听器, 需要时设置
        mPlayer.setOnHeaderTailerInfoListener(mHeaderTailerInfoListener);

        //设置缓冲事件监听器, 需要时设置
        mPlayer.setOnBufferChangedListener(mBufferChangedListener);

        //调用prepareAsync, 播放器开始准备, 必须调用
        mPlayer.prepareAsync();

        //debug code.
//        showPlaybackInfo(media);
    }

    private void doOnSuccess() {
        //login, 同步操作, 有网络接口调用, 可能耗时, 请注意. 初始只需调用一次, 登录成功后一直有效, 如需登出, 请调用logout
               if(SimpleRestClient.zuser_token != null && !"".equals(SimpleRestClient.zuser_token)){
                        PlayerSdk.getInstance().login(SimpleRestClient.zuser_token);
                    }else if(SimpleRestClient.zdevice_token != null && !"".equals(SimpleRestClient.zdevice_token)){
                        PlayerSdk.getInstance().login(SimpleRestClient.zdevice_token);
                    }else{
                        PlayerSdk.getInstance().login("76d0baca6075c45cd8a3a55fa6a23c05324489b865af03383292a41fde765ec6");
                    }

        //测试栏位
//        SdkVideo video = new SdkVideo("202168401", "310271100", BitStream.BITSTREAM_720P, false);
//        startPlayMovie(video);
        if (subItem != null) {
            titleText.setText(subItem.title);
        } else {
            titleText.setText(item.title);
        }
        if (tempOffset > 0 && isContinue == true && !live_video) {
            bufferText.setText("  " + BUFFERCONTINUE
                    + getTimeString(tempOffset));
        } else {
            bufferText.setText(PlAYSTART + "《" + titleText.getText() + "》");
        }
        String info = (String) bundle.get("iqiyi");
        isBuffer = true;
        showBuffer();
        if (tempOffset > 0 && !isfinish) {
            currPosition = tempOffset;
            seekPostion = tempOffset;
        } else {
            currPosition = 0;
            seekPostion = 0;
        }
        BitStream definition = getDefinityByQuality(currQuality);
        if (info != null && info.contains("iqiyi_4_0")) {
            startPlayMovie(AccessProxy.getQiYiInfo(info, definition));
        } else {
            String[] array = info.split(":");
            SdkVideo qiyiInfo = new SdkVideo(array[0],array[1],array[2],urlInfo.isIs_vip());
            startPlayMovie(qiyiInfo);
        }
        sid = MD5Utils.encryptByMD5(SimpleRestClient.sn_token+System.currentTimeMillis());
        startDuration = System.currentTimeMillis();
        if (subItem != null)
            callaPlay.videoStart(item, subItem.pk, subItem.title,
                    currQuality, null, 0, mSection, sid,"qiyi");
        else
            callaPlay.videoStart(item, null, item.title, currQuality,
                    null, 0, mSection, sid,"qiyi");
        isfinish = false;
        initQualtiyText();
    }

    private OnStateChangedListener mStateChangedListener = new OnStateChangedListener() {
        @Override
        public boolean onError(IMediaPlayer player, ISdkError error) {
            if (mPlayer == null)
                return false;
            addHistory(currPosition);
            ExToClosePlayer("error",
                    error.getCode() + " " + error.getMsgFromError());
            return false;
        }

        @Override
        public void onAdStart(IMediaPlayer player) {
            Log.d(TAG, "onAdStart");
            hidePanel();
            isAdPlaying = true;
            mPlayer=player;
            mTxtAdTimer.setVisibility(View.VISIBLE);
            startAdCountDown();
        }

        @Override
        public void onAdEnd(IMediaPlayer player) {
            isAdPlaying = false;
            mTxtAdTimer.setVisibility(View.GONE);
            stopAdCountDown();
        }

        @Override
        public void onStarted(IMediaPlayer player) {
            Log.d(TAG, "onStarted: current position=" + player.getCurrentPosition() + ", duration=" + player.getDuration());
            if (mPlayer == null)
                return;
            if(previewLength > 0){
                timeBar.setMax(previewLength);
                progress_play.setMax(previewLength);
                clipLength = previewLength;
            }else{
                clipLength = mPlayer.getDuration();
                timeBar.setMax(clipLength);
                progress_play.setMax(clipLength);
            }

            if(currPosition >0)
                mPlayer.seekTo(currPosition);
            if (subItem != null) {
                callaPlay
                        .videoPlayStart(item.pk, subItem.pk,
                                subItem.title, clip.pk,
                                currQuality, 0,sid,"qiyi");
            } else {
                callaPlay
                        .videoPlayStart(item.pk, null,
                                item.title, clip.pk,
                                currQuality, 0,sid,"qiyi");
            }
            showPanel();
            timeTaskStart(500);
            checkTaskStart(500);
            if (mHandler.hasMessages(MSG_PLAY_TIME))
                mHandler.removeMessages(MSG_PLAY_TIME);
            mHandler.sendEmptyMessage(MSG_PLAY_TIME);
        }

        @Override
        public void onCompleted(IMediaPlayer player) {
            Log.d(TAG, "onCompleted");
            //TODO, onPreviewComplete回调接口去掉, 保持状态不重复; 当onComplete回调时，判断如果是试看, 即表示试看结束
            if (mPlayer == null)
                return;
            if(mIsPreview){
                QiYiPlayActivity.this.finish();
                return;
            }
            gotoFinishPage();
        }

        @Override
        public void onPaused(IMediaPlayer player) {
            Log.d(TAG, "onPaused");
        }

        @Override
        public void onStopped(IMediaPlayer player) {
            Log.d(TAG, "onStopped");
        }

        @Override
        public void onPrepared(IMediaPlayer player) {
            if (mPlayer == null)
                return;
            if (subItem != null) {
                callaPlay.videoPlayLoad(
                        item.pk,
                        subItem.pk,
                        subItem.title,
                        clip.pk,
                        currQuality,
                        (System.currentTimeMillis() - startDuration) / 1000,
                        0, mediaip, sid,"","qiyi");
            } else {
                callaPlay.videoPlayLoad(
                        item.pk,
                        null,
                        item.title,
                        clip.pk,
                        currQuality,
                        (System.currentTimeMillis() - startDuration) / 1000,
                        0, mediaip, sid,"","qiyi");
            }
            if (seekPostion > 0)
                mPlayer.seekTo(seekPostion);
            else
                mPlayer.start();
        }
    };
    private OnBitStreamInfoListener mBitStreamInfoListener = new OnBitStreamInfoListener() {

        @Override
        public void onBitStreamListUpdate(IMediaPlayer player, List<BitStream> bitstreamList) {
            Log.d(TAG, "onBitStreamListReady(" + bitstreamList + ")");
            if (mPlayer == null)
                return;
            mBitStreamList = bitstreamList;
            for (BitStream d : mBitStreamList) {
                if (d.equals(BitStream.BITSTREAM_HIGH)) {
                    avalibleRate[0] = true;
                    // currQuality = 0;
                } else if (d.equals(BitStream.BITSTREAM_720P)) {
                    avalibleRate[1] = true;
                    // currQuality = 1;
                } else if (d.equals(BitStream.BITSTREAM_1080P)) {
                    avalibleRate[2] = true;
                    // currQuality = 2;
                }
            }
        }

        @Override
        public void onBitStreamSelected(IMediaPlayer player, final BitStream bitstream) {
            Log.d(TAG, "onPlaybackBitStreamSelected(" + bitstream + ")");
            if (mPlayer == null)
                return;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    qualityText.setVisibility(View.VISIBLE);
                }
            });
            currentDefinition = bitstream;
            if (currentDefinition == BitStream.BITSTREAM_HIGH) {
                currQuality = 0;
            } else if (currentDefinition == BitStream.BITSTREAM_720P) {
                currQuality = 1;
            } else {
                currQuality = 2;
            }
            // if (mHandler.hasMessages(MSG_INITQUALITYTITLE))
            mHandler.removeMessages(MSG_INITQUALITYTITLE);
            mHandler.sendEmptyMessage(MSG_INITQUALITYTITLE);
        }
    };

    private OnBufferChangedListener mBufferChangedListener = new OnBufferChangedListener() {

        @Override
        public void onBufferStart(IMediaPlayer player) {
            Log.d(TAG, "onBufferStart");
            if (mPlayer == null)
                return;
            isBuffer = true;
            showBuffer();
        }

        @Override
        public void onBufferEnd(IMediaPlayer player) {
            Log.d(TAG, "onBufferEnd");
            if (mPlayer == null)
                return;
            if (!mPlayer.isPlaying())
                mPlayer.start();
            isBuffer = false;
            hideBuffer();
            checkTaskStart(0);
        }
    };

    private OnSeekCompleteListener mSeekCompleteListener =  new OnSeekCompleteListener() {
        @Override
        public void onSeekCompleted(IMediaPlayer player) {
            Log.d(TAG, "onSeekComplete");
            if (mPlayer == null)
                return;
            if (!mPlayer.isPlaying())
                mPlayer.start();
            isBuffer = false;
            hideBuffer();
            checkTaskStart(0);
        }
    };

    private OnVideoSizeChangedListener mVideoSizeChangedListener = new OnVideoSizeChangedListener() {

        @Override
        public void onVideoSizeChanged(IMediaPlayer player, int width, int height) {
            Log.d(TAG, "onVideoSizeChanged(" + width + ", " + height + ")");
        }
    };

    private OnPreviewInfoListener mPreviewInfoListener = new OnPreviewInfoListener() {

        @Override
        public void onPreviewInfoReady(IMediaPlayer player, final boolean isPreview, final int previewEndTimeInSecond) {
            Log.d(TAG, "onPreviewInfoReady: isPreview=" + isPreview + ", previewEndTimeInSecond=" + previewEndTimeInSecond);
            mIsPreview = isPreview;
            String text = "isPreview=" + isPreview + ", previewEndTimeInSecond=" + previewEndTimeInSecond;
            previewLength = previewEndTimeInSecond;
        }
    };

    private OnHeaderTailerInfoListener mHeaderTailerInfoListener = new OnHeaderTailerInfoListener() {
        @Override
        public void onHeaderTailerInfoReady(IMediaPlayer player, int headerTime, int tailerTime) {
            //TODO, 片头片尾时间，单位改为毫秒
            Log.d(TAG, "onHeaderTailerInfoReady(" + headerTime + "/" + tailerTime + ")");
        }
    };

    private void startAdCountDown() {
        mHandler.removeMessages(MSG_AD_COUNTDOWN);
        mHandler.sendEmptyMessage(MSG_AD_COUNTDOWN);
    }

    private void stopAdCountDown() {
        mHandler.removeMessages(MSG_AD_COUNTDOWN);
    }

    private void startSakura() {
        Intent intent = new Intent();
        intent.setAction("cn.ismar.sakura.launcher");
        startActivity(intent);
    }
    View.OnClickListener listener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.QualityText:
                    creatPopWindows();
                    if(!isAnthologyShow) {
                        isAnthologyShow = true;
                        if(!panelShow)
                            showPanel();
                        hidePanelHandler.removeCallbacks(hidePanelRunnable);
                    }else{
                        isAnthologyShow = false;
                        hidePanelHandler.postDelayed(hidePanelRunnable,3000);
                    }
                    break;
                case R.id.anthology:
                    if(listItems.size()>0){
                        if(!isAnthologyShow) {
                            isAnthologyShow = true;
                            if(!panelShow)
                                showPanel();
                            anthology.setTextColor(getResources().getColor(R.color._ff9c3c));
                            getSupportFragmentManager().beginTransaction().show(mEpisodeFragment).commit();
                            hidePanelHandler.removeCallbacks(hidePanelRunnable);
                        }else{
                            isAnthologyShow = false;
                            anthology.setTextColor(getResources().getColor(R.color._e4e4e4));
                            getSupportFragmentManager().beginTransaction().hide(mEpisodeFragment).commit();
                            hidePanelHandler.postDelayed(hidePanelRunnable,3000);
                        }

                    }
                    break;
                case R.id.player_back:
                    finish();
                    break;
            }
        }
    };
    public void creatPopWindows(){
        View pop=View.inflate(this, R.layout.quality_pop_item, null);
        popupWindow = new PopupWindow(pop,getResources().getDimensionPixelSize(R.dimen.quality_pop_width), getResources().getDimensionPixelSize(R.dimen.quality_pop_height));
        //popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.quality_pop));
        popupWindow.setBackgroundDrawable(getResources().getDrawable(R.color._202020));
        popupWindow.getBackground().setAlpha(102);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        LinearLayout popMenu= (LinearLayout) pop.findViewById(R.id.pop);
        //for(int i=0;i<mBitStreamList.size();i++){
//            if(urls[i]!=null){
//                switch (i){
//                    case 0:
//                        textView.setText("流畅");
//                        break;
//                    case 1:
//                        textView.setText("高清");
//                        break;
//                    case 2:
//                        textView.setText("超清");
//                        break;
//                    default:
//                        textView.setText("自适应");
//                }
                 int j=0;
                for(int i=0;i<mBitStreamList.size();i++){
                BitStream d =mBitStreamList.get(i);
                View view=View.inflate(this,R.layout.pop_menu_item,null);
                TextView textView= (TextView) view.findViewById(R.id.quality_text);
                ImageView img= (ImageView) view.findViewById(R.id.quality_focus);
                        if (d.equals(BitStream.BITSTREAM_HIGH)) {
                         //   avalibleRate[0] = true;
                            // currQuality = 0;
                            j=0;
                            textView.setText("流畅");
                        } else if (d.equals(BitStream.BITSTREAM_720P)) {
                            //avalibleRate[1] = true;
                            // currQuality = 1;
                            j=1;
                            textView.setText("高清");
                        } else if (d.equals(BitStream.BITSTREAM_1080P)) {
                            // avalibleRate[2] = true;
                            textView.setText("超清");
                            j=2;
                            // currQuality = 2;
                        }
                if(j==currQuality){
                    textView.setTextColor(getResources().getColor(R.color._ff9c3c));
                    img.setBackgroundResource(R.drawable.quality_chosed);
                }
                final int k=j;
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        changeQuality(k);
                        popupWindow.dismiss();
                    }
                });
                popMenu.addView(view);
            }
        int[] location = new int[2];
        pop.getLocationOnScreen(location);
        Log.i("Height", panelLayout.getHeight() + "");

        popupWindow.showAtLocation(panelLayout, Gravity.NO_GRAVITY, location[0] + 2325, location[1] + 985);
    }

    @Override
    public void onEpisodeItemSelected(ItemEntity.SubItem msubItem) {
        try {
            hidePanel();
            anthology.setTextColor(getResources().getColor(R.color._e4e4e4));
            if (subItem != null)
                callaPlay
                        .videoExit(
                                item.pk,
                                subItem.pk,
                                subItem.title,
                                clip.pk,
                                currQuality,
                                0,
                                "end",
                                currPosition,
                                (System.currentTimeMillis() - startDuration),
                                item.slug, sid, item.fromPage,
                                item.content_model,"qiyi");// String
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
                                item.slug, sid, item.fromPage,
                                item.content_model,"qiyi");
        } catch (Exception e) {
            Log.e(TAG, " log Sender videoExit end " + e.toString());
        }
        subItemUrl = simpleRestClient.root_url + "/api/subitem/" + msubItem.getPk() + "/";
        bundle.remove("url");
        bundle.putString("url", subItemUrl);
        currPosition = 0;
        tempOffset = 0;
        addHistory(currPosition);
        mPlayer.stop();
        isBuffer = true;
        showBuffer();
        new ItemByUrlTask().execute();
    }

    public void fetchItemInfo(String itemId) {

        Retrofit retrofit = new Retrofit.Builder()
                .client(HttpManager.getInstance().mClient)
                .baseUrl(appendProtocol(SimpleRestClient.root_url))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        retrofit.create(HttpAPI.ApiItem.class).doRequest(itemId).enqueue(new retrofit2.Callback<ItemEntity>() {
            @Override
            public void onResponse(retrofit2.Response<ItemEntity> response) {
                if (response.body()!= null){
                    ItemEntity itemEntity = response.body();
                    if (itemEntity.getSubitems() != null && itemEntity.getSubitems().length != 0) {
                        switch (itemEntity.getContentModel()){
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
    //滑动改变音量
    private void changeVolumn(float movePercent) {
        if(bright.getVisibility()==View.VISIBLE||play_progress.getVisibility()==View.VISIBLE||isAdPlaying){
            return;
        }
        volumn.setVisibility(View.VISIBLE);
        float newVolumn=currentVolumn+movePercent*100;
        if(newVolumn>100){
            newVolumn=100;
        }else if(newVolumn<0){
            newVolumn=0;
        }
        setVolumn((int) newVolumn);
    }
    //滑动改变亮度
    private void changeBright(float movePercent) {
        if(volumn.getVisibility()==View.VISIBLE||play_progress.getVisibility()==View.VISIBLE||isAdPlaying){
            return;
        }
        bright.setVisibility(View.VISIBLE);
        float newBright=currentBright+movePercent*100;
        if(newBright>100){
            newBright=100;
        }else if(newBright<0){
            newBright=0;
        }
        setBright((int) newBright);
    }
    private void changePlayProgress(float movePercent){
        if(volumn.getVisibility()==View.VISIBLE||bright.getVisibility()==View.VISIBLE||isAdPlaying){
            return;
        }
        if(movePercent>0){
            play_progress.setBackgroundResource(R.drawable.moveon);
        }else{
            play_progress.setBackgroundResource(R.drawable.moveback);
        }
        play_progress.setVisibility(View.VISIBLE);
        progress_play.setProgress((int) (currPosition + movePercent*1000));
        current_play_progress.setText(getTimeString((int) (currPosition+movePercent*1000)<0?0:(int) (currPosition+movePercent*1000))+"/"+getTimeString(clipLength));
        mPlayer.seekTo((int) (currPosition+movePercent*1000));
        timeBar.setProgress(currPosition);
        currPosition=mPlayer.getCurrentPosition();
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
        Window window=getWindow();
        WindowManager.LayoutParams params=window.getAttributes();
        params.screenBrightness=bright/100f;
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
        return brightness*100/255;
    }
}
