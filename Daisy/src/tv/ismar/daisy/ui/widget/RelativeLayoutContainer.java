package tv.ismar.daisy.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import tv.ismar.daisy.R;

public class RelativeLayoutContainer extends RelativeLayout {

	private Rect mBound;
	private NinePatchDrawable mDrawable;
	private Rect mRect;
	private Animation scaleSmallAnimation;
	private Animation scaleBigAnimation;
	private boolean isDrawBorder = false;

	public RelativeLayoutContainer(Context context) {
		super(context);
		init();
	}

	public void setDrawBorder(boolean isDrawBorder) {
		this.isDrawBorder = isDrawBorder;
	}

	public RelativeLayoutContainer(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public RelativeLayoutContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	protected void init() {
		setWillNotDraw(false);
		mRect = new Rect();
		mBound = new Rect();
		mDrawable = (NinePatchDrawable) getResources().getDrawable(
				R.drawable.vod_gv_selector);// nav_focused_2,poster_shadow_4
		setChildrenDrawingOrderEnabled(true);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
	}

	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (isDrawBorder) {
			System.out.println("HomeItemContainer focus : true ");
			super.getDrawingRect(mRect);
			mBound.set(-21+mRect.left, -21+mRect.top, 21+mRect.right, mRect.bottom+21);
			mDrawable.setBounds(mBound);
			canvas.save();
			mDrawable.draw(canvas);
			canvas.restore();
		}else{
			mBound.setEmpty();
			mDrawable.setBounds(mBound);
			mDrawable.draw(canvas);
		}
		getRootView().requestLayout();
		getRootView().invalidate();
	}

	@Override
	protected void onFocusChanged(boolean gainFocus, int direction,
			Rect previouslyFocusedRect) {
		super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
		if (gainFocus) {
			isDrawBorder = true;
			bringToFront();
			// getRootView().requestLayout();
			// getRootView().invalidate();
			// zoomOut();
		} else {
			isDrawBorder = false;
			// zoomIn();
		}
	}

	private void zoomIn() {
		if (scaleSmallAnimation == null) {
			scaleSmallAnimation = AnimationUtils.loadAnimation(getContext(),
					R.anim.anim_scale_small);
		}
		startAnimation(scaleSmallAnimation);
	}

	private void zoomOut() {
		if (scaleBigAnimation == null) {
			scaleBigAnimation = AnimationUtils.loadAnimation(getContext(),
					R.anim.anim_scale_big);
		}
		startAnimation(scaleBigAnimation);
	}
}
