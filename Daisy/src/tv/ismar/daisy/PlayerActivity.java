package tv.ismar.daisy;

import static tv.ismar.daisy.DramaListActivity.ORDER_CHECK_BASE_URL;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.lang3.StringUtils;
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
import tv.ismar.daisy.core.preferences.AccountSharedPrefs;
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
import tv.ismar.daisy.views.IsmatvVideoView;
import tv.ismar.daisy.views.MarqueeView;
import tv.ismar.daisy.views.PaymentDialog;
import tv.ismar.player.SmartPlayer;
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
import android.os.Looper;
import android.os.Message;
import android.util.Base64;
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

import com.google.gson.JsonSyntaxException;
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
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		shardpref = AccountSharedPrefs.getInstance(this);
		setView();

		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		
	}

	@Override
	public void onResume() {
		super.onResume();
		if (needOnresume) {
			isBuffer = true;
			showBuffer();
			initPlayer();
			needOnresume = false;
		}
	}

	private void setView() {
		panelShowAnimation = AnimationUtils.loadAnimation(this,
				R.anim.fly_up);
		panelHideAnimation = AnimationUtils.loadAnimation(this,
				R.anim.fly_down);
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
		ad_count_view = (TextView) findViewById(R.id.ad_count_view);
		panelLayout.setVisibility(View.GONE);
		bufferLayout.setVisibility(View.GONE);
		qualityText.setVisibility(View.GONE);
		gesture_tipview = (ImageView)findViewById(R.id.gesture_tipview);
		am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		playPauseImage.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent keycode) {
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
		setVideoActionListener();
		if("false".equals(shardpref.getSharedPrefs(AccountSharedPrefs.FIRST_USE))){
			initClipInfo();
		}else{
			gesture_tipview.setVisibility(View.VISIBLE);
		}
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
			}
			// use to get mUrl, and registerActivity
			DaisyUtils.getVodApplication(this).addActivityToPool(
					this.toString(), this);
			// *********************
			// new ItemByUrlTask().execute();
			String info = bundle.getString("ismartv");
			urlInfo = AccessProxy.getIsmartvClipInfo(info);
			if (item.item_pk == item.pk)
				itemUrl = SimpleRestClient.root_url + "/api/item/" + item.pk
						+ "/";
			else
				itemUrl = SimpleRestClient.root_url + "/api/item/"
						+ item.item_pk + "/";
			if (isPreview) {
				initPlayer();
			} else {
				getAdInfo("qiantiepian");
			}
		}
	}

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
		if (!adElement.isEmpty()) {
			int count = adElement.size();
			if (count > 0) {
                if(!(adElement.size()==1&&!"video".equals(adElement.get(0).getMedia_type()))){
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
						i++;
					} else {
						adimageDialog = new AdImageDialog(this,
								R.style.UserinfoDialog, element.getMedia_url(),element.getTitle(),element.getMedia_id());

						adimageDialog.getWindow().clearFlags(
								WindowManager.LayoutParams.FLAG_DIM_BEHIND);;
						adimageDialog.show();
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
				new FetchSeriallTask().execute();
			}
		}
	}

	// 根据电视剧单集获取电视剧全部信息
	class FetchSeriallTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected void onPostExecute(Boolean result) {
			if (result || currNum==0) { // 免费或已经付费的电视剧单集直接播放
				initPlayer();
			}
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			boolean result;
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
				} else { // 电视剧联播，非第一集需要重新获取clip
					clip = item.clip;
					urlInfo = AccessProxy.parse(SimpleRestClient.root_url
							+ "/api/clip/" + clip.pk + "/", VodUserAgent
							.getAccessToken(SimpleRestClient.sn_token),
							PlayerActivity.this);
					result = true;
					if (urlInfo.getIqiyi_4_0().length() > 0) {
						Intent intent = new Intent();
						intent.setAction("tv.ismar.daisy.qiyiPlay");
						intent.putExtra("iqiyi", urlInfo.getIqiyi_4_0());
						intent.putExtra("item", item);
						startActivity(intent);
						PlayerActivity.this.finish();
					}
				}
				result = true;
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
				result = true;
				if (urlInfo.getIqiyi_4_0().length() > 0) {
					Intent intent = new Intent();
					intent.setAction("tv.ismar.daisy.qiyiPlay");
					intent.putExtra("iqiyi", urlInfo.getIqiyi_4_0());
					intent.putExtra("item", item);
					startActivity(intent);
					PlayerActivity.this.finish();
				}
			}
			return result;
		}
	}

	private void setVideoActionListener() {
		mVideoView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (isadvideoplaying)
					return false;
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

		mVideoView
				.setOnPreparedListenerUrl(new SmartPlayer.OnPreparedListenerUrl() {
					@Override
					public void onPrepared(SmartPlayer mp, String url) {
						if (paths != null
								&& paths[paths.length - 1].equals(url)) {

							if (mVideoView != null) {
                                TaskStart();
								if (live_video) {
									timeBar.setEnabled(false);
									mp.start();
								} else {
									clipLength = mp.getDuration();
									timeBar.setMax(clipLength);
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
									timeBar.setEnabled(true);
									isadvideoplaying = false;
								}
								checkTaskStart(0);
								timeTaskStart(0);
								if (item.pk != item.pk) {
									callaPlay.videoPlayLoad(
											item.item_pk,
											item.pk,
											item.title,
											clip.pk,
											currQuality,
											(System.currentTimeMillis() - startDuration) / 1000,
                                            speed, mediaip, sid);
									callaPlay.videoPlayStart(item.item_pk,
											item.pk, item.title, clip.pk,
											currQuality, speed);
								} else {
									callaPlay.videoPlayLoad(
											item.pk,
											null,
											item.title,
											clip.pk,
											currQuality,
											(System.currentTimeMillis() - startDuration) / 1000,
                                            speed, mediaip, sid);
									callaPlay
											.videoPlayStart(item.pk, null,
													item.title, clip.pk,
													currQuality, speed);
								}
							}

						} else {
							// 广告
							if (mHandler.hasMessages(AD_COUNT_ACTION))
								mHandler.removeMessages(AD_COUNT_ACTION);
							mHandler.sendEmptyMessageDelayed(AD_COUNT_ACTION,
									1000);
							mp.start();
							checkTaskStart(0);
						}

					}
				});
		mVideoView
				.setOnCompletionListenerUrl(new SmartPlayer.OnCompletionListenerUrl() {
					@Override
					public void onCompletion(SmartPlayer smartPlayer, String url) {
                        Log.i("zhangjiqiangtest","playerActivity onCompletion url=="+url+"//");
                        Log.i("zhangjiqiangtest","playerActivity onCompletion paths[paths.length - 1]=="+       paths[paths.length - 1]+"//");

						if (paths != null && url != null
								&& paths[paths.length - 1].equals(url)) {
							if (item.isPreview) {
								if (item.expense == null) {
									finish();
									return;
								}
                                Log.i("zhangjiqiangtest","playerActivity onCompletion PaymentDialog");
								mVideoView.stopPlayback();
								PaymentDialog dialog = new PaymentDialog(
										PlayerActivity.this,
										R.style.PaymentDialog,
										ordercheckListener);
								item.model_name = "item";
								dialog.setItem(item);
								dialog.show();
							} else{
                                Log.i("zhangjiqiangtest","playerActivity onCompletion gotoFinishPage");
                                gotoFinishPage();
                            }

						} else if (paths != null
								&& paths[paths.length - 2].equals(url)) {
                            Log.i("zhangjiqiangtest","playerActivity onCompletion url=="+url);
							if (mHandler.hasMessages(AD_COUNT_ACTION))
								mHandler.removeMessages(AD_COUNT_ACTION);
							ad_count_view.setVisibility(View.GONE);
							initPlayerRelatedUI();
							isadvideoplaying = false;
						}
                        else{
                            Log.i("zhangjiqiangtest","playerActivity onCompletion else");
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
				if (i == SmartPlayer.MEDIA_INFO_BUFFERING_START || i == 809) {
					isBuffer = true;
					bufferText.setText(BUFFERING + " " + 0 + "%");
					showBuffer();
				} else if (i == 704) {
					bufferText.setText(BUFFERING + " " + j + "%");
				} else if (i == SmartPlayer.MEDIA_INFO_BUFFERING_END
						|| i == 3) {
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
                speed = speed/1024*8;
            }
        });
	}

	private void getAdInfo(String adpid) {
		StringBuffer directorsBuffer = new StringBuffer();
		StringBuffer actorsBuffer = new StringBuffer();
		StringBuffer genresBuffer = new StringBuffer();
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

		String channelSection = "channel=" + "" + "&section=" + "";
		if (StringUtils.isNotEmpty(item.channel)
				&& StringUtils.isNotEmpty(item.slug)) {
			channelSection = "channel=" + item.channel + "&section="
					+ item.slug;
		}

		String params = channelSection
				+ "&itemid="
				+ item.pk
				+ "&topic="
				+ ""
				+ "&source="
				+ item.fromPage
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
							tempOffset = 0;
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
					 sid = VodUserAgent.getSid(urls[currQuality]);
					 mediaip = VodUserAgent.getMediaIp(urls[currQuality]);
					isBuffer = true;
					showBuffer();
					if (!isadvideoplaying) {
						paths = new String[1];
						paths[0] = urls[currQuality];
						mVideoView.setVideoPaths(paths);
						initPlayerRelatedUI();
					} else {
						if (paths != null) {
							paths[paths.length - 1] = urls[currQuality];
							mVideoView.setVideoPaths(paths);
						}
					}
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
		sid = VodUserAgent.getSid(urls[currQuality]);
		mediaip = VodUserAgent.getMediaIp(urls[currQuality]);
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
				if (item != null)
					callaPlay.videoStart(item, item.pk, item.title,
							currQuality, null, speed, mSection, sid);
				else
					callaPlay.videoStart(item, null, item.title, currQuality,
							null, speed, mSection, sid);
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
					if (item != null)
						callaPlay.videoExcept("noplayaddress", content,
								item.item_pk, item.pk, item.title, clip.pk,
								currQuality, speed);
					else
						callaPlay.videoExcept("noplayaddress", content,
								item.pk, null, item.title, clip.pk,
								currQuality, speed);
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
				Intent intent = new Intent("tv.ismar.daisy.PlayFinished");
				intent.putExtra("item", item);
				startActivity(intent);
				seekPostion = 0;
				currPosition = 0;
				mVideoView = null;
				addHistory(0);
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
										(System.currentTimeMillis() - startDuration) / 1000,
										item.slug, sid, "list",
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
										item.slug, sid, "list",
										item.content_model);
				} catch (Exception e) {
					Log.e(TAG, " log Sender videoExit end " + e.toString());
				}
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
			if (item.expense != null)
				history.price = (int) item.expense.price;
			else
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
        if(mVideoView==null){
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
		// showBuffer();
		Log.d(TAG, "pause");
		hideBuffer();
		mVideoView.pause();
		paused = true;
		if (!(popupDlg != null && popupDlg.isShowing()))
			getAdInfo("zanting");
		if (item.pk != item.item_pk)
			callaPlay.videoPlayPause(item.item_pk, item.pk, item.title,
					clip.pk, currQuality, speed, currPosition, sid);
		else
			callaPlay.videoPlayPause(item.pk, null, item.title, clip.pk,
					currQuality, speed, currPosition, sid);

	}

	protected void resumeItem() {
		if (!paused || mVideoView == null)
			return;
		// hideBuffer();
		Log.d(TAG, "resume");
		mVideoView.start();

		if (item.pk != item.item_pk)
			callaPlay.videoPlayContinue(item.item_pk, item.pk, item.title,
					clip.pk, currQuality, speed, currPosition, sid);
		else
			callaPlay.videoPlayContinue(item.pk, null, item.title, clip.pk,
					currQuality, speed, currPosition, sid);
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
		if(keyCode == KeyEvent.KEYCODE_BACK && !"false".equals(shardpref.getSharedPrefs(AccountSharedPrefs.FIRST_USE))){
			gesture_tipview.setVisibility(View.GONE);
			shardpref.setSharedPrefs(AccountSharedPrefs.FIRST_USE, "false");
			initClipInfo();
			return false;
		}
		if(!"false".equals(shardpref.getSharedPrefs(AccountSharedPrefs.FIRST_USE))){
			return false;			
		}
		if(isadvideoplaying){
			if (keyCode == KeyEvent.KEYCODE_BACK) {
				mVideoView.stopPlayback();
				finish();
			}
			return true;
		}
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
											(System.currentTimeMillis() - startDuration) / 1000,
											item.slug, sid, "list",
											item.content_model);
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
											(System.currentTimeMillis() - startDuration) / 1000,
											item.slug, sid, "list",
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
					};
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
		if (adElement != null && !adElement.isEmpty())
			return;
			bufferLayout.setVisibility(View.VISIBLE);
			bufferDuration = System.currentTimeMillis();
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
										(System.currentTimeMillis() - bufferDuration) / 1000,
										mediaip, sid);

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
									speed,
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
                                    speed,
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
				if (item.pk != item.item_pk)
					callaPlay.videoPlaySeek(item.item_pk, item.pk, item.title,
							clip.pk, currQuality, speed, currPosition, sid);
				else
					callaPlay.videoPlayContinue(item.pk, null, item.title,
							clip.pk, currQuality, speed, currPosition, sid);
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
				if (adimageDialog != null && adimageDialog.isShowing()){
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
					paths = new String[1];
					paths[0] = urls[currQuality];
					tempOffset = seekPostion;
					mVideoView.setVideoPaths(paths);
					// mVideoView.setVideoPath(urls[currQuality]);
					historyManager.addOrUpdateQuality(new Quality(0,
							urls[currQuality], currQuality));
					mediaip = VodUserAgent.getMediaIp(urls[currQuality]);
					if (item.pk != item.item_pk)
						callaPlay.videoSwitchStream(item.item_pk, item.pk,
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
		needOnresume = true;
		if(adAsyncTask != null && !adAsyncTask.isCancelled()){
			adAsyncTask.cancel(true);
		}

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
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		menu = null;
		urlInfo = null;
		mCheckHandler = null;
		historyManager = null;
		simpleRestClient = null;
		DaisyUtils.getVodApplication(this).removeActivtyFromPool(
				this.toString());
		sendPlayComplete();
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
					new FetchClipTask().execute();
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
}