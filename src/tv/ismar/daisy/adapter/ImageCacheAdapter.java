package tv.ismar.daisy.adapter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import tv.ismar.daisy.R;
import tv.ismar.daisy.core.ImageLabelUtils;
import tv.ismar.daisy.models.MovieBean;
import tv.ismar.daisy.views.AsyncImageView;
import tv.ismar.daisy.views.AsyncImageView.OnImageViewLoadListener;
import android.R.anim;
import android.content.Context;
import android.graphics.AvoidXfermode.Mode;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

public class ImageCacheAdapter extends BaseAdapter implements OnImageViewLoadListener {

	private Context mContext;
	// 搜索集合
	private List<MovieBean> movieList;
	// layout ID
	private int sourceid;
	private LayoutInflater mLayoutInflater;
	// 背景图ID
	private final int backgroudID = R.drawable.list_item_preview_bgg;
	private final int backType = R.drawable.iv_type_comic;
	private Animation myAnimation;
	private HashSet<AsyncImageView> mAsyncImageList = new HashSet<AsyncImageView>();
	private HashMap<String, Bitmap>hashCache = new HashMap<String, Bitmap>();

	// private HashMap<Integer, HashMap<String, Object>> cacheMap = new HashMap<Integer, HashMap<String, Object>>();
	public ImageCacheAdapter(Context context, int sourceid) {
		this.mContext = context;
		this.sourceid = sourceid;
		this.mLayoutInflater = LayoutInflater.from(context);
	}

	public ImageCacheAdapter(Context context, List<MovieBean> movieList, int sourceid) {
		this.mContext = context;
		this.movieList = movieList;
		this.sourceid = sourceid;
		this.hashCache.clear();
		this.mLayoutInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return movieList.size();
	}

	@Override
	public MovieBean getItem(int position) {
		return movieList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	List<Integer> lstTimes = new ArrayList<Integer>();
	long startTime = 0;

	private MovieBean movieBean;
	ViewHolder holder;
	
	
   private int count = 0;
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// startTime = System.nanoTime();
		count++;
		
		movieBean = (MovieBean) getItem(position);
		if (null == convertView) {
			// Log.e("create_converView", "new conver");
			holder = new ViewHolder();
			convertView = mLayoutInflater.inflate(sourceid, null);
			holder.imageView = (AsyncImageView) convertView.findViewById(R.id.itemImage);
			holder.imageView.setOnImageViewLoadListener(this);
			holder.tvItemText = (TextView) convertView.findViewById(R.id.itemText);
			holder.imageLabel = (AsyncImageView) convertView.findViewById(R.id.iv_label);
			holder.imageLabel.setOnImageViewLoadListener(this);
			holder.imageType = (Button) convertView.findViewById(R.id.iv_type);
			Log.i("zhnagjiqiang", "count=="+position);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.imageView.setDefaultImageResource(backgroudID);
		holder.imageLabel.setBackgroundResource(ImageLabelUtils.getImageLabel(movieBean));
		// holder.imageLabel.setVisibility(View.GONE);
		// holder.imageLabel.setTag(ImageLabelUtils.getImageLabel(movieBean));
		holder.imageType.setBackgroundResource(backType);
		holder.imageType.setText(ImageLabelUtils.getImageType(movieBean));
		holder.tvItemText.setText(movieBean.title);
		holder.imageView.setTag(movieBean.adlet_url);
		if (null != hashCache.get(movieBean.adlet_url)) {
			holder.imageView.setImageBitmap(hashCache.get(movieBean.adlet_url));
		}else{
			holder.imageView.setUrl(movieBean.adlet_url);
		}
		//holder.imageView.setUrl(movieBean.adlet_url);
		return convertView;
	}

	// // private void showImage(ImageView imageView, String thumb_url,ImageView imageLabel,String content_model,String quality) {
	// private void showImage(ImageView imageView, ImageView imageLabel, Button imageType, MovieBean movieBean, int position) {
	// task = new LoadImageTask();
	// // 参数传给了doInBackground
	// // task.execute(imageView, thumb_url, imageLabel, content_model, quality);
	// task.execute(imageView, imageLabel, imageType, movieBean, position);
	// }
	//
	// private final class LoadImageTask extends AsyncTask<Object, Integer, HashMap<String, Object>> {
	// // 主图片
	// ImageView imageView;
	// // 高清标签
	// ImageView imageLabel;
	// // 类型标签
	// Button imageType;
	// Integer uuid;
	//
	// @Override
	// protected HashMap<String, Object> doInBackground(Object... params) {// 耗时操作,运行在子线程
	// imageView = (ImageView) params[0];
	// imageLabel = (ImageView) params[1];
	// imageType = (Button) params[2];
	// uuid = (Integer) params[4];
	// try {
	// return ImageService.getImage((MovieBean) params[3]);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// return null;
	// }
	//
	// @Override
	// protected void onPostExecute(HashMap<String, Object> mHash) {
	// try {
	// if (null != mHash) {
	// if (null != mHash.get("imageView") && imageView.getTag().equals(uuid)) {
	// cacheMap.put(uuid, mHash);
	// myAnimation = AnimationUtils.loadAnimation(mContext, anim.fade_in);
	// imageView.setImageBitmap((Bitmap) mHash.get("imageView"));// 显示照片
	// imageView.startAnimation(myAnimation);
	// imageLabel.setVisibility(View.VISIBLE);
	// imageType.setVisibility(View.VISIBLE);
	// if (null != mHash.get("imageLabel")) {
	// imageLabel.setBackgroundResource((Integer) mHash.get("imageLabel"));
	// } else {
	// imageLabel.setVisibility(View.GONE);
	// }
	// if (null != mHash.get("imageType")) {
	// imageType.setText(mHash.get("imageType").toString());
	// imageType.setBackgroundResource(backType);
	// } else {
	// imageType.setVisibility(View.GONE);
	// }
	// } else {
	// imageLabel.setVisibility(View.GONE);
	// imageType.setVisibility(View.GONE);
	// }
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	// }

	public void setAsyncisPauseed(Boolean bool) {
		try {
			for (AsyncImageView imageView : mAsyncImageList) {
				imageView.setPaused(bool);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static class ViewHolder {
		public AsyncImageView imageView;
		public AsyncImageView imageLabel;
		public Button imageType;
		public TextView tvItemText;
	}
	
	@Override
	public void onLoadingStarted(AsyncImageView imageView) {
		mAsyncImageList.add(imageView);
	}

	@Override
	public void onLoadingEnded(AsyncImageView imageView, Bitmap image) {
		
		hashCache.put((String) imageView.getTag(), image);
		//myAnimation = AnimationUtils.loadAnimation(mContext, anim.fade_in);
		//imageView.setImageBitmap(Bitmap.createScaledBitmap(image, 284, 160,false));
		//imageView.startAnimation(myAnimation);
		mAsyncImageList.remove(imageView);
	}

	@Override
	public void onLoadingFailed(AsyncImageView imageView, Throwable throwable) {
//		imageView.setImageResource(backgroudID);
//		imageView.reload();
		mAsyncImageList.remove(imageView);
	}

	public void cancelAsync() {
		try {
			for (AsyncImageView imageView : mAsyncImageList) {
				imageView.stopLoading();
			}
			mAsyncImageList.clear();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void clearCache(){
		hashCache.clear();
		hashCache = null;
	}

}
