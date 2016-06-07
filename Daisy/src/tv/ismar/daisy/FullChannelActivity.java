package tv.ismar.daisy;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.client.CacheHttpClient;
import tv.ismar.daisy.data.ChannelEntity;
import tv.ismar.daisy.ui.fragment.launcher.GuideFragment;
import tv.ismar.daisy.ui.widget.dialog.MessageDialogFragment;
import tv.ismar.daisy.views.FullChannelFragment;

/**
 * Created by admin on 2016/6/7.
 */
public class FullChannelActivity extends FragmentActivity {

    private CacheHttpClient cacheHttpClient;
    private View contentView;
    private ChannelEntity[] mChannelEntitys;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_channel);
        contentView = LayoutInflater.from(this).inflate(R.layout.activity_full_channel, null);
        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
        FullChannelFragment fullChannelFragment=new FullChannelFragment();
        transaction.replace(R.id.full_channel_container,fullChannelFragment).commit();
        fetchChannels();

    }
    private boolean neterrorshow = false;
    private void showNetErrorPopup() {
        if(neterrorshow)
            return;
        final MessageDialogFragment dialog = new MessageDialogFragment(FullChannelActivity.this, getString(R.string.fetch_net_data_error), null);
        dialog.setButtonText(getString(R.string.setting_network), getString(R.string.i_know));
        try {
            dialog.showAtLocation(contentView, Gravity.CENTER,
                    new MessageDialogFragment.ConfirmListener() {
                        @Override
                        public void confirmClick(View view) {
                            Intent intent = new Intent(Settings.ACTION_SETTINGS);
                            FullChannelActivity.this.startActivity(intent);
                        }
                    }, new MessageDialogFragment.CancelListener() {

                        @Override
                        public void cancelClick(View view) {
                            dialog.dismiss();
                            neterrorshow = false;
                        }
                    });
            neterrorshow = true;
        } catch (android.view.WindowManager.BadTokenException e) {
        }
    }
    private void fetchChannels() {
        String api = SimpleRestClient.root_url + "/api/tv/channels/";
        cacheHttpClient = new CacheHttpClient();
        cacheHttpClient.doRequest(api, new CacheHttpClient.Callback() {
            @Override
            public void onSuccess(String result) {
                if(neterrorshow)
                    return;
                mChannelEntitys = new Gson().fromJson(result, ChannelEntity[].class);
//                fullChannelFragment.setChannelEntity(mChannelEntitys[0]);
                ChannelEntity[] tmp = mChannelEntitys;
                // tmp = mChannelEntitys;
                mChannelEntitys = new ChannelEntity[tmp.length + 1];
                int k = 0;

                ChannelEntity launcher = new ChannelEntity();
                launcher.setChannel("launcher");
                launcher.setName("      首页      ");
                launcher.setHomepage_template("launcher");
                mChannelEntitys[0] = launcher;
                for (ChannelEntity e : tmp) {
                    mChannelEntitys[k + 1] = e;
                    k++;
                }
//                createChannelView(mChannelEntitys);
//                if (StringUtils.isNotEmpty(homepage_template)) {
//                    for (int i = 0; i < mChannelEntitys.length; i++) {
//                        if (homepage_template.equals(mChannelEntitys[i].getHomepage_template()) && mChannelEntitys[i].getHomepage_url().contains(homepage_url)) {
//                            if(channelflag)
//                                break;
//                            channelflag = true;
//                            channelscrollIndex = i;
//                            if (channelscrollIndex > 0 && !fragmentSwitch.hasMessages(SWITCH_PAGE_FROMLAUNCH)) {
//                                fragmentSwitch.sendEmptyMessage(SWITCH_PAGE_FROMLAUNCH);
//                            }
//                            topView.setSubTitle(mChannelEntitys[i].getName());
//                            break;
//                        }
//                    }
//                }
//                if (currentFragment == null && !isFinishing() && channelscrollIndex<=0) {
//                    try {
//                        currentFragment = new GuideFragment();
//                        lastFragment = currentFragment;
//                        ChannelEntity channelEntity = new ChannelEntity();
//                        launcher.setChannel("launcher");
//                        launcher.setName("首页");
//                        launcher.setHomepage_template("launcher");
//                        currentFragment.setChannelEntity(channelEntity);
//                        FragmentTransaction transaction = getSupportFragmentManager()
//                                .beginTransaction();
//                        transaction.replace(R.id.container, currentFragment, "template").commitAllowingStateLoss();
//                    } catch (IllegalStateException e) {
//                    }
//
//                }
            }

            @Override
            public void onFailed(String error) {
                showNetErrorPopup();
            }
        });
    }

}
