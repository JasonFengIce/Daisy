package tv.ismar.daisy.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.GeofenceClient;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;
import org.sakuratya.horizontal.ui.HGridView;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import cn.ismartv.activator.Activator;
import cn.ismartv.activator.data.Result;
import tv.ismar.daisy.AppConstant;
import tv.ismar.daisy.BaseActivity;
import tv.ismar.daisy.R;
import tv.ismar.daisy.VodApplication;
import tv.ismar.daisy.adapter.ChannelAdapter;
import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.VodUserAgent;
import tv.ismar.daisy.core.client.CacheHttpClient;
import tv.ismar.daisy.core.preferences.AccountSharedPrefs;
import tv.ismar.daisy.core.service.PosterUpdateService;
import tv.ismar.daisy.core.update.AppUpdateUtilsV2;
import tv.ismar.daisy.data.ChannelEntity;
import tv.ismar.daisy.player.CallaPlay;
import tv.ismar.daisy.ui.ItemViewFocusChangeListener;
import tv.ismar.daisy.ui.Position;
import tv.ismar.daisy.ui.fragment.ChannelBaseFragment;
import tv.ismar.daisy.ui.fragment.launcher.ChildFragment;
import tv.ismar.daisy.ui.fragment.launcher.EntertainmentFragment;
import tv.ismar.daisy.ui.fragment.launcher.FilmFragment;
import tv.ismar.daisy.ui.fragment.launcher.GuideFragment;
import tv.ismar.daisy.ui.fragment.launcher.SportFragment;
import tv.ismar.daisy.ui.widget.LaunchHeaderLayout;
import tv.ismar.daisy.ui.widget.dialog.MessageDialogFragment;
import tv.ismar.daisy.utils.BitmapDecoder;
import tv.ismar.sakura.ui.widget.MessagePopWindow;

import static tv.ismar.daisy.VodApplication.LOCATION_CITY;
import static tv.ismar.daisy.VodApplication.LOCATION_DISTRICT;
import static tv.ismar.daisy.VodApplication.LOCATION_PROVINCE;

/**
 * Created by huaijie on 5/18/15.
 */
public class TVGuideActivity extends BaseActivity implements Activator.OnComplete, HGridView.OnScrollListener {
    private static final String TAG = "TVGuideActivity";
    private static final int SWITCH_PAGE = 0X01;
    private static final int SWITCH_PAGE_FROMLAUNCH = 0X02;
    private AppUpdateReceiver appUpdateReceiver;
    private ChannelBaseFragment currentFragment;
    private ChannelBaseFragment lastFragment;
    /**
     * PopupWindow
     */
    PopupWindow updatePopupWindow;
    MessagePopWindow exitPopupWindow;
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
    private ImageView arrow_left_visible;
    private ImageView arrow_right_visible;
    private FrameLayout toppanel;
    private ChannelEntity[] mChannelEntitys;
    private HashMap<String, TextView> channelHashMap;

    private ChannelChange channelChange = ChannelChange.CLICK_CHANNEL;
    private String homepage_template;
    private String homepage_url;

    private LaunchHeaderLayout topView;
    private View toppage_divide_view;
    private boolean scrollFromBorder;
    private ScrollType scrollType = ScrollType.right;
    private String lastviewTag;
    private int lastchannelindex;
    private boolean rightscroll;
    private LeavePosition leavePosition = LeavePosition.RightBottom;
    private ImageView guide_shadow_view;
    private static int channelscrollIndex = 0;

    public boolean isneedpause;

    private FragmentSwitchHandler fragmentSwitch;
    private BitmapDecoder bitmapDecoder;
    private Handler netErrorPopupHandler;
    private Runnable netErrorPopupRunnable;
    private CacheHttpClient cacheHttpClient;
    private enum LeavePosition {
        LeftTop,
        LeftBottom,
        RightTop,
        RightBottom
    }

    public LeavePosition getLeavePosition() {
        return leavePosition;
    }

    public void setLeavePosition(LeavePosition leavePosition) {
        this.leavePosition = leavePosition;
    }

    private Position mCurrentChannelPosition = new Position(new Position.PositioinChangeCallback() {
        @Override
        public void onChange(int position) {
            if (position == 0) {
                arrow_left.setVisibility(View.GONE);
                arrow_left_visible.setVisibility(View.GONE);
                if (channelChange != null && channelChange != ChannelChange.CLICK_CHANNEL)
                    channelChange = ChannelChange.RIGHT_ARROW;
            } else {
                arrow_left.setVisibility(View.VISIBLE);
                arrow_left_visible.setVisibility(View.VISIBLE);
            }

            if (position == mChannelEntitys.length - 1) {
                arrow_right.setVisibility(View.GONE);
                arrow_right_visible.setVisibility(View.GONE);
                if (channelChange != null && channelChange != ChannelChange.CLICK_CHANNEL)
                    channelChange = ChannelChange.LEFT_ARROW;
            } else {
                arrow_right.setVisibility(View.VISIBLE);
                arrow_right_visible.setVisibility(View.VISIBLE);
            }
            Log.i("TestFragment", "position==" + position);
            Message msg = new Message();
            msg.arg1 = position;
            msg.what = SWITCH_PAGE;
            if (fragmentSwitch.hasMessages(SWITCH_PAGE))
                fragmentSwitch.removeMessages(SWITCH_PAGE);
            fragmentSwitch.sendMessageDelayed(msg, 300);
            scroll.setSelection(position);
            if (!scrollFromBorder)
                scroll.requestFocus();
        }
    });

    private View.OnFocusChangeListener scrollViewListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {

            if (lastview == null)
                return;
            if (hasFocus) {
                TextView textview = (TextView) lastview.findViewById(R.id.channel_item);
                textview.setBackgroundResource(R.drawable.channel_item_normal);
                textview.setTextColor(NORMAL_CHANNEL_TEXTCOLOR);
                AnimationSet animationSet1 = new AnimationSet(true);
                ScaleAnimation scaleAnimation1 = new ScaleAnimation(1.05f, 1f, 1.05f, 1f,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);
                scaleAnimation1.setDuration(200);
                animationSet1.addAnimation(scaleAnimation1);
                animationSet1.setFillAfter(true);
                textview.startAnimation(animationSet1);
//                scroll.requestFocus();
                switch (v.getId()) {
                    case R.id.arrow_scroll_left:
                        scrollType = ScrollType.left;
//                        channelChange = ChannelChange.LEFT_ARROW;
//                        if (mCurrentChannelPosition.getPosition() - 1 >= 0) {
//                            mCurrentChannelPosition.setPosition(mCurrentChannelPosition.getPosition() - 1);
//                        } else {
//                            mCurrentChannelPosition.setPosition(0);
//                        }
                        scroll.arrowScroll(View.FOCUS_LEFT);
                        rightscroll = true;

                        break;
                    case R.id.arrow_scroll_right:
                        scrollType = ScrollType.right;
//                        channelChange = ChannelChange.RIGHT_ARROW;
//                        Log.i("TestFragment", "mCurrentChannelPosition.getPosition()" + mCurrentChannelPosition.getPosition());
//                        if (mCurrentChannelPosition.getPosition() + 1 <= mChannelEntitys.length - 1) {
//                            mCurrentChannelPosition.setPosition(mCurrentChannelPosition.getPosition() + 1);
//                        } else {
//                            mCurrentChannelPosition.setPosition(mChannelEntitys.length - 1);
//                        }
                        scroll.arrowScroll(View.FOCUS_RIGHT);
                        rightscroll = false;
                        break;
                }
                scrollFromBorder = true;
            }
        }
    };
    private OnClickListener arrowViewListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            TextView textview = (TextView) lastview.findViewById(R.id.channel_item);
            textview.setBackgroundResource(R.drawable.channel_item_normal);
            textview.setTextColor(NORMAL_CHANNEL_TEXTCOLOR);
            AnimationSet animationSet1 = new AnimationSet(true);
            ScaleAnimation scaleAnimation1 = new ScaleAnimation(1.05f, 1f, 1.05f, 1f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            scaleAnimation1.setDuration(200);
            animationSet1.addAnimation(scaleAnimation1);
            animationSet1.setFillAfter(true);
            textview.startAnimation(animationSet1);
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
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null)
            savedInstanceState = null;
        super.onCreate(savedInstanceState);
        fragmentSwitch = new FragmentSwitchHandler(this);
        activityTag = "BaseActivity";
        registerUpdateReceiver();
        contentView = LayoutInflater.from(this).inflate(R.layout.activity_tv_guide, null);
        setContentView(contentView);


        homepage_template = getIntent().getStringExtra("homepage_template");
        homepage_url = getIntent().getStringExtra("homepage_url");
        final View vv = findViewById(R.id.large_layout);
        bitmapDecoder = new BitmapDecoder();
        bitmapDecoder.decode(this, R.drawable.main_bg, new BitmapDecoder.Callback() {
            @Override
            public void onSuccess(BitmapDrawable bitmapDrawable) {
                vv.setBackgroundDrawable(bitmapDrawable);
            }
        });


        vv.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                boolean ret = false;
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        Log.i("zhangjiqiang", "KEYCODE_DPAD_LEFT getNextFocusRightId==" + v.getNextFocusLeftId() + "//getLeft" + v.getLeft());
                        ret = true;
                        break;
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        Log.i("zhangjiqiang", "KEYCODE_DPAD_RIGHT getNextFocusRightId==" + v.getNextFocusRightId() + "//getRight" + v.getRight());
                        ret = true;
                        break;
                }
                return ret;
            }
        });

        initViews();
        initTabView();
        activator = Activator.getInstance(this);
        activator.setOnCompleteListener(this);
        String localInfo = DaisyUtils.getVodApplication(this).getPreferences().getString(VodApplication.LOCATION_INFO, "");
        getHardInfo();
        updatePoster();
        String product = Build.BRAND.replace(" ", "_");
        String mode = VodUserAgent.getModelName();
		if (!activator.iswaiting)
			activator.active(product, mode,
					String.valueOf(SimpleRestClient.appVersion), localInfo);
    }


    private void initViews() {
        toppanel = (FrameLayout) findViewById(R.id.top_column_layout);
        toppage_divide_view = findViewById(R.id.toppage_divide_view);
        toppage_divide_view.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (scroll == null)
                    return;
                if (hasFocus) {
                    scroll.requestFocus();
                    TextView tv = (TextView) scroll.getSelectedView().findViewById(R.id.channel_item);
                    tv.setBackgroundResource(R.drawable.channel_item_selectd_focus);
                }
            }
        });
//        weatherFragment = new WeatherFragment();
//        getSupportFragmentManager().beginTransaction().add(R.id.top_column_layout, weatherFragment).commit();
        //  channelListView = (LinearLayout) findViewById(R.id.channel_h_list);
        tabListView = (LinearLayout) findViewById(R.id.tab_list);
        arrow_left = (ImageView) findViewById(R.id.arrow_scroll_left);
        arrow_right = (ImageView) findViewById(R.id.arrow_scroll_right);
        arrow_left_visible = (ImageView) findViewById(R.id.arrow_scroll_left_visible);
        arrow_right_visible = (ImageView) findViewById(R.id.arrow_scroll_right_visible);
        //arrow_left.setOnClickListener(arrowViewListener);
        // arrow_right.setOnClickListener(arrowViewListener);
        arrow_left.setOnFocusChangeListener(scrollViewListener);
        arrow_right.setOnFocusChangeListener(scrollViewListener);
        guide_shadow_view = (ImageView) findViewById(R.id.guide_shadow_view);
    }

    @Override
    public void onBackPressed() {
        showExitPopup(contentView);
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
        String api = SimpleRestClient.root_url + "/api/tv/channels/";
        cacheHttpClient = new CacheHttpClient(); 
        cacheHttpClient.doRequest(api, new CacheHttpClient.Callback() {
            @Override
            public void onSuccess(String result) {
                topView.setVisibility(View.VISIBLE);
                mChannelEntitys = new Gson().fromJson(result, ChannelEntity[].class);

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
                createChannelView(mChannelEntitys);
                if (StringUtils.isNotEmpty(homepage_template)) {
                    for (int i = 0; i < mChannelEntitys.length; i++) {
                        if (homepage_template.equals(mChannelEntitys[i].getHomepage_template()) && mChannelEntitys[i].getHomepage_url().contains(homepage_url)) {
                            channelscrollIndex = i;
                            if (channelscrollIndex > 0) {
                                fragmentSwitch.sendEmptyMessage(SWITCH_PAGE_FROMLAUNCH);
                            }
                            topView.setSubTitle(mChannelEntitys[i].getName());
                            break;
                        }
                    }
                }
                if (currentFragment == null && !isFinishing() && channelscrollIndex<=0) {
                    try {
                        currentFragment = new GuideFragment();
                        FragmentTransaction transaction = getSupportFragmentManager()
                                .beginTransaction();
                        transaction.replace(R.id.container, currentFragment, "template").commitAllowingStateLoss();
                    } catch (IllegalStateException e) {
                    }

                }
            }

            @Override
            public void onFailed(String error) {
                Log.e(TAG, "fetchChannels failed");
            }
        });
    }

    private HGridView scroll;
    private View lastview = null;
    private View clickView = null;

    private void setFocusChannelView(View view) {
        TextView channelBtn = (TextView) view.findViewById(R.id.channel_item);
        channelBtn.setBackgroundResource(R.drawable.channel_focus_frame);
        channelBtn.setTextColor(FOCUS_CHANNEL_TEXTCOLOR);
        AnimationSet animationSet = new AnimationSet(true);
        ScaleAnimation scaleAnimation = new ScaleAnimation(1, 1.05f, 1, 1.05f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(200);
        animationSet.addAnimation(scaleAnimation);
        animationSet.setFillAfter(true);
        channelBtn.startAnimation(animationSet);
    }

    private void setLostFocusChannel(View view) {
        TextView channelBtn = (TextView) view.findViewById(R.id.channel_item);
        channelBtn.setTextColor(NORMAL_CHANNEL_TEXTCOLOR);
        channelBtn.setBackgroundResource(R.drawable.channel_item_normal);
        AnimationSet animationSet = new AnimationSet(true);
        ScaleAnimation scaleAnimation = new ScaleAnimation(1.05f, 1f, 1.05f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(200);
        animationSet.addAnimation(scaleAnimation);
        animationSet.setFillAfter(true);
        channelBtn.startAnimation(animationSet);
    }

    private void setClickChannelView(View view) {
        TextView channelBtn = (TextView) view.findViewById(R.id.channel_item);
        channelBtn.setBackgroundResource(R.drawable.channel_item_focus);
        channelBtn.setTextColor(NORMAL_CHANNEL_TEXTCOLOR);
        AnimationSet animationSet = new AnimationSet(true);
        ScaleAnimation scaleAnimation = new ScaleAnimation(1, 1.05f, 1, 1.05f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(200);
        animationSet.addAnimation(scaleAnimation);
        animationSet.setFillAfter(true);
        channelBtn.startAnimation(animationSet);
    }

    private int FOCUS_CHANNEL_BG = 0xffffba00;
    private int FOCUS_CHANNEL_TEXTCOLOR = 0xffffba00;
    private int NORMAL_CHANNEL_TEXTCOLOR = 0xffffffff;

    private void createChannelView(ChannelEntity[] channelEntities) {
        List<ChannelEntity> channelList;
//        ChannelEntity launcher = new ChannelEntity();
//        launcher.setChannel("launcher");
//        launcher.setName("首页");
//        launcher.setHomepage_template("launcher");
        channelList = new ArrayList<ChannelEntity>();
        // channelList.add(launcher);
        for (ChannelEntity entity : channelEntities) {
            channelList.add(entity);
        }
        scroll = (HGridView) contentView.findViewById(R.id.h_grid_view);
        scroll.setOnScrollListener(this);
        channelHashMap = new HashMap<String, TextView>();
        final ChannelAdapter imageAdapter = new ChannelAdapter(this, channelList, R.layout.item_channel);
//        imageAdapter.setOnClickCallback(new ChannelAdapter.OnClickCallback() {
//            @Override
//            public void onClickView(View v) {
//                setClickChannelView(v);
//                imageAdapter.setOnClickCallback(null);
//            }
//        });
        scroll.setAdapter(imageAdapter);
        imageAdapter.setMap(channelHashMap);
        imageAdapter.setList((ArrayList<ChannelEntity>) channelList);
        imageAdapter.setOnClickListener(channelClickListener);
        scroll.setVisibility(View.VISIBLE);
        scroll.setFocusable(true);
        scroll.requestFocus();
        //scroll.mFocusListener = mFocusListener;
        scroll.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View view, boolean flag) {
                if (channelscrollIndex > 0 || scroll.getSelectedView() == null)
                    return;
                TextView v = (TextView) scroll.getSelectedView().findViewById(R.id.channel_item);
                if (flag && scrollFromBorder) {
                    v.setBackgroundResource(R.drawable.channel_item_selectd_focus);
                } else {
                    //v.setTextColor(R.color._ffffff);
                    v.setBackgroundResource(R.drawable.channel_item_focus);
                }
                v.invalidate();
            }
        });
        scroll.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (view == null || channelscrollIndex >0) return;
                TextView channelBtn = (TextView) view.findViewById(R.id.channel_item);
                channelChange = ChannelChange.CLICK_CHANNEL;

//                if (arrow_left.getVisibility() == View.GONE) {
//                    arrow_left.setVisibility(View.VISIBLE);
//                }
//                if (arrow_right.getVisibility() == View.GONE) {
//                    arrow_right.setVisibility(View.VISIBLE);
//                }

                int channelPosition = i;


                if (lastview != null) {


                    TextView mlastview = (TextView) lastview.findViewById(R.id.channel_item);
                    mlastview.setBackgroundResource(0);
                    mlastview.setTextColor(NORMAL_CHANNEL_TEXTCOLOR);
                    AnimationSet animationSet1 = new AnimationSet(true);
                    ScaleAnimation scaleAnimation1 = new ScaleAnimation(1.05f, 1f, 1.05f, 1f,
                            Animation.RELATIVE_TO_SELF, 0.5f,
                            Animation.RELATIVE_TO_SELF, 0.5f);
                    scaleAnimation1.setDuration(200);
                    animationSet1.addAnimation(scaleAnimation1);
                    animationSet1.setFillAfter(true);
                    mlastview.startAnimation(animationSet1);
                }
                // if(view!=clickView){
                if (!scrollFromBorder)
                    channelBtn.setBackgroundResource(R.drawable.channel_item_selectd_focus);
                else
                    channelBtn.setBackgroundResource(R.drawable.channel_item_focus);
                AnimationSet animationSet = new AnimationSet(true);
                ScaleAnimation scaleAnimation = new ScaleAnimation(1, 1.05f, 1, 1.05f,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);
                scaleAnimation.setDuration(200);
                animationSet.addAnimation(scaleAnimation);
                animationSet.setFillAfter(true);
                channelBtn.startAnimation(animationSet);


                if (lastview == view) {
                    return;
                }

                lastview = view;
                // }
                mCurrentChannelPosition.setPosition(channelPosition);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        scroll.setHorizontalFadingEdgeEnabled(true);
        scroll.setFadingEdgeLength(72);
        scroll.requestFocus();
    }

    private void registerUpdateReceiver() {
        appUpdateReceiver = new AppUpdateReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AppConstant.APP_UPDATE_ACTION);
        registerReceiver(appUpdateReceiver, intentFilter);
    }

    @Override
    public void onScrollStateChanged(HGridView view, int scrollState) {

    }

    @Override
    public void onScroll(HGridView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (firstVisibleItem == 0) {
            // scroll.setSelection(0);
        	if(channelscrollIndex == 0){
            setClickChannelView(scroll.getChildAt(0));
            lastview = scroll.getChildAt(0);
            TextView v = (TextView) scroll.getSelectedView().findViewById(R.id.channel_item);
            v.setBackgroundResource(R.drawable.channel_item_selectd_focus);
        	}else{
                lastview = scroll.getChildAt(0);
                TextView v = (TextView) scroll.getSelectedView().findViewById(R.id.channel_item);
                v.setBackgroundResource(0);
        	}
            scroll.setOnScrollListener(null);
        }

    }


    /**
     * receive app update broadcast, and show update popup window
     */
    class AppUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final Bundle bundle = intent.getBundleExtra("data");
            contentView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    showUpdatePopup(contentView, bundle);
                }
            }, 2000);

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
        contentView.setBackgroundResource(R.drawable.app_update_bg);
        guide_shadow_view.setVisibility(View.VISIBLE);
        float density = getResources().getDisplayMetrics().density;

        int appUpdateHeight = (int) (getResources().getDimension(R.dimen.app_update_bg_height));
        int appUpdateWidht = (int) (getResources().getDimension(R.dimen.app_update_bg_width));


        updatePopupWindow = new PopupWindow(null, appUpdateHeight, appUpdateWidht);
        updatePopupWindow.setContentView(contentView);
        updatePopupWindow.setFocusable(true);
        updatePopupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        Button updateNow = (Button) contentView.findViewById(R.id.update_now_bt);
        LinearLayout updateMsgLayout = (LinearLayout) contentView.findViewById(R.id.update_msg_layout);

        final String path = bundle.getString("path");

        ArrayList<String> msgs = bundle.getStringArrayList("msgs");

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = (int) (getResources().getDimension(R.dimen.app_update_content_margin_left));
        layoutParams.topMargin = (int) (getResources().getDimension(R.dimen.app_update_line_margin_));

        for (String msg : msgs) {
            View textLayout = LayoutInflater.from(this).inflate(R.layout.update_msg_text_item, null);
            TextView textView = (TextView) textLayout.findViewById(R.id.update_msg_text);
            textView.setText(msg);
            updateMsgLayout.addView(textLayout);
        }

        updateNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePopupWindow.dismiss();
                guide_shadow_view.setVisibility(View.GONE);
                installApk(context, path);
            }
        });
    }

    public void installApk(Context mContext, String path) {
        Uri uri = Uri.parse("file://" + path);
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

    private void showExitPopup(View view) {
        exitPopupWindow = new MessagePopWindow(this);
        exitPopupWindow.setFirstMessage(R.string.exit_prompt);
//        WindowManager.LayoutParams lp = getWindow().getAttributes();
//		lp.alpha = 0.5f;
//		getWindow().setAttributes(lp);
        guide_shadow_view.setVisibility(View.VISIBLE);
        exitPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                guide_shadow_view.setVisibility(View.GONE);
            }
        });
        exitPopupWindow.showAtLocation(view, Gravity.CENTER, 0, 0, new MessagePopWindow.ConfirmListener() {
                    @Override
                    public void confirmClick(View view) {
                        exitPopupWindow.dismiss();
                        CallaPlay callaPlay = new CallaPlay();
                        callaPlay.app_exit(System.currentTimeMillis() - app_start_time, SimpleRestClient.appVersion);
                        TVGuideActivity.this.finish();
                        ArrayList<String> cache_log = tv.ismar.daisy.core.MessageQueue.getQueueList();
                        HashSet<String> hasset_log = new HashSet<String>();
                        for (int i = 0; i < cache_log.size(); i++) {
                            hasset_log.add(cache_log.get(i));
                        }
                        DaisyUtils
                                .getVodApplication(TVGuideActivity.this)
                                .getEditor()
                                .putStringSet(VodApplication.CACHED_LOG,
                                        hasset_log);
                        DaisyUtils.getVodApplication(getApplicationContext())
                                .save();
                        System.exit(0);
                    }
                },
                new MessagePopWindow.CancelListener() {
                    @Override
                    public void cancelClick(View view) {
                        exitPopupWindow.dismiss();
//                        WindowManager.LayoutParams lp = getWindow().getAttributes();
//        				lp.alpha = 1f;
//        				getWindow().setAttributes(lp);
                        guide_shadow_view.setVisibility(View.GONE);
                    }
                }
        );
    }

    @Override
    public void onSuccess(Result result) {
        saveActivedInfo(result);
        saveSimpleRestClientPreferences(this, result);
        DaisyUtils.getVodApplication(TVGuideActivity.this).getNewContentModel();
        fetchChannels();


        sendLoncationRequest();
        String appUpdateHost = "http://" + result.getUpgrade_domain();
//        AppUpdateUtils.getInstance(this).checkUpdate(appUpdateHost);
        AppUpdateUtilsV2.getInstance(this).checkAppUpdate(appUpdateHost);
    }

    private void saveActivedInfo(Result result) {
        AccountSharedPrefs accountSharedPrefs = AccountSharedPrefs.getInstance();
        accountSharedPrefs.setSharedPrefs(AccountSharedPrefs.APP_UPDATE_DOMAIN, result.getUpgrade_domain());
        accountSharedPrefs.setSharedPrefs(AccountSharedPrefs.LOG_DOMAIN, result.getLog_Domain());
        accountSharedPrefs.setSharedPrefs(AccountSharedPrefs.API_DOMAIN, result.getDomain());
        accountSharedPrefs.setSharedPrefs(AccountSharedPrefs.ADVERTISEMENT_DOMAIN, result.getAd_domain());

        accountSharedPrefs.setSharedPrefs(AccountSharedPrefs.DEVICE_TOKEN, result.getDevice_token());
        accountSharedPrefs.setSharedPrefs(AccountSharedPrefs.SN_TOKEN, result.getSn_Token());


    }


    @Override
    public void onFailed(String erro) {
        Log.e(TAG, "active error: " + erro);
        fetchChannels();
        if (StringUtils.isEmpty(SimpleRestClient.sn_token)) {
            netErrorPopupHandler = new Handler(Looper.getMainLooper());
            netErrorPopupRunnable = new Runnable() {
                @Override
                public void run() {
                    showNetErrorPopup();
                }
            };
            netErrorPopupHandler.postDelayed(netErrorPopupRunnable, 2000);
        }
    }

    private void showNetErrorPopup() {
        final MessageDialogFragment dialog = new MessageDialogFragment(TVGuideActivity.this, getString(R.string.fetch_net_data_error), null);
        dialog.setButtonText(getString(R.string.setting_network), getString(R.string.i_know));
        try {
            dialog.showAtLocation(contentView, Gravity.CENTER,
                    new MessageDialogFragment.ConfirmListener() {
                        @Override
                        public void confirmClick(View view) {
                            Intent intent = new Intent(Settings.ACTION_SETTINGS);
                            TVGuideActivity.this.startActivity(intent);
                        }
                    }, new MessageDialogFragment.CancelListener() {

                        @Override
                        public void cancelClick(View view) {
                            dialog.dismiss();
                        }
                    });
        } catch (android.view.WindowManager.BadTokenException e) {
        }
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
            DaisyUtils.getVodApplication(TVGuideActivity.this).getEditor().putString(VodApplication.LOCATION_INFO, location.getAddrStr());
            DaisyUtils.getVodApplication(TVGuideActivity.this).getEditor().putString(LOCATION_PROVINCE, location.getProvince());
            DaisyUtils.getVodApplication(TVGuideActivity.this).getEditor().putString(LOCATION_CITY, location.getCity());
            DaisyUtils.getVodApplication(TVGuideActivity.this).getEditor().putString(LOCATION_DISTRICT, location.getDistrict());
            DaisyUtils.getVodApplication(TVGuideActivity.this).save();
        }
    }

    BitmapDecoder ddddBitmapDecoder;

    private void selectChannelByPosition(int position) {
        String tag;
        if (lastchannelindex < position) {
            scrollType = ScrollType.right;
        } else {
            scrollType = ScrollType.left;
        }
        if (position == 0) {
            arrow_left.setVisibility(View.GONE);
            arrow_left_visible.setVisibility(View.GONE);
            if (channelChange != null && channelChange != ChannelChange.CLICK_CHANNEL)
                channelChange = ChannelChange.RIGHT_ARROW;
        } else {
            arrow_left.setVisibility(View.VISIBLE);
            arrow_left_visible.setVisibility(View.VISIBLE);
        }

        if (position == mChannelEntitys.length - 1) {
            arrow_right.setVisibility(View.GONE);
            arrow_right_visible.setVisibility(View.GONE);
            if (channelChange != null && channelChange != ChannelChange.CLICK_CHANNEL)
                channelChange = ChannelChange.LEFT_ARROW;
        } else {
            arrow_right.setVisibility(View.VISIBLE);
            arrow_right_visible.setVisibility(View.VISIBLE);
        }
        ChannelEntity channelEntity = mChannelEntitys[position];
        topView.setSubTitle(channelEntity.getName());
        currentFragment = null;
        Log.i("template==", channelEntity.getHomepage_template());
        if ("template1".equals(channelEntity.getHomepage_template())) {
            currentFragment = new FilmFragment();
            tag = "template1";
        } else if ("template2".equals(channelEntity.getHomepage_template())) {
            currentFragment = new EntertainmentFragment();
            tag = "template2";
        } else if ("template3".equals(channelEntity.getHomepage_template())) {
            currentFragment = new SportFragment();
            tag = "template3";
        } else if ("template4".equals(channelEntity.getHomepage_template())) {
            currentFragment = new ChildFragment();
            tag = "template4";
        } else {
            currentFragment = new GuideFragment();
            tag = "template";
        }
        if (lastFragment != null) {
            if (lastFragment instanceof ChildFragment) {
                if (currentFragment instanceof ChildFragment) {
                } else {
                    destroybackground();
                    if (ddddBitmapDecoder != null) {
                        ddddBitmapDecoder.removeAllCallback();
//						ddddBitmapDecoder.interrupt();
                    }
                    ddddBitmapDecoder = new BitmapDecoder();
                    ddddBitmapDecoder.decode(this, R.drawable.main_bg,
                            new BitmapDecoder.Callback() {
                                @Override
                                public void onSuccess(
                                        BitmapDrawable bitmapDrawable) {
                                    contentView
                                            .setBackgroundDrawable(bitmapDrawable);
                                }
                            });
                }
            } else {
                if (currentFragment instanceof ChildFragment) {
                    destroybackground();
                    if (ddddBitmapDecoder != null) {
                        ddddBitmapDecoder.removeAllCallback();
//						ddddBitmapDecoder.interrupt();
                    }
                    ddddBitmapDecoder = new BitmapDecoder();
                    ddddBitmapDecoder.decode(this,
                            R.drawable.channel_child_bg,
                            new BitmapDecoder.Callback() {
                                @Override
                                public void onSuccess(
                                        BitmapDrawable bitmapDrawable) {
                                    contentView
                                            .setBackgroundDrawable(bitmapDrawable);
                                }
                            });
                } else {
                }
            }
        }
        lastFragment = currentFragment;
        if (scrollFromBorder) {
            currentFragment.setScrollFromBorder(scrollFromBorder);
            currentFragment.setRight(rightscroll);
            currentFragment.setBottomFlag(lastviewTag);
        }
        // currentFragment.view = scroll;
        //currentFragment.position = position;
//        scrollFromBorder = false;
        currentFragment.setChannelEntity(channelEntity);
        ChannelBaseFragment t = (ChannelBaseFragment) getSupportFragmentManager().findFragmentByTag("template");
        ChannelBaseFragment t1 = (ChannelBaseFragment) getSupportFragmentManager().findFragmentByTag("template1");
        ChannelBaseFragment t2 = (ChannelBaseFragment) getSupportFragmentManager().findFragmentByTag("template2");
        ChannelBaseFragment t3 = (ChannelBaseFragment) getSupportFragmentManager().findFragmentByTag("template3");
        ChannelBaseFragment t4 = (ChannelBaseFragment) getSupportFragmentManager().findFragmentByTag("template4");
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if ("template".equals(tag)) {
            if (t1 != null)
                transaction.hide(t1);
            if (t2 != null)
                transaction.hide(t2);
            if (t3 != null)
                transaction.hide(t3);
            if (t4 != null)
                transaction.hide(t4);
            if (t != null) {
                transaction.show(t);
                transaction.commitAllowingStateLoss();
            } else {
                replaceFragment(currentFragment, tag, transaction);
            }
        }
        if ("template1".equals(tag)) {
            if (t != null)
                transaction.hide(t);
            if (t2 != null)
                transaction.hide(t2);
            if (t3 != null)
                transaction.hide(t3);
            if (t4 != null)
                transaction.hide(t4);
            if (t1 != null) {
                t1.setChannelEntity(channelEntity);
                t1.refreshData();
                transaction.show(t1);
                transaction.commitAllowingStateLoss();
            } else {
                replaceFragment(currentFragment, tag, transaction);
            }
        }
        if ("template2".equals(tag)) {
            if (t != null)
                transaction.hide(t);
            if (t1 != null)
                transaction.hide(t1);
            if (t3 != null)
                transaction.hide(t3);
            if (t4 != null)
                transaction.hide(t4);
            if (t2 != null) {
                t2.setChannelEntity(channelEntity);
                t2.refreshData();
                transaction.show(t2);
                transaction.commitAllowingStateLoss();
            } else {
                replaceFragment(currentFragment, tag, transaction);
            }
        }
        if ("template3".equals(tag)) {
            if (t != null)
                transaction.hide(t);
            if (t1 != null)
                transaction.hide(t1);
            if (t2 != null)
                transaction.hide(t2);
            if (t4 != null)
                transaction.hide(t4);
            if (t3 != null) {
                t3.setChannelEntity(channelEntity);
                t3.refreshData();
                transaction.show(t3);
                transaction.commitAllowingStateLoss();
            } else {
                replaceFragment(currentFragment, tag, transaction);
            }
        }
        if ("template4".equals(tag)) {
            if (t != null)
                transaction.hide(t);
            if (t1 != null)
                transaction.hide(t1);
            if (t2 != null)
                transaction.hide(t2);
            if (t3 != null)
                transaction.hide(t3);
            if (t4 != null) {
                t4.setChannelEntity(channelEntity);
                t4.refreshData();
                transaction.show(t4);
                transaction.commitAllowingStateLoss();
            } else {
                replaceFragment(currentFragment, tag, transaction);
            }
        }
//        replaceFragment(currentFragment,tag);
        View view2 = scroll.getChildAt(0);
        if (view2 == null)
            return;
        lastchannelindex = position;
        if (view2.getLeft() > 0) {
            switch (mCurrentChannelPosition.getPosition()) {
                case 0:
                    scroll.setNextFocusUpId(R.id.guidefragment_firstpost);
                    break;
                case 1:
                    scroll.setNextFocusUpId(R.id.filmfragment_secondpost);
                    break;
                case 2:
                    scroll.setNextFocusUpId(R.id.filmfragment_thirdpost);
                    break;
                case 3:
                    scroll.setNextFocusUpId(R.id.vaiety_channel2_image);
                    break;
                case 4:
                    scroll.setNextFocusUpId(R.id.vaiety_channel3_image);
                    break;
                case 5:
                    scroll.setNextFocusUpId(R.id.sport_channel4_image);
                    break;
                case 6:
                    scroll.setNextFocusUpId(R.id.vaiety_channel4_image);
                    break;
                case 7:
                    scroll.setNextFocusUpId(R.id.child_more);
                    break;
                case 8:
                    scroll.setNextFocusUpId(R.id.listmore);
                    break;
                case 9:
                    scroll.setNextFocusUpId(R.id.listmore);
                    break;
                default:
                    break;
            }
        } else {
            switch (mCurrentChannelPosition.getPosition()) {
                case 0:
                    scroll.setNextFocusUpId(R.id.guidefragment_firstpost);
                    break;
                case 1:
                    scroll.setNextFocusUpId(R.id.filmfragment_firstpost);
                    break;
                case 2:
                    scroll.setNextFocusUpId(R.id.filmfragment_secondpost);
                    break;
                case 3:
                    scroll.setNextFocusUpId(R.id.vaiety_channel2_image);
                    break;
                case 4:
                    scroll.setNextFocusUpId(R.id.vaiety_channel2_image);
                    break;
                case 5:
                    scroll.setNextFocusUpId(R.id.sport_channel3_image);
                    break;
                case 6:
                    scroll.setNextFocusUpId(R.id.vaiety_channel4_image);
                    break;
                case 7:
                    scroll.setNextFocusUpId(R.id.child_more);
                    break;
                case 8:
                    scroll.setNextFocusUpId(R.id.listmore);
                    break;
                case 9:
                    scroll.setNextFocusUpId(R.id.listmore);
                    break;
                default:
                    break;
            }
        }
    }

    private void setbackground(int id) {


        BitmapFactory.Options opt = new BitmapFactory.Options();

        opt.inPreferredConfig = Bitmap.Config.ALPHA_8;

        opt.inPurgeable = true;

        opt.inInputShareable = true;
        opt.inTargetDensity = getResources().getDisplayMetrics().densityDpi;
        opt.inDensity = getResources().getDisplayMetrics().densityDpi;

        InputStream is = getResources().openRawResource(

                id);

        Bitmap bm = BitmapFactory.decodeStream(is, null, opt);

        BitmapDrawable bd = new BitmapDrawable(getResources(), bm);
        contentView.setBackgroundDrawable(bd);
    }

    private void destroybackground() {
        BitmapDrawable bd = (BitmapDrawable) contentView.getBackground();
        contentView.setBackgroundResource(0);//别忘了把背景设为null，避免onDraw刷新背景时候出现used a recycled bitmap错误
        if (bd == null)
            return;
        bd.setCallback(null);
        bd.getBitmap().recycle();
    }

    private void replaceFragment(Fragment fragment, String tag, FragmentTransaction transaction) {
        switch (scrollType) {
            case left:
                transaction.setCustomAnimations(
                        R.anim.push_right_in,
                        R.anim.push_right_out);
                break;
            case right:
                transaction.setCustomAnimations(
                        R.anim.push_left_in,
                        R.anim.push_left_out);
                break;
        }

        transaction.replace(R.id.container, fragment, tag).commit();
    }

    private void saveSimpleRestClientPreferences(Context context, Result result) {
        SimpleRestClient.root_url = "http://" + result.getDomain();
        SimpleRestClient.sRoot_url = "http://" + result.getDomain();
        SimpleRestClient.ad_domain = "http://" + result.getAd_domain();
        SimpleRestClient.log_domain = "http://" + result.getLog_Domain();
        SimpleRestClient.device_token = result.getDevice_token();
        DaisyUtils.getVodApplication(context).getEditor().putString(VodApplication.DEVICE_TOKEN, SimpleRestClient.device_token);
        DaisyUtils.getVodApplication(context).save();
        SimpleRestClient.sn_token = result.getSn_Token();
        SimpleRestClient.mobile_number = DaisyUtils.getVodApplication(this).getPreferences().getString(VodApplication.MOBILE_NUMBER, "");
        SimpleRestClient.access_token = DaisyUtils.getVodApplication(this).getPreferences().getString(VodApplication.AUTH_TOKEN, "");
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
    protected void onResume() {
        super.onResume();
        channelscrollIndex = 0;
        topView = (LaunchHeaderLayout) findViewById(R.id.top_column_layout);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (netErrorPopupHandler != null && netErrorPopupRunnable != null) {
            netErrorPopupHandler.removeCallbacks(netErrorPopupRunnable);
        }
        if (fragmentSwitch.hasMessages(SWITCH_PAGE))
            fragmentSwitch.removeMessages(SWITCH_PAGE);
        if (fragmentSwitch.hasMessages(SWITCH_PAGE_FROMLAUNCH))
            fragmentSwitch.removeMessages(SWITCH_PAGE_FROMLAUNCH);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(appUpdateReceiver);
        if (bitmapDecoder != null && bitmapDecoder.isAlive()) {
            bitmapDecoder.interrupt();
        }
        if (ddddBitmapDecoder != null && ddddBitmapDecoder.isAlive()) {
            ddddBitmapDecoder.interrupt();
        }
        if (!(updatePopupWindow == null)) {
            updatePopupWindow.dismiss();
        }
        if (exitPopupWindow != null) {
            exitPopupWindow.dismiss();
        }
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        homepage_template = intent.getStringExtra("homepage_template");
        homepage_url = intent.getStringExtra("homepage_url");
        if (StringUtils.isEmpty(homepage_template)
                || StringUtils.isEmpty(homepage_url)) {
//			fetchChannels();
        } else {
            if (StringUtils.isNotEmpty(SimpleRestClient.root_url)) {
                fetchChannels();
            }
        }
    }


    public void setLastViewTag(String flag) {
        lastviewTag = flag;
    }

    public void resetBorderFocus() {
        scrollFromBorder = false;
    }

    private enum ScrollType {
        left,
        right;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ("lcd_s3a01".equals(VodUserAgent.getModelName())) {
            if (keyCode == 707 || keyCode == 774 || keyCode == 253) {
                isneedpause = false;
            }
        } else {
            if (keyCode == 223 || keyCode == 499 || keyCode == 480) {
                isneedpause = false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    static class FragmentSwitchHandler extends Handler {
        private WeakReference<TVGuideActivity> weakReference;

        public FragmentSwitchHandler(TVGuideActivity activity) {
            weakReference = new WeakReference<TVGuideActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            TVGuideActivity activity = weakReference.get();
            if (activity != null) {
                switch (msg.what) {
                    case SWITCH_PAGE:
                        activity.selectChannelByPosition(msg.arg1);
                        break;
                    case SWITCH_PAGE_FROMLAUNCH:
                    	channelscrollIndex=channelscrollIndex-1;
                        activity.scroll.arrowScroll(View.FOCUS_RIGHT);
                        if (channelscrollIndex > 0) {
                            sendEmptyMessage(SWITCH_PAGE_FROMLAUNCH);
                        }
                        break;
                }
            }
        }
    }

}
