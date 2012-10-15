package tv.ismar.daisy.views;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

import tv.ismar.daisy.R;
import tv.ismar.daisy.VodApplication;
import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.ItemOfflineException;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.models.ContentModel;
import tv.ismar.daisy.models.Favorite;
import tv.ismar.daisy.models.Item;
import tv.ismar.daisy.models.ItemList;
import tv.ismar.daisy.models.Section;
import tv.ismar.daisy.models.SectionList;
import tv.ismar.daisy.persistence.FavoriteManager;
import tv.ismar.daisy.views.HistoryFragment.GetItemTask;
import tv.ismar.daisy.views.ItemListScrollView.OnColumnChangeListener;
import tv.ismar.daisy.views.ItemListScrollView.OnItemClickedListener;
import tv.ismar.daisy.views.ItemListScrollView.OnSectionPrepareListener;
import tv.ismar.daisy.views.ScrollableSectionList.OnSectionSelectChangedListener;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FavoriteFragment extends Fragment implements OnSectionSelectChangedListener, OnColumnChangeListener, OnItemClickedListener, OnSectionPrepareListener {
	
	private ItemListScrollView mItemListScrollView;
	private ScrollableSectionList mScrollableSectionList;
	private TextView mChannelLabel;
	
	private SectionList mSectionList;
	
	private int mCurrentSectionPosition = 0;
	
	private FavoriteManager mFavoriteManager;
	private SimpleRestClient mRestClient;
	
	private ArrayList<Favorite> mFavorites;
	private ContentModel[] mContentModels;
	
	private RelativeLayout mNoVideoContainer;
	private HashMap<String, ItemList> mItemListMap;
	
	private void initViews(View fragmentView) {
		mItemListScrollView = (ItemListScrollView) fragmentView.findViewById(R.id.itemlist_scroll_view);
		mItemListScrollView.setOnSectionPrepareListener(this);
		mItemListScrollView.setOnColumnChangeListener(this);
		mItemListScrollView.setOnItemClickedListener(this);
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
		mFavoriteManager = DaisyUtils.getFavoriteManager(getActivity());
		mRestClient = new SimpleRestClient();
		VodApplication application = DaisyUtils.getVodApplication(getActivity());
		mContentModels = application.mContentModel;
		mSectionList = new SectionList();
		mItemListMap = new HashMap<String, ItemList>();
	}
	
	class GetFavoriteTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			mFavorites = mFavoriteManager.getAllFavorites();
			for(int i=0;i<mFavorites.size();i++) {
				String content_model = mFavorites.get(i).content_model;
//				String url = mFavorites.get(i).url;
				Item item = getItem(mFavorites.get(i));
				if(item!=null) {
					ItemList itemList = mItemListMap.get(content_model);
					if(itemList==null) {
						itemList = new ItemList();
						itemList.slug = content_model;
						itemList.objects = new ArrayList<Item>();
						//get title represented by content_model
						for(ContentModel cm: mContentModels) {
							if(cm.content_model.equals(content_model)) {
								itemList.title = cm.title;
								break;
							}
						}
						Section section = new Section();
						section.slug = content_model;
						section.title = itemList.title;
						mSectionList.add(section);
						mItemListMap.put(content_model, itemList);
					}
					itemList.objects.add(item);
				}
				
			}
			for(Section section: mSectionList) {
				section.count = mItemListMap.get(section.slug).objects.size();
				mItemListMap.get(section.slug).count = section.count;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if(mSectionList.size()==0) {
				no_video();
				return;
			}
			mScrollableSectionList.init(mSectionList, 1365);
			for(int i=0;i<mSectionList.size();i++) {
				ItemList itemList = mItemListMap.get(mSectionList.get(i).slug);
				mItemListScrollView.addSection(itemList, i);
			}
			int totalColumnsOfSectionX = mItemListScrollView.getTotalColumnCount(mCurrentSectionPosition);
			mScrollableSectionList.setPercentage(mCurrentSectionPosition, (int)(1f/(float)totalColumnsOfSectionX*100f));
			mItemListScrollView.jumpToSection(mCurrentSectionPosition);
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
	
	@Override
	public void onPrepareNeeded(int position) {
		Section section = mSectionList.get(position);
		ItemList itemList = mItemListMap.get(section.slug);
		mItemListScrollView.updateSection(itemList, position);
	}
	
	class GetItemTask extends AsyncTask<Item, Void, Integer> {
		
		private static final int ITEM_OFFLINE = 0;
		private static final int ITEM_SUCCESS_GET = 1;
		private static final int NETWORK_EXCEPTION = 2;
		
		private Item item;
		
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
	public void onSectionSelectChanged(int index) {
		Section section = mSectionList.get(index);
		ItemList itemList = mItemListMap.get(section.slug);
		mItemListScrollView.updateSection(itemList, index);
		mItemListScrollView.jumpToSection(index);
	}
	@Override
	public void onResume() {
		if(mItemListScrollView != null) {
			mItemListScrollView.setPause(false);
		}
		super.onResume();
	}
	@Override
	public void onPause() {
		if(mItemListScrollView != null) {
			mItemListScrollView.setPause(true);
		}
		super.onPause();
	}
	
	private void no_video() {
		mNoVideoContainer.setVisibility(View.VISIBLE);
		mNoVideoContainer.setBackgroundResource(R.drawable.favorite_no_video);
		mScrollableSectionList.setVisibility(View.GONE);
		mItemListScrollView.setVisibility(View.GONE);
	}
	
	private void showDialog(final int dialogType, final AsyncTask task, final Object[] params)  {
		AlertDialogFragment newFragment = AlertDialogFragment.newInstance(dialogType);
		newFragment.setPositiveListener(new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(dialogType==AlertDialogFragment.NETWORK_EXCEPTION_DIALOG) {
					task.execute(params);
				} else {
					mFavoriteManager.deleteFavoriteByUrl((String)params[0]);
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
		mItemListMap = new HashMap<String, ItemList>();
		new GetFavoriteTask().execute();
	}
	
}
