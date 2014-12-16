package tv.ismar.daisy;

import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.dao.DBHelper;
import tv.ismar.daisy.views.ChannelFragment;
import tv.ismar.daisy.views.FavoriteFragment;
import tv.ismar.daisy.views.HistoryFragment;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;

public class ChannelListActivity extends Activity {
	
	private final static String TAG = "ChannelListActivity";
	
	private OnMenuToggleListener mOnMenuToggleListener;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.channel_layout);		  
		Intent intent = getIntent();
		 DisplayMetrics metric = new DisplayMetrics();
	        getWindowManager().getDefaultDisplay().getMetrics(metric);
	        int width = metric.widthPixels;  // 屏幕宽度（像素）
	        int height = metric.heightPixels;  // 屏幕高度（像素）
	        float density = metric.density;  // 屏幕密度（0.75 / 1.0 / 1.5）
	        int densityDpi = metric.densityDpi;  // 屏幕密度DPI（120 / 160 / 240）
    	        Log.i("zjq", "densityDpi=="+densityDpi+"heightPixels="+height);
    	        float rate = (float)densityDpi/(float)160;
    	        DBHelper.rate = rate;
		String title = null;
		String url = null;
		String channel = null;
		if(intent!=null){
			Bundle bundle = intent.getExtras();
			if(bundle!=null){
				url =bundle.getString("url");
				
				title = bundle.getString("title");
				
				channel = bundle.getString("channel");
			}
		}
		if(url==null) {
			url = "http://cord.tvxio.com/api/tv/sections/chinesemovie/";
		//	url = "http://cord.tvxio.com/api/live/channel/movie/";
		}
		if(title==null) {
			title = "华语电影";
		}
		if(channel==null) {
			channel = "$histories_dd";
		}
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		if(channel!=null) {
			if(channel.equals("$bookmarks")) {
				Fragment favoriteFragment = new FavoriteFragment();
				fragmentTransaction.add(R.id.fragment_container, favoriteFragment);
			} else if(channel.equals("$histories")) {
				Fragment historyFragment = new HistoryFragment();
				fragmentTransaction.add(R.id.fragment_container, historyFragment);
			} else {
				ChannelFragment channelFragment = new ChannelFragment();
				channelFragment.mChannel = channel;
				channelFragment.mTitle = title;
				channelFragment.mUrl = url;
				fragmentTransaction.add(R.id.fragment_container, channelFragment);
			}
			fragmentTransaction.commit();
		}
		
		DaisyUtils.getVodApplication(this).addActivityToPool(this.toString(), this);
	}

	@Override
	protected void onDestroy() {
//		System.exit(0);
		DaisyUtils.getVodApplication(this).removeActivtyFromPool(this.toString());
		super.onDestroy();
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if(keyCode==KeyEvent.KEYCODE_MENU) {
			if(mOnMenuToggleListener!=null) {
				mOnMenuToggleListener.OnMenuToggle();
				return true;
			}
		}
		return super.onKeyUp(keyCode, event);
	}
	
	public void registerOnMenuToggleListener(OnMenuToggleListener listener) {
		mOnMenuToggleListener = listener;
	}
	
	public void unregisterOnMenuToggleListener() {
		mOnMenuToggleListener = null;
	}
	
	public interface OnMenuToggleListener {
		public void OnMenuToggle();
	}
	public  int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}
	
}
