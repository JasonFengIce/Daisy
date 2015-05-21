package tv.ismar.daisy.core;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import tv.ismar.daisy.VodApplication;
import tv.ismar.daisy.exception.ItemOfflineException;
import tv.ismar.daisy.exception.NetworkException;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

public class NetworkUtils {
	private static String UA = "A11/V1 Unknown";
	
	private static final String TAG = "NetworkUtils";
	
	private static final String URL = "http://127.0.0.1:21098/log/track/";
	
	private static final int BUFFERSIZE = 1024;
	DataCollectionTask mDataCollectionTask;
	public static String getJsonStr(String target) throws ItemOfflineException, NetworkException {
		String urlStr = target;
		Log.i("zhuabao", "url=="+urlStr);
		try {
			if(SimpleRestClient.device_token == null){
				VodApplication.setDevice_Token();
			}
			URL url = new URL(urlStr+"?device_token="+SimpleRestClient.device_token+"&access_token="+SimpleRestClient.access_token);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			StringBuffer sb = new StringBuffer();
			if(url.toString().contains("oumeijuchang")){
			Log.v("aaaa", url.toString());
			}
			//conn.addRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2062.94 Safari/537.36");
//			conn.addRequestProperty("Accept", "*/*");
			//conn.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.addRequestProperty("Accept-Encoding", "gzip,deflate,sdch");
			conn.setIfModifiedSince(System.currentTimeMillis());
			//conn.setUseCaches(false);
			conn.connect();
			int resCode = conn.getResponseCode();
			GZIPInputStream is = null;
			BufferedReader buff ;
			String encoding = conn.getContentEncoding();
			 if(encoding!=null && encoding.contains("gzip")){//首先判断服务器返回的数据是否支持gzip压缩，
					is = new GZIPInputStream(conn.getInputStream());
					buff = new BufferedReader(new InputStreamReader(is,"UTF-8")); //如果支持则应该使用GZIPInputStream解压，否则会出现乱码无效数据
				}
			 else{
				 buff = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));
			 }
			// buff = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));
			switch(resCode) {
			case 404:
				throw new ItemOfflineException(urlStr);
			case 599:
				throw new NetworkException(urlStr);
			case 400:
				sb.append("参数不对");
				return sb.toString();
			case 406:
				sb.append("device_token非标准格式");
				return sb.toString();
			}							          			 
			String line = null;
			
//			  try {
//				     
//	                Map<String, List<String>> map = conn.getHeaderFields();
//	     
//	                System.out.println("显示响应Header信息...\n");
//	     
//	                for (Map.Entry<String, List<String>> entry : map.entrySet()) {
//	                        System.out.println("Key : " + entry.getKey() + 
//	                                           " ,Value : " + entry.getValue());
//	                }
//	     
//	                System.out.println("\n使用key获得响应Header信息 \n");
//	                List<String> server = map.get("Server");
//	     
//	                if (server == null) {
//	                        System.out.println("Key 'Server' is not found!");
//	                } else {
//	                        for (String values : server) {
//	                                System.out.println(values);
//	                        }
//	                }
//	     
//	        } catch (Exception e) {
//	                e.printStackTrace();
//	        }
			  
			while((line=buff.readLine())!=null) {
				sb.append(line);
			}
			buff.close();
			conn.disconnect();
			return sb.toString();
		} catch(Exception e) {
			if(e instanceof ItemOfflineException) {
				throw (ItemOfflineException)e;
			} else if(e instanceof NetworkException) {
				throw (NetworkException)e;
			}
		}
		return null;
	}
	public static String getJsonStrByPost(String url,String values) throws ItemOfflineException, NetworkException{
		StringBuffer response = new StringBuffer();
		 try{
	        URL postUrl = new URL(url);
	        HttpURLConnection connection = (HttpURLConnection) postUrl
	                .openConnection();
	        connection.setDoOutput(true);
	        connection.setDoInput(true);
	        connection.setRequestMethod("POST");
	        //connection.setUseCaches(false);
	        connection.addRequestProperty("Accept-Encoding", "gzip,deflate,sdch");
	        connection.setInstanceFollowRedirects(true);
	        connection.setRequestProperty("Content-Type",
	                "application/x-www-form-urlencoded");
	        connection.setRequestProperty("Accept", "application/json");
	        connection.connect();
	        DataOutputStream out = new DataOutputStream(connection
	                .getOutputStream());
	      //  OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), "utf-8");
	        out.writeBytes(values);
	        out.flush();
	        out.close();
	        int status = connection.getResponseCode();
	        GZIPInputStream is = null;
			String encoding = connection.getContentEncoding();
			BufferedReader buff ;
			 if(encoding!=null && encoding.contains("gzip")){//首先判断服务器返回的数据是否支持gzip压缩，
					is = new GZIPInputStream(connection.getInputStream());
					buff = new BufferedReader(new InputStreamReader(is,"UTF-8")); //如果支持则应该使用GZIPInputStream解压，否则会出现乱码无效数据
				}
			 else{
				 buff = new BufferedReader(new InputStreamReader(connection.getInputStream(),"UTF-8"));
			 }
	        if(status==200){
//	            BufferedReader reader = new BufferedReader(new InputStreamReader(
//		                connection.getInputStream(),"UTF-8"));
		        String line;
		        while ((line = buff.readLine()) != null) {
		            response.append(line);
		        }
		        if(response.toString().equals("")){
		        	buff.close();	
		        	return "200";
		        }
		        buff.close();		        	
	        }
	        else if(status==201){
	        	//历史记录创建成功
	        	return "200";
	        }
	        else if(status==202){
	        	return "200";
	        }
	        else{
				switch(status) {
				case 404:
					throw new NetworkException("404");
				case 599:
					throw new NetworkException("599");
				case 400:
					throw new NetworkException("400");
				case 406:
					throw new NetworkException("406");
				}
	        	connection.disconnect();
	        }
	        connection.disconnect();
		 }
		 catch(Exception e) {
				if(e instanceof ItemOfflineException) {
					throw (ItemOfflineException)e;
				} else if(e instanceof NetworkException) {
					throw (NetworkException)e;
				}
			}
		 return response.toString();
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
	 * 记录日志信息到本地
	 */
//	public static boolean SaveLogToLocal(String eventName,HashMap<String,Object> propertiesMap){
//		try {
//			String jsonContent = getContentJson(eventName, propertiesMap);
//			SystemFileUtil.writeLogToLocal(jsonContent);
//			return true;
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return false;
//		}
//		
//	}
	
	public static boolean SaveLogToLocal(String eventName,HashMap<String,Object> propertiesMap){
		try {
			String jsonContent = getContentJson(eventName, propertiesMap);
			//SystemFileUtil.writeLogToLocal(jsonContent);
			 MessageQueue.addQueue(jsonContent);
			//LogSender(eventName, propertiesMap);
			return true;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
	}
	/**
	 * 
	 * LogSender 上报日志记录
	 * 
	 * @param eventName 上报的日志类型名。
	 * 
	 * @param propertiesMap 键值对，属性值
	 * 
	 * @return true、false  是否成功
	 * 
	 */
	public static Boolean LogSender(String Content) {
		try {
			String jsonContent = base64Code(Content);
			String url="http://ismartv.calla.tvxio.com/log";
			java.net.URL connURL = new java.net.URL(url);
			java.net.HttpURLConnection httpConn = (java.net.HttpURLConnection) connURL.openConnection();
			httpConn.setRequestMethod("POST");
			httpConn.setConnectTimeout(10000);
			httpConn.setReadTimeout(10000); 
			httpConn.setDoOutput(true);                 

	        httpConn.setDoInput(true); 
			httpConn.setRequestProperty("Accept", "*/*");
            httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			httpConn.setRequestProperty("Host", "a21.calla.tvxio.com");
			httpConn.setRequestProperty("Connection", "Keep-Alive");
			//httpConn.setRequestProperty("User-Agent", "ideatv_A21/S0054.38 TD04007053");
			httpConn.setRequestProperty("User-Agent", VodUserAgent.getUserAgent(VodUserAgent.getMACAddress()));
			httpConn.setRequestProperty("Pragma:", "no-cache");  
			httpConn.setRequestProperty("Cache-Control", "no-cache");
			httpConn.setRequestProperty("Accept-Encoding", "gzip");
			httpConn.setUseCaches(false);
			httpConn.connect();
			DataOutputStream out = new DataOutputStream(httpConn.getOutputStream());
			
			
			String content = "sn=" + SimpleRestClient.sn_token + "&modelname="
					+ VodUserAgent.getModelName() + "&data="
					+ URLEncoder.encode(jsonContent, "UTF-8") + "&deviceToken="
					+ SimpleRestClient.device_token + "&acessToken="
					+ SimpleRestClient.access_token;
			
			
			
			
			//String content = "data=" + URLEncoder.encode(jsonContent, "UTF-8");
	        out.writeBytes(content);
	        /////gzip
	       // out.write(MessageGZIP.compressToByte(content));
	        out.flush();
	        out.close(); // flush and close
	        BufferedReader reader = new BufferedReader(new InputStreamReader(httpConn.getInputStream(), "UTF-8"));
    		String line;
    		int code = httpConn.getResponseCode();
    		Log.i("LogSender", "LogSender code=="+code);
    		while ((line = reader.readLine()) != null)
    		{
    			System.out.println(line);
    		}
    		reader.close();
    		httpConn.disconnect();
			return true;
		} catch (MalformedURLException e) {
			Log.e(TAG, "" +" MalformedURLException "+e.toString());
			return false;
		} catch (IOException e) {
			Log.e(TAG, "" +" IOException "+e.toString());
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "" +" Exception "+e.toString());
			return false;
		}
		
	}
	
	private static boolean isSupportGzip(){
		boolean isSupport = false;
		
		String url="http://a21.calla.tvxio.com/log";
		java.net.URL connURL;
		java.net.HttpURLConnection httpConn = null;
		try {
			connURL = new java.net.URL(url);
			httpConn = (java.net.HttpURLConnection) connURL.openConnection();
			httpConn.setRequestProperty("Accept-Encoding","gzip"); 
			String gzip = httpConn.getContentEncoding();
			if(gzip!=null&&"gzip".equals(gzip)){
				isSupport = true;
			}

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			if(httpConn!=null)
			httpConn.disconnect();
		}
		return isSupport;
	}
	
	/**
	 * 
	 * LogSender 上报日志文件
	 * 
	 * @param eventName 上报的日志类型名。
	 * 
	 * @param propertiesMap 键值对，属性值
	 * 
	 * @return true、false  是否成功
	 * 
	 */
	public static Boolean LogUpLoad(Context context) {
		java.net.HttpURLConnection httpConn = null;
		try {
			String jsonContent = getFileToString();
			if(jsonContent == null){
				return false;
			}
			Log.d(TAG,"base64 =="+jsonContent);
			String url="http://ismartv.calla.tvxio.com/log";
			//String url = "http://192.168.1.185:8099/shipinkefu/22.mp4";
			java.net.URL connURL = new java.net.URL(url);
			httpConn = (java.net.HttpURLConnection) connURL.openConnection();
			httpConn.setRequestMethod("POST");
			httpConn.setConnectTimeout(10000);
			httpConn.setReadTimeout(10000); 
			httpConn.setDoOutput(true);                 

	        httpConn.setDoInput(true); 
			httpConn.setRequestProperty("Accept", "*/*");
            httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//			httpConn.setRequestProperty("Host", "a21.calla.tvxio.com");
			httpConn.setRequestProperty("Connection", "Keep-Alive");
			//httpConn.setRequestProperty("User-Agent", "ideatv_A21/S0054.38 TD04007053");
			httpConn.setRequestProperty("User-Agent", VodUserAgent.getUserAgent(VodUserAgent.getMACAddress()));
			httpConn.setRequestProperty("Pragma:", "no-cache");  
			httpConn.setRequestProperty("Cache-Control", "no-cache");
//			boolean isSupport = isSupportGzip();
//			if(isSupport)
//			  httpConn.setRequestProperty("Accept-Encoding","gzip");
			httpConn.setUseCaches(false);
			//String gzip1 = httpConn.getContentEncoding();
			//Log.i("zjq", "gzip1=="+gzip1);
			httpConn.connect();
			
			DataOutputStream out = new DataOutputStream(httpConn.getOutputStream());
			String content = "sn=" + SimpleRestClient.sn_token + "&modelname="
					+ VodUserAgent.getModelName() + "&data="
					+ URLEncoder.encode(jsonContent, "UTF-8") + "&deviceToken="
					+ SimpleRestClient.device_token + "&acessToken="
					+ SimpleRestClient.access_token;
			Log.d(TAG,content);
			byte[] datas = content.getBytes();

			byte[] b = new byte[BUFFERSIZE*BUFFERSIZE]; 
			if(datas.length<=b.length)
	          out.writeBytes(content);
			else{
				//out.write(buffer, offset, count)
				int mod = datas.length%b.length;
				int count = datas.length/b.length;
				int i=1;
				for(i=1; i<=count; i++){
//					byte[] c = new byte[1024*1024];
//					System.arraycopy(datas, (i-1)*1024*1024, c, 0, c.length);
					out.write(datas, (i-1)*BUFFERSIZE*BUFFERSIZE, BUFFERSIZE*BUFFERSIZE);
				}
				out.write(datas,(i-1)*BUFFERSIZE*BUFFERSIZE,mod);
			}

	        out.flush();
	        out.close(); // flush and close
	        BufferedReader reader = new BufferedReader(new InputStreamReader(httpConn.getInputStream(), "UTF-8"));
    		String line;
    		int code = httpConn.getResponseCode();
    		String response = httpConn.getResponseMessage();
    		String gzip2 = httpConn.getContentEncoding();
    		Log.i("zjq", "gzip2=="+gzip2);
    		while ((line = reader.readLine()) != null)
    		{
    			System.out.println(line);
    		}
    		SystemFileUtil.delete();
    		reader.close();
    		httpConn.disconnect();
			return true;
		} catch (MalformedURLException e) {
			Log.e(TAG, "event" +" MalformedURLException "+e.toString());
			if(httpConn!=null)
			   httpConn.disconnect();
			return false;
		} catch (IOException e) {
			Log.e(TAG, "event" +" IOException "+e.toString());
			if(httpConn!=null)
				   httpConn.disconnect();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			if(httpConn!=null)
				   httpConn.disconnect();
			Log.e(TAG, "event" +" Exception "+e.toString());
			return false;
		}
		
	}
	
	 //把文件转换成字节数组  
    private static String getFileToString() throws Exception {  
    	File f = new File(SystemFileUtil.LogPath);
    	if(f.exists()){
            FileInputStream in = new FileInputStream(SystemFileUtil.LogPath);
            ByteArrayOutputStream out = new ByteArrayOutputStream();  
            byte[] b = new byte[1024];  
            int n;  
            while ((n = in.read(b)) != -1) {  
                out.write(b, 0, n);  
            }  
            in.close(); 
            byte[] content = out.toByteArray();
            return new String(content,0,content.length);
    	}
    	else
    		return null;
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
        Log.d(TAG," Log data For Test === " + logJson.toString());
        return logJson.toString();
	}

	private static String base64Code(String date){
		try {
			//return Base64.encodeToString(date.getBytes("UTF-8"),Base64.NO_PADDING|Base64.NO_WRAP);
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
	
	public static class DataCollectionTask extends AsyncTask<Object, Void, Void> {
		
		@SuppressWarnings("unchecked")
		@Override
		protected Void doInBackground(Object... params) {
			if(params!=null && params.length>0) {
				String eventName = (String) params[0];
				HashMap<String, Object> properties = null;
				if(params.length>1 && params[1]!=null) {
					properties = (HashMap<String, Object>) params[1];
				}
				//LogSender(eventName, properties);
				
				//SaveLogToLocal(eventName,properties);
				String jsonContent;
				try {
					jsonContent = getContentJson(eventName, properties);
					 MessageQueue.addQueue(jsonContent);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return null;
		}
		
	}

	public static String httpsRequestHttps(String requestUrl,String outputStr) {
		String str = null;
		StringBuffer buffer = new StringBuffer();
		try {
			// 创建SSLContext对象，并使用我们指定的信任管理器初始化
			TrustManager[] tm = { new tv.ismar.daisy.utils.MyX509TrustManager() };
			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, tm, new java.security.SecureRandom());
			// 从上述SSLContext对象中得到SSLSocketFactory对象
			SSLSocketFactory ssf = sslContext.getSocketFactory();
			URL url = new URL(requestUrl);
			HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
			conn.setSSLSocketFactory(ssf);
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			// 设置请求方式（GET/POST）
			conn.setRequestMethod("POST");

			// 当outputStr不为null时向输出流写数据
			if (null != outputStr) {
				OutputStream outputStream = conn.getOutputStream();
				// 注意编码格式
				outputStream.write(outputStr.getBytes("UTF-8"));
				outputStream.close();
			}

			// 从输入流读取返回内容
			InputStream inputStream = conn.getInputStream();
			InputStreamReader inputStreamReader = new InputStreamReader(
					inputStream, "utf-8");
			BufferedReader bufferedReader = new BufferedReader(
					inputStreamReader);
			while ((str = bufferedReader.readLine()) != null) {
				buffer.append(str);
			}
			str = buffer.toString();
			// 释放资源
			bufferedReader.close();
			inputStreamReader.close();
			inputStream.close();
			inputStream = null;
			conn.disconnect();
		} catch (ConnectException ce) {
		} catch (Exception e) {
		}
		return str;
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
	public static final String VIDEO_PLAY_START = "video_play_start";
	/**
	 * 播放暂停
	 */
	public static final String VIDEO_PLAY_PAUSE = "video_play_pause";
	/**
	 * 播放继续
	 */
	public static final String VIDEO_PLAY_CONTINUE = "video_play_continue";
	/**
	 * 播放快进/快退
	 */
	public static final String VIDEO_PLAY_SEEK = "video_play_seek";
	/**
	 * 播放快进/快退缓冲结束
	 */
	public static final String VIDEO_PLAY_SEEK_BLOCKEND = "video_play_seek_blockend";
	/**
	 * 播放缓冲结束
	 */
	public static final String VIDEO_PLAY_BLOCKEND = "video_play_blockend";
	/**
	 * 播放时网速
	 */
	public static final String VIDEO_PLAY_SPEED = "video_play_speed";
	/**
	 * 播放时下载速度慢
	 */
	public static final String VIDEO_LOW_SPEED = "video_low_speed";
	/**
	 * 播放器退出
	 */
	public static final String VIDEO_EXIT = "video_exit";
	/**
	 * 视频收藏
	 */
	public static final String VIDEO_COLLECT = "video_collect";
	/**
	 * 进入收藏界面
	 */
	public static final String VIDEO_COLLECT_IN = "video_collect_in";
	/**
	 * 退出收藏界面
	 */
	public static final String VIDEO_COLLECT_OUT = "video_collect_out";
	/**
	 * 视频存入历史
	 */
	public static final String VIDEO_HISTORY = "video_history";
	/**
	 * 进入播放历史界面
	 */
	public static final String VIDEO_HISTORY_IN = "video_history_in";
	/**
	 * 退出播放历史界面
	 */
	public static final String VIDEO_HISTORY_OUT = "video_history_out";
	/**
	 * 视频评分
	 */
	public static final String VIDEO_SCORE = "video_score";
	/**
	 * 视频评论
	 */
	public static final String VIDEO_COMMENT = "video_comment";

	/**
	 * 启动某视频频道
	 */
	public static final String VIDEO_CHANNEL_IN = "video_channel_in";

	/**
	 * 退出某视频频道
	 */
	public static final String VIDEO_CHANNEL_OUT = "video_channel_out";

	/**
	 * 进入分类浏览
	 */
	public static final String VIDEO_CATEGORY_IN = "video_category_in";

	/**
	 * 退出分类浏览
	 */
	public static final String VIDEO_CATEGORY_OUT = "video_category_out";

	/**
	 * 进入媒体详情页
	 */
	public static final String VIDEO_DETAIL_IN = "video_detail_in";

	/**
	 * 退出媒体详情页
	 */
	public static final String VIDEO_DETAIL_OUT = "video_detail_out";
	/**
	 * 在详情页进入关联
	 */
	public static final String VIDEO_RELATE = "video_relate";

	/**
	 * 进入关联界面
	 */
	public static final String VIDEO_RELATE_IN = "video_relate_in";
	/**
	 * 退出关联界面
	 */
	public static final String VIDEO_RELATE_OUT = "video_relate_out";
	/**
	 * 进入专题浏览
	 */
	public static final String VIDEO_TOPIC_IN = "video_topic_in";
	/**
	 * 退出专题浏览
	 */
	public static final String VIDEO_TOPIC_OUT = "video_topic_out";
	/**
	 * 视频预约
	 */
	public static final String VIDEO_NOTIFY = "video_notify";
	/**
	 * 点击视频购买
	 */
	public static final String VIDEO_EXPENSE_CLICK = "video_expense_click";
	/**
	 * 视频购买
	 */
	public static final String VIDEO_EXPENSE = "video_expense";
	/**
	 * 搜索
	 */
	public static final String VIDEO_SEARCH = "video_search";
	/**
	 * 搜索结果命中
	 */
	public static final String VIDEO_SEARCH_ARRIVE = "video_search_arrive";
	/**
	 * 播放器异常
	 */
	public static final String VIDEO_EXCEPT = "video_except";
	/**
	 * 栏目页异常
	 */
	public static final String CATEGORY_EXCEPT = "category_except";
	/**
	 * 详情页异常
	 */
	public static final String DETAIL_EXCEPT = "detail_except";
	/**
	 * 用户点击某个推荐影片
	 */
	public static final String LAUNCHER_VOD_CLICK = "launcher_vod_click";
	/**
	 * 预告片播放
	 */
	public static final String LAUNCHER_VOD_TRAILER_PLAY = "launcher_vod_trailer_play";
	/**
	 * 用户登录
	 */
	public static final String USER_LOGIN = "user_login";
	/**
	 * 进入筛选界面
	 */
	public static final String VIDEO_FILTER_IN = "video_filter_in";
	/**
	 * 退出筛选界面
	 */
	public static final String VIDEO_FILTER_OUT = "video_filter_out";
	/**
	 * 使用筛选
	 */
	public static final String VIDEO_FILTER = "video_filter";
	/**
	 * 进入我的频道
	 */
	public static final String VIDEO_MYCHANNEL_IN = "video_mychannel_in";
	/**
	 * 退出我的频道
	 */
	public static final String VIDEO_MYCHANNEL_OUT = "video_mychannel_out";
	/**
	 * 进入剧集列表界面
	 */
	public static final String VIDEO_DRAMALIST_IN = "video_dramalist_in";
	/**
	 * 退出剧集列表界面
	 */
	public static final String VIDEO_DRAMALIST_OUT = "video_dramalist_out";
}
