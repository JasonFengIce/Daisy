package tv.ismar.daisy.views;


import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewDebug.ExportedProperty;
import android.widget.TextView;

public class MyMarqueeText extends TextView {

	public MyMarqueeText(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	@Override
	@ExportedProperty(category = "focus")
	public boolean isFocused() {
		// TODO Auto-generated method stub
		return true;
	}
	
//	@Override
//	protected void onFocusChanged(boolean focused, int direction,
//			Rect previouslyFocusedRect) {
//		// TODO Auto-generated method stub
//		//super.onFocusChanged(focused, direction, previouslyFocusedRect);
//	}

}
