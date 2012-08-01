package tv.ismar.daisy.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Base64;
import android.util.Log;

public class CallaSender {

	private static final String TAG = "CallaSender";
	
	private static final String URL = "http://calla.tvxio.com/log";
	
	
	private static CallaSender instance = null;
	
    private CallaSender(){     
    	
    }     
    public static CallaSender getInstance(){     
        if (instance == null)     
            instance = new CallaSender();      
                return instance;      
    }     

	
	public void httpConn(String eventName,HashMap<String,Object> propertiesMap) {
		BufferedReader in = null;
		try {
			String jsonContent = getContentJson(eventName, propertiesMap);
			Log.d(TAG, eventName+" properties = " + jsonContent);
			jsonContent.replaceAll("-", "+");
			jsonContent.replaceAll("_", "/");
			String url=URL+"/A11/track/?date="+base64Code(jsonContent);
			java.net.URL connURL = new java.net.URL(url);
			java.net.HttpURLConnection httpConn = (java.net.HttpURLConnection) connURL.openConnection();
			httpConn.setDoOutput(true);
			httpConn.setDoInput(true); 
			httpConn.setRequestMethod("POST");
			httpConn.setConnectTimeout(10000);
			httpConn.setReadTimeout(10000);  
			httpConn.setRequestProperty("Accept", "application/json");
			httpConn.setRequestProperty("User-Agent", VodUserAgent.getUserAgent(VodUserAgent.getMACAddress()));
			httpConn.connect();
			Log.d(TAG, eventName+" SUCCESS ");
		} catch (MalformedURLException e) {
			Log.d(TAG, eventName +" MalformedURLException "+e.toString());
			e.printStackTrace();
		} catch (IOException e) {
			Log.d(TAG, eventName +" IOException "+e.toString());
			e.printStackTrace();
		} catch (Exception e) {
			Log.d(TAG, eventName +" Exception "+e.toString());
			e.printStackTrace();
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
}
