package tv.ismar.daisy.ui.adapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import tv.ismar.daisy.R;
import tv.ismar.daisy.models.PrivilegeItem;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

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
			convertView.setTag(holder);
		}
		else{
			holder = (ViewHolder) convertView.getTag();
		}
		PrivilegeItem item = mList.get(position);
		holder.title.setText(item.getTitle());
		return convertView;
	}
	public static class ViewHolder {
		TextView title;
		//TextView duration;
		//Button renew;
	}
}

