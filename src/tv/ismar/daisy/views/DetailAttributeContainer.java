package tv.ismar.daisy.views;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;

import tv.ismar.daisy.ItemDetailActivity;
import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.models.ContentModel;

import android.content.Context;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DetailAttributeContainer extends LinearLayout {
	
	private final static String TAG = "DetailAttributeContainer";
	
	private int mMaxHeight;
	
	private LinkedHashMap<String, String> mAttributeMap;
	
	private ContentModel mContentModel;
	
	private int mCurrentHeight;
	
	private boolean hasAdjustSpaceItem = true;
	
//	private boolean hasAdjustSpaceTitle = true;
	
	private int mTotalHeight;

	public DetailAttributeContainer(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public DetailAttributeContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public DetailAttributeContainer(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	
	public void addAttribute(LinkedHashMap<String, String> attrMap, ContentModel m) {
		mAttributeMap = attrMap;
		mContentModel = m;
		buildAttributeList(mAttributeMap);
	}
	
	private void buildAttributeList(LinkedHashMap<String, String> attrMap) {
		for(Map.Entry<String, String> entry: attrMap.entrySet()){
			if(entry.getValue()==null || mContentModel.attributes.get(entry.getKey())==null){
				continue;
			}
			LinearLayout infoLine = new LinearLayout(getContext());
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(471,LinearLayout.LayoutParams.WRAP_CONTENT);
			layoutParams.topMargin = 15;
			
			infoLine.setLayoutParams(layoutParams);
			infoLine.setOrientation(LinearLayout.HORIZONTAL);
			TextView itemName = new TextView(getContext());
			itemName.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
			itemName.setTextColor(0xff999999);
			itemName.setTextSize(30f);
			itemName.setText(mContentModel.attributes.get(entry.getKey())+":");
			infoLine.addView(itemName);
			TextView itemValue = new TextView(getContext());
			itemValue.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
			itemValue.setTextColor(0xffbbbbbb);
			itemValue.setTextSize(30f);
			itemValue.setText(entry.getValue());
			infoLine.addView(itemValue);
			addView(infoLine);
		}
	}

	public void setMaxHeight(int max) {
		mMaxHeight = max;
		if(mCurrentHeight>0 && mCurrentHeight > mMaxHeight) {
			adjustItemHeight();
		}
	}
	
	
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		int h = b - t;
		if(mMaxHeight == 0) {
			mMaxHeight = h;
		}
		mCurrentHeight = h;
		Log.d(TAG, "mCurrentHeight: "+mCurrentHeight+" mMaxHeight: " + mMaxHeight);
		if(mCurrentHeight>0 && h > mMaxHeight && getChildCount() > 1) {
			if(hasAdjustSpaceItem){
				hasAdjustSpaceItem = adjustItemHeight();
			}
			post(new Runnable() {
				
				@Override
				public void run() {
					requestLayout();
					invalidate();
				}
			});
		}
	}

	/**
	 * Adjust the max lines of an items value to fit the maxHeight of the container.
	 * @return true if some item still have space (mutiple lines) to adjust. otherwise, return false;
	 */
	private boolean adjustItemHeight() {
		final int childCount = getChildCount();
		int maxLineCount = 0;
		int maxLinePosition = 1;
		for(int i=1; i< childCount; ++i) {
			int lineCount = getLineCountAt(i);
			if(maxLineCount< lineCount) {
				maxLineCount = lineCount;
				maxLinePosition = i;
			}
		}
		
		if(maxLineCount <= 1 || getHeight() <= mMaxHeight) {
			return false;
		}
		LinearLayout lineToBeAdjust = (LinearLayout) getChildAt(maxLinePosition);
		TextView viewToBeAdjust = (TextView) lineToBeAdjust.getChildAt(1);
		viewToBeAdjust.setMaxLines(maxLineCount-1);
		viewToBeAdjust.setEllipsize(TruncateAt.END);
		return true;
	}
	
	private int getLineCountAt(int position) {
		LinearLayout line = (LinearLayout) getChildAt(position);
		TextView content = (TextView) line.getChildAt(1);
		return content.getLineCount();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		
		int count = getChildCount();
		mTotalHeight = 0;
		if(count > 0) {
			for(int i=0; i < count; i++) {
				View child = getChildAt(i);
				measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
				int childHeight = child.getMeasuredHeight();
				Log.d(TAG, "childHeight: "+childHeight);
				mTotalHeight += childHeight;
			}
		}
		if(mTotalHeight>mMaxHeight && mMaxHeight >0) {
			
		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	
	
	
	
}
