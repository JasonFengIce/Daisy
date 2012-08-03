package tv.ismar.daisy.core;

import tv.ismar.daisy.models.Attribute;
import tv.ismar.daisy.models.ContentModelList;
import tv.ismar.daisy.models.Item;
import tv.ismar.daisy.models.ItemList;
import tv.ismar.daisy.models.SectionList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class SimpleRestClient {
	public String root_url = "http://cord.tvxio.com";

	private Gson gson;

	public SimpleRestClient() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(Attribute.class, new AttributeDeserializer());
		gson = gsonBuilder.create();
	}

	public ContentModelList getContentModelLIst(String url) {
		String jsonStr = NetworkUtils.getJsonStr(root_url + url);
		return gson.fromJson(jsonStr, ContentModelList.class);
	}

	public SectionList getSections(String url) {
		String jsonStr = NetworkUtils.getJsonStr(url);
		SectionList list = gson.fromJson(jsonStr, SectionList.class);
		return list;
	}

	public ItemList getItemList(String url) {
		String jsonStr = NetworkUtils.getJsonStr(url);
		ItemList list = gson.fromJson(jsonStr, ItemList.class);
		return list;
	}

	public Item getItem(String url) {
		String jsonStr = NetworkUtils.getJsonStr(url);
		// Log.d("Item is", jsonStr);

		return gson.fromJson(jsonStr, Item.class);
	}

	public Item[] getRelatedItem(String api) {
		String jsonStr = NetworkUtils.getJsonStr(root_url + api);
		return gson.fromJson(jsonStr, Item[].class);
	}
	
}
