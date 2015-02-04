package com.ismartv.api.t;

import android.content.Context;
import android.util.Log;

import com.ismartv.api.AESDemo;
import com.ismartv.bean.ClipInfo;
import com.pplive.android.player.PlayCodeUtil;
import com.qiyi.video.player.data.Definition;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;

import tv.ismar.daisy.qiyimediaplayer.SdkVideo;

public class AccessProxy {

	private static String myDeviceType = "";
	private static String myDeviceVersion = "";
	private static String mySN = "";
	private static String userAgent = "";
	private static final String token = "?access_token=";
	private static final String key = "&sign=";
	private static final String keyCrypt = "smartvdefaultkey";
	private static String result = null;

	public static void init(String deviceType, String deviceVersion, String sn) {
		if (deviceType != null)
			myDeviceType = deviceType.replace(" ", "_");
		if (deviceVersion != null)
			myDeviceVersion = deviceVersion.replace(" ", "_");
		if (sn != null)
			mySN = sn;
		userAgent = (new StringBuilder(String.valueOf(myDeviceType)))
				.append("/").append(myDeviceVersion).append(" ").append(mySN)
				.append(" thirdpartyid").toString();
	}

	public static ClipInfo parse(String clipUrl, String access_token,
			Context context) {
		getStream(getFullUrl(clipUrl, access_token));
		return jsonToObject(result);
	}

	public static String getvVideoClipInfo() {
		return result;
	}
	public static ClipInfo getIsmartvClipInfo(String content) {
		return jsonToObject(content);
	}
    public static SdkVideo getQiYiInfo(String content){
    	SdkVideo qiyiInfo = null;	
    	JSONObject json;
    	try {
    		json = new JSONObject(content);
			String info = json.getString("iqiyi_4_0");
			String[] array = info.split(":");
    		qiyiInfo = new SdkVideo(array[0], array[1], array[2],Definition.DEFINITON_1080P);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return qiyiInfo;
    }
	public static String getVideoInfo(String clipUrl, String access_token) {
		getStream(getFullUrl(clipUrl, access_token));
		JSONObject json;
		String info="";
		try {
			json = new JSONObject(result);
			if(json.has("iqiyi_4_0")){
				return "iqiyi";
			}
			else{
				info = "ismartv";
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return info;
	}

	public static void destroy() {
	}

	private static String getFullUrl(String url, String access_token) {
		String full_url = (new StringBuilder(String.valueOf(url))).append(mySN)
				.append("/").toString();
		if (access_token != null)
			full_url = (new StringBuilder(String.valueOf(full_url)))
					.append("?access_token=").append(access_token)
					.append("&sign=").append(getAES(access_token)).toString();
		return full_url;
	}

	private static String getAES(String access_token) {
		String result = null;
		String contents = (new StringBuilder(String.valueOf((new Date())
				.getTime()))).append(mySN).toString();
		if (access_token != null && access_token.length() > 0) {
			if (access_token.length() > 15) {
				result = AESDemo.encrypttoStr(contents,
						access_token.substring(0, 16));
			} else {
				int leng = 16 - access_token.length();
				for (int i = 0; i < leng; i++)
					access_token = (new StringBuilder(
							String.valueOf(access_token))).append("0")
							.toString();

				result = AESDemo.encrypttoStr(contents,
						access_token.substring(0, 16));
			}
		} else {
			result = AESDemo.encrypttoStr(contents, keyCrypt);// 1422928853725001122334455
		}
		return result;
	}

	private static void getStream(String full_url) {
		int i = 0;
		HttpURLConnection httpConn = null;
		InputStreamReader inputStreamReader = null;
		do
			try {
				URL connURL = new URL(full_url);
				httpConn = (HttpURLConnection) connURL.openConnection();
				httpConn.setRequestProperty("Accept", "application/json");
				httpConn.setRequestProperty("User-Agent", userAgent);
				httpConn.setConnectTimeout(10000);
				httpConn.connect();
				inputStreamReader = new InputStreamReader(
						httpConn.getInputStream(), "UTF-8");
				result = readJSONString(inputStreamReader);
				inputStreamReader.close();
				httpConn.disconnect();
				break;
			} catch (Exception e) {
				i++;
				Log.e("Exception", e.toString());
			}
		while (i < 3);
	}

	private static String readJSONString(InputStreamReader requestReader) {
		StringBuffer json = new StringBuffer();
		String line = null;
		try {
			BufferedReader reader = new BufferedReader(requestReader);
			while ((line = reader.readLine()) != null)
				json.append(line);
		} catch (Exception e) {
			System.out.println(e.toString());
		}
		return json.toString();
	}

	private static ClipInfo jsonToObject(String myjson) {
		ClipInfo ci = null;
		if (myjson == null || myjson.length() <= 0)
			return ci;
		JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(myjson);
			ci = new ClipInfo();
			ci.setAdaptive(getURLStr(jsonObject.getString("adaptive")));
			ci.setHigh(getURLStr(jsonObject.getString("high")));
			ci.setLow(getURLStr(jsonObject.getString("low")));
			ci.setUltra(getURLStr(jsonObject.getString("ultra")));
			ci.setMedium(getURLStr(jsonObject.getString("medium")));
			ci.setNormal(getURLStr(jsonObject.getString("normal")));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return ci;
	}

	private static String getURLStr(String url) {
		if (url != null && url != "null") {
			if (url.startsWith("ppvod")) {
				url = pptvplay(url);
				return url;
			}
			url.startsWith("uusee");
		} else {
			return null;
		}
		return url;
	}

	private static String pptvplay(String url) {
		String uri = null;
		if (url == null || url == "null")
			return uri;
		uri = PlayCodeUtil.getVideoUrlM3u8(url);
		return uri;
	}
}