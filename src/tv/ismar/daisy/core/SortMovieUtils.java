package tv.ismar.daisy.core;

import tv.ismar.daisy.models.MovieBean;

import java.util.ArrayList;
import java.util.List;

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
