package tv.ismar.daisy.core;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class NetworkUtils {
	private static String UA = "A11/V1 Unknown";
	
	public static String getJsonStr(String target) {
		String urlStr = target;
		try {
			URL url = new URL(urlStr);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			StringBuffer sb = new StringBuffer();
			conn.addRequestProperty("User-Agent", UA);
			conn.addRequestProperty("Accept", "application/json");
			conn.connect();

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
			e.printStackTrace();
		}
		
		
		return null;
	}
	
	public static InputStream getInputStream(String target) {
		String urlStr = target;
		try {
			URL url = new URL(urlStr);
			URLConnection conn = url.openConnection();
<<<<<<< HEAD
			conn.addRequestProperty("User-Agent", UA);
			conn.addRequestProperty("Accept", "application/json");
			conn.connect();
			return conn.getInputStream();
=======
			StringBuffer sb = new StringBuffer();
			conn.addRequestProperty("User-Agent", UA);
			conn.addRequestProperty("Accept", "application/json");
			conn.connect();
			
			return conn.getInputStream();
			
>>>>>>> 15fbd302918113623af431d68ef3c106bd33b6fb
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		return null;
	}
}
