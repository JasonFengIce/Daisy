package tv.ismar.daisy.qiyimediaplayer;

import tv.ismar.daisy.views.IsmatvVideoView;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

public class IsmartvPlayer extends MediaPlayView{

	private IsmatvVideoView mVideoView;
	public IsmartvPlayer(ViewGroup container){
		mVideoView = new IsmatvVideoView(container.getContext());
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);
		lp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
		// btn1 位于父 View 的顶部，在父 View 中水平居中
		container.addView(mVideoView, lp);
	}
}
