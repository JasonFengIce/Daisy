package tv.ismar.daisy.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import tv.ismar.daisy.R;
import tv.ismar.daisy.core.ImageService;
import tv.ismar.daisy.models.MovieBean;
import android.R.anim;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ImageCacheAdapter extends BaseAdapter {
	private Context mContext;
	//搜索集合
	private List<MovieBean> movieList;
	//layout ID
	private int sourceid;
	private LayoutInflater mLayoutInflater;
	//UUID对象
	private String uuid;
	//背景图ID
	private int backgroudID = R.drawable.list_item_preview_bg;
	private int backType = R.drawable.iv_type_comic;
	private Animation myAnimation;
	
	List<Bitmap> listBitmap = new ArrayList<Bitmap>();

	public ImageCacheAdapter(Context context, List<MovieBean> movieList, int sourceid) {
		this.mContext = context;
		this.movieList = movieList;
		this.sourceid = sourceid;
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
	private LoadImageTask task;

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		startTime = System.nanoTime();
		movieBean = (MovieBean) getItem(position);
		if (null == convertView) {
//			Log.e("create_converView", "new conver");
			holder = new ViewHolder();
			convertView = mLayoutInflater.inflate(sourceid, null);
			holder.imageView = (ImageView) convertView.findViewById(R.id.itemImage);
			holder.tvItemText = (TextView) convertView.findViewById(R.id.itemText);
			holder.imageLabel = (ImageView) convertView.findViewById(R.id.iv_label);
			holder.imageType = (Button) convertView.findViewById(R.id.iv_type);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		uuid = UUID.randomUUID().toString();
		holder.imageView.setTag(uuid);

		// int endTime = (int) (System.nanoTime() - startTime);
		// lstTimes.add(endTime);
		// if (lstTimes.size() == 10) {
		// int total = 0;
		// for (int i = 0; i < lstTimes.size(); i++)
		// total = total + lstTimes.get(i);
		// Log.e("10个所花的时间：" + total / 1000 + " μs", "所用内存：" + Runtime.getRuntime().totalMemory() / 1024 + " KB");
		// lstTimes.clear();
		// }

		holder.tvItemText.setText(movieBean.title);
		holder.imageView.setImageResource(backgroudID);
		holder.imageLabel.setVisibility(View.GONE);
		holder.imageType.setVisibility(View.GONE);
		showImage(holder.imageView, holder.imageLabel, holder.imageType, movieBean, uuid);
		return convertView;
	}

	// private void showImage(ImageView imageView, String thumb_url,ImageView imageLabel,String content_model,String quality) {
	private void showImage(ImageView imageView, ImageView imageLabel, Button imageType, MovieBean movieBean, String uuid) {
		task = new LoadImageTask();
		// 参数传给了doInBackground
		// task.execute(imageView, thumb_url, imageLabel, content_model, quality);
		task.execute(imageView, imageLabel, imageType, movieBean, uuid);
	}

	private final class LoadImageTask extends AsyncTask<Object, Integer, Object[]> {
		//主图片
		ImageView imageView;
		//高清标签
		ImageView imageLabel;
		//类型标签
		Button imageType;
		String uuid;
		

		@Override
		protected Object[] doInBackground(Object... params) {// 耗时操作,运行在子线程
			imageView = (ImageView) params[0];
			imageLabel = (ImageView) params[1];
			imageType = (Button) params[2];
			uuid = (String) params[4];
			try {
				return ImageService.getImage((MovieBean) params[3]);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Object[] object) {
			try {
				if (null != object[0] && imageView.getTag().equals(uuid)) {
					myAnimation = AnimationUtils.loadAnimation(mContext, anim.fade_in);
					imageView.setImageBitmap((Bitmap) object[0]);// 显示照片
					imageView.startAnimation(myAnimation);
					imageLabel.setVisibility(View.VISIBLE);
					imageType.setVisibility(View.VISIBLE);
					if (null != object[1]) {
						imageLabel.setBackgroundResource((Integer) object[1]);
					} else {
						imageLabel.setVisibility(View.GONE);
					}
					if (null != object[2]) {
						imageType.setText(object[2].toString());
						imageType.setBackgroundResource(backType);
					} else {
						imageType.setVisibility(View.GONE);
					}
				} else {
					imageLabel.setVisibility(View.GONE);
					imageType.setVisibility(View.GONE);
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
		Button imageType;
	}
}
