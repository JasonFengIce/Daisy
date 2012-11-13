package tv.ismar.daisy.player;

import java.util.HashMap;

import tv.ismar.daisy.core.CallaSender;
import tv.ismar.daisy.core.NetworkUtils;

public class CallaPlay {

	private static CallaSender csender = CallaSender.getInstance();
	
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
	
	
	public static HashMap<String,Object> videoStart(Integer item,Integer subitem,String title,Integer clip,String quality,Integer userid,Integer speed){
		
		HashMap<String,Object> tempMap =  new HashMap<String,Object>();
		tempMap.put("item", item);
		tempMap.put("subitem", subitem);
		tempMap.put("title", title);
		tempMap.put("clip", clip);
		tempMap.put("quality", quality);
		tempMap.put("userid", userid);
		tempMap.put("speed", speed);
		NetworkUtils.LogSender(NetworkUtils.VIDEO_START, tempMap);
		
		return tempMap;
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
	public static HashMap<String,Object> videoPlayLoad(Integer item,Integer subitem,String title,Integer clip,String quality,Integer duration,Integer speed ,String mediaip){
		
		HashMap<String,Object> tempMap =  new HashMap<String,Object>();
		tempMap.put("item", item);
		tempMap.put("subitem", subitem);
		tempMap.put("title", title);
		tempMap.put("clip", clip);
		tempMap.put("quality", quality);
		tempMap.put("duration", duration);
		tempMap.put("speed", speed);
		tempMap.put("mediaip",mediaip);
		NetworkUtils.LogSender(NetworkUtils.VIDEO_PLAY_LOAD, tempMap);
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
	
	public static HashMap<String,Object> videoSwitchStream(Integer item,Integer subitem,String title,Integer clip,String quality,String mode,String userid,Integer speed ,String mediaip){
		
		HashMap<String,Object> tempMap =  new HashMap<String,Object>();
		tempMap.put("item", item);
		tempMap.put("subitem", subitem);
		tempMap.put("title", title);
		tempMap.put("clip", clip);
		tempMap.put("quality", quality);
		tempMap.put("mode", mode);
		tempMap.put("userid", userid);
		tempMap.put("speed", speed);
		tempMap.put("mediaip",mediaip);
		NetworkUtils.LogSender(NetworkUtils.VIDEO_SWITCH_STREAM, tempMap);
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
	
	public static HashMap<String,Object> videoPlayStart(Integer item,Integer subitem,String title,Integer clip,String quality,Integer speed ){
		
		HashMap<String,Object> tempMap =  new HashMap<String,Object>();
		tempMap.put("item", item);
		tempMap.put("subitem", subitem);
		tempMap.put("title", title);
		tempMap.put("clip", clip);
		tempMap.put("quality", quality);
		tempMap.put("speed", speed);
		csender.httpConn("video_play_start", tempMap);
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
	public static HashMap<String,Object> videoPlayPause(Integer item,Integer subitem,String title,Integer clip,String quality,Integer speed ){
		
		HashMap<String,Object> tempMap =  new HashMap<String,Object>();
		tempMap.put("item", item);
		tempMap.put("subitem", subitem);
		tempMap.put("title", title);
		tempMap.put("clip", clip);
		tempMap.put("quality", quality);
		tempMap.put("speed", speed);
		csender.httpConn("video_play_pause", tempMap);
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
	
	public static HashMap<String,Object> videoPlayContinue(Integer item,Integer subitem,String title,Integer clip,String quality,Integer speed,Integer position ){
			
			HashMap<String,Object> tempMap =  new HashMap<String,Object>();
			tempMap.put("item", item);
			tempMap.put("subitem", subitem);
			tempMap.put("title", title);
			tempMap.put("clip", clip);
			tempMap.put("quality", quality);
			tempMap.put("speed", speed);
			tempMap.put("position", position);
			csender.httpConn("video_play_continue", tempMap);
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
	
	public static HashMap<String,Object> videoPlaySeek(Integer item,Integer subitem,String title,Integer clip,String quality,Integer speed,Integer position ){
		
		HashMap<String,Object> tempMap =  new HashMap<String,Object>();
		tempMap.put("item", item);
		tempMap.put("subitem", subitem);
		tempMap.put("title", title);
		tempMap.put("clip", clip);
		tempMap.put("quality", quality);
		tempMap.put("speed", speed);
		tempMap.put("position", position);
		csender.httpConn("video_play_seek", tempMap);
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
	
	public static HashMap<String,Object> videoPlaySeekBlockend(Integer item,Integer subitem,String title,Integer clip,String quality,Integer speed,Integer position,Integer duration,String mediaip ){
		
		HashMap<String,Object> tempMap =  new HashMap<String,Object>();
		tempMap.put("item", item);
		tempMap.put("subitem", subitem);
		tempMap.put("title", title);
		tempMap.put("clip", clip);
		tempMap.put("quality", quality);
		tempMap.put("speed", speed);
		tempMap.put("position", position);
		tempMap.put("duration", duration);
		tempMap.put("mediaip", mediaip);
		csender.httpConn("video_play_seek_blockend", tempMap);
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
	public static HashMap<String,Object> videoPlayBlockend(Integer item,Integer subitem,String title,Integer clip,String quality,Integer speed,Integer position,Integer duration,String mediaip ){
		
		HashMap<String,Object> tempMap =  new HashMap<String,Object>();
		tempMap.put("item", item);
		tempMap.put("subitem", subitem);
		tempMap.put("title", title);
		tempMap.put("clip", clip);
		tempMap.put("quality", quality);
		tempMap.put("speed", speed);
		tempMap.put("position", position);
		tempMap.put("duration", duration);
		tempMap.put("mediaip", mediaip);
		csender.httpConn("video_play_blockend", tempMap);
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
	
	
	public static HashMap<String,Object> videoPlaySpeed(Integer item,Integer subitem,String title,Integer clip,String quality,Integer speed,String mediaip ){
		
		HashMap<String,Object> tempMap =  new HashMap<String,Object>();
		tempMap.put("item", item);
		tempMap.put("subitem", subitem);
		tempMap.put("title", title);
		tempMap.put("clip", clip);
		tempMap.put("quality", quality);
		tempMap.put("speed", speed);
		tempMap.put("mediaip", mediaip);
		csender.httpConn("video_play_speed", tempMap);
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
	
	public static HashMap<String,Object> videoLowSpeed(Integer item,Integer subitem,String title,Integer clip,String quality,Integer speed,String mediaip ){
		
		HashMap<String,Object> tempMap =  new HashMap<String,Object>();
		tempMap.put("item", item);
		tempMap.put("subitem", subitem);
		tempMap.put("title", title);
		tempMap.put("clip", clip);
		tempMap.put("quality", quality);
		tempMap.put("speed", speed);
		tempMap.put("mediaip", mediaip);
		csender.httpConn("video_low_speed", tempMap);
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
	
	public static HashMap<String,Object> videoExit(Integer item,Integer subitem,String title,Integer clip,String quality,Integer speed,String to){
		
		HashMap<String,Object> tempMap =  new HashMap<String,Object>();
		tempMap.put("item", item);
		tempMap.put("subitem", subitem);
		tempMap.put("title", title);
		tempMap.put("clip", clip);
		tempMap.put("quality", quality);
		tempMap.put("speed", speed);
		tempMap.put("to", to);
		csender.httpConn("video_exit", tempMap);
		return tempMap;

	}
	
	
}
