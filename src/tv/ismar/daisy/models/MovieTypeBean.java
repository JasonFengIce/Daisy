package tv.ismar.daisy.models;

import java.util.ArrayList;
import java.util.List;

public class MovieTypeBean {
	// movie 为电影
	public List<MovieBean> movList = new ArrayList<MovieBean>();
	// teleplay 为电视剧
	public List<MovieBean> teleplayList = new ArrayList<MovieBean>();
	// variety 为综艺
	public List<MovieBean> varietyList = new ArrayList<MovieBean>();
	// documentary 为纪录片
	public List<MovieBean> documentaryList = new ArrayList<MovieBean>();
	// entertainment 为娱乐
	public List<MovieBean> entertainmentList = new ArrayList<MovieBean>();
	// music 为音乐
	public List<MovieBean> musicList = new ArrayList<MovieBean>();
	// comic 为喜剧
	public List<MovieBean> comicList = new ArrayList<MovieBean>();
	// trailer 为片花
	public List<MovieBean> trailerList = new ArrayList<MovieBean>();
}
