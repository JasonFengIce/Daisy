package tv.ismar.daisy.views;

import java.util.LinkedHashMap;
import java.util.Map;

import tv.ismar.daisy.R;
import tv.ismar.daisy.VodApplication;
import tv.ismar.daisy.models.ContentModel;
import android.content.Context;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DetailAttributeContainer extends LinearLayout {
	
	private final static String TAG = "DetailAttributeContainer";
	
	private LinkedHashMap<String, String> mAttributeMap;
	
	private ContentModel mContentModel;
	private Context myContext;
	private float textsize; 
	public DetailAttributeContainer(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		myContext = context;
		textsize = myContext.getResources().getDimension(R.dimen.item_detail_introlabel_textsize)/VodApplication.rate;
		// TODO Auto-generated constructor stub
	}

	public DetailAttributeContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
		myContext = context;
		textsize = myContext.getResources().getDimension(R.dimen.item_detail_introlabel_textsize)/VodApplication.rate;
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
			LinearLayout.LayoutParams layoutParams;
			int width = getResources().getDimensionPixelSize(R.dimen.DetailAttributeContainer_Layout_W);
			layoutParams = new LinearLayout.LayoutParams(width,LinearLayout.LayoutParams.WRAP_CONTENT);
									
//			layoutParams.topMargin =(15f);
			infoLine.setLayoutParams(layoutParams);
			infoLine.setOrientation(LinearLayout.HORIZONTAL);
			TextView itemName = new TextView(getContext());
			itemName.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
			itemName.setTextColor(0xff999999);
			itemName.setTextSize(textsize);
			itemName.setText(mContentModel.attributes.get(entry.getKey())+":");
			infoLine.addView(itemName);
			TextView itemValue = new TextView(getContext());
            itemValue.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));				
			itemValue.setTextColor(0xffbbbbbb);
			itemValue.setTextSize(textsize);
			itemValue.setText(entry.getValue());
			itemValue.setMaxLines(2);
			itemValue.setEllipsize(TruncateAt.END);
			infoLine.addView(itemValue);
			addView(infoLine);
		}
	}

}
