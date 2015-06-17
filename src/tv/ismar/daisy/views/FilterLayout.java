package tv.ismar.daisy.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import org.json.JSONException;
import org.json.JSONObject;
import tv.ismar.daisy.R;

import java.util.ArrayList;

/**
 * Created by zhangjiqiang on 15-6-9.
 */
public class FilterLayout extends LinearLayout {
    private ViewInfoListener mListener;
    private HorizontalScrollView genre;
    private HorizontalScrollView air_date;
    private HorizontalScrollView area;
    private ArrayList<String> mConditions;
    private String content_model;
    private Button submitBtn;
    public FilterLayout(Context context) {
        super(context);
        mConditions = new ArrayList<String>();
        LayoutInflater mInflater = LayoutInflater.from(context);
        View myView;
        myView = mInflater.inflate(R.layout.filter_view,null);
        addView(myView);

    }
    public FilterLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FilterLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void init(String info){
         genre = (HorizontalScrollView)findViewById(R.id.genre);
         air_date = (HorizontalScrollView)findViewById(R.id.air_date);
         area = (HorizontalScrollView)findViewById(R.id.area);
         submitBtn = (Button)findViewById(R.id.filter_submit);
         submitBtn.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View view) {
                //submit
                String url = "http://cordadmintest.tvxio.com/api/tv/filtrate/$"+content_model+"/";
                String condition="";
                int count = mConditions.size();
                for(int i=0;i<count;i++){
                    condition += mConditions.get(i) + "!";
                }
                condition = condition.substring(0,condition.length()-1);
                if(mListener!=null)
                    mListener.onFilterSubmitClick(condition);
            }
        });
        try {
            JSONObject ss = new JSONObject(info);
            JSONObject attributesObj = ss.getJSONObject("attributes");
            if(attributesObj.has("genre")){

            }
            if(attributesObj.has("air_date")){

            }
            if(attributesObj.has("area")){

            }
            if(this.mListener!=null)
                mListener.onViewInfoCallBack(450);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public interface ViewInfoListener{
        public void onViewInfoCallBack(int height);
        public void onFilterSubmitClick(String condition);
    }
    public void setViewInfoListener(ViewInfoListener l){
        this.mListener = l;
    }
}
