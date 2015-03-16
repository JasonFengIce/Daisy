package tv.ismar.daisy;

import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.SimpleRestClient;
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
import android.view.KeyEvent;

public class ChannelListActivity extends Activity {
	
	private final static String TAG = "ChannelListActivity";
	
	private OnMenuToggleListener mOnMenuToggleListener;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.channel_layout);		  
		Intent intent = getIntent();
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
			//url = "http://cord.tvxio.com/api/tv/sections/chinesemovie/";
			url = SimpleRestClient.root_url+"/api/tv/sections/chinesemovie/";
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
			} 
			else if(channel.equals("search")){
				Intent searchIntent = new Intent();
				searchIntent.setClass(ChannelListActivity.this, SearchActivity.class);
				startActivity(searchIntent);
				finish();
			}
			else {
				ChannelFragment channelFragment = new ChannelFragment();
				channelFragment.mChannel = channel;
				channelFragment.mTitle = title;  //chinesemovie
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
