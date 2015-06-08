package tv.ismar.daisy.views;

import tv.ismar.daisy.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;

public class LabelImageView extends AsyncImageView {

	private String focustitle = "";
	private int focustitlesize;
	private int focuspaddingtop;
	private int focusbackground;
	private int focustitlepaddingtop;

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
		focuspaddingtop = a.getDimensionPixelOffset(
				R.styleable.LabelImageView_focuspaddingtop, 0);
		focustitlesize = a.getDimensionPixelOffset(
				R.styleable.LabelImageView_focustextsize, 0);
		focusbackground = a.getColor(
				R.styleable.LabelImageView_focusbackground, 0);
		focustitlepaddingtop = a.getDimensionPixelOffset(
				R.styleable.LabelImageView_focustextpaddingtop, 0);
		a.recycle();

	}

	protected void onFocusChanged(boolean gainFocus, int direction,
			Rect previouslyFocusedRect) {
		super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
		if (gainFocus) {

		}
	}

	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		int width = getLayoutParams().width;
		int height = getLayoutParams().height;
		Paint paint = new Paint();
		int paddingBottom = getPaddingBottom();
		// 绘制看点背景
		paint.setColor(Color.WHITE);
		if (focustitle.length() > 0) {
			paint.setColor(focusbackground);
			canvas.drawRect(new Rect(4, focuspaddingtop, width-4, height
					- paddingBottom), paint);
			// 看点内容
			paint.setColor(Color.WHITE);
			paint.setTextSize(focustitlesize);
			float focuswidth = paint.measureText(focustitle);
			int xfocus = (int) ((width - focuswidth) / 2);
			canvas.drawText(focustitle, xfocus, focuspaddingtop
					+ focustitlepaddingtop, paint);
		}
	}
}
