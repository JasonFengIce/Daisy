package tv.ismar.daisy;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.VodUserAgent;
import tv.ismar.daisy.models.Clip;
import tv.ismar.daisy.models.Favorite;
import tv.ismar.daisy.models.History;
import tv.ismar.daisy.models.Item;
import tv.ismar.daisy.models.Quality;
import tv.ismar.daisy.persistence.FavoriteManager;
import tv.ismar.daisy.persistence.HistoryManager;
import tv.ismar.daisy.player.ISTVVodMenu;
import tv.ismar.daisy.player.ISTVVodMenuItem;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import com.ismartv.api.t.AccessProxy;
import com.ismartv.bean.ClipInfo;
import com.qiyi.video.player.IVideoStateListener;
import com.qiyi.video.player.QiyiVideoPlayer;
import com.qiyi.video.player.data.Definition;
import com.qiyi.video.player.data.IPlaybackInfo;
import com.qiyi.video.utils.LogUtils;

public class QiYiPlayActivity extends VodMenuAction {
	private static final int MSG_AD_COUNTDOWN = 100;
	private static final int MSG_PLAY_TIME = 101;
	private static final int SEEK_STEP = 30000;
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
	private int currQuality = 0;// 0: normal 1: 720P 2:1080P
	private Animation panelShowAnimation;
	private Animation panelHideAnimation;
	private Definition currentDefinition;
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
	private Dialog popupDlg = null;
	private InputStream logoInputStream;
	private HistoryManager historyManager;
	private History mHistory;
	private SimpleRestClient simpleRestClient;
	private String itemUrl;
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
	// private CallaPlay callaPlay = new CallaPlay();
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
	private boolean[] avalibleRate = { false, false, false };
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
		DaisyUtils.getVodApplication(this).addActivityToPool(this.toString(),
				this);
		bundle = getIntent().getExtras();
		item = (Item) bundle.get("item");
		clip = item.clip;
		live_video = item.live_video;
		itemUrl = item.clip.url;
		titleText.setText(item.title);
		showBuffer();
		// new initPlayTask().execute();
		initQiyiVideoPlayer();
	}

	private class initPlayTask extends AsyncTask<String, Void, Void> {

		@Override
		protected void onPostExecute(Void result) {
			// initPlayer();
		}

		@Override
		protected Void doInBackground(String... params) {
			try {
				if (item != null) {
					if (item.item_pk != item.pk) {
						currNum = item.position;
						itemUrl = SimpleRestClient.root_url + "/api/item/"
								+ item.item_pk + "/";
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	public void initQiyiVideoPlayer() {
		FrameLayout.LayoutParams flParams = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER);
		FrameLayout frameContainer = (FrameLayout) findViewById(R.id.fl_videoview_container);
		frameContainer.setVisibility(View.VISIBLE);
		mPlayer = QiyiVideoPlayer.createVideoPlayer(this, frameContainer,
				flParams, /* bundle */null, mVideoStateListener);
		String info = (String) bundle.get("iqiyi");
		favoriteManager = DaisyUtils.getFavoriteManager(this);
		historyManager = DaisyUtils.getHistoryManager(this);
		mHistory = historyManager.getHistoryByUrl(itemUrl);
		favorite = favoriteManager.getFavoriteByUrl(itemUrl);
		if (mHistory != null) {
			isContinue = mHistory.is_continue;
			tempOffset = (int) mHistory.last_position;
		}
		if (tempOffset > 0 && isContinue) {
			currPosition = tempOffset;
			seekPostion = tempOffset;
		} else {
			currPosition = 0;
			seekPostion = 0;
		}
		mPlayer.setVideo(AccessProxy.getQiYiInfo(info));
		mPlayer.start();
	}

	private IVideoStateListener mVideoStateListener = new IVideoStateListener() {

		@Override
		public void onAdEnd() {

		}

		@Override
		public void onAdStart() {

		}

		@Override
		public void onBitStreamListReady(final List<Definition> definitionList) {
			mBitStreamList = definitionList;
			for (Definition d : definitionList) {
				if (d.equals(Definition.DEFINITON_HIGH)) {
					avalibleRate[0] = true;
				} else if (d.equals(Definition.DEFINITON_720P)) {
					avalibleRate[1] = true;
				} else if (d.equals(Definition.DEFINITON_1080P)) {
					avalibleRate[2] = true;
				}
			}
		}

		@Override
		public void onBufferEnd() {
			isBuffer = false;
			hideBuffer();
			checkTaskStart();
		}

		@Override
		public void onBufferStart() {
			isBuffer = true;
			showBuffer();
		}

		@Override
		public boolean onError(IPlaybackInfo arg0, int arg1, String arg2,
				String arg3) {
			return false;
		}

		@Override
		public void onHeaderTailerInfoReady(int arg0, int arg1) {
		}

		@Override
		public void onMovieComplete() {
		}

		@Override
		public void onMoviePause() {
		}

		@Override
		public void onMovieStart() {
			isBuffer = false;
			if (seekPostion > 0)
				mPlayer.seekTo(seekPostion);
			hideBuffer();
			showPanel();
			timeTaskStart();
			mHandler.removeMessages(MSG_PLAY_TIME);
			mHandler.sendEmptyMessage(MSG_PLAY_TIME);
		}

		@Override
		public void onMovieStop() {

		}

		@Override
		public void onPlaybackBitStreamSelected(final Definition definition) {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					qualityText.setVisibility(View.VISIBLE);
					qualityText.setText(DEFINITION_NAMES.get(definition));
				}
			});
			currentDefinition = definition;
		}

		@Override
		public void onPrepared() {
			timeBar.setMax(mPlayer.getDuration());
		}

		@Override
		public void onSeekComplete() {
			timeTaskStart();
			hideBuffer();
		}

		@Override
		public void onVideoSizeChange(int arg0, int arg1) {
		}

	};

	private boolean isVodMenuVisible() {
		if (menu == null) {
			return false;
		}
		return menu.isVisible();
	}

	private void showBuffer() {
		if (!isBuffer && !bufferLayout.isShown()) {
			bufferLayout.setVisibility(View.VISIBLE);
			bufferDuration = System.currentTimeMillis();
			isBuffer = true;
		}
	}

	private void hideBuffer() {
		if (isBuffer && bufferLayout.isShown()) {
			bufferText.setText(BUFFERING);
			bufferLayout.setVisibility(View.GONE);
			isBuffer = false;
			isSeekBuffer = false;
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
				int secondaryProgress = mPlayer.getCachePercent() * duration
						/ 100;
				timeBar.setProgress(curPos);
				timeBar.setSecondaryProgress(secondaryProgress);
				// if (Math.abs(secondaryProgress - curPos) != 0) {
				// hideBuffer();
				// } else {
				// showBuffer();
				// }
				sendEmptyMessageDelayed(MSG_PLAY_TIME, 1000);
				if (LogUtils.mIsDebug)
					LogUtils.d(TAG,
							"MSG_PLAY_TIME: isPlaying=" + mPlayer.isPlaying());

			default:
				break;
			}
		}
	};

	public boolean onVodMenuOpened(ISTVVodMenu menu) {

		if (avalibleRate[0]) {
			menu.findItem(1).enable();
		} else {
			menu.findItem(1).disable();
		}
		if (avalibleRate[1]) {
			menu.findItem(2).enable();
		} else {
			menu.findItem(2).disable();
		}
		if (avalibleRate[2]) {
			menu.findItem(3).enable();
		} else {
			menu.findItem(3).disable();
		}
		menu.findItem(4).disable();
		if (panelShow) {
			hidePanel();
		}

		if (isContinue) {
			menu.findItem(8).select();
			menu.findItem(9).unselect();
		} else {
			menu.findItem(8).unselect();
			menu.findItem(9).select();
		}

		return true;
	}

	class SeekBarChangeEvent implements SeekBar.OnSeekBarChangeListener {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			if (!live_video) {
				if (mPlayer.getDuration() > 0) {
					timeBar.setProgress(progress);
				}

				updataTimeText();

			}
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
		super.onResume();
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
		try {
			addHistory(seekPostion);
			checkTaskPause();
			timeTaskPause();
			removeAllHandler();
			mPlayer.stop();
		} catch (Exception e) {
			Log.d(TAG, "Player close to Home");
		}
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
		if (isVodMenuVisible())
			return;
		if (!panelShow) {
			panelLayout.startAnimation(panelShowAnimation);
			panelLayout.setVisibility(View.VISIBLE);
			panelShow = true;
			hidePanelHandler.postDelayed(hidePanelRunnable, 20000);
		}

	}

	private void pauseItem() {
		if (paused)
			return;
		// //showBuffer();
		Log.d(TAG, "pause");
		hideBuffer();
		if (mPlayer.isPlaying())
			mPlayer.pause();
		paused = true;
		// if (subItem != null)
		// callaPlay.videoPlayPause(item.pk, subItem.pk, item.title, clip.pk,
		// currQuality, 0, currPosition, sid);
		// else
		// callaPlay.videoPlayPause(item.pk, null, item.title, clip.pk,
		// currQuality, 0, currPosition, sid);

	}

	private void timeTaskStart() {
		mHandler.removeCallbacks(mUpdateTimeTask);
		mHandler.post(mUpdateTimeTask);

	}

	private void timeTaskPause() {
		mHandler.removeCallbacks(mUpdateTimeTask);
	}

	private Runnable mUpdateTimeTask = new Runnable() {
		@Override
		public void run() {
			if (mPlayer != null) {
				if (mPlayer.isPlaying()) {
					seekPostion = mPlayer.getCurrentPosition();
				}
				mHandler.postDelayed(mUpdateTimeTask, 500);
			} else {
				Log.d(TAG, "mVideoView ======= null or err");
				timeTaskPause();
			}
		}
	};

	private void resumeItem() {
		if (!paused)
			return;
		hideBuffer();
		Log.d(TAG, "resume");
		mPlayer.start();
		// if (subItem != null)
		// callaPlay.videoPlayContinue(item.pk, subItem.pk, item.title,
		// clip.pk, currQuality, 0, currPosition, sid);
		// else
		// callaPlay.videoPlayContinue(item.pk, null, item.title, clip.pk,
		// currQuality, 0, currPosition, sid);
		if (!isBuffer) {
			timeTaskStart();
		}
		paused = false;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		boolean ret = false;
		if (!isVodMenuVisible()) {
			switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_LEFT:
				if (!live_video) {
					fbImage.setImageResource(R.drawable.vodplayer_controller_rew);
					showPanel();
					showBuffer();
					mPlayer.seek(-SEEK_STEP);
					// if (subItem != null)
					// callaPlay.videoPlaySeek(item.pk, subItem.pk,
					// item.title, clip.pk, currQuality, 0,
					// currPosition, sid);
					// else
					// callaPlay.videoPlayContinue(item.pk, null, item.title,
					// clip.pk, currQuality, 0, currPosition, sid);
					isSeekBuffer = true;
					ret = true;
					isSeek = false;
					offsets = 0;
					offn = 1;
				}

				break;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				if (!live_video) {
					ffImage.setImageResource(R.drawable.vodplayer_controller_ffd);
					showPanel();
					showBuffer();
					mPlayer.seek(SEEK_STEP);
					// if (subItem != null)
					// callaPlay.videoPlaySeek(item.pk, subItem.pk,
					// item.title, clip.pk, currQuality, 0,
					// currPosition, sid);
					// else
					// callaPlay.videoPlayContinue(item.pk, null, item.title,
					// clip.pk, currQuality, 0, currPosition, sid);
					isSeekBuffer = true;
					ret = true;
					isSeek = false;
					offsets = 0;
					offn = 1;
				}

				break;
			case KeyEvent.KEYCODE_DPAD_CENTER:
			case KeyEvent.KEYCODE_ENTER:
				if (paused) {
					resumeItem();
					playPauseImage
							.setImageResource(R.drawable.vodplayer_controller_play);
				} else {
					pauseItem();
					playPauseImage
							.setImageResource(R.drawable.vodplayer_controller_pause);
				}
				ret = true;
				break;
			case KeyEvent.KEYCODE_MENU:
				if (menu != null && menu.isVisible())
					return false;
				if (menu == null) {
					createWindow();
					menu = new ISTVVodMenu(this);
					ret = createMenu(menu);
				}
				if (itemUrl != null && favoriteManager != null
						&& favoriteManager.getFavoriteByUrl(itemUrl) != null) {
					menu.findItem(5)
							.setTitle(
									getResources()
											.getString(
													R.string.vod_bookmark_remove_bookmark_setting));
				}
				if (onVodMenuOpened(menu)) {
					menu.show();
					hideMenuHandler.postDelayed(hideMenuRunnable, 10000);
				}
				break;
			default:
				break;
			}

			if (ret == false) {
				ret = super.onKeyUp(keyCode, event);
			}
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
							addHistory(seekPostion);
							checkTaskPause();
							timeTaskPause();
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

		sub = menu.addSubMenu(0,
				getResources().getString(R.string.vod_player_quality_setting));
		sub.addItem(1,
				getResources().getString(R.string.vod_player_quality_medium));
		sub.addItem(2,
				getResources().getString(R.string.vod_player_quality_high));
		sub.addItem(3,
				getResources().getString(R.string.vod_player_quality_ultra));
		sub.addItem(4,
				getResources().getString(R.string.vod_player_quality_adaptive));
		if (itemUrl != null && favoriteManager != null
				&& favoriteManager.getFavoriteByUrl(itemUrl) == null) {
			menu.addItem(
					5,
					getResources().getString(
							R.string.vod_player_bookmark_setting));
		} else {
			menu.addItem(
					5,
					getResources().getString(
							R.string.vod_bookmark_remove_bookmark_setting));
		}
		menu.addItem(6,
				getResources().getString(R.string.vod_player_related_setting));

		sub = menu.addSubMenu(7,
				getResources().getString(R.string.vod_player_continue_setting));
		sub.addItem(8, getResources()
				.getString(R.string.vod_player_continue_on));
		sub.addItem(9,
				getResources().getString(R.string.vod_player_continue_off));
		return true;
	}

	private void updataTimeText() {
		String text = getTimeString(currPosition) + "/"
				+ getTimeString(clipLength);
		timeText.setText(text);
	}

	private void initQualtiyText() {
		switch (currQuality) {
		case 0:
			qualityText
					.setBackgroundResource(R.drawable.vodplayer_stream_normal);
			break;
		case 1:
			qualityText.setBackgroundResource(R.drawable.vodplayer_stream_high);
			break;
		case 2:
			qualityText
					.setBackgroundResource(R.drawable.vodplayer_stream_ultra);
			break;
		case 3:
			qualityText.setText("自适应");
			qualityText.setBackgroundResource(R.drawable.rounded_edittext);
			break;
		default:
			qualityText
					.setBackgroundResource(R.drawable.vodplayer_stream_normal);
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
					playPauseImage
							.setImageResource(R.drawable.vod_player_pause);
					playPauseImage
							.setImageResource(R.drawable.vodplayer_controller_pause);
					isBuffer = true;
					currQuality = pos;
					if (id == 1) {
						mPlayer.switchBitStream(Definition.DEFINITON_HIGH);
					}
					if (id == 2) {
						mPlayer.switchBitStream(Definition.DEFINITON_720P);
					}
					if (id == 3) {
						mPlayer.switchBitStream(Definition.DEFINITON_1080P);
					}
					// if (subItem != null)
					// callaPlay.videoSwitchStream(item.pk, subItem.pk,
					// item.title, clip.pk, currQuality, "manual",
					// null, null, mediaip, sid);
					// else
					// callaPlay.videoSwitchStream(item.pk, null, item.title,
					// clip.pk, currQuality, "manual", null, null,
					// mediaip, sid);
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
			if (itemUrl != null && favoriteManager != null
					&& favoriteManager.getFavoriteByUrl(itemUrl) != null) {
				favoriteManager.deleteFavoriteByUrl(itemUrl);
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
					favoriteManager.addFavorite(favorite);
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
		return true;
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
			history.title = item.title;
			history.adlet_url = item.adlet_url;
			history.content_model = item.content_model;
			history.is_complex = item.is_complex;
			history.last_position = last_position;
			history.last_quality = currQuality;
			history.url = itemUrl;
			history.is_continue = isContinue;
			historyManager.addHistory(history);
		}
	}

	private Runnable checkStatus = new Runnable() {
		public void run() {
			if (mPlayer.isPlaying()
					&& Math.abs(mPlayer.getCurrentPosition() - seekPostion) > 0) {
				if (isBuffer || bufferLayout.isShown()) {
					isBuffer = false;
					hideBuffer();
				}
				if (live_video && (isBuffer || bufferLayout.isShown())) {
					isBuffer = false;
					hideBuffer();
				}
				bufferText.setText(BUFFERING);
				if (!isSeek && !isBuffer && !live_video) {
					currPosition = mPlayer.getCurrentPosition();
					timeBar.setProgress(currPosition);
				}
			} else {
				if (!paused && !isBuffer) {
					seekPostion = mPlayer.getCurrentPosition();
				}
			}
			mCheckHandler.postDelayed(checkStatus, 300);
		}

	};

	private void checkTaskStart() {
		mCheckHandler.removeCallbacks(checkStatus);
		mCheckHandler.post(checkStatus);
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

}
