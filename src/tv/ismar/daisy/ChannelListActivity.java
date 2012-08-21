package tv.ismar.daisy;

import tv.ismar.daisy.views.ChannelFragment;
import tv.ismar.daisy.views.HistoryFragment;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;

public class ChannelListActivity extends Activity {
	
	private int fragment_id = 0;

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
			url = "http://127.0.0.1:21098/cord/api/tv/sections/teleplay/";
		}
		if(title==null) {
			title = "电视剧";
		}
		if(channel==null) {
			channel = "teleplay";
		}
		
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		
		if(channel!=null) {
			if(channel.equals("$bookmarks")) {
				//do something..
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
		
//		mChannelLabel.setText(title);
//		new InitTask().execute(url, channel);
	}
}
