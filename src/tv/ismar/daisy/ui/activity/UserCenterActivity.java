package tv.ismar.daisy.ui.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import tv.ismar.daisy.R;
import tv.ismar.daisy.ui.fragment.usercenter.*;
import tv.ismar.daisy.ui.widget.TopPanelView;

import java.util.ArrayList;

/**
 * Created by huaijie on 7/3/15.
 */
public class UserCenterActivity extends FragmentActivity implements View.OnClickListener {

    private static final int[] INDICATOR_TEXT_RES_ARRAY = {
            R.string.usercenter_store,
            R.string.usercenter_userinfo,
            R.string.usercenter_login,
            R.string.usercenter_purchase_history,
            R.string.usercenter_help
    };


    private TopPanelView mTopPanelView;
    private LinearLayout userCenterIndicatorLayout;

    private StoreFragment storeFragment;
    private UserInfoFragment userInfoFragment;
    private LoginFragment loginFragment;
    private PurchaseHistoryFragment historyFragment;
    private HelpFragment helpFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usercenter);
        userCenterIndicatorLayout = (LinearLayout) findViewById(R.id.user_center_indicator_layout);
        storeFragment = new StoreFragment();
        userInfoFragment = new UserInfoFragment();
        loginFragment = new LoginFragment();
        historyFragment = new PurchaseHistoryFragment();
        helpFragment = new HelpFragment();

        initViews();
        createIndicatorView();

        getSupportFragmentManager().beginTransaction().add(R.id.user_center_container, storeFragment).commit();
    }


    private void initViews() {
        mTopPanelView = (TopPanelView) findViewById(R.id.top_column_layout);
        mTopPanelView.setChannelName(getText(R.string.user_center).toString());
    }

    /**
     * createIndicatorView
     */
    private void createIndicatorView() {
        for (int res : INDICATOR_TEXT_RES_ARRAY) {
            RelativeLayout frameLayout = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.item_usercenter_indicator, null);
            TextView textView = (TextView) frameLayout.findViewById(R.id.usercenter_indicator_text);
            textView.setText(res);
            textView.setId(res);
            textView.setOnClickListener(this);
            userCenterIndicatorLayout.addView(frameLayout);
        }
    }


    @Override
    public void onClick(View v) {
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
        }
    }

    public void switchToUserInfoFragment() {
        userCenterIndicatorLayout.getChildAt(1).requestFocus();
        getSupportFragmentManager().beginTransaction().replace(R.id.user_center_container, userInfoFragment).commit();
    }

    public enum UserCenter {

    }
}
