package tv.ismar.daisy.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;

import tv.ismar.daisy.ChannelListActivity;
import tv.ismar.daisy.ChannelListActivity.OnMenuToggleListener;
import tv.ismar.daisy.R;
import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.ItemOfflineException;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.models.History;
import tv.ismar.daisy.models.Item;
import tv.ismar.daisy.models.ItemList;
import tv.ismar.daisy.models.Section;
import tv.ismar.daisy.models.SectionList;
import tv.ismar.daisy.persistence.HistoryManager;
import tv.ismar.daisy.views.ItemListScrollView.OnColumnChangeListener;
import tv.ismar.daisy.views.ItemListScrollView.OnItemClickedListener;
import tv.ismar.daisy.views.ItemListScrollView.OnSectionPrepareListener;
import tv.ismar.daisy.views.MenuFragment.MenuItem;
import tv.ismar.daisy.views.MenuFragment.OnMenuItemClickedListener;
import tv.ismar.daisy.views.ScrollableSectionList.OnSectionSelectChangedListener;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class HistoryFragment extends Fragment implements OnSectionPrepareListener, 
														OnColumnChangeListener, 
														OnItemClickedListener, 
														OnSectionSelectChangedListener, 
														OnMenuToggleListener,
														OnMenuItemClickedListener{
	
	private HistoryManager mHistoryManager;
	private ItemListScrollView mItemListScrollView;
	private ScrollableSectionList mScrollableSectionList;
	private TextView mChannelLabel;
	
	private SectionList mSectionList;
	
	private ItemList mTodayItemList;
	private ItemList mYesterdayItemList;
	private ItemList mEarlyItemList;
	private int mCurrentSectionPosition = 0;
	private SimpleRestClient mRestClient;
	
	private RelativeLayout mNoVideoContainer;
	
	private LoadingDialog mLoadingDialog;
	
	private boolean isInGetHistoryTask;
	private boolean isInGetItemTask;
	
	private MenuFragment mMenuFragment;
	
	public final static String MENU_TAG = "HistoryMenu";
	
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
		mItemListScrollView = (ItemListScrollView) fragmentView.findViewById(R.id.itemlist_scroll_view);
		mItemListScrollView.setOnSectionPrepareListener(this);
		mItemListScrollView.setOnColumnChangeListener(this);
		mItemListScrollView.setOnItemClickedListener(this);
		mScrollableSectionList = (ScrollableSectionList) fragmentView.findViewById(R.id.section_tabs);
		mScrollableSectionList.setOnSectionSelectChangeListener(this);
		
		mChannelLabel = (TextView) fragmentView.findViewById(R.id.channel_label);
		mChannelLabel.setText(getResources().getString(R.string.vod_movielist_title_history));
		
		mNoVideoContainer = (RelativeLayout) fragmentView.findViewById(R.id.no_video_container);
	}
	
	private void initHistoryList(){
		
		//define today's ItemList
		mTodayItemList = new ItemList();
		mTodayItemList.objects = new ArrayList<Item>();
		mTodayItemList.slug = "today";
		mTodayItemList.title = getResources().getString(R.string.vod_movielist_today);
		//define yesterday's ItemList
		mYesterdayItemList = new ItemList();
		mYesterdayItemList.objects = new ArrayList<Item>();
		mYesterdayItemList.slug = "yesterday";
		mYesterdayItemList.title = getResources().getString(R.string.vod_movielist_yesterday);
		//define early days's ItemList
		mEarlyItemList = new ItemList();
		mEarlyItemList.objects = new ArrayList<Item>();
		mEarlyItemList.slug = "early";
		mEarlyItemList.title = getResources().getString(R.string.vod_movielist_recent);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mHistoryManager = DaisyUtils.getHistoryManager(getActivity());
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
		new GetHistoryTask().execute();
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
			final long todayStartPoint = getTodayStartPoint();
			final long yesterdayStartPoint = getYesterdayStartPoint();
			ArrayList<History> mHistories = mHistoryManager.getAllHistories();
			if(mHistories.size()>0) {
				Collections.sort(mHistories);
				for(int i=0;i<mHistories.size();++i) {
					History history = mHistories.get(i);
					Item item = getItem(history);
					if(history.last_played_time < yesterdayStartPoint){
						mEarlyItemList.objects.add(item);
					} else if(history.last_played_time > yesterdayStartPoint && history.last_played_time < todayStartPoint) {
						mYesterdayItemList.objects.add(item);
					} else {
						mTodayItemList.objects.add(item);
					}
				}
				mTodayItemList.count = mTodayItemList.objects.size();
				mYesterdayItemList.count = mYesterdayItemList.objects.size();
				mEarlyItemList.count = mEarlyItemList.objects.size();
				if(mTodayItemList.count > 0) {
					Section todaySection = new Section();
					todaySection.slug = mTodayItemList.slug;
					todaySection.title = mTodayItemList.title;
					todaySection.count = mTodayItemList.count;
					mSectionList.add(todaySection);
				}
				if(mYesterdayItemList.count > 0) {
					Section yesterdaySection = new Section();
					yesterdaySection.slug = mYesterdayItemList.slug;
					yesterdaySection.title = mYesterdayItemList.title;
					yesterdaySection.count = mYesterdayItemList.count;
					mSectionList.add(yesterdaySection);
				}
				if(mEarlyItemList.count > 0) {
					Section earlySection = new Section();
					earlySection.slug = mEarlyItemList.slug;
					earlySection.title = mEarlyItemList.title;
					earlySection.count = mEarlyItemList.count;
					mSectionList.add(earlySection);
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if(mSectionList.size() > 0) {
				mScrollableSectionList.init(mSectionList, 1365);
				int index=0;
				if(mTodayItemList.count > 0) {
					mItemListScrollView.addSection(mTodayItemList, index++);
				}
				if(mYesterdayItemList.count > 0) {
					mItemListScrollView.addSection(mYesterdayItemList, index++);
				}
				if(mEarlyItemList.count > 0) {
					mItemListScrollView.addSection(mEarlyItemList, index);
				}
				int totalColumnsOfSectionX = mItemListScrollView.getTotalColumnCount(mCurrentSectionPosition);
				mScrollableSectionList.setPercentage(mCurrentSectionPosition, (int)(1f/(float)totalColumnsOfSectionX*100f));
				mItemListScrollView.jumpToSection(mCurrentSectionPosition);
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
		if(mItemListScrollView != null) {
			mItemListScrollView.setPause(false);
		}
		((ChannelListActivity)getActivity()).registerOnMenuToggleListener(this);
		super.onResume();
	}

	@Override
	public void onPause() {
		if(mItemListScrollView != null) {
			mItemListScrollView.setPause(true);
		}
		((ChannelListActivity)getActivity()).unregisterOnMenuToggleListener();
		super.onPause();
	}
	
	@Override
	public void onSectionSelectChanged(int index) {
		Section section = mSectionList.get(index);
		if(section.slug.equals("today")) {
			mItemListScrollView.updateSection(mTodayItemList, index);
		} else if(section.slug.equals("yesterday")) {
			mItemListScrollView.updateSection(mYesterdayItemList, index);
		} else if(section.slug.equals("early")) {
			mItemListScrollView.updateSection(mEarlyItemList, index);
		}
		mItemListScrollView.jumpToSection(index);
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
				Intent intent = new Intent();
				if(item.is_complex) {
					intent.setAction("tv.ismar.daisy.Item");
				} else {
					intent.setAction("tv.ismar.daisy.Play");
				}
				intent.putExtra("url", item.url);
				startActivity(intent);
			}
			if(mLoadingDialog!=null && mLoadingDialog.isShowing()) {
				mLoadingDialog.dismiss();
			}
			isInGetItemTask = false;
		}
		
	}
	
	@Override
	public void onItemClicked(Item item) {
		new GetItemTask().execute(item);
	}
	
	@Override
	public void onColumnChanged(int position, int column, int totalColumn) {
		int percentage = (int)((float)(column+1)/(float)totalColumn*100f);
		mScrollableSectionList.setPercentage(position, percentage);
	}
	@Override
	public void onPrepareNeeded(int position) {
		Section section = mSectionList.get(position);
		if(section.slug.equals("today")) {
			mItemListScrollView.updateSection(mTodayItemList, position);
		} else if(section.slug.equals("yesterday")) {
			mItemListScrollView.updateSection(mYesterdayItemList, position);
		} else if(section.slug.equals("early")) {
			mItemListScrollView.updateSection(mEarlyItemList, position);
		}
	}
	
	private void no_video() {
		mNoVideoContainer.setVisibility(View.VISIBLE);
		mNoVideoContainer.setBackgroundResource(R.drawable.history_no_video);
		mScrollableSectionList.setVisibility(View.GONE);
		mItemListScrollView.setVisibility(View.GONE);
	}
	
	private void showDialog(final int dialogType, final AsyncTask task, final Object[] params) {
		AlertDialogFragment newFragment = AlertDialogFragment.newInstance(dialogType);
		newFragment.setPositiveListener(new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(dialogType==AlertDialogFragment.NETWORK_EXCEPTION_DIALOG && !isInGetItemTask) {
					task.execute(params);
				} else if (!isInGetHistoryTask) {
					mHistoryManager.deleteHistory((String)params[0]);
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
		mItemListScrollView.reset();
		mScrollableSectionList.reset();
		mSectionList = new SectionList();
		initHistoryList();
		new GetHistoryTask().execute();
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
			if(mItemListScrollView!=null && mItemListScrollView.mCurrentSelectedView!=null) {
				if(mItemListScrollView.mCurrentSelectedView.hasFocus()) {
					Item selectedItem = null;
					TextView titleView = (TextView) mItemListScrollView.mCurrentSelectedView.findViewById(R.id.list_item_title);
					Object obj = titleView.getTag();
					if(obj!=null) {
						selectedItem = (Item) obj;
						if(!isInGetHistoryTask && selectedItem.url!=null) {
							mHistoryManager.deleteHistory(selectedItem.url);
							reset();
						}
					}
				}
			}
			break;
		case 2:
			if(mItemListScrollView!=null) {
				if(!isInGetHistoryTask) {
					mHistoryManager.deleteAll();
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
		mLoadingDialog = null;
		mHistoryManager = null;
		mSectionList = null;
		mTodayItemList = null;
		mYesterdayItemList = null;
		mEarlyItemList = null;
		mItemListScrollView.clean();
		mItemListScrollView = null;
		mScrollableSectionList = null;
		mRestClient = null;
		mMenuFragment = null;
		super.onDetach();
	}
	
	
}
