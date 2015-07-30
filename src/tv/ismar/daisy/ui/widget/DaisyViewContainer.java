package tv.ismar.daisy.ui.widget;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import tv.ismar.daisy.R;

import java.util.ArrayList;

/**
 * Created by huaijie on 7/2/15.
 */
public class DaisyViewContainer extends LinearLayout {
    private static final String TAG = "DaisyViewContainer";
    private float horizontalSpacing;
    private float verticalSpacing;
    private Context mContext;
    private float rate;

    public DaisyViewContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        DisplayMetrics metric = new DisplayMetrics();
        ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(metric);
        int densityDpi = metric.densityDpi;
        rate = (float) densityDpi / (float) 160;

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.DaisyViewContainer);
        horizontalSpacing = typedArray.getDimensionPixelSize(R.styleable.DaisyViewContainer_horizontal_spacing, 0);
        verticalSpacing = typedArray.getDimensionPixelSize(R.styleable.DaisyViewContainer_vertical_spacing, 0);
        typedArray.recycle();
    }


    public void addAllViews(ArrayList<? extends View> allViews) {
        float spacing = 0;
        switch (getOrientation()) {
            case HORIZONTAL:
                spacing = horizontalSpacing / rate;
                for (int i = 0; i < allViews.size(); i++) {
                    if (i != 0) {
                        LayoutParams layoutParams = new LayoutParams(199, 278);
                        layoutParams.setMargins((int) 26, 0, 0, 0);
                        allViews.get(i).setLayoutParams(layoutParams);
                        addView(allViews.get(i));

                    } else {
					    LayoutParams layoutParams = new LayoutParams(199, 278);
                        addView(allViews.get(i),layoutParams);
                    }
                }
                break;
            case VERTICAL:
                spacing = verticalSpacing / rate;
                for (int i = 0; i < allViews.size(); i++) {
                    if (i != 0) {
                        LayoutParams layoutParams = new LayoutParams(297, 166);
                        layoutParams.setMargins(0, (int) spacing, 0, 0);
                        allViews.get(i).setLayoutParams(layoutParams);
                        addView(allViews.get(i));
                    } else {
                        addView(allViews.get(i));
                    }
                }
                break;
        }
        requestLayout();
        invalidate();
    }
}
