package tv.ismar.daisy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.sakuratya.horizontal.adapter.HGridAdapterImpl;
import org.sakuratya.horizontal.ui.HGridView;
import org.sakuratya.horizontal.ui.HGridView.OnScrollListener;

import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.EventProperty;
import tv.ismar.daisy.core.NetworkUtils;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.exception.ItemOfflineException;
import tv.ismar.daisy.exception.NetworkException;
import tv.ismar.daisy.models.Expense;
import tv.ismar.daisy.models.Item;
import tv.ismar.daisy.models.ItemCollection;
import tv.ismar.daisy.models.ItemList;
import tv.ismar.daisy.models.Section;
import tv.ismar.daisy.models.SectionList;
import tv.ismar.daisy.ui.widget.DaisyImageView;
import tv.ismar.daisy.views.AlertDialogFragment;
import tv.ismar.daisy.views.LoadingDialog;
import tv.ismar.daisy.views.ScrollableSectionList;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.TextView;

public class PackageListDetailActivity extends BaseActivity implements OnItemSelectedListener, OnItemClickListener, OnScrollListener{
 
	private SimpleRestClient mRestClient = new SimpleRestClient();
	
	private SectionList mSectionList;
	
	private ArrayList<ItemCollection> mItemCollections;
	
	private HGridView mHGridView;
	
	private HGridAdapterImpl mHGridAdapter;
	private ScrollableSectionList section_tabs;
	private LoadingDialog mLoadingDialog;
	private ItemList items;
	private int pk;
	private boolean isInitTaskLoading;
	private InitPackageItemTask mInitTask;
	private ConcurrentHashMap<Integer, GetItemListTask> mCurrentLoadingTask = new ConcurrentHashMap<Integer, PackageListDetailActivity.GetItemListTask>();
	private boolean mIsBusy = false;
	private TextView channel_label;
	private Button btn_search;
	private String itemlistUrl;
	private String lableString;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.historycollectlist_view);
		mLoadingDialog = new LoadingDialog(this, getResources().getString(R.string.loading));
		mLoadingDialog.setOnCancelListener(mLoadingCancelListener);
		DaisyUtils.getVodApplication(this).addActivityToPool(this.toString(), this);
		itemlistUrl = getIntent().getStringExtra("itemlistUrl");
		lableString = getIntent().getStringExtra("lableString");
		initView();
		getData();
	}
	@Override
	protected void onDestroy() {
		DaisyUtils.getVodApplication(this).removeActivtyFromPool(this.toString());
		super.onDestroy();
	}
	private void initView(){
		        channel_label = (TextView)findViewById(R.id.channel_label);
				if (StringUtils.isNotEmpty(lableString)) {
					channel_label.setText(lableString);
				} else {
					channel_label.setText("礼包内容");
				}

		        section_tabs = (ScrollableSectionList)findViewById(R.id.section_tabs);
		        section_tabs.setVisibility(View.GONE);
				mHGridView = (HGridView)findViewById(R.id.h_grid_view);
				mHGridView.setOnItemClickListener(this);
				mHGridView.setOnItemSelectedListener(this);
				mHGridView.setOnScrollListener(this);
				btn_search = (Button)findViewById(R.id.list_view_search);
				btn_search.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							Intent searchIntent = new Intent();
							searchIntent.setClass(PackageListDetailActivity.this, SearchActivity.class);
							startActivity(searchIntent);
						}
					});
	}
	private void getData(){
//		pk = getIntent().getIntExtra("pk", -1);
//		if(pk>0){
			new InitPackageItemTask().execute();
//		}
	}
	private OnCancelListener mLoadingCancelListener = new OnCancelListener() {
		
		@Override
		public void onCancel(DialogInterface dialog) {
			finish();
			dialog.dismiss();
		}
	};
	class InitPackageItemTask extends AsyncTask<Void, Void, Integer>{

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
		protected Integer doInBackground(Void... arg0) {
			// TODO Auto-generated method stub
			try {
				if(StringUtils.isNotEmpty(itemlistUrl)){
					items = mRestClient
							.getItemList(itemlistUrl);
				}else{
					items = mRestClient
							.getItemList(SimpleRestClient.root_url+"/api/package/list/"+pk + "/");		
				}
				
				if(items!=null){
					mItemCollections = new ArrayList<ItemCollection>();
				    int num_pages = (int) FloatMath.ceil((float)items.count / (float)ItemCollection.NUM_PER_PAGE);
					ItemCollection itemCollection = new ItemCollection(num_pages, items.count, "1", "1");
					mItemCollections.add(itemCollection);
				}
				if(isCancelled()) {
					return RESUTL_CANCELED;
				} else {
					return RESULT_SUCCESS;
				}
			} catch (Exception e) {
				e.printStackTrace();
				return RESULT_NETWORKEXCEPTION;
			}
		}
		@Override
		protected void onPostExecute(Integer result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if(mLoadingDialog!=null && mLoadingDialog.isShowing()) {
				mLoadingDialog.dismiss();
			}
			isInitTaskLoading = false;
			if(result!=RESULT_SUCCESS) {
				showDialog(AlertDialogFragment.NETWORK_EXCEPTION_DIALOG, (mInitTask = new InitPackageItemTask()), new String[]{"", ""});
				return;
			}
			try{
				if(items!=null ) {
					mHGridAdapter = new HGridAdapterImpl(PackageListDetailActivity.this, mItemCollections,false);
					mHGridAdapter.setList(mItemCollections);
					if(mHGridAdapter.getCount()>0){
						mHGridView.setAdapter(mHGridAdapter);
						mHGridView.setFocusable(true);
						mHGridView.setHorizontalFadingEdgeEnabled(true);
						mHGridView.setFadingEdgeLength(144);
						mItemCollections.get(0).fillItems(0, items.objects);
						mHGridAdapter.setList(mItemCollections);
					}

				} else {
					showDialog(AlertDialogFragment.NETWORK_EXCEPTION_DIALOG, (mInitTask = new InitPackageItemTask()), new String[]{"", ""});
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		
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
	class GetItemListTask extends AsyncTask<Object, Void, ItemList> {
		
		private Integer index;

		@Override
		protected void onCancelled() {
			mCurrentLoadingTask.remove(index);
			super.onCancelled();
		}

		@Override
		protected ItemList doInBackground(Object... params) {
			try {
				index = (Integer) params[0];
				
				mCurrentLoadingTask.put(index, this);
				int[] sectionAndPage = getSectionAndPageFromIndex(index);
				int page = sectionAndPage[1] + 1;
				ItemList itemList =null;
				if(StringUtils.isNotEmpty(itemlistUrl)){
					 itemList = mRestClient.getItemList(itemlistUrl+page+"/");
				}else{
					 itemList = mRestClient.getItemList(SimpleRestClient.root_url+"/api/package/list/"+pk+"/"+page+"/");
				}

				if(isCancelled()) {
					return null;
				} else {
					return itemList;
				}
			} catch (Exception e) {
				if(e instanceof ItemOfflineException) {
					HashMap<String, Object> exceptionProperties = new HashMap<String, Object>();
					exceptionProperties.put(EventProperty.CODE, "nocategory");
					exceptionProperties.put(EventProperty.CONTENT, "get category error : "+((ItemOfflineException) e).getUrl());
					NetworkUtils.SaveLogToLocal(NetworkUtils.CATEGORY_EXCEPT, exceptionProperties);
				} else if(e instanceof NetworkException) {
					HashMap<String, Object> exceptionProperties = new HashMap<String, Object>();
					exceptionProperties.put(EventProperty.CODE, "networkconnerror");
					exceptionProperties.put(EventProperty.CONTENT, "network connect error : " + ((ItemOfflineException) e).getUrl());
					NetworkUtils.SaveLogToLocal(NetworkUtils.CATEGORY_EXCEPT, exceptionProperties);
				}
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
				showDialog(AlertDialogFragment.NETWORK_EXCEPTION_DIALOG, new GetItemListTask(), new Object[]{index});
			}
		}
		
	}
	public void showDialog(int dialogType, final AsyncTask task, final Object[] params ) {
		AlertDialogFragment newFragment = AlertDialogFragment.newInstance(dialogType);
		newFragment.setPositiveListener(new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				if(!isInitTaskLoading) {
					task.execute(params);
				}
				dialog.dismiss();
			}
		});
		newFragment.setNegativeListener(new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				PackageListDetailActivity.this.finish();
				dialog.dismiss();
			}
		});
		FragmentManager manager = getFragmentManager();
		if(manager!=null) {
			newFragment.show(manager, "dialog");
		}
	}
	@Override
	public void onScrollStateChanged(HGridView view, int scrollState) {
		// TODO Auto-generated method stub
		if(scrollState==OnScrollListener.SCROLL_STATE_FOCUS_MOVING) {
			mIsBusy = true;
		} else if(scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
			mIsBusy = false;
		}
	}
	@Override
	public void onScroll(HGridView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub
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
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		Item item = mHGridAdapter.getItem(position);
		if(item!=null){
			Intent intent = new Intent();
			if(item.is_complex) {
				Expense f = item.expense;
				intent.setAction("tv.ismar.daisy.Item");
				intent.putExtra("url", item.url);
				startActivity(intent);
			}
			else{
				
			}
		}
	}
	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		mIsBusy = false;
		super.onResume();
	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		mIsBusy = true;
		super.onPause();
	}
}
