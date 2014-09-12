package org.sakuratya.horizontal.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.AdapterView.OnItemSelectedListener;
public class DGridView extends GridView implements OnItemSelectedListener{

	
	//private Rect mSelector;
	private int currentSecected = -1;
	private ListAdapter adapter=null;
	protected Drawable mSelector;
	private int lastPosition=-1;;
	private int first =0;
	private boolean isdraw = false;
	public DGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mSelector = getSelector();
		if(mSelector!=null) {
			mSelector.setCallback(null);
			unscheduleDrawable(mSelector);
			mSelector.setCallback(this);
			mSelector.setState(getDrawableState());
		}

setOnItemSelectedListener(this);
	}

	public DGridView(Context context) {
		// TODO Auto-generated constructor stub
		super(context);
	}
	



	@Override
	public void setAdapter(ListAdapter adapter) {
		// TODO Auto-generated method stub
		this.adapter = adapter;
		super.setAdapter(adapter);
	}
	protected boolean shouldShowSelector() {
		return hasFocus();
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		Log.i("zhangjiqiang", "onItemSelected=="+position);
		currentSecected = position;
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
    public boolean verifyDrawable(Drawable dr) {
        return mSelector == dr || super.verifyDrawable(dr);
    }
	
	@Override
	protected void dispatchDraw(Canvas canvas) {
		if(shouldShowSelector()){
			View v = getChildAt(currentSecected);
			if(v!=null&&v.isSelected()){
				Log.i("zhnagjiqiang", "draw secector");
				mSelector.setBounds(new Rect(v.getLeft(),v.getTop()-30,v.getRight(),v.getBottom()-30));
				mSelector.draw(canvas);
				lastPosition = currentSecected;
			}
		}
	
		super.dispatchDraw(canvas);
	}
	@Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (mSelector != null) {
            mSelector.setState(getDrawableState());
        }
    }
	
	
	@Override
	protected int[] onCreateDrawableState(int extraSpace) {
		return super.onCreateDrawableState(extraSpace);
	}
	

	
	@Override
    protected void dispatchSetPressed(boolean pressed) {
        // Don't dispatch setPressed to our children. We call setPressed on ourselves to
        // get the selector in the right state, but we don't want to press each child.
    }
	
	
	@Override
	public View getSelectedView() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSelection(int arg0) {
		// TODO Auto-generated method stub
		
	}
}
