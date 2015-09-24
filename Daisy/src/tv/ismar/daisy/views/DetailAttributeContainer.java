package tv.ismar.daisy.views;

import android.content.Context;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import tv.ismar.daisy.R;
import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.models.ContentModel;

import java.util.LinkedHashMap;
import java.util.Map;

public class DetailAttributeContainer extends LinearLayout {
	
	private final static String TAG = "DetailAttributeContainer";
	
	private LinkedHashMap<String, String> mAttributeMap;
	
	private ContentModel mContentModel;
	private Context myContext;
	private float textsize;
	private float rate;
	public DetailAttributeContainer(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		myContext = context;
		rate = DaisyUtils.getVodApplication(context).getRate(context);
		textsize = myContext.getResources().getDimension(R.dimen.item_detail_introlabel_textsize)/rate;
		// TODO Auto-generated constructor stub
	}

	public DetailAttributeContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
		myContext = context;
		rate = DaisyUtils.getVodApplication(getContext()).getRate(getContext());
		textsize = myContext.getResources().getDimension(R.dimen.item_detail_introlabel_textsize)/rate;
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
    public void addAttributeForfilm(LinkedHashMap<String, String> attrMap, ContentModel m) {
        mAttributeMap = attrMap;
        mContentModel = m;
        buildAttributeListforfilm(mAttributeMap);
    }
	private void buildAttributeList(LinkedHashMap<String, String> attrMap) {

		for(Map.Entry<String, String> entry: attrMap.entrySet()){
			if(entry.getValue()==null || mContentModel.attributes.get(entry.getKey())==null){
				continue;
			}
			LinearLayout infoLine = new LinearLayout(getContext());
			LinearLayout.LayoutParams layoutParams;
			int width = getResources().getDimensionPixelSize(R.dimen.item_detail_attribute_width);
			layoutParams = new LinearLayout.LayoutParams(width,LinearLayout.LayoutParams.WRAP_CONTENT);
									
			layoutParams.topMargin =((int)(21/1));
			infoLine.setLayoutParams(layoutParams);
			infoLine.setOrientation(LinearLayout.HORIZONTAL);
			TextView itemName = new TextView(getContext());
			itemName.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
			itemName.setTextColor(0xffffffff);
			itemName.setTextSize(textsize);
			itemName.setText(mContentModel.attributes.get(entry.getKey())+":");
			infoLine.addView(itemName);
			TextView itemValue = new TextView(getContext());
            itemValue.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));				
			itemValue.setTextColor(0xffffffff);
			itemValue.setTextSize(textsize);
			itemValue.setText(entry.getValue());
			itemValue.setMaxLines(1);
			itemValue.setEllipsize(TruncateAt.END);
			itemValue.setSingleLine(true);
			infoLine.addView(itemValue);
			itemValue.setLineSpacing(3.4f, 1f);
			//lineSpacingExtra
			addView(infoLine);
		}
	}
    private void buildAttributeListforfilm(LinkedHashMap<String, String> attrMap) {
        int i=0;
        for(Map.Entry<String, String> entry: attrMap.entrySet()){
            if(entry.getValue()==null || mContentModel.attributes.get(entry.getKey())==null){
                continue;
            }
            if(i>=4){
                return;
            }
            LinearLayout infoLine = new LinearLayout(getContext());
            LinearLayout.LayoutParams layoutParams;
            int width = getResources().getDimensionPixelSize(R.dimen.item_detail_attribute_width);
            layoutParams = new LinearLayout.LayoutParams(width,LinearLayout.LayoutParams.WRAP_CONTENT);

            layoutParams.topMargin =((int)(11/1));
            infoLine.setLayoutParams(layoutParams);
            infoLine.setOrientation(LinearLayout.HORIZONTAL);
            TextView itemName = new TextView(getContext());
            itemName.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            itemName.setTextColor(0xffffffff);
            itemName.setTextSize(24/rate);
            itemName.setText(mContentModel.attributes.get(entry.getKey())+":");
            infoLine.addView(itemName);
            TextView itemValue = new TextView(getContext());
            itemValue.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            itemValue.setTextColor(0xffffffff);
            itemValue.setTextSize(24/rate);
            itemValue.setText(entry.getValue());
            itemValue.setMaxLines(1);
            itemValue.setEllipsize(TruncateAt.END);
            itemValue.setSingleLine(true);
            infoLine.addView(itemValue);
            itemValue.setLineSpacing(3.4f, 1f);
            //lineSpacingExtra
            addView(infoLine);
            i++;
        }
    }
    public void buildAttributeListOnZY(LinkedHashMap<String, String> attrMap,ContentModel m){
        mAttributeMap = attrMap;
        mContentModel = m;
        int i = 0;
        int count = attrMap.entrySet().size();
        LinearLayout infoLine = null;
        LinearLayout.LayoutParams layoutParamsInfo;


        for(Map.Entry<String, String> entry: attrMap.entrySet()){
            int layoutLeft = (int)(22/1);
            if(entry.getValue()==null || mContentModel.attributes.get(entry.getKey())==null){
                continue;
            }
            if(i%3==0){
                layoutLeft = 0;
                infoLine = new LinearLayout(getContext());
                layoutParamsInfo = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                if(i>=3)
                   layoutParamsInfo.topMargin = (int)(30/1);
                layoutParamsInfo.rightMargin = (int)(73/1);
                infoLine.setLayoutParams(layoutParamsInfo);
                infoLine.setOrientation(LinearLayout.HORIZONTAL);
                addView(infoLine);
            }
            addChildrenByZY(infoLine,mContentModel.attributes.get(entry.getKey()),entry.getValue(),layoutLeft,i);
            i++;
        }
    }
    public void  buildAttributeListOnFilm(LinkedHashMap<String, String> attrMap,ContentModel m){
        mAttributeMap = attrMap;
        mContentModel = m;
        int i = 0;
        int count = attrMap.entrySet().size();
        LinearLayout infoLine = null;
        LinearLayout.LayoutParams layoutParamsInfo;
        for(Map.Entry<String, String> entry: attrMap.entrySet()){
            if(entry.getValue()==null || mContentModel.attributes.get(entry.getKey())==null){
                continue;
            }
            int layoutLeft = (int)(9);
            if(i%2==0&&(i<=3)){
                layoutLeft = 0;
                infoLine = new LinearLayout(getContext());
                layoutParamsInfo = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                if(i>1)
                layoutParamsInfo.topMargin = (int)(22);

                infoLine.setLayoutParams(layoutParamsInfo);
                infoLine.setOrientation(LinearLayout.HORIZONTAL);
                addView(infoLine);
            }
            if(i>3){
                layoutLeft = 0;
                infoLine = new LinearLayout(getContext());
                infoLine.setOrientation(LinearLayout.HORIZONTAL);
                layoutParamsInfo = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                layoutParamsInfo.topMargin = (int)(22/1);
                infoLine.setLayoutParams(layoutParamsInfo);
                infoLine.setOrientation(LinearLayout.HORIZONTAL);
                addView(infoLine);
            }
            addChildren(infoLine,mContentModel.attributes.get(entry.getKey()),entry.getValue(),layoutLeft,i);
            i++;
        }

    }
    private void addChildrenByZY(ViewGroup infoLine,String key,String value,int distanceLeft,int position){
        LinearLayout.LayoutParams itemNameParams = null;
        TextView itemValue = new TextView(getContext());
        if((position+1)%3==0){
            itemNameParams = new LinearLayout.LayoutParams((int)(390/1), (int)(34/1));
        }
        else{
            itemNameParams = new LinearLayout.LayoutParams((int)(390/1), (int)(34/1));
        }
        itemNameParams.leftMargin = distanceLeft;
        // LinearLayout.LayoutParams itemValueParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        itemValue.setLayoutParams(itemNameParams);
        itemValue.setTextColor(0xffffffff);
        itemValue.setTextSize(textsize);
        itemValue.setText(key+" : " +value);
        itemValue.setMaxLines(1);
        itemValue.setEllipsize(TruncateAt.END);
        itemValue.setSingleLine(true);
        infoLine.addView(itemValue);
    }
    private void addChildren(ViewGroup infoLine,String key,String value,int distanceLeft,int lineNumber){
        LinearLayout.LayoutParams itemNameParams = null;

        if(distanceLeft>0){
            itemNameParams = new LinearLayout.LayoutParams((int)(289/1), (int)(27/1));
        }
        else if(distanceLeft==0){
            if(lineNumber==-1){
                itemNameParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            }
            else if(lineNumber>=4){
                itemNameParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,(int)(27/1));
            }
            else{
                itemNameParams = new LinearLayout.LayoutParams((int)(341/1), (int)(27/1));
            }
        }

       // TextView itemName = new TextView(getContext());
       // itemName.setLayoutParams(itemNameParams);
      //  itemNameParams.leftMargin = distanceLeft;
      //  itemName.setTextColor(0xffffffff);
      //  itemName.setTextSize(textsize);
      //  itemName.setText(key+":");
     //   infoLine.addView(itemName);
        itemNameParams.leftMargin = distanceLeft;
        TextView itemValue = new TextView(getContext());
       // LinearLayout.LayoutParams itemValueParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        itemValue.setLayoutParams(itemNameParams);
        itemValue.setTextColor(0xffffffff);
        itemValue.setTextSize((int)(24/rate));
        itemValue.setText(key+" : " +value);
        itemValue.setMaxLines(1);
        itemValue.setEllipsize(TruncateAt.END);
        itemValue.setSingleLine(true);
        infoLine.addView(itemValue);
    }

}
