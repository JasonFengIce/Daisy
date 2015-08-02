package tv.ismar.daisy.ui.fragment.usercenter;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
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
import cn.ismartv.activator.Activator;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import tv.ismar.daisy.R;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.client.IsmartvUrlClient;
import tv.ismar.daisy.data.usercenter.AccountsOrdersEntity;
import tv.ismar.daisy.ui.widget.recycleview.widget.LinearLayoutManager;
import tv.ismar.daisy.ui.widget.recycleview.widget.RecyclerView;
import tv.ismar.daisy.utils.Util;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by huaijie on 7/3/15.
 */
public class PurchaseHistoryFragment extends Fragment {
    private static final String TAG = "PurchaseHistoryFragment";


    private Context mContext;


    private LinearLayout accountOrderListView;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mContext = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_phurchase, null);
        accountOrderListView = (LinearLayout) view.findViewById(R.id.orderlist);
//        accountOrderListView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST));
//        accountOrderListView.setItemAnimator(new DefaultItemAnimator());
//        accountOrderListView.setLayoutManager(new LinearLayoutManager(mContext));
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        fetchAccountsOrders();
    }

//    private void fetchAccountsOrders() {
//        String api = SimpleRestClient.root_url + "/accounts/orders/";
//        Activator activator = Activator.getInstance(mContext);
//
//        String timestamp = String.valueOf(System.currentTimeMillis());
//        String sign = activator.PayRsaEncode("sn=" + SimpleRestClient.sn_token + "&timestamp=" + timestamp);
//
//        HashMap<String, String> params = new HashMap<String, String>();
//        params.put("timestamp", timestamp);
//        params.put("sign", sign);
//
//
//        new IsmartvUrlClient().doRequest(IsmartvUrlClient.Method.POST, api, params, new IsmartvUrlClient.CallBack() {
//            @Override
//            public void onSuccess(String result) {
//                Log.d(TAG, "fetchAccountsOrders: " + result);
//                AccountsOrdersEntity accountsOrdersEntity = new Gson().fromJson(result, AccountsOrdersEntity.class);
//                ArrayList<AccountsOrdersEntity.OrderEntity> arrayList = new ArrayList<AccountsOrdersEntity.OrderEntity>();
//                AccountOrderAdapter accountOrderAdapter;
//                if (!TextUtils.isEmpty(SimpleRestClient.access_token) && !TextUtils.isEmpty(SimpleRestClient.mobile_number)) {
//
//
//                    for (AccountsOrdersEntity.OrderEntity entity : accountsOrdersEntity.getOrder_list()) {
//                        entity.type = "order_list";
//                        arrayList.add(entity);
//                    }
//                    for (AccountsOrdersEntity.OrderEntity entity : accountsOrdersEntity.getSn_order_list()) {
//                        entity.type = "snorder_list";
//                        arrayList.add(entity);
//                    }
//                    //arrayList.addAll(accountsOrdersEntity.getOrder_list());
//                    // arrayList.addAll(accountsOrdersEntity.getSn_order_list());
//                    accountOrderAdapter = new AccountOrderAdapter(mContext, arrayList);
//                } else {
//                    for (AccountsOrdersEntity.OrderEntity entity : accountsOrdersEntity.getSn_order_list()) {
//                        entity.type = "snorder_list";
//                        arrayList.add(entity);
//                    }
//                    accountOrderAdapter = new AccountOrderAdapter(mContext, arrayList);
//                }
//                accountOrderListView.setAdapter(accountOrderAdapter);
//            }
//
//            @Override
//            public void onFailed(Exception exception) {
//
//            }
//        });
//    }


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
                ArrayList<AccountsOrdersEntity.OrderEntity> arrayList = new ArrayList<AccountsOrdersEntity.OrderEntity>();
                HomeAdapter accountOrderAdapter;
                if (!TextUtils.isEmpty(SimpleRestClient.access_token) && !TextUtils.isEmpty(SimpleRestClient.mobile_number)) {


                    for (AccountsOrdersEntity.OrderEntity entity : accountsOrdersEntity.getOrder_list()) {
                        entity.type = "order_list";
                        arrayList.add(entity);
                    }
                    for (AccountsOrdersEntity.OrderEntity entity : accountsOrdersEntity.getSn_order_list()) {
                        entity.type = "snorder_list";
                        arrayList.add(entity);
                    }
                    //arrayList.addAll(accountsOrdersEntity.getOrder_list());
                    // arrayList.addAll(accountsOrdersEntity.getSn_order_list());
//                    accountOrderAdapter = new HomeAdapter(mContext, arrayList);
                    createHistoryListView(arrayList);

                } else {
                    for (AccountsOrdersEntity.OrderEntity entity : accountsOrdersEntity.getSn_order_list()) {
                        entity.type = "snorder_list";
                        arrayList.add(entity);
                    }

                    createHistoryListView(arrayList);
//                    accountOrderAdapter = new HomeAdapter(mContext, arrayList);
                }


//                accountOrderListView.setAdapter(accountOrderAdapter);
            }

            @Override
            public void onFailed(Exception exception) {

            }
        });
    }


    private void createHistoryListView(ArrayList<AccountsOrdersEntity.OrderEntity> orderEntities) {
        accountOrderListView.removeAllViews();

        for (int i = 0; i < orderEntities.size(); i++) {
            View convertView = LayoutInflater.from(mContext).inflate(R.layout.orderlistitem, null);

            AccountsOrdersEntity.OrderEntity item = orderEntities.get(i);

            TextView title = (TextView) convertView.findViewById(R.id.orderlistitem_title);
            TextView buydate_txt = (TextView) convertView.findViewById(R.id.orderlistitem_time);
            TextView orderlistitem_remainday = (TextView) convertView.findViewById(R.id.orderlistitem_remainday);
            TextView totalfee = (TextView) convertView.findViewById(R.id.orderlistitem_cost);
            ImageView icon = (ImageView) convertView.findViewById(R.id.orderlistitem_icon);
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
                icon.setId(0x99474723);
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

    class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.MyViewHolder> {
        private Context homeContext;
        private ArrayList<AccountsOrdersEntity.OrderEntity> mArrayList;

        public HomeAdapter(Context context, ArrayList<AccountsOrdersEntity.OrderEntity> arrayList) {
            this.homeContext = context;
            this.mArrayList = arrayList;
        }


        @Override

        public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.orderlistitem, viewGroup, false);
            MyViewHolder holder = new MyViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            AccountsOrdersEntity.OrderEntity item = mArrayList.get(position);
            String orderday = homeContext.getResources().getString(R.string.personcenter_orderlist_item_orderday);
            String remainday = homeContext.getResources().getString(R.string.personcenter_orderlist_item_remainday);
            String cost = homeContext.getResources().getString(R.string.personcenter_orderlist_item_cost);
            String paySource = homeContext.getResources().getString(R.string.personcenter_orderlist_item_paysource);
            holder.title.setText(item.getTitle());
            holder.buydate_txt.setText(String.format(orderday, item.getStart_date()));
            holder.orderlistitem_remainday.setText(String.format(remainday, remaindDay(item.getExpiry_date())));
            Log.d(TAG, "remainday: " + remaindDay(item.getExpiry_date()));
            holder.totalfee.setText(String.format(cost, item.getTotal_fee()));
            holder.orderlistitem_paychannel.setText(String.format(paySource, getValueBySource(item.getSource())));
            Picasso.with(homeContext).load(item.getThumb_url()).into(holder.icon);
            if (!TextUtils.isEmpty(item.getInfo())) {
                String account = item.getInfo().split("@")[0];
                String mergedate = item.getInfo().split("@")[1];
                SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd");
                String mergeTime = time.format(Timestamp.valueOf(mergedate));

                if (item.type.equals("order_list")) {
                    holder.purchaseExtra.setText("( " + mergeTime + "合并至视云账户" + SimpleRestClient.mobile_number + " )");
                } else if (item.type.equals("snorder_list")) {
                    holder.purchaseExtra.setText(mergeTime + "合并至视云账户" + account);
                }

                holder.purchaseExtra.setVisibility(View.VISIBLE);
                holder.mergeTxt.setVisibility(View.INVISIBLE);
            } else {
                holder.purchaseExtra.setVisibility(View.INVISIBLE);
                holder.mergeTxt.setVisibility(View.INVISIBLE);
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

        @Override
        public int getItemCount() {
            return mArrayList.size();
        }


        class MyViewHolder extends RecyclerView.ViewHolder {
            TextView purchaseExtra;
            ImageView icon;
            TextView title;
            TextView buydate_txt;
            TextView orderlistitem_remainday;
            TextView totalfee;
            TextView orderlistitem_paychannel;
            TextView mergeTxt;

            public MyViewHolder(View convertView) {
                super(convertView);
//                textView = (ImageView) itemView.findViewById(R.id.id_number);

                title = (TextView) convertView.findViewById(R.id.orderlistitem_title);
                buydate_txt = (TextView) convertView.findViewById(R.id.orderlistitem_time);
                orderlistitem_remainday = (TextView) convertView.findViewById(R.id.orderlistitem_remainday);
                totalfee = (TextView) convertView.findViewById(R.id.orderlistitem_cost);
                icon = (ImageView) convertView.findViewById(R.id.orderlistitem_icon);
                orderlistitem_paychannel = (TextView) convertView.findViewById(R.id.orderlistitem_paychannel);
                purchaseExtra = (TextView) convertView.findViewById(R.id.purchase_extra);
                mergeTxt = (TextView) convertView.findViewById(R.id.orderlistitem_merge);
            }
        }

    }


    class DividerItemDecoration extends RecyclerView.ItemDecoration {

        final int[] ATTRS = new int[]{
                android.R.attr.listDivider
        };

        public static final int HORIZONTAL_LIST = LinearLayoutManager.HORIZONTAL;

        public static final int VERTICAL_LIST = LinearLayoutManager.VERTICAL;

        private Drawable mDivider;

        private int mOrientation;

        public DividerItemDecoration(Context context, int orientation) {
            final TypedArray a = context.obtainStyledAttributes(ATTRS);
            mDivider = a.getDrawable(0);
            a.recycle();
            setOrientation(orientation);
        }

        public void setOrientation(int orientation) {
            if (orientation != HORIZONTAL_LIST && orientation != VERTICAL_LIST) {
                throw new IllegalArgumentException("invalid orientation");
            }
            mOrientation = orientation;
        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent) {
            if (mOrientation == VERTICAL_LIST) {
                drawVertical(c, parent);
            } else {
                drawHorizontal(c, parent);
            }

        }


        public void drawVertical(Canvas c, RecyclerView parent) {
            final int left = parent.getPaddingLeft();
            final int right = parent.getWidth() - parent.getPaddingRight();

            final int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);
                RecyclerView v = new RecyclerView(parent.getContext());
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                        .getLayoutParams();
                final int top = child.getBottom() + params.bottomMargin;
                final int bottom = top + mDivider.getIntrinsicHeight();
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }

        public void drawHorizontal(Canvas c, RecyclerView parent) {
            final int top = parent.getPaddingTop();
            final int bottom = parent.getHeight() - parent.getPaddingBottom();

            final int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                        .getLayoutParams();
                final int left = child.getRight() + params.rightMargin;
                final int right = left + mDivider.getIntrinsicHeight();
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }

        @Override
        public void getItemOffsets(Rect outRect, int itemPosition, RecyclerView parent) {
            if (mOrientation == VERTICAL_LIST) {
                outRect.set(0, 0, 0, mDivider.getIntrinsicHeight());
            } else {
                outRect.set(0, 0, mDivider.getIntrinsicWidth(), 0);
            }
        }

    }
}

