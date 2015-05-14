package tv.ismar.daisy;

import static tv.ismar.daisy.DramaListActivity.ORDER_CHECK_BASE_URL;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.EventProperty;
import tv.ismar.daisy.core.ImageUtils;
import tv.ismar.daisy.core.NetworkUtils;
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
import tv.ismar.daisy.views.IsmatvVideoView;
import tv.ismar.daisy.views.MarqueeView;
import tv.ismar.daisy.views.PaymentDialog;
import tv.ismar.player.SmartPlayer;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
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
import android.widget.SeekBar;
import android.widget.TextView;

import com.ismartv.api.t.AccessProxy;
import com.ismartv.bean.ClipInfo;

public class PlayerActivity extends VodMenuAction {

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

	private boolean paused = false;
	private boolean isSeekBuffer = false;
	private boolean panelShow = false;
	private int currQuality = 0;
	private String urls[] = new String[6];
	private Animation panelShowAnimation;
	private Animation panelHideAnimation;
	// private Animation bufferHideAnimation;
	private ImageView logoImage;
	private LinearLayout panelLayout;
	private MarqueeView titleText;
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setView();

		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		int height = metric.heightPixels; // 屏幕高度（像素）
		int densityDpi = metric.densityDpi; // 屏幕密度DPI（120 / 160 / 240）
	}

	@Override
	public void onResume() {
		super.onResume();
		if (needOnresume) {
			isBuffer = true;
			showBuffer();
			new initPlayTask().execute();
			needOnresume = false;
		}
	}

	private void setView() {
		panelShowAnimation = AnimationUtils.loadAnimation(this,
				R.drawable.fly_up);
		panelHideAnimation = AnimationUtils.loadAnimation(this,
				R.drawable.fly_down);
		// bufferHideAnimation =
		// AnimationUtils.loadAnimation(this,R.drawable.fade_out);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		setContentView(R.layout.vod_player);

		mVideoView = (IsmatvVideoView) findViewById(R.id.video_view);
		panelLayout = (LinearLayout) findViewById(R.id.PanelLayout);
		titleText = (MarqueeView) findViewById(R.id.TitleText);

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
					if (mVideoView.getDuration() > 0 && !live_video) {
						isSeek = true;
						showPanel();
						isBuffer = true;
						showBuffer();
						fastBackward(SHORT_STEP);
						mVideoView.seekTo(currPosition);
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
					if (mVideoView.getDuration() > 0 && !live_video) {
						isSeek = true;
						showPanel();
						isBuffer = true;
						showBuffer();
						fastForward(SHORT_STEP);
						mVideoView.seekTo(currPosition);
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

		mVideoView.setOnHoverListener(onhoverlistener);
		mVideoView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (isVodMenuVisible()) {
					hideMenuHandler.post(hideMenuRunnable);
				} else {
					if (!paused) {
						pauseItem();
						playPauseImage
								.setImageResource(R.drawable.vod_playbtn_selector);
					} else {
						resumeItem();
						playPauseImage
								.setImageResource(R.drawable.vod_pausebtn_selector);
					}
				}
				return false;
			}
		});
		mVideoView.setOnPreparedListener(new SmartPlayer.OnPreparedListener() {
			@Override
			public void onPrepared(SmartPlayer mp) {

				Log.d(TAG, "mVideoView onPrepared tempOffset ==" + tempOffset);
				if (mVideoView != null) {
					if (live_video) {
						timeBar.setEnabled(false);

					} else {
						clipLength = mVideoView.getDuration();
						timeBar.setMax(clipLength);
						mp.seekTo(currPosition);
						timeBar.setProgress(currPosition);
						timeBar.setEnabled(true);
					}
					if (currPosition == 0)
						mp.start();
					timeTaskStart(0);
					checkTaskStart(0);
					urls[0] = urlInfo.getNormal();
					urls[1] = urlInfo.getMedium();
					urls[2] = urlInfo.getHigh();
					urls[3] = urlInfo.getAdaptive();
					if (subItem != null) {
						callaPlay.videoPlayLoad(
								item.pk,
								subItem.pk,
								item.title,
								clip.pk,
								currQuality,
								(System.currentTimeMillis() - startDuration) / 1000,
								0, null, sid);
						callaPlay.videoPlayStart(item.pk, subItem.pk,
								item.title, clip.pk, currQuality, 0);
					} else {
						callaPlay.videoPlayLoad(
								item.pk,
								null,
								item.title,
								clip.pk,
								currQuality,
								(System.currentTimeMillis() - startDuration) / 1000,
								0, null, sid);
						callaPlay.videoPlayStart(item.pk, null, item.title,
								clip.pk, currQuality, 0);
					}
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
				.setOnCompletionListener(new SmartPlayer.OnCompletionListener() {

					@Override
					public void onCompletion(SmartPlayer mp) {
						Log.d(TAG, "mVideoView  Completion");
						if (item.isPreview) {
							mVideoView.stopPlayback();
							PaymentDialog dialog = new PaymentDialog(
									PlayerActivity.this, R.style.PaymentDialog,
									ordercheckListener);
							item.model_name = "item";
							dialog.setItem(item);
							dialog.show();
						} else
							gotoFinishPage();
					}
				});

		mVideoView
				.setOnSeekCompleteListener(new SmartPlayer.OnSeekCompleteListener() {

					@Override
					public void onSeekComplete(SmartPlayer mp) {
						// TODO Auto-generated method stub
						if (!mp.isPlaying())
							mp.start();
						if (paused) {
							mp.pause();
						}
						isBuffer = false;
						isSeek = false;
						hideBuffer();
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
				Log.v("aaaa", "i =" + i + ",.,.j=" + j);
				if (i == SmartPlayer.MEDIA_INFO_BUFFERING_START) {
					isBuffer = true;
					bufferText.setText(BUFFERING + " " + 0 + "%");
					showBuffer();
				} else if (i == 704) {
					bufferText.setText(BUFFERING + " " + j + "%");
				} else if (i == SmartPlayer.MEDIA_INFO_BUFFERING_END) {
					bufferText.setText(BUFFERING + " " + 100 + "%");
					isBuffer = false;
					hideBuffer();
				}
				return false;
			}
		});
		initClipInfo();
	}

	private void initClipInfo() {
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
			clip = item.clip;
			live_video = item.live_video;
			isPreview = item.isPreview;
			// use to get mUrl, and registerActivity
			DaisyUtils.getVodApplication(this).addActivityToPool(
					this.toString(), this);
			// *********************
			// new ItemByUrlTask().execute();
			String info = bundle.getString("ismartv");
			urlInfo = AccessProxy.getIsmartvClipInfo(info);
			new initPlayTask().execute();
		}
	}

	private class initPlayTask extends AsyncTask<String, Void, Void> {

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			initPlayer();
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
					if (item.expense != null) {
						orderCheck();
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
				initPlayer();
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
					currNum = item.position;
					if (item != null) {
						clip = item.clip;
						urlInfo = AccessProxy.parse(SimpleRestClient.root_url
								+ "/api/clip/" + clip.pk + "/",
								VodUserAgent.getAccessToken(sn),
								PlayerActivity.this);
						if (urlInfo.getIqiyi_4_0().length() > 0) {
							Intent intent = new Intent();
							intent.setAction("tv.ismar.daisy.qiyiPlay");
							intent.putExtra("iqiyi", urlInfo.getIqiyi_4_0());
							intent.putExtra("item", item);
							startActivity(intent);
							PlayerActivity.this.finish();
							return null;
						}
					}
				} else {
					obj = bundle.get("item");
					if (obj != null) {
						item = (Item) obj;
						live_video = item.live_video;
						if (item.clip != null) {
							clip = item.clip;
							urlInfo = AccessProxy.parse(
									SimpleRestClient.root_url + "/api/clip/"
											+ clip.pk + "/",
									VodUserAgent.getAccessToken(sn),
									PlayerActivity.this);
						}
					} else {
						Log.e(TAG, "init player bundle item and url is null");
					}
				}
				// ////////////////////////////////////////
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
						item = simpleRestClient.getItem(itemUrl);
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
		try {
			if (urlInfo != null) {
				urls[0] = urlInfo.getNormal();
				urls[1] = urlInfo.getMedium();
				urls[2] = urlInfo.getHigh();
				urls[3] = urlInfo.getAdaptive();
				favoriteManager = DaisyUtils.getFavoriteManager(this);
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
					if (subItem != null && subItem.item_pk != subItem.pk) {
						mHistory = historyManager.getHistoryByUrl(itemUrl);
						if (SimpleRestClient.isLogin()) {
							favorite = favoriteManager.getFavoriteByUrl(
									itemUrl, "yes");
						} else {
							favorite = favoriteManager.getFavoriteByUrl(
									itemUrl, "no");
						}
						titleText.setText(subItem.title);
					} else {
						mHistory = historyManager.getHistoryByUrl(itemUrl);
						if (SimpleRestClient.isLogin()) {
							favorite = favoriteManager.getFavoriteByUrl(
									itemUrl, "yes");
						} else {
							favorite = favoriteManager.getFavoriteByUrl(
									itemUrl, "no");
						}
						titleText.setText(item.title);
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
							tempOffset = 0;
							isContinue = true;
						}
					}
					initQualtiyText();
					qualityText.setVisibility(View.VISIBLE);
					if (tempOffset > 0 && isContinue == true && !live_video) {
						bufferText.setText("  " + BUFFERCONTINUE
								+ getTimeString(tempOffset));
					} else {
						bufferText.setText(PlAYSTART + "《"
								+ titleText.getText() + "》");
					}
					new LogoImageTask().execute();
				}

				if (tempOffset > 0 && isContinue) {
					currPosition = tempOffset;
					seekPostion = tempOffset;
				} else {
					currPosition = 0;
					seekPostion = 0;
				}

				Log.d(TAG, "RES_INT_OFFSET currPosition=" + currPosition);
				if (urls != null && mVideoView != null) {
					TaskStart();// cmstest.tvxio.com
					sid = VodUserAgent.getSid(urls[currQuality]);
					mediaip = VodUserAgent.getMediaIp(urls[currQuality]);
					mVideoView.setVideoPath(urls[currQuality]);
					ismedialplayerinit = false;
				}

			} else {
				ExToClosePlayer("url", "m3u8 content error ,");
			}
		} catch (Exception e) {
			Log.e(TAG, e.toString());
			ExToClosePlayer("url", e.toString());
		}
	}

	private Handler logHandler = new Handler();

	private void TaskStart() {
		logHandler.removeCallbacks(logTaskRunnable);
		logHandler.post(logTaskRunnable);
	}

	private void timeTaskStop() {
		logHandler.removeCallbacks(logTaskRunnable);
	}

	private Runnable logTaskRunnable = new Runnable() {
		@Override
		public void run() {
			try {
				startDuration = System.currentTimeMillis();
				if (subItem != null)
					callaPlay.videoStart(item, subItem.pk, subItem.title,
							currQuality, null, 0, mSection, sid);
				else
					callaPlay.videoStart(item, null, item.title, currQuality,
							null, 0, mSection, sid);
				timeTaskStop();
			} catch (Exception e) {
				Log.e(TAG, " Sender log videoPlayStart " + e.toString());
			}
		}
	};

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
					if (live_video && (isBuffer || bufferLayout.isShown())) {
						isBuffer = false;
						hideBuffer();
					}
					if (mVideoView.getAlpha() < 1) {
						mVideoView.setAlpha(1);
						bufferText.setText(BUFFERING);
					}
					if (!isSeek && !isBuffer && !live_video) {
						currPosition = mVideoView.getCurrentPosition();
						timeBar.setProgress(currPosition);
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
		if (mVideoView != null) {
			if (listItems != null && listItems.size() > 0
					&& currNum < (listItems.size() - 1)) {
				subItem = listItems.get(currNum + 1);
				subItemUrl = simpleRestClient.root_url + "/api/subitem/"
						+ subItem.pk + "/";
				bundle.remove("url");
				bundle.putString("url", subItemUrl);
				addHistory(0);
				if (mVideoView != null) {
					mVideoView.setAlpha(0);
				}
				checkContinueOrPay(subItem.pk);
			} else {
				Intent intent = new Intent("tv.ismar.daisy.PlayFinished");
				intent.putExtra("item", item);
				startActivity(intent);
				seekPostion = 0;
				currPosition = 0;
				mVideoView = null;
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
										(System.currentTimeMillis() - startDuration) / 1000,
										mSection, sid, "list",
										item.content_model);
				} catch (Exception e) {
					Log.e(TAG, " log Sender videoExit end " + e.toString());
				}
				PlayerActivity.this.finish();

			}
		}

	}

	private void checkContinueOrPay(int pk) {
		if (payedItemspk.contains(pk) || item.expense == null) {
			isBuffer = true;
			showBuffer();
			new ItemByUrlTask().execute();
		} else {
			for (Item i : listItems) {
				if (i.pk == pk) {
					subItem = i;
					break;
				}
			}
			mVideoView.stopPlayback();
			PaymentDialog dialog = new PaymentDialog(PlayerActivity.this,
					R.style.PaymentDialog, ordercheckListener);
			item.model_name = "subitem";
			item.pk = pk;
			item.title = subItem.title;
			dialog.setItem(item);
			dialog.show();
		}
	}

	private void gotoRelatePage() {
		Intent intent = new Intent();
		intent.setClass(PlayerActivity.this,
				tv.ismar.daisy.RelatedActivity.class);
		intent.putExtra("item", item);
		startActivity(intent);
		addHistory(seekPostion);
		checkTaskPause();
		timeTaskPause();
		mVideoView.stopPlayback();
		try {
			if (subItem != null)
				callaPlay.videoExit(item.pk, subItem.pk, item.title, clip.pk,
						currQuality, 0, "relate", currPosition,
						(System.currentTimeMillis() - startDuration) / 1000,
						mSection, sid, "list", item.content_model);
			else
				callaPlay.videoExit(item.pk, null, item.title, clip.pk,
						currQuality, 0, "relate", currPosition,
						(System.currentTimeMillis() - startDuration) / 1000,
						mSection, sid, "list", item.content_model);
			mVideoView.stopPlayback();
		} catch (Exception e) {
			Log.e(TAG, "log Sender videoExit relate " + e.toString());
		}

		PlayerActivity.this.finish();
	}

	private void addHistory(int last_position) {
		if (item != null && historyManager != null) {
			Log.d(TAG, "historyManager title ==" + item.title);
			Log.d(TAG, "historyManager itemUrl ==" + itemUrl);
			Log.d(TAG, "historyManager subItemUrl ==" + subItemUrl);
			Log.d(TAG, "historyManager last_position ==" + last_position);
			Log.d(TAG, "historyManager isContinue ==" + isContinue);
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
			params = params + "&subitem=" + subItem.item_pk;
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
		if (isVodMenuVisible())
			return;
		if (!panelShow) {
			panelLayout.startAnimation(panelShowAnimation);
			panelLayout.setVisibility(View.VISIBLE);
			panelShow = true;
			hidePanelHandler.postDelayed(hidePanelRunnable, 3000);
		} else {
			hidePanelHandler.removeCallbacks(hidePanelRunnable);
			hidePanelHandler.postDelayed(hidePanelRunnable, 3000);
		}

	}

	private void hidePanel() {
		if (panelShow) {
			panelLayout.startAnimation(panelHideAnimation);
			panelLayout.setVisibility(View.GONE);
			panelShow = false;
		}
	}

	private void pauseItem() {
		if (paused || mVideoView == null)
			return;
		// //showBuffer();
		Log.d(TAG, "pause");
		hideBuffer();
		mVideoView.pause();
		paused = true;
		if (subItem != null)
			callaPlay.videoPlayPause(item.pk, subItem.pk, item.title, clip.pk,
					currQuality, 0, currPosition, sid);
		else
			callaPlay.videoPlayPause(item.pk, null, item.title, clip.pk,
					currQuality, 0, currPosition, sid);

	}

	private void resumeItem() {
		if (!paused || mVideoView == null)
			return;
		// hideBuffer();
		Log.d(TAG, "resume");
		mVideoView.start();

		if (subItem != null)
			callaPlay.videoPlayContinue(item.pk, subItem.pk, item.title,
					clip.pk, currQuality, 0, currPosition, sid);
		else
			callaPlay.videoPlayContinue(item.pk, null, item.title, clip.pk,
					currQuality, 0, currPosition, sid);
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
		if (!isVodMenuVisible() && mVideoView != null) {
			switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_LEFT:
				mHandler.removeCallbacks(mUpdateTimeTask);
				mHandler.removeCallbacks(checkStatus);
				if (mVideoView.getDuration() > 0 && !live_video) {
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
				mHandler.removeCallbacks(mUpdateTimeTask);
				mHandler.removeCallbacks(checkStatus);
				if (mVideoView.getDuration() > 0 && !live_video) {
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
				if (mVideoView.getDuration() > 0) {
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
				if (!paused) {
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
				String net = "";
				if (SimpleRestClient.isLogin()) {
					net = "yes";
				} else {
					net = "no";
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
		if (mVideoView != null && ret == false) {
			ret = super.onKeyDown(keyCode, event);
		}
		return ret;
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
								.setImageResource(R.drawable.vod_pausebtn_selector);
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
						if (popupDlg != null && mVideoView != null) {
							addHistory(seekPostion);
							checkTaskPause();
							timeTaskPause();
							popupDlg.dismiss();
							try {
								if (subItem != null)
									callaPlay.videoExit(
											item.pk,
											subItem.pk,
											item.title,
											clip.pk,
											currQuality,
											0,
											"detail",
											currPosition,
											(System.currentTimeMillis() - startDuration) / 1000,
											mSection, sid, "list",
											item.content_model);
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
											(System.currentTimeMillis() - startDuration) / 1000,
											mSection, sid, "list",
											item.content_model);
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
										.setImageResource(R.drawable.vod_pausebtn_selector);
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
		if (isBuffer && !bufferLayout.isShown()) {
			bufferLayout.setVisibility(View.VISIBLE);
			bufferDuration = System.currentTimeMillis();
		}
		mHandler.sendEmptyMessageDelayed(BUFFER_COUNTDOWN_ACTION, 1000);
	}

	protected void hideBuffer() {
		if (mHandler.hasMessages(BUFFER_COUNTDOWN_ACTION)) {
			mHandler.removeMessages(BUFFER_COUNTDOWN_ACTION);
			buffercountDown = 0;
		}
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
										item.title,
										clip.pk,
										currQuality,
										0,
										currPosition,
										(System.currentTimeMillis() - bufferDuration) / 1000,
										mediaip, sid);

					} else {
						callaPlay
								.videoPlayBlockend(
										item.pk,
										subItem.pk,
										item.title,
										clip.pk,
										currQuality,
										0,
										currPosition,
										(System.currentTimeMillis() - bufferDuration) / 1000,
										mediaip, sid);
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
									(System.currentTimeMillis() - bufferDuration) / 1000,
									mediaip, sid);

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
									(System.currentTimeMillis() - bufferDuration) / 1000,
									mediaip, sid);
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
				if (subItem != null)
					callaPlay.videoPlaySeek(item.pk, subItem.pk, item.title,
							clip.pk, currQuality, 0, currPosition, sid);
				else
					callaPlay.videoPlayContinue(item.pk, null, item.title,
							clip.pk, currQuality, 0, currPosition, sid);
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
				getResources().getString(R.string.vod_player_quality_medium));
		sub.addItem(2,
				getResources().getString(R.string.vod_player_quality_high));
		sub.addItem(3,
				getResources().getString(R.string.vod_player_quality_ultra));
		sub.addItem(4,
				getResources().getString(R.string.vod_player_quality_adaptive));
		menu.addItem(20, getResources().getString(R.string.kefucentertitle));
		menu.addItem(30, getResources().getString(R.string.playfromstarttitle));

		return true;
	}

	public boolean onVodMenuOpened(ISTVVodMenu menu) {
		for (int i = 0; i < 4; i++) {
			ISTVVodMenuItem item;
			item = menu.findItem(i + 1);
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

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			if (!live_video) {
				if (mVideoView.getDuration() > 0) {
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
				mVideoView.seekTo(seekBar.getProgress());
				isBuffer = false;
				isSeekBuffer = true;
				isSeek = false;
				offsets = 0;
				offn = 1;
				hideBuffer();
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
							.setImageResource(R.drawable.vod_pausebtn_selector);
					isBuffer = true;
					showBuffer();
					currQuality = pos;
					// mVideoView = (IsmatvVideoView)
					// findViewById(R.id.video_view);
					mVideoView.setVideoPath(urls[currQuality]);
					historyManager.addOrUpdateQuality(new Quality(0,
							urls[currQuality], currQuality));
					mediaip = VodUserAgent.getMediaIp(urls[currQuality]);
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
			subItemUrl = simpleRestClient.root_url + "/api/subitem/" + id + "/";
			bundle.remove("url");
			bundle.putString("url", subItemUrl);
			addHistory(0);
			if (mVideoView != null) {
				mVideoView.setAlpha(0);
			}

			checkContinueOrPay(id);
		}

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
			// qualityText.setText("流畅");
			qualityText
					.setBackgroundResource(R.drawable.vodplayer_stream_normal);
			break;
		case 1:
			// qualityText.setText("高清");
			qualityText.setBackgroundResource(R.drawable.vodplayer_stream_high);
			break;
		case 2:
			// qualityText.setText("超清");
			qualityText
					.setBackgroundResource(R.drawable.vodplayer_stream_ultra);
			break;

		case 3:
			qualityText.setText("自适应");
			qualityText.setBackgroundResource(R.drawable.rounded_edittext);
			break;

		default:
			// qualityText.setText("流畅");
			qualityText
					.setBackgroundResource(R.drawable.vodplayer_stream_normal);
			break;
		}

	}

	@Override
	protected void onPause() {
		try {
			createHistory(seekPostion);
			addHistory(seekPostion);
			checkTaskPause();
			timeTaskPause();
			removeAllHandler();
			mVideoView.stopPlayback();
		} catch (Exception e) {
			Log.d(TAG, "Player close to Home");
		}
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		menu = null;
		urlInfo = null;
		mCheckHandler = null;
		historyManager = null;
		simpleRestClient = null;
		favoriteManager = null;
		DaisyUtils.getVodApplication(this).removeActivtyFromPool(
				this.toString());
		sendPlayComplete();
		super.onDestroy();
	}

	private void removeAllHandler() {

		mHandler.removeCallbacks(mUpdateTimeTask);
		mHandler.removeCallbacks(finishPlayerActivity);
		hideMenuHandler.removeCallbacks(hideMenuRunnable);
		mHandler.removeCallbacks(mUpdateTimeTask);
		mCheckHandler.removeCallbacks(checkStatus);
		hidePanelHandler.removeCallbacks(hidePanelRunnable);
		mHandler.removeCallbacksAndMessages(null);
	}

	private void sendPlayComplete() {
		Intent intent = new Intent();
		intent.setAction(AppConstant.VOD_PLAYER_COMPLETE_ACTION);
		sendBroadcast(intent);
	}

	private PaymentDialog.OrderResultListener ordercheckListener = new PaymentDialog.OrderResultListener() {

		@Override
		public void payResult(boolean result) {
			if (item.subitems != null && item.expense != null) {
				if (result) {
					isBuffer = true;
					seekPostion = 0;
					currPosition = 0;
					tempOffset = 0;
					showBuffer();
					payedItemspk.add(item.pk);
					new ItemByUrlTask().execute();
				} else {
					PlayerActivity.this.finish();
				}
			} else {
				if (result) {
					if (mHistory != null) {
						mHistory.last_position = item.preview.length * 1000;
					} else {
						tempOffset = item.preview.length * 1000;
					}
					paystatus = true;
					new ItemByUrlTask().execute();
				} else {
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
				showPanel();
				break;
			}
			return false;
		}

	};

	boolean needOnresume = false;

	private void startSakura() {
        Intent intent = new Intent();
        intent.setAction("cn.ismar.sakura.launcher");
        startActivity(intent);
	}

	private void orderCheck() {
		SimpleRestClient client = new SimpleRestClient();
		String typePara = "&item=" + item.pk;
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
			} else {
				try {
					JSONArray array = new JSONArray(info);
					for (int i = 0; i < array.length(); i++) {
						JSONObject seria = array.getJSONObject(i);
						int pk = seria.getInt("object_pk");
						payedItemspk.add(pk);
					}
				} catch (JSONException e) {
					for (Item i : listItems) {
						payedItemspk.add(i.pk);
					}
				}
			}
		}

		@Override
		public void onFailed(String error) {

		}
	};
}