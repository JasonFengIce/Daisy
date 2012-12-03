package tv.ismar.daisy.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;

import org.sakuratya.horizontal.adapter.HGridAdapterImpl;
import org.sakuratya.horizontal.ui.HGridView;

import tv.ismar.daisy.ChannelListActivity;
import tv.ismar.daisy.ChannelListActivity.OnMenuToggleListener;
import tv.ismar.daisy.R;
import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.ItemOfflineException;
import tv.ismar.daisy.core.NetworkUtils;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.models.History;
import tv.ismar.daisy.models.Item;
import tv.ismar.daisy.models.ItemCollection;
import tv.ismar.daisy.models.Section;
import tv.ismar.daisy.models.SectionList;
import tv.ismar.daisy.views.MenuFragment.MenuItem;
import tv.ismar.daisy.views.MenuFragment.OnMenuItemClickedListener;
import tv.ismar.daisy.views.ScrollableSectionList.OnSectionSelectChangedListener;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.FloatMath;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
	
	private MenuFragment mMenuFragment;
	
	public final static String MENU_TAG = "HistoryMenu";
	
	private HashMap<String, Object> mDataCollectionProperties;
	
	private int mSelectedPosition = INVALID_POSITION;
	
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
		mScrollableSectionList.setOnSectionSelectChangeListener(this);
		
		mChannelLabel = (TextView) fragmentView.findViewById(R.id.channel_label);
		mChannelLabel.setText(getResources().getString(R.string.vod_movielist_title_history));
		
		mNoVideoContainer = (RelativeLayout) fragmentView.findViewById(R.id.no_video_container);
	}
	
	private void initHistoryList(){
		
		//define today's ItemList
		mTodayItemList = new ItemCollection(1, 0, "today", getResources().getString(R.string.vod_movielist_today));
		//define yesterday's ItemList
		mYesterdayItemList = new ItemCollection(1, 0, "yesterday", getResources().getString(R.string.vod_movielist_yesterday));
		//define early days's ItemList
		mEarlyItemList = new ItemCollection(1, 0, "early", getResources().getString(R.string.vod_movielist_recent));
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
		View fragmentView = inflater.inflate(R.layout.list_view, container, false);
		initViews(fragmentView);
		mGetHistoryTask = new GetHistoryTask();
		mGetHistoryTask.execute();
		return fragmentView;
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
						if(history.last_played_time < yesterdayStartPoint){
							mEarlyItemList.objects.put(mEarlyItemList.count++, item);
						} else if(history.last_played_time > yesterdayStartPoint && history.last_played_time < todayStartPoint) {
							mYesterdayItemList.objects.put(mYesterdayItemList.count++, item);
						} else {
							mTodayItemList.objects.put(mTodayItemList.count++, item);
						}
					}
					mTodayItemList.num_pages = (int) FloatMath.ceil((float)mTodayItemList.count / (float)ItemCollection.NUM_PER_PAGE);
					mYesterdayItemList.num_pages = (int) FloatMath.ceil((float)mYesterdayItemList.count /(float) ItemCollection.NUM_PER_PAGE);
					mEarlyItemList.num_pages = (int) FloatMath.ceil((float)mEarlyItemList.count / (float)ItemCollection.NUM_PER_PAGE);
					if(mTodayItemList.count > 0) {
						Section todaySection = new Section();
						todaySection.slug = mTodayItemList.slug;
						todaySection.title = mTodayItemList.title;
						todaySection.count = mTodayItemList.count;
						mSectionList.add(todaySection);
						Arrays.fill(mTodayItemList.hasFilledValidItem, true);
					}
					if(mYesterdayItemList.count > 0) {
						Section yesterdaySection = new Section();
						yesterdaySection.slug = mYesterdayItemList.slug;
						yesterdaySection.title = mYesterdayItemList.title;
						yesterdaySection.count = mYesterdayItemList.count;
						mSectionList.add(yesterdaySection);
						Arrays.fill(mYesterdayItemList.hasFilledValidItem, true);
					}
					if(mEarlyItemList.count > 0) {
						Section earlySection = new Section();
						earlySection.slug = mEarlyItemList.slug;
						earlySection.title = mEarlyItemList.title;
						earlySection.count = mEarlyItemList.count;
						mSectionList.add(earlySection);
						Arrays.fill(mEarlyItemList.hasFilledValidItem, true);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if(mSectionList!=null && mSectionList.size() > 0) {
				mScrollableSectionList.init(mSectionList, 1365);
				ArrayList<ItemCollection> itemCollections = new ArrayList<ItemCollection>();
				if(mTodayItemList.count > 0) {
					itemCollections.add(mTodayItemList);
				}
				if(mYesterdayItemList.count > 0) {
					itemCollections.add(mYesterdayItemList);
				}
				if(mEarlyItemList.count > 0) {
					itemCollections.add(mEarlyItemList);
				}
				mHGridAdapter = new HGridAdapterImpl(getActivity(), itemCollections);
				mHGridView.setAdapter(mHGridAdapter);
				mHGridView.setFocusable(true);
				mHGridView.setHorizontalFadingEdgeEnabled(true);
				mHGridView.setFadingEdgeLength(144);
				int rows = mHGridView.getRows();
				int totalColumnsOfSectionX = (int) FloatMath.ceil((float)mHGridAdapter.getSectionCount(mCurrentSectionPosition) / (float)rows);
				mScrollableSectionList.setPercentage(mCurrentSectionPosition, (int)(1f/(float)totalColumnsOfSectionX*100f));
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

		private Item item;
		
		@Override
		protected void onPreExecute() {
			if(mLoadingDialog!=null && !mLoadingDialog.isShowing()) {
				mLoadingDialog.show();
			}
			isInGetItemTask = true;
		}

		@Override
		protected Integer doInBackground(Item... params) {
			item = params[0];
			Item i;
			try {
				i = mRestClient.getItem(item.url);
			} catch (ItemOfflineException e) {
				e.printStackTrace();
				return ITEM_OFFLINE;
			}
			if(i==null) {
				return NETWORK_EXCEPTION;
			} else {
				item = i;
				return ITEM_SUCCESS_GET;
			}
			
		}

		@Override
		protected void onPostExecute(Integer result) {
			if(result== ITEM_OFFLINE) {
				showDialog(AlertDialogFragment.ITEM_OFFLINE_DIALOG, null, new Object[]{item.url});
			} else if(result == NETWORK_EXCEPTION) {
				showDialog(AlertDialogFragment.NETWORK_EXCEPTION_DIALOG, new GetItemTask(), new Item[]{item});
			} else {
				String url = SimpleRestClient.sRoot_url + "/api/item/" + item.pk + "/";
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
				mDataCollectionProperties.put("quality", history.last_quality);
				// start a new activity.
				Intent intent = new Intent();
				if(item.is_complex) {
					intent.setAction("tv.ismar.daisy.Item");
				} else {
					intent.setAction("tv.ismar.daisy.Play");
				}
				intent.putExtra("url", url);
				startActivity(intent);
			}
			if(mLoadingDialog!=null && mLoadingDialog.isShowing()) {
				mLoadingDialog.dismiss();
			}
			isInGetItemTask = false;
		}
		
	}
	
	private void no_video() {
		mNoVideoContainer.setVisibility(View.VISIBLE);
		mNoVideoContainer.setBackgroundResource(R.drawable.history_no_video);
		mScrollableSectionList.setVisibility(View.GONE);
		mHGridView.setVisibility(View.GONE);
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
		mScrollableSectionList.reset();
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
					DaisyUtils.getHistoryManager(getActivity()).deleteAll();
					reset();
				}
			}
			break;
		}
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
		mScrollableSectionList.setPercentage(sectionIndex, percentage);
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		mSelectedPosition = INVALID_POSITION;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Item item = mHGridAdapter.getItem(position);
		new GetItemTask().execute(item);
	}
	
}
