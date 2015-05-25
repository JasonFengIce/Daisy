package tv.ismar.daisy.ui.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.ismartv.launcher.data.ChannelEntity;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import tv.ismar.daisy.AppConstant;
import tv.ismar.daisy.R;
import tv.ismar.daisy.adapter.ChannleListAdapter;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.client.ClientApi;
import tv.ismar.daisy.ui.fragment.*;
import tv.ismar.daisy.ui.widget.ChannelGridView;
import tv.ismar.daisy.ui.widget.HorizontalListView;

import java.util.ArrayList;

/**
 * Created by huaijie on 5/18/15.
 */
public class TVGuideActivity extends FragmentActivity {
    private static final String TAG = "TVGuideActivity";

    public static final String TAG_GUIDE_FRAGMENT = "guide";
    public static final String TAG_CHILD_FRAGMENT = "child";
    public static final String TAG_ENTERTAINMENT_FRAGMENT = "entertainment";
    public static final String TAG_FILM_FRAGMENT = "film";
    public static final String TAG_SPORT_FRAGMENT = "sport";


    private ChildFragment childFragment;
    private EntertainmentFragment entertainmentFragment;
    private FilmFragment filmFragment;
    private SportFragment sportFragment;
    private GuideFragment guideFragment;


    private LinearLayout channelListView;

//    private ChannelGridView channelGrid;

    private Button change;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tv_guide);
        fetchChannels();

        channelListView = (LinearLayout) findViewById(R.id.channel_h_list);

        if (savedInstanceState == null) {
            final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.container, new GuideFragment(), TAG_GUIDE_FRAGMENT).commit();
        } else {

        }
    }


    /**
     * fetch channel
     */
    private void fetchChannels() {
        String deviceToken = SimpleRestClient.device_token;
        String host = SimpleRestClient.root_url;
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(AppConstant.LOG_LEVEL)
                .setEndpoint(host)
                .build();
        ClientApi.Channels client = restAdapter.create(ClientApi.Channels.class);
        client.excute(deviceToken, new Callback<ChannelEntity[]>() {
            @Override
            public void success(ChannelEntity[] channelEntities, Response response) {
                for (ChannelEntity channelEntity : channelEntities) {
                    TextView textView = new TextView(TVGuideActivity.this);
                    textView.setFocusable(true);
                    textView.setPadding(100, 0, 100, 0);
                    textView.setBackgroundResource(R.drawable.selector_button);
                    textView.setText(channelEntity.getName());
                    channelListView.addView(textView);

                }
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.e(TAG, retrofitError.getMessage());
            }
        });
    }

}
