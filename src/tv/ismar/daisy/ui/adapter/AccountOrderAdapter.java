package tv.ismar.daisy.ui.adapter;

import android.content.Context;
import android.text.TextUtils;
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
import tv.ismar.daisy.views.AsyncImageView;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by huaijie on 7/3/15.
 */
public class AccountOrderAdapter extends BaseAdapter {
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
        holder.totalfee.setText(String.format(cost, item.getTotal_fee()));
        holder.orderlistitem_paychannel.setText(String.format(paySource, getValueBySource(item.getSource())));
        Picasso.with(mContext).load(item.getThumb_url()).into(holder.icon);
        if (!TextUtils.isEmpty(item.getInfo())) {
            holder.purchaseExtra.setVisibility(View.VISIBLE);
            holder.purchaseExtra.setText("(" + item.getInfo() + "合并至视云账户" + SimpleRestClient.mobile_number + ")");
        } else {
            holder.purchaseExtra.setVisibility(View.GONE);
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
