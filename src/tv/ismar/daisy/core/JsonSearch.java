package tv.ismar.daisy.core;

import android.text.TextUtils;
import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import tv.ismar.daisy.models.MovieBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Json解析Utils
 * 
 * @author liuhao
 */
public class JsonSearch {
	private static JSONObject jsonArray;

	/**
	 * 解析搜索结果
	 * 
	 * @param jsonObject
	 * @return List<MovieBean>
	 */
	public static List<MovieBean> parseSearchJson(String jsonObject) {
		try {
			if (TextUtils.isEmpty(jsonObject))
				return null;
			List<MovieBean> movieList = new ArrayList<MovieBean>();
			MovieBean movieBean = new MovieBean();
			JSONObject jObject = new JSONObject(jsonObject);
			int count = jObject.getInt("count");
			Gson gson = new Gson();
			for (int j = 0; count != 0 && j < count && j < 100; j++) {
				jsonArray = jObject.getJSONArray("objects").getJSONObject(j);
				movieBean = gson.fromJson(jsonArray.toString(), movieBean.getClass());
				movieList.add(movieBean);
			}
			return movieList;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 搜索热门关键字
	 * 
	 * @return list
	 */
	public static List<String> parseHotWords(String jsonHots) {
		if (TextUtils.isEmpty(jsonHots))
			return null;
		List<String> listHot = new ArrayList<String>();
		String hotTitle;
		try {
			JSONArray jsonHot = new JSONArray(jsonHots);
			for (int i = 0; i < jsonHot.length(); i++) {
				hotTitle = jsonHot.getJSONObject(i).getString("title");
				listHot.add(hotTitle);
			}
		} catch (JSONException e) {
			listHot = null;
			e.printStackTrace();
		}
		return listHot;
	}

	/**
	 * 搜索提示接口
	 */
	public static List<String> parseSearchPrompt(String jsonPrompt) {
		if (TextUtils.isEmpty(jsonPrompt))
			return null;
		List<String> listPrompt = new ArrayList<String>();
		String strPrompt;
		try {
			JSONArray jsonArray = new JSONArray(jsonPrompt);
			for (int i = 0; i < jsonArray.length() && i < 100; i++) {
				strPrompt = jsonArray.getString(i);
				listPrompt.add(strPrompt);
			}
			return listPrompt;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
