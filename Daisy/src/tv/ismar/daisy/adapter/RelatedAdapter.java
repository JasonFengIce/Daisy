package tv.ismar.daisy.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import tv.ismar.daisy.R;
import tv.ismar.daisy.models.Item;
import tv.ismar.daisy.views.AsyncImageView;
import tv.ismar.daisy.views.AsyncImageView.OnImageViewLoadListener;
import tv.ismar.daisy.views.LabelImageView;
import tv.ismar.daisy.views.RotateTextView;

import java.util.ArrayList;
import java.util.HashSet;

public class RelatedAdapter extends BaseAdapter implements OnImageViewLoadListener {
	
	private Context mContext;
	private ArrayList<Item> mItemList;
	
	private HashSet<AsyncImageView> mOnLoadingImageQueue;
	private boolean isPortrait = false;
	public RelatedAdapter(Context context, ArrayList<Item> itemList,boolean isPortrait) {
        this.isPortrait = isPortrait;
		mContext = context;
		mOnLoadingImageQueue = new HashSet<AsyncImageView>();
		if(itemList!=null && itemList.size()>0){
			if(itemList.size()<=12) {
				mItemList = itemList;
			} else {
				mItemList = new ArrayList<Item>();
				for(int i=0; i<12;++i) {
					mItemList.add(itemList.get(i));
				}
			}
			
		} else {
			mItemList = new ArrayList<Item>();
		}
	}

	@Override
	public int getCount() {
		return mItemList.size();
	}

	@Override
	public Item getItem(int position) {
		
		return mItemList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder = null;
		if(convertView == null) {
			holder = new Holder();
            if(!isPortrait)
			   convertView = LayoutInflater.from(mContext).inflate(R.layout.list_view_related_item, null);
            else
                convertView = LayoutInflater.from(mContext).inflate(R.layout.list_portrait_relateditem,null);
//			AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(348, 252);
//			View titleView = convertView.findViewById(R.id.list_item_title);
//			titleView.setFocusable(true);
//			convertView.setClickable(true);
//			convertView.setLayoutParams(layoutParams);
			convertView.setTag(holder);
		} else {
			holder = (Holder)convertView.getTag();
		}
		holder.previewImage = (AsyncImageView) convertView.findViewById(R.id.list_item_preview_img);
		holder.title = (TextView) convertView.findViewById(R.id.list_item_title);
        if(!isPortrait)
		   holder.previewImage.setUrl(mItemList.get(position).adlet_url);
        else{
            holder.previewImage.setUrl(mItemList.get(position).list_url);
            if(mItemList.get(position).focus!=null)
              ((LabelImageView)holder.previewImage).setFocustitle(mItemList.get(position).focus);
        }
		holder.title.setText(mItemList.get(position).title);
		holder.qualityLabel = (ImageView) convertView.findViewById(R.id.list_item_quality_label);
		holder.ItemBeanScore = (TextView)convertView.findViewById(R.id.ItemBeanScore);
        holder.price = (RotateTextView)convertView.findViewById(R.id.expense_txt);
		holder.price.setDegrees(315);
		if(mItemList.get(position).bean_score>0){
			holder.ItemBeanScore.setText(""+mItemList.get(position).bean_score);
			holder.ItemBeanScore.setVisibility(View.VISIBLE);
		}
		else{
			holder.ItemBeanScore.setVisibility(View.INVISIBLE);
		}
        if(mItemList.get(position).expense!=null){
			if(mItemList.get(position).expense.cptitle!=null){
				holder.price.setText(mItemList.get(position).expense.cptitle);
				holder.price.setVisibility(View.VISIBLE);
				if(mItemList.get(position).expense.pay_type==1){
					holder.price.setBackgroundResource(R.drawable.list_single_buy);
				}else if((mItemList.get(position).expense.cpname).startsWith("ismar")){
					holder.price.setBackgroundResource(R.drawable.list_ismar);
				}else if("iqiyi".equals(mItemList.get(position).expense.cpname)){
					holder.price.setBackgroundResource(R.drawable.list_lizhi);
				}
			}
        }
        else{
            holder.price.setVisibility(View.GONE);
        }
		return convertView;
	}

	static class Holder {
		AsyncImageView previewImage;
		TextView title;
		ImageView qualityLabel;
		TextView ItemBeanScore;
		RotateTextView price;
	}
	
	public void cancel() {
		for(AsyncImageView imageView: mOnLoadingImageQueue) {
			imageView.stopLoading();
		}
		mOnLoadingImageQueue.clear();
		mOnLoadingImageQueue = null;
		mItemList = null;
	}

	@Override
	public void onLoadingStarted(AsyncImageView imageView) {
		mOnLoadingImageQueue.add(imageView);
	}

	@Override
	public void onLoadingEnded(AsyncImageView imageView, Bitmap image) {
		mOnLoadingImageQueue.remove(imageView);
	}

	@Override
	public void onLoadingFailed(AsyncImageView imageView, Throwable throwable) {
		mOnLoadingImageQueue.remove(imageView);
	}

	

	
//	@Override
//	public int getSectionCount(int sectionIndex) {
//		return mList.get(sectionIndex).count;
//	}
//
//	@Override
//	public String getLabelText(int sectionIndex) {
//		return mList.get(sectionIndex).title;
//	}
	

}
