package tv.ismar.daisy;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.EventProperty;
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
import tv.ismar.daisy.views.IsmatvVideoView;
import tv.ismar.player.SmartPlayer;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
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
import com.ismartv.api.AccessProxy;
import com.ismartv.bean.ClipInfo;

public class PlayerActivity extends Activity {

	@SuppressWarnings("unused")
	private static final String SAMPLE = "http://114.80.0.33/qyrrs?url=http%3A%2F%2Fjq.v.tvxio.com%2Fcdn%2F0%2F7b%2F78fadc2ffa42309bda633346871f26%2Fhigh%2Fslice%2Findex.m3u8&quality=high&sn=weihongchang_s52&clipid=779521&sid=85d3f919a918460d9431136d75db17f03&sign=08a868ad3c4e3b37537a13321a6f9d4b";

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
	private Handler mHandler = new Handler();
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
	private String sid;
	private String mediaip;
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
		mVideoView = (IsmatvVideoView) findViewById(R.id.video_view);
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
				//ExToClosePlayer("url"," m3u8 quality is null ,or get m3u8 err");
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
						urlInfo = AccessProxy.parse("http://cord.tvxio.com"
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
									"http://cord.tvxio.com" + "/api/clip/"
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
						subItemUrl = "http://cord.tvxio.com"
								+ "/api/subitem/" + item.pk + "/";
						subItem = simpleRestClient.getItem(subItemUrl);
						itemUrl = "http://cord.tvxio.com" + "/api/item/"
								+ item.item_pk + "/";
						item = simpleRestClient.getItem(itemUrl);
						if (item != null && item.subitems != null) {
							
							listItems = new ArrayList<Item>();
							
							for (int i = 0; i < item.subitems.length; i++) {
								listItems.add(item.subitems[i]);
							}
						}
					} else {
						itemUrl = "http://cord.tvxio.com" + "/api/item/"
								+ item.item_pk + "/";
						item = simpleRestClient.getItem(itemUrl);
					}

				}
			} catch (Exception e) {
				Log.e(TAG, e.toString());
//				ExToClosePlayer("url","m3u8 quality is null ,or get m3u8 err /n"+e.toString());
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

//				mVideoView.setVideoPath(urls[currQuality]);
//				mVideoView.setOnPreparedListener(new SmartPlayer.OnPreparedListener(){
//
//					@Override
//					public void onPrepared(SmartPlayer arg0) {
//						
//						arg0.start();
//						checkTaskStart();
//					}});
				if (urls != null && mVideoView != null) {
					TaskStart();//cmstest.tvxio.com
					mVideoView.setVideoPath(urls[currQuality]);
					sid = VodUserAgent.getSid(urls[currQuality]);
					mediaip = VodUserAgent.getMediaIp(urls[currQuality]);
					mVideoView
							.setOnPreparedListener(new SmartPlayer.OnPreparedListener() {
								@Override
								public void onPrepared(SmartPlayer mp) {

									Log.d(TAG,
											"mVideoView onPrepared tempOffset =="
													+ tempOffset);
									if (mVideoView != null) {
										clipLength = mVideoView.getDuration();
										timeBar.setMax(clipLength);
										mp.start();
										mp.seekTo(currPosition);
										timeBar.setProgress(currPosition);
										timeTaskStart();
										checkTaskStart();
										urls[0] = urlInfo.getNormal();
										urls[1] = urlInfo.getMedium();
										urls[2] = urlInfo.getHigh();
										urls[3] = urlInfo.getAdaptive();
										if (subItem != null){
											callaPlay.videoPlayLoad(item.pk, subItem.pk, item.title,
													clip.pk, currQuality,(System.currentTimeMillis() - startDuration) / 1000,0,null, sid);
											callaPlay.videoPlayStart(item.pk, subItem.pk, item.title,
													clip.pk, currQuality, 0);
										}else{
											callaPlay.videoPlayLoad(item.pk, null, item.title,
													clip.pk, currQuality,(System.currentTimeMillis() - startDuration) / 1000,0,null, sid);
											callaPlay.videoPlayStart(item.pk, null, item.title,
													clip.pk, currQuality, 0);
										}
									}
								}
							});
					mVideoView
							.setOnErrorListener(new SmartPlayer.OnErrorListener() {
								@Override
								public boolean onError(SmartPlayer mp,
										int what, int extra) {
									Log.d(TAG,
											"mVideoView  Error setVideoPath urls[currQuality] ");
									addHistory(currPosition);
									ExToClosePlayer("error",what+" "+extra);
									return false;
								}
							});

					mVideoView
							.setOnCompletionListener(new SmartPlayer.OnCompletionListener() {

								@Override
								public void onCompletion(SmartPlayer mp) {
									Log.d(TAG, "mVideoView  Completion");
									gotoFinishPage();
								}
							});
					
					mVideoView.setOnSeekCompleteListener(new SmartPlayer.OnSeekCompleteListener() {
						
						@Override
						public void onSeekComplete(SmartPlayer mp) {
							// TODO Auto-generated method stub
							isBuffer = false;
							hideBuffer();
						}
					});
				}

			} else {
				ExToClosePlayer("url","m3u8 content error ,");
			}
		} catch (Exception e) {
			Log.e(TAG, e.toString());
			ExToClosePlayer("url",e.toString());
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
					callaPlay.videoStart(item, subItem.pk, subItem.title, currQuality, null, 0, mSection, sid);
				else
					callaPlay.videoStart(item, null, item.title, currQuality, null, 0, mSection, sid);
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

	private void ExToClosePlayer(String code ,String content) {
		if (bufferText != null) {
			bufferText.setText(EXTOCLOSE);
			if (code == "url") {
				try {
					if (subItem != null)
						callaPlay.videoExcept("noplayaddress",content, item.pk,
										subItem.pk, item.title, clip.pk,
										currQuality, 0);
					else
						callaPlay.videoExcept("noplayaddress",content, item.pk, null,
								item.title, clip.pk, currQuality, 0);
				} catch (Exception e) {
					Log.e(TAG,
							" Sender log videoExcept noplayaddress "
									+ e.toString());
				}
			}
			if (code == "error") {
				try {
					if (subItem != null)
						callaPlay.videoExcept("mediaexception",content, item.pk,
								subItem.pk, item.title, clip.pk, currQuality,
								currPosition);
					else
						callaPlay.videoExcept("mediaexception",content, item.pk, null,
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
//				 Log.d(TAG,
//				 "seekPostion == "+Math.abs(mVideoView.getCurrentPosition()-seekPostion));
				if (mVideoView.isPlaying()
						&& Math.abs(mVideoView.getCurrentPosition()
								- seekPostion) > 0) {
//					if (isBuffer || bufferLayout.isShown()) {
//						//isBuffer = false;
//						//hideBuffer();
//					} else {
						if (mVideoView.getAlpha() < 1){
							mVideoView.setAlpha(1);
							bufferText.setText(BUFFERING);
						}
							
				//	}
					if (!isSeek  && !isBuffer) {
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
//							showBuffer();
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
				if (mVideoView != null) {
					mVideoView.setAlpha(0);
				}
				isBuffer = true;
				showBuffer();
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
						callaPlay.videoExit(item.pk, subItem.pk, item.title,clip.pk, currQuality, 0, "end",currPosition,(System.currentTimeMillis() - startDuration) / 1000, mSection, sid, "list", item.content_model);//String section,String sid,String source,String channel
					else
						callaPlay.videoExit(item.pk, null, item.title, clip.pk,currQuality, 0, "end",currPosition,(System.currentTimeMillis() - startDuration) / 1000, mSection, sid, "list", item.content_model);
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
						currQuality, 0, "relate",currPosition,(System.currentTimeMillis() - startDuration) / 1000,mSection, sid, "list", item.content_model);
			else
				callaPlay.videoExit(item.pk, null, item.title, clip.pk,
						currQuality, 0, "relate",currPosition,(System.currentTimeMillis() - startDuration) / 1000, mSection, sid, "list", item.content_model);
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
		if (subItem != null)			
			callaPlay.videoPlayPause(item.pk, subItem.pk, item.title,clip.pk, currQuality, 0, currPosition, sid);
		else
			callaPlay.videoPlayPause(item.pk, null, item.title, clip.pk,currQuality, 0, currPosition, sid);
		
		
	}

	private void resumeItem() {
		if (!paused || mVideoView == null)
			return;
		// hideBuffer();
		Log.d(TAG, "resume");
		mVideoView.start();
		
		if (subItem != null)			
			callaPlay.videoPlayContinue(item.pk, subItem.pk, item.title,clip.pk, currQuality, 0, currPosition, sid);
		else
			callaPlay.videoPlayContinue(item.pk, null, item.title,clip.pk, currQuality, 0, currPosition, sid);
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
					fbImage.setImageResource(R.drawable.vodplayer_controller_rew_pressed);
					isBuffer = true;
                    showBuffer();
					fastBackward(SHORT_STEP);                  
					ret = true;
				}
				break;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				if (mVideoView.getDuration() > 0) {
					isSeek = true;
					showPanel();
					ffImage.setImageResource(R.drawable.vodplayer_controller_ffd_pressed);
					isBuffer = true;
                    showBuffer();
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
//						playPauseImage
//								.setImageResource(R.drawable.vod_player_pause_focus);
						playPauseImage.setImageResource(R.drawable.vodplayer_controller_pause_pressed);
					} else {
						resumeItem();
//						playPauseImage
//								.setImageResource(R.drawable.vod_player_play_focus);
						playPauseImage.setImageResource(R.drawable.vodplayer_controller_play_pressed);
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
		
			 am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
				showPanel();
				ret = true;

				break;
			case KeyEvent.KEYCODE_DPAD_DOWN:
				am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
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
			case KeyEvent.KEYCODE_MENU:
				if (menu != null && menu.isVisible())
					return false;
				if (menu==null) {
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
				if (onVodMenuOpened(menu)) {
				menu.show();
				hideMenuHandler.postDelayed(hideMenuRunnable, 10000);
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
				fbImage.setImageResource(R.drawable.vodplayer_controller_rew);
				mVideoView.seekTo(currPosition);
				if (subItem != null)			
					callaPlay.videoPlaySeek(item.pk, subItem.pk, item.title,clip.pk, currQuality, 0, currPosition, sid);
				else
					callaPlay.videoPlayContinue(item.pk, null, item.title,clip.pk, currQuality, 0, currPosition, sid);
				isSeekBuffer = true ;
				Log.d(TAG, "LEFT seek to " + getTimeString(currPosition));
				ret = true;
				isSeek = false;
				offsets = 0;
				offn = 1;
				break;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				ffImage.setImageResource(R.drawable.vodplayer_controller_ffd);
				mVideoView.seekTo(currPosition);
				if (subItem != null)			
					callaPlay.videoPlaySeek(item.pk, subItem.pk, item.title,clip.pk, currQuality, 0, currPosition, sid);
				else
					callaPlay.videoPlayContinue(item.pk, null, item.title,clip.pk, currQuality, 0, currPosition, sid);
				isSeekBuffer = true ;
				Log.d(TAG, "RIGHT seek to" + getTimeString(currPosition));
				ret = true;
				isSeek = false;
				offsets = 0;
				offn = 1;
				break;
			case KeyEvent.KEYCODE_DPAD_CENTER:
			case KeyEvent.KEYCODE_ENTER:
				if (paused) {
				//	playPauseImage.setImageResource(R.drawable.vod_player_play);
					playPauseImage.setImageResource(R.drawable.vodplayer_controller_play);
				} else {
					//playPauseImage.setImageResource(R.drawable.vod_player_pause);
					playPauseImage.setImageResource(R.drawable.vodplayer_controller_pause);
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
			int H = DaisyUtils.getVodApplication(this).getheightPixels(this);
			if(H==720)
			    popupDlg.addContentView(view, new ViewGroup.LayoutParams(
					538,296));
			else
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
											0, "detail",currPosition,(System.currentTimeMillis() - startDuration) / 1000, mSection, sid, "list", item.content_model);
								else
									callaPlay.videoExit(item.pk, null,
											item.title, clip.pk, currQuality,
											0, "detail",currPosition,(System.currentTimeMillis() - startDuration) / 1000, mSection, sid, "list", item.content_model);
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
					if(isSeekBuffer){
						callaPlay.videoPlaySeekBlockend(
									item.pk,
									subItem.pk,
									item.title,
									clip.pk,
									currQuality,
									0,
									currPosition,
									(System.currentTimeMillis() - bufferDuration) / 1000,
									mediaip,sid);
						
					}else{
					callaPlay.videoPlayBlockend(
									item.pk,
									subItem.pk,
									item.title,
									clip.pk,
									currQuality,
									0,
									currPosition,
									(System.currentTimeMillis() - bufferDuration) / 1000,
									mediaip,sid);
					}
				else
					if(isSeekBuffer){
						callaPlay.videoPlaySeekBlockend(
								item.pk,
								null,
								item.title,
								clip.pk,
								currQuality,
								0,
								currPosition,
								(System.currentTimeMillis() - bufferDuration) / 1000,
								mediaip,sid);
						
					}else{
					callaPlay.videoPlayBlockend(
									item.pk,
									null,
									item.title,
									clip.pk,
									currQuality,
									0,
									currPosition,
									(System.currentTimeMillis() - bufferDuration) / 1000,
									mediaip,sid);
					}
				isSeekBuffer = false;
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
//					playPauseImage
//							.setImageResource(R.drawable.vod_player_pause);
					playPauseImage.setImageResource(R.drawable.vodplayer_controller_pause);
					isBuffer = true;
					currQuality = pos;
					mVideoView = (IsmatvVideoView) findViewById(R.id.video_view);
					mVideoView.setVideoPath(urls[currQuality].toString());
					historyManager.addOrUpdateQuality(new Quality(0,urls[currQuality], currQuality));
					mediaip = VodUserAgent.getMediaIp(urls[currQuality]);
					if (subItem != null)
						callaPlay.videoSwitchStream(item.pk, subItem.pk, item.title,clip.pk, currQuality,"manual",null,null,mediaip,sid);
					else
						callaPlay.videoSwitchStream(item.pk, null, item.title,clip.pk, currQuality,"manual",null,null,mediaip,sid);
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
			//qualityText.setText("流畅");
			qualityText.setBackgroundResource(R.drawable.vodplayer_stream_normal);
			break;
		case 1:
			//qualityText.setText("高清");
			qualityText.setBackgroundResource(R.drawable.vodplayer_stream_high);
			break;
		case 2:
			//qualityText.setText("超清");
			qualityText.setBackgroundResource(R.drawable.vodplayer_stream_ultra);
			break;

		case 3:
			qualityText.setText("自适应");
			qualityText.setBackgroundResource(R.drawable.rounded_edittext);
			break;

		default:
			//qualityText.setText("流畅");
			qualityText.setBackgroundResource(R.drawable.vodplayer_stream_normal);
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