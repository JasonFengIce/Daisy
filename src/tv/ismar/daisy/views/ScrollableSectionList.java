package tv.ismar.daisy.views;

import tv.ismar.daisy.R;
import tv.ismar.daisy.models.Section;
import tv.ismar.daisy.models.SectionList;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ScrollableSectionList extends HorizontalScrollView {
	
	private static final String TAG = "ScrollableSectionList";
	
	private LinearLayout mContainer;
	
	/*
	 * current selected section index. don't change this value directly.Always use ScrollableSectionList.changeSelection(int position) to change this value.
	 */
	private int mSelectPosition = 0;
	
	private OnSectionSelectChangedListener mSectionSelectChangedListener;
	
	private Rect mTempRect = new Rect();
	
	private boolean mSmoothScrollingEnabled = true;
	
//	private boolean isSectionWidthResized = false;
	
	private static final int LABEL_TEXT_COLOR_NOFOCUSED = 0xffbbbbbb;
	
	private static final int LABEL_TEXT_COLOR_FOCUSED = 0xff000000;
	
	private static final int LABEL_TEXT_BACKGROUND_COLOR_FOCUSED = 0xffe5aa50;
	
	private static final int LABEL_TEXT_BACKGROUND_SELECTED_NOFOCUSED = 0x80e5aa50;
	
	private static final int LABEL_TEXT_BACKGROUND_NOSELECTED_NOFOCUSED = 0x00000000;
	
	public ScrollableSectionList(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		initialize();
	}

	public ScrollableSectionList(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize();
	}

	public ScrollableSectionList(Context context) {
		super(context);
		initialize();
	}
	
	private void initialize() {
		this.setFadingEdgeLength(100);
		this.setHorizontalFadingEdgeEnabled(true);
	}

	public void init(SectionList sectionList, int totalWidth) {
		mContainer = new LinearLayout(getContext());
		mContainer.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, 66));
		int width = totalWidth / sectionList.size() - 10;
		width = width < 200 ? 200 : width;
		for(int i=0; i<sectionList.size(); i++) {
			String title = sectionList.get(i).title;
			int length = title.length();
			width = width > length * 38 ? width : length * 38 + 60;
		}
		for(int i=0; i<sectionList.size(); i++) {
			LinearLayout sectionHolder = getSectionLabelLayout(sectionList.get(i), width);
			sectionHolder.setOnFocusChangeListener(mOnFocusChangeListener);
			sectionHolder.setOnClickListener(mOnClickListener);
			sectionHolder.setTag(i);
			mContainer.addView(sectionHolder, i);
		}
		this.addView(mContainer);
		mContainer.getChildAt(0).requestFocus();
	}
	
	private LinearLayout getSectionLabelLayout(Section section, int width) {
		LinearLayout sectionHolder = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.section_list_item, null);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, 66);
		layoutParams.rightMargin = 10;
		sectionHolder.setLayoutParams(layoutParams);
		sectionHolder.setFocusable(true);
		TextView label = (TextView) sectionHolder.findViewById(R.id.section_label);
		label.setText(section.title);
		ProgressBar percentage = (ProgressBar) sectionHolder.findViewById(R.id.section_percentage);
		label.getLayoutParams().width = width;
		percentage.getLayoutParams().width = width;
		return sectionHolder;
	}
	
	private OnFocusChangeListener mOnFocusChangeListener = new OnFocusChangeListener() {
		
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			int index = (Integer) v.getTag();
			TextView label = (TextView) v.findViewById(R.id.section_label);
			ProgressBar percentageBar = (ProgressBar) v.findViewById(R.id.section_percentage);
			if(hasFocus){
				label.setBackgroundColor(LABEL_TEXT_BACKGROUND_COLOR_FOCUSED);
				label.setTextColor(LABEL_TEXT_COLOR_FOCUSED);
				if(index==mSelectPosition) {
					percentageBar.setProgressDrawable(getResources().getDrawable(R.drawable.section_percentage_hot_selected));
				} else {
					percentageBar.setProgressDrawable(getResources().getDrawable(R.drawable.section_percentage_hot_noselected));
				}
			} else {
				label.setTextColor(LABEL_TEXT_COLOR_NOFOCUSED);
				if(index==mSelectPosition) {
					label.setBackgroundColor(LABEL_TEXT_BACKGROUND_SELECTED_NOFOCUSED);
					percentageBar.setProgressDrawable(getResources().getDrawable(R.drawable.section_percentage_selected));
				} else {
					label.setBackgroundColor(LABEL_TEXT_BACKGROUND_NOSELECTED_NOFOCUSED);
					percentageBar.setProgressDrawable(getResources().getDrawable(R.drawable.section_percentage_noselected));
				}
			}
		}
	};
	
	private OnClickListener mOnClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			int index = (Integer) v.getTag();
			if(index!=mSelectPosition){
				ProgressBar currentPercentageBar = (ProgressBar) v.findViewById(R.id.section_percentage);
				currentPercentageBar.setProgressDrawable(getResources().getDrawable(R.drawable.section_percentage_hot_selected));
				View lastSelectedView = mContainer.getChildAt(mSelectPosition);
				ProgressBar lastPercentageBar = (ProgressBar) lastSelectedView.findViewById(R.id.section_percentage);
				lastPercentageBar.setProgressDrawable(getResources().getDrawable(R.drawable.section_percentage_noselected));
				TextView lastLabel = (TextView) lastSelectedView.findViewById(R.id.section_label);
				lastLabel.setBackgroundColor(LABEL_TEXT_BACKGROUND_NOSELECTED_NOFOCUSED);
				changeSelection(index);
				if(mSectionSelectChangedListener!=null) {
					mSectionSelectChangedListener.onSectionSelectChanged(index);
				}
			}
		}
	};
	/**
	 * use to modify the special position's section percentage progress bar.
	 * @param position  the section index which you want to modify
	 * @param percentage  the percentage,should be a 100 based integer. 
	 */
	public void setPercentage(int position, int percentage) {
		View sectionHolder = mContainer.getChildAt(position);
		TextView label = (TextView) sectionHolder.findViewById(R.id.section_label);
		ProgressBar percentageBar = (ProgressBar) sectionHolder.findViewById(R.id.section_percentage);
		if(position!=mSelectPosition) {
			View lastSectionHolder = mContainer.getChildAt(mSelectPosition);
			TextView lastLabel = (TextView) lastSectionHolder.findViewById(R.id.section_label);
			ProgressBar lastPercentageBar = (ProgressBar) lastSectionHolder.findViewById(R.id.section_percentage);
			lastLabel.setBackgroundColor(LABEL_TEXT_BACKGROUND_NOSELECTED_NOFOCUSED);
			lastPercentageBar.setProgressDrawable(getResources().getDrawable(R.drawable.section_percentage_noselected));
			lastPercentageBar.setProgress(0);
			
			changeSelection(position);
			label.setBackgroundColor(LABEL_TEXT_BACKGROUND_SELECTED_NOFOCUSED);
			percentageBar.setProgressDrawable(getResources().getDrawable(R.drawable.section_percentage_selected));
		}
		Log.d("CurrentPercentage", percentage + "");
		percentageBar.setProgress(percentage);
		
	}
	
	public void setOnSectionSelectChangeListener(OnSectionSelectChangedListener listener) {
		mSectionSelectChangedListener = listener;
	}
	
	/**
	 * indicate that section is changed by user click. usually use to update itemList of the section.
	 * @author bob
	 *
	 */
	public interface OnSectionSelectChangedListener {
		public void onSectionSelectChanged(int index);
	}

	@Override
	public boolean arrowScroll(int direction) {
		if(direction==View.FOCUS_RIGHT){
			View currentFocused = findFocus();
			if(currentFocused==null || currentFocused.getTag()==null) {
				return super.arrowScroll(direction);
			}
			int index = (Integer) currentFocused.getTag();
			if(index < mContainer.getChildCount()-1){
				return super.arrowScroll(direction);
			} else {
				//if currentFocused is the last element of the list. just do nothing.
				return true;
			}
		} else if(direction==View.FOCUS_LEFT){
			View currentFocused = findFocus();
			if(currentFocused==null || currentFocused.getTag()==null) {
				return super.arrowScroll(direction);
			}
			int index = (Integer) currentFocused.getTag();
			if(index > 0 ){
				return super.arrowScroll(direction);
			} else {
				//if currentFocused is the last element of the list. just do nothing.
				return true;
			}
		} else {
			return super.arrowScroll(direction);
		}
	}
	
	/**
     * @return whether the descendant of this scroll view is within delta
     *  pixels of being on the screen.
     */
    private boolean isWithinDeltaOfScreen(View descendant, int delta) {
        descendant.getDrawingRect(mTempRect);
        offsetDescendantRectToMyCoords(descendant, mTempRect);

        return (mTempRect.right + delta) >= getScrollX()
                && (mTempRect.left - delta) <= (getScrollX() + getWidth());
    }
    
    /**
     * Smooth scroll by a X delta
     *
     * @param delta the number of pixels to scroll by on the X axis
     */
    private void doScrollX(int delta) {
        if (delta != 0) {
            if (mSmoothScrollingEnabled) {
                smoothScrollBy(delta, 0);
            } else {
                scrollBy(delta, 0);
            }
        }
    	
    }
    
//    /**
//     * @return whether the descendant of this scroll view is scrolled off
//     *  screen.
//     */
//    private boolean isOffScreen(View descendant) {
//        return !isWithinDeltaOfScreen(descendant, 0);
//    }
//    
//    

	/*
	 * use to change the mSelectPosition.
	 */
	private void changeSelection(int position) {
		if(position < 0 || position >= mContainer.getChildCount()) {
			return;
		}
		mSelectPosition = position;
		View section = mContainer.getChildAt(position);
		final int maxJump = getMaxScrollAmount();
		
		if(isWithinDeltaOfScreen(section, maxJump)) {
			section.getDrawingRect(mTempRect);
			offsetDescendantRectToMyCoords(section, mTempRect);
			int delta = computeScrollDeltaToGetChildRectOnScreen(mTempRect);
			doScrollX(delta);
		}
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}


	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
//		if(mContainer!=null && mContainer.getChildCount()>0 && !isSectionWidthResized) {
//			Log.d(TAG, "onLayout called");
//			int width = getWidth() / mContainer.getChildCount() - 10;
//			width = width < 200 ? 200 : width;
//			int rightMargin = 0;
//			Log.d(TAG, "width: " + width);
//			for(int i=0; i<mContainer.getChildCount(); i++) {
//				LinearLayout sectionHolder = (LinearLayout) mContainer.getChildAt(i);
//				if(sectionHolder.getVisibility()!=View.GONE){
//					int sectionWidth = sectionHolder.getWidth();
//					rightMargin = ((LinearLayout.LayoutParams) sectionHolder.getLayoutParams()).rightMargin;
//					Log.d(TAG, "sectionWidth: " + sectionWidth);
//					if(sectionWidth == 0) {
//						
//						return;
//					}
//					width = sectionWidth > width ? sectionWidth : width;
//				}
//			}
//			Log.d(TAG, "onMeasure");
//			int childLeft = 0;
//			int childTop = getTop();
//			for(int i=0; i<mContainer.getChildCount(); i++) {
//				LinearLayout sectionHolder = (LinearLayout) mContainer.getChildAt(i);
//				sectionHolder.layout(childLeft, childTop, childLeft+width, childTop+sectionHolder.getHeight());
//				int top = childTop;
//				for(int j=0; j<sectionHolder.getChildCount();j++) {
//					View child = sectionHolder.getChildAt(j);
//					child.layout(childLeft, top, childLeft+ width, top + child.getHeight());
//					top += child.getHeight() + ((LinearLayout.LayoutParams)child.getLayoutParams()).bottomMargin;
//				}
//				childLeft += width + rightMargin;
//			}
//			isSectionWidthResized = true;
//			Log.d(TAG, "width: " + width);
//		}
		
	}
	
	public void reset() {
		removeAllViews();
	}
}
