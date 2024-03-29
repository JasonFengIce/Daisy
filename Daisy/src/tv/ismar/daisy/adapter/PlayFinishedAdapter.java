package tv.ismar.daisy.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import tv.ismar.daisy.R;
import tv.ismar.daisy.core.FinshedImageService;
import tv.ismar.daisy.models.Item;
import tv.ismar.daisy.views.RotateTextView;

public class PlayFinishedAdapter extends BaseAdapter {
	Context mContext;
	private Item[] listItem;
	private Item[] listItemSort;
	private int sourceid;
	private LayoutInflater mLayoutInflater;

	public PlayFinishedAdapter(Context context, Item[] items, int sourceid) {
		this.mContext = context;	
		this.listItemSort = items;
		this.sourceid = sourceid;
		this.mLayoutInflater = LayoutInflater.from(context);
		sortItem();
	}

	private void sortItem() {
		if (null == listItemSort){
			return;
		}
		if(listItemSort.length<=9) {
			listItem = listItemSort;
			return;
		}
		listItem = new Item[9];
		for (int i = 0; i < 9; i++) {
			listItem[i] = listItemSort[i];
		}
	}

	@Override
	public int getCount() {
		return listItem.length;
	}

	@Override
	public Item getItem(int position) {
		return listItem[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	Item item;
	ViewHolder holder;

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		item = getItem(position);
		holder = new ViewHolder();
		convertView = mLayoutInflater.inflate(sourceid, null);
		holder.imageView = (ImageView) convertView.findViewById(R.id.itemImage);
		holder.tvItemText = (TextView) convertView.findViewById(R.id.itemText);
		holder.imageLabel = (ImageView) convertView.findViewById(R.id.iv_label);
		holder.ItemBeanScore = (TextView) convertView.findViewById(R.id.ItemBeanScore);
		holder.expense_txt = (RotateTextView) convertView.findViewById(R.id.expense_txt);
		holder.expense_txt.setDegrees(315);
		holder.tvItemText.setText(item.title);
		showImage(holder.imageView, holder.imageLabel, item);
		if(item!=null){
			if(item.expense!=null){
				if(item.expense.cptitle!=null){
					holder.expense_txt.setText(item.expense.cptitle);
					holder.expense_txt.setVisibility(View.VISIBLE);
					if(item.expense.pay_type==1){
						holder.expense_txt.setBackgroundResource(R.drawable.list_single_buy);
					}else if((item.expense.cpname).startsWith("ismar")){
						holder.expense_txt.setBackgroundResource(R.drawable.list_ismar);
					}else if("iqiyi".equals(item.expense.cpname)){
						holder.expense_txt.setBackgroundResource(R.drawable.list_lizhi);
					}
				}
				if(item.bean_score>0){
					holder.ItemBeanScore.setVisibility(View.VISIBLE);
					holder.ItemBeanScore.setText(item.bean_score+"");
				}
			}
		}
		return convertView;
	}

	private void showImage(ImageView imageView, ImageView imageLabel, Item item) {
		LoadImageTask task = new LoadImageTask();
		// 参数传给了doInBackground
		task.execute(imageView, imageLabel, item);
	}

	private final class LoadImageTask extends AsyncTask<Object, Integer, Object[]> {
		// 主图片
		ImageView imageView;
		// 高清标签
		ImageView imageLabel;

		@Override
		protected Object[] doInBackground(Object... params) {// 耗时操作,运行在子线程
			imageView = (ImageView) params[0];
			imageLabel = (ImageView) params[1];
			try {
				return FinshedImageService.getImage((Item) params[2],mContext);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Object[] object) {
			try {
				imageView.setImageBitmap((Bitmap) object[0]);// 显示照片
				if (null == object[1]) {
					imageLabel.setVisibility(View.GONE);
				} else {
					imageLabel.setBackgroundResource((Integer) object[1]);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static class ViewHolder {
		ImageView imageView;
		TextView tvItemText;
		ImageView imageLabel;
		RotateTextView expense_txt;
		TextView ItemBeanScore;
	}
}
