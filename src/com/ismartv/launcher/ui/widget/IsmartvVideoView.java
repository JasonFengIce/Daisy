package com.ismartv.launcher.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

/**
 * Created by <huaijiefeng@gmail.com> on 9/9/14.
 */
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
}
