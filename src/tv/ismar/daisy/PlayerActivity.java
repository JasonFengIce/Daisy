package tv.ismar.daisy;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.ImageUtils;
import tv.ismar.daisy.core.NetworkUtils;
import tv.ismar.daisy.core.SimpleRestClient;
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
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
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
import android.widget.VideoView;

import com.ismartv.api.AccessProxy;
import com.ismartv.bean.ClipInfo;

public class PlayerActivity extends Activity {

	@SuppressWarnings("unused")
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
	private Handler mHandler = new Handler();
	private Handler mCheckHandler = new Handler();
	private int tempOffset = 0;
	private Item item;
	private Item subItem;
	private Clip clip;
	private Bundle bundle;
	private SeekBar timeBar;
	private VideoView mVideoView;
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
	private CallaPlay callaPlay = new CallaPlay();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setView();
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
		mVideoView = (VideoView) findViewById(R.id.video_view);
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
			bundle = intent.getExtras();
			// use to get mUrl, and registerActivity
			DaisyUtils.getVodApplication(this).addActivityToPool(
					this.toString(), this);
			// *********************
			new ItemByUrlTask().execute();
		}
	}

	// 初始化播放地址url
	private class ItemByUrlTask extends AsyncTask<String, Void, ClipInfo> {

		@Override
		protected void onPostExecute(ClipInfo result) {
			if (result != null) {
				initPlayer();
			} else {
				ExToClosePlayer("url");
			}
		}

		@Override
		protected ClipInfo doInBackground(String... arg0) {
			Object obj = bundle.get("url");
			Log.d(TAG, "init player bundle url === " + obj);
			String sn = VodUserAgent.getMACAddress();
			AccessProxy.init(VodUserAgent.deviceType,VodUserAgent.deviceVersion, sn);
			try {
				if (obj != null) {
					item = simpleRestClient.getItem((String) obj);
					if (item != null) {
						clip = item.clip;

						// try {
						// host = (new URL((String)obj)).getHost();
						// } catch (MalformedURLException e) {
						// e.printStackTrace();
						// }http://127.0.0.1:21098/cord
						urlInfo = AccessProxy.parse(simpleRestClient.root_url
								+ "/api/clip/" + clip.pk + "/",
								VodUserAgent.getAccessToken(sn),
								PlayerActivity.this);
					}
				} else {
					obj = bundle.get("item");
					if (obj != null) {
						item = (Item) obj;
						if (item.clip != null) {
							clip = item.clip;

							// try {
							// host = (new URL((String)obj)).getHost();
							// } catch (MalformedURLException e) {
							// e.printStackTrace();
							// }http://127.0.0.1:21098/cord
							urlInfo = AccessProxy.parse(
									simpleRestClient.root_url + "/api/clip/"
											+ clip.pk + "/",
									VodUserAgent.getAccessToken(sn),
									PlayerActivity.this);
						}
					} else {
						Log.e(TAG, "init player bundle item and url is null");
					}
				}
				if (item != null) {
					if (item.item_pk != item.pk) {
						currNum = item.position;
						Log.d(TAG, "currNum ===" + currNum);
						subItemUrl = simpleRestClient.root_url
								+ "/api/subitem/" + item.pk + "/";
						subItem = simpleRestClient.getItem(subItemUrl);
						itemUrl = simpleRestClient.root_url + "/api/item/"
								+ item.item_pk + "/";
						item = simpleRestClient.getItem(itemUrl);
						if (item != null && item.subitems != null) {
							
							listItems = new ArrayList<Item>();
							
							for (int i = 0; i < item.subitems.length; i++) {
								listItems.add(item.subitems[i]);
							}
						}
					} else {
						itemUrl = simpleRestClient.root_url + "/api/item/"
								+ item.item_pk + "/";
						item = simpleRestClient.getItem(itemUrl);
					}

				}
			} catch (Exception e) {
				Log.e(TAG, e.toString());
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
						favorite = favoriteManager.getFavoriteByUrl(itemUrl);
						titleText.setText(subItem.title);
					} else {
						mHistory = historyManager.getHistoryByUrl(itemUrl);
						favorite = favoriteManager.getFavoriteByUrl(itemUrl);
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
						} else {
							if (urls[mHistory.last_quality] != null
									&& !urls[mHistory.last_quality].isEmpty()) {
								currQuality = mHistory.last_quality;
							}
							tempOffset = 0;
							isContinue = mHistory.is_continue;
						}
					} else {
						tempOffset = 0;
						isContinue = true;
					}
					initQualtiyText();
					qualityText.setVisibility(View.VISIBLE);
					titleText.setSelected(true);
					if (tempOffset > 0 && isContinue == true) {
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
					mVideoView.setVideoPath(urls[currQuality]);
					mVideoView
							.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
								@Override
								public void onPrepared(MediaPlayer mp) {

									Log.d(TAG,
											"mVideoView onPrepared tempOffset =="
													+ tempOffset);
									if (mVideoView != null) {
										clipLength = mVideoView.getDuration();
										// bufferText.setText("");
										timeBar.setMax(clipLength);
										mVideoView.start();
										mVideoView.seekTo(currPosition);
										timeBar.setProgress(currPosition);
										timeTaskStart();
										checkTaskStart();

										urls[0] = urlInfo.getNormal();
										urls[1] = urlInfo.getMedium();
										urls[2] = urlInfo.getHigh();
										urls[3] = urlInfo.getAdaptive();
									}
								}
							});
					mVideoView
							.setOnErrorListener(new MediaPlayer.OnErrorListener() {
								@Override
								public boolean onError(MediaPlayer mp,
										int what, int extra) {
									Log.d(TAG,
											"mVideoView  Error setVideoPath urls[currQuality] ");
									addHistory(currPosition);
									ExToClosePlayer("error");
									return false;
								}
							});

					mVideoView
							.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

								@Override
								public void onCompletion(MediaPlayer mp) {
									Log.d(TAG, "mVideoView  Completion");
									gotoFinishPage();
								}
							});

					TaskStart();
				}

			} else {
				ExToClosePlayer("url");
			}
		} catch (Exception e) {
			Log.e(TAG, e.toString());
			ExToClosePlayer("url");
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
				if (subItem != null)
					callaPlay.videoPlayStart(item.pk, subItem.pk, item.title,
							clip.pk, currQuality, 0);
				else
					callaPlay.videoPlayStart(item.pk, null, item.title,
							clip.pk, currQuality, 0);
				timeTaskStop();
			} catch (Exception e) {
				Log.e(TAG, " Sender log videoPlayStart " + e.toString());
			}
		}
	};

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

	private void ExToClosePlayer(String content) {
		if (bufferText != null) {
			bufferText.setText(EXTOCLOSE);
			if (content == "url") {
				try {
					if (subItem != null)
						callaPlay
								.videoExcept("noplayaddress", item.pk,
										subItem.pk, item.title, clip.pk,
										currQuality, 0);
					else
						callaPlay.videoExcept("noplayaddress", item.pk, null,
								item.title, clip.pk, currQuality, 0);
				} catch (Exception e) {
					Log.e(TAG,
							" Sender log videoExcept noplayaddress "
									+ e.toString());
				}
			}
			if (content == "error") {
				try {
					if (subItem != null)
						callaPlay.videoExcept("mediaexception", item.pk,
								subItem.pk, item.title, clip.pk, currQuality,
								currPosition);
					else
						callaPlay.videoExcept("mediaexception", item.pk, null,
								item.title, clip.pk, currQuality, currPosition);
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

	private void checkTaskStart() {
		mCheckHandler.removeCallbacks(checkStatus);
		mCheckHandler.post(checkStatus);
	}

	private void checkTaskPause() {
		mCheckHandler.removeCallbacks(checkStatus);
	}

	private int i = 0;
	private Runnable checkStatus = new Runnable() {
		public void run() {
			if (mVideoView != null) {
				// Log.d(TAG,
				// "seekPostion == "+Math.abs(mVideoView.getCurrentPosition()-seekPostion));
				if (mVideoView.isPlaying()
						&& Math.abs(mVideoView.getCurrentPosition()
								- seekPostion) > 0) {
					if (isBuffer || bufferLayout.isShown()) {
						isBuffer = false;
						hideBuffer();
					} else {
						if (mVideoView.getAlpha() < 1)
							mVideoView.setAlpha(1);
					}
					if (!isSeek && !paused && !isBuffer) {
						currPosition = mVideoView.getCurrentPosition();
						timeBar.setProgress(currPosition);
					}
					i = 0;
				} else {
					if (!paused && !isBuffer) {
						i += 1;
						seekPostion = mVideoView.getCurrentPosition();
						if (i > 1) {
							isBuffer = true;
							showBuffer();
						}
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
			if (listItems != null && listItems.size() > 0 && currNum < (listItems.size() - 1)) {
				subItem = listItems.get(currNum + 1);
				subItemUrl = simpleRestClient.root_url + "/api/subitem/"+ subItem.pk + "/";
				bundle.remove("url");
				bundle.putString("url", subItemUrl);
				addHistory(0);
				new ItemByUrlTask().execute();
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
						callaPlay.videoExit(item.pk, subItem.pk, item.title,clip.pk, currQuality, 0, "end");
					else
						callaPlay.videoExit(item.pk, null, item.title, clip.pk,currQuality, 0, "end");
				} catch (Exception e) {
					Log.e(TAG, " log Sender videoExit end " + e.toString());
				}
				PlayerActivity.this.finish();

			}
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
						currQuality, 0, "relate");
			else
				callaPlay.videoExit(item.pk, null, item.title, clip.pk,
						currQuality, 0, "relate");
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
	}

	private void resumeItem() {
		if (!paused || mVideoView == null)
			return;
		// hideBuffer();
		Log.d(TAG, "resume");
		mVideoView.start();
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
		} else {
			if (offsets > 0) {
				offn = offsets / 5;
			}
		}
		if (clipLength  > 1000000) {
			if (offn < 11) {
				currPosition += clipLength * offn * 0.01;
			} else {
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
		if (clipLength  > 1000000) {
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
				if (mVideoView.getDuration() > 0) {
					isSeek = true;
					showPanel();
					fbImage.setImageResource(R.drawable.vod_player_fb_focus);
					fastBackward(SHORT_STEP);
					ret = true;
				}
				break;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				if (mVideoView.getDuration() > 0) {
					isSeek = true;
					showPanel();
					ffImage.setImageResource(R.drawable.vod_player_ff_focus);
					fastForward(SHORT_STEP);
					ret = true;
				}
				break;
			case KeyEvent.KEYCODE_DPAD_CENTER:
			case KeyEvent.KEYCODE_ENTER:
				if (mVideoView.getDuration() > 0) {
					showPanel();
					if (!paused) {
						pauseItem();
						playPauseImage
								.setImageResource(R.drawable.vod_player_pause_focus);
					} else {
						resumeItem();
						playPauseImage
								.setImageResource(R.drawable.vod_player_play_focus);
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

				showPanel();
				ret = true;

				break;
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
		}
		if (mVideoView != null && ret == false) {
			ret = super.onKeyDown(keyCode, event);
		}
		return ret;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		boolean ret = false;
		if (mVideoView != null && !isVodMenuVisible()
				&& mVideoView.getDuration() > 0) {
			switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_LEFT:
				fbImage.setImageResource(R.drawable.vod_player_fb);
				mVideoView.seekTo(currPosition);
				Log.d(TAG, "LEFT seek to " + getTimeString(currPosition));
				ret = true;
				isSeek = false;
				offsets = 0;
				offn = 1;
				break;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				ffImage.setImageResource(R.drawable.vod_player_ff);
				mVideoView.seekTo(currPosition);
				Log.d(TAG, "RIGHT seek to" + getTimeString(currPosition));
				ret = true;
				isSeek = false;
				offsets = 0;
				offn = 1;
				break;
			case KeyEvent.KEYCODE_DPAD_CENTER:
			case KeyEvent.KEYCODE_ENTER:
				if (paused) {
					playPauseImage.setImageResource(R.drawable.vod_player_play);
				} else {
					playPauseImage
							.setImageResource(R.drawable.vod_player_pause);
				}
				ret = true;
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

	public void showPopupDialog(int type, String msg) {
		if (type == DIALOG_OK_CANCEL) {
			popupDlg = new Dialog(this, R.style.PopupDialog);
			View view;
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.popup_2btn, null);
			popupDlg.addContentView(view, new ViewGroup.LayoutParams(
					ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT));
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
									callaPlay.videoExit(item.pk, subItem.pk,
											item.title, clip.pk, currQuality,
											0, "detail");
								else
									callaPlay.videoExit(item.pk, null,
											item.title, clip.pk, currQuality,
											0, "detail");
							} catch (Exception e) {
								Log.e(TAG,
										" Sender log videoPlayStart "
												+ e.toString());
							}
							mVideoView.stopPlayback();
							PlayerActivity.this.finish();
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
			try {
				if (subItem != null)
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
									null);
				else
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
									null);
			} catch (Exception e) {
				Log.e(TAG, " Sender log videoPlayBlockend " + e.toString());
			}
		}
	}

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
			updataTimeText();
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			Log.d(TAG, "onStartTrackingTouch" + seekBar.getProgress());
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			Log.d(TAG, "onStopTrackingTouch" + seekBar.getProgress());
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu m) {
		boolean ret;
		if (menu != null && menu.isVisible())
			return false;
		m.add("menu");
		ret = super.onPrepareOptionsMenu(m);
		if (ret) {
			createWindow();
			menu = new ISTVVodMenu(this);
			ret = createMenu(menu);
		}
		if (itemUrl != null && favoriteManager != null
				&& favoriteManager.getFavoriteByUrl(itemUrl) != null) {
			menu.findItem(5).setTitle(
					getResources().getString(
							R.string.vod_bookmark_remove_bookmark_setting));
		}
		return ret;
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu m) {
		if (onVodMenuOpened(menu)) {
			menu.show();
			hideMenuHandler.postDelayed(hideMenuRunnable, 10000);
		}
		return false;
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

	// reset()-->setDataSource(path)-->prepare()-->start()-->stop()--reset()-->
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
							.setImageResource(R.drawable.vod_player_pause);
					isBuffer = true;
					currQuality = pos;
					historyManager.addOrUpdateQuality(new Quality(0,
							urls[currQuality], currQuality));
					mVideoView = (VideoView) findViewById(R.id.video_view);
					mVideoView.setVideoPath(urls[currQuality].toString());
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

	private void updataTimeText() {
		String text = getTimeString(currPosition) + "/"
				+ getTimeString(clipLength);
		timeText.setText(text);
	}

	private void initQualtiyText() {

		switch (currQuality) {
		case 0:
			qualityText.setText("流畅");
			break;
		case 1:
			qualityText.setText("高清");
			break;
		case 2:
			qualityText.setText("超清");
			break;

		case 3:
			qualityText.setText("自适应");
			break;

		default:
			qualityText.setText("流畅");
			break;
		}

	}

	@Override
	protected void onPause() {
		try {
			addHistory(seekPostion);
			checkTaskPause();
			timeTaskPause();
			removeAllHandler();
			mVideoView.stopPlayback();
			PlayerActivity.this.finish();
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

}