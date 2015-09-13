package tv.ismar.daisy.ui.widget;

import tv.ismar.daisy.R;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class LabelImageView extends ImageView {

	private Animation scaleSmallAnimation;
	private Animation scaleBigAnimation;
	private Rect mBound;
	private Drawable mDrawable;
	private Rect mRect;

	public LabelImageView(Context context) {
		this(context, null);
	}

	public LabelImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public LabelImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setWillNotDraw(false);
		mRect = new Rect();
		mBound = new Rect();
		mDrawable = (Drawable) getResources().getDrawable(
				R.drawable.usercenter_focus_border);
	}

	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		super.getDrawingRect(mRect);
//		if (hasFocus()) {
			mBound.set(-5+mRect.left, -5+mRect.top, 5+mRect.right, mRect.bottom+5);
			mDrawable.setBounds(mBound);
			canvas.save();
			mDrawable.draw(canvas);
			canvas.restore();
//		}
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
