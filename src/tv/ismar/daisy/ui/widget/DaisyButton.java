package tv.ismar.daisy.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;

/**
 * Created by huaijie on 1/9/15.
 */
public class DaisyButton extends Button {
    private static final String TAG = "SakuraButton";

    public DaisyButton(Context context) {
        super(context);
    }

    public DaisyButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DaisyButton(Context context, AttributeSet attrs, int defStyle) {
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

}
