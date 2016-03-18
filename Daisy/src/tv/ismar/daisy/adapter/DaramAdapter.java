package tv.ismar.daisy.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnHoverListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import tv.ismar.daisy.R;
import tv.ismar.daisy.models.Item;
import tv.ismar.daisy.player.InitPlayerTool;
import tv.ismar.daisy.views.PaymentDialog;

import java.util.List;

public class DaramAdapter extends BaseAdapter implements OnHoverListener,
		OnFocusChangeListener {
	Context mContext;
	private List<Item> subitemlist;
	private int sourceid;
	private LayoutInflater mLayoutInflater;
	private Item dramaItem;
	public TextView mTvDramaType;
    private InitPlayerTool tool;
	public DaramAdapter(Context context, List<Item> subitemlist,
			Item dramaitem, int sourceid) {
		this.mContext = context;
		this.subitemlist = subitemlist;
		this.sourceid = sourceid;
		this.dramaItem = dramaitem;
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
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mLayoutInflater.inflate(sourceid, null);
			holder.btnCount = (Button) convertView.findViewById(R.id.btn_count);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		if (subitem.remainDay > 0) {
			holder.btnCount
					.setBackgroundResource(R.drawable.daram_grid_payed_selector);
		} else {
			holder.btnCount
					.setBackgroundResource(R.drawable.daram_grid_selector);
		}
		holder.btnCount.setText(String.valueOf(subitem.position + 1));
		holder.btnCount.setTag(String.valueOf(position));
		holder.btnCount.setOnFocusChangeListener(this);
		//holder.btnCount.setOnHoverListener(this);
		holder.btnCount.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int position = Integer.parseInt((String) v.getTag());
				subitem = getItem(position);
				if (dramaItem.expense != null && subitem.remainDay <= 0) {
					PaymentDialog dialog = new PaymentDialog(mContext,
							R.style.PaymentDialog, innerordercheckListener);
					subitem.model_name = "subitem";
					subitem.expense = dramaItem.expense;
					dialog.setItem(subitem);
					dialog.show();
				} else {
					try {
						if(tool != null)
							tool.removeAsycCallback();
						else {
							tool = new InitPlayerTool(v.getContext());	
						}
						tool.fromPage = dramaItem.fromPage;
						tool.initClipInfo(subitem.url, InitPlayerTool.FLAG_URL);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});

		return convertView;
	}

	public static class ViewHolder {
		Button btnCount;
	}

	public Button testbtn;

	@Override
	public boolean onHover(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		int what = event.getAction();
		switch (what) {
		case MotionEvent.ACTION_HOVER_ENTER:
		case MotionEvent.ACTION_HOVER_MOVE:
			// v.setBackgroundResource(R.drawable.daram_grid_selector);
			// int position = Integer.parseInt((String) v.getTag());
			// subitem = getItem(position);

//			int position = Integer.parseInt((String) v.getTag());
//			subitem = getItem(position);
//			if (dramaItem.expense != null && subitem.remainDay <= 0) {
//				v.setBackgroundResource(R.drawable.daram_grid_selector);
//			} else {
//				if (dramaItem.expense != null && subitem.remainDay > 0)
//					v.setBackgroundResource(R.drawable.daram_grid_payed_selector);
//				else {
//					v.setBackgroundResource(R.drawable.daram_grid_selector);
//				}
//			}
//			mTvDramaType.setText(subitem.title);
//			v.requestFocusFromTouch();
			break;
		}
		return false;
	}

	@Override
	public void onFocusChange(View v, boolean hasfocus) {
		// TODO Auto-generated method stub
		int position = Integer.parseInt((String) v.getTag());
		subitem = getItem(position);
		if (hasfocus) {			
//			if (dramaItem.expense != null && subitem.remainDay <= 0) {
//				v.setBackgroundResource(R.drawable.vod_detail_series_episode_focus);
//			} else {
//				if (dramaItem.expense != null && subitem.remainDay > 0)
//					v.setBackgroundResource(R.drawable.vod_detail_series_episode_payed_focus);
//				else {
//					v.setBackgroundResource(R.drawable.vod_detail_series_episode_focus);
//				}
//			}
			// 分类
			mTvDramaType.setText(subitem.title);
		}
//		 else{
//			 if(dramaItem.expense != null && subitem.remainDay <= 0)
//		        v.setBackgroundResource(R.drawable.vod_detail_series_episode_backgroud);
//			 else{
//				 if (dramaItem.expense != null && subitem.remainDay > 0){
//					 v.setBackgroundResource(R.drawable.vod_detail_series_episode_payed_backgroud);
//				 }
//				 else{
//					 v.setBackgroundResource(R.drawable.vod_detail_series_episode_backgroud);
//				 }
//			 }
//		 }
	}

	private PaymentDialog.OrderResultListener innerordercheckListener = new PaymentDialog.OrderResultListener() {

		@Override
		public void payResult(boolean result) {
			if(result){
			subitem.remainDay = subitem.expense.duration;
			DaramAdapter.this.notifyDataSetChanged();
			}
		}
	};
	
	public void removeAsycCallback(){
		if(tool != null)
			tool.removeAsycCallback();
	}
}
