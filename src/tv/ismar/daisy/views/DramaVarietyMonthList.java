package tv.ismar.daisy.views;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import tv.ismar.daisy.BaseActivity;
import tv.ismar.daisy.R;
import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.models.Item;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by zhangjiqiang on 15-7-10.
 */
public class DramaVarietyMonthList extends BaseActivity {

    private Item mItem;
    private int currentMonth = 0;
    private HashMap<String,ArrayList<Item>> maps;
    private LinearLayout month_section_layout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_view_nosection_month);
        maps = new HashMap<String, ArrayList<Item>>();
        DaisyUtils.getVodApplication(this).addActivityToPool(this.toString(), this);
        initView();
        getData();
    }

    private void initView(){
        month_section_layout = (LinearLayout) findViewById(R.id.month_section_layout);
    }
    private void getData(){
        Bundle bundle = getIntent().getExtras();
        if (null == bundle)
            return;
        mItem = (Item) bundle.get("item");
        Item[] subItems = mItem.subitems;

        for(Item item :subItems){
            int month = item.month;
            ArrayList<Item> list = null;
            if(currentMonth!=month){
                list = new ArrayList<Item>();
                Button monthSection = new Button(this);
                monthSection.setClickable(true);
                monthSection.setText(month + "月");
                monthSection.setTag(month + "");
                monthSection.setBackgroundResource(R.drawable.month_btn_selector);
                monthSection.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String key = (String) view.getTag();
                        ArrayList<Item> lists = maps.get(key);

                    }
                });
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(105, ViewGroup.LayoutParams.MATCH_PARENT);

                if(currentMonth>0){
                    params.leftMargin = 34;
                }
                month_section_layout.addView(monthSection,params);
                currentMonth = month;
                maps.put(month+"",list);
            }
            list.add(item);
        }
    }
    @Override
    protected void onDestroy() {
        DaisyUtils.getVodApplication(this).removeActivtyFromPool(this.toString());
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
