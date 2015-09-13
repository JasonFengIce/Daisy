package tv.ismar.daisy.views;

import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.sakuratya.horizontal.adapter.HGridFilterAdapterImpl;
import org.sakuratya.horizontal.ui.HGridView;
import tv.ismar.daisy.R;
import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.exception.ItemOfflineException;
import tv.ismar.daisy.exception.NetworkException;
import tv.ismar.daisy.models.Item;
import tv.ismar.daisy.models.ItemCollection;
import tv.ismar.daisy.models.ItemList;
import tv.ismar.daisy.player.InitPlayerTool;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zhangjiqiang on 15-6-23.
 */
public class FilterResultFragment extends BackHandledFragment implements AdapterView.OnItemSelectedListener, AdapterView.OnItemClickListener, HGridView.OnScrollListener {

    public String mChannel;
    public String content_model;
    public String conditions;
    public String filterCondition;
    private View fragmentView;
    private LoadingDialog mLoadingDialog;
    private SimpleRestClient mRestClient;
    private ArrayList<ItemCollection> mItemCollections;
    private HGridView mHGridView;
    private HGridFilterAdapterImpl mHGridAdapter;
    private ItemList items;
    private boolean mIsBusy = false;
    private boolean isInitTaskLoading;
    private String url;
    private final static int NODATA = -1;
    private InitItemTask mInitTask;
    private ConcurrentHashMap<Integer, GetItemListTask> mCurrentLoadingTask = new ConcurrentHashMap<Integer, GetItemListTask>();
    private ProgressBar percentageBar;
    private boolean isNoData = false;
    public boolean isPortrait = false;
    private Button left_shadow;
    private Button right_shadow;
    private float rate;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rate = DaisyUtils.getVodApplication(getActivity()).getRate(getActivity());
        if(fragmentView==null){
            mLoadingDialog = new LoadingDialog(getActivity(), getResources().getString(R.string.loading));
            mLoadingDialog.setOnCancelListener(mLoadingCancelListener);
            mRestClient = new SimpleRestClient();

             if(!isPortrait)
               fragmentView = inflater.inflate(R.layout.filter_result_list_view,container,false);
             else
               fragmentView = inflater.inflate(R.layout.filter_portraitresult_list_view,container,false);
            doFilterRequest();
        }
        return fragmentView;
    }
    private void initView(View fragmentView,boolean hasResult){
            if(hasResult){
                percentageBar = (ProgressBar) fragmentView.findViewById(R.id.filter_percentage);
                LinearLayout layout = (LinearLayout)fragmentView.findViewById(R.id.filter_condition_layout);
                buildFilterListView(layout,conditions);
                mHGridView = (HGridView)fragmentView.findViewById(R.id.filter_grid);
                left_shadow = (Button)fragmentView.findViewById(R.id.left_shadow);
                right_shadow = (Button)fragmentView.findViewById(R.id.right_shadow);
            }else{
                left_shadow = (Button)fragmentView.findViewById(R.id.recommend_left_shadow);
                right_shadow = (Button)fragmentView.findViewById(R.id.recommend_right_shadow);
                View noresult_layout = fragmentView.findViewById(R.id.noresult_layout);
                View result_layout = fragmentView.findViewById(R.id.result_layout);
                noresult_layout.setVisibility(View.VISIBLE);
                result_layout.setVisibility(View.GONE);
                percentageBar = (ProgressBar)fragmentView.findViewById(R.id.recommend_filter_percentage);
                mHGridView = (HGridView)fragmentView.findViewById(R.id.recommend_filter_grid);
                Button filterBtn = (Button)fragmentView.findViewById(R.id.refilter_btn);
                filterBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        getFragmentManager().popBackStack();
                    }
                });
                url = SimpleRestClient.root_url+"/api/tv/filtrate/"+"$"+"movie"+"/"+"area*10022$10261$10263$10378$10479$10483$10484$10494"+"/1";
                isNoData = true;
                mInitTask = new InitItemTask();
                new InitItemTask().execute();
            }
        mHGridView.leftbtn = left_shadow;
        mHGridView.rightbtn = right_shadow;
        mHGridView.setOnItemClickListener(this);
        mHGridView.setOnItemSelectedListener(this);
        mHGridView.setOnScrollListener(this);
    }
    private void buildFilterListView(ViewGroup container,String str){
            if(str.length()>0){
                String[] labels = str.split("!");
                int count = labels.length;
                for(int i=0;i<count;i++){
                    TextView label = new TextView(getActivity());

                    label.setText(labels[i]);
                    label.setTextColor(0xffffffff);
                    label.setBackgroundResource(R.drawable.filter_btn_focused);
                    label.setTextSize(30/rate);
                    label.setGravity(Gravity.CENTER);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int)(120/1), (int)(46/1));
                    params.gravity = Gravity.CENTER_VERTICAL;
                    params.rightMargin = (int)(11/1);
                   // params.topMargin = 11;
                    container.addView(label,params);
                }
            }
    }
    private DialogInterface.OnCancelListener mLoadingCancelListener = new DialogInterface.OnCancelListener() {

        @Override
        public void onCancel(DialogInterface dialog) {
            getFragmentManager().popBackStack();
            dialog.dismiss();
        }
    };
    @Override
    public void onPause() {
        mIsBusy = true;
        if(mHGridAdapter!=null) {
            mHGridAdapter.cancel();
        }

        if(mLoadingDialog!=null&& mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }

        final ConcurrentHashMap<Integer, GetItemListTask> currentLoadingTask = mCurrentLoadingTask;
        for(Integer index:currentLoadingTask.keySet()) {
            currentLoadingTask.get(index).cancel(true);
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        mIsBusy = false;
        super.onResume();
    }
    private int getIndexFromSectionAndPage(int sectionIndex, int page) {
        return sectionIndex * 10000 + page;
    }

    private int[] getSectionAndPageFromIndex(int index) {
        int[] sectionAndPage = new int[2];
        sectionAndPage[0] = index / 10000;
        sectionAndPage[1] = index - index/10000 * 10000;
        return sectionAndPage;
    }
    class InitItemTask extends AsyncTask<Void, Void, Integer>{

        private final static int RESULT_NETWORKEXCEPTION = -1;
        private final static int RESUTL_CANCELED = -2;
        private final static int RESULT_SUCCESS = 0;
        @Override
        protected void onPreExecute() {
            if(mLoadingDialog!=null && !mLoadingDialog.isShowing()) {
                mLoadingDialog.show();
            }
            isInitTaskLoading = true;
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            try {
                items = mRestClient.getItemList(url);
                if(items!=null&&items.count>0){
                    mItemCollections = new ArrayList<ItemCollection>();
                    int num_pages = (int) FloatMath.ceil((float) items.count / (float) ItemCollection.NUM_PER_PAGE);
                    ItemCollection itemCollection = new ItemCollection(num_pages, items.count, "1", "1");
                    mItemCollections.add(itemCollection);
                }
                else{
                    return NODATA;
                }
                if(isCancelled()) {
                    return RESUTL_CANCELED;
                } else {
                    return RESULT_SUCCESS;
                }
            } catch (NetworkException e) {
                e.printStackTrace();
                return RESULT_NETWORKEXCEPTION;
            } catch (ItemOfflineException e) {
                e.printStackTrace();
                return RESULT_NETWORKEXCEPTION;
            }
        }
        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if(mLoadingDialog!=null && mLoadingDialog.isShowing()) {
                mLoadingDialog.dismiss();
            }
            isInitTaskLoading = false;
            if(result==NODATA) {
                //无数据
               initView(fragmentView,false);
               cancel(true);
               return;
            }
            else if(result==RESULT_SUCCESS&&!isNoData){
                initView(fragmentView,true);
            }
            try{
                if(items!=null ) {
                    mHGridAdapter = new HGridFilterAdapterImpl(getActivity(), mItemCollections,false);
                    mHGridAdapter.setIsPortrait(isPortrait);
                    mHGridAdapter.setList(mItemCollections);
                    if(mHGridAdapter.getCount()>0){
                        mHGridView.setAdapter(mHGridAdapter);
                        mHGridView.setFocusable(true);
                     //   mHGridView.setHorizontalFadingEdgeEnabled(true);
                       // mHGridView.setFadingEdgeLength(144);
                        mItemCollections.get(0).fillItems(0, items.objects);
                        mHGridAdapter.setList(mItemCollections);
                    }

                } else {
                    showDialog();
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }
    class GetItemListTask extends AsyncTask<Object, Void, ItemList>{
        private Integer index;
        @Override
        protected void onCancelled() {
            mCurrentLoadingTask.remove(index);
            super.onCancelled();
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ItemList doInBackground(Object... params) {
            try {
                index = (Integer) params[0];

                mCurrentLoadingTask.put(index, this);
                int[] sectionAndPage = getSectionAndPageFromIndex(index);
                int page = sectionAndPage[1] + 1;
                String url;
                if(!isNoData)
                    url =SimpleRestClient.root_url+"/api/tv/filtrate/"+"$"+content_model+"/"+filterCondition+"/"+page;
                else
                    url = SimpleRestClient.root_url+"/api/tv/filtrate/"+"$"+"movie"+"/"+"area*10022$10261$10263$10378$10479$10483$10484$10494/"+page;

                ItemList itemList = mRestClient.getItemList(url);
                if(isCancelled()) {
                    return null;
                } else {
                    return itemList;
                }
            } catch (Exception e) {

                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(ItemList itemList) {
            mCurrentLoadingTask.remove(index);
            if(itemList!=null && itemList.objects!=null) {
                int sectionIndex = getSectionAndPageFromIndex(index)[0];
                int page = getSectionAndPageFromIndex(index)[1];
                ItemCollection itemCollection = mItemCollections.get(sectionIndex);
                itemCollection.fillItems(page, itemList.objects);
                mHGridAdapter.setList(mItemCollections);
            } else {
                showDialog();
            }
        }
    }
    private void doFilterRequest(){
      //http://v2.sky.tvxio.com/v2_0/SKY/dto/api/tv/retrieval/" + mChannel + "/
     //api/tv/filtrate/$variety/genre*10008!area*10003/1
        url = SimpleRestClient.root_url+"/api/tv/filtrate/"+"$"+content_model+"/"+filterCondition+"/1";

        mInitTask = new InitItemTask();
        new InitItemTask().execute();
    }
    public void showDialog() {
        AlertDialogFragment newFragment = AlertDialogFragment.newInstance(AlertDialogFragment.NETWORK_EXCEPTION_DIALOG);
        newFragment.setPositiveListener(new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                doFilterRequest();
                dialog.dismiss();
            }
        });
        newFragment.setNegativeListener(new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                getFragmentManager().popBackStack();
                dialog.dismiss();
            }
        });
        FragmentManager manager = getFragmentManager();

        if(manager!=null) {
            newFragment.show(manager, "dialog");
        }
    }
    @Override
    public void onDestroyView() {
        if(mInitTask!=null && mInitTask.getStatus()!=AsyncTask.Status.FINISHED) {
            mInitTask.cancel(true);
        }
        super.onDestroyView();
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        Item item = mHGridAdapter.getItem(position);
        if(item!=null) {
            Intent intent = new Intent();
            if(item.is_complex) {
                DaisyUtils.gotoSpecialPage(getActivity(),item.content_model,item.url,"retrieval");
            } else {
                InitPlayerTool tool = new InitPlayerTool(getActivity());
                tool.fromPage = "retrieval";
                tool.setonAsyncTaskListener(new InitPlayerTool.onAsyncTaskHandler() {

                    @Override
                    public void onPreExecute(Intent intent) {
                        // TODO Auto-generated method stub
                        //intent.putExtra(EventProperty.SECTION, "");
                        mLoadingDialog.show();
                    }

                    @Override
                    public void onPostExecute() {
                        // TODO Auto-generated method stub
                        mLoadingDialog.dismiss();
                    }
                });
                tool.initClipInfo(item.url, InitPlayerTool.FLAG_URL);
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        int sectionIndex = mHGridAdapter.getSectionIndex(position);
        int rows = mHGridView.getRows();
        int itemCount = 0;
        for(int i=0; i < sectionIndex; i++) {
            itemCount += mHGridAdapter.getSectionCount(i);

        }
        int columnOfX = (position - itemCount) / rows + 1;
        int totalColumnOfSectionX = (int)(FloatMath.ceil((float)mHGridAdapter.getSectionCount(sectionIndex) / (float) rows));
        int percentage = (int) ((float)columnOfX / (float)totalColumnOfSectionX * 100f);
        Log.i("asdfghjkl","percentage=="+percentage);
       // percentageBar.setProgressDrawable(getResources().getDrawable(R.drawable.section_percentage_selected));
        percentageBar.setProgress(percentage);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onScrollStateChanged(HGridView view, int scrollState) {
        if(scrollState== HGridView.OnScrollListener.SCROLL_STATE_FOCUS_MOVING) {
            mIsBusy = true;
        } else if(scrollState == HGridView.OnScrollListener.SCROLL_STATE_IDLE) {
            mIsBusy = false;
        }
    }

    @Override
    public void onScroll(HGridView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if(!mIsBusy) {
            // We put the composed index which need to loading to this list. and check with
            // mCurrentLoadingTask soon after
            ArrayList<Integer> needToLoadComposedIndex = new ArrayList<Integer>();
            // The index of child in HGridView
            int index = 0;
            int sectionIndex = mHGridAdapter.getSectionIndex(firstVisibleItem);
            int itemCount = 0;
            for(int i=0; i < sectionIndex;i++) {
                itemCount += mHGridAdapter.getSectionCount(i);
            }
            // The index of current section.
            int indexOfSection = firstVisibleItem - itemCount;

            while(index < visibleItemCount) {
                final ItemCollection itemCollection = mItemCollections.get(sectionIndex);
                int num_pages = itemCollection.num_pages;
                int page = indexOfSection / ItemCollection.NUM_PER_PAGE;
                if(!itemCollection.isItemReady(indexOfSection)) {
                    int composedIndex = getIndexFromSectionAndPage(sectionIndex, page);
                    needToLoadComposedIndex.add(composedIndex);
                }

                if(page<num_pages - 1) {
                    // Go to next page of this section.
                    index += (page+1) * ItemCollection.NUM_PER_PAGE - indexOfSection;
                    indexOfSection = (page + 1) * ItemCollection.NUM_PER_PAGE;
                } else {
                    // This page is already the last page of current section.
                    index += mHGridAdapter.getSectionCount(sectionIndex) - indexOfSection;
                    indexOfSection = 0;
                    sectionIndex++;
                }
            }
            if(needToLoadComposedIndex.isEmpty()){
                return;
            }

            // Check the composedIndex in mCurrentLoadingTask if it existed do nothing, else start a task.
            // cancel other task that not in needToLoadComposedIndex list.
            final ConcurrentHashMap<Integer, GetItemListTask> currentLoadingTask = mCurrentLoadingTask;

            for(Integer i: currentLoadingTask.keySet()) {
                if(!needToLoadComposedIndex.contains(i)) {
                    currentLoadingTask.get(i).cancel(true);
                }
            }

            for(int i=0; i<needToLoadComposedIndex.size();i++) {
                int composedIndex = needToLoadComposedIndex.get(i);
                if(!currentLoadingTask.containsKey(composedIndex)) {
                    new GetItemListTask().execute(composedIndex);
                }
            }
        }
    }
}
