package tv.ismar.daisy.models;

import org.json.JSONException;
import org.json.JSONObject;

public class AdRequestElement {

	private String adpid = "";
	private String sn = "";
	private String modelName = "";
	private String version = "";
	private String accessToken = "";
	private String deviceToken = "";
	private String province = "";
	private String city = "";
	private String channel = "";
	private String section = "";
	private String itemid = "";
	private String topic = "";
	private String source = "";
	private String app = "";
	private String resolution = "";
	private String dpi = "";
	private String genre = "";
	private String content_model = "";
	private String director = "";
	private String actor = "";
	private String clipid = "";
	private String live_video = "";
	private String vendor = "";
	private String expense = "";
	private String length = "";

	public String getAdpid() {
		return adpid;
	}

	public void setAdpid(String adpid) {
		this.adpid = adpid;
	}

	public void setSn(String sn) {
		this.sn = sn;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public void setDeviceToken(String deviceToken) {
		this.deviceToken = deviceToken;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public void setSection(String section) {
		this.section = section;
	}

	public void setItemid(String itemid) {
		this.itemid = itemid;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public void setApp(String app) {
		this.app = app;
	}

	public void setResolution(String resolution) {
		this.resolution = resolution;
	}

	public void setDpi(String dpi) {
		this.dpi = dpi;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}

	public void setContent_model(String content_model) {
		this.content_model = content_model;
	}

	public void setDirector(String director) {
		this.director = director;
	}

	public void setActor(String actor) {
		this.actor = actor;
	}

	public void setClipid(String clipid) {
		this.clipid = clipid;
	}

	public void setLive_video(String live_video) {
		this.live_video = live_video;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	public void setExpense(String expense) {
		this.expense = expense;
	}

	public void setLength(String length) {
		this.length = length;
	}

	public String parseToJsonString() {
		JSONObject root = new JSONObject();
		try {
			root.put("adpid", "['"+adpid+"']");
			root.put("sn", sn);
			root.put("modelName", modelName);
			root.put("version", version);
			root.put("accessToken", accessToken);
			root.put("deviceToken", deviceToken);
			root.put("province", province);
			root.put("city", city);
			root.put("channel", channel);
			root.put("section", section);
			root.put("itemid", itemid);
			root.put("topic", topic);
			root.put("source", source);
			root.put("app", app);
			root.put("resolution", resolution);
			root.put("dpi", dpi);
			root.put("genre", genre);
			root.put("content_model", content_model);
			root.put("director", director);
			root.put("actor", actor);
			root.put("clipid", clipid);
			root.put("live_video", live_video);
			root.put("vendor", vendor);
			root.put("expense", expense);
			root.put("length", length);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return root.toString();
	}
}
