package tv.ismar.daisy.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

/**
 * Created by huaijie on 3/17/15.
 */
public class DaisyImageView extends ImageView {
    private static final String TAG = "DaisyImageView";

    public DaisyImageView(Context context) {
        super(context);
    }

    public DaisyImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DaisyImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean dispatchHoverEvent(MotionEvent event) {


        if ((event.getAction() == MotionEvent.ACTION_HOVER_ENTER || event.getAction() == MotionEvent.ACTION_HOVER_MOVE) && isEnabled()) {
            setClickable(true);
            setFocusableInTouchMode(true);
            setFocusable(true);
            requestFocusFromTouch();
            requestFocus();
        }
        return false;
    }


    public void dispatchHoverEvent(MotionEvent event, boolean clearFocus) {
        if (isEnabled()) {

            if (clearFocus) {
                clearFocus();
            } else {
                dispatchHoverEvent(event);
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (isEnabled()) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    performClick();
                    break;
            }
        }

        return super.dispatchTouchEvent(event);
    }
}
