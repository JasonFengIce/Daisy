package tv.ismar.daisy.ui.widget;

import tv.ismar.daisy.R;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TopPanelView extends LinearLayout {

	private TextView channelName;
	private TextView searchview;
	private TextView messageview;
	private TextView usercenterview;
	private TextView favoriteview;
	private TextView historyview;

	public TopPanelView(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater mInflater = LayoutInflater.from(context);
		View myView = mInflater.inflate(R.layout.top_panel, null);
		addView(myView);
		channelName = (TextView) findViewById(R.id.guide_channelname);
		searchview = (TextView) findViewById(R.id.guide_search);
		messageview = (TextView) findViewById(R.id.guide_message);
		usercenterview = (TextView) findViewById(R.id.guide_user_center);
		favoriteview = (TextView) findViewById(R.id.guide_my_favorite);
		historyview = (TextView) findViewById(R.id.guide_play_history);
		searchview.setOnClickListener(viewClickListener);
		messageview.setOnClickListener(viewClickListener);
		usercenterview.setOnClickListener(viewClickListener);
		favoriteview.setOnClickListener(viewClickListener);
		historyview.setOnClickListener(viewClickListener);
	}

	private View.OnClickListener viewClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			switch (v.getId()) {
			case R.id.guide_search:
				intent.setClassName("tv.ismar.daisy",
						"tv.ismar.daisy.SearchActivity");
				break;
			case R.id.guide_message:
				intent.setClassName("tv.ismar.daisy",
						"tv.ismar.daisy.SearchActivity");
				break;
			case R.id.guide_user_center:
				intent.setClassName("tv.ismar.daisy",
						"tv.ismar.daisy.PersonCenterActivity");
				break;
			case R.id.guide_my_favorite:
				intent.setClassName("tv.ismar.daisy",
						"tv.ismar.daisy.ChannelListActivity");
				intent.putExtra("channel", "$bookmarks");
			case R.id.guide_play_history:
				intent.setClassName("tv.ismar.daisy",
						"tv.ismar.daisy.ChannelListActivity");
				intent.putExtra("channel", "$histories");
				break;
			}
			getContext().startActivity(intent);
		}
	};

	public void setChannelName(String channeltitle) {
		channelName.setText(channeltitle);
	}
}
