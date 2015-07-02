package tv.ismar.daisy.ui.adapter;

import java.util.ArrayList;

import tv.ismar.daisy.R;
import tv.ismar.daisy.models.Item;
import tv.ismar.daisy.views.AsyncImageView;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class PackageGridlistAdapter extends BaseAdapter {
	ArrayList<Item> mList;
	Context mContext;
	ViewHolder holder;

	public PackageGridlistAdapter(Context context, ArrayList<Item> list) {
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
					R.layout.package_gridlist_item, null);
			holder = new ViewHolder();
			holder.title = (TextView) convertView
					.findViewById(R.id.package_grid_title);
			holder.icon = (AsyncImageView) convertView
					.findViewById(R.id.package_grid_image);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		Item item = mList.get(position);
		holder.title.setText(item.title);
		holder.icon.setUrl(item.poster_url);
		return convertView;
	}

	public static class ViewHolder {
		AsyncImageView icon;
		TextView title;
	}
}
