package tv.ismar.daisy;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.models.Clip;
import tv.ismar.daisy.models.Favorite;
import tv.ismar.daisy.models.History;
import tv.ismar.daisy.models.Item;
import tv.ismar.daisy.persistence.FavoriteManager;
import tv.ismar.daisy.persistence.HistoryManager;
import tv.ismar.daisy.player.CallaPlay;
import tv.ismar.daisy.player.ISTVVodMenu;
import tv.ismar.daisy.qiyimediaplayer.SdkVideo;
import tv.ismar.daisy.views.IsmatvVideoView;

import com.ismartv.api.t.AccessProxy;
import com.ismartv.bean.ClipInfo;
import com.qiyi.video.player.IVideoStateListener;
import com.qiyi.video.player.QiyiVideoPlayer;
import com.qiyi.video.player.data.Definition;
import com.qiyi.video.player.data.IPlaybackInfo;
import com.qiyi.video.utils.LogUtils;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class QiYiPlayActivity extends Activity {
	private static final int MSG_AD_COUNTDOWN = 100;
	private static final int MSG_PLAY_TIME = 101;
	// seek steps 5s 10s 30s 1m 5m 10m
	private static final int[] SEEK_STEPS = { 5000, 10000, 30000, 60000,
			300000, 600000 };
	private static final HashMap<Definition, String> DEFINITION_NAMES;
	@SuppressWarnings("unused")
	private static final String SAMPLE = "http://114.80.0.33/qyrrs?url=http%3A%2F%2Fjq.v.tvxio.com%2Fcdn%2F0%2F7b%2F78fadc2ffa42309bda633346871f26%2Fhigh%2Fslice%2Findex.m3u8&quality=high&sn=weihongchang_s52&clipid=779521&sid=85d3f919a918460d9431136d75db17f03&sign=08a868ad3c4e3b37537a13321a6f9d4b";
	private QiyiVideoPlayer mPlayer;
	private static final String PREFS_NAME = "tv.ismar.daisy";
	private static final String TAG = "PLAYER";
	private static final String BUFFERCONTINUE = " 上次放映：";
	private static final String PlAYSTART = " 即将放映：";
	private static final String BUFFERING = " 正在加载 ";
	private static final String EXTOCLOSE = " 网络数据异常，即将退出播放器";
	@SuppressWarnings("unused")
	private static final String HOST = "cord.tvxio.com";

	private static final int SHORT_STEP = 1;
	private static final int DIALOG_OK_CANCEL = 0;

	private boolean paused = false;
	private boolean isBuffer = true;
	private boolean isSeekBuffer = false;
	private boolean panelShow = false;
	private int currQuality = 0;
	private String urls[] = new String[6];
	private Animation panelShowAnimation;
	private Animation panelHideAnimation;
	// private Animation bufferHideAnimation;
	private LinearLayout bufferLayout;
	private ImageView logoImage;
	private LinearLayout panelLayout;
	private TextView titleText;
	private TextView qualityText;
	private TextView timeText;
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
	private FavoriteManager favoriteManager;
	private Favorite favorite;
	private List<Item> listItems = new ArrayList<Item>();
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
	private List<Definition> mBitStreamList = new ArrayList<Definition>();
	static {
		DEFINITION_NAMES = new HashMap<Definition, String>();
		DEFINITION_NAMES.put(Definition.DEFINITON_HIGH, "高清");

		DEFINITION_NAMES.put(Definition.DEFINITON_720P, "720P");
		DEFINITION_NAMES.put(Definition.DEFINITON_720P_DOLBY, "杜比720P");
		DEFINITION_NAMES.put(Definition.DEFINITON_720P_H265, "H265_720P");

		DEFINITION_NAMES.put(Definition.DEFINITON_1080P, "1080P");
		DEFINITION_NAMES.put(Definition.DEFINITON_1080P_DOLBY, "杜比1080P");
		DEFINITION_NAMES.put(Definition.DEFINITON_1080P_H265, "H265_1080P");

		DEFINITION_NAMES.put(Definition.DEFINITON_4K, "4K");
		DEFINITION_NAMES.put(Definition.DEFINITON_4K_DOLBY, "杜比4K");
		DEFINITION_NAMES.put(Definition.DEFINITON_4K_H265, "H265_4K");
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.vod_player);
        initView();
	}

	public void initView() {
		panelShowAnimation = AnimationUtils.loadAnimation(this,
				R.drawable.fly_up);
		panelHideAnimation = AnimationUtils.loadAnimation(this,
				R.drawable.fly_down);
		// bufferHideAnimation =
		// AnimationUtils.loadAnimation(this,R.drawable.fade_out);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		setContentView(R.layout.vod_player);
		mVideoView = (IsmatvVideoView) findViewById(R.id.video_view);
		mVideoView.setVisibility(View.GONE);
		panelLayout = (LinearLayout) findViewById(R.id.PanelLayout);
		titleText = (TextView) findViewById(R.id.TitleText);
		qualityText = (TextView) findViewById(R.id.QualityText);
		timeText = (TextView) findViewById(R.id.TimeText);
		timeBar = (SeekBar) findViewById(R.id.TimeSeekBar);
		timeBar.setOnSeekBarChangeListener(new SeekBarChangeEvent());
		playPauseImage = (ImageView) findViewById(R.id.PlayPauseImage);
		ffImage = (ImageView) findViewById(R.id.FFImage);
		fbImage = (ImageView) findViewById(R.id.FBImage);
		bufferLayout = (LinearLayout) findViewById(R.id.BufferLayout);
		bufferText = (TextView) findViewById(R.id.BufferText);
		logoImage = (ImageView) findViewById(R.id.logo_image);
		panelLayout.setVisibility(View.GONE);
		bufferLayout.setVisibility(View.GONE);
		qualityText.setVisibility(View.GONE);
		am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		mRootLayout = (RelativeLayout) findViewById(R.id.RootRelativeLayout);
		playPauseImage.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent keycode) {
				// TODO Auto-generated method stub
				switch (keycode.getAction()) {
				case MotionEvent.ACTION_DOWN:
					if (!paused) {

					} else {

					}
					break;

				default:
					break;
				}
				return false;
			}
		});
		fbImage.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent keycode) {
				// TODO Auto-generated method stub
				switch (keycode.getAction()) {
				case MotionEvent.ACTION_DOWN:
					if (mVideoView.getDuration() > 0 && !live_video) {

					}
					break;

				default:
					break;
				}
				return false;
			}
		});
		ffImage.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent keycode) {
				// TODO Auto-generated method stub
				switch (keycode.getAction()) {
				case MotionEvent.ACTION_DOWN:
					if (mVideoView.getDuration() > 0 && !live_video) {

					}
					break;

				default:
					break;
				}
				return false;
			}
		});
		DaisyUtils.getVodApplication(this).addActivityToPool(
		this.toString(), this);
		bundle = getIntent().getExtras();
		item = (Item) bundle.get("item");
		if(item!=null){
			Log.i("qq123", "pk");
		}
		showBuffer();
		initQiyiVideoPlayer();
	}

	public void initQiyiVideoPlayer() {
		FrameLayout.LayoutParams flParams = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER);
		FrameLayout frameContainer = (FrameLayout) findViewById(R.id.fl_videoview_container);
		frameContainer.setVisibility(View.VISIBLE);
		mPlayer = QiyiVideoPlayer.createVideoPlayer(this, frameContainer,
				flParams, /* bundle */null, mVideoStateListener);
		String info =  (String) bundle.get("iqiyi");
		mPlayer.setVideo(AccessProxy.getQiYiInfo(info));
		mPlayer.start();
	}

	private IVideoStateListener mVideoStateListener = new IVideoStateListener() {

		@Override
		public void onAdEnd() {
			// TODO Auto-generated method stub

		}

		@Override
		public void onAdStart() {
			// TODO Auto-generated method stub

		}

		@Override
		public void onBitStreamListReady(final List<Definition> definitionList) {
			// TODO Auto-generated method stub
			mBitStreamList = definitionList;
		}

		@Override
		public void onBufferEnd() {
			// TODO Auto-generated method stub
			isBuffer = false;
			hideBuffer();
		}

		@Override
		public void onBufferStart() {
			// TODO Auto-generated method stub
			isBuffer = true;
			showBuffer();
		}

		@Override
		public boolean onError(IPlaybackInfo arg0, int arg1, String arg2,
				String arg3) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void onHeaderTailerInfoReady(int arg0, int arg1) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onMovieComplete() {
			// TODO Auto-generated method stub

		}

		@Override
		public void onMoviePause() {
			// TODO Auto-generated method stub

		}

		@Override
		public void onMovieStart() {
			// TODO Auto-generated method stub
			isBuffer = false;
			hideBuffer();
			showPanel();
			mHandler.removeMessages(MSG_PLAY_TIME);
			mHandler.sendEmptyMessage(MSG_PLAY_TIME);
		}

		@Override
		public void onMovieStop() {
			// TODO Auto-generated method stub

		}

		@Override
		public void onPlaybackBitStreamSelected(final Definition definition) {
			// TODO Auto-generated method stub
			mHandler.post(new Runnable() {
                @Override
                public void run() {
                	qualityText.setVisibility(View.VISIBLE);
                	qualityText.setText(definition.name());
                }
            });
		}

		@Override
		public void onPrepared() {
			// TODO Auto-generated method stub
            timeBar.setMax(mPlayer.getDuration());
		}

		@Override
		public void onSeekComplete() {
			// TODO Auto-generated method stub

		}

		@Override
		public void onVideoSizeChange(int arg0, int arg1) {
			// TODO Auto-generated method stub

		}

	};

	private void showBuffer() {
		if (isBuffer && !bufferLayout.isShown()) {
			bufferLayout.setVisibility(View.VISIBLE);
			bufferDuration = System.currentTimeMillis();
		}
	}

	private void hideBuffer() {
		if (!isBuffer && bufferLayout.isShown()) {
			bufferText.setText(BUFFERING);
			bufferLayout.setVisibility(View.GONE);
		}
	}

	private Handler mHandler = new Handler(Looper.getMainLooper()) {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_AD_COUNTDOWN:
				sendEmptyMessageDelayed(MSG_AD_COUNTDOWN, 1000);
				break;
			case MSG_PLAY_TIME:
				String playTime;
				int curPos = mPlayer.getCurrentPosition();
				int duration = mPlayer.getDuration();
				playTime = getPlaybackTimeString(curPos);
				playTime += " / ";
				playTime += getPlaybackTimeString(duration);
				timeText.setText(playTime);
				timeBar.setMax(duration);
				int secondaryProgress = mPlayer.getCachePercent() * duration / 100;
				timeBar.setProgress(curPos);
				timeBar.setSecondaryProgress(secondaryProgress);
				if(Math.abs(secondaryProgress-curPos)!=0){
					isBuffer = false;
					hideBuffer();
				}
				else{
					isBuffer = true;
					showBuffer();
				}
				sendEmptyMessageDelayed(MSG_PLAY_TIME, 1000);
				if (LogUtils.mIsDebug)
					LogUtils.d(TAG,
							"MSG_PLAY_TIME: isPlaying=" + mPlayer.isPlaying());
				
			default:
				break;
			}
		}
	};

	class SeekBarChangeEvent implements SeekBar.OnSeekBarChangeListener {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {

		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			Log.d(TAG, "onStartTrackingTouch" + seekBar.getProgress());
			if (!live_video) {

			}

		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			Log.d(TAG, "onStopTrackingTouch" + seekBar.getProgress());
			if (!live_video) {

			}

		}
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
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

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	private void releasePlayer() {
		mHandler.removeCallbacksAndMessages(null);
		if (mPlayer != null) {
			mPlayer.releasePlayer();
		}
		mPlayer = null;
	}

	private static String getPlaybackTimeString(int timeInMs) {
		int second = timeInMs / 1000;
		int minute = second / 60;
		if (minute > 0) {
			second %= 60;
		}
		int hour = minute / 60;
		if (hour > 0) {
			minute %= 60;
		}

		String hourStr = String.format("%02d", hour);
		String minStr = String.format("%02d", minute);
		String secStr = String.format("%02d", second);
		String ret = hourStr + ":" + minStr + ":" + secStr;
		if (LogUtils.mIsDebug)
			LogUtils.d("1", "getPlaybackTimeString(" + timeInMs + "): hour="
					+ hour + ", minute=" + minute + ", second=" + second
					+ ", result=" + ret);
		return ret;
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
			panelShow = false;
		}
	}
	private void showPanel() {
//		if (isVodMenuVisible())
//			return;
		if (!panelShow) {
			panelLayout.startAnimation(panelShowAnimation);
			panelLayout.setVisibility(View.VISIBLE);
			panelShow = true;
			//hidePanelHandler.postDelayed(hidePanelRunnable, 20000);
		}

	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		boolean ret = false;
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if (panelShow) {
				hidePanel();
				ret = true;
			} else {
				showPopupDialog(
						DIALOG_OK_CANCEL,
						getResources().getString(
								R.string.vod_player_exit_dialog));
				ret = true;
			}
			break;

		default:
			break;
		}
		return ret;
	}
	public void showPopupDialog(int type, String msg) {
		if (type == DIALOG_OK_CANCEL) {
			popupDlg = new Dialog(this, R.style.PopupDialog);
			View view;
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.popup_2btn, null);
			int width = getResources().getDimensionPixelSize(
					R.dimen.popup_dialog_width);
			int height = getResources().getDimensionPixelSize(
					R.dimen.popup_dialog_height);

			popupDlg.addContentView(view, new ViewGroup.LayoutParams(width,
					height));
			TextView tv = (TextView) view.findViewById(R.id.PopupText);
			tv.setText(msg);
			Button btn1 = null, btn2 = null;
			btn1 = (Button) view.findViewById(R.id.LeftButton);
			btn1.setText(R.string.vod_ok);
			btn2 = (Button) view.findViewById(R.id.RightButton);
			btn2.setText(R.string.vod_cancel);
			if (btn1 != null) {
				btn1.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						if (popupDlg != null && mPlayer != null) {
							//addHistory(seekPostion);
							//checkTaskPause();
							//timeTaskPause();
							popupDlg.dismiss();					
							releasePlayer();
							finish();
						}
					};
				});
			}

			if (btn2 != null) {
				btn2.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						if (popupDlg != null) {
							popupDlg.dismiss();
						}
					};
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
}
