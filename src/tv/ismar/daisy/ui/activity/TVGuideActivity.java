package tv.ismar.daisy.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
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
import tv.ismar.daisy.core.service.PosterUpdateService;
import tv.ismar.daisy.ui.fragment.*;
import tv.ismar.daisy.ui.widget.ChannelGridView;
import tv.ismar.daisy.ui.widget.DaisyButton;
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


    private AppUpdateReceiver appUpdateReceiver;


    private ChildFragment childFragment;
    private EntertainmentFragment entertainmentFragment;
    private FilmFragment filmFragment;
    private SportFragment sportFragment;
    private GuideFragment guideFragment;


    /**
     * PopupWindow
     */
    PopupWindow updatePopupWindow;
    PopupWindow exitPopupWindow;
    PopupWindow netErrorPopupWindow;


    private LinearLayout channelListView;
    private LinearLayout tabListView;

//    private ChannelGridView channelGrid;

    private Button change;


    private View contentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contentView = LayoutInflater.from(this).inflate(R.layout.activity_tv_guide, null);
        setContentView(contentView);
        fetchChannels();
        channelListView = (LinearLayout) findViewById(R.id.channel_h_list);
        tabListView = (LinearLayout) findViewById(R.id.tab_list);
        initTabView();
        if (savedInstanceState == null) {
            final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.container, new GuideFragment(), TAG_GUIDE_FRAGMENT).commit();
        } else {

        }
    }


    private void initTabView() {
        int res[] = {R.drawable.selector_tab_film, R.drawable.selector_tab_game, R.drawable.selector_tab_list};


        for (int i = 0; i < res.length; i++) {
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    (int) getResources().getDimension(R.dimen.guide_tab_item_wh),
                    (int) getResources().getDimension(R.dimen.guide_tab_item_wh));
            layoutParams.weight = 1;
            if (i != res.length - 1) {
                layoutParams.setMargins(0, 0, (int) getResources().getDimension(R.dimen.guide_tab_margin_rl), 0);
            }
            ImageView imageView = new ImageView(this);
            imageView.setImageResource(res[i]);
            imageView.setFocusable(true);
            imageView.setFocusableInTouchMode(true);
            imageView.setClickable(true);
            imageView.setLayoutParams(layoutParams);
            tabListView.addView(imageView);
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
                for (int i = 0; i < channelEntities.length; i++) {
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(188, 66);
                    TextView textView = new TextView(TVGuideActivity.this);
                    textView.setFocusable(true);
                    if (i != channelEntities.length - 1) {
                        layoutParams.setMargins(0, 0, 14, 0);
                    }
                    textView.setFocusable(true);
                    textView.setFocusableInTouchMode(true);
                    textView.setClickable(true);
                    textView.setTextSize(getResources().getDimension(R.dimen.guide_channel_size));
                    textView.setGravity(Gravity.CENTER);
                    textView.setBackgroundResource(R.drawable.selector_channel_item);
                    textView.setLayoutParams(layoutParams);
                    textView.setText(channelEntities[i].getName());
                    textView.setTextColor(getResources().getColor(R.color.white));
                    channelListView.addView(textView);

                }
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.e(TAG, retrofitError.getMessage());
            }
        });
    }


    private void registerUpdateReceiver() {
        appUpdateReceiver = new AppUpdateReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AppConstant.APP_UPDATE_ACTION);
        registerReceiver(appUpdateReceiver, intentFilter);
    }


    /**
     * receive app update broadcast, and show update popup window
     */
    class AppUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getBundleExtra("data");
            showUpdatePopup(contentView, bundle);
        }
    }


    /**
     * show update popup, now update app or next time update
     *
     * @param view   popup window location
     * @param bundle update data
     */
    private void showUpdatePopup(View view, Bundle bundle) {
        final Context context = this;
        View contentView = LayoutInflater.from(context)
                .inflate(R.layout.popup_update, null);
        contentView.setBackgroundResource(R.drawable.popup_bg_yellow);
        updatePopupWindow = new PopupWindow(null, 1400, 500);
        updatePopupWindow.setContentView(contentView);
        updatePopupWindow.setFocusable(true);
        updatePopupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        DaisyButton updateNow = (DaisyButton) contentView.findViewById(R.id.update_now_bt);

        TextView updateTitle = (TextView) contentView.findViewById(R.id.update_title);
        LinearLayout updateMsgLayout = (LinearLayout) contentView.findViewById(R.id.update_msg_layout);

        final String path = bundle.getString("path");
        String title = bundle.getString("title");
        updateTitle.setText(title);

        ArrayList<String> msgs = bundle.getStringArrayList("msgs");

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = 200;
        layoutParams.topMargin = 20;

        for (String msg : msgs) {
            TextView textView = new TextView(this);
            textView.setTextSize(getResources().getDimensionPixelSize(R.dimen.update_msg_textsize));
            textView.setLayoutParams(layoutParams);
            textView.setText(msg);
            updateMsgLayout.addView(textView);
        }

        updateNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePopupWindow.dismiss();
                installApk(context, path);
            }
        });
    }

    public void installApk(Context mContext, String path) {
        Uri uri = Uri.parse("file://" + path);
        if (AppConstant.DEBUG)
            Log.d(TAG, uri.toString());
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    private void updatePoster() {
        Intent intent = new Intent();
        intent.setClass(this, PosterUpdateService.class);
        startService(intent);
    }

    /**
     * showExitPopup
     *
     * @param view
     */
    private void showExitPopup(View view) {
        final Context context = this;
        View contentView = LayoutInflater.from(context)
                .inflate(R.layout.popup_exit, null);
        exitPopupWindow = new PopupWindow(null, 740, 341);
        exitPopupWindow.setContentView(contentView);
        exitPopupWindow.setFocusable(true);
        exitPopupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        DaisyButton confirmExit = (DaisyButton) contentView.findViewById(R.id.confirm_exit);
        DaisyButton cancelExit = (DaisyButton) contentView.findViewById(R.id.cancel_exit);

        confirmExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                isfinished = true;
//                videoView.stopPlayback();
//                mHandler.removeCallbacksAndMessages(null);
                exitPopupWindow.dismiss();
//                superOnbackPressed();
            }
        });

        cancelExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exitPopupWindow.dismiss();
            }
        });
    }

}
