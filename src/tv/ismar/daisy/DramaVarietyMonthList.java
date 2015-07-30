package tv.ismar.daisy;

import android.content.Intent;
import android.os.Bundle;
import android.util.FloatMath;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import org.sakuratya.horizontal.adapter.HGridAdapterImpl;
import org.sakuratya.horizontal.ui.HGridView;
import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.models.Item;
import tv.ismar.daisy.models.ItemCollection;
import tv.ismar.daisy.player.InitPlayerTool;
import tv.ismar.daisy.ui.widget.LaunchHeaderLayout;
import tv.ismar.daisy.views.LoadingDialog;
import tv.ismar.daisy.views.MonthSectionButton;

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
    private FrameLayout top_column_layout;
    private Button arrow_left;
    private Button arrow_right;
    private int mCurrentPosition=-1;
    private int mLastPosition = -1;

    private LaunchHeaderLayout weatherFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_view_nosection_month);
        maps = new HashMap<String, ArrayList<Item>>();
        DaisyUtils.getVodApplication(this).addActivityToPool(this.toString(), this);
        initView();
        getData();
        View vv = month_section_layout.getChildAt(0);
        ((Button)month_section_layout.getChildAt(0)).performClick();
        month_section_layout.getChildAt(0).setFocusable(true);
        month_section_layout.getChildAt(0).requestFocus();
    }

    private void initView(){
        weatherFragment = (LaunchHeaderLayout)findViewById(R.id.top_column_layout);


        month_section_layout = (LinearLayout) findViewById(R.id.month_section_layout);
        loadDialog = new LoadingDialog(this, getString(R.string.vod_loading));
        mHGridView = (HGridView)findViewById(R.id.h_grid_view);
        mHGridView.setOnItemClickListener(this);
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
        weatherFragment.setTitle((getIntent().getStringExtra("title")));
        weatherFragment.hideSubTiltle();
        mItem = (Item) bundle.get("item");
        Item[] subItems = mItem.subitems;
        ArrayList<Item> list = null;
        int k = 0;
        for(Item item :subItems){
            int month = item.month;
            if(currentMonth!=month){
                list = new ArrayList<Item>();
                MonthSectionButton monthSection = new MonthSectionButton(this);
                monthSection.setClickable(true);
                monthSection.setText(month + "月");
                monthSection.setTag(month + "");
                monthSection.setPosition(k);
                monthSection.setTextSize(30);
                monthSection.setFocusable(true);
                monthSection.setTextColor(0xffbbbbbb);
                monthSection.setBackgroundResource(R.drawable.month_btn_normal);
                monthSection.setClickable(true);
                monthSection.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View view, boolean hasfocus) {
                         int position = ((MonthSectionButton)view).getPosition();
                        if(hasfocus){
                            view.setBackgroundResource(R.drawable.month_btn_pressed);
                            ((MonthSectionButton) view).setTextSize(36);
                           if(position!=mLastPosition){
                               MonthSectionButton lastView = (MonthSectionButton) month_section_layout.getChildAt(mLastPosition);
                               if(lastView!=null){
                                   lastView.setBackgroundResource(R.drawable.month_btn_normal);
                                   lastView.setTextSize(30);
                               }
                               mLastPosition = position;
                           }
                        }
                        else{
                         ////////////////////////////////////
                        }
                    }
                });
                monthSection.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String key = (String) view.getTag();
                        int position = ((MonthSectionButton)view).getPosition();
                        if(position==mCurrentPosition){
                           // mLastPosition = position;
                            return;
                        }
                        view.setBackgroundResource(R.drawable.month_btn_pressed);
                        ((MonthSectionButton) view).setTextSize(36);
                        MonthSectionButton lastView = (MonthSectionButton) month_section_layout.getChildAt(mCurrentPosition);
                        if(lastView!=null){
                            lastView.setBackgroundResource(R.drawable.month_btn_normal);
                            lastView.setTextSize(30);
                        }
                        ArrayList<Item> lists = maps.get(key);
                        mItemCollections = new ArrayList<ItemCollection>();
                        int num_pages = (int) FloatMath.ceil((float) lists.size() / (float) ItemCollection.NUM_PER_PAGE);
                        ItemCollection itemCollection = new ItemCollection(num_pages, lists.size(), "1", "1");
                        mItemCollections.add(itemCollection);

                        mHGridAdapter = new HGridAdapterImpl(DramaVarietyMonthList.this, mItemCollections,false);
                        mHGridAdapter.setTemplate(2);
                        mHGridAdapter.setList(mItemCollections);
                        mCurrentPosition = position;
                        mLastPosition = position;
                        if(mHGridAdapter.getCount()>0){
                            mHGridView.setAdapter(mHGridAdapter);
                            mHGridView.setFocusable(true);
                            mItemCollections.get(0).fillItems(0, lists);
                            mHGridAdapter.setList(mItemCollections);
                            weatherFragment.setVisibility(View.VISIBLE);
                            if(arrow_left.isShown()){
                                arrow_left.setVisibility(View.INVISIBLE);
                            }
                            if(arrow_right.isShown()){
                                arrow_right.setVisibility(View.INVISIBLE);
                            }
                            if(mHGridAdapter.getCount()>8){
                                arrow_right.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                if(currentMonth>0){
                    params.leftMargin = 34;
                }
                month_section_layout.addView(monthSection,params);
                currentMonth = month;
                k++;
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
