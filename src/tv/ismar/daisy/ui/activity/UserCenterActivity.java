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
import com.google.gson.Gson;
import com.tencent.msdk.api.*;
import com.tencent.msdk.consts.CallbackFlag;
import com.tencent.msdk.consts.EPlatform;
import com.tencent.msdk.tools.Logger;
import tv.ismar.daisy.*;
import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.client.IsmartvUrlClient;
import tv.ismar.daisy.data.usercenter.AuthTokenEntity;
import tv.ismar.daisy.ui.fragment.usercenter.*;
import tv.ismar.daisy.ui.widget.LaunchHeaderLayout;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by huaijie on 7/3/15.
 */
public class UserCenterActivity extends BaseActivity implements View.OnClickListener,BaseActivity.OnLoginCallback {

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
        setIsinitMSDK(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usercenter);
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
        isFirstLogin = true;
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

    private void saveToLocal(String authToken, String phoneNumber) {
        SimpleRestClient.access_token = authToken;
        SimpleRestClient.mobile_number = phoneNumber;

        DaisyUtils.getVodApplication(UserCenterActivity.this).getEditor().putString(VodApplication.AUTH_TOKEN, authToken);
        DaisyUtils.getVodApplication(UserCenterActivity.this).getEditor().putString(VodApplication.MOBILE_NUMBER, phoneNumber);
        DaisyUtils.getVodApplication(UserCenterActivity.this).save();
        //fetchFavorite();
       // getHistoryByNet();
    }

    @Override
    public void onLoginSuccess(String result) {
        AuthTokenEntity authTokenEntity = new Gson().fromJson(result, AuthTokenEntity.class);
        Log.i("pangziinfo", "authTokenEntity.getAuth_token()==" + authTokenEntity.getAuth_token());
    }

    @Override
    public void onLoginFailed() {

    }


//    private void showLoginSuccessPopup() {
//        View popupLayout = LayoutInflater.from(UserCenterActivity.this).inflate(R.layout.popup_login_success, null);
//        TextView textView = (TextView) popupLayout.findViewById(R.id.login_success_msg);
//        String msg = mContext.getText(R.string.login_success).toString();
//        String phoneNumber = phoneNumberEdit.getText().toString();
//        textView.setText(String.format(msg, phoneNumber));
//
//        Button button = (Button) popupLayout.findViewById(R.id.login_success_btn);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                loginPopup.dismiss();
//                showAccountsCombinePopup();
//            }
//        });
//
//
//        int width = (int) mContext.getResources().getDimension(R.dimen.login_pop_width);
//        int height = (int) mContext.getResources().getDimension(R.dimen.login_pop_height);
//        loginPopup = new PopupWindow(popupLayout, width, height);
//        loginPopup.setFocusable(true);
//        loginPopup.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.transparent));
//        int xOffset = (int) mContext.getResources().getDimension(R.dimen.loginfragment_successPop_xOffset);
//        int yOffset = (int) mContext.getResources().getDimension(R.dimen.loginfragment_successPop_yOffset);
//
//        loginPopup.showAtLocation(fragmentView, Gravity.CENTER, xOffset, yOffset);
//    }
}
