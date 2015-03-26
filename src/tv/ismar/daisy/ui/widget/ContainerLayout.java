package tv.ismar.daisy.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

/**
 * Created by huaijie on 2015/3/26.
 */
public class ContainerLayout extends RelativeLayout {
    public ContainerLayout(Context context) {
        super(context);
    }

    public ContainerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ContainerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onHoverChanged(boolean hovered) {
        super.onHoverChanged(true);
        if (hovered) {
            requestFocus();
        } else {
            clearFocus();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                performClick();
                return true;
            default:
                return super.dispatchTouchEvent(event);
        }

    }

}
