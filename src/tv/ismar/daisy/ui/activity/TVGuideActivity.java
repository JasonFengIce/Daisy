package tv.ismar.daisy.ui.activity;

import static tv.ismar.daisy.VodApplication.DEVICE_TOKEN;
import static tv.ismar.daisy.VodApplication.DOMAIN;
import static tv.ismar.daisy.VodApplication.LOCATION_CITY;
import static tv.ismar.daisy.VodApplication.LOCATION_DISTRICT;
import static tv.ismar.daisy.VodApplication.LOCATION_PROVINCE;
import static tv.ismar.daisy.VodApplication.LOG_DOMAIN;
import static tv.ismar.daisy.VodApplication.PREFERENCE_FILE_NAME;
import static tv.ismar.daisy.VodApplication.SN_TOKEN;
import static tv.ismar.daisy.VodApplication.ad_domain;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import tv.ismar.daisy.AppConstant;
import tv.ismar.daisy.R;
import tv.ismar.daisy.VodApplication;
import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.client.ClientApi;
import tv.ismar.daisy.core.service.PosterUpdateService;
import tv.ismar.daisy.core.update.AppUpdateUtils;
import tv.ismar.daisy.data.ChannelEntity;
import tv.ismar.daisy.ui.ItemViewFocusChangeListener;
import tv.ismar.daisy.ui.fragment.ChannelBaseFragment;
import tv.ismar.daisy.ui.fragment.ChildFragment;
import tv.ismar.daisy.ui.fragment.EntertainmentFragment;
import tv.ismar.daisy.ui.fragment.FilmFragment;
import tv.ismar.daisy.ui.fragment.GuideFragment;
import tv.ismar.daisy.ui.fragment.SportFragment;
import tv.ismar.daisy.ui.widget.DaisyButton;
import tv.ismar.daisy.ui.widget.TopPanelView;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import cn.ismartv.activator.Activator;
import cn.ismartv.activator.data.Result;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.GeofenceClient;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

/**
 * Created by huaijie on 5/18/15.
 */
public class TVGuideActivity extends FragmentActivity implements
        Activator.OnComplete {
    private static final String TAG = "TVGuideActivity";

    private static final int GETDOMAIN = 0x06;


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

    private static final String KIND = "sky";
    private static final String VERSION = "1.0";
    private static final String MANUFACTURE = "sky";
    private int currentChannelIndex =0;
    private ImageView arrow_left;
    private ImageView arrow_right;
    private TopPanelView toppanel;
    private ChannelEntity[] channels;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerUpdateReceiver();
        AppUpdateUtils.getInstance().checkUpdate(this);
        contentView = LayoutInflater.from(this).inflate(R.layout.activity_tv_guide, null);
        setContentView(contentView);
        channelListView = (LinearLayout) findViewById(R.id.channel_h_list);
        tabListView = (LinearLayout) findViewById(R.id.tab_list);
        arrow_left = (ImageView)findViewById(R.id.arrow_scroll_left);
        arrow_right = (ImageView)findViewById(R.id.arrow_scroll_right);
        toppanel =(TopPanelView)findViewById(R.id.top_column_layout);
        toppanel.setChannelName("首页");
        arrow_left.setOnClickListener(arrowViewListener);
        arrow_right.setOnClickListener(arrowViewListener);
        initTabView();

        activator = Activator.getInstance(this);
        activator.setOnCompleteListener(this);
        String localInfo = DaisyUtils.getVodApplication(this).getPreferences().getString(VodApplication.LOCATION_INFO, "");
        sendLoncationRequest();
        activator.active(MANUFACTURE, KIND, VERSION, localInfo);
        getHardInfo();
    }

    @Override
    public void onBackPressed() {
        if (currentFragment.getClass().getName().equals(GuideFragment.class.getName())) {
            showExitPopup(contentView);
        } else {
            contentView.setBackgroundResource(R.color.normal_activity_bg);
            currentFragment = new GuideFragment();
            replaceFragment(currentFragment);
            toppanel.setChannelName("首页");
            currentChannelIndex = 0;
			if (arrow_left.getVisibility() == View.VISIBLE) {
				arrow_left.setVisibility(View.GONE);
			}
			if (arrow_right.getVisibility() == View.VISIBLE) {
				arrow_right.setVisibility(View.GONE);
			}
        }
    }

    public void superOnbackPressed() {
        super.onBackPressed();
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
        Log.d(TAG, "sn: " + SimpleRestClient.sn_token);
        String deviceToken = SimpleRestClient.device_token;
        String host = SimpleRestClient.root_url;
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(AppConstant.LOG_LEVEL)
                .setEndpoint(host)
                .build();
        ClientApi.Channels client = restAdapter
                .create(ClientApi.Channels.class);
        client.excute(deviceToken, new Callback<ChannelEntity[]>() {
            @Override
            public void success(ChannelEntity[] channelEntities,
                                Response response) {
            	channels = channelEntities;
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
                    textView.setTextSize(getResources().getDimension(R.dimen.tv_guide_channel_textSize));
                    textView.setGravity(Gravity.CENTER);
                    textView.setBackgroundResource(R.drawable.selector_channel_item);
                    textView.setLayoutParams(layoutParams);
                    textView.setText(channelEntities[i].getName());
                    textView.setTextColor(getResources()
                            .getColor(R.color.white));
                    textView.setTag(channelEntities[i]);
                    textView.setTag(R.drawable.selector_channel_item,i);
                    textView.setOnClickListener(channelClickListener);
                    textView.setOnFocusChangeListener(new ItemViewFocusChangeListener());
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
        exitPopupWindow.setFocusable(true);
        exitPopupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        DaisyButton confirmExit = (DaisyButton) contentView.findViewById(R.id.confirm_exit);
        DaisyButton cancelExit = (DaisyButton) contentView.findViewById(R.id.cancel_exit);

        confirmExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exitPopupWindow.dismiss();
                superOnbackPressed();
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
    }

    @Override
    public void onFailed(String erro) {
        checkNetWork(erro);
    }

    private void checkNetWork(String error) {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo.State wifiState = cm.getNetworkInfo(
                ConnectivityManager.TYPE_WIFI).getState();
        if (cm.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET) == null) {
            if (wifiState != NetworkInfo.State.CONNECTED) {
                showNetErrorPopup();
            } else {
                // showDialog(error);
            }
        } else {
            NetworkInfo.State ethernetState = cm.getNetworkInfo(
                    ConnectivityManager.TYPE_ETHERNET).getState();
            if (wifiState != NetworkInfo.State.CONNECTED
                    && ethernetState != NetworkInfo.State.CONNECTED) {
                showNetErrorPopup();
            } else {
                // showDialog(error);
            }
        }
    }

    private void showNetErrorPopup() {
        // final Context context = this;
        View contentView = LayoutInflater.from(this).inflate(
                R.layout.popup_net_error, null);
        netErrorPopupWindow = new PopupWindow(null, 740, 341);
        netErrorPopupWindow.setContentView(contentView);
        netErrorPopupWindow.setFocusable(true);
        netErrorPopupWindow.showAtLocation(contentView, Gravity.CENTER, 0, 0);
        DaisyButton confirmExit = (DaisyButton) contentView
                .findViewById(R.id.confirm_exit);

        confirmExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                netErrorPopupWindow.dismiss();
                superOnbackPressed();
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

    private OnClickListener channelClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
			if (arrow_left.getVisibility() == View.GONE) {
				arrow_left.setVisibility(View.VISIBLE);
			}
			if (arrow_right.getVisibility() == View.GONE) {
				arrow_right.setVisibility(View.VISIBLE);
			}
			for (int i = 0; i < channelListView.getChildCount(); i++) {
				if (v.getTag(R.drawable.selector_channel_item)
						.toString()
						.equals(channelListView.getChildAt(i)
								.getTag(R.drawable.selector_channel_item)
								.toString())) {
					v.setBackgroundResource(R.drawable.channel_item_focus);
				} else {
					v.setBackgroundResource(R.drawable.selector_channel_item);
				}
			}
        	ChannelEntity channelEntity = (ChannelEntity)v.getTag();
            toppanel.setChannelName(channelEntity.getName());
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

    };

	private OnClickListener arrowViewListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v.getId() == R.id.arrow_scroll_left) {
				currentChannelIndex--;
			} else if (v.getId() == R.id.arrow_scroll_right) {
				currentChannelIndex++;
			}
			if (currentChannelIndex < 0)
				currentChannelIndex = 0;
			if (currentChannelIndex > channelListView.getChildCount() - 1)
				currentChannelIndex = channelListView.getChildCount() - 1;
			View view = channelListView.getChildAt(currentChannelIndex);
			view.requestFocus();
			view.performClick();
		}

	};

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
    
    private void getHardInfo(){
    	DisplayMetrics metric = new DisplayMetrics();
    	getWindowManager().getDefaultDisplay().getMetrics(metric);
    	SimpleRestClient.densityDpi = metric.densityDpi;
    	SimpleRestClient.densityDpi = metric.widthPixels;
    	SimpleRestClient.densityDpi = metric.heightPixels;
    	PackageManager manager = getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
//            String appVersionName = info.versionName;
            SimpleRestClient.appVersion = info.versionCode;
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch blockd
            e.printStackTrace();
        }
    }
}
