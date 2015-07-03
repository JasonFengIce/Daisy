package tv.ismar.daisy.ui.fragment.usercenter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import cn.ismartv.activator.Activator;
import com.google.gson.Gson;
import tv.ismar.daisy.R;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.client.IsmartvUrlClient;
import tv.ismar.daisy.data.usercenter.AccountBalanceEntity;
import tv.ismar.daisy.data.usercenter.AccountPlayAuthEntity;
import tv.ismar.daisy.ui.adapter.AccoutPlayAuthAdapter;
import tv.ismar.daisy.ui.adapter.PrivilegeAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by huaijie on 7/3/15.
 */
public class UserInfoFragment extends Fragment {
    private static final String TAG = "UserInfoFragment";

    private Context mContext;


    private TextView balanceTextView;

    private ListView playAuthListView;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mContext = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_userinfo, null);
        balanceTextView = (TextView) view.findViewById(R.id.remain_money_value);
        playAuthListView = (ListView) view.findViewById(R.id.privilegelist);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchAccountsBalance();
        fetchAccountsPlayauths();
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

}
