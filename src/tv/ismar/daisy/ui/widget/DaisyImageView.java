package tv.ismar.daisy.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;
import tv.ismar.daisy.R;

/**
 * Created by huaijie on 3/17/15.
 */
public class DaisyImageView extends ImageView {
    private static final String TAG = "DaisyImageView";

    private Drawable focusBg;
    private Drawable normalBg;

    public DaisyImageView(Context context) {
        super(context);
    }

    public DaisyImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.DaisyImageView);
        focusBg = typedArray.getDrawable(R.styleable.DaisyImageView_focus_bg);
        normalBg = typedArray.getDrawable(R.styleable.DaisyImageView_normal_bg);
        setImageDrawable(normalBg);
        typedArray.recycle();
    }

    public DaisyImageView(Context context, AttributeSet attrs, int defStyle) {
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
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if (gainFocus && focusBg != null) {
            setImageDrawable(focusBg);
        } else if (!gainFocus && normalBg != null) {
            setImageDrawable(normalBg);
        }
    }


}
