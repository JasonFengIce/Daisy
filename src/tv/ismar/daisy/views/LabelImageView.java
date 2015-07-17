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
import android.util.AttributeSet;

public class LabelImageView extends AsyncImageView {

	private String focustitle = "";
	private int focustitlesize;
	private float focuspaddingtop;
	private int focusbackground;
	private float focustitlepaddingtop;
	private int frontcolor;
	private int modetype;

	public void setModetype(int modetype) {
		this.modetype = modetype;
	}

	public String getFocustitle() {
		return focustitle;
	}

	public void setFocustitle(String focustitle) {
		if ((StringUtils.isNotEmpty(focustitle)) && focustitle.length() > 8)
			focustitle = focustitle.substring(0, 8);
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
			if (!isFocused()) {
				paint.setColor(frontcolor);
				canvas.drawRect(new Rect(0, 0, width, height), paint);
			}
		}
	}
}
