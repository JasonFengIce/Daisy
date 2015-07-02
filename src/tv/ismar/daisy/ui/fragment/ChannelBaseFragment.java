package tv.ismar.daisy.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import tv.ismar.daisy.data.ChannelEntity;
import android.support.v4.app.Fragment;
import tv.ismar.daisy.ui.activity.TVGuideActivity;

public class ChannelBaseFragment extends Fragment {
    private TVGuideActivity activity;
    protected ChannelEntity channelEntity;

    public ChannelEntity getChannelEntity() {
        return channelEntity;
    }

    public void setChannelEntity(ChannelEntity channelEntity) {
        this.channelEntity = channelEntity;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (TVGuideActivity) activity;
    }


    @Override
    public void onResume() {
        super.onResume();
        if (null != channelEntity && !TextUtils.isEmpty(channelEntity.getChannel()))
            activity.channelRequestFocus(channelEntity.getChannel());
    }
}
