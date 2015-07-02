package tv.ismar.daisy.ui.adapter;

import java.util.ArrayList;

import tv.ismar.daisy.R;
import tv.ismar.daisy.models.OrderListItem;
import tv.ismar.daisy.views.AsyncImageView;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class OrderListAdapter extends BaseAdapter {
	ArrayList<OrderListItem> mList;
	Context mContext;
	ViewHolder holder;

	public OrderListAdapter(Context context, ArrayList<OrderListItem> list) {
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
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.orderlistitem, null);
			holder = new ViewHolder();
			holder.title = (TextView) convertView
					.findViewById(R.id.orderlistitem_title);
			holder.buydate_txt = (TextView) convertView
					.findViewById(R.id.orderlistitem_time);
			holder.orderlistitem_remainday = (TextView) convertView
					.findViewById(R.id.orderlistitem_remainday);
			holder.totalfee = (TextView) convertView
					.findViewById(R.id.orderlistitem_cost);
			holder.icon = (AsyncImageView) convertView
					.findViewById(R.id.orderlistitem_icon);
			holder.orderlistitem_paychannel = (TextView) convertView
					.findViewById(R.id.orderlistitem_paychannel);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		OrderListItem item = mList.get(position);
		String orderday = mContext.getResources().getString(
				R.string.personcenter_orderlist_item_orderday);
		String remainday = mContext.getResources().getString(
				R.string.personcenter_orderlist_item_remainday);
		String cost = mContext.getResources().getString(
				R.string.personcenter_orderlist_item_cost);
		String paysource = mContext.getResources().getString(
				R.string.personcenter_orderlist_item_paysource);
		holder.title.setText(item.getTitle());
		holder.buydate_txt
				.setText(String.format(orderday, item.getStart_date()));
		holder.orderlistitem_remainday.setText(String.format(remainday,
				item.getExpiry_date()));
		holder.totalfee.setText(String.format(cost, item.getTotal_fee()));
		holder.orderlistitem_paychannel.setText(String.format(paysource,
				item.getPaysource()));
		holder.icon.setUrl(item.getThumb_url());
		return convertView;
	}

	public static class ViewHolder {
		AsyncImageView icon;
		TextView title;
		TextView buydate_txt;
		TextView orderlistitem_remainday;
		TextView totalfee;
		TextView orderlistitem_paychannel;
	}
}
