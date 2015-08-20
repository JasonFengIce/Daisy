package tv.ismar.daisy.core;

import tv.ismar.daisy.models.MovieBean;

import java.util.ArrayList;
import java.util.List;

/**
 * 查询，单例类
 * 
 * @author liuhao
 * 
 */
public class SearchMovieService {
	private static SearchMovieService instance;
	private SearchMovieService() {
	}

	/**
	 * 查询单例
	 * 
	 * @return
	 */
	public static SearchMovieService getInstance() {
		if (null == instance) {
			synchronized (SearchMovieService.class) {
				if (null == instance) {
					instance = new SearchMovieService();
				}
			}
		}
		return instance;
	}

	/**
	 * 搜索返回结果
	 */
	public List<MovieBean> getSearchResult(String searchWhere) {
		String jsonString = null;
		List<MovieBean> listSearchResult = new ArrayList<MovieBean>();
		try {
			jsonString = NetworkUtils.getJsonStr(HttpUtil.spliceSearchURL(searchWhere),"");
			listSearchResult = JsonSearch.parseSearchJson(jsonString);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return listSearchResult;
	}

	/**
	 * 获得热门搜索
	 * 
	 * @param searchWhere
	 * @return
	 */
	public List<String> getHotWords() {
		String jsonString = null;
		List<String> listHotWords = new ArrayList<String>();
		try {
			//jsonString = HttpUtil.getJsonByGet(HttpUtil.spliceHotwordsURL());
			jsonString = NetworkUtils.getJsonStr(HttpUtil.spliceHotwordsURL(),"");
			listHotWords = JsonSearch.parseHotWords(jsonString);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return listHotWords;
	}

	/**
	 * 获得搜索推荐词
	 * 
	 * @param searchWhere
	 * @return
	 */
	public List<String> getSearchHelper(String searchWhere) {
		String jsonString = null;
		List<String> listSearchHelper = new ArrayList<String>();
		try {
			jsonString = NetworkUtils.getJsonStr(HttpUtil.spliceSuggestURL(searchWhere),"");
			listSearchHelper = JsonSearch.parseSearchPrompt(jsonString);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return listSearchHelper;
	}
}
