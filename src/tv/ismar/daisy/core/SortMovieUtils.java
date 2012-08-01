package tv.ismar.daisy.core;

import java.util.ArrayList;
import java.util.List;

import tv.ismar.daisy.models.MovieBean;

public class SortMovieUtils {

	public static List<MovieBean> sort(List<MovieBean> movieList) {
		
		List<MovieBean> listBean = new ArrayList<MovieBean>();
		for (MovieBean movieBean : movieList) {
			if (movieBean.content_model.equals("movie")) {
				listBean.add(movieBean);
			}
		}
		for (MovieBean movieBean : movieList) {
			if (!movieBean.content_model.equals("movie")) {
				listBean.add(movieBean);
			}
		}
		
		return listBean;
	}

}
