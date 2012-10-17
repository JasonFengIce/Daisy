package tv.ismar.daisy.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class DetailAttributeContainer extends LinearLayout {
	
	private int mMaxHeight;
	

	public DetailAttributeContainer(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public DetailAttributeContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public DetailAttributeContainer(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public void setMaxHeight(int max) {
		mMaxHeight = max;
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
	}

	
}
