package tv.ismar.daisy.views;

import java.util.ArrayList;
import java.util.HashMap;

import tv.ismar.daisy.R;
import tv.ismar.daisy.VodApplication;
import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.models.ContentModel;
import tv.ismar.daisy.models.Favorite;
import tv.ismar.daisy.models.Item;
import tv.ismar.daisy.models.ItemList;
import tv.ismar.daisy.models.Section;
import tv.ismar.daisy.models.SectionList;
import tv.ismar.daisy.persistence.FavoriteManager;
import tv.ismar.daisy.views.ItemListScrollView.OnColumnChangeListener;
import tv.ismar.daisy.views.ItemListScrollView.OnItemClickedListener;
import tv.ismar.daisy.views.ItemListScrollView.OnSectionPrepareListener;
import tv.ismar.daisy.views.ScrollableSectionList.OnSectionSelectChangedListener;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
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
			mScrollableSectionList.init(mSectionList);
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
	@Override
	public void onItemClicked(String url) {
		// TODO Auto-generated method stub
		
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
	
}