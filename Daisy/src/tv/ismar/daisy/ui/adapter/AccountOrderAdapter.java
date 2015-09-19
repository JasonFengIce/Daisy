package tv.ismar.daisy.ui.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import tv.ismar.daisy.R;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.data.usercenter.AccountsOrdersEntity;
import tv.ismar.daisy.utils.Util;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by huaijie on 7/3/15.
 */
public class AccountOrderAdapter extends BaseAdapter {
    private static final String TAG = "AccountOrderAdapter";

    ArrayList<AccountsOrdersEntity.OrderEntity> mList;
    Context mContext;
    ViewHolder holder;

    public AccountOrderAdapter(Context context, ArrayList<AccountsOrdersEntity.OrderEntity> list) {
        this.mList = list;
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int id) {
        return id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.orderlistitem, null);
            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.orderlistitem_title);
            holder.buydate_txt = (TextView) convertView.findViewById(R.id.orderlistitem_time);
            holder.orderlistitem_remainday = (TextView) convertView.findViewById(R.id.orderlistitem_remainday);
            holder.totalfee = (TextView) convertView.findViewById(R.id.orderlistitem_cost);
            holder.icon = (ImageView) convertView.findViewById(R.id.orderlistitem_icon);
            holder.orderlistitem_paychannel = (TextView) convertView.findViewById(R.id.orderlistitem_paychannel);
            holder.purchaseExtra = (TextView) convertView.findViewById(R.id.purchase_extra);
            holder.mergeTxt = (TextView) convertView.findViewById(R.id.orderlistitem_merge);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        AccountsOrdersEntity.OrderEntity item = mList.get(position);
        String orderday = mContext.getResources().getString(R.string.personcenter_orderlist_item_orderday);
        String remainday = mContext.getResources().getString(R.string.personcenter_orderlist_item_remainday);
        String cost = mContext.getResources().getString(R.string.personcenter_orderlist_item_cost);
        String paySource = mContext.getResources().getString(R.string.personcenter_orderlist_item_paysource);
        holder.title.setText(item.getTitle());
        holder.buydate_txt.setText(String.format(orderday, item.getStart_date()));
        holder.orderlistitem_remainday.setText(String.format(remainday, remaindDay(item.getExpiry_date())));
        Log.d(TAG, "remainday: " + remaindDay(item.getExpiry_date()));
        holder.totalfee.setText(String.format(cost, item.getTotal_fee()));
        holder.orderlistitem_paychannel.setText(String.format(paySource, getValueBySource(item.getSource())));
        Picasso.with(mContext).load(item.getThumb_url()).into(holder.icon);
        if (!TextUtils.isEmpty(item.getInfo())) {
            String account = item.getInfo().split("@")[0];
            String mergedate = item.getInfo().split("@")[1];
            SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd");
            String mergeTime = time.format(Timestamp.valueOf(mergedate));

            if (item.type.equals("order_list")) {
                holder.purchaseExtra.setText("( " + mergeTime + "合并至账户" + SimpleRestClient.mobile_number + " )");
            } else if (item.type.equals("snorder_list")) {
                holder.purchaseExtra.setText(mergeTime + "合并至账户" + account);
            }

            holder.purchaseExtra.setVisibility(View.VISIBLE);
            holder.mergeTxt.setVisibility(View.INVISIBLE);
        } else {
            holder.purchaseExtra.setVisibility(View.INVISIBLE);
            holder.mergeTxt.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }

    public static class ViewHolder {
        TextView purchaseExtra;
        ImageView icon;
        TextView title;
        TextView buydate_txt;
        TextView orderlistitem_remainday;
        TextView totalfee;
        TextView orderlistitem_paychannel;
        TextView mergeTxt;
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


//    private int remaindDay(String exprieTime) {
//        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        Date startDate = new GregorianCalendar().getTime();
//        ParsePosition pos = new ParsePosition(0);
//        Date exprietDate = formatter.parse(exprieTime, pos);
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(startDate);
//        int startDay = calendar.get(Calendar.DAY_OF_YEAR);
//        calendar.setTime(exprietDate);
//        int exprieDay = calendar.get(Calendar.DAY_OF_YEAR);
//        int remaindDay = exprieDay - startDay;
//        if (remaindDay < 0) {
//            return 0;
//        }
//        return remaindDay;
//    }

    private int remaindDay(String exprieTime) {
        try {
            return Util.daysBetween(Util.getTime(), exprieTime) + 1;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }


}
