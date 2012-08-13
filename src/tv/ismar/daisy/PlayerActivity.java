package tv.ismar.daisy;

import java.io.IOException;
import java.io.InputStream;

import tv.ismar.daisy.core.ImageUtils;
import tv.ismar.daisy.core.NetworkUtils;
import tv.ismar.daisy.core.SimpleRestClient;
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
import android.content.pm.ActivityInfo;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
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

import com.ismartv.api.AccessProxy;
import com.ismartv.bean.ClipInfo;

public class PlayerActivity extends Activity implements SurfaceHolder.Callback{

	private static final String PREFS_NAME = "tv.ismar.daisy";
	private static final String TAG = "PLAYER";
	
	private static final int SHORT_STEP = 30000;

	private static final int LONG_FB_PERCENT = 10;
	private static final int LONG_FF_PERCENT = 10;
	private static final int KEY_REPEAT_COUNT = 4;
	
	private static final int DIALOG_OK_CANCEL = 0;
	private static final int DIALOG_IKNOW = 2;
	private static final int DIALOG_NET_BROKEN = 3;

	
	private static final int DIALOG_ITEM_CLICK_NET_BROKEN = 6;
	
	private boolean paused = false;
	private boolean panelShow = false;
	private int currQuality = 0;
	private String urls[] = new String[6];
	private boolean noFinish = false;
	private Animation panelShowAnimation;
	private Animation panelHideAnimation;
	private Animation bufferShowAnimation;
//	private Animation bufferHideAnimation;
	private RelativeLayout bufferLayout;
	private ImageView logoImage;
	private AnimationDrawable bufferAnim;
	private LinearLayout panelLayout;
	private TextView titleText;
	private TextView timeText;
	private ImageView playPauseImage;
	private ImageView ffImage;
	private ImageView fbImage;
	private int clipLength = 0;
	private int currPosition = 0;
	private int clipOffset = 0;
	private boolean isContinue = true;
	private ISTVVodMenu menu = null;
	private ClipInfo urlInfo = new ClipInfo();
	private Handler mHandler = new Handler();
	private long mStartTime = 0;
	private int tempOffset = 0;
	private Item item;
	private Clip clip;
	private Bundle bundle;
	private long tempTime;
	private SeekBar timeBar;
	private SurfaceView surfaceView;
	private SurfaceHolder surfaceHolder;
	private MediaPlayer mediaPlayer;
	private Dialog popupDlg = null;
	private InputStream logoInputStream;
	
	private boolean keyLeftDown = false;
	private boolean keyRightDown = false;
	private boolean keyOKDown = false;
	private int keyLeftRepeat = 0;
	private int keyRightRepeat = 0;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		noFinish = false;
		setView();
	}
	
	@SuppressWarnings("deprecation")
	private void setView(){
		panelShowAnimation = AnimationUtils.loadAnimation(this,R.drawable.fly_up);
		panelHideAnimation = AnimationUtils.loadAnimation(this,R.drawable.fly_down);
		bufferShowAnimation = AnimationUtils.loadAnimation(this,R.drawable.fade_in);
//		bufferHideAnimation = AnimationUtils.loadAnimation(this,R.drawable.fade_out);
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		setContentView(R.layout.vod_player);
		surfaceView = (SurfaceView) findViewById(R.id.surface_view);
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setSizeFromLayout(); 
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		panelLayout = (LinearLayout) findViewById(R.id.PanelLayout);
		titleText = (TextView) findViewById(R.id.TitleText);
		timeText = (TextView) findViewById(R.id.TimeText);
		timeBar = (SeekBar) findViewById(R.id.TimeSeekBar);
		timeBar.setOnSeekBarChangeListener(new SeekBarChangeEvent());
		playPauseImage = (ImageView) findViewById(R.id.PlayPauseImage);
		ffImage = (ImageView) findViewById(R.id.FFImage);
		fbImage = (ImageView) findViewById(R.id.FBImage);
		bufferLayout = (RelativeLayout) findViewById(R.id.BufferLayout);
		bufferAnim = (AnimationDrawable) ((ImageView) findViewById(R.id.BufferImage)).getBackground();
		logoImage = (ImageView) findViewById(R.id.logo_image);
		isContinue = getSharedPreferences(PREFS_NAME, 0).getBoolean("continue_play", true);
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.d(TAG, "surfaceCreated");
		
		initClipInfo();
	}
	
	private void initClipInfo() {
		Intent intent = getIntent();
		if (intent != null) {
			bundle = intent.getExtras();
			new ItemByUrlTask().execute();
		}
	}
	
	
	//初始化播放地址url
	private class ItemByUrlTask extends AsyncTask<String, Void, ClipInfo> {
			
		@Override
		protected void onPostExecute(ClipInfo result) {
			showBuffer();
			initPlayer();
		}
		@Override
		protected ClipInfo doInBackground(String... arg0) {
			Object obj = bundle.get("url");
			String sn = VodUserAgent.getMACAddress();
			if(obj!=null){
				SimpleRestClient simpleRestClient = new SimpleRestClient();
				item = simpleRestClient.getItem((String)obj);
				
				AccessProxy.init(VodUserAgent.deviceType,VodUserAgent.deviceVersion, sn);
				if(item!=null){
					clip = item.clip;
					String host = "cord.tvxio.com";
//							try {
//								host = (new URL((String)obj)).getHost();
//							} catch (MalformedURLException e) {
//								e.printStackTrace();
//							}
					urlInfo = AccessProxy.parse("http://" + host  + "/api/clip/"+ clip.pk + "/", VodUserAgent.getUserAgent(sn),PlayerActivity.this);
				}
			}else{
				obj = bundle.get("item");
				if(obj!=null){
					item = (Item)obj;
					if(item.clip!=null){
						clip = item.clip;
						String host = "cord.tvxio.com";
	//							try {
	//								host = (new URL((String)obj)).getHost();
	//							} catch (MalformedURLException e) {
	//								e.printStackTrace();
	//							}
						urlInfo = AccessProxy.parse("http://" + host  + "/api/clip/"+ clip.pk + "/", VodUserAgent.getUserAgent(sn),PlayerActivity.this);
					}
				}
			}
			
			return urlInfo;
			
		}
	}
	
	//初始化logo图片
	private class LogoImageTask extends AsyncTask<String, Void, String> {
		
		@Override
		protected void onPostExecute(String result) {
			logoImage.setImageBitmap(ImageUtils.getBitmapFromInputStream(logoInputStream, 160, 50));
		}
		@Override
		protected String doInBackground(String... arg0) {
			if(item.logo!=null&&item.logo.length()>0)
				if(NetworkUtils.getInputStream(item.logo)!=null){
					Log.d(TAG, "item.logo ===" + item.logo);
					logoInputStream = NetworkUtils.getInputStream(item.logo);
				}
			return "success";
			
		}
	}
	
	
	private void initPlayer() {
		urls[0] = urlInfo.getNormal();
		urls[1] = urlInfo.getMedium();
		urls[2] = urlInfo.getHigh();
		urls[3] = urlInfo.getAdaptive();
		if(item!=null){
			titleText.setText(item.title);
			titleText.setSelected(true);
			new LogoImageTask().execute();
		}
		clipLength = clip.length * 1000;
		
		if (currPosition>0||isContinue)
			currPosition = tempOffset * 1000;
		else
			clipOffset = 0;
		Log.d(TAG, "RES_INT_OFFSET currPosition=" + currPosition + ",clipOffset=" + clipOffset);
		
		try {

			if(mediaPlayer!=null){
				mediaPlayer.release();
			}else{
				mediaPlayer = new MediaPlayer();
			}
			mediaPlayer.setDisplay(surfaceHolder);
			mediaPlayer.setDataSource(urls[currQuality]);
			mediaPlayer.prepareAsync();
			
		} catch (Exception e) {
			Log.d(TAG, e.getMessage());
			e.printStackTrace();
		}
		mediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
			@Override
			public boolean onInfo(MediaPlayer mp, int what, int extra) {
				switch (what) {
				case MediaPlayer.MEDIA_INFO_BUFFERING_START:
					Log.d(TAG, "buffering start");
					showBuffer();
					timeTaskPause();
					break;
				case MediaPlayer.MEDIA_INFO_BUFFERING_END:
					Log.d(TAG, "buffering end");
					hideBuffer();
					timeTaskStart();
					break;
				}
				return true;
			}
		});
		mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
			@Override
			public void onBufferingUpdate(MediaPlayer arg0,int bufferingProgress) {
//				timeBar.setSecondaryProgress(bufferingProgress);
//				int currentProgress = skbProgress.getMax()* mediaPlayer.getCurrentPosition()/ mediaPlayer.getDuration();
//				Log.d(TAG ,"% play "+bufferingProgress + "% buffer");

			}
		});

		mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

			@Override
			public void onPrepared(MediaPlayer mp) {
				if(mediaPlayer.getVideoWidth()>0&&mediaPlayer.getVideoHeight()>0){
					clipLength = mediaPlayer.getDuration();
					String text = getTimeString(currPosition) + "/" + getTimeString(clipLength);
					timeText.setText(text);
					mediaPlayer.start();
					timeBar.setMax(clipLength);
					timeTaskStart();
				}
			}
		});
		mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				Log.d(TAG, "mediaPlayer onCompletion");
				timeTaskPause();
				if(!noFinish){
					noFinish = true;
					gotofinishpage();
				}
			}
		});
		mediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener(){

			@Override
			public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
			
//				Log.d(TAG, "MediaPlayer onVideoSizeChanged width =="+width);
//				Log.d(TAG, "MediaPlayer onVideoSizeChanged height =="+height);
//				
//				Log.d(TAG, "SurfaceViewWidth == "+ surfaceView.getWidth() );
//				Log.d(TAG, "SurfaceViewHeight == "+ surfaceView.getHeight());
			}
			
		});
		showPanel();
	}
//	private class AccessProxyTask extends AsyncTask<String, Void, ClipInfo> {
//
//		protected void onPostExecute(ClipInfo result) {
//				showBuffer();
//				initPlayer();
//		}
//
//		@Override
//		protected ClipInfo doInBackground(String... arg0) {
//			String sn = VodUserAgent.getMACAddress();
//			AccessProxy.init(VodUserAgent.deviceType,VodUserAgent.deviceVersion, sn);
//			String host = "cord.tvxio.com";
//			// try {
//			// host = (new URL(item.poster_url)).getHost();
//			// } catch (MalformedURLException e) {
//			// e.printStackTrace();
//			// }
//			urlInfo = AccessProxy.parse("http://" + host + "/api/clip/" + clip.pk + "/", VodUserAgent.getUserAgent(sn), PlayerActivity.this);
//			return urlInfo;
//
//		}
//	}
	
	private void timeTaskStart() {
		mStartTime = SystemClock.uptimeMillis();
		mHandler.post(mUpdateTimeTask);
	}

	private void timeTaskPause() {
		mHandler.removeCallbacks(mUpdateTimeTask);
	}

	private Runnable mUpdateTimeTask = new Runnable() {
		public void run() {
			if (mediaPlayer.isPlaying()) {
				if(bufferAnim.isRunning()){
					hideBuffer();
				}
				tempTime = SystemClock.uptimeMillis();
				currPosition += (int) (tempTime - mStartTime);
				mStartTime = tempTime;
				timeBar.setProgress(currPosition);
			}
			mHandler.postDelayed(mUpdateTimeTask, 200);
		}
	};
	private void gotofinishpage() {
		Intent intent = new Intent("tv.ismar.daisy.PlayFinished");
		intent.putExtra("item", item);
		startActivity(intent);
		finish();
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
		if (paused||mediaPlayer==null)
			return;
		showBuffer();
		Log.d(TAG, "pause");
		mediaPlayer.pause();
		paused = true;
	}

	private void resumeItem() {
		if (!paused||mediaPlayer==null)
			return;
		hideBuffer();
		mediaPlayer.start();
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
	}

	private void fastBackward(int step) {
		if (currPosition == 0)
			return;
		currPosition -= step;

		if (currPosition < 0)
			currPosition = 0;

		Log.d(TAG, "fb " + currPosition);
	}
	
	public void seekTo() {
		Log.d(TAG, "seekTo currPosition=" + currPosition);
		if (currPosition > clipLength && clipLength != 0) {
			currPosition = clipLength;
			gotofinishpage();
		}
		mediaPlayer.seekTo(currPosition);
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
					fastBackward(SHORT_STEP);
				} else if ((keyLeftRepeat % KEY_REPEAT_COUNT) == 0) {
					if (clipLength != 0)
						fastBackward(clipLength / LONG_FB_PERCENT);
					else
						fastBackward(SHORT_STEP);
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
					fastForward(SHORT_STEP);
				} else if ((keyRightRepeat % KEY_REPEAT_COUNT) == 0) {
					if (clipLength != 0)
						fastForward(clipLength / LONG_FF_PERCENT);
					else
						fastForward(SHORT_STEP);
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
							timeTaskPause();
							noFinish = true;
							if(mediaPlayer!=null)
								mediaPlayer.reset();
							PlayerActivity.this.finish();
							surfaceView.destroyDrawingCache();
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
			menu.hide();
			hideMenuHandler.removeCallbacks(hideMenuRunnable);
		}
	};
	
	
	private boolean isVodMenuVisible() {
		if (menu == null)
			return false;
		return menu.isVisible();
	}
	
	private void showBuffer() {
		Log.d(TAG, "show buffer");
		if(!bufferAnim.isRunning()){
			bufferLayout.setVisibility(View.VISIBLE);
			bufferLayout.startAnimation(bufferShowAnimation);
			bufferAnim.start();
		}
	}
	
	private void hideBuffer() {	
		Log.d(TAG, "hide buffer");
		if(bufferAnim.isRunning()){
			bufferLayout.setVisibility(View.GONE);
			bufferAnim.stop();
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
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
//		mediaPlayer.setScreenOnWhilePlaying(screenOn)
		 
		Log.d(TAG, "surfaceChanged");
		
	}
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(TAG, "surfaceDestroyed");
		
	}
	
	
	
	public boolean createMenu(ISTVVodMenu menu) {
		ISTVVodMenuItem sub;

		sub = menu.addSubMenu(0,getResources().getString(R.string.vod_player_quality_setting));
		sub.addItem(1,getResources().getString(R.string.vod_player_quality_medium));
		sub.addItem(2,getResources().getString(R.string.vod_player_quality_high));
		sub.addItem(3,getResources().getString(R.string.vod_player_quality_ultra));
		sub.addItem(4,getResources().getString(R.string.vod_player_quality_adaptive));

		menu.addItem(5,getResources().getString(R.string.vod_player_bookmark_setting),false, false);
		menu.addItem(6,getResources().getString(R.string.vod_player_related_setting),false, false);

		sub = menu.addSubMenu(7,getResources().getString(R.string.vod_player_continue_setting));
		sub.addItem(8, getResources().getString(R.string.vod_player_continue_on));
		sub.addItem(9, getResources().getString(R.string.vod_player_continue_off));
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
		public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
			String text = getTimeString(currPosition) + "/" + getTimeString(clipLength);
			timeText.setText(text);
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			Log.d(TAG, "onStartTrackingTouch"+seekBar.getProgress());
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			Log.d(TAG, "onStopTrackingTouch"+seekBar.getProgress());
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
		return ret;
	}
	@Override
	public boolean onMenuOpened(int featureId, Menu m) {
		if (onVodMenuOpened(menu)) {
			menu.show();
			hideMenuHandler.postDelayed(hideMenuRunnable, 5000);
		}
		return false;
	}
	
	private void createWindow() {
		View win;
		ViewGroup root = (ViewGroup) findViewById(Window.ID_ANDROID_CONTENT);
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		win = inflater.inflate(R.layout.menu, null);
		ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
		win.setLayoutParams(lp);
		root.addView(win);
	}
	
	public boolean onVodMenuClicked(ISTVVodMenu menu, int id) {
		if (id > 0 && id < 5) {
			int pos = id - 1;
			if (urls[pos] != null) {
				try {
					noFinish = true;
					mediaPlayer.reset();
					currQuality = pos;
					mediaPlayer.setDataSource(urls[currQuality].toString());
					mediaPlayer.prepare();
					mediaPlayer.start();
				} catch (IllegalArgumentException e) {
					
					e.printStackTrace();
				} catch (SecurityException e) {
				
					e.printStackTrace();
				} catch (IllegalStateException e) {
					
					e.printStackTrace();
				} catch (IOException e) {
			
					e.printStackTrace();
				}
				Log.d(TAG, "play URL " + urls[currQuality].toString() + ",currPosition="+ currPosition);
				if (currPosition > 0 || isContinue) {
					mediaPlayer.seekTo(currPosition);
				} else {
					currPosition = 0;
				}
			}
		}
		return true;
	}
	
//	private void savaScreenShot()
//    {
//		
//        mediaPlayer.pause();
//        Bitmap bitmap = null;
//        try
//        {
//        	surfaceView.setDrawingCacheEnabled(true);
//            bitmap = surfaceView.getDrawingCache();
//            surfaceView.setDrawingCacheEnabled(false);
//            surfaceView.destroyDrawingCache();
//        }
//        catch (Exception ex)
//        {
//        	surfaceView.setDrawingCacheEnabled(false);
//            surfaceView.destroyDrawingCache();
//
//        }finally
//	        {
//	        //截图保存路径
//	        String mScreenshotPath = Environment.getExternalStorageDirectory() + "/ScreenShot";
//	        String path = mScreenshotPath ;
//	        
//	        java.io.File file = new java.io.File(path);
//	        
//	        java.io.FileOutputStream fos;
//	        try
//	        {
//	            Toast.makeText(PlayerActivity.this, "savaScreenShot success" + path, Toast.LENGTH_SHORT).show();
//	            fos = new java.io.FileOutputStream(file);
//	            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
//	            fos.close();
//	        }
//	        catch (java.io.FileNotFoundException e)
//	        {
//	            Log.e("Panel", "FileNotFoundException", e);
//	        }
//	        catch (IOException e)
//	        {
//	            Log.e("Panel", "IOEception", e);
//	        }
//	        mediaPlayer.start();
//	    }
//	}
	
	
//	public static Bitmap getHttpBitmap(String url){
//
//		  URL myFileURL;
//		  Bitmap bitmap=null;
//		
//		  try{
//			  myFileURL = new URL(url);
//			  HttpURLConnection  conn = (HttpURLConnection)myFileURL.openConnection();
//			  conn.setConnectTimeout(6000);
//			  conn.setUseCaches(false);
//			  conn=(HttpURLConnection)myFileURL.openConnection();
//			  InputStream is = conn.getInputStream();
//			  bitmap = BitmapFactory.decodeStream(is);
//			  is.close();
//			  conn.disconnect();
//		  }catch(Exception e){
//			
//			  e.printStackTrace();
//		  }   
//		  return bitmap;
//
//		}

	

//	private void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//		Log.d(TAG, "onMeasure");
//		int width = surfaceView.getDefaultSize(mVideoWidth, widthMeasureSpec);
//		int height = surfaceView.getDefaultSize(mVideoHeight, heightMeasureSpec);
//		if (mVideoWidth > 0 && mVideoHeight > 0) {
//			if (mVideoWidth * height > width * mVideoHeight) {
//				Log.d(TAG, "image too tall, correcting");
//				height = width * mVideoHeight / mVideoWidth;
//			} else if (mVideoWidth * height < width * mVideoHeight) {
//				Log.d(TAG, "image too wide, correcting");
//				width = height * mVideoWidth / mVideoHeight;
//			} else {
//				Log.d(TAG, "aspect ratio is correct: " + width+"/"+height+"="+ mVideoWidth+"/"+mVideoHeight);
//			}
//		}
//		Log.d(TAG, "setting size: " + width + 'x' + height);
//		
//	}
}