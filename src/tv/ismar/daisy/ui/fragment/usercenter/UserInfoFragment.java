package tv.ismar.daisy.ui.fragment.usercenter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import cn.ismartv.activator.Activator;
import com.google.gson.Gson;
import tv.ismar.daisy.R;
import tv.ismar.daisy.VodApplication;
import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.client.IsmartvUrlClient;
import tv.ismar.daisy.data.usercenter.AccountBalanceEntity;
import tv.ismar.daisy.data.usercenter.AccountPlayAuthEntity;
import tv.ismar.daisy.player.InitPlayerTool;
import tv.ismar.daisy.ui.adapter.AccountOrderAdapter;
import tv.ismar.daisy.ui.adapter.AccoutPlayAuthAdapter;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by huaijie on 7/3/15.
 */
public class UserInfoFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener {
    private static final String TAG = "UserInfoFragment";

    private Context mContext;

    private TextView deviceNumber;
    private TextView balanceTextView;
    private ListView playAuthListView;
    private Button associationText;
    private Button changeButton;

    private TextView phoneNumber;


    private View fragmentView;

    private LoginFragment loginFragment;

    private LinearLayout userInfoLayout;

    private AccoutPlayAuthAdapter accoutPlayAuthAdapter;

    private SharedPreferences sharedPreferences;

    private View phoneNumberLayout;

    private View snNumberLayout;

    private TextView associationPrompt;

    SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            String phone = sharedPreferences.getString("mobile_number", "");
            if (TextUtils.isEmpty(phone)) {
                phoneNumberLayout.setVisibility(View.VISIBLE);
                associationText.setVisibility(View.GONE);
                associationPrompt.setVisibility(View.GONE);
                phoneNumber.setText(phone);

            } else {
                phoneNumberLayout.setVisibility(View.GONE);
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
        playAuthListView = (ListView) fragmentView.findViewById(R.id.privilegelist);
        playAuthListView.setOnItemClickListener(this);
        associationText = (Button) fragmentView.findViewById(R.id.association_button);
        userInfoLayout = (LinearLayout) fragmentView.findViewById(R.id.userinfo_layout);
        changeButton = (Button) fragmentView.findViewById(R.id.change);
        associationPrompt=(TextView)fragmentView.findViewById(R.id.association_prompt);

        phoneNumberLayout = fragmentView.findViewById(R.id.phone_number_layout);
        snNumberLayout = fragmentView.findViewById(R.id.sn_number_layout);

        associationText.setOnClickListener(this);
        changeButton.setOnClickListener(this);

        loginFragment = new LoginFragment();
        getChildFragmentManager().beginTransaction().add(R.id.association_phone_layout, loginFragment).commit();
        getChildFragmentManager().beginTransaction().hide(loginFragment).commit();
        sharedPreferences = mContext.getSharedPreferences("Daisy", Context.MODE_PRIVATE);
        sharedPreferences.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);

        if (!TextUtils.isEmpty(SimpleRestClient.mobile_number)) {
            phoneNumberLayout.setVisibility(View.VISIBLE);
            associationText.setVisibility(View.GONE);
            associationPrompt.setVisibility(View.GONE);

        }

        return fragmentView;
    }


    @Override
    public void onResume() {
        super.onResume();
        fetchAccountsBalance();
        fetchAccountsPlayauths();
        initViewByLoginStatus();
    }

    private void fetchAccountsBalance() {
        String api = SimpleRestClient.root_url + "/accounts/balance/";
        new IsmartvUrlClient().doRequest(api, new IsmartvUrlClient.CallBack() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "fetchAccountBalance: " + result);
                AccountBalanceEntity accountBalanceEntity = new Gson().fromJson(result, AccountBalanceEntity.class);
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
                ArrayList<AccountPlayAuthEntity.PlayAuth> playAuths = new ArrayList<AccountPlayAuthEntity.PlayAuth>();;
                if (!TextUtils.isEmpty(SimpleRestClient.access_token) && !TextUtils.isEmpty(SimpleRestClient.mobile_number)) {
                    if (!accountPlayAuthEntity.getSn_playauth_list().isEmpty()){
                        playAuths = new ArrayList<AccountPlayAuthEntity.PlayAuth>();
                        playAuths.addAll(accountPlayAuthEntity.getSn_playauth_list());
                    }

                    playAuths.addAll(accountPlayAuthEntity.getPlayauth_list());
                    accoutPlayAuthAdapter = new AccoutPlayAuthAdapter(mContext, playAuths);
                } else {
                    accoutPlayAuthAdapter = new AccoutPlayAuthAdapter(mContext, accountPlayAuthEntity.getSn_playauth_list());
                }
                playAuthListView.setAdapter(accoutPlayAuthAdapter);
            }

            @Override
            public void onFailed(Exception exception) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.association_button:
                showAssociationPopupWindow();
                break;
            case R.id.change:
                showAssociationPopupWindow();
                break;
        }
    }

    private void initViewByLoginStatus() {
        deviceNumber.setText("SN： " + SimpleRestClient.sn_token);
        if (!DaisyUtils.getVodApplication(mContext).getPreferences().getString(VodApplication.AUTH_TOKEN, "").equals("")) {
            phoneNumber.setText("手机号：" + SimpleRestClient.mobile_number);
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
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String url = accoutPlayAuthAdapter.getList().get(position).getUrl();
        if (!TextUtils.isEmpty(url)) {
            InitPlayerTool tool = new InitPlayerTool(mContext);
            tool.initClipInfo(url, InitPlayerTool.FLAG_URL);
        }
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
}
