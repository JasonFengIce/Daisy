package tv.ismar.daisy.views;

import java.util.ArrayList;

import tv.ismar.daisy.R;
import tv.ismar.daisy.core.ImageUtils;
import tv.ismar.daisy.core.NetworkUtils;
import tv.ismar.daisy.models.Item;
import tv.ismar.daisy.models.ItemList;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ItemListScrollView extends HorizontalScrollView implements OnFocusChangeListener, OnClickListener {

	private static final String TAG = "ItemListScrollView";
	
	public int mCurrentPosition;
	
	public ArrayList<ItemListContainer> mSectionContainerList;
	
	public LinearLayout mContainer;
	
	private static final int MAX_PRELOAD_COLUMN_COUNT = 2;

	private final Rect mTempRect = new Rect();
	
	private LayoutInflater mInflater;
	
	private static final int TEXT_COLOR_FOCUSED = 0xff000000;
	private static final int TEXT_COLOR_NOFOCUSED = 0xffbbbbbb;
	
	
	/**
     * Whether arrow scrolling is animated.
     */
    private boolean mSmoothScrollingEnabled = true;
    
    private OnSectionPrepareListener mOnSectionPrepareListener;
    private OnColumnChangeListener mOnColumnChangeListener;
    private OnItemClickedListener mOnItemClickedListener;
	
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
		mInflater = LayoutInflater.from(getContext());
		mSectionContainerList = new ArrayList<ItemListContainer>();
		mContainer = new LinearLayout(getContext());
		mContainer.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.MATCH_PARENT));
		
		this.addView(mContainer);
		this.setHorizontalFadingEdgeEnabled(true);
		this.setFadingEdgeLength(100);
	}
	
	private LinearLayout getLoadingView() {
		LinearLayout emptySpace = new LinearLayout(getContext());
		emptySpace.setLayoutParams(new FrameLayout.LayoutParams(1920, FrameLayout.LayoutParams.MATCH_PARENT));
		emptySpace.setGravity(Gravity.CENTER);
		View loadingView = mInflater.inflate(R.layout.loading_dialog_layout, null);
		loadingView.setLayoutParams(new LinearLayout.LayoutParams(372, 132));
		emptySpace.addView(loadingView);
		return emptySpace;
	}
	
	/**
	 * Use to initialize the child container with section.These sections may have no element yet.But we still create the holder for every section anyway. so we can
	 * easily add elements in the future. 
	 * @param itemList itemList contains all data for these views to be created. Make sure that itemList has {@link ItemList.slug} and {@link ItemList.title} not null.
	 */
	public void addSection(ItemList itemList, int index){
		String sectionTitle = itemList.title;
		
		// wrapper use to hold label ,shadow and container so that they can overlap each other.
		RelativeLayout wrapper = new RelativeLayout(getContext());
		wrapper.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));
		
		TextView titleTagTextView = getSectionTag(sectionTitle);
		titleTagTextView.setFocusable(false);
		ImageView shadow = new ImageView(getContext());
		shadow.setImageResource(R.drawable.section_shadow_bg);
		RelativeLayout.LayoutParams shadowLayoutParams = new RelativeLayout.LayoutParams(499, 810);
		shadowLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
		shadowLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
		shadow.setLayoutParams(shadowLayoutParams);
		shadow.setFocusable(false);
		
		ItemListContainer itemListContainer = new ItemListContainer(getContext());
		//if itemList.objects == null add an mEmptySpace to fill the position space. and show a loading ProgressBar.
		addViewsToContainer(itemListContainer, itemList.objects, index);
			
		RelativeLayout.LayoutParams itemListContainerLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT);
		itemListContainerLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
		itemListContainerLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
		itemListContainerLayoutParams.leftMargin = 65;
		itemListContainer.setLayoutParams(itemListContainerLayoutParams);
		itemListContainer.setFocusable(false);
		wrapper.addView(shadow);
		wrapper.addView(titleTagTextView);
		wrapper.addView(itemListContainer);
		wrapper.setFocusable(false);
		mContainer.addView(wrapper);
		mSectionContainerList.add(itemListContainer);
	}
	
	/*
	 * Add views to container.
	 */
	private void addViewsToContainer(ItemListContainer container, ArrayList<Item> items, Integer position){
		if(items==null) {
			container.addView(getLoadingView());
			return;
		}
		for(int i=0; i<items.size(); i++) {
			Item item = items.get(i);
			String title = item.title;
			RelativeLayout cellHolder = (RelativeLayout) mInflater.inflate(R.layout.list_view_item, null);
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(348, 252);
			layoutParams.rightMargin = 81;
			cellHolder.setLayoutParams(layoutParams);
			cellHolder.setTag(position);
			cellHolder.setFocusable(true);
			TextView titleView =(TextView) cellHolder.findViewById(R.id.list_item_title);
			titleView.setTag(item.item_url);
			titleView.setText(title);
			ImageView previewImage = (ImageView) cellHolder.findViewById(R.id.list_item_preview_img);
			previewImage.setTag(item.adlet_url);
//			new GetImageTask().execute(previewImage);
			cellHolder.setOnFocusChangeListener(this);
			cellHolder.setOnClickListener(this);
			container.addView(cellHolder, i);
		}
	}
	
	class GetImageTask extends AsyncTask<ImageView, Void, Bitmap> {
		
		private ImageView view;

		@Override
		protected Bitmap doInBackground(ImageView... params) {
			view = params[0];
			String url = (String) view.getTag();
			return ImageUtils.getBitmapFromInputStream(NetworkUtils.getInputStream(url), 319, 179);
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			if(result!=null) {
				view.setImageBitmap(result);
			}
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
		this.requestLayout();
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
		textView.setLineSpacing(2, 1);
		textView.setText(newTitle);
		textView.setTextSize(35);
		textView.setTextColor(TEXT_COLOR_NOFOCUSED);
		textView.setGravity(Gravity.CENTER);
		int height = 0 ;
		if(title.length()<=2){
			height = 118;
			textView.setBackgroundResource(R.drawable.label_two_words);
		} else if (title.length()==3) {
			height = 166;
			textView.setBackgroundResource(R.drawable.label_three_words);
		} else if (title.length()==4) {
			height = 214;
			textView.setBackgroundResource(R.drawable.label_four_words);
		}
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(53, height);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
		layoutParams.leftMargin = 17;
		textView.setLayoutParams(layoutParams);
		return textView;
	}
	
	public View findNextFocusedView(int position,int col, int row, int direction) {
		View nextFocused = null;
		ItemListContainer currentContainer = mSectionContainerList.get(position);
		if(direction==View.FOCUS_RIGHT && col < currentContainer.getChildCount()-1) {
			LinearLayout nextCol = (LinearLayout) currentContainer.getChildAt(col+1);
			if(nextCol.getChildCount()<row+1) {
				nextFocused = nextCol.getChildAt(nextCol.getChildCount()-1);
			} else {
				nextFocused = nextCol.getChildAt(row);
			}
		} else if(direction==View.FOCUS_LEFT && col > 0) {
			LinearLayout nextCol = (LinearLayout) currentContainer.getChildAt(col-1);
			if(nextCol.getChildCount()<row+1) {
				nextFocused = nextCol.getChildAt(nextCol.getChildCount()-1);
			} else {
				nextFocused = nextCol.getChildAt(row);
			}
		} else {
		
			if(direction == View.FOCUS_RIGHT && position < mSectionContainerList.size()-1) {
				ItemListContainer nextItemListContainer = mSectionContainerList.get(position + 1);
				LinearLayout nextColumn = (LinearLayout) nextItemListContainer.getChildAt(0);
				nextFocused = nextColumn.getChildAt(0);
//				moveToSection(position+1, direction, nextColumn.getChildAt(0));
//				int nextCol = (Integer) nextColumn.getTag();
//				if(mOnColumnChangeListener!=null) {
//		        	mOnColumnChangeListener.onColumnChanged(position+1, nextCol, nextItemListContainer.getChildCount());
//		        }
			} else if(direction == View.FOCUS_LEFT && position > 0) {
				ItemListContainer nextItemListContainer = mSectionContainerList.get(position - 1);
				LinearLayout nextColumn = (LinearLayout) nextItemListContainer.getChildAt(nextItemListContainer.getChildCount()-1);
				nextFocused = nextColumn.getChildAt(0);
//				moveToSection(position-1, direction, nextColumn.getChildAt(0));
//				int nextCol = (Integer) nextColumn.getTag();
//				if(mOnColumnChangeListener!=null) {
//		        	mOnColumnChangeListener.onColumnChanged(position-1, nextCol, nextItemListContainer.getChildCount());
//		        }
			}
		}
		return nextFocused;
	}
	
	private View prepareSection(View currentFocused, int direction){
		int position = (Integer) currentFocused.getTag();
		ItemListContainer itemListContainer = mSectionContainerList.get(position);
		LinearLayout colHolder = (LinearLayout) currentFocused.getParent();
		int column = (Integer) colHolder.getTag();
		int totalColumns = itemListContainer.getChildCount();
		if(direction==View.FOCUS_RIGHT && (totalColumns - (column+1) <= MAX_PRELOAD_COLUMN_COUNT)) {
			 if(mOnSectionPrepareListener!=null && position<mSectionContainerList.size()-1){
				 mOnSectionPrepareListener.onPrepareNeeded(position + 1);
			 }
		} else if(direction==View.FOCUS_LEFT && (column+1 <= MAX_PRELOAD_COLUMN_COUNT)) {
			if(mOnSectionPrepareListener!=null && position > 0){
				mOnSectionPrepareListener.onPrepareNeeded(position - 1);
			 }
		}
		int row = 0;
		for(int i=0;i<colHolder.getChildCount();i++) {
			if(currentFocused == getChildAt(i)){
				row = i;
				break;
			}
		}
		View nextFocused = findNextFocusedView(position, column, row, direction);
		
		if(nextFocused!=null) {
			// indicate the nextfocus column and its postion's total column. to notify the watcher of OnColumnChangeListener
	        int nextPosition = (Integer) nextFocused.getTag();
//	        Log.d(TAG, "nextFocused position:"+nextPosition);
	        ItemListContainer nextItemListContainer = mSectionContainerList.get(nextPosition);
	        int nextTotalColumns = nextItemListContainer.getChildCount();
	        View nextColHolder = (View) nextFocused.getParent();
	        int nextColumn = (Integer) nextColHolder.getTag();
	        mCurrentPosition = nextPosition;
//	        Log.d(TAG, "nextFocused column:"+nextColumn+"  totalColumns:"+nextTotalColumns);
	        if(mOnColumnChangeListener!=null) {
	        	mOnColumnChangeListener.onColumnChanged(nextPosition, nextColumn, nextTotalColumns);
	        }
		}
		return nextFocused;
	}
	
	public int getTotalColumnCount(int position) {
		return mSectionContainerList.get(position).getChildCount();
	}
	
	@Override
	public boolean arrowScroll(int direction) {

        View currentFocused = findFocus();
        if (currentFocused == this) currentFocused = null;
        
        View nextFocused = null;
        if(currentFocused!=null){
        	nextFocused = prepareSection(currentFocused, direction);
        }
        
//        View nextFocused = FocusFinder.getInstance().findNextFocus(this, currentFocused, direction);
        
        
        final int maxJump = getMaxScrollAmount();

        if (nextFocused != null && isWithinDeltaOfScreen(nextFocused, maxJump)) {
            nextFocused.getDrawingRect(mTempRect);
            offsetDescendantRectToMyCoords(nextFocused, mTempRect);
            int scrollDelta = computeScrollDeltaToGetChildRectOnScreen(mTempRect);
            if(scrollDelta>0) {
            	scrollDelta += 137;
            } else if(scrollDelta<0){
            	scrollDelta -= 137;
            }
            doScrollX(scrollDelta);
            nextFocused.requestFocus(direction);
        } else {
            // no new focus
//            int scrollDelta = maxJump;
//
//            if (direction == View.FOCUS_LEFT && getScrollX() < scrollDelta) {
//                scrollDelta = getScrollX();
//            } else if (direction == View.FOCUS_RIGHT && getChildCount() > 0) {
//
//                int daRight = getChildAt(0).getRight();
//
//                int screenRight = getScrollX() + getWidth();
//
//                if (daRight - screenRight < maxJump) {
//                    scrollDelta = daRight - screenRight;
//                }
//            }
//            if (scrollDelta == 0) {
//                return false;
//            }
//            doScrollX(direction == View.FOCUS_RIGHT ? scrollDelta : -scrollDelta);
        	return false;
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

	

	/**
     * Use to move the next section(indexed by position).scroll the screen.
     * @param position
     * @param direction
     */
    public void moveToSection(int position, int direction, View nextFocused) {
    	ItemListContainer itemListContainer = mSectionContainerList.get(position);
		itemListContainer.getDrawingRect(mTempRect);
		offsetDescendantRectToMyCoords(itemListContainer, mTempRect);
		int scrollLeft = getScrollX();
		int scrollRight = scrollLeft + getWidth();
		int delta = 0;
    	if(direction==View.FOCUS_LEFT) {
    		delta = mTempRect.right - scrollRight - 137;
    	} else if (direction==View.FOCUS_RIGHT) {
    		delta = mTempRect.left - scrollLeft - 137;
    	}
    	doScrollX(delta);
    	nextFocused.requestFocus();
    }
    
    /**
     * Use to move the section that are not nearby.
     * @param position
     * @param direction
     */
    public void jumpToSection(int position) {
    	ItemListContainer itemListContainer = mSectionContainerList.get(position);
    	itemListContainer.getDrawingRect(mTempRect);
    	offsetDescendantRectToMyCoords(itemListContainer, mTempRect);
    	int delta = mTempRect.left - 137 - getScrollX();
    	mCurrentPosition = position;
    	doScrollX(delta);
//    	LinearLayout newColumn = (LinearLayout) itemListContainer.getChildAt(0);
//    	View newFocused = newColumn.getChildAt(0);
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
    
    public void setOnColumnChangeListener(OnColumnChangeListener listener) {
    	mOnColumnChangeListener = listener;
    }
    
    /**
     * When a focus change to the next column.
     * @author bob
     *
     */
    public interface OnColumnChangeListener {
    	public void onColumnChanged(int position, int column, int totalColumn);
    }

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		TextView titleView = (TextView) v.findViewById(R.id.list_item_title);
		if(hasFocus) {
			titleView.setTextColor(TEXT_COLOR_FOCUSED);
			v.setBackgroundResource(R.drawable.list_item_bg_hot);
		} else {
			titleView.setTextColor(TEXT_COLOR_NOFOCUSED);
			v.setBackgroundResource(android.R.color.transparent);
		}
	}

	@Override
	public void onClick(View v) {
		View titleView = v.findViewById(R.id.list_item_title);
		String url = (String) titleView.getTag();
		if(mOnItemClickedListener!=null) {
			mOnItemClickedListener.onItemClicked(url);
		}
	}
	
	public void setOnItemClickedListener(OnItemClickedListener listener) {
		mOnItemClickedListener = listener;
	}
	
	public interface OnItemClickedListener {
		public void onItemClicked(String url);
	}
	
}
