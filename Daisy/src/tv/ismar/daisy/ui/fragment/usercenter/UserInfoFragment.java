package tv.ismar.daisy.ui.fragment.usercenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import cn.ismartv.activator.Activator;
import com.google.gson.Gson;
import org.w3c.dom.Text;
import tv.ismar.daisy.BaseActivity;
import tv.ismar.daisy.R;
import tv.ismar.daisy.VodApplication;
import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.client.IsmartvUrlClient;
import tv.ismar.daisy.data.usercenter.AccountBalanceEntity;
import tv.ismar.daisy.data.usercenter.AccountPlayAuthEntity;
import tv.ismar.daisy.models.Item;
import tv.ismar.daisy.player.InitPlayerTool;
import tv.ismar.daisy.ui.activity.UserCenterActivity;
import tv.ismar.daisy.ui.adapter.AccoutPlayAuthAdapter;
import tv.ismar.daisy.utils.Util;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by huaijie on 7/3/15.
 */
public class UserInfoFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "UserInfoFragment";

    private Context mContext;
    private AccoutPlayAuthAdapter accoutPlayAuthAdapter;
    private SharedPreferences sharedPreferences;

    private TextView deviceNumber;
    private TextView balanceTextView;
    private TextView deviceNameTextView;

    private LinearLayout playAuthListView;
    private Button associationText;
    private Button changeButton;
    private TextView phoneNumber;
    private View fragmentView;
    private LoginFragment loginFragment;
    private LinearLayout userInfoLayout;
    private View phoneNumberLayout;
    private View snNumberLayout;
    private TextView associationPrompt;
    private AccountBalanceEntity accountBalanceEntity;

    private boolean isCombined;

    SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            String phone = sharedPreferences.getString("mobile_number", "");
            if (TextUtils.isEmpty(phone)) {
                phoneNumberLayout.setVisibility(View.VISIBLE);
                changeButton.setEnabled(true);
                phoneNumber.setText(phone);


            } else {
                changeButton.setEnabled(false);
                phoneNumberLayout.setVisibility(View.GONE);
                changeButton.setFocusable(false);
            }
        }
    };


    private SharedPreferences.OnSharedPreferenceChangeListener accountSharedPrefsListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            isCombined = sharedPreferences.getBoolean(LoginFragment.ACCOUNT_COMBINE, false);
            if (isCombined) {
                associationText.setVisibility(View.GONE);
                associationPrompt.setVisibility(View.GONE);

            } else {
                associationText.setVisibility(View.VISIBLE);
                associationPrompt.setVisibility(View.VISIBLE);

            }
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mContext = activity;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_userinfo, null);
        phoneNumber = (TextView) fragmentView.findViewById(R.id.phone_number);
        deviceNumber = (TextView) fragmentView.findViewById(R.id.device_number);
        balanceTextView = (TextView) fragmentView.findViewById(R.id.remain_money_value);
        deviceNameTextView = (TextView) fragmentView.findViewById(R.id.device_name);
        playAuthListView = (LinearLayout) fragmentView.findViewById(R.id.privilegelist);
        associationText = (Button) fragmentView.findViewById(R.id.association_button);
        userInfoLayout = (LinearLayout) fragmentView.findViewById(R.id.userinfo_layout);
        changeButton = (Button) fragmentView.findViewById(R.id.change);
        changeButton.setNextFocusUpId(changeButton.getId());

        associationPrompt = (TextView) fragmentView.findViewById(R.id.association_prompt);

        phoneNumberLayout = fragmentView.findViewById(R.id.phone_number_layout);
        snNumberLayout = fragmentView.findViewById(R.id.sn_number_layout);

        deviceNameTextView.setText(Build.MODEL);

        associationText.setOnClickListener(this);
        changeButton.setOnClickListener(this);

        loginFragment = new LoginFragment();
        getChildFragmentManager().beginTransaction().add(R.id.association_phone_layout, loginFragment).commit();
        getChildFragmentManager().beginTransaction().hide(loginFragment).commit();

        sharedPreferences = mContext.getSharedPreferences("Daisy", Context.MODE_PRIVATE);
        sharedPreferences.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);

        SharedPreferences accountPrefs = mContext.getSharedPreferences(LoginFragment.ACCOUNT_SHARED_PREFS, Context.MODE_PRIVATE);
        isCombined = accountPrefs.getBoolean(LoginFragment.ACCOUNT_COMBINE, false);
        accountPrefs.registerOnSharedPreferenceChangeListener(accountSharedPrefsListener);


        if (TextUtils.isEmpty(SimpleRestClient.mobile_number)) {
            phoneNumberLayout.setVisibility(View.GONE);
            changeButton.setFocusable(false);
        } else {
            phoneNumberLayout.setVisibility(View.VISIBLE);
            changeButton.setFocusable(true);
            changeButton.setNextFocusRightId(associationText.getId());
            associationText.setNextFocusLeftId(changeButton.getId());
        }

        mSimpleRestClient = new SimpleRestClient();


        fragmentView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (!TextUtils.isEmpty(SimpleRestClient.mobile_number)) {
                        changeButton.requestFocus();
                    } else {
                        associationText.requestFocus();
                    }

                }
            }
        });
        return fragmentView;
    }

    public void changge() {


        playAuthListView.setFocusable(false);
        associationText.setFocusable(false);
        if (!TextUtils.isEmpty(SimpleRestClient.mobile_number)) {
            Log.i("qihuanzhanghu", "phoneNumberLayout VISIBLE");
            phoneNumberLayout.setVisibility(View.VISIBLE);
        }

        if (isCombined) {
            associationText.setVisibility(View.GONE);
            associationPrompt.setVisibility(View.GONE);
        }

        fetchAccountsBalance();
        fetchAccountsPlayauths();
        initViewByLoginStatus();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(accountBalanceEntity == null){
        if (!TextUtils.isEmpty(SimpleRestClient.mobile_number)) {
            Log.i("qihuanzhanghu", "phoneNumberLayout VISIBLE");
            phoneNumberLayout.setVisibility(View.VISIBLE);
        }

        if (isCombined) {
            associationText.setVisibility(View.GONE);
            associationPrompt.setVisibility(View.GONE);
        }

        fetchAccountsBalance();
        fetchAccountsPlayauths();
        initViewByLoginStatus();
        }
    }

    private void fetchAccountsBalance() {
        String api = SimpleRestClient.root_url + "/accounts/balance/";
        new IsmartvUrlClient().doRequest(api, new IsmartvUrlClient.CallBack() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "fetchAccountBalance: " + result);
                accountBalanceEntity = new Gson().fromJson(result, AccountBalanceEntity.class);
                balanceTextView.setText(String.valueOf(accountBalanceEntity.getBalance() + accountBalanceEntity.getSn_balance()));
            }

            @Override
            public void onFailed(Exception exception) {

            }
        });
    }

    private void fetchAccountsPlayauths() {
        String api = SimpleRestClient.root_url + "/accounts/playauths/";

        String timestamp = String.valueOf(System.currentTimeMillis());
        Activator activator = Activator.getInstance(mContext);
        String sign = activator.PayRsaEncode("sn=" + SimpleRestClient.sn_token + "&timestamp=" + timestamp);

        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("timestamp", timestamp);
        params.put("sign", sign);


        new IsmartvUrlClient().doRequest(IsmartvUrlClient.Method.POST, api, params, new IsmartvUrlClient.CallBack() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "fetchAccountsPlayauths: " + result);
                AccountPlayAuthEntity accountPlayAuthEntity = new Gson().fromJson(result, AccountPlayAuthEntity.class);
                ArrayList<AccountPlayAuthEntity.PlayAuth> playAuths = new ArrayList<AccountPlayAuthEntity.PlayAuth>();
                if (!TextUtils.isEmpty(SimpleRestClient.access_token) && !TextUtils.isEmpty(SimpleRestClient.mobile_number)) {
                    if (!accountPlayAuthEntity.getSn_playauth_list().isEmpty()) {
                        playAuths = new ArrayList<AccountPlayAuthEntity.PlayAuth>();
                        playAuths.addAll(accountPlayAuthEntity.getSn_playauth_list());
                    }

                    playAuths.addAll(accountPlayAuthEntity.getPlayauth_list());
//                    accoutPlayAuthAdapter = new AccoutPlayAuthAdapter(mContext, playAuths);
                    createPlayAuthListView(playAuths);
                } else {
//                    accoutPlayAuthAdapter = new AccoutPlayAuthAdapter(mContext, accountPlayAuthEntity.getSn_playauth_list());
                    createPlayAuthListView(accountPlayAuthEntity.getSn_playauth_list());
                }
//                playAuthListView.setAdapter(accoutPlayAuthAdapter);
            }

            @Override
            public void onFailed(Exception exception) {

            }
        });
    }

    private View.OnFocusChangeListener playAuthFocusListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            TextView titleTextView = (TextView) v.findViewById(R.id.title_txt);
            TextView remaindDay = (TextView) v.findViewById(R.id.buydate_txt);
            if (hasFocus) {
                titleTextView.setTextColor(mContext.getResources().getColor(R.color.location_text_focus));
                titleTextView.setTextSize(mContext.getResources().getDimension(R.dimen.userinfo_playauth_item_focus_textsize));
                remaindDay.setTextColor(mContext.getResources().getColor(R.color.location_text_focus));
                remaindDay.setTextSize(mContext.getResources().getDimension(R.dimen.userinfo_playauth_item_focus_textsize));
            } else {
                titleTextView.setTextColor(mContext.getResources().getColor(R.color.white));
                titleTextView.setTextSize(mContext.getResources().getDimension(R.dimen.userinfo_playauth_item_normal_textsize));
                remaindDay.setTextColor(mContext.getResources().getColor(R.color.white));
                remaindDay.setTextSize(mContext.getResources().getDimension(R.dimen.userinfo_playauth_item_normal_textsize));
            }

        }
    };

    private View.OnClickListener playAuthClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String url = ((AccountPlayAuthEntity.PlayAuth) v.getTag()).getUrl();
            if (!TextUtils.isEmpty(url)) {
                InitPlayerTool tool = new InitPlayerTool(mContext);
                tool.initClipInfo(url, InitPlayerTool.FLAG_URL);
            }
        }
    };

    private void createPlayAuthListView(ArrayList<AccountPlayAuthEntity.PlayAuth> playAuths) {
        if (playAuths.size() == 0) {
            playAuthListView.setFocusable(false);
        } else {

            playAuthListView.removeAllViews();
            String remainday = mContext.getResources().getString(R.string.personcenter_orderlist_item_remainday);
            for (int i = 0; i < playAuths.size(); i++) {
                View convertView = LayoutInflater.from(mContext).inflate(R.layout.privilege_listview_item, null);
                TextView title = (TextView) convertView.findViewById(R.id.title_txt);
                TextView buydate_txt = (TextView) convertView.findViewById(R.id.buydate_txt);
                convertView.setTag(playAuths.get(i));
                buydate_txt.setText(String.format(remainday, remaindDay(playAuths.get(i).getExpiry_date())));
                title.setText(playAuths.get(i).getTitle());
                convertView.setOnFocusChangeListener(playAuthFocusListener);
                convertView.setOnClickListener(playAuthClickListener);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                if (i == 0) {
                    convertView.setId(R.id.userinfo_palyauth_list_first_id);
                    convertView.setNextFocusUpId(changeButton.getId());
                    changeButton.setNextFocusDownId(convertView.getId());
                }

                if (i != 0) {
                    layoutParams.setMargins(0, 51, 0, 0);

                }
                if (i == playAuths.size() - 1) {
                    convertView.setId(R.id.userinfo_palyauth_list_last_id);
                    convertView.setNextFocusDownId(convertView.getId());
                }
                convertView.setLayoutParams(layoutParams);
                playAuthListView.addView(convertView);

            }
        }
    }


    private int remaindDay(String exprieTime) {
        try {
            return Util.daysBetween(Util.getTime(), exprieTime) + 1;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.association_button:
                if (!SimpleRestClient.isLogin()) {
                    ((BaseActivity) getActivity()).loginQQorWX();
                } else {
                    loginFragment.showAccountsCombinePopup();
                }

                break;
            case R.id.change:
                // showAssociationPopupWindow();
                playAuthListView.setFocusable(false);
                associationText.setFocusable(false);
                ((UserCenterActivity) getActivity()).setAccountListener(new UserCenterActivity.OnLoginByChangeCallback() {

                    @Override
                    public void onLoginSuccess() {
                        Log.i("qihuanzhanghu", "onLoginSuccess");
                        playAuthListView.setFocusable(true);
                        associationText.setFocusable(true);
                        onResume();
                    }
                });
                ((BaseActivity) getActivity()).changaccount();
                break;
        }
    }

    private void initViewByLoginStatus() {
        deviceNumber.setText(SimpleRestClient.sn_token);
        Log.i("qihuanzhanghu", "mobile_number==" + SimpleRestClient.mobile_number);
        if (!DaisyUtils.getVodApplication(mContext).getPreferences().getString(VodApplication.AUTH_TOKEN, "").equals("")) {
            Log.i("qihuanzhanghu", "mobile_number mobile_number");
            phoneNumber.setText(SimpleRestClient.mobile_number);
        }
    }


    private void showAssociationPopupWindow() {
        loginFragment.setBackground(true);
        getChildFragmentManager().beginTransaction().show(loginFragment).commit();
        playAuthListView.setFocusable(false);
        associationText.setFocusable(false);

        loginFragment.getView().requestFocus();
        loginFragment.setLoginCallback(new LoginFragment.OnLoginCallback() {
            @Override
            public void onLoginSuccess() {
                getChildFragmentManager().beginTransaction().hide(loginFragment).commit();
                playAuthListView.setFocusable(true);
                associationText.setFocusable(true);
                onResume();
            }
        });
    }


    public boolean isLoginFragmentShowing() {
        if (loginFragment != null && loginFragment.isVisible()) {
            return true;
        } else {
            return false;
        }
    }

    public void hideLoginFragment() {
        associationText.setFocusable(true);
        playAuthListView.setFocusable(true);
        getChildFragmentManager().beginTransaction().hide(loginFragment).commit();
    }


    private PopupWindow loginPopup;
    private PopupWindow combineAccountPop;
    private SimpleRestClient mSimpleRestClient;
    private Item[] mHistoriesByNet;

}
