package tv.ismar.daisy.adapter;

import java.util.List;
import tv.ismar.daisy.R;
import tv.ismar.daisy.models.Item;
import tv.ismar.daisy.player.InitPlayerTool;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

public class DaramAdapter extends BaseAdapter {
	Context mContext;
	private List<Item> subitemlist;
	private int sourceid;
	private LayoutInflater mLayoutInflater;
	public DaramAdapter(Context context, List<Item> subitemlist, int sourceid) {
		this.mContext = context;
		this.subitemlist = subitemlist;
		this.sourceid = sourceid;
		this.mLayoutInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return subitemlist.size();
	}

	@Override
	public Item getItem(int position) {
		return subitemlist.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	Item subitem;
	ViewHolder holder;
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		subitem = getItem(position);
		if(convertView==null){
			holder = new ViewHolder();
			convertView = mLayoutInflater.inflate(sourceid, null);
			holder.btnCount = (Button) convertView.findViewById(R.id.btn_count);
			convertView.setTag(holder);
		}
		else{
			holder = (ViewHolder) convertView.getTag();
		}
				
		holder.btnCount.setText(String.valueOf(subitem.position + 1));
		holder.btnCount.setTag(String.valueOf(position));
		holder.btnCount.setOnClickListener(new OnClickListener() {	
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
			int position = Integer.parseInt((String) v.getTag());
			subitem = getItem(position);
			try {
				InitPlayerTool tool = new InitPlayerTool(v.getContext());
				tool.initClipInfo(subitem.url, InitPlayerTool.FLAG_URL);
			} catch (Exception e) {
				e.printStackTrace();
			}
			}
		});

		return convertView;
	}

	public static class ViewHolder {
		Button btnCount;
	}
	
	public Button testbtn;
}
