package tv.ismar.daisy.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import cn.ismartv.activator.Activator;
import com.google.gson.Gson;
import tv.ismar.daisy.*;
import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.client.IsmartvUrlClient;
import tv.ismar.daisy.data.usercenter.AuthTokenEntity;
import tv.ismar.daisy.models.Favorite;
import tv.ismar.daisy.models.History;
import tv.ismar.daisy.models.Item;
import tv.ismar.daisy.ui.fragment.usercenter.*;
import tv.ismar.daisy.ui.widget.LaunchHeaderLayout;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by huaijie on 7/3/15.
 */
public class UserCenterActivity extends BaseActivity implements View.OnClickListener, BaseActivity.OnLoginCallback {

    private static final String TAG = "UserCenterActivity";

    public static final String ACCOUNT_SHARED_PREFS = "account";
    public static final String ACCOUNT_COMBINE = "combine";

    private static final int[] INDICATOR_TEXT_RES_ARRAY = {
            R.string.usercenter_store,
            R.string.usercenter_userinfo,
            R.string.usercenter_login,
            R.string.usercenter_purchase_history,
            R.string.usercenter_help,
            R.string.usercenter_location
    };

    private ArrayList<View> indicatorView;

    private LinearLayout userCenterIndicatorLayout;

    private StoreFragment storeFragment;
    private UserInfoFragment userInfoFragment;
    private LoginFragment loginFragment;
    private PurchaseHistoryFragment historyFragment;
    private HelpFragment helpFragment;
    private LocationFragment locationFragment;

    private LaunchHeaderLayout topView;

    private SharedPreferences accountPreference;

    public static final String LOCATION_FRAGMENT = "location";
    private boolean isFirstLogin = false;


    private PopupWindow loginPopup;
    private PopupWindow combineAccountPop;

    private Context mContext;
    private SharedPreferences accountSharedPrefs;

    private View mContentView;
    private SimpleRestClient mSimpleRestClient;
    private Item[] mHistoriesByNet;

    private String mAccessToken;
    private String mNickName;

    private SharedPreferences.OnSharedPreferenceChangeListener changeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            String accessToken = accountPreference.getString("auth_token", "");
            String phoneNumber = accountPreference.getString("mobile_number", "");
            if (!TextUtils.isEmpty(accessToken) && !TextUtils.isEmpty(phoneNumber)) {
                indicatorView.get(2).setEnabled(false);
                indicatorView.get(2).setFocusable(false);

            } else {
                indicatorView.get(2).setEnabled(true);
                indicatorView.get(2).setFocusable(true);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mContext = this;
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
        storeFragment = new StoreFragment();
        userInfoFragment = new UserInfoFragment();
        loginFragment = new LoginFragment();
        historyFragment = new PurchaseHistoryFragment();
        helpFragment = new HelpFragment();
        locationFragment = new LocationFragment();


        setLoginCallback(this);


        initViews();
        createIndicatorView();

        String flag = getIntent().getStringExtra("flag");
        if (!TextUtils.isEmpty(flag) && flag.equals(LOCATION_FRAGMENT)) {
            getSupportFragmentManager().beginTransaction().add(R.id.user_center_container, locationFragment).commit();
            indicatorView.get(5).setBackgroundResource(R.drawable.table_selected_bg);
        } else {
            getSupportFragmentManager().beginTransaction().add(R.id.user_center_container, storeFragment).commit();
            indicatorView.get(0).setBackgroundResource(R.drawable.table_selected_bg);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initViews() {
        topView = (LaunchHeaderLayout) findViewById(R.id.top_column_layout);


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
            RelativeLayout frameLayout = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.item_usercenter_indicator, null);
            Button textView = (Button) frameLayout.findViewById(R.id.usercenter_indicator_text);
            textView.setText(res);
            textView.setId(res);
            textView.setOnClickListener(this);
            indicatorView.add(textView);
            userCenterIndicatorLayout.addView(frameLayout);
        }

        if (!TextUtils.isEmpty(SimpleRestClient.access_token) && !TextUtils.isEmpty(SimpleRestClient.mobile_number)) {
            indicatorView.get(2).setEnabled(false);
            indicatorView.get(2).setFocusable(false);

        } else {
            indicatorView.get(2).setEnabled(true);
            indicatorView.get(2).setFocusable(true);
        }
    }


    @Override
    public void onClick(View v) {
        int currentViewId = v.getId();
        switch (v.getId()) {
            case R.string.usercenter_store:
                getSupportFragmentManager().beginTransaction().replace(R.id.user_center_container, storeFragment).commit();
                break;
            case R.string.usercenter_userinfo:
                getSupportFragmentManager().beginTransaction().replace(R.id.user_center_container, userInfoFragment).commit();
                break;
            case R.string.usercenter_login:
                // getSupportFragmentManager().beginTransaction().replace(R.id.user_center_container, loginFragment).commit();

                loginQQorWX();


                break;
            case R.string.usercenter_purchase_history:
                getSupportFragmentManager().beginTransaction().replace(R.id.user_center_container, historyFragment).commit();
                break;
            case R.string.usercenter_help:
                getSupportFragmentManager().beginTransaction().replace(R.id.user_center_container, helpFragment).commit();
                break;
            case R.string.usercenter_location:
                getSupportFragmentManager().beginTransaction().replace(R.id.user_center_container, locationFragment).commit();
                break;
        }
        for (View view : indicatorView) {
            if (view.getId() == currentViewId) {
                view.setBackgroundResource(R.drawable.table_selected_bg);
            } else {
                view.setBackgroundResource(R.drawable.selector_channel_item);
            }
        }
    }

    public void switchToUserInfoFragment() {
        userCenterIndicatorLayout.getChildAt(1).requestFocus();
        getSupportFragmentManager().beginTransaction().replace(R.id.user_center_container, userInfoFragment).commit();
    }

    @Override
    public void onBackPressed() {
        if (userInfoFragment.isLoginFragmentShowing()) {
            userInfoFragment.hideLoginFragment();
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public void onLoginSuccess(String result) {
        callWGQueryQQUserInfo();
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
        showLoginSuccessPopup();
    }


    private void accountsCombine() {
        String api = SimpleRestClient.root_url + "/accounts/combine/";
        long timestamp = System.currentTimeMillis();
        Activator activator = Activator.getInstance(mContext);
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
        View popupLayout = LayoutInflater.from(mContext).inflate(R.layout.popup_login_success, null);
        TextView textView = (TextView) popupLayout.findViewById(R.id.login_success_msg);
        String msg = mContext.getText(R.string.login_success).toString();
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


        int width = (int) mContext.getResources().getDimension(R.dimen.login_pop_width);
        int height = (int) mContext.getResources().getDimension(R.dimen.login_pop_height);
        loginPopup = new PopupWindow(popupLayout, width, height);
        loginPopup.setFocusable(true);
        loginPopup.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.transparent));
        int xOffset = (int) mContext.getResources().getDimension(R.dimen.loginfragment_successPop_xOffset);
        int yOffset = (int) mContext.getResources().getDimension(R.dimen.loginfragment_successPop_yOffset);

        loginPopup.showAtLocation(mContentView, Gravity.CENTER, xOffset, yOffset);
    }


    public void showAccountsCombinePopup() {
        View popupLayout = LayoutInflater.from(mContext).inflate(R.layout.popup_account_combine, null);
        int width = (int) mContext.getResources().getDimension(R.dimen.login_pop_width);
        int height = (int) mContext.getResources().getDimension(R.dimen.login_pop_height);
        combineAccountPop = new PopupWindow(popupLayout, width, height);
        combineAccountPop.setFocusable(true);
        combineAccountPop.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.transparent));
        int xOffset = (int) mContext.getResources().getDimension(R.dimen.loginfragment_successPop_xOffset);
        int yOffset = (int) mContext.getResources().getDimension(R.dimen.loginfragment_successPop_yOffset);
        combineAccountPop.showAtLocation(mContentView, Gravity.CENTER, xOffset, yOffset);

        Button confirm = (Button) popupLayout.findViewById(R.id.confirm_account_combine);
        Button cancel = (Button) popupLayout.findViewById(R.id.cancel_account_combine);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accountsCombine();
                combineAccountPop.dismiss();
                ((UserCenterActivity) mContext).switchToUserInfoFragment();

            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = accountSharedPrefs.edit();
                editor.putBoolean(ACCOUNT_COMBINE, false);
                editor.apply();

                combineAccountPop.dismiss();
                ((UserCenterActivity) mContext).switchToUserInfoFragment();
            }
        });


    }


    private void saveToLocal(String authToken, String phoneNumber) {
        SimpleRestClient.access_token = authToken;
        SimpleRestClient.mobile_number = phoneNumber;

        DaisyUtils.getVodApplication(mContext).getEditor().putString(VodApplication.AUTH_TOKEN, authToken);
        DaisyUtils.getVodApplication(mContext).getEditor().putString(VodApplication.MOBILE_NUMBER, phoneNumber);
        DaisyUtils.getVodApplication(mContext).save();
        fetchFavorite();
        getHistoryByNet();
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
            DaisyUtils.getHistoryManager(mContext).addHistory(history,
                    "yes");
        else
            DaisyUtils.getHistoryManager(mContext)
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
            // DaisyUtils.getFavoriteManager(getContext())
            // .deleteFavoriteByUrl(url,"yes");
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
            DaisyUtils.getFavoriteManager(mContext).addFavorite(favorite, favorite.isnet);
        }
    }

    private boolean isFavorite(Item mItem) {
        if (mItem != null) {
            String url = mItem.item_url;
            if (url == null && mItem.pk != 0) {
                url = SimpleRestClient.sRoot_url + "/api/item/" + mItem.pk + "/";
            }
            Favorite favorite = DaisyUtils.getFavoriteManager(mContext).getFavoriteByUrl(url, "yes");
            if (favorite != null) {
                return true;
            }
        }

        return false;
    }


}
