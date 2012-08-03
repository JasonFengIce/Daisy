package tv.ismar.daisy.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.util.Log;

/**
 * Http访问网络工具类
 * 
 * @author liuhao
 */
public class HttpUtil {
	public final static String TAG = HttpUtil.class.getSimpleName();
	// 接口入口地址
//	 public static String ROOT_URL = "http://cord.tvxio.com/";
	public static String ROOT_URL = "http://127.0.0.1:21098/cord/";
	// 搜索接口
	public static String search_URL = "api/tv/search/";
	// 提示接口
	public static String suggest_URL = "api/tv/suggest/";
	// 推荐词
	public static String hotwords_URL = "api/tv/hotwords/";
	// http://cord.tvxio.com/api/tv/search/$movie/%E6%82%AC%E5%B4%96/1

	// 接口入口地址host
	static String host = "cord.tvxio.com";

	static String server = ROOT_URL;
	// 设备名
	static String devName = "A11";
	// 设备版本
	static String devVersion = "1.0";
	// 附加信息
	static String mac = null;

	/**
	 * 获得本机MAC
	 * 
	 * @return MAC
	 */
	public static String getMACAddress() {
		if (mac == null) {
			try {
				byte addr[];
				addr = NetworkInterface.getByName("eth0").getHardwareAddress();
				mac = "";
				for (int i = 0; i < 6; i++) {
					mac += String.format("%02X", addr[i]);
				}
			} catch (Exception e) {
				mac = "00112233445566";
			}
		}
		return mac;
	}

	public static String getUserAgent() {
		return devName + "/" + devVersion + " " + getMACAddress();
	}

	protected synchronized static URL getURL(String path) {
		String addr = server + path;
		URL u = null;

		try {
			u = new URL(addr);
		} catch (Exception e) {
			Log.d(TAG, "parse URL failed!");
		}

		return u;
	}

	/**
	 * 通过Url获取HttpGet对象
	 */
	public static HttpGet getHttpGet(String url) {
		return new HttpGet(url);
	}

	/**
	 * 通过Url获取HttpPost对象
	 */
	public static HttpPost getHttpPost(String urlString) {
		return new HttpPost(urlString);
	}

	/**
	 * 通过HttpGet获取HttpResponse对象
	 * 
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	public static HttpResponse getHttpResponseByGet(HttpGet httpGet) throws ClientProtocolException, IOException {
		HttpClient client = null;
		HttpParams params = new BasicHttpParams();// Set the timeout in milliseconds until a connection is established.
		HttpConnectionParams.setConnectionTimeout(params, 10000);// Set the default socket timeout (SO_TIMEOUT) // in milliseconds which is the timeout for waiting for data.
		HttpConnectionParams.setSoTimeout(params, 10000);
		client = new DefaultHttpClient(params);

		return client.execute(httpGet);
	}

	/**
	 * 通过HttpPost获取HttpResponse对象
	 * 
	 * @throws Exception
	 */
	private static URL url = null;
	private static URLConnection urlConnection = null;
	private static String json = null;

	public static URLConnection getHttpConnectionByGet(String httpPath) throws Exception {
		url = new URL(httpPath);
		urlConnection = url.openConnection();
		urlConnection.addRequestProperty("User-Agent", getUserAgent());
		urlConnection.setConnectTimeout(20000);
		urlConnection.setReadTimeout(20000);
		return urlConnection;
	}

	public static String getJsonByGet(String url) throws Exception {
		if (null == url)
			return null;
		InputStream inputS = null;
		URLConnection urlConn = getHttpConnectionByGet(url);
		if (null == urlConn)
			return null;
		inputS = urlConn.getInputStream();
		byte[] data = StreamUtils.readStream(inputS);
		json = new String(data);
		inputS.close();
		return json;
	}

	/**
	 * 通过url发送get请求，并得到返回结果
	 */
	public static String queryStringByGet(String url) {
		String resultString = null;
		// 获取HttpPost实例
		HttpGet request = getHttpGet(url);
		try {
			HttpResponse response = getHttpResponseByGet(request);
			// 判断是否请求成功
			if (200 == response.getStatusLine().getStatusCode()) {
				resultString = EntityUtils.toString(response.getEntity());
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			resultString = "connection error";
		} catch (IOException e) {
			e.printStackTrace();
			resultString = "connection error";
		}

		return resultString;
	}

	/**
	 * 通过HttpGet对象发送请求，获取请求结果
	 */
	public static String queryStringByGet(HttpGet request) {
		String resultString = null;

		try {
			HttpResponse response = getHttpResponseByGet(request);

			// 判断是否请求成功
			if (200 == response.getStatusLine().getStatusCode()) {
				resultString = EntityUtils.toString(response.getEntity());
			}

		} catch (ClientProtocolException e) {
			e.printStackTrace();
			resultString = "connection error";
		} catch (IOException e) {
			e.printStackTrace();
			resultString = "connection error";
		}
		return resultString;
	}

	static String keyWord = null;

	// 拼接搜索接口
	public static String spliceSearchURL(String str) throws UnsupportedEncodingException {
		keyWord = URLEncoder.encode(str, "UTF-8");
		String searchURL = ROOT_URL + search_URL + keyWord + "/1/";
		return searchURL;
	}

	// 拼接提示接口
	public static String spliceSuggestURL(String str) throws UnsupportedEncodingException {
		keyWord = URLEncoder.encode(str, "UTF-8");
		String suggestURL = ROOT_URL + suggest_URL + keyWord;
		return suggestURL;
	}

	// 拼接热门搜索词接口
	public static String spliceHotwordsURL() {
		String hotwordURL = ROOT_URL + hotwords_URL;
		return hotwordURL;
	}

}
