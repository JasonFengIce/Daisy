package tv.ismar.daisy.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.View;
import android.widget.LinearLayout;

public class ItemListContainer extends LinearLayout {
	
	private static final int DEFAULT_ROWS = 3;
	
	private int mTotalCellCount;

	public ItemListContainer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public ItemListContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ItemListContainer(Context context) {
		super(context);
		init();
	}
	
	private void init() {
		setOrientation(HORIZONTAL);
	}

	@Override
	public void addView(View child, int index) {
		int currentCol = getCurrentCol(index);
		int currentRow = getCurrentRow(index);
		View colHolder = this.getChildAt(currentCol);
		if(colHolder==null) {
			colHolder = new LinearLayout(getContext());
			colHolder.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
			((LinearLayout)colHolder).setOrientation(VERTICAL);
			super.addView(colHolder, currentCol);
		}
		
		colHolder.setTag(currentCol);
		if(((LinearLayout)colHolder).getChildAt(currentRow)==null){
			((LinearLayout)colHolder).addView(child, currentRow);
			mTotalCellCount++;
		}
		
	}

	
	
	/*
	 * return a zero based column number
	 */
	private int getCurrentCol(int index) {
		return (int) FloatMath.floor((float)index / (float)DEFAULT_ROWS);
	}
	
	public int getTotalCellCount() {
		return mTotalCellCount;
	}
	
	/*
	 * return a zero based row number
	 */
	private int getCurrentRow(int index) {
		return index - (index / DEFAULT_ROWS)*DEFAULT_ROWS;
	}

	
}
