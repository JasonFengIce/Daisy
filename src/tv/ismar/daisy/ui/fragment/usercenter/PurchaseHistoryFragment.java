package tv.ismar.daisy.ui.fragment.usercenter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import cn.ismartv.activator.Activator;
import com.google.gson.Gson;
import tv.ismar.daisy.R;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.client.IsmartvUrlClient;
import tv.ismar.daisy.data.usercenter.AccountsOrdersEntity;
import tv.ismar.daisy.ui.adapter.AccountOrderAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by huaijie on 7/3/15.
 */
public class PurchaseHistoryFragment extends Fragment {
    private static final String TAG = "PurchaseHistoryFragment";


    private Context mContext;


    private ListView accountOrderListView;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mContext = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_phurchase, null);
        accountOrderListView = (ListView) view.findViewById(R.id.orderlist);
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        fetchAccountsOrders();
    }

    private void fetchAccountsOrders() {
        String api = SimpleRestClient.root_url + "/accounts/orders/";
        Activator activator = Activator.getInstance(mContext);

        String timestamp = String.valueOf(System.currentTimeMillis());
        String sign = activator.PayRsaEncode("sn=" + SimpleRestClient.sn_token + "&timestamp=" + timestamp);

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("timestamp", timestamp);
        params.put("sign", sign);


        new IsmartvUrlClient().doRequest(IsmartvUrlClient.Method.POST, api, params, new IsmartvUrlClient.CallBack() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "fetchAccountsOrders: " + result);
                AccountsOrdersEntity accountsOrdersEntity = new Gson().fromJson(result, AccountsOrdersEntity.class);

                ArrayList<AccountsOrdersEntity.OrderEntity> orderEntities = new ArrayList<AccountsOrdersEntity.OrderEntity>();
                orderEntities.addAll(accountsOrdersEntity.getOrder_list());
                orderEntities.addAll(accountsOrdersEntity.getSn_order_list());

                AccountOrderAdapter accountOrderAdapter = new AccountOrderAdapter(mContext, orderEntities);
                accountOrderListView.setAdapter(accountOrderAdapter);

            }

            @Override
            public void onFailed(Exception exception) {

            }
        });
    }
}

