package tv.ismar.daisy.views;

import tv.ismar.daisy.models.Section;
import tv.ismar.daisy.models.SectionList;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

public class ScrollableSectionList extends HorizontalScrollView {
	
	private LinearLayout mContainer;

	public ScrollableSectionList(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public ScrollableSectionList(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public ScrollableSectionList(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public void init(SectionList sectionList) {
		mContainer = new LinearLayout(getContext());
		mContainer.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, 66));
		for(int i=0; i<sectionList.size();i++) {
			
		}
	}
	
	private void getSectionLabelLayout(Section section) {
		
	}
}
