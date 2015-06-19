package tv.ismar.daisy.core;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tv.ismar.daisy.data.HomePagerEntity;
import tv.ismar.daisy.exception.ItemOfflineException;
import tv.ismar.daisy.exception.NetworkException;
import tv.ismar.daisy.models.Attribute;
import tv.ismar.daisy.models.ChannelList;
import tv.ismar.daisy.models.ContentModelList;
import tv.ismar.daisy.models.Item;
import tv.ismar.daisy.models.ItemList;
import tv.ismar.daisy.models.SectionList;
import tv.ismar.daisy.models.SportsGameList;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

public class SimpleRestClient {
	// public String root_url = "http://cord.tvxio.com";
	// public String root_url = "http://127.0.0.1:21098/cord";

	// public static String sRoot_url = "http://127.0.0.1:21098/cord";

	public static String root_url = "";
//	public static String sRoot_url = "http://cord.tvxio.com/v2_0/A21/dto";
	public static String sRoot_url = "http://skytest.tvxio.com/v2_0/A21/dto";
    public static String ad_domain = "lilac.tvxio.com";
    public static String log_domain = "cord.tvxio.com";
    public static String device_token;
    public static String sn_token;
    public static String access_token="";
    public static String mobile_number="";
	private Gson gson;

	public SimpleRestClient() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(Attribute.class,
				new AttributeDeserializer());
		gson = gsonBuilder.create();
	}
    public Item[] getItems(String str){
    	return gson.fromJson(str, Item[].class);
    	
    }
    public Item getItemRecord(String str){
    	return gson.fromJson(str, Item.class);
    }
	public static String readContentFromPost(String url,String sn){
		StringBuffer response = new StringBuffer();
		 try{
	        URL postUrl = new URL("http://peach.tvxio.com/trust/"+url+"/");
	        HttpURLConnection connection = (HttpURLConnection) postUrl
	                .openConnection();
	        connection.setDoOutput(true);
	        connection.setDoInput(true);
	        connection.setRequestMethod("POST");
	        connection.setUseCaches(false);
	        connection.setInstanceFollowRedirects(true);
	        connection.setRequestProperty("Content-Type",
	                "application/x-www-form-urlencoded");
	        connection.setRequestProperty("Accept", "application/json");
	        connection.connect();
	        DataOutputStream out = new DataOutputStream(connection
	                .getOutputStream());
	        String content;
	        if(url.equals("active"))
	              content = "sn="+sn+"&kind=a21&"+"manufacture=lenovo&version=v2_0";
	          else
	        	  content = "sn="+sn+"&kind=a21&"+"manufacture=lenovo&api_version=v2_0";
	        out.writeBytes(content);
	        	        
	        out.flush();
	        out.close();
	        int status = connection.getResponseCode();
	        if(status==200){
	            BufferedReader reader = new BufferedReader(new InputStreamReader(
		                connection.getInputStream(),"UTF-8"));
		        out.flush();
		        out.close(); // flush and close
		        String line;
		        while ((line = reader.readLine()) != null) {
		            response.append(line);
		        }
		        reader.close();
		        if(url.equals("register")&&line==null){
			        connection.disconnect();
		        	return "200" ;
		        }		        	
	        }
	        else{
	        	connection.disconnect();
	        	return "";
	        }
		 }
		 catch(IOException e){
			 e.printStackTrace();
			 return "";
		 }
		 return response.toString();
	   }

	/**
	 * Extract the item id from given url, check whether the given url is an
	 * subitem.
	 * 
	 * @param url
	 *            is the valid url contains item id(or item pk)
	 * @param isSubItem
	 *            is an boolean array with a size of one, use to gain the result
	 *            of subitem check.
	 * @return a item id.
	 */
	public static int getItemId(String url, boolean[] isSubItem) {
		int id = 0;
		try {
			if (url.contains("/item/")) {
				isSubItem[0] = false;
			} else {
				isSubItem[0] = true;
			}
			Pattern p = Pattern.compile("/(\\d+)/?$");
			Matcher m = p.matcher(url);
			if (m.find()) {
				String idStr = m.group(1);
				if (idStr != null) {
					id = Integer.parseInt(idStr);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return id;
	}

	public ContentModelList getContentModelLIst(String url) {
		try {
			String jsonStr = NetworkUtils.getJsonStr(root_url + url);
			return gson.fromJson(jsonStr, ContentModelList.class);
		} catch (JsonSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ItemOfflineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NetworkException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public ContentModelList getContentModelList(InputStream in) {
		return gson.fromJson(new InputStreamReader(in), ContentModelList.class);
	}

	public ChannelList getChannelList() {
		try {
			String api = "/api/tv/channels/";
			String jsonStr = NetworkUtils.getJsonStr(root_url + api);
			return gson.fromJson(jsonStr, ChannelList.class);
		} catch (JsonSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ItemOfflineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NetworkException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public SectionList getSectionsByChannel(String channel)
			throws NetworkException {
		try {
			String url = root_url + "/api/tv/sections/" + channel + "/";
			String jsonStr = NetworkUtils.getJsonStr(url);
			SectionList list = gson.fromJson(jsonStr, SectionList.class);
			return list;
		} catch (JsonSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ItemOfflineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public HomePagerEntity getVaietyHome(String url)
			throws NetworkException {
		HomePagerEntity entity = null;
		try {
			String jsonStr = NetworkUtils.getJsonStr(url);
			entity = gson.fromJson(jsonStr, HomePagerEntity.class);
			return entity;
		} catch (JsonSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ItemOfflineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return entity;
	}

	public HomePagerEntity getSportHome(String url)
			throws NetworkException {
		HomePagerEntity entity = null;
		try {
			String jsonStr = NetworkUtils.getJsonStr(url);
			entity = gson.fromJson(jsonStr, HomePagerEntity.class);
			return entity;
		} catch (JsonSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ItemOfflineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return entity;
	}

	public SportsGameList getSportGames()
			throws NetworkException {
		SportsGameList entity = null;
		try {
//			String url = root_url + "/api/tv/homepage/zongyi/";
			String url = "http://skytest.tvxio.com" + "/api/tv/living_video/";
			String jsonStr = NetworkUtils.getJsonStr(url);
			entity = gson.fromJson(jsonStr, SportsGameList.class);
			return entity;
		} catch (JsonSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ItemOfflineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return entity;
	}

	public SectionList getSections(String url) throws NetworkException,
			ItemOfflineException {
		try {
			String jsonStr = NetworkUtils.getJsonStr(url);
			SectionList list = gson.fromJson(jsonStr, SectionList.class);
			return list;
		} catch (JsonSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
public SectionList getsectionss(String content){
	SectionList list = gson.fromJson(content, SectionList.class);
	return list;
}
	public ItemList getItemList(String url) throws NetworkException,
			ItemOfflineException {
		try {
			String jsonStr = NetworkUtils.getJsonStr(url);
			ItemList list = gson.fromJson(jsonStr, ItemList.class);
			return list;
		} catch (JsonSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public Item getItem(String url) throws ItemOfflineException,
			NetworkException, JsonSyntaxException {

		String jsonStr = NetworkUtils.getJsonStr(url);
		// Log.d("Item is", jsonStr);

		return gson.fromJson(jsonStr, Item.class);
	}

	public Item[] getRelatedItem(String api) throws NetworkException {
		try {
			String jsonStr = NetworkUtils.getJsonStr(root_url + api);
			return gson.fromJson(jsonStr, Item[].class);
		} catch (JsonSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ItemOfflineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	public void doSendRequest(String url,String method,String params,HttpPostRequestInterface l){
		//NetworkUtils.getJsonStrByPost(url, "");
		RequestParams q = new RequestParams();
		handler = l;
		if (!(url.contains("https") || url.contains("http"))){
			q.url = root_url + url;
		}else{
			q.url = url;
		}
		q.values = params;
		q.method = method;
		new GetDataTask().execute(q);
	}
    public void doTopicRequest(String url,String method,String params,HttpPostRequestInterface l){
        //NetworkUtils.getJsonStrByPost(url, "");
        RequestParams q = new RequestParams();
        handler = l;
        if (!(url.contains("https") || url.contains("http"))){
            q.url =  url;
        }else{
            q.url = url;
        }
        q.values = params;
        q.method = method;
        new GetDataTask().execute(q);
    }
	class GetDataTask extends AsyncTask<RequestParams, Void, String> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			if(handler!=null){
				handler.onPrepare();
			}
		}
		@Override
		protected String doInBackground(RequestParams... params) {
			String jsonStr = "";
			
				RequestParams p = params[0];
				String url = p.url;
				String values = p.values;
				String method = p.method;
				try {
					if("post".equalsIgnoreCase(method)){
					if (url.contains("https")) {
						jsonStr = NetworkUtils.httpsRequestHttps(url, values);
					} else {
						jsonStr = NetworkUtils.getJsonStrByPost(url, values);
					}
					}else{
						jsonStr = NetworkUtils.getJsonStr(url);	
					}
				} catch (ItemOfflineException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					jsonStr = e.getUrl();
				} catch (NetworkException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					jsonStr = e.getUrl();
				}
			
			return jsonStr;
		}

		@Override
		protected void onPostExecute(String result) {
			if(handler!=null&&result!=null){
				if("".equals(result)){
					handler.onFailed("网络异常");
				}
				else if("200".equals(result)){
					handler.onSuccess(result);
				}
				else if("406".equals(result)){
					handler.onFailed("device_token非标准格式 ");
				}
				else if("400".equals(result)){
					handler.onFailed("参数不对 ");
				}
				else if("404".equals(result)){
					handler.onFailed("404 NOT FOUND");
				}
				else if("599".equals(result)){
					handler.onFailed("599 连接错误");
				}
				else if(!"".equals(result)){
					handler.onSuccess(result);
				}
			}
		}

	}
	public class RequestParams{
		public String url;
		public String values;
		public String method;
	}
	public void setHttpPostRequestInterface(HttpPostRequestInterface l){
		handler = l;
	}
	private HttpPostRequestInterface handler;
	public interface HttpPostRequestInterface{
		public void onPrepare();
		public void onSuccess(String info);
		public void onFailed(String error);
	} 
	
	public static  boolean isLogin(){
		if("".equals(SimpleRestClient.access_token)){
			return false;
		}
		return true;
	}
}
