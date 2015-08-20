package tv.ismar.daisy.qiyimediaplayer;

import android.view.ViewGroup;
public class MediaPlayView {

	private MediaPlayView instance;


	public MediaPlayView initPlayer(String flag,ViewGroup container){
		if(flag.equals("qiyi")){
			instance = new QiyiMediaPlayer(container);
		}
		else{
			instance = new IsmartvPlayer(container);
		}
		return instance;			
	}
	
	public void setVideoPath(String path){
		
	}
}
