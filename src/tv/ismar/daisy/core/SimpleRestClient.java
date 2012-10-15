package tv.ismar.daisy.core;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;

import tv.ismar.daisy.models.Attribute;
import tv.ismar.daisy.models.ChannelList;
import tv.ismar.daisy.models.ContentModelList;
import tv.ismar.daisy.models.Item;
import tv.ismar.daisy.models.ItemList;
import tv.ismar.daisy.models.SectionList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

public class SimpleRestClient {
//	public String root_url = "http://cord.tvxio.com";
	public String root_url = "http://127.0.0.1:21098/cord";

	private Gson gson;

	public SimpleRestClient() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(Attribute.class, new AttributeDeserializer());
		gson = gsonBuilder.create();
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
		}
		return null;
	}
	
	
	public ContentModelList getContentModelList(InputStream in) {
		return gson.fromJson(new InputStreamReader(in), ContentModelList.class);
	}
	
	public ChannelList getChannelList() {
		try {
			String api = "/api/tv/channels/";
			String jsonStr = NetworkUtils.getJsonStr(root_url+api);
			return gson.fromJson(jsonStr, ChannelList.class);
		} catch (JsonSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ItemOfflineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public SectionList getSectionsByChannel(String channel) {
		try {
			String url = root_url + "/api/tv/sections/"+channel+"/";
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
	
	public SectionList getSections(String url) {
		try {
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

	public ItemList getItemList(String url) {
		try {
			String jsonStr = NetworkUtils.getJsonStr(url);
			ItemList list = gson.fromJson(jsonStr, ItemList.class);
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

	public Item getItem(String url) throws ItemOfflineException {
		try {
			String jsonStr = NetworkUtils.getJsonStr(url);
			// Log.d("Item is", jsonStr);

			return gson.fromJson(jsonStr, Item.class);
		} catch (JsonSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public Item[] getRelatedItem(String api) {
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

}
