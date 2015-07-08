package tv.ismar.daisy.ui.fragment.usercenter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import cn.ismartv.activator.Activator;
import com.google.gson.Gson;
import tv.ismar.daisy.R;
import tv.ismar.daisy.VodApplication;
import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.client.IsmartvUrlClient;
import tv.ismar.daisy.data.usercenter.AccountBalanceEntity;
import tv.ismar.daisy.data.usercenter.AccountPlayAuthEntity;
import tv.ismar.daisy.ui.adapter.AccoutPlayAuthAdapter;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by huaijie on 7/3/15.
 */
public class UserInfoFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "UserInfoFragment";

    private Context mContext;

    private TextView deviceNumber;
    private TextView balanceTextView;
    private ListView playAuthListView;
    private TextView associationText;

    private TextView phoneNumber;


    private View fragmentView;

    private LoginFragment loginFragment;

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
        associationText = (TextView) fragmentView.findViewById(R.id.association);
        associationText.setOnClickListener(this);

        loginFragment = new LoginFragment();
        getChildFragmentManager().beginTransaction().add(R.id.association_phone_layout, loginFragment).commit();
        getChildFragmentManager().beginTransaction().hide(loginFragment).commit();


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

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("timestamp", timestamp);
        params.put("sign", sign);


        new IsmartvUrlClient().doRequest(IsmartvUrlClient.Method.POST, api, params, new IsmartvUrlClient.CallBack() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "fetchAccountsPlayauths: " + result);
                AccountPlayAuthEntity accountPlayAuthEntity = new Gson().fromJson(result, AccountPlayAuthEntity.class);
                ArrayList<AccountPlayAuthEntity.PlayAuth> playAuths = new ArrayList<AccountPlayAuthEntity.PlayAuth>();
                playAuths.addAll(accountPlayAuthEntity.getPlayauth_list());
                playAuths.addAll(accountPlayAuthEntity.getSn_playauth_list());
                AccoutPlayAuthAdapter accoutPlayAuthAdapter = new AccoutPlayAuthAdapter(mContext, playAuths);
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
            case R.id.association:
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
        loginFragment.getView().requestFocus();

    }




}
