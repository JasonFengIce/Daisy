package com.ismartv.launcher.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.HorizontalScrollView;

/**
 * Created by <huaijiefeng@gmail.com> on 9/4/14.
 */
public class MyHorizontalView extends HorizontalScrollView {
    public MyHorizontalView(Context context) {
        super(context);
    }

    public MyHorizontalView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyHorizontalView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    @Override
    public boolean executeKeyEvent(KeyEvent event) {



        return super.executeKeyEvent(event);
    }
}
