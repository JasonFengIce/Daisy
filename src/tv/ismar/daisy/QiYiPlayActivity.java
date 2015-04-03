package tv.ismar.daisy;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.SimpleRestClient.HttpPostRequestInterface;
import tv.ismar.daisy.core.VodUserAgent;
import tv.ismar.daisy.models.Clip;
import tv.ismar.daisy.models.Favorite;
import tv.ismar.daisy.models.History;
import tv.ismar.daisy.models.Item;
import tv.ismar.daisy.models.Quality;
import tv.ismar.daisy.persistence.FavoriteManager;
import tv.ismar.daisy.persistence.HistoryManager;
import tv.ismar.daisy.player.CallaPlay;
import tv.ismar.daisy.player.ISTVVodMenu;
import tv.ismar.daisy.player.ISTVVodMenuItem;
import tv.ismar.daisy.qiyimediaplayer.SdkVideo;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnHoverListener;
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
import com.ismartv.api.t.AccessProxy;
import com.ismartv.bean.ClipInfo;
import com.qiyi.video.player.IVideoStateListener;
import com.qiyi.video.player.QiyiVideoPlayer;
import com.qiyi.video.player.data.Definition;
import com.qiyi.video.player.data.IPlaybackInfo;

public class QiYiPlayActivity extends VodMenuAction {
	private static final int MSG_AD_COUNTDOWN = 100;
	private static final int MSG_PLAY_TIME = 101;
	private static final int MSG_INITQUALITYTITLE = 102;
	private static final int SEEK_STEP = 30000;
	private static final int SHORT_STEP = 1;
	private static final HashMap<Definition, String> DEFINITION_NAMES;
	private QiyiVideoPlayer mPlayer;
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
		playPauseImage.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent keycode) {
				// TODO Auto-generated method stub
				switch (keycode.getAction()) {
				case MotionEvent.ACTION_DOWN:
					// if (mVideoView.getDuration() > 0) {
					if (!paused) {
						pauseItem();
						playPauseImage
								.setImageResource(R.drawable.vod_playbtn_selector);
					} else {
						resumeItem();
						playPauseImage
								.setImageResource(R.drawable.vod_pausebtn_selector);
					}

					// }
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
					if (mPlayer.getDuration() > 0 && !live_video) {
						isSeek = true;
						showPanel();
//						fbImage.setImageResource(R.drawable.vod_controlb_selector);
						isBuffer = true;
						showBuffer();
						mPlayer.seek(-SEEK_STEP);
						isSeekBuffer = true;
						Log.d(TAG, "LEFT seek to "
								+ getTimeString(currPosition));
						isSeek = false;
						offsets = 0;
						offn = 1;
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
					if (mPlayer.getDuration() > 0 && !live_video) {
						isSeek = true;
						showPanel();
//						ffImage.setImageResource(R.drawable.vod_controlf_selector);
						isBuffer = true;
						showBuffer();
						mPlayer.seek(SEEK_STEP);
						isSeekBuffer = true;
						Log.d(TAG, "RIGHT seek to"
								+ getTimeString(currPosition));
						isSeek = false;
						offsets = 0;
						offn = 1;
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
		clip = item.clip;
		live_video = item.live_video;
		itemUrl = item.clip.url;
		titleText.setText(item.title);
		simpleRestClient = new SimpleRestClient();
		new initPlayTask().execute();
		initQiyiVideoPlayer();
	}

	private class initPlayTask extends AsyncTask<String, Void, Void> {

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			// initPlayer();
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
				setQiyiVideo();
			} else {
				// ExToClosePlayer("url"," m3u8 quality is null ,or get m3u8 err");
			}
		}

		@Override
		protected ClipInfo doInBackground(String... params) {
			Object obj = bundle.get("url");
			Log.d(TAG, "init player bundle url === " + obj);
			String sn = VodUserAgent.getMACAddress();
			AccessProxy.init(VodUserAgent.deviceType,
					VodUserAgent.deviceVersion, sn);
			try {
				if (obj != null) {
					item = simpleRestClient.getItem((String) obj);
					if (item != null) {
						clip = item.clip;
						urlInfo = AccessProxy.parse(SimpleRestClient.root_url
								+ "/api/clip/" + clip.pk + "/",
								VodUserAgent.getAccessToken(sn),
								QiYiPlayActivity.this);
						if (urlInfo.getIqiyi_4_0().length() == 0) {
							Intent intent = new Intent();
							intent.setAction("tv.ismar.daisy.Play");
							intent.putExtra("item", item);
							intent.putExtra("ismartv",
									AccessProxy.getvVideoClipInfo());
							startActivity(intent);
							QiYiPlayActivity.this.finish();
							return null;
						} else {
							bundle.putString("iqiyi", urlInfo.getIqiyi_4_0());
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
		FrameLayout frameContainer = (FrameLayout) findViewById(R.id.fl_videoview_container);
		frameContainer.setVisibility(View.VISIBLE);
		frameContainer.setOnHoverListener(onhoverlistener);
		frameContainer.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (!paused) {
					pauseItem();
					playPauseImage
							.setImageResource(R.drawable.vod_playbtn_selector);
				} else {
					resumeItem();
					playPauseImage
							.setImageResource(R.drawable.vod_pausebtn_selector);
				}
				return false;
			}
		});
		mPlayer = QiyiVideoPlayer.createVideoPlayer(this, frameContainer,
				flParams, /* bundle */null, mVideoStateListener);
		favoriteManager = DaisyUtils.getFavoriteManager(this);
		historyManager = DaisyUtils.getHistoryManager(this);
		mHistory = historyManager.getHistoryByUrl(itemUrl);
		if (SimpleRestClient.isLogin()) {
			favorite = favoriteManager.getFavoriteByUrl(itemUrl, "yes");
		} else {
			favorite = favoriteManager.getFavoriteByUrl(itemUrl, "no");
		}

		Quality quality = historyManager.getQuality();
		if (quality != null) {
			currQuality = quality.quality;
		}
		if (mHistory != null) {
			isContinue = mHistory.is_continue;
			tempOffset = (int) mHistory.last_position;
		}

		setQiyiVideo();
	}

	private void setQiyiVideo() {
		titleText.setText(item.title);
		if (tempOffset > 0 && isContinue == true && !live_video) {
			bufferText.setText("  " + BUFFERCONTINUE
					+ getTimeString(tempOffset));
		} else {
			bufferText.setText(PlAYSTART + "《" + titleText.getText() + "》");
		}
		String info = (String) bundle.get("iqiyi");
		showBuffer();
		if (tempOffset > 0 && isContinue) {
			currPosition = tempOffset;
			seekPostion = tempOffset;
		} else {
			currPosition = 0;
			seekPostion = 0;
		}
		if (info != null && info.contains("iqiyi_4_0")) {
			mPlayer.setVideo(AccessProxy.getQiYiInfo(info));
		} else {
			String[] array = info.split(":");
			SdkVideo qiyiInfo = new SdkVideo(array[0], array[1], array[2],
					Definition.DEFINITON_1080P);
			mPlayer.setVideo(qiyiInfo);
		}
		mPlayer.start(seekPostion);
		initQualtiyText();
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
					// currQuality = 0;
				} else if (d.equals(Definition.DEFINITON_720P)) {
					avalibleRate[1] = true;
					// currQuality = 1;
				} else if (d.equals(Definition.DEFINITON_1080P)) {
					avalibleRate[2] = true;
					// currQuality = 2;
				}
			}
		}

		@Override
		public void onBufferEnd() {
			hideBuffer();
			checkTaskStart();
		}

		@Override
		public void onBufferStart() {
		}

		@Override
		public boolean onError(IPlaybackInfo arg0, int arg1, String arg2,
				String arg3) {
			addHistory(currPosition);
			ExToClosePlayer("error", arg1 + " " + arg2);
			return false;
		}

		@Override
		public void onHeaderTailerInfoReady(int arg0, int arg1) {
		}

		@Override
		public void onMovieComplete() {
			gotoFinishPage();
		}

		@Override
		public void onMoviePause() {
		}

		@Override
		public void onMovieStart() {
			clipLength = mPlayer.getDuration();
//			if (seekPostion > 0)
//				mPlayer.seek(seekPostion);
			showPanel();
			timeTaskStart();
			checkTaskStart();
			if (mHandler.hasMessages(MSG_PLAY_TIME))
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
				}
			});
			currentDefinition = definition;
			if (currentDefinition == Definition.DEFINITON_HIGH) {
				currQuality = 0;
			} else if (currentDefinition == Definition.DEFINITON_720P) {
				currQuality = 1;
			} else {
				currQuality = 2;
			}
			// if (mHandler.hasMessages(MSG_INITQUALITYTITLE))
			mHandler.removeMessages(MSG_INITQUALITYTITLE);
			mHandler.sendEmptyMessage(MSG_INITQUALITYTITLE);
		}

		@Override
		public void onPrepared() {
			timeBar.setMax(mPlayer.getDuration());
		}

		@Override
		public void onSeekComplete() {
			// timeTaskStart();
			checkTaskStart();
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
		if (isBuffer && !bufferLayout.isShown()) {
			bufferLayout.setVisibility(View.VISIBLE);
			bufferDuration = System.currentTimeMillis();
		}
	}

	private void hideBuffer() {
		if (!isBuffer && bufferLayout.isShown()) {
			bufferText.setText(BUFFERING);
			bufferLayout.setVisibility(View.GONE);
			isBuffer = false;
		}
	}

	private Handler mHandler = new Handler(Looper.getMainLooper()) {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_AD_COUNTDOWN:
				sendEmptyMessageDelayed(MSG_AD_COUNTDOWN, 1000);
				break;
			case MSG_PLAY_TIME:
				updataTimeText();
				break;
			case MSG_INITQUALITYTITLE:
				initQualtiyText();
				break;
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

		return true;
	}

	class SeekBarChangeEvent implements SeekBar.OnSeekBarChangeListener {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			if (!live_video) {
				if (mPlayer.getDuration() > 0) {
					timeBar.setProgress(progress);
					Log.d(TAG, "LEFT seek to " + getTimeString(currPosition));
				}
				updataTimeText();
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
			}

		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if(needOnresume){
			initQiyiVideoPlayer();
			needOnresume = false;
		}
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
			createHistory(seekPostion);
			addHistory(seekPostion);
			checkTaskPause();
			timeTaskPause();
			removeAllHandler();
			mPlayer.stop();
			mPlayer = null;
		} catch (Exception e) {
			Log.d(TAG, "Player close to Home");
		}
		super.onPause();
	}

	private void releasePlayer() {
		mHandler.removeCallbacksAndMessages(null);
		if (mPlayer != null) {
			mPlayer.stop();
			mPlayer.releasePlayer();
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
			hidePanelHandler.postDelayed(hidePanelRunnable, 3000);
		}else{
			hidePanelHandler.removeCallbacks(hidePanelRunnable);
			hidePanelHandler.postDelayed(hidePanelRunnable, 3000);
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
		if (subItem != null)
			callaPlay.videoPlayPause(item.pk, subItem.pk, item.title, clip.pk,
					currQuality, 0, currPosition, sid);
		else
			callaPlay.videoPlayPause(item.pk, null, item.title, clip.pk,
					currQuality, 0, currPosition, sid);

	}

	private void timeTaskStart() {
		mHandler.removeCallbacks(mUpdateTimeTask);
		mHandler.post(mUpdateTimeTask);

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
								item.pk, subItem.pk, item.title, clip.pk,
								currQuality, 0);
					else
						callaPlay.videoExcept("noplayaddress", content,
								item.pk, null, item.title, clip.pk,
								currQuality, 0);
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
								item.pk, subItem.pk, item.title, clip.pk,
								currQuality, currPosition);
					else
						callaPlay.videoExcept("mediaexception", content,
								item.pk, null, item.title, clip.pk,
								currQuality, currPosition);
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
				if (mPlayer.isPlaying()) {
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

	private void resumeItem() {
		if (!paused)
			return;
		hideBuffer();
		Log.d(TAG, "resume");
		mPlayer.start();
		if (subItem != null)
			callaPlay.videoPlayContinue(item.pk, subItem.pk, item.title,
					clip.pk, currQuality, 0, currPosition, sid);
		else
			callaPlay.videoPlayContinue(item.pk, null, item.title, clip.pk,
					currQuality, 0, currPosition, sid);
		if (!isBuffer) {
			timeTaskStart();
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
		if (!isVodMenuVisible() && mPlayer != null) {
			switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_LEFT:
				if (mPlayer.getDuration() > 0 && !live_video) {
					isSeek = true;
					showPanel();
					isBuffer = true;
					showBuffer();
					fastBackward(SHORT_STEP);
					ret = true;
				}
				break;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				if (mPlayer.getDuration() > 0 && !live_video) {
					isSeek = true;
					showPanel();
					isBuffer = true;
					showBuffer();
					fastForward(SHORT_STEP);
					ret = true;
				}
				break;
			case KeyEvent.KEYCODE_DPAD_CENTER:
			case KeyEvent.KEYCODE_ENTER:
				if (mPlayer.getDuration() > 0) {
					showPanel();
					if (!paused) {
						pauseItem();
						playPauseImage
								.setImageResource(R.drawable.vod_playbtn_selector);
					} else {
						resumeItem();
						playPauseImage
								.setImageResource(R.drawable.vod_pausebtn_selector);
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

				am.adjustStreamVolume(AudioManager.STREAM_MUSIC,
						AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
				showPanel();
				ret = true;

				break;
			case KeyEvent.KEYCODE_DPAD_DOWN:
				am.adjustStreamVolume(AudioManager.STREAM_MUSIC,
						AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
				ret = true;
				break;
			case KeyEvent.KEYCODE_BACK:
					showPopupDialog(
							DIALOG_OK_CANCEL,
							getResources().getString(
									R.string.vod_player_exit_dialog));
					ret = true;
					if(!paused){
					pauseItem();
					playPauseImage
							.setImageResource(R.drawable.vod_playbtn_selector);
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

			default:
				break;
			}
		}
		if (mPlayer != null && ret == false) {
			ret = super.onKeyDown(keyCode, event);
		}
		return ret;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		boolean ret = false;
		if (mPlayer != null && !isVodMenuVisible()
				&& mPlayer.getDuration() > 0) {
			switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_LEFT:
				if (!live_video) {
//					fbImage.setImageResource(R.drawable.vodplayer_controller_rew);
					mPlayer.seekTo(currPosition);
					if (subItem != null)
						callaPlay.videoPlaySeek(item.pk, subItem.pk,
								item.title, clip.pk, currQuality, 0,
								currPosition, sid);
					else
						callaPlay.videoPlayContinue(item.pk, null, item.title,
								clip.pk, currQuality, 0, currPosition, sid);
					isSeekBuffer = true;
					Log.d(TAG, "LEFT seek to " + getTimeString(currPosition));
					ret = true;
					isSeek = false;
					offsets = 0;
					offn = 1;
				}

				break;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				if (!live_video) {
//					ffImage.setImageResource(R.drawable.vodplayer_controller_ffd);
					mPlayer.seekTo(currPosition);
					if (subItem != null)
						callaPlay.videoPlaySeek(item.pk, subItem.pk,
								item.title, clip.pk, currQuality, 0,
								currPosition, sid);
					else
						callaPlay.videoPlayContinue(item.pk, null, item.title,
								clip.pk, currQuality, 0, currPosition, sid);
					isSeekBuffer = true;
					Log.d(TAG, "RIGHT seek to" + getTimeString(currPosition));
					ret = true;
					isSeek = false;
					offsets = 0;
					offn = 1;
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
							if(paused){
							resumeItem();
							playPauseImage
									.setImageResource(R.drawable.vod_pausebtn_selector);
							}
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
		sub.addItem(1,
				getResources().getString(R.string.vod_player_quality_medium),
				true, currQuality == 0);
		sub.addItem(2,
				getResources().getString(R.string.vod_player_quality_high),
				true, currQuality == 1);
		sub.addItem(3,
				getResources().getString(R.string.vod_player_quality_ultra),
				true, currQuality == 2);
		sub.addItem(4,
				getResources().getString(R.string.vod_player_quality_adaptive));
		menu.addItem(20, getResources().getString(R.string.kefucentertitle));
		menu.addItem(30, getResources().getString(R.string.playfromstarttitle));

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
					isBuffer = true;
					playPauseImage
					.setImageResource(R.drawable.vod_pausebtn_selector);
					currQuality = pos;
					if (currQuality == 0) {
						mPlayer.switchBitStream(Definition.DEFINITON_HIGH);
					} else if (currQuality == 1) {
						mPlayer.switchBitStream(Definition.DEFINITON_720P);
					} else {
						mPlayer.switchBitStream(Definition.DEFINITON_1080P);
					}
					// historyManager.addOrUpdateQuality(new Quality(0,
					// urls[currQuality], currQuality));
					mediaip = "127.0.0.1";
					if (subItem != null)
						callaPlay.videoSwitchStream(item.pk, subItem.pk,
								item.title, clip.pk, currQuality, "manual",
								null, null, mediaip, sid);
					else
						callaPlay.videoSwitchStream(item.pk, null, item.title,
								clip.pk, currQuality, "manual", null, null,
								mediaip, sid);
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
			subItemUrl = simpleRestClient.root_url + "/api/subitem/" + id + "/";
			bundle.remove("url");
			bundle.putString("url", subItemUrl);
			addHistory(0);
			isBuffer = true;
			showBuffer();
			new ItemByUrlTask().execute();
		}

		return true;
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
			historyManager.addHistory(history);
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

	private void gotoFinishPage() {
		timeTaskPause();
		checkTaskPause();
		if (mPlayer != null) {
			if (listItems != null && listItems.size() > 0
					&& currNum < (listItems.size() - 1)) {
				subItem = listItems.get(currNum + 1);
				subItemUrl = simpleRestClient.root_url + "/api/subitem/"
						+ subItem.pk + "/";
				bundle.remove("url");
				bundle.putString("url", subItemUrl);
				addHistory(0);
				isBuffer = true;
				showBuffer();
				currPosition = 0;
				new ItemByUrlTask().execute();
			} else {
				Intent intent = new Intent("tv.ismar.daisy.PlayFinished");
				intent.putExtra("item", item);
				startActivity(intent);
				seekPostion = 0;
				currPosition = 0;
				mPlayer.stop();
				mPlayer.releasePlayer();
				// mPlayer = null;
				addHistory(0);
				try {
					if (subItem != null)
						callaPlay
								.videoExit(
										item.pk,
										subItem.pk,
										item.title,
										clip.pk,
										currQuality,
										0,
										"end",
										currPosition,
										(System.currentTimeMillis() - startDuration) / 1000,
										mSection, sid, "list",
										item.content_model);// String
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
										(System.currentTimeMillis() - startDuration) / 1000,
										mSection, sid, "list",
										item.content_model);
				} catch (Exception e) {
					Log.e(TAG, " log Sender videoExit end " + e.toString());
				}
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
				showPanel();
//				fbImage.setImageResource(R.drawable.vodplayer_controller_rew_pressed);
				break;
			}
			return false;
		}

	};
	boolean needOnresume = false;
	   private void startSakura(){
	        if (AppConstant.DEBUG)
	            Log.d(TAG, "install vod service invoke...");
	        needOnresume = true;
	        try {
	          ApplicationInfo applicationInfo =  getPackageManager().getApplicationInfo(
	                    "cn.ismartv.speedtester", 0);
	            if(null!= applicationInfo){
	                Intent intent = new Intent();
	                intent.setClassName("cn.ismartv.speedtester", "cn.ismartv.speedtester.ui.activity.MenuActivity");
	                startActivity(intent);
	            }
	        } catch (PackageManager.NameNotFoundException e) {
	            Uri uri = Uri.parse("file://" + getFileStreamPath("Sakura.apk").getAbsolutePath());
	            Intent intent = new Intent(Intent.ACTION_VIEW);
	            intent.setDataAndType(uri, "application/vnd.android.package-archive");
	            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	            startActivity(intent);
	        }
	    }
}
