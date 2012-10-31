package tv.ismar.daisy.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Base64;
import android.util.Log;

public class NetworkUtils {
	private static String UA = "A11/V1 Unknown";
	
	private static final String TAG = "NetworkUtils";
	
	private static final String URL = "http://callatest.tvxio.com/log";
	
	public static String getJsonStr(String target) throws ItemOfflineException {
		String urlStr = target;
		try {
			URL url = new URL(urlStr);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			StringBuffer sb = new StringBuffer();
//			conn.addRequestProperty("User-Agent", UA);
//			conn.addRequestProperty("Accept", "application/json");
			if(conn.getResponseCode()==404) {
				throw new ItemOfflineException();
			}
			InputStream in = conn.getInputStream();
			BufferedReader buff = new BufferedReader(new InputStreamReader(in));
			String line = null;
			while((line=buff.readLine())!=null) {
				sb.append(line);
			}
			buff.close();
			conn.disconnect();
			return sb.toString();
		} catch (Exception e) {
			if(e instanceof ItemOfflineException) {
				throw (ItemOfflineException) e;
			}
			e.printStackTrace();
		}
		return null;
	}
	
	public static InputStream getInputStream(String target) {
		String urlStr = target;
		try {
			URL url = new URL(urlStr);
			URLConnection conn = url.openConnection();
			conn.addRequestProperty("User-Agent", UA);
			conn.addRequestProperty("Accept", "application/json");
//			conn.connect();
			return conn.getInputStream();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 
	 * LogSender 上报日志
	 * 
	 * @param eventName 上报的日志类型名。
	 * 
	 * @param propertiesMap 键值对，属性值
	 * 
	 * @return true、false  是否成功
	 * 
	 */
	public static boolean LogSender(String eventName,HashMap<String,Object> propertiesMap) {
		BufferedReader in = null;
		try {
			String jsonContent = getContentJson(eventName, propertiesMap);
			Log.d(TAG, eventName+" properties = " + jsonContent);
			jsonContent.replaceAll("-", "+");
			jsonContent.replaceAll("_", "/");
			String url=URL+"/A11/track/?date="+base64Code(jsonContent);
			java.net.URL connURL = new java.net.URL(url);
			java.net.HttpURLConnection httpConn = (java.net.HttpURLConnection) connURL.openConnection();
			httpConn.setRequestMethod("POST");
			httpConn.setConnectTimeout(10000);
			httpConn.setReadTimeout(10000);  
			httpConn.setRequestProperty("Accept", "application/json");
			httpConn.setRequestProperty("User-Agent", VodUserAgent.getUserAgent(VodUserAgent.getMACAddress()));
			httpConn.connect();
			Log.d(TAG, eventName+" SUCCESS ");
			return true;
		} catch (MalformedURLException e) {
			Log.e(TAG, eventName +" MalformedURLException "+e.toString());
			return false;
		} catch (IOException e) {
			Log.e(TAG, eventName +" IOException "+e.toString());
			return false;
		} catch (Exception e) {
			Log.e(TAG, eventName +" Exception "+e.toString());
			return false;
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				Log.d(TAG, eventName +" close() IOException  " + ex.toString());
				ex.printStackTrace();
			}
		}
	}
	
	private static String getContentJson(String eventName,HashMap<String,Object> propertiesMap) throws JSONException {
		JSONObject propertiesJson = new JSONObject();
	        propertiesJson.put("time", System.currentTimeMillis() / 1000);
	        if (propertiesMap != null) {
	            Set<String> set = propertiesMap.keySet();
	            for (String key : set) {
	                propertiesJson.put(key, propertiesMap.get(key));
	            }
	    }
		JSONObject logJson = new JSONObject();
        logJson.put("event", eventName);
        logJson.put("properties", propertiesJson);
        return logJson.toString();
	}
	
	private static String base64Code(String date){
		try {
			return Base64.encodeToString(date.getBytes("UTF-8"),Base64.DEFAULT);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	public static boolean urlEquals(String url1, String url2) {
		return removeRoot(url1).equals(removeRoot(url2));
	}
	
	public static String removeRoot(String url) {
		int start = url.indexOf("/api/");
		return url.substring(start, url.length());
	}
	
	/**
	 * 设备启动
	 */
	public static final String SYSTEM_ON = "system_on";
	/**
	 * 播放器打开
	 */
	public static final String VIDEO_START = "video_start";
	/**
	 * 开始播放缓冲结束
	 */
	public static final String VIDEO_PLAY_LOAD = "video_play_load";
	/**
	 * 切换码流
	 */
	public static final String VIDEO_SWITCH_STREAM = "video_switch_stream";
	/**
	 *	开始播放
	 */
	public static final String video_play_start = "video_play_start";
	/**
	 * 播放暂停
	 */
	public static final String video_play_pause = "video_play_pause";
	/**
	 * 播放继续
	 */
	public static final String video_play_continue = "video_play_continue";
	/**
	 * 播放快进/快退
	 */
	public static final String video_play_seek = "video_play_seek";
	/**
	 * 播放快进/快退缓冲结束
	 */
	public static final String video_play_seek_blockend = "video_play_seek_blockend";
	/**
	 * 播放缓冲结束
	 */
	public static final String video_play_blockend = "video_play_blockend";
	/**
	 * 播放时网速
	 */
	public static final String video_play_speed = "video_play_speed";
	/**
	 * 播放时下载速度慢
	 */
	public static final String video_low_speed = "video_low_speed";
	/**
	 * 播放器退出
	 */
	public static final String video_exit = "video_exit";
	/**
	 * 视频收藏
	 */
	public static final String video_collect = "video_collect";
	/**
	 * 进入收藏界面
	 */
	public static final String video_collect_in = "video_collect_in";
	/**
	 * 退出收藏界面
	 */
	public static final String video_collect_out = "video_collect_out";
	/**
	 * 视频存入历史
	 */
	public static final String video_history = "video_history";
	/**
	 * 进入播放历史界面
	 */
	public static final String video_history_in = "video_history_in";
	/**
	 * 退出播放历史界面
	 */
	public static final String video_history_out = "video_history_out";
	/**
	 * 视频评分
	 */
	public static final String video_score = "video_score";
	/**
	 * 视频评论
	 */
	public static final String video_comment = "video_comment";

	/**
	 * 启动某视频频道
	 */
	public static final String video_channel_in = "video_channel_in";

	/**
	 * 退出某视频频道
	 */
	public static final String video_channel_out = "video_channel_out";

	/**
	 * 进入分类浏览
	 */
	public static final String video_category_in = "video_category_in";

	/**
	 * 退出分类浏览
	 */
	public static final String video_category_out = "video_category_out";

	/**
	 * 进入媒体详情页
	 */
	public static final String video_detail_in = "video_detail_in";

	/**
	 * 退出媒体详情页
	 */
	public static final String video_detail_out = "video_detail_out";
	/**
	 * 在详情页进入关联
	 */
	public static final String video_relate = "video_relate";

	/**
	 * 进入关联界面
	 */
	public static final String video_relate_in = "video_relate_in";
	/**
	 * 退出关联界面
	 */
	public static final String video_relate_out = "video_relate_out";
	/**
	 * 进入专题浏览
	 */
	public static final String video_topic_in = "video_topic_in";
	/**
	 * 退出专题浏览
	 */
	public static final String video_topic_out = "video_topic_out";
	/**
	 * 视频预约
	 */
	public static final String video_notify = "video_notify";
	/**
	 * 点击视频购买
	 */
	public static final String video_expense_click = "video_expense_click";
	/**
	 * 视频购买
	 */
	public static final String video_expense = "video_expense";
	/**
	 * 搜索
	 */
	public static final String video_search = "video_search";
	/**
	 * 搜索结果命中
	 */
	public static final String video_search_arrive = "video_search_arrive";
	/**
	 * 播放器异常
	 */
	public static final String video_except = "video_except";
	/**
	 * 栏目页异常
	 */
	public static final String category_except = "category_except";
	/**
	 * 详情页异常
	 */
	public static final String detail_except = "detail_except";
	/**
	 * 用户点击某个推荐影片
	 */
	public static final String launcher_vod_click = "launcher_vod_click";
	/**
	 * 预告片播放
	 */
	public static final String launcher_vod_trailer_play = "launcher_vod_trailer_play";
	/**
	 * 用户登录
	 */
	public static final String user_login = "user_login";
	/**
	 * 进入筛选界面
	 */
	public static final String video_filter_in = "video_filter_in";
	/**
	 * 退出筛选界面
	 */
	public static final String video_filter_out = "video_filter_out";
	/**
	 * 使用筛选
	 */
	public static final String video_filter = "video_filter";
	/**
	 * 进入我的频道
	 */
	public static final String video_mychannel_in = "video_mychannel_in";
	/**
	 * 退出我的频道
	 */
	public static final String video_mychannel_out = "video_mychannel_out";
}
