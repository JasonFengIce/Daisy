package tv.ismar.daisy.views;

import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.FloatMath;
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
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.sakuratya.horizontal.adapter.HGridAdapterImpl;
import org.sakuratya.horizontal.ui.HGridView;
import org.sakuratya.horizontal.ui.ZGridView;
import tv.ismar.daisy.ChannelListActivity;
import tv.ismar.daisy.ChannelListActivity.OnMenuToggleListener;
import tv.ismar.daisy.R;
import tv.ismar.daisy.SearchActivity;
import tv.ismar.daisy.VodApplication;
import tv.ismar.daisy.adapter.RecommecdItemAdapter;
import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.NetworkUtils;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.SimpleRestClient.HttpPostRequestInterface;
import tv.ismar.daisy.exception.ItemOfflineException;
import tv.ismar.daisy.exception.NetworkException;
import tv.ismar.daisy.models.*;
import tv.ismar.daisy.player.InitPlayerTool;
import tv.ismar.daisy.player.InitPlayerTool.onAsyncTaskHandler;
import tv.ismar.daisy.ui.activity.UserCenterActivity;
import tv.ismar.daisy.views.MenuFragment.MenuItem;
import tv.ismar.daisy.views.MenuFragment.OnMenuItemClickedListener;
import tv.ismar.daisy.views.ScrollableSectionList.OnSectionSelectChangedListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class FavoriteFragment extends Fragment implements OnSectionSelectChangedListener,
														OnMenuToggleListener,
														OnMenuItemClickedListener,
														OnItemSelectedListener,
														OnItemClickListener{
	
	private static final int INVALID_POSITION = -1;
	
	private HGridView mHGridView;
	private ScrollableSectionList mScrollableSectionList;
	private TextView mChannelLabel;
	
	private HGridAdapterImpl mHGridAdapter;
	private ArrayList<ItemCollection> mItemCollections;
	private SectionList mSectionList;
	
	private int mCurrentSectionPosition = 0;
	
	private SimpleRestClient mRestClient;
	
	private ContentModel[] mContentModels;
	
	private RelativeLayout mNoVideoContainer;
	
	private boolean isInGetFavoriteTask;
	private boolean isInGetItemTask;
	
	private MenuFragment mMenuFragment;
	private LoadingDialog mLoadingDialog;
	
	private int mSelectedPosition = INVALID_POSITION;
	
	private HashMap<String, Object> mDataCollectionProperties;
	
	public final static String MENU_TAG = "FavoriteMenu";
	
	private ConcurrentHashMap<String, FavoriteFragment.GetItemTask> mCurrentGetItemTask = new ConcurrentHashMap<String, FavoriteFragment.GetItemTask>();
	
	private ZGridView recommend_gridview;
    private View divider;
    private TextView recommend_txt;
    private TextView collect_or_history_txt;
    private VideoEntity tvHome;
    private Item[] FavoriteList;
    private Button search_btn;
    private Button left_shadow;
    private Button right_shadow;
    private View gideview_layuot;
	private void initViews(View fragmentView) {
        View background = fragmentView.findViewById(R.id.large_layout);
        DaisyUtils.setbackground(R.drawable.main_bg,background);
        View vv = fragmentView.findViewById(R.id.tabs_layout);
        vv.setVisibility(View.GONE);
		mHGridView = (HGridView) fragmentView.findViewById(R.id.h_grid_view);
        left_shadow = (Button)fragmentView.findViewById(R.id.left_shadow);
        right_shadow = (Button)fragmentView.findViewById(R.id.right_shadow);
        gideview_layuot = fragmentView.findViewById(R.id.gideview_layuot);
        mHGridView.leftbtn = left_shadow;
        mHGridView.rightbtn = right_shadow;
		mHGridView.setOnItemClickListener(this);
		mHGridView.setOnItemSelectedListener(this);
		mScrollableSectionList = (ScrollableSectionList) fragmentView.findViewById(R.id.section_tabs);
		mScrollableSectionList.setVisibility(View.GONE);
//		mScrollableSectionList.setOnSectionSelectChangeListener(this);
		
		mChannelLabel = (TextView) fragmentView.findViewById(R.id.channel_label);
		mChannelLabel.setText(getResources().getString(R.string.favorite));
		
		mNoVideoContainer = (RelativeLayout) fragmentView.findViewById(R.id.no_video_container);
		recommend_gridview = (ZGridView)fragmentView.findViewById(R.id.recommend_gridview);
		recommend_txt = (TextView)fragmentView.findViewById(R.id.recommend_txt);
		collect_or_history_txt = (TextView)fragmentView.findViewById(R.id.collect_or_history_txt);
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
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View fragmentView = inflater.inflate(R.layout.historycollectlist_view, container, false);
		initViews(fragmentView);
		if("".equals(SimpleRestClient.access_token))
		   new GetFavoriteTask().execute();
		else
			GetFavoriteByNet();
		return fragmentView;
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mRestClient = new SimpleRestClient();
		VodApplication application = DaisyUtils.getVodApplication(getActivity());
		mContentModels = application.mContentModel;
		mLoadingDialog = new LoadingDialog(getActivity(), getResources().getString(R.string.loading));
		createMenu();
	}
	private void GetFavoriteByNet(){
		mRestClient.doSendRequest("/api/bookmarks/", "get", "", new HttpPostRequestInterface() {
			
			@Override
			public void onSuccess(String info) {
				// TODO Auto-generated method stub
				//解析json
				FavoriteList = mRestClient.getItems(info);
				if(FavoriteList!=null&&FavoriteList.length>0){
					mItemCollections = new ArrayList<ItemCollection>();
				    int num_pages = (int) FloatMath.ceil((float)FavoriteList.length / (float)ItemCollection.NUM_PER_PAGE);
					ItemCollection itemCollection = new ItemCollection(num_pages, FavoriteList.length, "1", "1");
					mItemCollections.add(itemCollection);
					mHGridAdapter = new HGridAdapterImpl(getActivity(), mItemCollections,false);
					mHGridAdapter.setList(mItemCollections);
					if(mHGridAdapter.getCount()>0){
						mHGridView.setAdapter(mHGridAdapter);
						mHGridView.setFocusable(true);
						//mHGridView.setHorizontalFadingEdgeEnabled(true);
						//mHGridView.setFadingEdgeLength(144);
						ArrayList<Item> items  = new ArrayList<Item>();
						for(Item i:FavoriteList){
							items.add(i);
						}
						mItemCollections.get(0).fillItems(0, items);
						mHGridAdapter.setList(mItemCollections);
					}
				}
				else{
					no_video();
				}
				mLoadingDialog.dismiss();
			}
			
			@Override
			public void onPrepare() {
				// TODO Auto-generated method stub
				mLoadingDialog.show();
			}
			
			@Override
			public void onFailed(String error) {
				// TODO Auto-generated method stub
				no_video();
				mLoadingDialog.dismiss();
			}
		});
	}
	class GetFavoriteTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			if(mLoadingDialog!=null && !mLoadingDialog.isShowing()) {
				mLoadingDialog.show();
			}
			isInGetFavoriteTask = true;
		}

		@Override
		protected Void doInBackground(Void... params) {
			ArrayList<Favorite> favorites = DaisyUtils.getFavoriteManager(getActivity()).getAllFavorites("no");			
			mSectionList = new SectionList();
			HashMap<String, ItemCollection> itemCollectionMap = new HashMap<String, ItemCollection>();
			for(Favorite favorite: favorites) {
				String content_model = favorite.content_model;
				Item item = getItem(favorite);
				if(item!=null) {
					ItemCollection itemCollection = itemCollectionMap.get(content_model);
					if(itemCollection==null) {
						Section section = new Section();
						section.slug = content_model;
						for(ContentModel cm: mContentModels) {
							if(cm.content_model.equals(content_model)) {
								section.title = cm.title;
								break;
							}
						}
						itemCollection = new ItemCollection(1, 0, content_model, section.title);
						mSectionList.add(section);
						itemCollectionMap.put(content_model, itemCollection);
					}
					itemCollection.objects.put(itemCollection.count++, item);
				}
			}
			mItemCollections = new ArrayList<ItemCollection>();
			for(Section section:mSectionList) {
				ItemCollection itemCollection= itemCollectionMap.get(section.slug);
				int count = itemCollection.objects.size();
				itemCollection.num_pages = (int)FloatMath.ceil((float)count / (float)ItemCollection.NUM_PER_PAGE);
				section.count = count;
				// we have already complete data collection.
				itemCollection.hasFilledValidItem = new boolean[itemCollection.num_pages];
				Arrays.fill(itemCollection.hasFilledValidItem, true);
				mItemCollections.add(itemCollection);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if(mLoadingDialog!=null && mLoadingDialog.isShowing()) {
				mLoadingDialog.dismiss();
			}
			
			isInGetFavoriteTask = false;
			if(mSectionList.size()==0) {
				no_video();
				return;
			}
//			mScrollableSectionList.init(mSectionList, 1365,false);
			mHGridAdapter = new HGridAdapterImpl(getActivity(), mItemCollections);
			mHGridView.setAdapter(mHGridAdapter);
			mHGridView.setFocusable(true);
		//	mHGridView.setHorizontalFadingEdgeEnabled(true);
			//mHGridView.setFadingEdgeLength(144);
			int num_rows = mHGridView.getRows();
			int totalColumnsOfSectionX = (int) FloatMath.ceil((float)mItemCollections.get(mCurrentSectionPosition).count / (float) num_rows);
//			mScrollableSectionList.setPercentage(mCurrentSectionPosition, (int)(1f/(float)totalColumnsOfSectionX*100f));
		}
		
	}
	
	public Item getItem(Favorite favorite) {
		Item item = new Item();
		item.url = favorite.url;
		item.title = favorite.title;
		item.adlet_url = favorite.adlet_url;
		item.content_model = favorite.content_model;
		item.is_complex = favorite.is_complex;
		item.quality = favorite.quality;
		return item;
	}
	
	class GetItemTask extends AsyncTask<Item, Void, Integer> {
		
		private static final int ITEM_OFFLINE = 0;
		private static final int ITEM_SUCCESS_GET = 1;
		private static final int NETWORK_EXCEPTION = 2;
		
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
//			try {
//			//	i = mRestClient.getItem(item.url);
//                return ITEM_SUCCESS_GET;
//			} catch (ItemOfflineException e) {
//				e.printStackTrace();
//				return ITEM_OFFLINE;
//			} catch (JsonSyntaxException e) {
//				e.printStackTrace();
//				return NETWORK_EXCEPTION;
//			} catch (NetworkException e) {
//				e.printStackTrace();
//				return NETWORK_EXCEPTION;
//			}
//			if(i==null) {
//				return NETWORK_EXCEPTION;
//			} else {
//				return ITEM_SUCCESS_GET;
//			}
            return ITEM_SUCCESS_GET;
		}

		@Override
		protected void onPostExecute(Integer result) {
			mCurrentGetItemTask.remove(item.url);
			if(result== ITEM_OFFLINE) {
				showDialog(AlertDialogFragment.ITEM_OFFLINE_DIALOG, null, new Object[]{item.url});
			} else if(result == NETWORK_EXCEPTION) {
				showDialog(AlertDialogFragment.NETWORK_EXCEPTION_DIALOG, new GetItemTask(), new Item[]{item});
			} else {
				// Use to data collection.
				mDataCollectionProperties = new HashMap<String, Object>();
				boolean[] isSubItem = new boolean[1]; 
				int id = SimpleRestClient.getItemId(item.url, isSubItem);
				if(isSubItem[0]) {
					mDataCollectionProperties.put("to_subitem", id);
				} else {
					mDataCollectionProperties.put("to_item", id);
				}
				mDataCollectionProperties.put("to_title", item.title);
				
				// start new Activity.
				Intent intent = new Intent();
				if(item.is_complex) {
//					if("variety".equals(item.content_model)){
//						intent.setAction("tv.ismar.daisy.EntertainmentItem");
//						intent.putExtra("channel", "娱乐综艺");
//					}else {
//						intent.setAction("tv.ismar.daisy.Item");
//					}
//					intent.putExtra("url", item.url);
//					startActivity(intent);
                    DaisyUtils.gotoSpecialPage(getActivity(),item.content_model,SimpleRestClient.sRoot_url+"/api/item/"+id+"/","favorite");
				} else {
					InitPlayerTool tool = new InitPlayerTool(getActivity());
                    tool.fromPage = "favorite";
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
					tool.initClipInfo(SimpleRestClient.sRoot_url+"/api/item/"+id+"/", InitPlayerTool.FLAG_URL);
				}
			}
			
			if(mLoadingDialog!=null && mLoadingDialog.isShowing()) {
				mLoadingDialog.dismiss();
			}
			isInGetItemTask = false;
		}
		
	}
	
	@Override
	public void onSectionSelectChanged(int index) {
		mHGridView.jumpToSection(index);
	}
	@Override
	public void onResume() {
		((ChannelListActivity)getActivity()).registerOnMenuToggleListener(this);
		new NetworkUtils.DataCollectionTask().execute(NetworkUtils.VIDEO_COLLECT_IN);
		super.onResume();
	}
	@Override
	public void onPause() {
		if(mHGridAdapter!=null) {
			mHGridAdapter.cancel();
		}
		
		ConcurrentHashMap<String, FavoriteFragment.GetItemTask> currentGetItemTask = mCurrentGetItemTask;
		for(String url: currentGetItemTask.keySet()) {
			currentGetItemTask.get(url).cancel(true);
		}
		
		((ChannelListActivity)getActivity()).unregisterOnMenuToggleListener();
		HashMap<String, Object> properties = mDataCollectionProperties;
		new NetworkUtils.DataCollectionTask().execute(NetworkUtils.VIDEO_COLLECT_OUT, properties);
		mDataCollectionProperties = null;
		super.onPause();
	}
	
	private void no_video() {
		mNoVideoContainer.setVisibility(View.VISIBLE);
		mNoVideoContainer.setBackgroundResource(R.drawable.no_record);
        gideview_layuot.setVisibility(View.GONE);
//		mScrollableSectionList.setVisibility(View.GONE);
		mHGridView.setVisibility(View.GONE);
		collect_or_history_txt.setText(getResources().getString(R.string.no_collect_record));
		getTvHome();
	}
	
	private void showDialog(final int dialogType, final AsyncTask task, final Object[] params)  {
		AlertDialogFragment newFragment = AlertDialogFragment.newInstance(dialogType);
		newFragment.setPositiveListener(new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(dialogType==AlertDialogFragment.NETWORK_EXCEPTION_DIALOG && !isInGetItemTask) {
					task.execute(params);
				} else if(!isInGetFavoriteTask) {
					DaisyUtils.getFavoriteManager(getActivity()).deleteFavoriteByUrl((String)params[0],"no");
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
//		mScrollableSectionList.reset();
		new GetFavoriteTask().execute();
	}
	
	private void createMenu() {
		mMenuFragment = MenuFragment.newInstance();
		mMenuFragment.setResId(R.string.vod_bookmark_clear);
		mMenuFragment.setOnMenuItemClickedListener(this);
	}
	
	@Override
	public void onMenuItemClicked(MenuItem item) {
		switch(item.id) {
		case 1:
			if(mHGridAdapter!=null) {
				if(mSelectedPosition!=INVALID_POSITION) {
					Item selectedItem = mHGridAdapter.getItem(mSelectedPosition);
					if(!isInGetFavoriteTask && selectedItem!=null && selectedItem.url!=null) {
						DaisyUtils.getFavoriteManager(getActivity()).deleteFavoriteByUrl(selectedItem.url,"no");
						reset();
					}
				}
			}
			break;
		case 2:
			if(mHGridAdapter!=null) {
				if(!isInGetFavoriteTask) {
					if("".equals(SimpleRestClient.access_token)){
						DaisyUtils.getFavoriteManager(getActivity()).deleteAll("no");
						reset();
					}
					else{
						DaisyUtils.getFavoriteManager(getActivity()).deleteAll("yes");
						EmptyAllFavorite();
					}
				}
			}
			break;
		case 3 : startSakura();break;
		case 4 : startPersoncenter();break;
		}
	}
	private void EmptyAllFavorite(){
		mRestClient.doSendRequest("/api/bookmarks/empty/", "post", "access_token="+SimpleRestClient.access_token+"&device_token="+SimpleRestClient.device_token, new HttpPostRequestInterface() {
			
			@Override
			public void onSuccess(String info) {
				// TODO Auto-generated method stub
				no_video();
			}
			
			@Override
			public void onPrepare() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onFailed(String error) {
				// TODO Auto-generated method stub
				no_video();
			}
		});
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
	@Override
	public void onDetach() {
		if(mLoadingDialog.isShowing()){
			mLoadingDialog.dismiss();
		}
		mLoadingDialog = null;
		mSectionList = null;
//		mScrollableSectionList = null;
		mRestClient = null;
		mMenuFragment = null;
		super.onDetach();
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
			if(tvHome.getObjects().get(position).isIs_complex()){
                DaisyUtils.gotoSpecialPage(getActivity(),tvHome.getObjects().get(position).getContent_model(),tvHome.getObjects().get(position).getItem_url(),"tvhome");
				//startActivity(intent);
			}
			else{
				InitPlayerTool tool = new InitPlayerTool(getActivity());
                tool.fromPage = "tvhome";
				tool.initClipInfo(tvHome.getObjects().get(position).getItem_url(), InitPlayerTool.FLAG_URL);
			}
			break;
		}
	}
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		if(!SimpleRestClient.isLogin()){
			mSelectedPosition = position;
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
//			mScrollableSectionList.setPercentage(sectionIndex, percentage);
		}		
	}
	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		mSelectedPosition = INVALID_POSITION;
	}
	private Handler mainHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			Bundle dataBundle = msg.getData();
				setTvHome(dataBundle.getString("content"));
			}
		};
		private void setTvHome(String content) {
			try{
				Gson gson = new Gson();
				tvHome = gson.fromJson(content.toString(),
						VideoEntity.class);
				if(tvHome.getObjects()!=null&&tvHome.getObjects().size()>0){
					RecommecdItemAdapter recommendAdapter = new RecommecdItemAdapter(getActivity(), tvHome);
					recommend_gridview.setAdapter(recommendAdapter);
					recommend_gridview.setFocusable(true);	
					recommend_gridview.setOnItemClickListener(this);
				}
			}catch(Exception e){
				recommend_txt.setVisibility(View.INVISIBLE);
				e.printStackTrace();
			}
		}
	private void getTvHome() {
		new Thread() {
			@Override
			public void run() {
				super.run();
				String content ="";
			
//					URL getUrl = new URL(SimpleRestClient.root_url
//							+ "/api/tv/section/tvhome/"+"?device_token="+SimpleRestClient.device_token);
//					HttpURLConnection connection = (HttpURLConnection) getUrl
//							.openConnection();
//					//connection.setIfModifiedSince(System.currentTimeMillis());
//					connection.setReadTimeout(9000);
//					connection.connect();
//					int status = connection.getResponseCode();
//					if(status==200){
//						BufferedReader reader = new BufferedReader(
//								new InputStreamReader(connection.getInputStream(),"UTF-8"));				
//						String lines;
//						while ((lines = reader.readLine()) != null) {
//							content.append(lines);
//						}
						
						try {
							content = NetworkUtils.getJsonStr(SimpleRestClient.root_url+"/api/tv/section/tvhome/","");
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

					
//					else if(status==304){
//						String info = DaisyUtils.getVodApplication(getActivity()).getPreferences().getString("recommend", "");
//						Message message = new Message();
//						Bundle data = new Bundle();
//						data.putString("content", info);
//						message.setData(data);
//						mainHandler.sendMessage(message);
//					}
				} 
			

		}.start();
	}

	   private void startSakura(){
           Intent intent = new Intent();
           intent.setAction("cn.ismar.sakura.launcher");
           startActivity(intent);
	    }
	   
	   private void startPersoncenter(){
		   Intent intent = new Intent(getActivity(),UserCenterActivity.class);
		   startActivity(intent);
	   }
}
