package tv.ismar.daisy.views;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.FocusFinder;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnHoverListener;
import android.widget.*;
import tv.ismar.daisy.R;
import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.models.Section;
import tv.ismar.daisy.models.SectionList;

import java.text.DecimalFormat;

public class RelateScrollableSectionList extends HorizontalScrollView {
	
	private static final String TAG = "ScrollableSectionList";
	
	private boolean isChangeBarStyle = false;
	private LinearLayout mContainer;
	
	/*
	 * current selected section index. don't change this value directly.Always use ScrollableSectionList.changeSelection(int position) to change this value.
	 */
	private int mSelectPosition = 0;
	private int lastSelectPosition = 0;
	
	private OnSectionSelectChangedListener mSectionSelectChangedListener;
	
	private Rect mTempRect = new Rect();
	
	private boolean mSmoothScrollingEnabled = true;
	
//	private boolean isSectionWidthResized = false;
	
	private static final int LABEL_TEXT_COLOR_NOFOCUSED = 0xffffffff;
	
	private static final int LABEL_TEXT_COLOR_FOCUSED = 0xffffba00;

    private static final int LABEL_TEXT_COLOR_CLICKED = 0xff00a8ff;

    private static final int LABEL_TEXT_BACKGROUND_COLOR_FOCUSED = 0xffe5aa50;
	
	private static final int LABEL_TEXT_BACKGROUND_SELECTED_NOFOCUSED = 0x80e5aa50;
	
	private static final int LABEL_TEXT_BACKGROUND_NOSELECTED_NOFOCUSED = 0x00000000;
	
	public RelateScrollableSectionList(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		initialize();
	}

	public RelateScrollableSectionList(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize();
	}

	public RelateScrollableSectionList(Context context) {
		super(context);
		initialize();	
	}
	
	private void initialize() {
		this.setFadingEdgeLength(100);
		this.setHorizontalFadingEdgeEnabled(true);
	}
    float rate;
	public void init(SectionList sectionLists, int totalWidth,boolean isChangeBarStyle) {
        rate = DaisyUtils.getVodApplication(getContext()).getRate(getContext());
		mContainer = new LinearLayout(getContext());
		this.isChangeBarStyle = isChangeBarStyle;
//		int H = DaisyUtils.getVodApplication(getContext()).getheightPixels(getContext());
//		if(H==720)			
		    mContainer.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, getResources().
		    		getDimensionPixelSize(R.dimen.channel_section_tabs_H)));
//		else
//			mContainer.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, 66));
//		int width = totalWidth / sectionList.size() - 10;
//		width = width < 200 ? 200 : width;
//		for(int i=0; i<sectionList.size(); i++) {
//			String title = sectionList.get(i).title;
//			int length = title.length();
//			width = width > length * 38 ? width : length * 38 + 60;
//		}
		    SectionList sectionList = new SectionList();
		    for(Section s: sectionLists){
		    	if(s.count!=0){
		    		sectionList.add(s);
		    	}
		    }
		for(int i=0; i<sectionList.size(); i++) {			
			RelativeLayout sectionHolder = getSectionLabelLayout(sectionList.get(i), getResources()
					.getDimensionPixelSize(R.dimen.channel_section_tabs_W));
			sectionHolder.setOnFocusChangeListener(mOnFocusChangeListener);
			sectionHolder.setOnClickListener(mOnClickListener);
			sectionHolder.setOnHoverListener(mOnTouchListener);
			sectionHolder.setTag(i);
			mContainer.addView(sectionHolder, i);
			//sectionHolder.setNextFocusUpId(R.id.list_view_search);
		}
		this.addView(mContainer);
		if(mContainer.getChildAt(0)!=null){
			//mContainer.getChildAt(0).requestFocus();
			View v = mContainer.getChildAt(0);
			TextView label = (TextView) v.findViewById(R.id.section_label);
			ProgressBar percentageBar = (ProgressBar) v.findViewById(R.id.section_percentage);
			//label.setPadding(label.getPaddingLeft(),  getResources().getDimensionPixelSize(R.dimen.channel_section_tabs_text_PT), label.getPaddingRight(), label.getPaddingBottom());
			
			//percentageBar.setProgress(0);
			int textsize = getResources().getDimensionPixelSize(R.dimen.channel_section_tabs_label_ctextsize);
			float rate = DaisyUtils.getVodApplication(getContext()).getRate(getContext());
			 textsize = (int) (textsize/rate);
			 label.setTextSize(textsize);
			label.setTextColor(LABEL_TEXT_COLOR_NOFOCUSED);
            label.setBackgroundResource(R.drawable.sectionfocus);
			percentageBar.setProgressDrawable(getResources().getDrawable(R.drawable.section_percentage_hot_selected));
		}

	}
	private RelativeLayout getSectionLabelLayout(Section section, int width) {
		RelativeLayout sectionHolder = (RelativeLayout) LayoutInflater.from(getContext()).inflate(R.layout.relatesection_list_item, null);

		//		LinearLayout.LayoutParams layoutParams;
//		if(H==720)
//		    layoutParams = new LinearLayout.LayoutParams(width, 44);
//		else
//			layoutParams = new LinearLayout.LayoutParams(width, 66);
//		layoutParams.rightMargin = 10;
	//	sectionHolder.setLayoutParams(layoutParams);
		sectionHolder.setFocusable(true);
		TextView label = (TextView) sectionHolder.findViewById(R.id.section_label);
		label.setText(section.title);
		ProgressBar percentage = (ProgressBar) sectionHolder.findViewById(R.id.section_percentage);
		label.getLayoutParams().width = width;
		percentage.getLayoutParams().width = width;
		return sectionHolder;
	}
	private View lastView = null;
    public TextView sectionWhenGoto;
    public static int STATE_GOTO_GRIDVIEW = 2;
    public static int STATE_SECTION = 3;
    public static int STATE_LEAVE_GRIDVIEW = 4;
    public int currentState = STATE_SECTION;
    public TextView sectionhovered;
	private OnFocusChangeListener mOnFocusChangeListener = new OnFocusChangeListener() {
		
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			int index = (Integer) v.getTag();
			float rate = DaisyUtils.getVodApplication(getContext()).getRate(getContext());
			if(left!=null&&right!=null){
				if(lastView!=null){
		             if(index==0&&hasFocus&&index!=lastSelectPosition){
		            	 right.setVisibility(View.VISIBLE);
		            	 left.setVisibility(View.INVISIBLE);
		             }
				}

	             if(0<index&&index<mContainer.getChildCount()-1&&hasFocus){
	            	 left.setVisibility(View.VISIBLE);
	            	 right.setVisibility(View.VISIBLE);
	             }
	             if(index==mContainer.getChildCount()-1){
	            	 right.setVisibility(View.INVISIBLE);
	             }
			}
			TextView label = (TextView) v.findViewById(R.id.section_label);
			ProgressBar percentageBar = (ProgressBar) v.findViewById(R.id.section_percentage);
            int textsize = getResources().getDimensionPixelSize(R.dimen.channel_section_tabs_label_ctextsize);
            textsize = (int) (textsize/rate);
            if(hasFocus){
				if(v.isHovered()){
					label.setBackgroundResource(R.drawable.channel_focus_frame);
					return;
				}
				if(sectionhovered != null){
					((RelativeLayout) sectionhovered.getParent()).setHovered(false);
				}
				if (sectionhovered != null) {
					sectionhovered
							.setBackgroundResource(android.R.color.transparent);
				}
                if(index==mSelectPosition){
                    label.setTextColor(LABEL_TEXT_COLOR_NOFOCUSED);
                    label.setTextSize(textsize);
                    label.setBackgroundResource(R.drawable.sectionfocus);
                    return;
                }

                else{

                    if(currentState==STATE_LEAVE_GRIDVIEW){
                        currentState = STATE_SECTION;
                        mContainer.getChildAt(mSelectPosition).requestFocus();
                    }else if(currentState==STATE_SECTION){
                        label.setTextColor(LABEL_TEXT_COLOR_NOFOCUSED);
                        label.setTextSize(textsize);
                        v.performClick();
                    }
                }
            } else {
                if(index==mSelectPosition){
                    sectionWhenGoto = label;
                    return;
                }
                label.setTextColor(LABEL_TEXT_COLOR_NOFOCUSED);

            }
		}
	};
	
	public void setSectionTabProperty(View currentView,View lastSelectedView){
		float rate = DaisyUtils.getVodApplication(getContext()).getRate(getContext());
		TextView lastLabel = (TextView) lastSelectedView.findViewById(R.id.section_label);
		lastLabel.setTextColor(LABEL_TEXT_COLOR_NOFOCUSED);
//		lastLabel.setPadding(lastLabel.getPaddingLeft(), getResources().
//				getDimensionPixelSize(R.dimen.channel_section_tabs_label_paddingT), lastLabel.getPaddingRight(), lastLabel.getPaddingBottom());
        lastLabel.setBackgroundResource(android.R.color.transparent);
		lastLabel.setTextSize(getResources().getDimensionPixelSize(R.dimen.channel_section_tabs_label_textsize)/rate);	
		TextView label = (TextView) currentView.findViewById(R.id.section_label);
		//label.setPadding(label.getPaddingLeft(), getResources().getDimensionPixelSize(R.dimen.channel_section_tabs_text_PT), label.getPaddingRight(), label.getPaddingBottom());
		
		//percentageBar.setProgress(0);
		int textsize = getResources().getDimensionPixelSize(R.dimen.channel_section_tabs_label_ctextsize);
		textsize = (int) (textsize/rate);
		label.setTextSize(textsize);
		label.setTextColor(LABEL_TEXT_COLOR_NOFOCUSED);
        label.setBackgroundResource(R.drawable.sectionfocus);
	}

	private OnHoverListener mOnTouchListener = new OnHoverListener() {

		@Override
		public boolean onHover(View v, MotionEvent keycode) {
			// TODO Autogenerated method stub
			int index = (Integer) v.getTag();
			float rate = DaisyUtils.getVodApplication(getContext()).getRate(
					getContext());
			TextView label = (TextView) v.findViewById(R.id.section_label);
			switch (keycode.getAction()) {
			case MotionEvent.ACTION_HOVER_ENTER:
			case MotionEvent.ACTION_HOVER_MOVE:
				v.setFocusable(true);
				v.setFocusableInTouchMode(true);
				v.setHovered(true);
				v.requestFocus();
				sectionhovered = label;
				if (index == mSelectPosition) {
					label.setBackgroundResource(R.drawable.usercenter_indicator_overlay);
					return false;
				}else{
					label.setBackgroundResource(R.drawable.usercenter_indicator_selected);
				}
//				if(sectionWhenGoto != null)
//					  sectionWhenGoto.setBackgroundResource(R.drawable.gotogridview);
				break;
			case MotionEvent.ACTION_HOVER_EXIT:
				if (index == mSelectPosition) {
					label.setBackgroundResource(R.drawable.gotogridview);
					return false;
				}
				label.setTextColor(LABEL_TEXT_COLOR_NOFOCUSED);
				label.setBackgroundResource(android.R.color.transparent);
				v.setHovered(false);
			default:
				break;
			}
			return false;
		}
	};

	private OnClickListener mOnClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			int index = (Integer) v.getTag();
			
			if(index!=mSelectPosition){
				ProgressBar currentPercentageBar = (ProgressBar) v.findViewById(R.id.section_percentage);
				if(!isChangeBarStyle)
				  currentPercentageBar.setProgressDrawable(getResources().getDrawable(R.drawable.section_percentage_hot_selected));
				else
				  currentPercentageBar.setProgressDrawable(getResources().getDrawable(R.drawable.progress_line));
				

				View lastSelectedView = mContainer.getChildAt(mSelectPosition);
				ProgressBar lastPercentageBar = (ProgressBar) lastSelectedView.findViewById(R.id.section_percentage);
				lastPercentageBar.setProgressDrawable(getResources().getDrawable(R.drawable.section_percentage_noselected));
				setSectionTabProperty(v,lastSelectedView);
				changeSelection(index);
               // lastSelectPosition = index;
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
		ProgressBar percentageBar = (ProgressBar) sectionHolder.findViewById(R.id.section_percentage);
		if(position!=mSelectPosition) {
			View lastSectionHolder = mContainer.getChildAt(mSelectPosition);
			TextView lastLabel = (TextView) lastSectionHolder.findViewById(R.id.section_label);
			ProgressBar lastPercentageBar = (ProgressBar) lastSectionHolder.findViewById(R.id.section_percentage);
			//lastLabel.setBackgroundColor(LABEL_TEXT_BACKGROUND_NOSELECTED_NOFOCUSED);
			lastPercentageBar.setProgressDrawable(getResources().getDrawable(R.drawable.section_percentage_noselected));
			lastPercentageBar.setProgress(0);
			
			changeSelection(position);
			//label.setBackgroundColor(LABEL_TEXT_BACKGROUND_SELECTED_NOFOCUSED);
			percentageBar.setProgressDrawable(getResources().getDrawable(R.drawable.section_percentage_hot_selected));
			setSectionTabProperty(sectionHolder, lastSectionHolder);
           // lastSelectPosition = position;
		}
//		Log.d("CurrentPercentage", percentage + "");
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

//	@Override
//	public boolean arrowScroll(int direction) {
//		if(direction==View.FOCUS_RIGHT){
//			View currentFocused = findFocus();
//			if(currentFocused==null || currentFocused.getTag()==null) {
//				return super.arrowScroll(direction);
//			}
//			int index = (Integer) currentFocused.getTag();
//			if(index < mContainer.getChildCount()-1){
//				return super.arrowScroll(direction);
//			} else {
//				//if currentFocused is the last element of the list. just do nothing.
//				return true;
//			}
//		} else if(direction==View.FOCUS_LEFT){
//			View currentFocused = findFocus();
//			if(currentFocused==null || currentFocused.getTag()==null) {
//				return super.arrowScroll(direction);
//			}
//			int index = (Integer) currentFocused.getTag();
//			if(index > 0 ){
//				return super.arrowScroll(direction);
//			} else {
//				//if currentFocused is the last element of the list. just do nothing.
//				return true;
//			}
//		} else {
//			return super.arrowScroll(direction);
//		}
//	}
	public View left;
	public View right;
	public View parent;
	
    public boolean arrowScroll(int direction) {
    	    
    	   if(left==null||right==null){
    	    	return super.arrowScroll(direction);
    	    }

    	   View currentFocused = findFocus();
    	   
           if (currentFocused == this) currentFocused = null;

           View nextFocused = FocusFinder.getInstance().findNextFocus(this, currentFocused, direction);
           if(currentFocused!=null){
        	   if(currentFocused.getTag()!=null){
            	   if(direction==View.FOCUS_LEFT)
             	      nextFocused = mContainer.getChildAt((Integer) currentFocused.getTag()-1);
             	   else if(direction==View.FOCUS_RIGHT)
             		  nextFocused = mContainer.getChildAt((Integer) currentFocused.getTag()+1); 
        	   }
           }
           final int maxJump = getMaxScrollAmount();

           if (nextFocused != null && isWithinDeltaOfScreen(nextFocused, maxJump)) {
        	   if(nextFocused.getTag()!=null){
            	   int index = (Integer) nextFocused.getTag();
            	   if(direction==View.FOCUS_LEFT){
            		   right.setVisibility(View.VISIBLE);
            		   if(index==0){
            			   right.setVisibility(View.VISIBLE);
            			   left.setVisibility(View.INVISIBLE);
            		   }
            	   }
            	   else if(direction==View.FOCUS_RIGHT){
                	   left.setVisibility(View.VISIBLE);
                	   if(index==mContainer.getChildCount()-1){
                		   right.setVisibility(View.INVISIBLE);
                	   }
            	   }
        	   }
               nextFocused.getDrawingRect(mTempRect);
               offsetDescendantRectToMyCoords(nextFocused, mTempRect);
               int scrollDelta = computeScrollDeltaToGetChildRectOnScreen(mTempRect);
               doScrollX(scrollDelta);
               nextFocused.requestFocus(direction);
           }
           else{
        	   int scrollDelta = maxJump;
        	    if (direction == View.FOCUS_LEFT) {
                	
                	if(getScrollX() < scrollDelta){
                		left.setVisibility(View.INVISIBLE);
                		right.setVisibility(View.VISIBLE);
                        scrollDelta = getScrollX();
                	}
                	else if(getScrollX()==scrollDelta){
                		left.setVisibility(View.INVISIBLE);
                		right.setVisibility(View.VISIBLE);
                	}
                	else{
                		right.setVisibility(View.VISIBLE);
                	}
                }
                else if (direction == View.FOCUS_RIGHT && getChildCount() > 0) {

                    int daRight = getChildAt(0).getRight();

                    int screenRight = getScrollX() + getWidth();

                    if (daRight - screenRight < maxJump) {
                    	right.setVisibility(View.INVISIBLE);
                    	left.setVisibility(View.VISIBLE);
                        scrollDelta = daRight - screenRight;
                    }
                    else{
                    	left.setVisibility(View.VISIBLE);
                    }
                }
                if (scrollDelta == 0) {
                    return false;
                }
                doScrollX(direction == View.FOCUS_RIGHT ? scrollDelta : -scrollDelta); 
           }

           if (currentFocused != null && currentFocused.isFocused()
                   && isOffScreen(currentFocused)) {
               final int descendantFocusability = getDescendantFocusability();  // save
               setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
               requestFocus();
               setDescendantFocusability(descendantFocusability);  // restore
           }
        
        return true;
    }
    /**
     * @return whether the descendant of this scroll view is scrolled off
     *  screen.
     */
    private boolean isOffScreen(View descendant) {
        return !isWithinDeltaOfScreen(descendant, 0);
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
	public void changeSelection(int position) {
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
//	@Override
//	public boolean onTouchEvent(MotionEvent ev) {
//		// TODO Auto-generated method stub
//		return false;
//	}
	public void reset() {
		removeAllViews();
	}
	
}
