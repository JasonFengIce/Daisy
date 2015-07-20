package tv.ismar.daisy.ui.activity;

import android.content.*;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import cn.ismartv.activator.Activator;
import cn.ismartv.activator.data.Result;
import com.baidu.location.*;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import tv.ismar.daisy.AppConstant;
import tv.ismar.daisy.BaseActivity;
import tv.ismar.daisy.R;
import tv.ismar.daisy.VodApplication;
import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.client.ClientApi;
import tv.ismar.daisy.core.service.PosterUpdateService;
import tv.ismar.daisy.core.update.AppUpdateUtils;
import tv.ismar.daisy.data.ChannelEntity;
import tv.ismar.daisy.ui.ItemViewFocusChangeListener;
import tv.ismar.daisy.ui.Position;
import tv.ismar.daisy.ui.fragment.*;
import tv.ismar.daisy.ui.fragment.launcher.*;
import tv.ismar.daisy.ui.widget.DaisyButton;
import tv.ismar.daisy.ui.widget.TopPanelView;

import java.util.ArrayList;
import java.util.HashMap;

import static tv.ismar.daisy.AppConstant.KIND;
import static tv.ismar.daisy.AppConstant.MANUFACTURE;
import static tv.ismar.daisy.VodApplication.*;

/**
 * Created by huaijie on 5/18/15.
 */
public class TVGuideActivity extends BaseActivity implements Activator.OnComplete {
    private static final String TAG = "TVGuideActivity";

    private AppUpdateReceiver appUpdateReceiver;
    private ChannelBaseFragment currentFragment;

    /**
     * PopupWindow
     */
    PopupWindow updatePopupWindow;
    PopupWindow exitPopupWindow;
    PopupWindow netErrorPopupWindow;

    private LinearLayout channelListView;
    private LinearLayout tabListView;

    private View contentView;
    private Activator activator;

    private LocationClient mLocationClient;
    private MyLocationListener mMyLocationListener;
    private GeofenceClient mGeofenceClient;

    private ImageView arrow_left;
    private ImageView arrow_right;
    private FrameLayout toppanel;
    private ChannelEntity[] mChannelEntitys;
    private HashMap<String, TextView> channelHashMap;

    private ChannelChange channelChange;

    private WeatherFragment weatherFragment;

    private Position mCurrentChannelPosition = new Position(new Position.PositioinChangeCallback() {
        @Override
        public void onChange(int position) {
            if (position == 0) {
                arrow_left.setVisibility(View.GONE);
                if (channelChange != null && channelChange != ChannelChange.CLICK_CHANNEL)
                    channelChange = ChannelChange.RIGHT_ARROW;
            } else {
                arrow_left.setVisibility(View.VISIBLE);
            }

            if (position == mChannelEntitys.length - 1) {
                arrow_right.setVisibility(View.GONE);
                if (channelChange != null && channelChange != ChannelChange.CLICK_CHANNEL)
                    channelChange = ChannelChange.LEFT_ARROW;
            } else {
                arrow_right.setVisibility(View.VISIBLE);
            }
            selectChannelByPosition(position);
        }
    });


    private OnClickListener arrowViewListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.arrow_scroll_left:
                    channelChange = ChannelChange.LEFT_ARROW;
                    if (mCurrentChannelPosition.getPosition() - 1 >= 0) {
                        mCurrentChannelPosition.setPosition(mCurrentChannelPosition.getPosition() - 1);
                    } else {
                        mCurrentChannelPosition.setPosition(0);
                    }

                    break;
                case R.id.arrow_scroll_right:
                    channelChange = ChannelChange.RIGHT_ARROW;
                    if (mCurrentChannelPosition.getPosition() + 1 <= mChannelEntitys.length - 1) {
                        mCurrentChannelPosition.setPosition(mCurrentChannelPosition.getPosition() + 1);
                    } else {
                        mCurrentChannelPosition.setPosition(mChannelEntitys.length - 1);
                    }
                    break;
            }
        }

    };

    private OnClickListener channelClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            channelChange = ChannelChange.CLICK_CHANNEL;

            if (arrow_left.getVisibility() == View.GONE) {
                arrow_left.setVisibility(View.VISIBLE);
            }
            if (arrow_right.getVisibility() == View.GONE) {
                arrow_right.setVisibility(View.VISIBLE);
            }
            int channelPosition = (Integer) v.getTag();
            mCurrentChannelPosition.setPosition(channelPosition);
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerUpdateReceiver();
        AppUpdateUtils.getInstance().checkUpdate(this);
        contentView = LayoutInflater.from(this).inflate(R.layout.activity_tv_guide, null);
        setContentView(contentView);
        initViews();
        initTabView();
        activator = Activator.getInstance(this);
        activator.setOnCompleteListener(this);
        String localInfo = DaisyUtils.getVodApplication(this).getPreferences().getString(VodApplication.LOCATION_INFO, "");
        activator.active(MANUFACTURE, KIND, String.valueOf(SimpleRestClient.appVersion), localInfo);
        getHardInfo();
        updatePoster();
    }

    private void initViews() {
        toppanel = (FrameLayout) findViewById(R.id.top_column_layout);
        weatherFragment = new WeatherFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.top_column_layout, weatherFragment).commit();
        channelListView = (LinearLayout) findViewById(R.id.channel_h_list);
        tabListView = (LinearLayout) findViewById(R.id.tab_list);
        arrow_left = (ImageView) findViewById(R.id.arrow_scroll_left);
        arrow_right = (ImageView) findViewById(R.id.arrow_scroll_right);
        arrow_left.setOnClickListener(arrowViewListener);
        arrow_right.setOnClickListener(arrowViewListener);
    }

    @Override
    public void onBackPressed() {

        if (currentFragment != null) {
            if (currentFragment.getClass().getName().equals(GuideFragment.class.getName())) {
                showExitPopup(contentView);
            } else {
                contentView.setBackgroundResource(R.color.normal_activity_bg);
                currentFragment = new GuideFragment();
                replaceFragment(currentFragment);
                weatherFragment.setTitle(getText(R.string.ismartv_cinema).toString());
                weatherFragment.setSubTitle("首页");

                if (arrow_left.getVisibility() == View.VISIBLE) {
                    arrow_left.setVisibility(View.GONE);
                }
                if (arrow_right.getVisibility() == View.VISIBLE) {
                    arrow_right.setVisibility(View.GONE);
                }
            }
        } else {
            showExitPopup(contentView);
        }

    }
//
//    public void superOnbackPressed() {
//        super.onBackPressed();
//    }


    private void initTabView() {
        int res[] = {R.drawable.selector_tab_film,
                R.drawable.selector_tab_game, R.drawable.selector_tab_list};
        for (int i = 0; i < res.length; i++) {
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    64, 64);
            layoutParams.weight = 1;
            if (i != res.length - 1) {
                layoutParams.setMargins(0, 0, 68, 0);
            }
            ImageView imageView = new ImageView(this);
            imageView.setImageResource(res[i]);
            imageView.setFocusable(true);
            imageView.setFocusableInTouchMode(true);
            imageView.setClickable(true);
            imageView.setLayoutParams(layoutParams);
            imageView.setOnFocusChangeListener(new ItemViewFocusChangeListener());
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
                mChannelEntitys = channelEntities;
                createChannelView(channelEntities);
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                String message = retrofitError.getMessage();
                if (null != message)
                    Log.e(TAG, retrofitError.getMessage());
            }
        });
    }
  private tv.ismar.daisy.views.MyHorizontalScrollView scroll;
    private void createChannelView(ChannelEntity[] channelEntities) {
        scroll = (tv.ismar.daisy.views.MyHorizontalScrollView)contentView.findViewById(R.id.scroll);
        channelHashMap = new HashMap<String, TextView>();
        scroll.mLeftDistance = 77;
        channelListView.removeAllViews();
        int i;
        for ( i = 0; i < channelEntities.length; i++) {
            FrameLayout frameLayout = (FrameLayout) LayoutInflater.from(this).inflate(R.layout.item_channel, null);
            Button textView = (Button) frameLayout.findViewById(R.id.channel_item);
            textView.setText(channelEntities[i].getName());
            textView.setOnClickListener(channelClickListener);
            textView.setOnFocusChangeListener(new ItemViewFocusChangeListener());
            textView.setTag(i);
            channelListView.addView(frameLayout);
            channelHashMap.put(channelEntities[i].getChannel(), textView);
        }
        FrameLayout frameLayout1 = (FrameLayout) LayoutInflater.from(this).inflate(R.layout.item_channel, null);
        Button textView1 = (Button) frameLayout1.findViewById(R.id.channel_item);
        textView1.setTag(i);
        textView1.setText("test1");

        FrameLayout frameLayout2 = (FrameLayout) LayoutInflater.from(this).inflate(R.layout.item_channel, null);
        Button textView2 = (Button) frameLayout2.findViewById(R.id.channel_item);
        i++;
        textView2.setTag(i);
        textView2.setText("test2");

        FrameLayout frameLayout3 = (FrameLayout) LayoutInflater.from(this).inflate(R.layout.item_channel, null);
        Button textView3 = (Button) frameLayout3.findViewById(R.id.channel_item);
        textView3.setText("test3");
        i++;
        textView3.setTag(i);

        channelListView.addView(frameLayout1);
    //    channelListView.addView(frameLayout2);
     //   channelListView.addView(frameLayout3);

      //  scroll.setHorizontalFadingEdgeEnabled(true);
      //  scroll.setFadingEdgeLength(100);
    }

    private void registerUpdateReceiver() {
        appUpdateReceiver = new AppUpdateReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AppConstant.APP_UPDATE_ACTION);
        registerReceiver(appUpdateReceiver, intentFilter);
    }

    public void onUserCenterClick() {
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();
        transaction.setCustomAnimations(
                R.anim.fade_in,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.fade_out);
//        transaction.replace(R.id.container, new UserCenterFragment()).commit();


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
        View contentView = LayoutInflater.from(context).inflate(R.layout.popup_update, null);
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
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
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
        View contentView = LayoutInflater.from(context).inflate(R.layout.popup_exit, null);
        exitPopupWindow = new PopupWindow(null, 740, 341);
        exitPopupWindow.setContentView(contentView);
        exitPopupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.transparent));
        exitPopupWindow.setFocusable(true);
        exitPopupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        Button confirmExit = (Button) contentView.findViewById(R.id.confirm_exit);
        Button cancelExit = (Button) contentView.findViewById(R.id.cancel_exit);

        confirmExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exitPopupWindow.dismiss();
                TVGuideActivity.this.finish();
            }
        });

        cancelExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exitPopupWindow.dismiss();
            }
        });
    }

    @Override
    public void onSuccess(Result result) {

        saveSimpleRestClientPreferences(this, result);
        DaisyUtils.getVodApplication(TVGuideActivity.this).getNewContentModel();
        fetchChannels();

        currentFragment = new GuideFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.container, currentFragment).commit();
        sendLoncationRequest();
    }

    @Override
    public void onFailed(String erro) {
        Log.e(TAG, "active error: " + erro);
        showNetErrorPopup();
    }


    private void showNetErrorPopup() {
        View contentView = LayoutInflater.from(this).inflate(R.layout.popup_net_error, null);
        netErrorPopupWindow = new PopupWindow(null, 740, 341);
        netErrorPopupWindow.setContentView(contentView);
        netErrorPopupWindow.setFocusable(true);
        netErrorPopupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.transparent));
        netErrorPopupWindow.showAtLocation(contentView, Gravity.CENTER, 0, 0);

        Button settingNetwork = (Button) contentView.findViewById(R.id.setting_network);
        Button iKnow = (Button) contentView.findViewById(R.id.i_know);

        settingNetwork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                TVGuideActivity.this.startActivity(intent);
            }
        });

        iKnow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                netErrorPopupWindow.dismiss();
            }
        });
    }


    private void sendLoncationRequest() {
        mLocationClient = new LocationClient(this);
        mMyLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(mMyLocationListener);
        mGeofenceClient = new GeofenceClient(getApplicationContext());
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);// 设置定位模式
        // option.setCoorType("bd09ll");//返回的定位结果是百度经纬度,默认值gcj02
        option.setScanSpan(5000);// 设置发起定位请求的间隔时间为5000ms
        option.setIsNeedAddress(true);// 返回的定位结果包含地址信息
        option.setOpenGps(false);
        mLocationClient.setLocOption(option);
        mLocationClient.start();
        mLocationClient.requestLocation();
    }

    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            mLocationClient.stop();
            DaisyUtils
                    .getVodApplication(TVGuideActivity.this)
                    .getEditor()
                    .putString(VodApplication.LOCATION_INFO,
                            location.getAddrStr());
            DaisyUtils.getVodApplication(TVGuideActivity.this).getEditor()
                    .putString(LOCATION_PROVINCE, location.getProvince());
            DaisyUtils.getVodApplication(TVGuideActivity.this).getEditor()
                    .putString(LOCATION_CITY, location.getCity());
            DaisyUtils.getVodApplication(TVGuideActivity.this).getEditor()
                    .putString(LOCATION_DISTRICT, location.getDistrict());
            DaisyUtils.getVodApplication(TVGuideActivity.this).save();
        }
    }


    private void selectChannelByPosition(int position) {
        ChannelEntity channelEntity = mChannelEntitys[position];
        weatherFragment.setSubTitle(channelEntity.getName());
        if ("template1".equals(channelEntity.getHomepage_template())) {
            currentFragment = new FilmFragment();
            contentView.setBackgroundResource(R.color.normal_activity_bg);
        } else if ("template2".equals(channelEntity.getHomepage_template())) {
            currentFragment = new EntertainmentFragment();
            contentView.setBackgroundResource(R.color.normal_activity_bg);
        } else if ("template3".equals(channelEntity.getHomepage_template())) {
            currentFragment = new SportFragment();
            contentView.setBackgroundResource(R.color.normal_activity_bg);
        } else if ("template4".equals(channelEntity.getHomepage_template())) {
            currentFragment = new ChildFragment();
            contentView.setBackgroundResource(R.drawable.channel_child_bg);
        }
        currentFragment.setChannelEntity(channelEntity);
        replaceFragment(currentFragment);
    }


    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();
        transaction.setCustomAnimations(
                R.anim.push_left_in,
                R.anim.push_left_out,
                R.anim.push_left_in,
                R.anim.push_left_out);
        transaction.replace(R.id.container, fragment).commit();
    }

    private void saveSimpleRestClientPreferences(Context context, Result result) {
        SimpleRestClient.root_url = "http://" + result.getDomain();
        SimpleRestClient.sRoot_url = "http://" + result.getDomain();
        SimpleRestClient.ad_domain = "http://" + result.getAd_domain();
        SimpleRestClient.log_domain = "http://" + result.getLog_Domain();
        SimpleRestClient.device_token = result.getDevice_token();
        SimpleRestClient.sn_token = result.getSn_Token();

        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(ad_domain, SimpleRestClient.ad_domain);
        editor.putString(DEVICE_TOKEN, SimpleRestClient.device_token);
        editor.putString(DOMAIN, SimpleRestClient.root_url);
        editor.putString(SN_TOKEN, SimpleRestClient.sn_token);
        editor.putString(LOG_DOMAIN, SimpleRestClient.log_domain);
        editor.apply();

        SimpleRestClient.mobile_number = DaisyUtils.getVodApplication(this)
                .getPreferences()
                .getString(VodApplication.MOBILE_NUMBER, "");
        SimpleRestClient.access_token = DaisyUtils.getVodApplication(this)
                .getPreferences().getString(VodApplication.AUTH_TOKEN, "");
    }

    private void getHardInfo() {
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        SimpleRestClient.densityDpi = metric.densityDpi;
        SimpleRestClient.screenWidth = metric.widthPixels;
        SimpleRestClient.screenHeight = metric.heightPixels;
        PackageManager manager = getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
            SimpleRestClient.appVersion = info.versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void channelRequestFocus(String channel) {
        switch (channelChange) {
            case CLICK_CHANNEL:
                channelHashMap.get(channel).requestFocus();
                channelHashMap.get(channel).requestFocusFromTouch();
                break;
            case LEFT_ARROW:
                arrow_left.requestFocus();
                arrow_left.requestFocusFromTouch();
                break;
            case RIGHT_ARROW:
                arrow_right.requestFocus();
                arrow_right.requestFocusFromTouch();
                break;
        }

    }

    enum ChannelChange {
        CLICK_CHANNEL,
        LEFT_ARROW,
        RIGHT_ARROW
    }


    @Override
    protected void onDestroy() {
        unregisterReceiver(appUpdateReceiver);
        if (!(updatePopupWindow == null)) {
            updatePopupWindow.dismiss();
        }
        if (exitPopupWindow != null) {
            exitPopupWindow.dismiss();
        }
        super.onDestroy();
    }
}
