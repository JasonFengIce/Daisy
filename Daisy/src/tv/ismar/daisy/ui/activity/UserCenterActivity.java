package tv.ismar.daisy.ui.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.*;
import cn.ismartv.activator.Activator;
import com.activeandroid.query.Select;
import com.google.gson.Gson;
import tv.ismar.daisy.BaseActivity;
import tv.ismar.daisy.R;
import tv.ismar.daisy.VodApplication;
import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.client.IsmartvUrlClient;
import tv.ismar.daisy.core.preferences.AccountSharedPrefs;
import tv.ismar.daisy.data.usercenter.AuthTokenEntity;
import tv.ismar.daisy.models.Favorite;
import tv.ismar.daisy.models.History;
import tv.ismar.daisy.models.Item;
import tv.ismar.daisy.ui.fragment.usercenter.*;
import tv.ismar.daisy.ui.widget.LaunchHeaderLayout;

import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by huaijie on 7/3/15.
 */
public class UserCenterActivity extends BaseActivity implements View.OnClickListener, BaseActivity.OnLoginCallback, OnFocusChangeListener {

    private static final String TAG = "UserCenterActivity";
    private static final int MSG_INDICATOR_CHANGE = 0x0001;
    public static final String ACCOUNT_SHARED_PREFS = "account";
    public static final String ACCOUNT_COMBINE = "combine";
    public static final String LOCATION_FRAGMENT = "location";
    private static final int[] INDICATOR_TEXT_RES_ARRAY = {
            R.string.usercenter_store,
            R.string.usercenter_userinfo,
            R.string.usercenter_login_register,
            R.string.usercenter_purchase_history,
            R.string.usercenter_help,
            R.string.usercenter_location
    };

    private ArrayList<View> indicatorView;
    private LinearLayout userCenterIndicatorLayout;
    //    private StoreFragment storeFragment;
//    private UserInfoFragment userInfoFragment;
    private LoginFragment loginFragment;
    //    private PurchaseHistoryFragment historyFragment;
//    private HelpFragment helpFragment;
    private LocationFragment locationFragment;
    private LaunchHeaderLayout topView;
    private SharedPreferences accountPreference;
    private PopupWindow loginPopup;
    private PopupWindow combineAccountPop;
    private SharedPreferences accountSharedPrefs;
    private View mContentView;
    private SimpleRestClient mSimpleRestClient;
    private Item[] mHistoriesByNet;
    private String mAccessToken;
    private String mNickName;
    private static ImageView verticalDividerView;

    private IndicatorType mIndicatorType = IndicatorType.STORE;
    private int currentFragmentIndictor;
    private SharedPreferences.OnSharedPreferenceChangeListener changeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            String accessToken = accountPreference.getString("auth_token", "");
            String phoneNumber = accountPreference.getString("mobile_number", "");
            if (!TextUtils.isEmpty(accessToken) && !TextUtils.isEmpty(phoneNumber)) {
                changeViewState(indicatorView.get(2), ViewState.Disable);

            } else {
                changeViewState(indicatorView.get(2), ViewState.Enable);

            }
        }
    };

    private boolean fargmentIsActive = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        accountSharedPrefs = getSharedPreferences(ACCOUNT_SHARED_PREFS, Context.MODE_PRIVATE);
        mContentView = LayoutInflater.from(this).inflate(R.layout.activity_usercenter, null);
        setContentView(mContentView);
        mSimpleRestClient = new SimpleRestClient();
        View background = findViewById(R.id.large_layout);
        DaisyUtils.setbackground(R.drawable.main_bg, background);
        accountPreference = getSharedPreferences("Daisy", Context.MODE_PRIVATE);
        accountPreference.registerOnSharedPreferenceChangeListener(changeListener);
        userCenterIndicatorLayout = (LinearLayout) findViewById(R.id.user_center_indicator_layout);
//        storeFragment = new StoreFragment();
//        userInfoFragment = new UserInfoFragment();
//        loginFragment = new LoginFragment();
//        historyFragment = new PurchaseHistoryFragment();
//        helpFragment = new HelpFragment();
        locationFragment = new LocationFragment();
        setLoginCallback(this);
        initViews();
        createIndicatorView();
        String flag = getIntent().getStringExtra("flag");

        if (!TextUtils.isEmpty(flag) && flag.equals(LOCATION_FRAGMENT)) {
            changeViewState(indicatorView.get(5), ViewState.Overlay);
            mIndicatorType = IndicatorType.LOCATION;
            getSupportFragmentManager().beginTransaction().add(R.id.user_center_container, locationFragment).commit();
            currentFragmentIndictor = R.string.usercenter_location;
        } else {
            changeViewState(indicatorView.get(0), ViewState.Overlay);
            mIndicatorType = IndicatorType.STORE;
            getSupportFragmentManager().beginTransaction().add(R.id.user_center_container, new StoreFragment()).commit();
            currentFragmentIndictor = R.string.usercenter_store;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        fargmentIsActive = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        fargmentIsActive = false;
        if (messageHandler.hasMessages(MSG_INDICATOR_CHANGE))
            messageHandler.removeMessages(MSG_INDICATOR_CHANGE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initViews() {
        topView = (LaunchHeaderLayout) findViewById(R.id.top_column_layout);
        topView.hideIndicatorTable();

        verticalDividerView = (ImageView) findViewById(R.id.vertical_divider_line);
        verticalDividerView.setTag(R.id.vertical_divider_line);
        verticalDividerView.setOnFocusChangeListener(this);
    }


    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();

        topView.setTitle(getText(R.string.user_center).toString());
        topView.hideSubTiltle();
    }

    /**
     * createIndicatorView
     */
    private void createIndicatorView() {
        indicatorView = new ArrayList<View>();
        userCenterIndicatorLayout.removeAllViews();
        for (int res : INDICATOR_TEXT_RES_ARRAY) {
            View frameLayout = LayoutInflater.from(this).inflate(R.layout.item_usercenter_indicator, null);
            TextView textView = (TextView) frameLayout.findViewById(R.id.indicator_text);
            textView.setText(res);
            frameLayout.setTag(res);
            frameLayout.setOnClickListener(this);
            frameLayout.setOnFocusChangeListener(this);
            frameLayout.setNextFocusRightId(R.id.vertical_divider_line);
            indicatorView.add(frameLayout);
            userCenterIndicatorLayout.addView(frameLayout);
        }

        if (!TextUtils.isEmpty(SimpleRestClient.access_token) && !TextUtils.isEmpty(SimpleRestClient.mobile_number)) {
            changeViewState(indicatorView.get(2), ViewState.Disable);
        } else {
            changeViewState(indicatorView.get(2), ViewState.Enable);
        }
    }


    public void switchToUserInfoFragment() {
        userCenterIndicatorLayout.getChildAt(1).requestFocus();
        getSupportFragmentManager().beginTransaction().replace(R.id.user_center_container, new UserInfoFragment()).commit();
        currentFragmentIndictor = R.string.usercenter_userinfo;
    }

//    @Override
//    public void onBackPressed() {
//        if (userInfoFragment.isLoginFragmentShowing()) {
//            userInfoFragment.hideLoginFragment();
//        } else {
//            super.onBackPressed();
//        }
//    }


    @Override
    public void onLoginSuccess(String result) {
        AuthTokenEntity authTokenEntity = new Gson().fromJson(result, AuthTokenEntity.class);
        Log.i("pangziinfo", "authTokenEntity.getAuth_token()==" + authTokenEntity.getAuth_token());
        mAccessToken = authTokenEntity.getAuth_token();
    }

    @Override
    public void onLoginFailed() {

    }

    @Override
    public void oncallWGQueryQQUserInfo(String nickName) {
        mNickName = nickName;
        saveToLocal(mAccessToken, mNickName);
        if (listener != null) {
//            userInfoFragment = new UserInfoFragment();
            listener = null;
        }
        showLoginSuccessPopup();
    }


    public void accountsCombine() {
        String api = SimpleRestClient.root_url + "/accounts/combine/";
        long timestamp = System.currentTimeMillis();
        Activator activator = Activator.getInstance(this);
        String rsaResult = activator.PayRsaEncode("sn=" + SimpleRestClient.sn_token + "&timestamp=" + timestamp);

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("device_token", SimpleRestClient.device_token);
        params.put("access_token", SimpleRestClient.access_token);
        params.put("timestamp", String.valueOf(timestamp));
        params.put("sign", rsaResult);

        new IsmartvUrlClient().doRequest(IsmartvUrlClient.Method.POST, api, params, new IsmartvUrlClient.CallBack() {
            @Override
            public void onSuccess(String result) {
                SharedPreferences.Editor editor = accountSharedPrefs.edit();
                editor.putBoolean(ACCOUNT_COMBINE, true);
                editor.apply();

                Log.d(TAG, "accountsCombine: " + result);
            }

            @Override
            public void onFailed(Exception exception) {
                SharedPreferences.Editor editor = accountSharedPrefs.edit();
                editor.putBoolean(ACCOUNT_COMBINE, false);
                editor.apply();
                Log.e(TAG, "accountsCombine: " + exception.getMessage());
            }
        });


    }


    private void showLoginSuccessPopup() {
        View popupLayout = LayoutInflater.from(this).inflate(R.layout.popup_login_success, null);
        TextView textView = (TextView) popupLayout.findViewById(R.id.login_success_msg);
        String msg = getText(R.string.login_success).toString();
        String phoneNumber = mNickName;
        textView.setText(String.format(msg, phoneNumber));

        Button button = (Button) popupLayout.findViewById(R.id.login_success_btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginPopup.dismiss();
                showAccountsCombinePopup();
            }
        });


        int width = (int) getResources().getDimension(R.dimen.login_pop_width);
        int height = (int) getResources().getDimension(R.dimen.login_pop_height);
        loginPopup = new PopupWindow(popupLayout, width, height);
        loginPopup.setFocusable(true);
        loginPopup.setBackgroundDrawable(getResources().getDrawable(R.drawable.transparent));
        int xOffset = (int) getResources().getDimension(R.dimen.loginfragment_successPop_xOffset);
        int yOffset = (int) getResources().getDimension(R.dimen.loginfragment_successPop_yOffset);

        loginPopup.showAtLocation(mContentView, Gravity.CENTER, xOffset, yOffset);
    }


    public void showAccountsCombinePopup() {
        View popupLayout = LayoutInflater.from(this).inflate(R.layout.popup_account_combine, null);
        int width = (int) getResources().getDimension(R.dimen.login_pop_width);
        int height = (int) getResources().getDimension(R.dimen.login_pop_height);
        combineAccountPop = new PopupWindow(popupLayout, width, height);
        combineAccountPop.setFocusable(true);
        combineAccountPop.setBackgroundDrawable(getResources().getDrawable(R.drawable.transparent));
        int xOffset = (int) getResources().getDimension(R.dimen.loginfragment_successPop_xOffset);
        int yOffset = (int) getResources().getDimension(R.dimen.loginfragment_successPop_yOffset);
        combineAccountPop.showAtLocation(mContentView, Gravity.CENTER, xOffset, yOffset);

        Button confirm = (Button) popupLayout.findViewById(R.id.confirm_account_combine);
        Button cancel = (Button) popupLayout.findViewById(R.id.cancel_account_combine);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accountsCombine();
                combineAccountPop.dismiss();
                switchToUserInfoFragment();

            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = accountSharedPrefs.edit();
                editor.putBoolean(ACCOUNT_COMBINE, false);
                editor.apply();

                combineAccountPop.dismiss();
                switchToUserInfoFragment();
            }
        });


    }

    @Override
    public void onClick(View v) {
        handlerClick(v);
    }

    private void clearFocus(View exceptView, List<View> viewList) {
        for (View view : viewList) {
            if (exceptView != view) {
                changeViewState(view, ViewState.None);
            }
        }

        if (!TextUtils.isEmpty(SimpleRestClient.access_token) && !TextUtils.isEmpty(SimpleRestClient.mobile_number)) {
            changeViewState(indicatorView.get(2), ViewState.Disable);
        }
    }


    private void handlerClick(View v) {
        if (fargmentIsActive) {


            switch ((Integer) v.getTag()) {
                case R.string.usercenter_store:
                    if (currentFragmentIndictor == R.string.usercenter_store)
                        return;
                    getSupportFragmentManager().beginTransaction().replace(R.id.user_center_container, new StoreFragment()).commitAllowingStateLoss();
                    currentFragmentIndictor = R.string.usercenter_store;
                    break;
                case R.string.usercenter_userinfo:
                    if (currentFragmentIndictor == R.string.usercenter_userinfo)
                        return;
                    getSupportFragmentManager().beginTransaction().replace(R.id.user_center_container, new UserInfoFragment()).commitAllowingStateLoss();
                    currentFragmentIndictor = R.string.usercenter_userinfo;
                    break;
                case R.string.usercenter_login_register:
                    if (currentFragmentIndictor == R.string.usercenter_login_register)
                        return;
                    loginQQorWX();
                    currentFragmentIndictor = R.string.usercenter_login_register;
                    break;
                case R.string.usercenter_purchase_history:
                    if (currentFragmentIndictor == R.string.usercenter_purchase_history)
                        return;
                    getSupportFragmentManager().beginTransaction().replace(R.id.user_center_container, new PurchaseHistoryFragment()).commitAllowingStateLoss();
                    currentFragmentIndictor = R.string.usercenter_purchase_history;
                    break;
                case R.string.usercenter_help:
                    if (currentFragmentIndictor == R.string.usercenter_help)
                        return;
                    getSupportFragmentManager().beginTransaction().replace(R.id.user_center_container, new HelpFragment()).commitAllowingStateLoss();
                    currentFragmentIndictor = R.string.usercenter_help;
                    break;
                case R.string.usercenter_location:
                    locationFragment.focus = indicatorView.get(5);
                    getSupportFragmentManager().beginTransaction().replace(R.id.user_center_container, locationFragment).commitAllowingStateLoss();
                    currentFragmentIndictor = R.string.usercenter_location;
                    break;
            }

            if (v == indicatorView.get(2)) {
                if (TextUtils.isEmpty(SimpleRestClient.access_token)) {
                    changeViewState(v, ViewState.Overlay);
                } else {
                    changeViewState(v, ViewState.Disable);
                }
            }
        }

    }


    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            switch ((Integer) v.getTag()) {
                case R.id.vertical_divider_line:
                    Log.d(TAG, "onFocusChange: " + "vertical_divider_line ---> " + mIndicatorType.name());
                    deliverFocusEvent();
                    break;
                case R.string.usercenter_store:
                    verticalDividerView.setFocusable(false);
                    mIndicatorType = IndicatorType.STORE;
                    break;
                case R.string.usercenter_userinfo:
                    verticalDividerView.setFocusable(false);
                    mIndicatorType = IndicatorType.USERINFO;
                    break;
                case R.string.usercenter_login_register:
                    verticalDividerView.setFocusable(false);
                    mIndicatorType = IndicatorType.LOGIN;
                    break;
                case R.string.usercenter_purchase_history:
                    verticalDividerView.setFocusable(false);
                    mIndicatorType = IndicatorType.HISTORY;
                    break;
                case R.string.usercenter_help:
                    verticalDividerView.setFocusable(false);
                    mIndicatorType = IndicatorType.HELP;
                    break;
                case R.string.usercenter_location:
                    locationFragment.focus = v;
                    verticalDividerView.setFocusable(false);
                    mIndicatorType = IndicatorType.LOCATION;
                    break;
            }
            if ((Integer) v.getTag() != R.id.vertical_divider_line) {
                clearFocus(v, indicatorView);
                changeViewState(v, ViewState.Overlay);

                messageHandler.removeMessages(MSG_INDICATOR_CHANGE);
                Message message = messageHandler.obtainMessage(MSG_INDICATOR_CHANGE, v);
                messageHandler.sendMessageDelayed(message, 300);
            }


        } else {
            switch ((Integer) v.getTag()) {
                case R.id.vertical_divider_line:
                    break;
                case R.string.usercenter_store:
                case R.string.usercenter_userinfo:
                case R.string.usercenter_purchase_history:
                case R.string.usercenter_help:
                case R.string.usercenter_location:
                    changeViewState(v, ViewState.Select);
                    verticalDividerView.setFocusable(true);
                    break;
            }
        }
    }

    private Handler messageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_INDICATOR_CHANGE:
                    View view = (View) msg.obj;
                    handlerClick(view);
                    break;
            }
        }
    };


    private void deliverFocusEvent() {
        switch (mIndicatorType) {
            case STORE:
                indicatorView.get(0).requestFocus();
                break;
            case USERINFO:
                indicatorView.get(1).requestFocus();
                break;
            case LOGIN:
                indicatorView.get(2).requestFocus();
                break;
            case HISTORY:
                indicatorView.get(3).requestFocus();
                break;
            case HELP:
                indicatorView.get(4).requestFocus();
                break;
            case LOCATION:
                indicatorView.get(5).requestFocus();
                break;
        }

    }


    private enum IndicatorType {
        STORE,
        USERINFO,
        LOGIN,
        HISTORY,
        HELP,
        LOCATION,
    }


    private void changeViewState(View parentView, ViewState viewState) {
        TextView textView = (TextView) parentView.findViewById(R.id.indicator_text);
        ImageView textSelectImage = (ImageView) parentView.findViewById(R.id.text_select_bg);
        ImageView textFocusImage = (ImageView) parentView.findViewById(R.id.text_focus_bg);
        switch (viewState) {
            case Select:
                textView.setTextColor(getResources().getColor(R.color._ffffff));
                textSelectImage.setVisibility(View.INVISIBLE);
                textFocusImage.setImageResource(R.drawable.usercenter_indicator_focused);
                textFocusImage.setVisibility(View.VISIBLE);
                break;
            case Focus:
                textView.setTextColor(getResources().getColor(R.color._ff9c3c));
                textSelectImage.setVisibility(View.VISIBLE);
                textFocusImage.setVisibility(View.INVISIBLE);

                break;
            case Overlay:
                textView.setTextColor(getResources().getColor(R.color._ffffff));
                textSelectImage.setVisibility(View.INVISIBLE);
                textFocusImage.setImageResource(R.drawable.usercenter_indicator_overlay);
                textFocusImage.setVisibility(View.VISIBLE);
                break;
            case None:
                textView.setTextColor(getResources().getColor(R.color._ffffff));
                textSelectImage.setVisibility(View.INVISIBLE);
                textFocusImage.setVisibility(View.INVISIBLE);
                break;
            case Disable:
                textSelectImage.setVisibility(View.INVISIBLE);
                textFocusImage.setVisibility(View.INVISIBLE);
                textView.setText(R.string.usercenter_login);
                textView.setTextColor(getResources().getColor(R.color.personinfo_login_button_disable));
                parentView.setFocusable(false);
                parentView.setFocusableInTouchMode(false);
                break;
            case Enable:
                parentView.setFocusable(true);
                parentView.setFocusableInTouchMode(true);
                textView.setText(R.string.usercenter_login_register);
                textView.setTextColor(getResources().getColor(R.color._ffffff));
                textSelectImage.setVisibility(View.INVISIBLE);
                textFocusImage.setVisibility(View.INVISIBLE);
                parentView.setBackgroundResource(R.drawable._000000000);
                break;
        }

    }

    private enum ViewState {
        Enable,
        Disable,
        Select,
        Focus,
        Overlay,
        None
    }


    private void addHistory(Item item) {
        History history = new History();
        history.title = item.title;
        history.adlet_url = item.adlet_url;
        history.content_model = item.content_model;
        history.is_complex = item.is_complex;
        history.last_position = item.offset;
        history.last_quality = item.quality;
        if ("subitem".equals(item.model_name)) {
            history.sub_url = item.url;
            history.url = SimpleRestClient.root_url + "/api/item/" + item.item_pk + "/";
        } else {
            history.url = item.url;

        }


        history.is_continue = true;
        if (SimpleRestClient.isLogin())
            DaisyUtils.getHistoryManager(this).addHistory(history,
                    "yes");
        else
            DaisyUtils.getHistoryManager(this)
                    .addHistory(history, "no");

    }

    private void getHistoryByNet() {

        mSimpleRestClient.doSendRequest("/api/histories/", "get", "",
                new SimpleRestClient.HttpPostRequestInterface() {

                    @Override
                    public void onSuccess(String info) {
                        // TODO Auto-generated method stub
                        // Log.i(tag, msg);

                        // 解析json
                        mHistoriesByNet = mSimpleRestClient.getItems(info);
                        if (mHistoriesByNet != null) {
                            for (Item i : mHistoriesByNet) {
                                addHistory(i);
                            }
                        }

                    }

                    @Override
                    public void onPrepare() {
                        // TODO Auto-generated method stub
                    }

                    @Override
                    public void onFailed(String error) {
                        // TODO Auto-generated method stub
                        // Log.i(tag, msg);
                    }
                });
    }

    private void fetchFavorite() {
        String api = SimpleRestClient.root_url + "/api/bookmarks/";

        new IsmartvUrlClient().doRequest(api, new IsmartvUrlClient.CallBack() {
            @Override
            public void onSuccess(String result) {
                Item[] favoriteList = new Gson().fromJson(result, Item[].class);
                for (Item item : favoriteList) {
                    addFavorite(item);
                }
            }

            @Override
            public void onFailed(Exception exception) {
                Log.e(TAG, "fetchFavorite: " + exception.getMessage());
            }
        });

    }


    //
    private void addFavorite(Item mItem) {
        if (isFavorite(mItem)) {
            String url = SimpleRestClient.sRoot_url + "/api/item/" + mItem.pk + "/";
        } else {
            String url = SimpleRestClient.sRoot_url + "/api/item/" + mItem.pk + "/";
            Favorite favorite = new Favorite();
            favorite.title = mItem.title;
            favorite.adlet_url = mItem.adlet_url;
            favorite.content_model = mItem.content_model;
            favorite.url = url;
            favorite.quality = mItem.quality;
            favorite.is_complex = mItem.is_complex;
            favorite.isnet = "yes";
            DaisyUtils.getFavoriteManager(this).addFavorite(favorite, favorite.isnet);
        }
    }

    private boolean isFavorite(Item mItem) {
        if (mItem != null) {
            String url = mItem.item_url;
            if (url == null && mItem.pk != 0) {
                url = SimpleRestClient.sRoot_url + "/api/item/" + mItem.pk + "/";
            }
            Favorite favorite = DaisyUtils.getFavoriteManager(this).getFavoriteByUrl(url, "yes");
            if (favorite != null) {
                return true;
            }
        }

        return false;
    }


    private void saveToLocal(String authToken, String phoneNumber) {
        SimpleRestClient.access_token = authToken;
        SimpleRestClient.mobile_number = phoneNumber;
        DaisyUtils.getVodApplication(this).getEditor().putString(VodApplication.AUTH_TOKEN, authToken);
        DaisyUtils.getVodApplication(this).getEditor().putString(VodApplication.MOBILE_NUMBER, phoneNumber);
        DaisyUtils.getVodApplication(this).save();
        fetchFavorite();
        getHistoryByNet();
    }


    public interface OnLoginByChangeCallback {
        void onLoginSuccess();
    }

    private OnLoginByChangeCallback listener;

    public void setAccountListener(OnLoginByChangeCallback l) {

        listener = l;
    }

}
