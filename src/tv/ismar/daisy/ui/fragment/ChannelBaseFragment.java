package tv.ismar.daisy.ui.fragment;

import tv.ismar.daisy.R;
import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.data.ChannelEntity;
import tv.ismar.daisy.data.HomePagerEntity.Carousel;
import tv.ismar.daisy.data.HomePagerEntity.Poster;
import tv.ismar.daisy.player.InitPlayerTool;
import tv.ismar.daisy.ui.activity.TVGuideActivity;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;

public class ChannelBaseFragment extends Fragment {
	protected ChannelEntity channelEntity;
	protected TVGuideActivity context;

	public ChannelEntity getChannelEntity() {
		return channelEntity;
	}

	public void setChannelEntity(ChannelEntity channelEntity) {
		this.channelEntity = channelEntity;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.context = (TVGuideActivity) activity;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (null != channelEntity
				&& !TextUtils.isEmpty(channelEntity.getChannel()))
			context.channelRequestFocus(channelEntity.getChannel());
	}

	protected View.OnClickListener ItemClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			String url = null;
			String contentMode = null;
			String title = null;
			String mode_name = null;
			if (view.getTag() instanceof Poster) {
				Poster new_name = (Poster) view.getTag();
				contentMode = new_name.getContent_model();
				url = new_name.getUrl();
				title = new_name.getTitle();
				mode_name = new_name.getModel_name();
			} else if (view.getTag(R.drawable.launcher_selector) instanceof Carousel) {
				Carousel new_name = (Carousel) view
						.getTag(R.drawable.launcher_selector);
				contentMode = new_name.getContent_model();
				url = new_name.getUrl();
				title = new_name.getTitle();
				mode_name = new_name.getModel_name();
			}
			Intent intent = new Intent();
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			if (url == null) {
				intent.setAction("tv.ismar.daisy.Channel");
				intent.putExtra("title", channelEntity.getName());
				intent.putExtra("url", channelEntity.getUrl());
				intent.putExtra("channel", channelEntity.getChannel());
				intent.putExtra("portraitflag", channelEntity.getStyle());
				context.startActivity(intent);
			} else {
				if ("item".equals(mode_name)) {
					DaisyUtils.gotoSpecialPage(context,contentMode,url);
				} else if ("topic".equals(mode_name)) {
					intent.setAction("tv.ismar.daisy.Topic");
					intent.putExtra("url", url);
					context.startActivity(intent);
				} else if ("section".equals(mode_name)) {
					intent.setAction("tv.ismar.daisy.packagelist");
					intent.putExtra("title", title);
					intent.putExtra("itemlistUrl", url);
					intent.putExtra("lableString", title);
					context.startActivity(intent);
				} else if ("package".equals(mode_name)) {
					intent.setAction("tv.ismar.daisy.packageitem");
					intent.putExtra("url", url);
					context.startActivity(intent);
				} else if ("clip".equals(mode_name)) {
					InitPlayerTool tool = new InitPlayerTool(context);
					tool.initClipInfo(url, InitPlayerTool.FLAG_URL);
				}
			}
		}
	};

}
