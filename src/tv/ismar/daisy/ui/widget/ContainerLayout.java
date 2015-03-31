package tv.ismar.daisy.ui.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * Created by huaijie on 2015/3/26.
 */
public class ContainerLayout extends RelativeLayout {
    private static final String TAG = "ContainerLayout";

    public ContainerLayout(Context context) {
        super(context);
    }

    public ContainerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ContainerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private OnItemHoverListener itemHoverListener;


    public interface OnItemHoverListener {
        public void onItemHover(View view);
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
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        if (gainFocus) {
            if (null == itemHoverListener) {
                Log.e(TAG, "itemHoverListener  not be null");
            } else {
                itemHoverListener.onItemHover(this);
            }
        }
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
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

    public void setItemHoverListener(OnItemHoverListener itemHoverListener) {
        this.itemHoverListener = itemHoverListener;
    }
}
