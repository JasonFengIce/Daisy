package tv.ismar.daisy;

import tv.ismar.daisy.views.AlertDialogFragment;
import tv.ismar.daisy.views.ChannelFragment;
import tv.ismar.daisy.views.FavoriteFragment;
import tv.ismar.daisy.views.HistoryFragment;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;

public class ChannelListActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.channel_layout);
//		initViews();
		Intent intent = getIntent();
		String url = null;
		String title = null;
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
			url = "http://cord.tvxio.com/api/bookmarks/";
		}
		if(title==null) {
			title = "华语电影";
		}
		if(channel==null) {
			channel = "$bookmarks";
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
		
	}

	@Override
	protected void onDestroy() {
		System.exit(0);
		super.onDestroy();
	}
	
	
}
