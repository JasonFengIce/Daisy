package tv.ismar.daisy.core;

import java.util.ArrayList;
import java.util.List;

import tv.ismar.daisy.models.MovieBean;
import android.util.Log;

public class GroupList {

	public static List<List<MovieBean>> getGroupList(List<MovieBean> movieBean) {
		List<List<MovieBean>> movieList = new ArrayList<List<MovieBean>>();
		// 类型产品类型:
		// 优雅的分割线--------------------------------------------------
		// movie 为电影
		List<MovieBean> movList = new ArrayList<MovieBean>();
		// teleplay 为电视剧
		List<MovieBean> teleplayList = new ArrayList<MovieBean>();
		// variety 为综艺
		List<MovieBean> varietyList = new ArrayList<MovieBean>();
		// documentary 为纪录片
		List<MovieBean> documentaryList = new ArrayList<MovieBean>();
		// entertainment 为娱乐
		List<MovieBean> entertainmentList = new ArrayList<MovieBean>();
		// music 为音乐
		List<MovieBean> musicList = new ArrayList<MovieBean>();
		// comic 为喜剧
		List<MovieBean> comicList = new ArrayList<MovieBean>();
		// trailer 为片花
		List<MovieBean> trailerList = new ArrayList<MovieBean>();
		// 优雅的分割线--------------------------------------------------

		// MovieBean mBean = new MovieBean();
		for (MovieBean list : movieBean) {
			Log.e("asd", list.content_model);
			if (list.content_model.equals("movie")) {
				movList.add(list);
				movieList.add(movList);
			} else if (list.content_model.equals("teleplay")) {
				teleplayList.add(list);
				movieList.add(teleplayList);
			} else if (list.content_model.equals("variety")) {
				varietyList.add(list);
				movieList.add(varietyList);
			} else if (list.content_model.equals("documentary")) {
				documentaryList.add(list);
				movieList.add(documentaryList);
			} else if (list.content_model.equals("entertainment")) {
				entertainmentList.add(list);
				movieList.add(entertainmentList);
			} else if (list.content_model.equals("music")) {
				musicList.add(list);
				movieList.add(musicList);
			} else if (list.content_model.equals("comic")) {
				comicList.add(list);
				movieList.add(comicList);
			} else if (list.content_model.equals("trailer")) {
				trailerList.add(list);
				movieList.add(trailerList);
			}
		}
		return movieList;
	}
}
