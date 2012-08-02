package tv.ismar.daisy.views;

import tv.ismar.daisy.R;
import tv.ismar.daisy.models.Section;
import tv.ismar.daisy.models.SectionList;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ScrollableSectionList extends HorizontalScrollView {
	
	private LinearLayout mContainer;
	
	private int mSelectPosition = 0;
	
	private OnSectionSelectChangedListener mSectionSelectChangedListener;
	
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

	public void init(SectionList sectionList) {
		mContainer = new LinearLayout(getContext());
		mContainer.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, 66));
		for(int i=0; i<sectionList.size();i++) {
			LinearLayout sectionHolder = getSectionLabelLayout(sectionList.get(i));
			sectionHolder.setOnFocusChangeListener(mOnFocusChangeListener);
			sectionHolder.setOnClickListener(mOnClickListener);
			sectionHolder.setTag(i);
			mContainer.addView(sectionHolder, i);
		}
		this.addView(mContainer);
		mContainer.getChildAt(0).requestFocus();
	}
	
	private LinearLayout getSectionLabelLayout(Section section) {
		LinearLayout sectionHolder = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.section_list_item, null);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(200, 66);
		layoutParams.rightMargin = 10;
		sectionHolder.setLayoutParams(layoutParams);
		sectionHolder.setFocusable(true);
		TextView label = (TextView) sectionHolder.findViewById(R.id.section_label);
//		ProgressBar percentage = (ProgressBar) sectionHolder.findViewById(R.id.section_percentage);
		label.setText(section.title);
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
				mSelectPosition = index;
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
			
			mSelectPosition = position;
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
			int index = (Integer) currentFocused.getTag();
			if(index < mContainer.getChildCount()-1){
				return super.arrowScroll(direction);
			} else {
				//if currentFocused is the last element of the list. just do nothing.
				return true;
			}
		} else {
			return super.arrowScroll(direction);
		}
	}
	
	
}
