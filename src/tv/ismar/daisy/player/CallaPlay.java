package tv.ismar.daisy.player;

import java.util.HashMap;

import tv.ismar.daisy.core.NetworkUtils;
import android.os.AsyncTask;

public class CallaPlay {

	private HashMap<String,Object> properties =  new HashMap<String,Object>();
	private String eventName="";
	/**
	 * 播放器打开 video_start 
	 * 
	 * @param item(媒体id) INTEGER
	 * @param subitem(子媒体id, 可空) INTEGER
	 * @param title(名称) STRING
	 * @param clip (视频id) INTEGER
	 * @param quality(视频清晰度: normal |  medium | high | ultra | adaptive) STRING 
	 * @param userid (用户ID) STRING
	 * @param speed (网速, 单位Kbits/s) INTEGER
	 * @return HashMap<String,Object>
	 */
	public  HashMap<String,Object> videoStart(Integer item,Integer subitem,String title,Integer clip,Integer quality,Integer userid,Integer speed){
		
		HashMap<String,Object> tempMap =  new HashMap<String,Object>();
		tempMap.put("item", item);
		if(subitem!=null)
			tempMap.put("subitem", subitem);
		tempMap.put("title", title);
		tempMap.put("clip", clip);
		tempMap.put("quality", switchQuality(quality));
		tempMap.put("userid", userid);
		eventName =	NetworkUtils.VIDEO_START;
		properties = tempMap;
		new LogTask().execute();
		return properties;
	}
	
	/**
	 * 开始播放缓冲结束 video_play_load
	 * 
	 * @param item(媒体id) INTEGER
	 * @param subitem(子媒体id, 可空) INTEGER
	 * @param title(名称) STRING
	 * @param clip (视频id) INTEGER
	 * @param quality(视频清晰度: normal |  medium | high | ultra | adaptive | adaptive_norma l | adaptive_medium | adaptive_high | adaptive_ultra) STRING 
	 * @param duration(缓存时间, 单位s) INTEGER
	 * @param speed (网速, 单位Kbits/s) INTEGER 
	 * @param mediaip（媒体IP）STRING
	 * @return HashMap<String,Object>
	 */
	public  HashMap<String,Object> videoPlayLoad(Integer item,Integer subitem,String title,Integer clip,Integer quality,Integer duration,Integer speed ,String mediaip){
		
		HashMap<String,Object> tempMap =  new HashMap<String,Object>();
		tempMap.put("item", item);
		if(subitem!=null)
			tempMap.put("subitem", subitem);
		tempMap.put("title", title);
		tempMap.put("clip", clip);
		tempMap.put("quality", switchQuality(quality));
		tempMap.put("duration", duration);
		//tempMap.put("speed", speed);
		tempMap.put("mediaip",mediaip);
		eventName =	NetworkUtils.VIDEO_PLAY_LOAD;
		properties = tempMap;
		new LogTask().execute();
		return tempMap;
		
	}
	
	/**
	 * 切换码流 video_switch_stream
	 * 
	 * @param item(媒体id) INTEGER
	 * @param subitem(子媒体id, 可空) INTEGER
	 * @param title(名称) STRING
	 * @param clip (视频id) INTEGER
	 * @param quality(视频清晰度: normal |  medium | high | ultra | adaptive | adaptive_norma l | adaptive_medium | adaptive_high | adaptive_ultra) STRING
	 * @param mode(切换模式：auto | manual) STRING
	 * @param userid(用户ID) STRING
	 * @param speed (网速, 单位Kbits/s) INTEGER 
	 * @param mediaip（媒体IP）STRING
	 * @return HashMap<String,Object>
	 */
	
	public  HashMap<String,Object> videoSwitchStream(Integer item,Integer subitem,String title,Integer clip,Integer quality,String mode,String userid,Integer speed ,String mediaip){
		
		HashMap<String,Object> tempMap =  new HashMap<String,Object>();
		tempMap.put("item", item);
		if(subitem!=null)
			tempMap.put("subitem", subitem);
		tempMap.put("title", title);
		tempMap.put("clip", clip);
		tempMap.put("quality", switchQuality(quality));
		tempMap.put("mode", mode);
		tempMap.put("userid", userid);
		//tempMap.put("speed", speed);
		tempMap.put("mediaip",mediaip);
		eventName =	NetworkUtils.VIDEO_SWITCH_STREAM;
		properties = tempMap;
		new LogTask().execute();
		return tempMap;
		
	}
	
	
	/**
	 * 
	 * 开始播放 video_play_start
	 * 
	 * @param item(媒体id) INTEGER
	 * @param subitem(子媒体id, 可空) INTEGER
	 * @param title(名称) STRING
	 * @param clip (视频id) INTEGER
	 * @param quality(视频清晰度: normal |  medium | high | ultra | adaptive) STRING
	 * @param speed (网速, 单位Kbits/s) INTEGER
	 * @return HashMap<String,Object>
	 */
	
	public  HashMap<String,Object> videoPlayStart(Integer item,Integer subitem,String title,Integer clip,Integer quality,Integer speed ){
		
		HashMap<String,Object> tempMap =  new HashMap<String,Object>();
		tempMap.put("item", item);
		if(subitem!=null)
			tempMap.put("subitem", subitem);
		tempMap.put("title", title);
		tempMap.put("clip", clip);
		tempMap.put("quality", switchQuality(quality));
		//tempMap.put("speed", speed);
		eventName =	NetworkUtils.VIDEO_PLAY_START;
		properties = tempMap;
		new LogTask().execute();
		return tempMap;
		
	}
	
	/**
	 * 
	 *	播放暂停 video_play_pause
	 * 
	 * @param item(媒体id) INTEGER
	 * @param subitem(子媒体id, 可空) INTEGER
	 * @param title(名称) STRING
	 * @param clip (视频id) INTEGER
	 * @param quality(视频清晰度: normal |  medium | high | ultra | adaptive) STRING
	 * @param position(位置，单位s) INTEGER
	 * @param speed (网速, 单位Kbits/s) INTEGER
	 * @return HashMap<String,Object>
	 */
	public  HashMap<String,Object> videoPlayPause(Integer item,Integer subitem,String title,Integer clip,String quality,Integer speed,Integer position){
		
		HashMap<String,Object> tempMap =  new HashMap<String,Object>();
		tempMap.put("item", item);
		if(subitem!=null)
			tempMap.put("subitem", subitem);
		tempMap.put("title", title);
		tempMap.put("clip", clip);
		tempMap.put("quality", quality);
		tempMap.put("position", position);
		//tempMap.put("speed", speed);
		eventName =	NetworkUtils.VIDEO_PLAY_PAUSE;
		properties = tempMap;
		new LogTask().execute();
		return tempMap;
		
	}
	
	/**
	 * 	播放继续 video_play_continue
	 *	@param item(媒体id) INTEGER
	 *	@param subitem(子媒体id, 可空) INTEGER
	 *	@param title(名称) STRING
	 *	@param clip (视频id) INTEGER
	 *	@param quality(视频清晰度: normal |  medium | high | ultra | adaptive) STRING
	 *	@param position(位置，单位s) INTEGER
	 *	@param speed (网速, 单位Kbits/s) INTEGER
	 *	@return HashMap<String,Object>
	 */
	
	public  HashMap<String,Object> videoPlayContinue(Integer item,Integer subitem,String title,Integer clip,Integer quality,Integer speed,Integer position ){
			
			HashMap<String,Object> tempMap =  new HashMap<String,Object>();
			tempMap.put("item", item);
			if(subitem!=null)
				tempMap.put("subitem", subitem);
			tempMap.put("title", title);
			tempMap.put("clip", clip);
			tempMap.put("quality", switchQuality(quality));
			//tempMap.put("speed", speed);
			tempMap.put("position", position/1000);
			eventName =	NetworkUtils.VIDEO_PLAY_CONTINUE;
			properties = tempMap;
			new LogTask().execute();
			return tempMap;
			
		}
	
	/**
	 * 
	 * 播放快进/快退 video_play_seek
	 * @param item(媒体id) INTEGER
	 * @param subitem(子媒体id, 可空) INTEGER
	 * @param title(名称) STRING
	 * @param clip (视频id) INTEGER
	 * @param quality(视频清晰度: normal |  medium | high | ultra | adaptive) STRING
	 * @param position(目标位置, 单位s) INTEGER
	 * @param speed (网速, 单位Kbits/s) INTEGER
	 * @return HashMap<String,Object>
	 */
	
	public  HashMap<String,Object> videoPlaySeek(Integer item,Integer subitem,String title,Integer clip,Integer quality,Integer speed,Integer position ){
		
		HashMap<String,Object> tempMap =  new HashMap<String,Object>();
		tempMap.put("item", item);
		if(subitem!=null)
			tempMap.put("subitem", subitem);
		tempMap.put("title", title);
		tempMap.put("clip", clip);
		tempMap.put("quality", switchQuality(quality));
		//tempMap.put("speed", speed);
		tempMap.put("position", position/1000);
		eventName =	NetworkUtils.VIDEO_PLAY_SEEK;
		properties = tempMap;
		new LogTask().execute();
		return tempMap;
		
	}

	/**
	 * 
	 * 播放快进/快退缓冲结束 video_play_seek_blockend
	 * @param item (媒体id) INTEGER
	 * @param subitem(子媒体id, 可空) INTEGER
	 * @param title(名称) STRING
	 * @param clip (视频id) INTEGER
	 * @param quality(视频清晰度: normal |  medium | high | ultra | adaptive | adaptive_norma l | adaptive_medium | adaptive_high | adaptive_ultra) STRING
	 * @param position(缓冲位置，单位s) INTEGER
	 * @param speed (网速, 单位Kbits/s) INTEGER 
	 * @param duration(缓存时间, 单位s) INTEGER
	 * @param mediaip（媒体IP）STRING
	 * @return HashMap<String,Object>
	 */
	
	public  HashMap<String,Object> videoPlaySeekBlockend(Integer item,Integer subitem,String title,Integer clip,Integer quality,Integer speed,Integer position,Integer duration,String mediaip ){
		
		HashMap<String,Object> tempMap =  new HashMap<String,Object>();
		tempMap.put("item", item);
		if(subitem!=null)
			tempMap.put("subitem", subitem);
		tempMap.put("title", title);
		tempMap.put("clip", clip);
		tempMap.put("quality", switchQuality(quality));
		//tempMap.put("speed", speed);
		tempMap.put("position", position/1000);
		eventName =	NetworkUtils.VIDEO_PLAY_SEEK_BLOCKEND;
		properties = tempMap;
		new LogTask().execute();
		return tempMap;
		
	}
	
	/**
	 * 
	 * 播放缓冲结束 video_play_blockend
	 * @param item (媒体id) INTEGER
	 * @param subitem(子媒体id, 可空) INTEGER
	 * @param title(名称) STRING
	 * @param clip (视频id) INTEGER
	 * @param quality(视频清晰度: normal |  medium | high | ultra | adaptive | adaptive_normal | adaptive_medium | adaptive_high | adaptive_ultra) STRING
	 * @param position(缓冲位置，单位s) INTEGER
	 * @param speed (网速, 单位Kbits/s) INTEGER 
	 * @param duration(缓存时间, 单位s) INTEGER 
	 * @param mediaip（媒体IP）STRING
	 * @return HashMap<String,Object> 
	 */
	public  HashMap<String,Object> videoPlayBlockend(Integer item,Integer subitem,String title,Integer clip,Integer quality,Integer speed,Integer position,long duration,String mediaip ){
		
		HashMap<String,Object> tempMap =  new HashMap<String,Object>();
		tempMap.put("item", item);
		if(subitem!=null)
			tempMap.put("subitem", subitem);
		tempMap.put("title", title);
		tempMap.put("clip", clip);
		tempMap.put("quality", switchQuality(quality));
		//tempMap.put("speed", speed);
		tempMap.put("position", position/1000);
		tempMap.put("duration", duration);
		tempMap.put("mediaip", mediaip);
		eventName =	NetworkUtils.VIDEO_PLAY_BLOCKEND;
		properties = tempMap;
		new LogTask().execute();
		return tempMap;
		
	}
	
	/**
	 * 播放时网速 video_play_speed
	 * 
	 * @param item (媒体id) INTEGER
	 * @param subitem (子媒体id, 可空) INTEGER
	 * @param title (名称) STRING
	 * @param clip (视频id) INTEGER
	 * @param quality (视频清晰度: normal |  medium | high | ultra | adaptive) STRING
	 * @param speed (网速, 单位Kbits/s) INTEGER
	 * @param mediaip (媒体IP) STRING
	 * @return HashMap<String,Object> 
	 */
	
	
	public  HashMap<String,Object> videoPlaySpeed(Integer item,Integer subitem,String title,Integer clip,Integer quality,Integer speed,String mediaip ){
		
		HashMap<String,Object> tempMap =  new HashMap<String,Object>();
		tempMap.put("item", item);
		if(subitem!=null)
			tempMap.put("subitem", subitem);
		tempMap.put("title", title);
		tempMap.put("clip", clip);
		tempMap.put("quality", switchQuality(quality));
		//tempMap.put("speed", speed);
		tempMap.put("mediaip", mediaip);
		eventName =	NetworkUtils.VIDEO_PLAY_SEEK;
		properties = tempMap;
		new LogTask().execute();
		return tempMap;
		
	}
	
	/**
	 * 播放时下载速度慢  video_low_speed
	 * 
	 * @param item (媒体id) INTEGER
	 * @param subitem (子媒体id, 可空) INTEGER
	 * @param title (名称) STRING
	 * @param clip (视频id) INTEGER
	 * @param quality (视频清晰度: normal |  medium | high | ultra | adaptive) STRING
	 * @param speed (网速, 单位Kbits/s) INTEGER
	 * @param mediaip (媒体IP) STRING
	 * @return HashMap<String,Object> 
	 */
	
	public  HashMap<String,Object> videoLowSpeed(Integer item,Integer subitem,String title,Integer clip,Integer quality,Integer speed,String mediaip ){
		
		HashMap<String,Object> tempMap =  new HashMap<String,Object>();
		tempMap.put("item", item);
		if(subitem!=null)
			tempMap.put("subitem", subitem);
		tempMap.put("title", title);
		tempMap.put("clip", clip);
		tempMap.put("quality", switchQuality(quality));
		//tempMap.put("speed", speed);
		tempMap.put("mediaip", mediaip);
		eventName =	NetworkUtils.VIDEO_PLAY_SPEED;
		properties = tempMap;
		new LogTask().execute();
		return tempMap;
		
	}
	/**
	 * 播放器退出 video_exit
	 * 
	 * @param item (媒体id) INTEGER
	 * @param subitem (子媒体id, 可空) INTEGER
	 * @param title (名称) STRING
	 * @param clip (视频id) INTEGER
	 * @param quality (视频清晰度: normal |  medium | high | ultra | adaptive) STRING
	 * @param speed (网速, 单位Kbits/s) INTEGER
	 * @param to(去向：detail | end) STRING
	 * @return HashMap<String,Object>
	 */
	
	public  HashMap<String,Object> videoExit(Integer item,Integer subitem,String title,Integer clip,Integer quality,Integer speed,String to){
		
		HashMap<String,Object> tempMap =  new HashMap<String,Object>();
		tempMap.put("item", item);
		if(subitem!=null)
			tempMap.put("subitem", subitem);
		tempMap.put("title", title);
		tempMap.put("clip", clip);
		tempMap.put("quality", switchQuality(quality));
		//tempMap.put("speed", speed);
		tempMap.put("to", to);
		eventName =	NetworkUtils.VIDEO_EXIT;
		properties = tempMap;
		new LogTask().execute();
		return tempMap;

	}
	
	/**
	 * 
	 * 播放缓冲结束 videoExcept
	 * @param code(异常码servertimeout|servertimeout|noplayaddress|mediaexception|mediatimeout|filenotfound|nodetail|debuggingexception|noextras) STRING
	 * @param content(异常内容) STRING
	 * @param item (媒体id) INTEGER
	 * @param subitem(子媒体id, 可空) INTEGER
	 * @param title(名称) STRING
	 * @param clip (视频id) INTEGER
	 * @param quality(视频清晰度: normal |  medium | high | ultra | adaptive | adaptive_normal | adaptive_medium | adaptive_high | adaptive_ultra) STRING
	 * @param position(播放位置，单位s) INTEGER
	 * @param speed (网速, 单位Kbits/s) INTEGER 
	 * @param mediaip（媒体IP）STRING
	 * @return HashMap<String,Object> 
	 */
	
	public  HashMap<String,Object> videoExcept(String code,String content,Integer item,Integer subitem,String title,Integer clip,Integer quality,Integer position){
		HashMap<String,Object> tempMap =  new HashMap<String,Object>();
		tempMap.put("code", code);
		tempMap.put("content", content);
		tempMap.put("item", item);
		if(subitem!=null)
			tempMap.put("subitem", subitem);
		tempMap.put("title", title);
		tempMap.put("clip", clip);
		tempMap.put("position", position/1000);
		tempMap.put("quality", switchQuality(quality));
		//tempMap.put("speed", speed);
		eventName =	NetworkUtils.VIDEO_EXCEPT;
		properties = tempMap;
		new LogTask().execute();
		return tempMap;
	}
	
	private  String switchQuality(Integer currQuality)
	{
		String quality ="";
		switch (currQuality) {
		case 0:
			quality = "normal";
			break;
		case 1:
			quality = "medium";
			break;
		case 2:
			quality = "high";
			break;
		case 3:
			quality = "adaptive";
			break;
		default:
			quality = "";
			break;
		}
		return quality;
	}
	
	private class LogTask extends AsyncTask<String, Void,Boolean> {
		
		@Override
		protected void onPostExecute(Boolean result) {
			
		}

		@Override
		protected Boolean doInBackground(String... params) {
			return NetworkUtils.LogSender(eventName, properties);
		}
		
	}
}
