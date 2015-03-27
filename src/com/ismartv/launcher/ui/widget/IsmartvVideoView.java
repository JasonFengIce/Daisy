package com.ismartv.launcher.ui.widget;

import tv.ismar.daisy.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.VideoView;


public class IsmartvVideoView extends VideoView {

    public IsmartvVideoView(Context context) {
        super(context);
    }

    public IsmartvVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IsmartvVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(0, widthMeasureSpec);
        int height = getDefaultSize(0, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }
    @Override
    protected boolean dispatchHoverEvent(MotionEvent event) {
    	// TODO Auto-generated method stub
    	   if ((event.getAction() == MotionEvent.ACTION_HOVER_ENTER ) || (event.getAction() == MotionEvent.ACTION_HOVER_MOVE )) {
               setFocusableInTouchMode(true);
               setFocusable(true);
               requestFocusFromTouch();
               requestFocus();
           } else {
               clearFocus();
           }
          
    	return true;
    }
}
