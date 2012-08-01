package tv.ismar.daisy;

import tv.ismar.daisy.core.VodUserAgent;
import tv.ismar.daisy.models.Clip;
import tv.ismar.daisy.models.Item;
import tv.ismar.daisy.player.ISTVVodMenu;
import tv.ismar.daisy.player.ISTVVodMenuItem;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.ismartv.api.AccessProxy;
import com.ismartv.bean.ClipInfo;

public class PlayerActivity extends Activity {
	private static final String TAG = "ISTVVodPlayer";

	private static final String PREFS_NAME = "tv.ismar.daisy";

	private static final int PANEL_HIDE_TIME = 6;
	private static final int SHORT_FB_STEP = 10000;
	private static final int SHORT_FF_STEP = 10000;
	private static final int LONG_FB_PERCENT = 10;
	private static final int LONG_FF_PERCENT = 10;
	private static final int KEY_REPEAT_COUNT = 4;

	public final int RES_URL_CLIP_ADAPTIVE = 0;
	public static final int RES_URL_CLIP_LOW = 1;
	public static final int RES_URL_CLIP_MEDIUM = 2;
	public static final int RES_URL_CLIP_NORMAL = 3;
	public static final int RES_URL_CLIP_HIGH = 4;
	public static final int RES_URL_CLIP_ULTRA = 5;
	public static final int RES_STR_ITEM_TITLE = 6;
	public static final int RES_INT_CLIP_LENGTH = 7;
	public static final int RES_BOOL_BOOKMARKED = 8;
	public static final int RES_INT_OFFSET = 9;
	public static final int RES_INT_EPISODE_REALCOUNT = 10;

	public static final int DIALOG_OK_CANCEL = 0;
	public static final int DIALOG_RETRY_CANCEL = 1;
	public static final int DIALOG_IKNOW = 2;
	public static final int DIALOG_NET_BROKEN = 3;
	public static final int DIALOG_CANNOT_GET_DATA = 4;
	public static final int DIALOG_LOGIN = 5;
	public static final int DIALOG_ITEM_CLICK_NET_BROKEN = 6;

	// private ISTVVodPlayerDoc doc;
	private int itemPK = 18821;
	private int subItemPK = -1;
	private String urls[] = new String[6];
	private String itemTitle;

	private int clipLength = 0;
	private int currPosition = 0;
	private int clipOffset = 0;
	private boolean clipBookmarked = false;
	private int episoderealcount;

	// private URL currURL = null;
	private String currURL = null;
	private int currQuality = 0;
	private boolean urlUpdate = false;
	private boolean playing = false;
	private boolean prepared = false;
	private boolean buffering = false;
	private boolean seeking = false;
	private boolean needSeek = false;
	private boolean paused = false;
	private boolean panelShow = false;
	private boolean bufferShow = false;
	private int panelHideCounter = 0;

	private Animation panelShowAnimation;
	private Animation panelHideAnimation;
	private Animation bufferShowAnimation;
	private Animation bufferHideAnimation;

	private VideoView videoView;// load

	private RelativeLayout bufferLayout;
	private AnimationDrawable bufferAnim;
	private LinearLayout panelLayout;
	private TextView titleText;
	private TextView timeText;
	private SeekBar timeBar;
	private ImageView playPauseImage;
	private ImageView ffImage;
	private ImageView fbImage;

	private boolean keyOKDown = false;
	private boolean keyLeftDown = false;
	private boolean keyRightDown = false;
	private boolean seekBarDown = false;
	private int keyLeftRepeat = 0;
	private int keyRightRepeat = 0;

	private boolean firstPlay;

	private int popupstatus = 0;
	/*
	 * private VodTimer replayTimer; private boolean replaytimerflag = false;
	 */

	private boolean isContinue = true;
	private boolean isChangeUrl = false;
	private ISTVVodMenu menu = null;
	private boolean winOK = false;

	// accessProxy 返回播放的码流地址
	private ClipInfo cinfo = new ClipInfo();
	// 播放时间轴计时器
	private Handler mHandler = new Handler();
	private long mStartTime = 0;

	private TextView bufferText;
	private int tempOffset = 0;
	private Item item;
	private Clip clip;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		/* before super, because onCreateVodMenu use subItemPK */
		// Bundle bundle = this.getIntent().getExtras();
		// if (bundle != null) {
		// itemPK = bundle.getInt("itemPK", itemPK);
		// subItemPK = bundle.getInt("subItemPK", subItemPK);
		// }
		Intent intent = getIntent();
		if (intent != null) {
			item = (Item) intent.getSerializableExtra("item");
			itemPK = item.pk;
			clip = item.clip;
		}

		super.onCreate(savedInstanceState);

		episoderealcount = 0;

		firstPlay = true;
		itemTitle = null;
		clipLength = 0;
		currPosition = 0;
		currURL = null;
		currQuality = 0;
		urlUpdate = false;
		playing = false;
		prepared = false;
		seeking = false;
		buffering = false;
		panelShow = false;
		bufferShow = false;
		keyOKDown = false;
		keyLeftDown = false;
		keyRightDown = false;

		isContinue = getSharedPreferences(PREFS_NAME, 0).getBoolean(
				"continue_play", true);
		Log.d(TAG, "isContinue=" + isContinue);

		panelShowAnimation = AnimationUtils.loadAnimation(this,
				R.drawable.fly_up);
		panelHideAnimation = AnimationUtils.loadAnimation(this,
				R.drawable.fly_down);
		bufferShowAnimation = AnimationUtils.loadAnimation(this,
				R.drawable.fade_in);
		bufferHideAnimation = AnimationUtils.loadAnimation(this,
				R.drawable.fade_out);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		setContentView(R.layout.vod_player);

		videoView = (VideoView) findViewById(R.id.VideoView);

		panelLayout = (LinearLayout) findViewById(R.id.PanelLayout);
		titleText = (TextView) findViewById(R.id.TitleText);
		timeText = (TextView) findViewById(R.id.TimeText);
		timeBar = (SeekBar) findViewById(R.id.TimeSeekBar);
		playPauseImage = (ImageView) findViewById(R.id.PlayPauseImage);
		ffImage = (ImageView) findViewById(R.id.FFImage);
		fbImage = (ImageView) findViewById(R.id.FBImage);

		new AccessProxyTask().execute();

		playPauseImage.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (!keyOKDown) {
					if (!paused) {
						pauseItem();
						playPauseImage
								.setImageResource(R.drawable.vod_player_play);
					} else {
						resumeItem();
						playPauseImage
								.setImageResource(R.drawable.vod_player_pause);
					}
					showPanel();
				}
			}
		});
		ffImage.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (!keyRightDown) {
					showPanel();
					fastForward(SHORT_FF_STEP);
					seekTo();
				}
			}
		});
		fbImage.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (!keyLeftDown) {
					showPanel();
					fastBackward(SHORT_FB_STEP);
					seekTo();
				}
			}
		});
		timeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if (fromUser) {
					currPosition = progress * clipLength / 100;
					onDocUpdate();
				}
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
				seekBarDown = true;
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
				seekBarDown = false;
				showPanel();
				seekTo();
			}
		});

		bufferLayout = (RelativeLayout) findViewById(R.id.BufferLayout);
		bufferText = (TextView) bufferLayout.findViewById(R.id.BufferText);
		bufferAnim = (AnimationDrawable) ((ImageView) findViewById(R.id.BufferImage)).getBackground();

		videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer player) {
				timeTaskStart();
				Log.d(TAG, "video prepared");
				if (!prepared) {
					prepared = true;
					if (currPosition != 0) {
						seekTo();
					}
				}
			}

		});

		videoView
				.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
					public void onCompletion(MediaPlayer mp) {
						Log.d(TAG, "!!!!!!!!!video complete");
						clipOffset = -1;
						storeOffset();
						int sub = 3333;// ----------接口获得subid 获得
						if (sub != -1) {
							for (int i = 0; i < 4; i++) {
								urls[i] = null;
							}
							itemTitle = null;
							clipLength = 0;
							currPosition = 0;
							currURL = null;
							currQuality = 0;
							urlUpdate = false;
							playing = false;
							prepared = false;
							seeking = false;
							buffering = false;
							firstPlay = false;

							showBuffer();
						} else {
							gotoplayerfinishpage(itemPK);// ----------------到播放完成后的页面

						}
					}
				});
		// videoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
		// public boolean onInfo(MediaPlayer mp, int what, int extra) {
		// switch (what) {
		// case MediaPlayer.MEDIA_INFO_BUFFERING_START:
		// Log.d(TAG, "buffering start");
		// buffering = true;
		// showBuffer();
		//
		// break;
		// case MediaPlayer.MEDIA_INFO_BUFFERING_END:
		// Log.d(TAG, "buffering end");
		// buffering = false;
		// // CallaPlay.videoPlayLoad(item, subitem, title, clip, quality,
		// duration, speed, mediaip);
		// seekComplete();
		// hideBuffer();
		// if (isChangeUrl) {
		// seekTo();
		// isChangeUrl = false;
		// }
		// break;
		// }
		// return true;
		// }
		// });
		//
		// videoView.setOnSeekCompleteListener(new
		// MediaPlayer.OnSeekCompleteListener() {
		// public void onSeekComplete(MediaPlayer mp) {
		// seekComplete();
		// }
		// });
		videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
			public boolean onError(MediaPlayer mp, int what, int extra) {
				timeTaskPause();
				if (playing && prepared) {

					popupstatus = 1;

				}
				return true;
			}

		});

		showBuffer();
	}

	public void onDestroy() {
		super.onDestroy();
	}

	public boolean onVodMenuClosed(ISTVVodMenu menu) {
		if (paused || !playing || !prepared) {
			showPanel();
		}

		return true;
	}

	private void seekComplete() {
		if (!seeking)
			return;

		seeking = false;
		if (needSeek) {
			seekTo();
		}
		hideBuffer();
		if (!seeking) {
			Log.d(TAG, "seek complete");
		}
	}

	private String getClipURL() {
		String url = urls[currQuality];

		if (url != null)
			return url;

		for (int i = 0; i < 4; i++) {
			if (urls[i] != null) {
				currQuality = i;
				Log.d(TAG, "seek complete");
				return urls[i];
			}
		}

		return null;
	}

	private void setQuality(int q) {
		if (q != currQuality) {
			timeTaskPause();
			currQuality = q;
			urlUpdate = true;
			isChangeUrl = true;
			onDocUpdate();
		}
	}

	protected void onDocGotResource(ClipInfo info) {
		Log.d(TAG, "RES_URL_CLIP_ADAPTIVE=" + info.getAdaptive());
		urlUpdate = true;
		Log.d(TAG, "RES_URL_CLIP_ADAPTIVE=" + info.getAdaptive());
		urls[3] = info.getAdaptive();

		Log.d(TAG, "RES_URL_CLIP_MEDIUM=" + info.getMedium());
		urls[1] = info.getMedium();

		Log.d(TAG, "RES_URL_CLIP_NORMAL=" + info.getNormal());
		urls[0] = info.getNormal();

		Log.d(TAG, "RES_URL_CLIP_HIGH=" + info.getHigh());
		urls[2] = info.getHigh();

		titleText.setText(item.title);
		titleText.setSelected(true);

		clipLength = clip.length * 1000;
		Log.d(TAG, "RES_INT_CLIP_LENGTH=" + clipLength);

		clipBookmarked = true;

		if (isContinue)
			currPosition = tempOffset * 1000;
		else
			clipOffset = 0;

		Log.d(TAG, "RES_INT_OFFSET currPosition=" + currPosition
				+ ",clipOffset=" + clipOffset + ",firstPlay=" + firstPlay);

		episoderealcount = 0;
		onDocUpdate();
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

	protected void onDocUpdate() {
		/* Redraw the activity */
		if (urlUpdate) {
			startVideo();
			urlUpdate = false;
		}
		if (clipLength > 0) {
			String text = getTimeString(currPosition) + "/"
					+ getTimeString(clipLength);
			timeText.setText(text);
			int val = currPosition * 100 / clipLength;
			timeBar.setProgress(val);
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
		panelHideCounter = PANEL_HIDE_TIME;
	}

	private void hidePanel() {
		if (panelShow) {
			panelLayout.startAnimation(panelHideAnimation);
			panelLayout.setVisibility(View.GONE);
			panelShow = false;
		}
	}

	private void pauseItem() {
		if (paused)
			return;

		Log.d(TAG, "pause");
		videoView.pause();
		paused = true;
	}

	private void resumeItem() {
		if (!paused)
			return;

		videoView.start();
		Log.d(TAG, "resume");
		paused = false;
	}

	private void fastForward(int step) {
		if (currPosition == clipLength)
			return;
		currPosition += step;

		if (currPosition > clipLength)
			currPosition = clipLength;

		Log.d(TAG, "ff " + currPosition);
		onDocUpdate();
	}

	private void fastBackward(int step) {
		if (currPosition == 0)
			return;
		currPosition -= step;

		if (currPosition < 0)
			currPosition = 0;

		Log.d(TAG, "fb " + currPosition);
		onDocUpdate();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean ret = false;

		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_LEFT:
			if (!isVodMenuVisible()) {
				if (!keyLeftDown) {
					keyLeftDown = true;
					keyLeftRepeat = 0;
					fbImage.setImageResource(R.drawable.vod_player_fb_focus);
					showPanel();
				} else {
					keyLeftRepeat++;
				}

				if (keyLeftRepeat == 0) {
					fastBackward(SHORT_FB_STEP);
				} else if ((keyLeftRepeat % KEY_REPEAT_COUNT) == 0) {
					if (clipLength != 0)
						fastBackward(clipLength / LONG_FB_PERCENT);
					else
						fastBackward(SHORT_FB_STEP);
				}
				ret = true;
			}
			break;
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			if (!isVodMenuVisible()) {
				if (!keyRightDown) {
					keyRightDown = true;
					keyRightRepeat = 0;
					ffImage.setImageResource(R.drawable.vod_player_ff_focus);
					showPanel();
				} else {
					keyRightRepeat++;
				}

				if (keyRightRepeat == 0) {
					fastForward(SHORT_FF_STEP);
				} else if ((keyRightRepeat % KEY_REPEAT_COUNT) == 0) {
					if (clipLength != 0)
						fastForward(clipLength / LONG_FF_PERCENT);
					else
						fastForward(SHORT_FF_STEP);
				}
				ret = true;
			}
			break;

		case KeyEvent.KEYCODE_DPAD_CENTER:
		case KeyEvent.KEYCODE_ENTER:
			if (!isVodMenuVisible()) {
				if (!keyOKDown) {
					if (!paused) {

						pauseItem();
						playPauseImage
								.setImageResource(R.drawable.vod_player_pause_focus);
					} else {

						resumeItem();
						playPauseImage
								.setImageResource(R.drawable.vod_player_play_focus);
					}
					showPanel();
					keyOKDown = true;
				}
				ret = true;
			}
			break;
		case KeyEvent.KEYCODE_A:
		case KeyEvent.KEYCODE_F1:
		case KeyEvent.KEYCODE_PROG_RED:
			if (!isVodMenuVisible()) {
				if (panelShow) {
					hidePanel();
				} else {
					showPanel();
				}
				ret = true;
			}
			break;
		case KeyEvent.KEYCODE_DPAD_UP:
			if (!isVodMenuVisible()) {
				showPanel();
				ret = true;
			}
			break;
		case KeyEvent.KEYCODE_BACK:
			if (panelShow) {
				hidePanel();
				ret = true;
			} else {
				popupstatus = 2;
				showPopupDialog(
						0,
						getResources().getString(
								R.string.vod_player_exit_dialog));
				ret = true;
			}
			break;
		default:
			break;
		}

		if (ret == false) {
			ret = super.onKeyDown(keyCode, event);
		}

		return ret;
	}

	public void seekTo() {
		Log.d(TAG, "seekTo currPosition=" + currPosition);
		if (!prepared || !playing)
			return;
		if (!seeking) {
			if (currPosition < 0)
				currPosition = 0;
			else if (currPosition > clipLength && clipLength != 0) {
				currPosition = clipLength;
				gotoplayerfinishpage(itemPK);
			}
			needSeek = false;

		} else {
			needSeek = true;
		}
		videoView.seekTo(currPosition);
		buffering = true;
		seeking = true;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		boolean ret = false;
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_LEFT:
			if (!isVodMenuVisible()) {
				fbImage.setImageResource(R.drawable.vod_player_fb);
				Log.d(TAG, "seek to " + currPosition);
				seekTo();
				keyLeftDown = false;
				ret = true;
			}
			break;
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			if (!isVodMenuVisible()) {
				ffImage.setImageResource(R.drawable.vod_player_ff);
				Log.d(TAG, "seek to " + currPosition);
				seekTo();
				keyRightDown = false;
				ret = true;
			}
			break;
		case KeyEvent.KEYCODE_DPAD_CENTER:
		case KeyEvent.KEYCODE_ENTER:
			if (!isVodMenuVisible()) {
				if (paused) {
					timeTaskPause();
					playPauseImage.setImageResource(R.drawable.vod_player_play);
				} else {
					timeTaskStart();
					playPauseImage
							.setImageResource(R.drawable.vod_player_pause);
				}
				keyOKDown = false;
				ret = true;
			}
			break;
		default:
			break;
		}

		if (ret == false) {
			ret = super.onKeyUp(keyCode, event);
		}

		return ret;
	}

	@Override
	public boolean onTouchEvent(MotionEvent evt) {
		boolean ret = false;

		switch (evt.getActionMasked()) {
		case MotionEvent.ACTION_DOWN:
			if (!panelShow) {
				showPanel();
				ret = true;
			}
			break;
		default:
			break;
		}

		return ret;
	}

	private void startVideo() {
		String url = getClipURL();

		if ((url != null) && ((currURL == null) || !(url.equals(currURL)))) {
			if (playing) {
				videoView.stopPlayback();

			}
			Log.d(TAG, "play URL " + url.toString() + ",currPosition="
					+ currPosition);
			videoView.setVideoPath(url.toString());
			playing = true;
			prepared = false;
			seeking = false;
			currURL = url;
			if (currPosition > 0 || isContinue) {
				seekTo();
			} else {
				currPosition = 0;
			}
			videoView.start();
			showPanel();
		}
	}

	private void stopVideo() {
		if (!playing)
			return;
		storeOffset();
		videoView.stopPlayback();
		showBuffer();
		playing = false;
		prepared = false;
		currURL = null;
		showPanel();
	}

	private void storeOffset() {
		int offset = (clipOffset == -1) ? -1 : (currPosition / 1000);

	}

	@Override
	protected void onPause() {
		Log.d(TAG, "!!!!!!!!!onPause");

		storeOffset();
		stopVideo();
		super.onPause();
	}

	@Override
	protected void onResume() {
		Log.d(TAG, "!!!!!!!!!onResume");

		super.onResume();

		startVideo();
	}

	private boolean episodeSubMenuCreated = false;

	private void addEpisodeSubMenu(ISTVVodMenu menu) {
		if (episodeSubMenuCreated)
			return;

		ISTVVodMenuItem episode_sub;

		episode_sub = menu.addSubMenu(10,
				getResources().getString(R.string.vod_player_episode));
		int i = 11;
		int j = 0;
		for (i = 11, j = 0; j < episoderealcount; i++, j++) {
			int spisodenum = j + 1;

			String str_tmp1 = getResources().getString(
					R.string.vod_player_prefixepisode);
			String str_tmp2 = new Integer(spisodenum).toString();
			String str_tmp3 = getResources().getString(
					R.string.vod_player_postfixepisode);

			episode_sub.addItem(i, str_tmp1 + str_tmp2 + str_tmp3);
		}

		menu.enable_scroll(false);

		episodeSubMenuCreated = true;
		// }
	}

	public boolean onCreateVodMenu(ISTVVodMenu menu) {
		ISTVVodMenuItem sub;
		ISTVVodMenuItem episode_sub;

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

		menu.addItem(5,
				getResources().getString(R.string.vod_player_bookmark_setting),
				false, false);
		menu.addItem(6,
				getResources().getString(R.string.vod_player_related_setting),
				false, false);

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
			} else {
				item.unselect();
			}
		}

		episodeSubMenuCreated = false;
		// addEpisodeSubMenu(menu);

		// menu.setTitle(
		// 5,
		// getResources().getString(
		// !clipBookmarked ? R.string.vod_player_bookmark_setting
		// : R.string.vod_player_remove_bookmark_setting));

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

	public boolean onVodMenuClicked(ISTVVodMenu menu, int id) {
		if (id > 0 && id < 5) {
			int pos = id - 1;
			if (urls[pos] != null) {
				setQuality(pos);
			}
		}

		return true;
	}

	private void gotoplayerfinishpage(int pk) {
		if (pk == -1)
			return;

		Bundle bundle = new Bundle();
		bundle.putInt("itemPK", pk);

		Intent intent = new Intent();
		intent.putExtras(bundle);

		startActivityForResult(intent, 1);
		finish();
	}

	private void gotorelatepage(int pk) {
		if (pk == -1)
			return;

		Bundle bundle = new Bundle();
		bundle.putInt("itemPK", pk);

		Intent intent = new Intent();
		intent.putExtras(bundle);

		startActivityForResult(intent, 1);
	}

	public void replayVideo() {
		if (playing) {
			Log.d(TAG, "replay the program");
			stopVideo();
			startVideo();
		}
	}

	public void onNetConnected() {
		replayVideo();
	}

	public boolean isVodMenuVisible() {
		if (menu == null)
			return false;
		return menu.isVisible();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu m) {
		boolean ret;
		if (menu != null && menu.isVisible())
			return false;
		m.add("menu");
		ret = super.onPrepareOptionsMenu(m);
		if (ret) {
			winOK = false;
			createWindow();
			menu = new ISTVVodMenu(this);
			ret = onCreateVodMenu(menu);
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

		if (winOK)
			return;

		ViewGroup root = (ViewGroup) findViewById(Window.ID_ANDROID_CONTENT);
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		win = inflater.inflate(R.layout.menu, null);
		ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);
		win.setLayoutParams(lp);
		root.addView(win);

		winOK = true;
	}

	private class AccessProxyTask extends AsyncTask<String, Void, ClipInfo> {

		protected void onPostExecute(ClipInfo result) {

			onDocGotResource(cinfo);
		}

		@Override
		protected ClipInfo doInBackground(String... arg0) {
			String sn = VodUserAgent.getMACAddress();
			AccessProxy.init(VodUserAgent.deviceType,
					VodUserAgent.deviceVersion, sn);
			cinfo = AccessProxy.parse("http://cms.tvxio.com/api/clip/"
					+ clip.pk + "/", VodUserAgent.getUserAgent(sn),
					PlayerActivity.this);

			return cinfo;

		}
	}

	private void showBuffer() {
		if (!bufferShow) {
			Log.d(TAG, "show buffer");
			bufferLayout.startAnimation(bufferShowAnimation);
			bufferLayout.setVisibility(View.VISIBLE);
			bufferAnim.start();
			bufferShow = true;
		}
	}

	private void hideBuffer() {
		if (bufferShow) {
			Log.d(TAG, "hide buffer");
			bufferLayout.startAnimation(bufferHideAnimation);
			bufferLayout.setVisibility(View.GONE);
			bufferAnim.stop();
			bufferShow = false;

		}
	}

	private void timeTaskStart() {
		mStartTime = SystemClock.uptimeMillis();
		mHandler.post(mUpdateTimeTask);
	}

	private void timeTaskPause() {
		mHandler.removeCallbacks(mUpdateTimeTask);
	}

	private Runnable mUpdateTimeTask = new Runnable() {
		public void run() {
			// System.out.println("videoView.isPlaying() == "+videoView.isPlaying());
			if (videoView.isPlaying()) {
				hideBuffer();
				long tempTime = SystemClock.uptimeMillis();
				currPosition += (int) (tempTime - mStartTime);
				mStartTime = tempTime;
				String text = getTimeString(currPosition) + "/"
						+ getTimeString(clipLength);
				timeText.setText(text);
				int val = currPosition * 100 / clipLength;
				timeBar.setProgress(val);
			} else {
				showBuffer();
				timeTaskPause();
			}
			mHandler.postDelayed(mUpdateTimeTask, 200);
		}
	};

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
			menu.hide();
			hideMenuHandler.removeCallbacks(hideMenuRunnable);
		}
	};
	private Dialog popupDlg = null;

	public void showPopupDialog(int type, String msg) {
		if (type == DIALOG_OK_CANCEL) {
			popupDlg = new Dialog(this, R.style.PopupDialog);
			View view;
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			if ((type == DIALOG_IKNOW) || (type == DIALOG_NET_BROKEN)
					|| (type == DIALOG_ITEM_CLICK_NET_BROKEN)) {
				view = inflater.inflate(R.layout.popup_1btn, null);
			} else {
				view = inflater.inflate(R.layout.popup_2btn, null);
			}
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
						if (popupDlg != null) {
							popupDlg.dismiss();
							PlayerActivity.this.finish();
							videoView.stopPlayback();
							videoView.destroyDrawingCache();
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
