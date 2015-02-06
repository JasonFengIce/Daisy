package tv.ismar.daisy.adapter;

import java.util.ArrayList;
import java.util.HashSet;

import org.sakuratya.horizontal.adapter.HGridAdapter;

import tv.ismar.daisy.R;
import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.models.Item;
import tv.ismar.daisy.views.AsyncImageView;
import tv.ismar.daisy.views.AsyncImageView.OnImageViewLoadListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

public class PackageItemAdapter extends HGridAdapter<Item> implements OnImageViewLoadListener {
	
	private Context mContext;
	private ArrayList<Item> mItemList;
	
	private HashSet<AsyncImageView> mOnLoadingImageQueue;
	
	public PackageItemAdapter(Context context, ArrayList<Item> itemList) {
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
			convertView = LayoutInflater.from(mContext).inflate(R.layout.list_view_item, null);
			convertView.setTag(holder);
		} else {
			holder = (Holder)convertView.getTag();
		}
		holder.previewImage = (AsyncImageView) convertView.findViewById(R.id.list_item_preview_img);
		holder.title = (TextView) convertView.findViewById(R.id.list_item_title);
		holder.previewImage.setUrl(mItemList.get(position).adlet_url);
		holder.title.setText(mItemList.get(position).title);
		holder.qualityLabel = (ImageView) convertView.findViewById(R.id.list_item_quality_label);
		return convertView;
	}
	
	static class Holder {
		AsyncImageView previewImage;
		TextView title;
		ImageView qualityLabel;
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


	@Override
	public int getSectionIndex(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getSectionCount(int sectionIndex) {
		// TODO Auto-generated method stub
		return mItemList.size();
	}

	@Override
	public String getLabelText(int sectionIndex) {
		// TODO Auto-generated method stub
		return "";
	}
	@Override
	public boolean hasSection() {
		return false;
	}

}
