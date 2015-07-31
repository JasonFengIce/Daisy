package tv.ismar.daisy.views;

import java.io.InputStream;

import org.apache.commons.lang3.StringUtils;

import tv.ismar.daisy.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class LabelImageView extends AsyncImageView {

	private String focustitle = "";
	private int focustitlesize;
	private float focuspaddingtop;
	private int focusbackground;
	private float focustitlepaddingtop;
	private int frontcolor;
	private int modetype;
	private int carouse_color;
	private boolean customfocus;
	private boolean customselected;
	private int maxfocustitle;

	public void setNeedzoom(boolean needzoom) {
		this.needzoom = needzoom;
	}

	private boolean needzoom;
	private Animation scaleSmallAnimation;
	private Animation scaleBigAnimation;
	private Rect mBound;
	private NinePatchDrawable mDrawable;
	private Rect mRect;

	public void setModetype(int modetype) {
		this.modetype = modetype;
	}

	public String getFocustitle() {
		return focustitle;
	}

	public void setFocustitle(String focustitle) {
		this.focustitle = focustitle;
	}

	public int getFocustitlesize() {
		return focustitlesize;
	}

	public void setFocustitlesize(int focustitlesize) {
		this.focustitlesize = focustitlesize;
	}

	public LabelImageView(Context context) {
		this(context, null);
	}

	public LabelImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public LabelImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.LabelImageView);
		focuspaddingtop = a.getFloat(
				R.styleable.LabelImageView_focuspaddingtop, 0.85f);
		focustitlesize = a.getDimensionPixelOffset(
				R.styleable.LabelImageView_focustextsize, 0);
		focusbackground = a.getColor(
				R.styleable.LabelImageView_focusbackground, 0);
		focustitlepaddingtop = a.getFloat(
				R.styleable.LabelImageView_focustextpaddingtop, 0.97f);
		frontcolor = a.getInt(R.styleable.LabelImageView_frontcolor, 0);
		carouse_color = context.getResources().getColor(R.color.carousel_focus);
		customfocus = a.getBoolean(R.styleable.LabelImageView_customfocus,
				false);
		needzoom = a.getBoolean(R.styleable.LabelImageView_needzoom, false);
		maxfocustitle = a.getInt(R.styleable.LabelImageView_maxfocustitle, 0);
		a.recycle();
		setWillNotDraw(false);
		mRect = new Rect();
		mBound = new Rect();
		mDrawable = (NinePatchDrawable) getResources().getDrawable(
				R.drawable.popup_bg_yellow);
	}

	protected void onFocusChanged(boolean gainFocus, int direction,
			Rect previouslyFocusedRect) {
		super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
		if (needzoom) {
			if (gainFocus) {
				bringToFront();
				getRootView().requestLayout();
				getRootView().invalidate();
				zoomOut();
			} else {
				zoomIn();
			}
		}
	}

	public void setFrontcolor(int frontcolor) {
		this.frontcolor = frontcolor;
	}

	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		super.getDrawingRect(mRect);
		int width = getLayoutParams().width;
		int height = getLayoutParams().height;
		int paddingright = getPaddingRight();
		int paddingtop = getPaddingTop();
		int paddingBottom = getPaddingBottom();
		if (width <= 0)
			width = getWidth();
		if (height <= 0)
			height = getHeight();
		Paint paint = new Paint();
		// 绘制角标
		if (modetype > 0) {
			int resId = R.drawable.entertainment_bg;
			switch (modetype) {
			case 1:
				resId = R.drawable.entertainment_bg;
				break;
			case 2:
				resId = R.drawable.variety_bg;
				break;
			case 3:
				resId = R.drawable.all_match;
				break;
			case 4:
				resId = R.drawable.living;
				break;
			case 5:
				resId = R.drawable.beonline;
				break;
			case 6:
				resId = R.drawable.collection;
				break;
			}
			InputStream is = getResources().openRawResource(resId);
			Bitmap mBitmap = BitmapFactory.decodeStream(is);
			canvas.drawBitmap(mBitmap, width - mBitmap.getWidth()
					- paddingright - 15, paddingtop, paint);
		}
		// 绘制看点背景
		paint.setColor(Color.WHITE);
		if (StringUtils.isNotEmpty(focustitle) && focustitle.length() > 0) {
			if (maxfocustitle > 0 && focustitle.length() > maxfocustitle) {
				focustitle = focustitle.substring(0, maxfocustitle - 1);
			}
			paint.setColor(focusbackground);
			canvas.drawRect(new Rect(getPaddingLeft(),
					(int) (focuspaddingtop * height), width - paddingright,
					height - paddingBottom), paint);
			// 看点内容
			paint.setColor(Color.WHITE);
			paint.setTextSize(focustitlesize);
			// FontMetrics fm = paint.getFontMetrics();
			// int focusTextHeight = (int)Math.ceil(fm.descent - fm.ascent);
			float focuswidth = paint.measureText(focustitle);
			int xfocus = (int) ((width - focuswidth) / 2);
			canvas.drawText(focustitle, xfocus,
					(int) (focustitlepaddingtop * height), paint);
		}
		// 绘制遮罩效果
		if (frontcolor != 0) {
			if (!customselected) {
				paint.setColor(frontcolor);
				canvas.drawRect(mRect, paint);
			}
		}

		// if (customfocus) {
		if (hasFocus()) {
			mBound.set(-3 + mRect.left, -3 + mRect.top, 3 + mRect.right,
					mRect.bottom + 3);
			mDrawable.setBounds(mBound);
			canvas.save();
			mDrawable.draw(canvas);
			canvas.restore();
		}
		// }
	}

	public void setCustomfocus(boolean customfocus) {
		this.customselected = customfocus;
		invalidate();
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
