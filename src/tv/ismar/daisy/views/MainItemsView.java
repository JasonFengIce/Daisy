package tv.ismar.daisy.views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

public class MainItemsView extends ViewGroup {

	private final static int INVALID_SCREEN = -1; 
	private int mNextScreen = INVALID_SCREEN; //First initialization, no next screen
	private int mCurrentScreen; //current screen is 0 when initialization
	private int mDefaultScreen; // default screen can specified,this we simply set to 0
	private Scroller mScroller; //the scroller controls the scrolling animation.
	
	private boolean mFirstLayout = true;
	
	public MainItemsView(Context context) {
		super(context);
		initView();
	}

	public MainItemsView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView();
	}

	public MainItemsView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}
	
	private void initView() {
		Context context = getContext();
		mScroller = new Scroller(context);
		mCurrentScreen = mDefaultScreen;
		
	}
	
	public int getCurrentScreen() {
		return mCurrentScreen;
	}
	

	@Override
	protected void dispatchDraw(Canvas canvas) {
		boolean fastDraw = mNextScreen == INVALID_SCREEN;
		if(fastDraw) {
			drawChild(canvas, getChildAt(mCurrentScreen), getDrawingTime());
		} else {
			final long drawingTime = getDrawingTime();
			final int childCount = getChildCount();
			
			for(int i=0; i< childCount; i++ ) {
				View child = getChildAt(i);
				drawChild(canvas, child, drawingTime);
			}
		

		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		final int width = MeasureSpec.getSize(widthMeasureSpec);
		int childCount = getChildCount();
		for(int i=0; i< childCount;i++) {
			View child = getChildAt(i);
			child.measure(widthMeasureSpec, heightMeasureSpec);
		}
		
		if(mFirstLayout){
			scrollTo(mCurrentScreen*width, 0);
			mFirstLayout = false;
		}
		
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		int childLeft = 0;
		int childCount = getChildCount();
		for(int i=0;i<childCount;i++){
			View child = getChildAt(i);
			if(child.getVisibility()!=View.GONE){
				int childWidth = child.getMeasuredWidth();
				child.layout(childLeft, 0, childLeft + childWidth, child.getMeasuredHeight());
				childLeft += childWidth;
			}
		}
	}

	@Override
	public void computeScroll() {
		if(mScroller.computeScrollOffset()) {
			int currX = mScroller.getCurrX();
			
			int scrollX = getScrollX();
			//if unfortunately the  currX and scrollX is just equal before reach the scroller's final goal.
			//we can invalidate the whole view ,that will invoke computeScroll() method again.Then we have a
			//chance to re-compare the two values.
			if(currX!=scrollX) {
				scrollTo(mScroller.getCurrX(), 0);
			} else {
				invalidate();
			}
		} else if(mNextScreen != INVALID_SCREEN) {
			//if mNextScreen != INVALID_SCREEN, that means the scroll has finished, and current screen has
			//become the target screen.So we have to set mCurrentScreen to target value.
			mCurrentScreen = Math.max(0, Math.min(mNextScreen, getChildCount()-1));
			clearChildrenCache();
			mNextScreen = INVALID_SCREEN;
		} 
	}

	public void snapToView(int whichScreen){
		snapToScreen(whichScreen, 0);
	}
	
	private void snapToScreen(int whichScreen, int velocity) {
		//whichScreen should be between 0 and getChildCount()-1
		whichScreen = Math.max(0, Math.min(whichScreen, getChildCount()-1));
		enableChildrenCache();
		View focusedChild = getFocusedChild();
		if(focusedChild != null && whichScreen != mCurrentScreen && focusedChild == getChildAt(mCurrentScreen)) {
			focusedChild.clearFocus();
		}
		mNextScreen = whichScreen;
		final int screenDelta = Math.max(1, Math.abs(whichScreen - mCurrentScreen));
		final int newX = whichScreen * getWidth();
		final int delta = newX - getScrollX();
		int duration = (screenDelta + 1) * 100;		
		velocity = Math.abs(velocity);
//		if(velocity > 0) {
//			duration += (duration/(velocity/BASELINE_FLING_VELOCITY)) * FLING_VELOCITY_INFLUENCE;
//		} else {
//			duration += 100;
//		}
		mScroller.startScroll(getScrollX(), 0, delta, 0, duration);
		invalidate();
	}
	
	private void enableChildrenCache() {
		int childCount = getChildCount();
		for(int i=0; i<childCount;i++){
			View layout = getChildAt(i);
			layout.setDrawingCacheEnabled(true);
			if(layout instanceof ViewGroup){
				((ViewGroup)layout).setAlwaysDrawnWithCacheEnabled(true);
			}
		}
	}

	private void clearChildrenCache() {
		final int childCount = getChildCount();
		for(int i=0; i< childCount; i++){
			View layout = getChildAt(i);
			if(layout instanceof ViewGroup){
				((ViewGroup)layout).setAlwaysDrawnWithCacheEnabled(false);
			}
		}
		
	}

}
