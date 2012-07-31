package tv.ismar.daisy.core;

import java.lang.reflect.Type;

import tv.ismar.daisy.models.Channel;
import tv.ismar.daisy.models.ChannelList;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class ChannelListDeserializer implements JsonDeserializer<ChannelList> {

	@Override
	public ChannelList deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		ChannelList channelList = new ChannelList();
		if(json.isJsonArray()){
			Log.d("json is a ","json Array");
			JsonArray jsonArray = json.getAsJsonArray();
			channelList = context.deserialize(jsonArray, ChannelList.class);
		}
		
		return channelList;
	}

}
