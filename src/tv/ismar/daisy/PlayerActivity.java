package tv.ismar.daisy;

import java.io.InputStream;

import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.ImageUtils;
import tv.ismar.daisy.core.NetworkUtils;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.VodUserAgent;
import tv.ismar.daisy.models.Clip;
import tv.ismar.daisy.models.Favorite;
import tv.ismar.daisy.models.History;
import tv.ismar.daisy.models.Item;
import tv.ismar.daisy.persistence.FavoriteManager;
import tv.ismar.daisy.persistence.HistoryManager;
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
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.ismartv.api.AccessProxy;
import com.ismartv.bean.ClipInfo;

public class PlayerActivity extends Activity {

	@SuppressWarnings("unused")
	private static final String PREFS_NAME = "tv.ismar.daisy";
	private static final String TAG = "PLAYER";
	
	
	private static final int SHORT_STEP = 20000;
	private static final int DIALOG_OK_CANCEL = 0;
	
	private boolean paused = false;
	private boolean isBuffer = true;
	private boolean panelShow = false;
	private int currQuality = 0;
	private String urls[] = new String[6];
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
	private boolean isContinue = true;
	private ISTVVodMenu menu = null;
	private ClipInfo urlInfo = new ClipInfo();
	private Handler mHandler = new Handler();
	private Handler mCheckHandler = new Handler();
	private int tempOffset = 0;
	private Item item;
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
	private int seekPostion = 0 ;
	private boolean isSeek = false;
	private FavoriteManager favoriteManager;
	private Favorite favorite;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setView();
	}
	
	private void setView(){
		panelShowAnimation = AnimationUtils.loadAnimation(this,R.drawable.fly_up);
		panelHideAnimation = AnimationUtils.loadAnimation(this,R.drawable.fly_down);
		bufferShowAnimation = AnimationUtils.loadAnimation(this,R.drawable.fade_in);
//		bufferHideAnimation = AnimationUtils.loadAnimation(this,R.drawable.fade_out);
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		setContentView(R.layout.vod_player);
		mVideoView = (VideoView) findViewById(R.id.video_view);
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
			simpleRestClient = new SimpleRestClient();
			Object obj = bundle.get("url");
			String sn = VodUserAgent.getMACAddress();
			AccessProxy.init(VodUserAgent.deviceType,VodUserAgent.deviceVersion, sn);
			if(obj!=null){
				item = simpleRestClient.getItem((String)obj);
				if(item!=null){
					clip = item.clip;
					String host = "cord.tvxio.com";
//							try {
//								host = (new URL((String)obj)).getHost();
//							} catch (MalformedURLException e) {
//								e.printStackTrace();
//							}
					urlInfo = AccessProxy.parse("http://" + host  + "/api/clip/"+clip.pk+"/", VodUserAgent.getAccessToken(sn),PlayerActivity.this);
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
						urlInfo = AccessProxy.parse("http://" + host  + "/api/clip/"+clip.pk+"/", VodUserAgent.getAccessToken(sn),PlayerActivity.this);
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
			if(logoInputStream!=null){
				logoImage.setImageBitmap(ImageUtils.getBitmapFromInputStream(logoInputStream, 160, 50));
			}
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
			favoriteManager = DaisyUtils.getFavoriteManager(this);
			historyManager = DaisyUtils.getHistoryManager(this);
			itemUrl = simpleRestClient.root_url+"/api/item/"+item.item_pk+"/";
			Log.d(TAG, "historyManager getHistoryByUrl == "+itemUrl);
			mHistory = historyManager.getHistoryByUrl(itemUrl);
			favorite = favoriteManager.getFavoriteByUrl(itemUrl);
			if(mHistory!=null){
				isContinue = mHistory.is_continue;
				tempOffset =  (int) mHistory.last_position;
				currQuality = mHistory.last_quality;
			}
			
			Log.d(TAG, "tempOffset == "+tempOffset);
		}
		
		if(item!=null){
			titleText.setText(item.title);
			titleText.setSelected(true);
			new LogoImageTask().execute();
		}
		
		
		if (tempOffset>0&&isContinue){
			currPosition = tempOffset;
			seekPostion = tempOffset;
		}
		Log.d(TAG, "RES_INT_OFFSET currPosition=" + currPosition);
		
		mVideoView.setVideoPath(urls[currQuality]);
		
		mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				
				Log.d(TAG, "mVideoView onPrepared tempOffset =="+tempOffset);
					if(mVideoView!=null){
						clipLength = mVideoView.getDuration();
						timeBar.setMax(clipLength);
						mVideoView.start();
						if(tempOffset>0&&isContinue){
							mVideoView.seekTo(tempOffset);
						}
						timeTaskStart();
						checkTaskStart();
					}
			}
		});
		showPanel();
	}

	
	private void timeTaskStart() {
		mHandler.post(mUpdateTimeTask);
	}

	private void timeTaskPause() {
		mHandler.removeCallbacks(mUpdateTimeTask);
	}

	private Runnable mUpdateTimeTask = new Runnable() {
		public void run() {
			if(mVideoView!=null){
				if (mVideoView.isPlaying()){
					seekPostion = mVideoView.getCurrentPosition();
					if(!isSeek){
						timeBar.setProgress(currPosition);
						currPosition = mVideoView.getCurrentPosition();
					}
				}else{
					if(mVideoView.getDuration()>0&&mVideoView.getCurrentPosition()>0&&mVideoView.getDuration()-mVideoView.getCurrentPosition()<3000){
						gotoFinishPage();
					}
				}
				mHandler.postDelayed(mUpdateTimeTask, 1000);
			}else{
				timeTaskPause();
			}
		}
	};
	
	
	private void checkTaskStart() {
		mCheckHandler.post(checkStatus);
	}

	private void checkTaskPause() {
		mCheckHandler.removeCallbacks(checkStatus);
	}

	private Runnable checkStatus = new Runnable(){
		public void run() {
			if(mVideoView!=null){
				Log.d(TAG, "seekPostion == "+Math.abs(mVideoView.getCurrentPosition()-seekPostion));
				if (mVideoView.isPlaying()&&Math.abs(mVideoView.getCurrentPosition()-seekPostion)>0) {
					Log.d(TAG, "seekPostion == "+Math.abs(mVideoView.getCurrentPosition()-seekPostion));
					if(isBuffer||bufferAnim.isRunning()){
						isBuffer = false;
						hideBuffer();
					}
					
				}else{
					seekPostion = mVideoView.getCurrentPosition();
					isBuffer = true;
					showBuffer();
				}
				mCheckHandler.postDelayed(checkStatus, 2000);
			}else{
				checkTaskPause();
			}
		
		}
		
	};
	
	
	private void gotoFinishPage() {
		Intent intent = new Intent("tv.ismar.daisy.PlayFinished");
		intent.putExtra("item", item);
		startActivity(intent);
		timeTaskPause();
		checkTaskPause();
		seekPostion = 0;
		currPosition = 0;
		mVideoView = null;
		addHistory(0);
		PlayerActivity.this.finish();
	}
	private void gotoRelatePage() {
		Intent intent = new Intent();
		intent.setClass(PlayerActivity.this, tv.ismar.daisy.RelatedActivity.class);
		intent.putExtra("item", item);
		startActivity(intent);
		timeTaskPause();
		checkTaskPause();
		addHistory(currPosition);
		seekPostion = 0;
		currPosition = 0;
		mVideoView = null;
		PlayerActivity.this.finish();
	}
	private void addHistory(int last_position){
		Log.d(TAG, "historyManager item.title =="+item.title);
		Log.d(TAG, "historyManager itemUrl =="+itemUrl);
		Log.d(TAG, "historyManager last_position =="+last_position);
		History history = new History();
		history.title = item.title;
		history.adlet_url = item.adlet_url;
		history.content_model = item.content_model;
		history.is_complex = item.is_complex;
		history.last_position = last_position;
		history.last_quality = currQuality;
		history.url = itemUrl;
		history.is_continue = isContinue;
		historyManager.addFavorite(history);
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
		if (paused||mVideoView==null)
			return;
//		showBuffer();
		Log.d(TAG, "pause");
		mVideoView.pause();
		paused = true;
	}

	private void resumeItem() {
		if (!paused||mVideoView==null)
			return;
//		hideBuffer();
		Log.d(TAG, "resume");
		mVideoView.start();
		if(!isBuffer){
			timeTaskStart();
		}
		paused = false;
	}

	private void fastForward(int step) {
		if (currPosition > clipLength)
			return;
		currPosition += step;

		if (currPosition > clipLength){
			gotoFinishPage();
		}
		timeBar.setProgress(currPosition);
		Log.d(TAG, "seek Forward " + currPosition);
	}

	private void fastBackward(int step) {
		if (currPosition < 0)
			return;
		currPosition -= step;

		if (currPosition < 0)
			currPosition = 0;
		timeBar.setProgress(currPosition);
		Log.d(TAG, "seek Backward " + currPosition);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean ret = false;
		if (!isVodMenuVisible()&&mVideoView!=null) {
			switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_LEFT:
				if(mVideoView.getDuration()>0){
					isSeek = true;
					showPanel();
					fbImage.setImageResource(R.drawable.vod_player_fb_focus);
					fastBackward(SHORT_STEP);
					ret = true;
				}
				break;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				if(mVideoView.getDuration()>0){
					isSeek = true;
					showPanel();
					ffImage.setImageResource(R.drawable.vod_player_ff_focus);
					fastForward(SHORT_STEP);
					ret = true;
				}
				break;
			case KeyEvent.KEYCODE_DPAD_CENTER:
			case KeyEvent.KEYCODE_ENTER:
				if(mVideoView.getDuration()>0){
					showPanel();
					if (!paused) {
						pauseItem();
						playPauseImage.setImageResource(R.drawable.vod_player_pause_focus);
					} else {
						resumeItem();
						playPauseImage.setImageResource(R.drawable.vod_player_play_focus);
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
			if (mVideoView!=null&&ret == false) {
				ret = super.onKeyDown(keyCode, event);
			}
		return ret;
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		boolean ret = false;
		if(mVideoView!=null&&!isVodMenuVisible()&&mVideoView.getDuration()>0){
			switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_LEFT:
					fbImage.setImageResource(R.drawable.vod_player_fb);
					mVideoView.seekTo(currPosition);
					Log.d(TAG, "LEFT seek to " + getTimeString(currPosition));
					ret = true;
					isSeek = false;
				break;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
					ffImage.setImageResource(R.drawable.vod_player_ff);
					mVideoView.seekTo(currPosition);
					Log.d(TAG, "RIGHT seek to" + getTimeString(currPosition));
					ret = true;
					isSeek = false;
				break;
			case KeyEvent.KEYCODE_DPAD_CENTER:
			case KeyEvent.KEYCODE_ENTER:
					if (paused) {
						playPauseImage.setImageResource(R.drawable.vod_player_play);
					} else {
						playPauseImage.setImageResource(R.drawable.vod_player_pause);
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
			popupDlg.addContentView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT));
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
							addHistory(currPosition);
							checkTaskPause();
							popupDlg.dismiss();
							mVideoView = null;
							timeTaskPause();
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
		if(isBuffer&&!bufferAnim.isRunning()){
			Log.d(TAG, "show buffer");
			bufferLayout.setVisibility(View.VISIBLE);
			bufferLayout.startAnimation(bufferShowAnimation);
			bufferAnim.start();
		}
	}
	
	private void hideBuffer() {		
		if(!isBuffer&&bufferAnim.isRunning()){
			Log.d(TAG, "hide buffer");
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
	
	public boolean createMenu(ISTVVodMenu menu) {
		ISTVVodMenuItem sub;

		sub = menu.addSubMenu(0,getResources().getString(R.string.vod_player_quality_setting));
		sub.addItem(1,getResources().getString(R.string.vod_player_quality_medium));
		sub.addItem(2,getResources().getString(R.string.vod_player_quality_high));
		sub.addItem(3,getResources().getString(R.string.vod_player_quality_ultra));
		sub.addItem(4,getResources().getString(R.string.vod_player_quality_adaptive));
		
		if(favoriteManager.getFavoriteByUrl(itemUrl)==null){
			menu.addItem(5,getResources().getString(R.string.vod_player_bookmark_setting));
		}else{
			menu.addItem(5,getResources().getString(R.string.vod_bookmark_remove_bookmark_setting));
		}
		menu.addItem(6,getResources().getString(R.string.vod_player_related_setting));

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
			updataTimeText();
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
		if(favoriteManager.getFavoriteByUrl(itemUrl)!=null){
			menu.findItem(5).setTitle(getResources().getString(R.string.vod_bookmark_remove_bookmark_setting));
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
		ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
		win.setLayoutParams(lp);
		root.addView(win);
	}
	//reset()-->setDataSource(path)-->prepare()-->start()-->stop()--reset()-->
	/**
	 * @param menu
	 * @param id
	 * @return
	 */
	public boolean onVodMenuClicked(ISTVVodMenu menu, int id) {
		if (id > 0 && id < 5) {
			int pos = id - 1;
			if (urls[pos] != null) {
				try {
					timeTaskPause();
					isBuffer = true;
					currQuality = pos;
					mVideoView = (VideoView) findViewById(R.id.video_view);
					mVideoView.setVideoPath(urls[currQuality].toString());
					if(currPosition>0){
						seekPostion = 0;
						mVideoView.seekTo(currPosition);
						
					}
					timeTaskStart();
					return true;
				}catch (Exception e) {
					Log.d(TAG,"Exception change url " + e);
					return false;
				}
			}
			return true;
		}
		if(id==5){
			if(favoriteManager.getFavoriteByUrl(itemUrl)!=null){
				favoriteManager.deleteFavoriteByUrl(itemUrl);
				menu.findItem(5).setTitle(getResources().getString(R.string.vod_player_bookmark_setting));
			}else{
				if(item!=null&&itemUrl!=null){
					favorite = new Favorite();
					favorite.adlet_url = item.adlet_url;
					favorite.content_model = item.content_model;
					favorite.is_complex = item.is_complex;
					favorite.title = item.title;
					favorite.url = itemUrl;
					favoriteManager.addFavorite(favorite);
					menu.findItem(5).setTitle(getResources().getString(R.string.vod_bookmark_remove_bookmark_setting));
				}
			}
			return true;
		}
		if(id==6){
			gotoRelatePage();
			return true;
		}
		if(id==8){
			isContinue = true;
			addHistory(seekPostion);
			return true;
		}
		if(id==9){
			isContinue = false;
			addHistory(seekPostion);
			return true;
		}
		return true;
	}
	
	private void updataTimeText(){
		String text = getTimeString(currPosition) + "/" + getTimeString(clipLength);
		timeText.setText(text);
	}

}