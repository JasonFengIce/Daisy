package tv.ismar.daisy;

import android.content.Intent;
import android.os.Bundle;
import android.util.FloatMath;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import org.sakuratya.horizontal.adapter.HGridAdapterImpl;
import org.sakuratya.horizontal.ui.HGridView;
import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.models.Item;
import tv.ismar.daisy.models.ItemCollection;
import tv.ismar.daisy.player.InitPlayerTool;
import tv.ismar.daisy.ui.widget.TopPanelView;
import tv.ismar.daisy.views.LoadingDialog;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by zhangjiqiang on 15-7-10.
 */
public class DramaVarietyMonthList extends BaseActivity implements AdapterView.OnItemClickListener{

    private Item mItem;
    private int currentMonth = 0;
    private HashMap<String,ArrayList<Item>> maps;
    private LinearLayout month_section_layout;
    private HGridView mHGridView;
    private HGridAdapterImpl mHGridAdapter;
    private ArrayList<ItemCollection> mItemCollections;
    private LoadingDialog loadDialog;
    private TopPanelView top_column_layout;
    private Button arrow_left;
    private Button arrow_right;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_view_nosection_month);
        maps = new HashMap<String, ArrayList<Item>>();
        DaisyUtils.getVodApplication(this).addActivityToPool(this.toString(), this);
        initView();
        getData();
        month_section_layout.getChildAt(0).setFocusable(true);
        month_section_layout.getChildAt(0).requestFocus();
        View vv = month_section_layout.getChildAt(0);
        ((Button)month_section_layout.getChildAt(0)).performClick();
    }

    private void initView(){
        top_column_layout = (TopPanelView)findViewById(R.id.top_column_layout);
        month_section_layout = (LinearLayout) findViewById(R.id.month_section_layout);
        loadDialog = new LoadingDialog(this, getString(R.string.vod_loading));
        mHGridView = (HGridView)findViewById(R.id.h_grid_view);
        arrow_left = (Button)findViewById(R.id.arrow_left);
        arrow_right = (Button)findViewById(R.id.arrow_right);
        mHGridView.leftbtn = arrow_left;
        mHGridView.rightbtn = arrow_right;

        arrow_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHGridView.pageScroll(View.FOCUS_LEFT);
            }
        });
        arrow_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHGridView.pageScroll(View.FOCUS_RIGHT);
            }
        });
    }
    private void getData(){
        Bundle bundle = getIntent().getExtras();
        if (null == bundle)
            return;
        top_column_layout.setChannelName(getIntent().getStringExtra("channel"));
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
                        mItemCollections = new ArrayList<ItemCollection>();
                        int num_pages = (int) FloatMath.ceil((float) lists.size() / (float) ItemCollection.NUM_PER_PAGE);
                        ItemCollection itemCollection = new ItemCollection(num_pages, lists.size(), "1", "1");
                        mItemCollections.add(itemCollection);

                        mHGridAdapter = new HGridAdapterImpl(DramaVarietyMonthList.this, mItemCollections,false);
                        mHGridAdapter.setTemplate(2);
                        mHGridAdapter.setList(mItemCollections);
                        if(mHGridAdapter.getCount()>0){
                            mHGridView.setAdapter(mHGridAdapter);
                            mHGridView.setFocusable(true);
                            mHGridView.setHorizontalFadingEdgeEnabled(true);
                            mHGridView.setFadingEdgeLength(144);
                            mItemCollections.get(0).fillItems(0, lists);
                            mHGridAdapter.setList(mItemCollections);
                            if(arrow_left.isShown()){
                                arrow_left.setVisibility(View.INVISIBLE);
                            }
                            if(arrow_right.isShown()){
                                arrow_right.setVisibility(View.INVISIBLE);
                            }
                            if(mHGridAdapter.getCount()>15){
                                arrow_right.setVisibility(View.VISIBLE);
                            }
                        }
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

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Item mSubItem = mHGridAdapter.getItem(position);
        try {
            InitPlayerTool tool = new InitPlayerTool(DramaVarietyMonthList.this);
            tool.setonAsyncTaskListener(new InitPlayerTool.onAsyncTaskHandler() {

                @Override
                public void onPreExecute(Intent intent) {
                    // TODO Auto-generated method stub
                    loadDialog.show();
                }

                @Override
                public void onPostExecute() {
                    // TODO Auto-generated method stub
                    if (loadDialog != null)
                        loadDialog.dismiss();
                }
            });
            tool.initClipInfo(mSubItem.url, InitPlayerTool.FLAG_URL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
