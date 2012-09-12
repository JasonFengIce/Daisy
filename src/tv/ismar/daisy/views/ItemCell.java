package tv.ismar.daisy.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.widget.RelativeLayout;

public class ItemCell extends RelativeLayout {
	
	private static final int DEFAULT_ROWS = 3;
	
	/**
	 * Which section this cell is belonged.
	 */
	public int position;
	
	public int index;
	
	public int row;
	
	public int col;
	
	public boolean isActive;
	
	public boolean isRecycled;

	public ItemCell(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public ItemCell(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public ItemCell(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public void setPosition(int position, int index) {
		this.position = position;
		this.index = index;
		this.row = getCurrentRow(index);
		this.col = getCurrentCol(index);
	}
	
	/*
	 * return a zero based row number
	 */
	private int getCurrentRow(int index) {
		return index - (index / DEFAULT_ROWS)*DEFAULT_ROWS;
	}
	
	/*
	 * return a zero based column number
	 */
	private int getCurrentCol(int index) {
		return (int) FloatMath.floor((float)index / (float)DEFAULT_ROWS);
	}

}
