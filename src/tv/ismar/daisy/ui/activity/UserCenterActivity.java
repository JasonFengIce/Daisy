package tv.ismar.daisy.ui.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import tv.ismar.daisy.BaseActivity;
import tv.ismar.daisy.R;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.ui.fragment.usercenter.*;
import tv.ismar.daisy.ui.widget.LaunchHeaderLayout;

import java.util.ArrayList;

/**
 * Created by huaijie on 7/3/15.
 */
public class UserCenterActivity extends BaseActivity implements View.OnClickListener {

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

    private SharedPreferences.OnSharedPreferenceChangeListener  changeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usercenter);

        accountPreference = getSharedPreferences("Daisy", Context.MODE_PRIVATE);
        accountPreference.registerOnSharedPreferenceChangeListener(changeListener);

        userCenterIndicatorLayout = (LinearLayout) findViewById(R.id.user_center_indicator_layout);
        storeFragment = new StoreFragment();
        userInfoFragment = new UserInfoFragment();
        loginFragment = new LoginFragment();
        historyFragment = new PurchaseHistoryFragment();
        helpFragment = new HelpFragment();
        locationFragment = new LocationFragment();

        initViews();
        createIndicatorView();


        getSupportFragmentManager().beginTransaction().add(R.id.user_center_container, storeFragment).commit();
    }


    private void initViews() {
        topView = (LaunchHeaderLayout)findViewById(R.id.top_column_layout);


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
                getSupportFragmentManager().beginTransaction().replace(R.id.user_center_container, loginFragment).commit();
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
				view.setBackgroundResource(R.drawable.daisy_btn_normal_bg);
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


}
