package tv.ismar.daisy.ui.fragment;

import tv.ismar.daisy.data.ChannelEntity;
import android.support.v4.app.Fragment;

public class ChannelBaseFragment extends Fragment {
	protected ChannelEntity channelEntity;

	public ChannelEntity getChannelEntity() {
		return channelEntity;
	}

	public void setChannelEntity(ChannelEntity channelEntity) {
		this.channelEntity = channelEntity;
	}

}
