package tv.ismar.daisy.views;

import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.FloatMath;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.google.gson.JsonSyntaxException;
import org.sakuratya.horizontal.adapter.HGridAdapterImpl;
import org.sakuratya.horizontal.ui.HGridView;
import org.sakuratya.horizontal.ui.ZGridView;
import tv.ismar.daisy.ChannelListActivity;
import tv.ismar.daisy.ChannelListActivity.OnMenuToggleListener;
import tv.ismar.daisy.PersonCenterActivity;
import tv.ismar.daisy.R;
import tv.ismar.daisy.SearchActivity;
import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.NetworkUtils;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.SimpleRestClient.HttpPostRequestInterface;
import tv.ismar.daisy.exception.ItemOfflineException;
import tv.ismar.daisy.exception.NetworkException;
import tv.ismar.daisy.models.History;
import tv.ismar.daisy.models.Item;
import tv.ismar.daisy.models.ItemCollection;
import tv.ismar.daisy.models.SectionList;
import tv.ismar.daisy.player.InitPlayerTool;
import tv.ismar.daisy.player.InitPlayerTool.onAsyncTaskHandler;
import tv.ismar.daisy.views.MenuFragment.MenuItem;
import tv.ismar.daisy.views.MenuFragment.OnMenuItemClickedListener;
import tv.ismar.daisy.views.ScrollableSectionList.OnSectionSelectChangedListener;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HistoryFragment extends Fragment implements OnSectionSelectChangedListener,
														OnMenuToggleListener,
														OnMenuItemClickedListener,
														OnItemClickListener,
														OnItemSelectedListener{
	
	private static final int INVALID_POSITION = -1;
	
	private HGridView mHGridView;
	private ScrollableSectionList mScrollableSectionList;
	private TextView mChannelLabel;
	
	private HGridAdapterImpl mHGridAdapter;
	private SectionList mSectionList;
	
	private ItemCollection mTodayItemList;
	private ItemCollection mYesterdayItemList;
	private ItemCollection mEarlyItemList;
	private int mCurrentSectionPosition = 0;
	private SimpleRestClient mRestClient;
	
	private RelativeLayout mNoVideoContainer;
	
	private LoadingDialog mLoadingDialog;
	
	private boolean isInGetHistoryTask;
	private boolean isInGetItemTask;
	
	private GetHistoryTask mGetHistoryTask;
	
	private ConcurrentHashMap<String, HistoryFragment.GetItemTask> mCurrentGetItemTask = new ConcurrentHashMap<String, HistoryFragment.GetItemTask>();
	
	private MenuFragment mMenuFragment;
	
	public final static String MENU_TAG = "HistoryMenu";
	
	private HashMap<String, Object> mDataCollectionProperties;
	
	private int mSelectedPosition = INVALID_POSITION;
	private ZGridView recommend_gridview;
    private View divider;
    private TextView recommend_txt;
	private TextView channel_label;
	private TextView collect_or_history_txt;
	private Item[] mHistoriesByNet;
	private Button search_btn;
	private ItemCollection mHistoryItemList;
	private long getTodayStartPoint() {
		long currentTime = System.currentTimeMillis();
		GregorianCalendar currentCalendar = new GregorianCalendar();
		currentCalendar.setTimeInMillis(currentTime);
		currentCalendar.set(GregorianCalendar.HOUR_OF_DAY, 0);
		currentCalendar.set(GregorianCalendar.MINUTE, 0);
		currentCalendar.set(GregorianCalendar.SECOND, 0);
		return currentCalendar.getTimeInMillis();
	}
	
	private long getYesterdayStartPoint() {
		long todayStartPoint = getTodayStartPoint();
		return todayStartPoint - 24*3600*1000;
	}
	private void initViews(View fragmentView) {
		mHGridView = (HGridView) fragmentView.findViewById(R.id.h_grid_view);
		mHGridView.setOnItemClickListener(this);
		mHGridView.setOnItemSelectedListener(this);
		mScrollableSectionList = (ScrollableSectionList) fragmentView.findViewById(R.id.section_tabs);
		//mScrollableSectionList.setOnSectionSelectChangeListener(this);
		mScrollableSectionList.setVisibility(View.GONE);
		mChannelLabel = (TextView) fragmentView.findViewById(R.id.channel_label);
		mChannelLabel.setText(getResources().getString(R.string.vod_movielist_title_history));
		
		mNoVideoContainer = (RelativeLayout) fragmentView.findViewById(R.id.no_video_container);
		collect_or_history_txt = (TextView)fragmentView.findViewById(R.id.collect_or_history_txt);
		recommend_gridview = (ZGridView)fragmentView.findViewById(R.id.recommend_gridview);
		divider = (View)fragmentView.findViewById(R.id.divider);
		divider.setVisibility(View.VISIBLE);
		recommend_txt = (TextView)fragmentView.findViewById(R.id.recommend_txt);
		
		search_btn = (Button)fragmentView.findViewById(R.id.list_view_search);
		search_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent searchIntent = new Intent();
                searchIntent.setClass(getActivity(), SearchActivity.class);
                startActivity(searchIntent);
			}
		});
	}
	
	private void initHistoryList(){
		
		//define today's ItemList
		mTodayItemList = new ItemCollection(1, 0, "today", getResources().getString(R.string.vod_movielist_today));
		//define yesterday's ItemList
		mYesterdayItemList = new ItemCollection(1, 0, "yesterday", getResources().getString(R.string.vod_movielist_yesterday));
		//define early days's ItemList
		mEarlyItemList = new ItemCollection(1, 0, "early", getResources().getString(R.string.vod_movielist_recent));
		
		mHistoryItemList = new ItemCollection(1,0,"1","1");
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mRestClient = new SimpleRestClient();
		mLoadingDialog = new LoadingDialog(getActivity(), getResources().getString(R.string.loading));
		initHistoryList();
		createMenu();
		mSectionList = new SectionList();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View fragmentView = inflater.inflate(R.layout.historycollectlist_view, container, false);
		initViews(fragmentView);
		if("".equals(SimpleRestClient.access_token)){
			mGetHistoryTask = new GetHistoryTask();
			mGetHistoryTask.execute(); //没有登录，取本地设备信息
		}
		else{
			//登录，网络获取
			getHistoryByNet();
		}
		return fragmentView;
	}
	private ArrayList<ItemCollection> mItemCollections;
	private void getHistoryByNet(){
		SimpleRestClient post = new SimpleRestClient();
		mRestClient.doSendRequest("/api/histories/", "get", "", new HttpPostRequestInterface() {
			
			@Override
			public void onSuccess(String info) {
				// TODO Auto-generated method stub
				//Log.i(tag, msg);
				mLoadingDialog.dismiss();
				//解析json
				mHistoriesByNet = mRestClient.getItems(info);
				if(mHistoriesByNet!=null&&mHistoriesByNet.length>0){
					mItemCollections = new ArrayList<ItemCollection>();
				    int num_pages = (int) FloatMath.ceil((float)mHistoriesByNet.length / (float)ItemCollection.NUM_PER_PAGE);
					ItemCollection itemCollection = new ItemCollection(num_pages, mHistoriesByNet.length, "1", "1");
					mItemCollections.add(itemCollection);
					mHGridAdapter = new HGridAdapterImpl(getActivity(), mItemCollections,false);
					mHGridAdapter.setList(mItemCollections);
					if(mHGridAdapter.getCount()>0){
						mHGridView.setAdapter(mHGridAdapter);
						mHGridView.setFocusable(true);
						mHGridView.setHorizontalFadingEdgeEnabled(true);
						mHGridView.setFadingEdgeLength(144);
						ArrayList<Item> items  = new ArrayList<Item>();
						for(Item i:mHistoriesByNet){
							items.add(i);
						}
						mItemCollections.get(0).fillItems(0, items);
						mHGridAdapter.setList(mItemCollections);
					}
				}
				else{
					no_video();
				}
			}
			
			@Override
			public void onPrepare() {
				// TODO Auto-generated method stub
				mLoadingDialog.show();
			}
			
			@Override
			public void onFailed(String error) {
				// TODO Auto-generated method stub
				//Log.i(tag, msg);
				mLoadingDialog.dismiss();
				no_video();
			}
		});
	}
	private void EmptyAllHistory(){
		if(!"".equals(SimpleRestClient.access_token)){
			//清空历史记录
			mRestClient.doSendRequest("/api/histories/empty/", "post", "access_token="+SimpleRestClient.access_token+"&device_token="+SimpleRestClient.device_token, 
					new HttpPostRequestInterface() {
				
				@Override
				public void onSuccess(String info) {
					// TODO Auto-generated method stub
					Log.i("", info);
					no_video();
				}
				
				@Override
				public void onPrepare() {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onFailed(String error) {
					// TODO Auto-generated method stub
					//mItemCollections
					no_video();
					Log.i("", error);
				}
			});
		}
	}
	class GetHistoryTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			if(mLoadingDialog!=null && !mLoadingDialog.isShowing()) {
				mLoadingDialog.show();
			}
			isInGetHistoryTask = true;
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				final long todayStartPoint = getTodayStartPoint();
				final long yesterdayStartPoint = getYesterdayStartPoint();
				ArrayList<History> mHistories = DaisyUtils.getHistoryManager(getActivity()).getAllHistories();
				if(mHistories.size()>0) {
					Collections.sort(mHistories);
					for(int i=0;i<mHistories.size();++i) {
						History history = mHistories.get(i);
						Item item = getItem(history);
//						if(history.last_played_time < yesterdayStartPoint){
//							mEarlyItemList.objects.put(mEarlyItemList.count++, item);
//						} else if(history.last_played_time > yesterdayStartPoint && history.last_played_time < todayStartPoint) {
//							mYesterdayItemList.objects.put(mYesterdayItemList.count++, item);
//						} else {
//							mTodayItemList.objects.put(mTodayItemList.count++, item);
//						}
						mHistoryItemList.objects.put(mHistoryItemList.count++, item);
					}
					//mTodayItemList.num_pages = (int) FloatMath.ceil((float)mTodayItemList.count / (float)ItemCollection.NUM_PER_PAGE);
					//mYesterdayItemList.num_pages = (int) FloatMath.ceil((float)mYesterdayItemList.count /(float) ItemCollection.NUM_PER_PAGE);
					//mEarlyItemList.num_pages = (int) FloatMath.ceil((float)mEarlyItemList.count / (float)ItemCollection.NUM_PER_PAGE);
					mHistoryItemList.num_pages = (int) FloatMath.ceil((float)mHistoryItemList.count / (float)ItemCollection.NUM_PER_PAGE);
					if(mHistoryItemList.count>0){
						Arrays.fill(mHistoryItemList.hasFilledValidItem, true);
					}
//					if(mTodayItemList.count > 0) {
//						Section todaySection = new Section();
//						todaySection.slug = mTodayItemList.slug;
//						todaySection.title = mTodayItemList.title;
//						todaySection.count = mTodayItemList.count;
//						mSectionList.add(todaySection);
//						Arrays.fill(mTodayItemList.hasFilledValidItem, true);
//					}
//					if(mYesterdayItemList.count > 0) {
//						Section yesterdaySection = new Section();
//						yesterdaySection.slug = mYesterdayItemList.slug;
//						yesterdaySection.title = mYesterdayItemList.title;
//						yesterdaySection.count = mYesterdayItemList.count;
//						mSectionList.add(yesterdaySection);
//						Arrays.fill(mYesterdayItemList.hasFilledValidItem, true);
//					}
//					if(mEarlyItemList.count > 0) {
//						Section earlySection = new Section();
//						earlySection.slug = mEarlyItemList.slug;
//						earlySection.title = mEarlyItemList.title;
//						earlySection.count = mEarlyItemList.count;
//						mSectionList.add(earlySection);
//						Arrays.fill(mEarlyItemList.hasFilledValidItem, true);
//					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if(mHistoryItemList!=null&&mHistoryItemList.count>0) {
				//mScrollableSectionList.init(mSectionList, 1365,false);
				ArrayList<ItemCollection> itemCollections = new ArrayList<ItemCollection>();
//				if(mTodayItemList.count > 0) {
//					itemCollections.add(mTodayItemList);
//				}
//				if(mYesterdayItemList.count > 0) {
//					itemCollections.add(mYesterdayItemList);
//				}
//				if(mEarlyItemList.count > 0) {
//					itemCollections.add(mEarlyItemList);
//				}
				itemCollections.add(mHistoryItemList);
				mHGridAdapter = new HGridAdapterImpl(getActivity(), itemCollections,false);
				mHGridView.setAdapter(mHGridAdapter);
				mHGridView.setFocusable(true);
				mHGridView.setHorizontalFadingEdgeEnabled(true);
				mHGridView.setFadingEdgeLength(144);
				//int rows = mHGridView.getRows();
				//int totalColumnsOfSectionX = (int) FloatMath.ceil((float)mHGridAdapter.getSectionCount(mCurrentSectionPosition) / (float)rows);
				//mScrollableSectionList.setPercentage(mCurrentSectionPosition, (int)(1f/(float)totalColumnsOfSectionX*100f));
			} else {
				no_video();
			}
			if(mLoadingDialog!=null && mLoadingDialog.isShowing()) {
				mLoadingDialog.dismiss();
			}
			isInGetHistoryTask = false;
		}
		
	}
	
	private Item getItem(History history) {
		Item item = new Item();
		item.adlet_url = history.adlet_url;
		item.is_complex = history.is_complex;
		item.url = history.url;
		item.content_model = history.content_model;
		item.quality = history.quality;
		item.title = history.title;
		return item;
	}

	@Override
	public void onResume() {
		((ChannelListActivity)getActivity()).registerOnMenuToggleListener(this);
		new NetworkUtils.DataCollectionTask().execute(NetworkUtils.VIDEO_HISTORY_IN);
		super.onResume();
	}

	@Override
	public void onPause() {
		if(mHGridAdapter!=null) {
			mHGridAdapter.cancel();
		}
		ConcurrentHashMap<String, HistoryFragment.GetItemTask> currentGetItemTask = mCurrentGetItemTask;
		for(String url: currentGetItemTask.keySet()) {
			currentGetItemTask.get(url).cancel(true);
		}
		
		((ChannelListActivity)getActivity()).unregisterOnMenuToggleListener();
		HashMap<String, Object> properties = mDataCollectionProperties;
		new NetworkUtils.DataCollectionTask().execute(NetworkUtils.VIDEO_HISTORY_OUT, properties);
		mDataCollectionProperties = null;
		super.onPause();
	}
	
	@Override
	public void onSectionSelectChanged(int index) {
		mHGridView.jumpToSection(index);
	}
	
	class GetItemTask extends AsyncTask<Item, Void, Integer> {
		
		private static final int ITEM_OFFLINE = 0;
		private static final int ITEM_SUCCESS_GET = 1;
		private static final int NETWORK_EXCEPTION = 2;
		private static final int TASK_CANCELLED = 3;

		private Item item;
		
		@Override
		protected void onPreExecute() {
			if(mLoadingDialog!=null && !mLoadingDialog.isShowing()) {
				mLoadingDialog.show();
			}
			isInGetItemTask = true;
		}
		

		@Override
		protected void onCancelled() {
			mCurrentGetItemTask.remove(item.url);
		}


		@Override
		protected Integer doInBackground(Item... params) {
			item = params[0];
			mCurrentGetItemTask.put(item.url, this);
			Item i;
			try {
				i = mRestClient.getItem(item.url);
			} catch (ItemOfflineException e) {
				e.printStackTrace();
				return ITEM_OFFLINE;
			} catch (JsonSyntaxException e) {
				e.printStackTrace();
				return NETWORK_EXCEPTION;
			} catch (NetworkException e) {
				e.printStackTrace();
				return NETWORK_EXCEPTION;
			}
			if(i==null && !isCancelled()) {
				return NETWORK_EXCEPTION;
			} else if(!isCancelled()) {
				item = i;
				return ITEM_SUCCESS_GET;
			} else {
				return TASK_CANCELLED;
			}
			
		}

		@Override
		protected void onPostExecute(Integer result) {
			
			if(result== ITEM_OFFLINE) {
				mCurrentGetItemTask.remove(item.url);
				showDialog(AlertDialogFragment.ITEM_OFFLINE_DIALOG, null, new Object[]{item.url});
			} else if(result == NETWORK_EXCEPTION) {
				mCurrentGetItemTask.remove(item.url);
				showDialog(AlertDialogFragment.NETWORK_EXCEPTION_DIALOG, new GetItemTask(), new Item[]{item});
			} else if(result == ITEM_SUCCESS_GET){
				String url = SimpleRestClient.sRoot_url + "/api/item/" + item.pk + "/";
				mCurrentGetItemTask.remove(url);
				History history = DaisyUtils.getHistoryManager(getActivity()).getHistoryByUrl(url);
				// Use to data collection.
				mDataCollectionProperties = new HashMap<String, Object>();
				int id = SimpleRestClient.getItemId(url, new boolean[1]);
				mDataCollectionProperties.put("to_item", id);
				if(history.sub_url!=null && item.subitems!=null) {
					int sub_id = SimpleRestClient.getItemId(history.sub_url, new boolean[1]);
					mDataCollectionProperties.put("to_subitem", sub_id);
					for(Item subitem: item.subitems) {
						if(sub_id==subitem.pk) {
							mDataCollectionProperties.put("to_clip", subitem.clip.pk);
							break;
						}
					}
				} else {
					mDataCollectionProperties.put("to_subitem", item.clip.pk);
				}
				mDataCollectionProperties.put("to_title", item.title);
				mDataCollectionProperties.put("position", history.last_position);
				String[] qualitys = new String[]{"normal", "high", "ultra", "adaptive"};
				mDataCollectionProperties.put("quality", qualitys[(history.quality >=0 && history.quality < qualitys.length)?history.quality:0]);
				// start a new activity.
				Intent intent = new Intent();
				if(item.is_complex) {
					intent.setAction("tv.ismar.daisy.Item");
					intent.putExtra("url", url);
					startActivity(intent);
				} else {
					InitPlayerTool tool = new InitPlayerTool(getActivity());
					tool.setonAsyncTaskListener(new onAsyncTaskHandler() {
						
						@Override
						public void onPreExecute(Intent intent) {
							// TODO Auto-generated method stub
							mLoadingDialog.show();
						}
						
						@Override
						public void onPostExecute() {
							// TODO Auto-generated method stub
							mLoadingDialog.dismiss();
						}
					});
					tool.initClipInfo(url, InitPlayerTool.FLAG_URL);
				}
			}
			if(mLoadingDialog!=null && mLoadingDialog.isShowing()) {
				mLoadingDialog.dismiss();
			}
			isInGetItemTask = false;
		}
		
	}
	
	private void no_video() {
		mNoVideoContainer.setVisibility(View.VISIBLE);
		mNoVideoContainer.setBackgroundResource(R.drawable.no_record);
		mScrollableSectionList.setVisibility(View.GONE);
		mHGridView.setVisibility(View.GONE);
		collect_or_history_txt.setText(getResources().getString(R.string.no_history_record));
		getTvHome();
	}
	
	private void showDialog(final int dialogType, final AsyncTask task, final Object[] params) {
		AlertDialogFragment newFragment = AlertDialogFragment.newInstance(dialogType);
		newFragment.setPositiveListener(new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(dialogType==AlertDialogFragment.NETWORK_EXCEPTION_DIALOG && !isInGetItemTask) {
					task.execute(params);
				} else if (!isInGetHistoryTask) {
					DaisyUtils.getHistoryManager(getActivity()).deleteHistory((String)params[0]);
					reset();
				}
				dialog.dismiss();
			}
		});
		newFragment.setNegativeListener(new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		newFragment.show(getFragmentManager(), "dialog");
	}
	
	private void reset() {
		//mScrollableSectionList.reset();
		mSectionList = new SectionList();
		initHistoryList();
		if(mGetHistoryTask!=null && mGetHistoryTask.getStatus()!=AsyncTask.Status.FINISHED) {
			mGetHistoryTask.cancel(true);
		}
		mGetHistoryTask = new GetHistoryTask();
		mGetHistoryTask.execute();
	}

	@Override
	public void OnMenuToggle() {
		if(mMenuFragment==null) {
			createMenu();
		}
		if(mMenuFragment.isShowing()) {
			mMenuFragment.dismiss();
		} else {
			mMenuFragment.show(getFragmentManager(), MENU_TAG);
		}
	}
	
	private void createMenu() {
		mMenuFragment = MenuFragment.newInstance();
		mMenuFragment.setResId(R.string.vod_history_clear);
		mMenuFragment.setOnMenuItemClickedListener(this);
	}

	@Override
	public void onMenuItemClicked(MenuItem item) {
		switch(item.id) {
		case 1:
			if(mHGridAdapter!=null && mSelectedPosition!=INVALID_POSITION) {
				Item selectedItem = mHGridAdapter.getItem(mSelectedPosition);
				if(!isInGetHistoryTask && selectedItem.url!=null) {
					DaisyUtils.getHistoryManager(getActivity()).deleteHistory(selectedItem.url);
					reset();
				}
			}
			break;
		case 2:
			if(mHGridAdapter!=null) {
				if(!isInGetHistoryTask) {
					if("".equals(SimpleRestClient.access_token)){
						DaisyUtils.getHistoryManager(getActivity()).deleteAll();
						reset();
					}
					else{
						EmptyAllHistory();
					}
				}
			}
			break;
		case 3 : startSakura();break;
		case 4 : startPersoncenter();break;
		}
		
	}

	private void RemoveHistoriesByNet(){
		
	}
	@Override
	public void onDetach() {
		if(mLoadingDialog.isShowing()){
			mLoadingDialog.dismiss();
		}
		if(mGetHistoryTask!=null && mGetHistoryTask.getStatus()!=AsyncTask.Status.FINISHED) {
			mGetHistoryTask.cancel(true);
		}
		mLoadingDialog = null;
		mRestClient = null;
		mMenuFragment = null;
		super.onDetach();
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		if("".equals(SimpleRestClient.access_token)){
			mSelectedPosition = position;
			// When selected column has changed, we need to update the ScrollableSectionList
//			int sectionIndex = mHGridAdapter.getSectionIndex(position);
//			int rows = mHGridView.getRows();
//			int itemCount = 0;
//			for(int i=0; i < sectionIndex; i++) {
//				itemCount += mHGridAdapter.getSectionCount(i);
//				
//			}
			//int columnOfX = (position - itemCount) / rows + 1;
			//int totalColumnOfSectionX = (int)(FloatMath.ceil((float)mHGridAdapter.getSectionCount(sectionIndex) / (float) rows)); 
			//int percentage = (int) ((float)columnOfX / (float)totalColumnOfSectionX * 100f);
			//mScrollableSectionList.setPercentage(sectionIndex, percentage);
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		mSelectedPosition = INVALID_POSITION;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		switch (parent.getId()) {
		case R.id.h_grid_view:
			Item item = mHGridAdapter.getItem(position);
			new GetItemTask().execute(item);
			break;

		case R.id.recommend_gridview:
//			Intent intent= new Intent();
////			if(tvHome.getObjects().get(position).isIs_complex()){
////				intent.setClassName("tv.ismar.daisy",
////						"tv.ismar.daisy.ItemDetailActivity");
////				intent.putExtra("url", tvHome.getObjects().get(position).getItem_url());
//				startActivity(intent);
//			}
//			else{
//				InitPlayerTool tool = new InitPlayerTool(getActivity());
////				tool.initClipInfo(tvHome.getObjects().get(position).getItem_url(), InitPlayerTool.FLAG_URL);
//			}
//			break;
		}
	}
	private Handler mainHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			Bundle dataBundle = msg.getData();
				setTvHome(dataBundle.getString("content"));
			}
		};
		private void setTvHome(String content) {
		}
		private void getTvHome() {
			new Thread() {
				@Override
				public void run() {
					super.run();
					String content ="";
				
//						URL getUrl = new URL(SimpleRestClient.root_url
//								+ "/api/tv/section/tvhome/"+"?device_token="+SimpleRestClient.device_token);
//						HttpURLConnection connection = (HttpURLConnection) getUrl
//								.openConnection();
//						//connection.setIfModifiedSince(System.currentTimeMillis());
//						connection.setReadTimeout(9000);
//						connection.connect();
//						int status = connection.getResponseCode();
//						if(status==200){
//							BufferedReader reader = new BufferedReader(
//									new InputStreamReader(connection.getInputStream(),"UTF-8"));				
//							String lines;
//							while ((lines = reader.readLine()) != null) {
//								content.append(lines);
//							}
							
							try {
								content = NetworkUtils.getJsonStr(SimpleRestClient.root_url+"/api/tv/section/tvhome/");
								Message message = new Message();
								Bundle data = new Bundle();
								data.putString("content", content);
								DaisyUtils.getVodApplication(getActivity()).getEditor().putString("recommend", content.toString());
								message.setData(data);
								mainHandler.sendMessage(message);
							} catch (ItemOfflineException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (NetworkException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						
//						else if(status==304){
//							String info = DaisyUtils.getVodApplication(getActivity()).getPreferences().getString("recommend", "");
//							Message message = new Message();
//							Bundle data = new Bundle();
//							data.putString("content", info);
//							message.setData(data);
//							mainHandler.sendMessage(message);
//						}
					} 
				

			}.start();
		}

		   private void startSakura(){
	            Intent intent = new Intent();
	            intent.setAction("cn.ismar.sakura.launcher");
	            startActivity(intent);
		    }

		   private void startPersoncenter(){
			   Intent intent = new Intent(getActivity(),PersonCenterActivity.class);
			   startActivity(intent);
		   }
}
