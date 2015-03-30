package tv.ismar.daisy.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sakuratya.horizontal.adapter.HGridAdapterImpl;
import org.sakuratya.horizontal.ui.HGridView;
import org.sakuratya.horizontal.ui.HGridView.OnScrollListener;

import tv.ismar.daisy.DramaListActivity;
import tv.ismar.daisy.ItemDetailActivity;
import tv.ismar.daisy.LauncherActivity;
import tv.ismar.daisy.R;
import tv.ismar.daisy.SearchActivity;
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
import tv.ismar.daisy.player.InitPlayerTool;
import tv.ismar.daisy.player.InitPlayerTool.onAsyncTaskHandler;
import tv.ismar.daisy.ui.widget.DaisyImageView;
import tv.ismar.daisy.views.ScrollableSectionList.OnSectionSelectChangedListener;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.FloatMath;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ChannelFragment extends Fragment implements OnItemSelectedListener, OnItemClickListener, OnScrollListener {
	
	private static final String TAG = "ChannelFragment";

	private SimpleRestClient mRestClient = new SimpleRestClient();
	
	private SectionList mSectionList;
	
	private ArrayList<ItemCollection> mItemCollections;
	
	private HGridView mHGridView;
	
	private HGridAdapterImpl mHGridAdapter;
	
	private ScrollableSectionList mScrollableSectionList;
	
	private TextView mChannelLabel;
	
	private int mCurrentSectionIndex = -1;
	
	public String mTitle;
	
	public String mUrl;
	
	public String mChannel;
	
	private LoadingDialog mLoadingDialog;
	
	private boolean isInitTaskLoading;
	
	private InitTask mInitTask;
	
	private ConcurrentHashMap<Integer, GetItemListTask> mCurrentLoadingTask = new ConcurrentHashMap<Integer, ChannelFragment.GetItemListTask>();
	
	private boolean mIsBusy = false;
	
	private HashMap<String, Object> mSectionProperties = new HashMap<String, Object>();
	
	//private ImageView leftarrow;
	private ImageView arrow_left;
	private ImageView arrow_right;
	private Button btn_search;
	private void initViews(View fragmentView) {
		mHGridView = (HGridView) fragmentView.findViewById(R.id.h_grid_view);
		mHGridView.setOnItemClickListener(this);
		mHGridView.setOnItemSelectedListener(this);
		mHGridView.setOnScrollListener(this);
		mScrollableSectionList = (ScrollableSectionList) fragmentView.findViewById(R.id.section_tabs);
		mScrollableSectionList.setOnSectionSelectChangeListener(mOnSectionSelectChangedListener);
		arrow_left = (ImageView)fragmentView.findViewById(R.id.arrow_left);
		arrow_right = (ImageView)fragmentView.findViewById(R.id.arrow_right);
		mScrollableSectionList.left = arrow_left;
		mScrollableSectionList.right = arrow_right;
		arrow_left.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mScrollableSectionList.arrowScroll(View.FOCUS_LEFT);
			}
		});
		arrow_right.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mScrollableSectionList.arrowScroll(View.FOCUS_RIGHT);
			}
		});
		mChannelLabel = (TextView) fragmentView.findViewById(R.id.channel_label);
		mChannelLabel.setText(mTitle);
		btn_search = (Button)fragmentView.findViewById(R.id.search);
		btn_search.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent searchIntent = new Intent();
					searchIntent.setClass(getActivity(), SearchActivity.class);
					startActivity(searchIntent);
				}
			});
	}
	View fragmentView;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if(fragmentView==null){
			mLoadingDialog = new LoadingDialog(getActivity(), getResources().getString(R.string.loading));
			mLoadingDialog.setOnCancelListener(mLoadingCancelListener);
			fragmentView = inflater.inflate(R.layout.list_view, container, false);
			initViews(fragmentView);
			mInitTask = new InitTask();
			mInitTask.execute(mUrl, mChannel);
			// Add data collection.
			HashMap<String, Object> properties = new HashMap<String, Object>();
			properties.put(EventProperty.CATEGORY, mChannel);
			properties.put(EventProperty.TITLE, mTitle);
			
			new NetworkUtils.DataCollectionTask().execute(NetworkUtils.VIDEO_CHANNEL_IN, properties);
		}

		return fragmentView;
	}
	
	private boolean isChannelUrl(String url) {
		if(TextUtils.isEmpty(url)) {
			return false;
		}
		String patternStr = ".+/api/tv/sections/[\\w\\d]+/";
		Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(url);
		return matcher.matches();
	}
	
	
	
	class InitTask extends AsyncTask<String, Void, Integer> {
		
		String url;
		String channel;
		int nextSection = 0;
		
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
		protected Integer doInBackground(String... params) {
			try {
				url = params[0];
				channel = params[1];
				if(!isChannelUrl(url)){
					mSectionList = mRestClient.getSectionsByChannel(channel);
					for(int i=0; i<mSectionList.size() && !isCancelled(); i++) {
						if(NetworkUtils.urlEquals(url, mSectionList.get(i).url)) {
							nextSection = i;
							break;
						}
					}
				}
				if(mSectionList==null && !isCancelled()){
					mSectionList = mRestClient.getSections(url);
//					try{ 
//						HttpClient httpClient = new DefaultHttpClient(); 
//						//仿地址链接直接跟参数，如：http://127.0.0.1:8080/test/test.php?name=; 
//						HttpGet httpGet = new HttpGet(url); 
//						HttpResponse httpResponse = httpClient.execute(httpGet); 
//						if(httpResponse.getStatusLine().getStatusCode()==200){ 
//						String result = EntityUtils.toString(httpResponse.getEntity(),"UTF-8"); 
//						Log.i("qqq", "result="+result);
//						mSectionList = mRestClient.getsectionss(result);
//						} 
//						}catch(Exception e){} 
				}
				if(mSectionList!=null) {
					mItemCollections = new ArrayList<ItemCollection>();
					for(int i=0; i<mSectionList.size(); i++) {
						Section section = mSectionList.get(i);
						int num_pages = (int) FloatMath.ceil((float)section.count / (float)ItemCollection.NUM_PER_PAGE);
						ItemCollection itemCollection = new ItemCollection(num_pages, section.count, section.slug, section.title);
						mItemCollections.add(itemCollection);
					}
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
			Log.i("zhangjq123","onPostExecute");
			if(mLoadingDialog!=null && mLoadingDialog.isShowing()) {
				mLoadingDialog.dismiss();
			}
			isInitTaskLoading = false;
			if(result!=RESULT_SUCCESS) {
				showDialog(AlertDialogFragment.NETWORK_EXCEPTION_DIALOG, (mInitTask = new InitTask()), new String[]{url, channel});
				return;
			}
			try{
				if(mSectionList!=null ) {
					if(mSectionList.size()>5)
						arrow_right.setVisibility(View.VISIBLE);
					mScrollableSectionList.init(mSectionList, getResources().getDimensionPixelSize(R.dimen.gridview_channel_section_tabs_width),false);
					mHGridAdapter = new HGridAdapterImpl(getActivity(), mItemCollections);
					mHGridAdapter.setList(mItemCollections);
					if(mHGridAdapter.getCount()>0){
						mHGridView.setAdapter(mHGridAdapter);
						mHGridView.setFocusable(true);
						mHGridView.setHorizontalFadingEdgeEnabled(true);
						mHGridView.setFadingEdgeLength(144);
						mHGridAdapter.hg = mHGridView;
						int num_rows = mHGridView.getRows();
						int totalColumnsOfSectionX = (int) FloatMath.ceil((float)mItemCollections.get(nextSection).count / (float) num_rows);
						mScrollableSectionList.setPercentage(nextSection, (int)(1f/(float)totalColumnsOfSectionX*100f));
						checkSectionChanged(nextSection);
					}

				} else {
					showDialog(AlertDialogFragment.NETWORK_EXCEPTION_DIALOG, (mInitTask = new InitTask()), new String[]{url, channel});
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
//			new GetItemListTask().execute();
			super.onPostExecute(result);
		}
		
	}
	
	class GetItemListTask extends AsyncTask<Object, Void, ItemList> {
		
		private Integer index;
		private String slug;

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
				int sectionIndex = sectionAndPage[0];
				// page in api must start at 1.
				int page = sectionAndPage[1] + 1;
				Section section = mSectionList.get(sectionIndex);
				slug = section.slug;
				String url = section.url + page +"/";
				ItemList itemList = mRestClient.getItemList(url);
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
					exceptionProperties.put(EventProperty.SECTION, slug);
					NetworkUtils.SaveLogToLocal(NetworkUtils.CATEGORY_EXCEPT, exceptionProperties);
				} else if(e instanceof NetworkException) {
					HashMap<String, Object> exceptionProperties = new HashMap<String, Object>();
					exceptionProperties.put(EventProperty.CODE, "networkconnerror");
					exceptionProperties.put(EventProperty.CONTENT, "network connect error : " + ((ItemOfflineException) e).getUrl());
					exceptionProperties.put(EventProperty.SECTION, slug);
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
	private OnSectionSelectChangedListener mOnSectionSelectChangedListener = new OnSectionSelectChangedListener() {
		
		@Override
		public void onSectionSelectChanged(int index) {
			checkSectionChanged(index);
			mHGridView.jumpToSection(index);
		}
	};

	@Override
	public void onResume() {
		mIsBusy = false;
		super.onResume();
	}

	@Override
	public void onPause() {
		// We don't want to load when this page has been invisible.
		// This can prevent onScroll event to put new task to mCurrentLoadingTask.
		mIsBusy = true;
		// Prevent AsyncImageView loading
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
		
		final HashMap<String, Object> properties = new HashMap<String, Object>();
		properties.putAll(mSectionProperties);
		new NetworkUtils.DataCollectionTask().execute(NetworkUtils.VIDEO_CATEGORY_OUT, properties);
		mSectionProperties.remove(EventProperty.TO_ITEM);
		mSectionProperties.remove(EventProperty.TO_TITLE);
		mSectionProperties.remove(EventProperty.POSITION);
		mSectionProperties.remove(EventProperty.TITLE);
		mSectionProperties.remove(EventProperty.SECTION);
		super.onPause();
	}

	

	@Override
	public void onDestroyView() {
		if(mInitTask!=null && mInitTask.getStatus()!=AsyncTask.Status.FINISHED) {
			mInitTask.cancel(true);
		}
		
		// Add data collection.
		HashMap<String, Object> properties = new HashMap<String, Object>();
		properties.put(EventProperty.CATEGORY, mChannel);
		properties.put(EventProperty.TITLE, mTitle);
		new NetworkUtils.DataCollectionTask().execute(NetworkUtils.VIDEO_CHANNEL_OUT, properties);
		mInitTask = null;
		mSectionList = null;
		mScrollableSectionList = null;
		super.onDestroyView();
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
				getActivity().finish();
				dialog.dismiss();
			}
		});
		FragmentManager manager = getFragmentManager();
		if(manager!=null) {
			newFragment.show(manager, "dialog");
		}
	}
	
	private OnCancelListener mLoadingCancelListener = new OnCancelListener() {
		
		@Override
		public void onCancel(DialogInterface dialog) {
			getActivity().finish();
			dialog.dismiss();
		}
	};

	@Override
	public void onDetach() {
		mRestClient = null;
		super.onDetach();
	}

	@Override
	public void onScrollStateChanged(HGridView view, int scrollState) {
		if(scrollState==OnScrollListener.SCROLL_STATE_FOCUS_MOVING) {
			mIsBusy = true;
			Log.d(TAG, "Scroll State Changed! current is SCROLL_STATE_FOCUS_MOVING");
		} else if(scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
			Log.d(TAG, "Scroll State Changed! current is SCROLL_STATE_IDLE");
			mIsBusy = false;
		}
		
		
	}

	@Override
	public void onScroll(HGridView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
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
				Log.d(TAG, "indexOfSection: "+ indexOfSection+" sectionIndex: "+sectionIndex + " index: "+ index + " page: " + page);
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
			
			Log.d(TAG, "needToloadComposedIndex: " + needToLoadComposedIndex.toString());
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
			Log.d(TAG, currentLoadingTask.size() + " tasks in currentLoadingTask: ");
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Item item = mHGridAdapter.getItem(position);
		if(item!=null){
			if(item.model_name.equals("package")){
				Intent intent = new Intent();
				intent.setAction("tv.ismar.daisy.packageitem");
				intent.putExtra("url", item.url);
				startActivity(intent);
			}
			else{
				if(item!=null) {
					mSectionProperties.put(EventProperty.TO_ITEM, item.pk);
					mSectionProperties.put(EventProperty.TO_TITLE, item.title);
					mSectionProperties.put(EventProperty.POSITION, position);
					int sectionIndex = mHGridAdapter.getSectionIndex(position);
					final Section s = mSectionList.get(sectionIndex);
					mSectionProperties.put(EventProperty.TITLE, s.title);
					mSectionProperties.put(EventProperty.SECTION, s.slug);
					Intent intent = new Intent();
					if(item.is_complex) {
						intent.setAction("tv.ismar.daisy.Item");
						intent.putExtra("url", item.url);
						intent.putExtra(EventProperty.SECTION, s.slug);
						startActivity(intent);
					} else {
						InitPlayerTool tool = new InitPlayerTool(getActivity());
						tool.setonAsyncTaskListener(new onAsyncTaskHandler() {
							
							@Override
							public void onPreExecute(Intent intent) {
								// TODO Auto-generated method stub
								intent.putExtra(EventProperty.SECTION, s.slug);
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
		}
	}
	
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		// When selected column has changed, we need to update the ScrollableSectionList
		int sectionIndex = mHGridAdapter.getSectionIndex(position);
		int rows = mHGridView.getRows();
		int itemCount = 0;
		for(int i=0; i < sectionIndex; i++) {
			itemCount += mHGridAdapter.getSectionCount(i);
			
		}
		int columnOfX = (position - itemCount) / rows + 1;
		int totalColumnOfSectionX = (int)(FloatMath.ceil((float)mHGridAdapter.getSectionCount(sectionIndex) / (float) rows)); 
		int percentage = (int) ((float)columnOfX / (float)totalColumnOfSectionX * 100f);
		mScrollableSectionList.setPercentage(sectionIndex, percentage);
		checkSectionChanged(sectionIndex);
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub
		
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
	
	private void checkSectionChanged(int newSectionIndex) {
		if(newSectionIndex != mCurrentSectionIndex && newSectionIndex >= 0) {
			Section newSection = mSectionList.get(newSectionIndex);
			mSectionProperties.put(EventProperty.SECTION, newSection.slug);
			mSectionProperties.put(EventProperty.TITLE, newSection.title);
			//mSectionProperties.put("sid", newSectionIndex);
			new NetworkUtils.DataCollectionTask().execute(NetworkUtils.VIDEO_CATEGORY_IN, mSectionProperties);
			if(mCurrentSectionIndex >=0 ){
				Section oldSection = mSectionList.get(mCurrentSectionIndex);
				HashMap<String, Object> sectionProperties = new HashMap<String, Object>();
				sectionProperties.put(EventProperty.SECTION, oldSection.slug);
				sectionProperties.put(EventProperty.TITLE, oldSection.title);
				//sectionProperties.put("sid", mCurrentSectionIndex);
				new NetworkUtils.DataCollectionTask().execute(NetworkUtils.VIDEO_CATEGORY_OUT, sectionProperties);
			}
			mCurrentSectionIndex = newSectionIndex;
		}
	}
}
