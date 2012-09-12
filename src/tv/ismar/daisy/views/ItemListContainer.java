package tv.ismar.daisy.views;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.AbsListView.RecyclerListener;

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
	
	private RecycleBin mRecycler = new RecycleBin();
	
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
	
	public View makeAndAddView(int index) {
		int col = getCurrentCol(index);
		int row = getCurrentRow(index);
		
		return null;
	}
	
	private void setupChild(int col, int row) {
		int rightMargin = 81;
		int childWidth = 348;
		int childHeight = 252;
		
	}
	
	public View getViewInSelection(int col, int row) {
		int index = col * 3 + row;
		View child = mRecycler.getActiveView(index);
		if(child==null && index < mCellCount) {
			child = mRecycler.getScrapView(index);
			((ItemCell) child).isActive = true;
			((ItemCell) child).isRecycled = true;
		} else {
			((ItemCell) child).isActive = true;
			((ItemCell) child).isRecycled = true;
		}
		return child;
	}
	
	/**
     * A RecyclerListener is used to receive a notification whenever a View is placed
     * inside the RecycleBin's scrap heap. This listener is used to free resources
     * associated to Views placed in the RecycleBin.
     *
     * @see android.widget.AbsListView.RecycleBin
     * @see android.widget.AbsListView#setRecyclerListener(android.widget.AbsListView.RecyclerListener)
     */
    public static interface RecyclerListener {
        /**
         * Indicates that the specified View was moved into the recycler's scrap heap.
         * The view is not displayed on screen any more and any expensive resource
         * associated with the view should be discarded.
         *
         * @param view
         */
        void onMovedToScrapHeap(View view);
    }
    
	class RecycleBin {
		private RecyclerListener mRecyclerListener;
		
		    /**
		 * The position of the first view stored in mActiveViews.
		 */
		private int mFirstActivePosition;
		
		/**
		 * Views that were on screen at the start of layout. This array is populated at the start of
		 * layout, and at the end of layout all view in mActiveViews are moved to mScrapViews.
		 * Views in mActiveViews represent a contiguous range of Views, with position of the first
		 * view store in mFirstActivePosition.
		 */
		private View[] mActiveViews = new View[0];
        
        private ArrayList<View> mCurrentScrap = new ArrayList<View>();
        
        public void markChildrenDirty() {
            final ArrayList<View> scrap = mCurrentScrap;
            final int scrapCount = scrap.size();
            for (int i = 0; i < scrapCount; i++) {
                scrap.get(i).forceLayout();
            }
        }
        
        public boolean shouldRecycleViewType(int viewType) {
            return viewType >= 0;
        }
        
        /**
         * Clears the scrap heap.
         */
        void clear() {
            final ArrayList<View> scrap = mCurrentScrap;
            final int scrapCount = scrap.size();
            for (int i = 0; i < scrapCount; i++) {
                removeDetachedView(scrap.remove(scrapCount - 1 - i), false);
            }
            
        }
        
        /**
         * Fill ActiveViews with all of the children of the AbsListView.
         *
         * @param childCount The minimum number of views mActiveViews should hold
         * @param firstActivePosition The position of the first view that will be stored in
         *        mActiveViews
         */
        void fillActiveViews(int childCount, int firstActivePosition) {
            if (mActiveViews.length < childCount) {
                mActiveViews = new View[childCount];
            }
            mFirstActivePosition = firstActivePosition;

            final View[] activeViews = mActiveViews;
            for (int i = 0; i < childCount; i++) {
                View child = getChildAt(i);
                activeViews[i] = child;
            }
        }
        

        /**
         * Get the view corresponding to the specified position. The view will be removed from
         * mActiveViews if it is found.
         *
         * @param position The position to look up in mActiveViews
         * @return The view if it is found, null otherwise
         */
        View getActiveView(int position) {
            int index = position - mFirstActivePosition;
            final View[] activeViews = mActiveViews;
            if (index >=0 && index < activeViews.length) {
                final View match = activeViews[index];
                activeViews[index] = null;
                return match;
            }
            return null;
        }


        /**
         * @return A view from the ScrapViews collection. These are unordered.
         */
        View getScrapView(int position) {
        	int size = mCurrentScrap.size();
        	if (size > 0) {
                // See if we still have a view for this position.
                for (int i=0; i<size; i++) {
                    View view = mCurrentScrap.get(i);
                    if (((ItemCell)view).index == position) {
                    	mCurrentScrap.remove(i);
                        return view;
                    }
                }
                return mCurrentScrap.remove(size - 1);
            } else {
                return null;
            }
        }
        
        /**
         * Put a view into the ScapViews list. These views are unordered.
         *
         * @param scrap The view to add
         */
        void addScrapView(View scrap, int position) {
            
            ((ItemCell) scrap).index = position;
            
            scrap.onStartTemporaryDetach();
            mCurrentScrap.add(scrap);
            

            if (mRecyclerListener != null) {
                mRecyclerListener.onMovedToScrapHeap(scrap);
            }
        }
        
        /**
         * Move all views remaining in mActiveViews to mScrapViews.
         */
        void scrapActiveViews() {
            final View[] activeViews = mActiveViews;
            final boolean hasListener = mRecyclerListener != null;

            ArrayList<View> scrapViews = mCurrentScrap;
            final int count = activeViews.length;
            for (int i = count - 1; i >= 0; i--) {
                final View victim = activeViews[i];
                if (victim != null) {
                    
                    activeViews[i] = null;
                    victim.onStartTemporaryDetach();
                    ((ItemCell)victim).index = mFirstActivePosition + i;
                    scrapViews.add(victim);

                    if (hasListener) {
                        mRecyclerListener.onMovedToScrapHeap(victim);
                    }

                    if (ViewDebug.TRACE_RECYCLER) {
                        ViewDebug.trace(victim,
                                ViewDebug.RecyclerTraceType.MOVE_FROM_ACTIVE_TO_SCRAP_HEAP,
                                mFirstActivePosition + i, -1);
                    }
                }
            }

            pruneScrapViews();
        }
        
        /**
         * Makes sure that the size of mScrapViews does not exceed the size of mActiveViews.
         * (This can happen if an adapter does not recycle its views).
         */
        private void pruneScrapViews() {
            final int maxViews = mActiveViews.length;
            final ArrayList<View> scrapPile = mCurrentScrap;
            int size = scrapPile.size();
            final int extras = size - maxViews;
            size--;
            for (int j = 0; j < extras; j++) {
                removeDetachedView(scrapPile.remove(size--), false);
            }
        }
        
        /**
         * Puts all views in the scrap heap into the supplied list.
         */
        void reclaimScrapViews(List<View> views) {
            views.addAll(mCurrentScrap);
        }
        
	}
	
}
