package org.sakuratya.horizontal.adapter;

import java.util.ArrayList;
import java.util.HashSet;

import tv.ismar.daisy.R;
import tv.ismar.daisy.models.Item;
import tv.ismar.daisy.models.ItemCollection;
import tv.ismar.daisy.views.AsyncImageView;
import tv.ismar.daisy.views.AsyncImageView.OnImageViewLoadListener;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class HGridAdapterImpl extends HGridAdapter<ItemCollection> implements OnImageViewLoadListener {
	
	private final static String TAG = "HGridAdapterImpl";
	
	private Context mContext;
	
	private int mSize = 0;
	
	private HashSet<AsyncImageView> mOnLoadingImageQueue = new HashSet<AsyncImageView>();
	
	public HGridAdapterImpl(Context context, ArrayList<ItemCollection> list) {
		mContext = context;
		if(list != null && list.size()>0) {
			mList = list;
			for(int i=0;i < list.size(); i++) {
				mSize +=list.get(i).count;
			}
		}
	}
	
	@Override
	public void setList(ArrayList<ItemCollection> list) {
		mSize = 0;
		for(int i=0;i < list.size(); i++) {
			mSize += list.get(i).count;
		}
		cancel();
		super.setList(list);
	}
	
	@Override
	public int getCount() {
		return mSize;
	}

	@Override
	public Item getItem(int position) {
		int size = 0;
		for(int i=0; i<mList.size(); i++) {
			final int sectionCount = mList.get(i).count;
			if(size +sectionCount > position) {
				int indexOfCurrentSection = position - size;
				return mList.get(i).objects.get(indexOfCurrentSection);
			}
			size += sectionCount;
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder = null;
		if(convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.list_view_item,null);
			holder = new Holder();
			holder.title = (TextView) convertView.findViewById(R.id.list_item_title);
			holder.previewImage = (AsyncImageView) convertView.findViewById(R.id.list_item_preview_img);
			holder.qualityLabel = (ImageView) convertView.findViewById(R.id.list_item_quality_label);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		
		int itemCount = 0;
		int sectionIndex = 0;
		int indexOfCurrentSection = 0;
		for(int i=0; i<mList.size(); i++) {
			final int sectionCount = mList.get(i).count;
			if(itemCount + sectionCount> position) {
				sectionIndex = i;
				indexOfCurrentSection = position - itemCount;
				break;
			}
			itemCount += sectionCount;
		}

		// This ItemCollection's currentIndex has been filled.
		if(mList.get(sectionIndex).isItemReady(indexOfCurrentSection)) {
			final Item item = mList.get(sectionIndex).objects.get(indexOfCurrentSection);
			holder.title.setText(item.title);
			holder.previewImage.setUrl(item.adlet_url);
//			Log.v("aaaa", "item.title ="+item.title+"<><>item.expense ="+item.expense.price);
			if(item.quality==3) {
				holder.qualityLabel.setImageResource(R.drawable.label_hd_small);
			} else if(item.quality==4 || item.quality==5) {
				holder.qualityLabel.setImageResource(R.drawable.label_uhd_small);
			} else {
				holder.qualityLabel.setImageDrawable(null);
			}
		} else {
			// This ItemCollection's currentIndex has not filled yet.
			// Show the default info.
			holder.title.setText(mContext.getResources().getString(R.string.onload));
			holder.previewImage.setUrl(null);
			holder.qualityLabel.setImageDrawable(null);
		}
		return convertView;
	}

	static class Holder {
		AsyncImageView previewImage;
		TextView title;
		ImageView qualityLabel;
	}

	@Override
	public int getSectionIndex(int position) {
		int size = 0;
		for(int i=0; i<mList.size(); i++) {
			size += mList.get(i).count;
			if(size > position) {
				return i;
			}
		}
		return 0;
	}
	
	@Override
	public boolean hasSection() {
		return true;
	}

	@Override
	public int getSectionCount(int sectionIndex) {
		return mList.get(sectionIndex).count;
	}

	@Override
	public String getLabelText(int sectionIndex) {
		return mList.get(sectionIndex).title;
	}
	
	public void cancel() {
		for(AsyncImageView imageView: mOnLoadingImageQueue) {
			imageView.stopLoading();
		}
		mOnLoadingImageQueue.clear();
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

}
