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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import cn.ismartv.activator.IsmartvActivator;
import tv.ismar.daisy.R;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.client.IsmartvUrlClient;
import tv.ismar.daisy.data.usercenter.AccountsOrdersEntity;
import tv.ismar.daisy.utils.Util;

/**
 * Created by huaijie on 7/3/15.
 */
public class PurchaseHistoryFragment extends Fragment {
    private static final String TAG = "PurchaseHistoryFragment";


    private Context mContext;


    private LinearLayout accountOrderListView;
    private AccountsOrdersEntity accountsOrdersEntity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mContext = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_phurchase, null);
        accountOrderListView = (LinearLayout) view.findViewById(R.id.orderlist);
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        if (accountsOrdersEntity == null)
            fetchAccountsOrders();
    }


    private void fetchAccountsOrders() {
        String api = SimpleRestClient.root_url + "/accounts/orders/";
        IsmartvActivator activator = IsmartvActivator.getInstance(mContext);

        String timestamp = String.valueOf(System.currentTimeMillis());
        String sign = activator.PayRsaEncode("sn=" + SimpleRestClient.sn_token + "&timestamp=" + timestamp);

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("timestamp", timestamp);
        params.put("sign", sign);


        new IsmartvUrlClient().doRequest(IsmartvUrlClient.Method.POST, api, params, new IsmartvUrlClient.CallBack() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "fetchAccountsOrders: " + result);
                accountsOrdersEntity = new Gson().fromJson(result, AccountsOrdersEntity.class);
                ArrayList<AccountsOrdersEntity.OrderEntity> arrayList = new ArrayList<AccountsOrdersEntity.OrderEntity>();
                if (!TextUtils.isEmpty(SimpleRestClient.access_token) && !TextUtils.isEmpty(SimpleRestClient.mobile_number)) {


                    for (AccountsOrdersEntity.OrderEntity entity : accountsOrdersEntity.getOrder_list()) {
                        entity.type = "order_list";
                        arrayList.add(entity);
                    }
                    for (AccountsOrdersEntity.OrderEntity entity : accountsOrdersEntity.getSn_order_list()) {
                        entity.type = "snorder_list";
                        arrayList.add(entity);
                    }
                    createHistoryListView(arrayList);

                } else {
                    for (AccountsOrdersEntity.OrderEntity entity : accountsOrdersEntity.getSn_order_list()) {
                        entity.type = "snorder_list";
                        arrayList.add(entity);
                    }

                    createHistoryListView(arrayList);
                }


            }

            @Override
            public void onFailed(Exception exception) {

            }
        });
    }


    private void createHistoryListView(ArrayList<AccountsOrdersEntity.OrderEntity> orderEntities) {
        accountOrderListView.removeAllViews();

        for (int i = 1; i < orderEntities.size(); i++) {
            View convertView = LayoutInflater.from(mContext).inflate(R.layout.orderlistitem, null);

            AccountsOrdersEntity.OrderEntity item = orderEntities.get(i);

            TextView title = (TextView) convertView.findViewById(R.id.orderlistitem_title);
            TextView buydate_txt = (TextView) convertView.findViewById(R.id.orderlistitem_time);
            TextView orderlistitem_remainday = (TextView) convertView.findViewById(R.id.orderlistitem_remainday);
            TextView totalfee = (TextView) convertView.findViewById(R.id.orderlistitem_cost);
            tv.ismar.daisy.views.LabelImageView icon = (tv.ismar.daisy.views.LabelImageView) convertView.findViewById(R.id.orderlistitem_icon);
            TextView orderlistitem_paychannel = (TextView) convertView.findViewById(R.id.orderlistitem_paychannel);
            TextView purchaseExtra = (TextView) convertView.findViewById(R.id.purchase_extra);
            TextView mergeTxt = (TextView) convertView.findViewById(R.id.orderlistitem_merge);


            String orderday = mContext.getResources().getString(R.string.personcenter_orderlist_item_orderday);
            String remainday = mContext.getResources().getString(R.string.personcenter_orderlist_item_remainday);
            String cost = mContext.getResources().getString(R.string.personcenter_orderlist_item_cost);
            String paySource = mContext.getResources().getString(R.string.personcenter_orderlist_item_paysource);
            title.setText(item.getTitle());
            buydate_txt.setText(String.format(orderday, item.getStart_date()));
            orderlistitem_remainday.setText(String.format(remainday, remaindDay(item.getExpiry_date())));
            Log.d(TAG, "remainday: " + remaindDay(item.getExpiry_date()));
            totalfee.setText(String.format(cost, item.getTotal_fee()));
            orderlistitem_paychannel.setText(String.format(paySource, getValueBySource(item.getSource())));
            Picasso.with(mContext).load(item.getThumb_url()).into(icon);
            if (!TextUtils.isEmpty(item.getInfo())) {
                String account = item.getInfo().split("@")[0];
                String mergedate = item.getInfo().split("@")[1];
                SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd");
                String mergeTime = time.format(Timestamp.valueOf(mergedate));

                if (item.type.equals("order_list")) {
                    purchaseExtra.setText("( " + mergeTime + "合并至视云账户" + SimpleRestClient.mobile_number + " )");
                } else if (item.type.equals("snorder_list")) {
                    purchaseExtra.setText(mergeTime + "合并至视云账户" + account);
                }

                purchaseExtra.setVisibility(View.VISIBLE);
                mergeTxt.setVisibility(View.INVISIBLE);
            } else {
                purchaseExtra.setVisibility(View.INVISIBLE);
                mergeTxt.setVisibility(View.INVISIBLE);
            }

            accountOrderListView.addView(convertView);
            if (i == 0) {
                icon.setId(R.id.purchase_history_list_first_id);
                icon.setNextFocusUpId(icon.getId());
            }

            if (i != orderEntities.size() - 1) {
                ImageView imageView = new ImageView(mContext);
                imageView.setBackgroundResource(R.color.history_divider);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
                imageView.setLayoutParams(layoutParams);
                accountOrderListView.addView(imageView);
            }
        }


    }


    private String getValueBySource(String source) {
        if (source.equals("weixin")) {
            return "微信";
        } else if (source.equals("alipay")) {
            return "支付宝";
        } else if (source.equals("balance")) {
            return "余额";
        } else if (source.equals("card")) {
            return "卡";
        } else {
            return source;
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


}

