package tv.ismar.daisy.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.GridView;

/**
 * Created by huaijie on 2015/3/23.
 */
public class DaisyGridView extends GridView {
    private static final String TAG = DaisyGridView.class.getSimpleName();

    public DaisyGridView(Context context) {
        super(context);
    }

    public DaisyGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DaisyGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onHoverEvent(MotionEvent event) {

//        int position = pointToPosition((int) event.getX(), (int) event.getY());
//
//        if (-1 != position) {
//            requestFocus();
//            View view = getChildAt(position);
//            view.setFocusable(true);
//            view.setFocusableInTouchMode(true);
//            view.setClickable(true);
////            getChildAt(position).requestFocus();
//            setSelection(position);
////            requestFocus(position);
//        } else {
////            clearFocus();
//        }
        return super.onHoverEvent(event);
    }

}
