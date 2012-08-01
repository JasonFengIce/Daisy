package tv.ismar.daisy.views;

import java.util.ArrayList;

import tv.ismar.daisy.models.Item;
import tv.ismar.daisy.models.ItemList;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.FocusFinder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ItemListScrollView extends HorizontalScrollView {
	
	public ArrayList<ItemListContainer> mSectionContainerList;
	
	public LinearLayout mContainer;
	
	private static final int MAX_PRELOAD_COLUMN_COUNT = 2;

	private final Rect mTempRect = new Rect();
	
	/**
     * Whether arrow scrolling is animated.
     */
    private boolean mSmoothScrollingEnabled = true;
    
    private OnSectionPrepareListener mOnSectionPrepareListener;

	
	public ItemListScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public ItemListScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ItemListScrollView(Context context) {
		super(context);
		init();
	}

	public void init() {
		mSectionContainerList = new ArrayList<ItemListContainer>();
		mContainer = new LinearLayout(getContext());
		mContainer.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.MATCH_PARENT));
		this.addView(mContainer);
		this.setHorizontalFadingEdgeEnabled(true);
		this.setFadingEdgeLength(100);
	}
	
	/**
	 * Use to initialize the child container with section.These sections may have no element yet.But we still create the holder for every section anyway. so we can
	 * easily add elements in the future. 
	 * @param itemList itemList contains all data for these views to be created. Make sure that itemList has {@link ItemList.slug} and {@link ItemList.title} not null.
	 */
	public void addSection(ItemList itemList, int index){
		String sectionTitle = itemList.title;
		TextView titleTagTextView = getSectionTag(sectionTitle);
		ItemListContainer itemListContainer = new ItemListContainer(getContext());
		if(itemList.objects!=null){
			addViewsToContainer(itemListContainer, itemList.objects, index);
		}
		itemListContainer.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));
		
		mContainer.addView(titleTagTextView);
		mContainer.addView(itemListContainer);
		mSectionContainerList.add(itemListContainer);
	}
	
	/*
	 * Add views to container.
	 */
	private void addViewsToContainer(ItemListContainer container, ArrayList<Item> items, Integer position){
		for(int i=0; i<items.size(); i++) {
			Item item = items.get(i);
			String title = item.title;
			Button button = new Button(getContext());
			button.setLayoutParams(new LinearLayout.LayoutParams(320, 180));
			button.setText(title);
			button.setTag(position);
			container.addView(button, i);
		}
	}
	
	/**
	 * Use to show the given position's section.
	 * @param itemList
	 * @param position
	 */
	public void updateSection(ItemList itemList, int position) {
		ItemListContainer itemListContainer = mSectionContainerList.get(position);
		if(itemListContainer.getTotalCellCount()!=itemList.objects.size()){
			itemListContainer.removeAllViews();
			addViewsToContainer(itemListContainer, itemList.objects, position);
		}
	}
	
	
	/*
	 * return a TextView that shows the title words in a vertical direction.
	 */
	private TextView getSectionTag(String title) {
		TextView textView = new TextView(getContext());
		char[] nChar = new char[title.length()*2-1];
		int index = 0;
		for(int i=0;i<nChar.length;i++) {
			if(i%2==0){
				nChar[i] = title.charAt(index);
				index++;
			} else {
				nChar[i] = '\n';
			}
		}
		String newTitle = String.valueOf(nChar);
		textView.setLineSpacing(3, 5);
		textView.setText(newTitle);
		return textView;
	}
	
	private void prepareSection(View currentFocused, int direction){
		int position = (Integer) currentFocused.getTag();
		ItemListContainer itemListContainer = mSectionContainerList.get(position);
		View colHolder = (View) currentFocused.getParent();
		int column = (Integer) colHolder.getTag();
		int totalColumns = itemListContainer.getChildCount();
		if(direction==View.FOCUS_RIGHT && (totalColumns - (column+1) <= MAX_PRELOAD_COLUMN_COUNT)) {
			 if(mOnSectionPrepareListener!=null && position<mSectionContainerList.size()-1){
				 mOnSectionPrepareListener.onPrepareNeeded(position + 1);
			 }
		} else if(direction==View.FOCUS_LEFT && (column+1 <= FOCUS_RIGHT)) {
			if(mOnSectionPrepareListener!=null && position > 0){
				mOnSectionPrepareListener.onPrepareNeeded(position - 1);
			 }
		}
	}
	
	@Override
	public boolean arrowScroll(int direction) {

        View currentFocused = findFocus();
        if (currentFocused == this) currentFocused = null;

        View nextFocused = FocusFinder.getInstance().findNextFocus(this, currentFocused, direction);
        
        if(currentFocused!=null){
        	prepareSection(currentFocused, direction);
        }

        final int maxJump = getMaxScrollAmount();

        if (nextFocused != null && isWithinDeltaOfScreen(nextFocused, maxJump)) {
            nextFocused.getDrawingRect(mTempRect);
            offsetDescendantRectToMyCoords(nextFocused, mTempRect);
            int scrollDelta = computeScrollDeltaToGetChildRectOnScreen(mTempRect);
            doScrollX(scrollDelta);
            nextFocused.requestFocus(direction);
        } else {
            // no new focus
            int scrollDelta = maxJump;

            if (direction == View.FOCUS_LEFT && getScrollX() < scrollDelta) {
                scrollDelta = getScrollX();
            } else if (direction == View.FOCUS_RIGHT && getChildCount() > 0) {

                int daRight = getChildAt(0).getRight();

                int screenRight = getScrollX() + getWidth();

                if (daRight - screenRight < maxJump) {
                    scrollDelta = daRight - screenRight;
                }
            }
            if (scrollDelta == 0) {
                return false;
            }
            doScrollX(direction == View.FOCUS_RIGHT ? scrollDelta : -scrollDelta);
        }

        if (currentFocused != null && currentFocused.isFocused()
                && isOffScreen(currentFocused)) {
            // previously focused item still has focus and is off screen, give
            // it up (take it back to ourselves)
            // (also, need to temporarily force FOCUS_BEFORE_DESCENDANTS so we are
            // sure to
            // get it)
            final int descendantFocusability = getDescendantFocusability();  // save
            setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
            requestFocus();
            setDescendantFocusability(descendantFocusability);  // restore
        }
        return true;
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
    
    /**
     * @return whether the descendant of this scroll view is scrolled off
     *  screen.
     */
    private boolean isOffScreen(View descendant) {
        return !isWithinDeltaOfScreen(descendant, 0);
    }
    
    public void setOnSectionPrepareListener(OnSectionPrepareListener listener){
    	mOnSectionPrepareListener = listener;
    }
    
    /**
     * When current focused view is nearly the edge of currentSection. this listener will be invoked to notify the watcher
     * update the next section's view.
     * @author bob
     *
     */
    public interface OnSectionPrepareListener {
    	public void onPrepareNeeded(int position);
    }
    
}
