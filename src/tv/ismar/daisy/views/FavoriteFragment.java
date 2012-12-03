package tv.ismar.daisy.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import org.sakuratya.horizontal.adapter.HGridAdapterImpl;
import org.sakuratya.horizontal.ui.HGridView;
import tv.ismar.daisy.ChannelListActivity.OnMenuToggleListener;
import tv.ismar.daisy.ChannelListActivity;
import tv.ismar.daisy.R;
import tv.ismar.daisy.VodApplication;
import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.ItemOfflineException;
import tv.ismar.daisy.core.NetworkUtils;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.models.ContentModel;
import tv.ismar.daisy.models.Favorite;
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
	
	private void initViews(View fragmentView) {
		
		mHGridView = (HGridView) fragmentView.findViewById(R.id.h_grid_view);
		mHGridView.setOnItemClickListener(this);
		mHGridView.setOnItemSelectedListener(this);
		mScrollableSectionList = (ScrollableSectionList) fragmentView.findViewById(R.id.section_tabs);
		mScrollableSectionList.setOnSectionSelectChangeListener(this);
		
		mChannelLabel = (TextView) fragmentView.findViewById(R.id.channel_label);
		mChannelLabel.setText(getResources().getString(R.string.favorite));
		
		mNoVideoContainer = (RelativeLayout) fragmentView.findViewById(R.id.no_video_container);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View fragmentView = inflater.inflate(R.layout.list_view, container, false);
		initViews(fragmentView);
		new GetFavoriteTask().execute();
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
			ArrayList<Favorite> favorites = DaisyUtils.getFavoriteManager(getActivity()).getAllFavorites();
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
			mScrollableSectionList.init(mSectionList, 1365);
			mHGridAdapter = new HGridAdapterImpl(getActivity(), mItemCollections);
			mHGridView.setAdapter(mHGridAdapter);
			mHGridView.setFocusable(true);
			mHGridView.setHorizontalFadingEdgeEnabled(true);
			mHGridView.setFadingEdgeLength(144);
			int num_rows = mHGridView.getRows();
			int totalColumnsOfSectionX = (int) FloatMath.ceil((float)mItemCollections.get(mCurrentSectionPosition).count / (float) num_rows);
			mScrollableSectionList.setPercentage(mCurrentSectionPosition, (int)(1f/(float)totalColumnsOfSectionX*100f));
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
		((ChannelListActivity)getActivity()).unregisterOnMenuToggleListener();
		HashMap<String, Object> properties = mDataCollectionProperties;
		new NetworkUtils.DataCollectionTask().execute(NetworkUtils.VIDEO_COLLECT_OUT, properties);
		mDataCollectionProperties = null;
		super.onPause();
	}
	
	private void no_video() {
		mNoVideoContainer.setVisibility(View.VISIBLE);
		mNoVideoContainer.setBackgroundResource(R.drawable.favorite_no_video);
		mScrollableSectionList.setVisibility(View.GONE);
		mHGridView.setVisibility(View.GONE);
	}
	
	private void showDialog(final int dialogType, final AsyncTask task, final Object[] params)  {
		AlertDialogFragment newFragment = AlertDialogFragment.newInstance(dialogType);
		newFragment.setPositiveListener(new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(dialogType==AlertDialogFragment.NETWORK_EXCEPTION_DIALOG && !isInGetItemTask) {
					task.execute(params);
				} else if(!isInGetFavoriteTask) {
					DaisyUtils.getFavoriteManager(getActivity()).deleteFavoriteByUrl((String)params[0]);
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
		new GetFavoriteTask().execute();
	}
	
	private void createMenu() {
		mMenuFragment = MenuFragment.newInstance();
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
						DaisyUtils.getFavoriteManager(getActivity()).deleteFavoriteByUrl(selectedItem.url);
						reset();
					}
				}
			}
			break;
		case 2:
			if(mHGridAdapter!=null) {
				if(!isInGetFavoriteTask) {
					DaisyUtils.getFavoriteManager(getActivity()).deleteAll();
					reset();
				}
			}
			break;
		}
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
		mScrollableSectionList = null;
		mRestClient = null;
		mMenuFragment = null;
		super.onDetach();
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Item item = mHGridAdapter.getItem(position);
		new GetItemTask().execute(item);
		
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
	
}
