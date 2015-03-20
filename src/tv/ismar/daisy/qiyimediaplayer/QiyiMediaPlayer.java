package tv.ismar.daisy.qiyimediaplayer;

import java.util.List;

import com.qiyi.video.player.IVideoStateListener;
import com.qiyi.video.player.QiyiVideoPlayer;
import com.qiyi.video.player.data.Definition;
import com.qiyi.video.player.data.IPlaybackInfo;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

public class QiyiMediaPlayer extends MediaPlayView{
	private QiyiVideoPlayer mPlayer;
	public QiyiMediaPlayer(ViewGroup container){
	    RelativeLayout.LayoutParams rlParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        rlParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
 
        mPlayer = QiyiVideoPlayer.createVideoPlayer(container.getContext(), container, rlParams, /*bundle*/ null, mVideoStateListener);
	}
	@Override
	public void setVideoPath(String path) {
		// TODO Auto-generated method stub
		super.setVideoPath(path);
	}
	private IVideoStateListener mVideoStateListener = new IVideoStateListener(){

		@Override
		public void onAdEnd() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onAdStart() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onBitStreamListReady(List<Definition> arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onBufferEnd() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onBufferStart() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean onError(IPlaybackInfo arg0, int arg1, String arg2,
				String arg3) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void onHeaderTailerInfoReady(int arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onMovieComplete() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onMoviePause() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onMovieStart() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onMovieStop() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onPlaybackBitStreamSelected(Definition arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onPrepared() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onSeekComplete() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onVideoSizeChange(int arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}
		
	};
}