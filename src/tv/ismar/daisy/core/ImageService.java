package tv.ismar.daisy.core;

import tv.ismar.daisy.R;
import tv.ismar.daisy.models.MovieBean;
import android.graphics.BitmapFactory;

public class ImageService {
	/**
	 * 
	 * @param path
	 * @return BitMap
	 * @throws Exception
	 */

	public static Object[] getImage(MovieBean movieBean) throws Exception {
		Object[] object = new Object[4];
		object[0] = (BitmapFactory.decodeStream(NetworkUtils.getInputStream(movieBean.adlet_url)));
		int resourceLabel = 0;
		String resourceType = null;
		// movie 为电影
		if (movieBean.content_model.equals("movie")) {
			// holder.imageLabel.setVisibility(View.VISIBLE);
//			resourceLabel = R.drawable.iv_type_movie;
			if (movieBean.quality.equals("2")) {
				resourceLabel = R.drawable.label_no;
			} else if (movieBean.quality.equals("3")) {
				resourceLabel = R.drawable.label_uhd;
			} else if (movieBean.quality.equals("4")) {
				resourceLabel = R.drawable.label_hd;
			} 
			object[1] = resourceLabel;
			resourceType = "电影";
		}
		// holder.imageLabel.setVisibility(View.GONE);
		// teleplay 为电视剧trailer
		else if (movieBean.content_model.equals("teleplay")) {
			resourceType = "电视剧";
			// variety 为综艺
		} else if (movieBean.content_model.equals("variety")) {
			resourceType = "综艺";
			// documentary 为纪录片
		} else if (movieBean.content_model.equals("documentary")) {
			resourceType = "纪录片";
			// entertainment 为娱乐
		} else if (movieBean.content_model.equals("entertainment")) {
			resourceType = "娱乐";
			// trailer 为片花
		} else if (movieBean.content_model.equals("trailer")) {
			resourceType = "片花";
			// music 为音乐
		} else if (movieBean.content_model.equals("music")) {
			resourceType = "音乐";
			// comic 为喜剧
		} else if (movieBean.content_model.equals("comic")) {
			resourceType = "喜剧";
		} 
		object[2] = resourceType;
		object[3] = movieBean.title;

		return object;
	}
}
