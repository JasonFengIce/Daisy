package tv.ismar.daisy.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import tv.ismar.daisy.R;
import tv.ismar.daisy.models.PrivilegeItem;

import java.util.ArrayList;

public class PrivilegeAdapter extends BaseAdapter {
    ArrayList<PrivilegeItem> mList;
    Context mContext;
    ViewHolder holder;
    public PrivilegeAdapter(Context context,ArrayList<PrivilegeItem> list){
    	this.mList = list;
    	this.mContext = context;
    }
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mList.get(position);
	}

	@Override
	public long getItemId(int id) {
		// TODO Auto-generated method stub
		return id;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if(convertView==null){
			convertView = LayoutInflater.from(mContext).inflate(R.layout.privilege_listview_item,null);
			holder = new ViewHolder();
			holder.title = (TextView) convertView.findViewById(R.id.title_txt);
			holder.buydate_txt = (TextView)convertView.findViewById(R.id.buydate_txt);
			convertView.setTag(holder);
		}
		else{
			holder = (ViewHolder) convertView.getTag();
		}
		PrivilegeItem item = mList.get(position);
		holder.title.setText(item.getTitle());
		holder.buydate_txt.setText(item.getExceeddate());
		return convertView;
	}
	public static class ViewHolder {
		TextView title;
		TextView buydate_txt;
		//TextView duration;
		//Button renew;
	}
}

