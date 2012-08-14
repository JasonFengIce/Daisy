package tv.ismar.daisy.views;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class ItemListContainer extends LinearLayout {
	
	private static final String TAG = "";
	
	private static final int DEFAULT_ROWS = 3;
	
//	private Rect mTempRect;
	
	private ArrayList<View> mCellList;
	
	private int mCellCount = 0;
	
	private int maxWidth;
	private int measureWidthMode;
	private int maxHeight;
	private int measureHeightMode;
	
	private boolean hasEverRecycled = false;
	
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
		mCellList = new ArrayList<View>();
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
			mCellList.add(child);
			if(!hasEverRecycled) {
				++mCellCount;
			}
		}
		
	}

	public View getCellAt(int index) {
		if(index>=mCellList.size()) {
			return null;
		} else {
			try {
				return mCellList.get(index);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}
	
	public ArrayList<View> recycleAll() {
		for(int i=0; i<getChildCount();++i) {
			ViewGroup colHolder = (ViewGroup) getChildAt(i);
			colHolder.removeAllViews();
		}
		return mCellList;
	}
	
	@Override
	public void removeAllViews() {
		hasEverRecycled = true;
		mCellList.clear();
		super.removeAllViews();
	}

	/*
	 * return a zero based column number
	 */
	private int getCurrentCol(int index) {
		return (int) FloatMath.floor((float)index / (float)DEFAULT_ROWS);
	}
	
	public int getTotalCellCount() {
		return mCellCount;
	}
	
	/*
	 * return a zero based row number
	 */
	private int getCurrentRow(int index) {
		return index - (index / DEFAULT_ROWS)*DEFAULT_ROWS;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if(hasEverRecycled) {
			final int width = maxWidth;
			final int height = maxHeight;
			widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, measureWidthMode);
			heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, measureHeightMode);
			hasEverRecycled = false;
		} else {
			measureWidthMode = MeasureSpec.getMode(widthMeasureSpec);
			measureHeightMode = MeasureSpec.getMode(heightMeasureSpec);
			maxWidth = getWidth();
			maxHeight = getHeight();
		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
	}

}
