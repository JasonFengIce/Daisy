package tv.ismar.daisy.ui.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
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

    private ChildFragmentContainer childFragmentContainer;


//    private ChannelGridView channelGrid;

    private Button change;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tv_guide);
        childFragmentContainer = new ChildFragmentContainer();

        if (savedInstanceState == null) {
            final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.container, new GuideFragment(), TAG_GUIDE_FRAGMENT).commit();
        } else {

        }
//        channelGrid = (ChannelGridView) findViewById(R.id.channel_grid);


//        fetchChannels();

        HorizontalListView horizontalListView = (HorizontalListView) findViewById(R.id.channel_h_list);

        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            list.add("channel --- " + i);
        }

        ChannleListAdapter channleListAdapter = new ChannleListAdapter(this, list);
        horizontalListView.setAdapter(channleListAdapter);

        change = (Button) findViewById(R.id.change);
        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!childFragmentContainer.isVisible()) {
                    final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.container, childFragmentContainer, TAG_ENTERTAINMENT_FRAGMENT).commit();
                }
            }
        });


    }


    /**
     * fetch channel
     */
//    private void fetchChannels() {
//        String deviceToken = SimpleRestClient.device_token;
//        String host = SimpleRestClient.root_url;
//        RestAdapter restAdapter = new RestAdapter.Builder()
//                .setLogLevel(AppConstant.LOG_LEVEL)
//                .setEndpoint(host)
//                .build();
//        ClientApi.Channels client = restAdapter.create(ClientApi.Channels.class);
//        client.excute(deviceToken, new Callback<ChannelEntity[]>() {
//            @Override
//            public void success(ChannelEntity[] channelEntities, Response response) {
//                channelGrid.setAdapter(channelEntities);
//            }
//
//            @Override
//            public void failure(RetrofitError retrofitError) {
//                Log.e(TAG, retrofitError.getMessage());
//            }
//        });
//    }

}
