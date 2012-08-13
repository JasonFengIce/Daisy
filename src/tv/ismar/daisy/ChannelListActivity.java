package tv.ismar.daisy;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tv.ismar.daisy.core.NetworkUtils;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.models.ItemList;
import tv.ismar.daisy.models.SectionList;
import tv.ismar.daisy.views.ItemListScrollView;
import tv.ismar.daisy.views.ItemListScrollView.OnColumnChangeListener;
import tv.ismar.daisy.views.ItemListScrollView.OnItemClickedListener;
import tv.ismar.daisy.views.ItemListScrollView.OnSectionPrepareListener;
import tv.ismar.daisy.views.ScrollableSectionList;
import tv.ismar.daisy.views.ScrollableSectionList.OnSectionSelectChangedListener;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

public class ChannelListActivity extends Activity {

	/*
	 * defines max number of cells can be show in a single screen.
	 */
	private static final int MAX_CELLS_IN_SCREEN = 15;
	
	private SimpleRestClient mRestClient = new SimpleRestClient();
	
	private SectionList mSectionList;
	
	private ArrayList<ItemList> mItemLists;
	
	private ItemListScrollView mItemListScrollView;
	
	private ScrollableSectionList mScrollableSectionList;
	
	private TextView mChannelLabel;
	
	private int mCurrentSectionPosition = 0;
	
	private void initViews() {
		mItemListScrollView = (ItemListScrollView) findViewById(R.id.itemlist_scroll_view);
		mItemListScrollView.setOnSectionPrepareListener(mOnSectionPrepareListener);
		mItemListScrollView.setOnColumnChangeListener(mOnColumnChangeListener);
		mItemListScrollView.setOnItemClickedListener(mOnItemClickedListener);
		mScrollableSectionList = (ScrollableSectionList) findViewById(R.id.section_tabs);
		mScrollableSectionList.setOnSectionSelectChangeListener(mOnSectionSelectChangedListener);
		
		mChannelLabel = (TextView) findViewById(R.id.channel_label);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_view);
		initViews();
		Intent intent = getIntent();
		String url = null;
		String title = null;
		String channel = null;
		if(intent!=null){
			Bundle bundle = intent.getExtras();
			if(bundle!=null){
				url =bundle.getString("url");
				
				title = bundle.getString("title");
				
				channel = bundle.getString("channel");
			}
		}
		if(url==null) {
			url = "http://127.0.0.1:21098/cord/api/tv/sections/teleplay/";
		}
		if(title==null) {
			title = "电视剧";
		}
		if(channel==null) {
			channel = "teleplay";
		}
		if(channel!=null && channel.contains("$")) {
			return;
		}
		mChannelLabel.setText(title);
		new InitTask().execute(url, channel);
	}
	
	private boolean isChannelUrl(String url) {
		String patternStr = ".+/api/tv/sections/[\\w\\d]+/";
		Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(url);
		return matcher.matches();
	}
	
	
	
	class InitTask extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			mItemLists = new ArrayList<ItemList>();
			String url = params[0];
			String channel = params[1];
			if(!isChannelUrl(url)){
				mSectionList = mRestClient.getSectionsByChannel(channel);
				for(int i=0; i<mSectionList.size(); i++) {
					if(NetworkUtils.urlEquals(url, mSectionList.get(i).url)) {
						mCurrentSectionPosition = i;
						break;
					}
				}
			}
			if(mSectionList==null){
				mSectionList = mRestClient.getSections(url);
				if(mSectionList==null) {
					mSectionList = mRestClient.getSections(url);
				}
			}
			int itemsCount = 0;
			for(int i=0; i<mSectionList.size();i++) {
				ItemList itemList = null;
				if(i==mCurrentSectionPosition || itemsCount<=MAX_CELLS_IN_SCREEN){
					itemList = mRestClient.getItemList(mSectionList.get(i).url);
					// if the itemList's items is not able to fill the full single screen. we need load the second itemList.
					if(itemList.objects!=null) {
						itemsCount += itemList.objects.size();
					}
				} else {
					itemList = new ItemList();
				}
				itemList.slug = mSectionList.get(i).slug;
				itemList.title = mSectionList.get(i).title;
				mItemLists.add(itemList);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if(mSectionList!=null && mItemLists.get(mCurrentSectionPosition)!=null) {
				mScrollableSectionList.init(mSectionList);
				for(int i=0; i<mItemLists.size(); i++) {
					mItemListScrollView.addSection(mItemLists.get(i), i);
				}
				int totalColumnsOfSectionX = mItemListScrollView.getTotalColumnCount(mCurrentSectionPosition);
				mScrollableSectionList.setPercentage(mCurrentSectionPosition, (int)(1f/(float)totalColumnsOfSectionX*100f));
				mItemListScrollView.jumpToSection(mCurrentSectionPosition);
			}
//			new GetItemListTask().execute();
			super.onPostExecute(result);
		}
		
	}
	
	class GetItemListTask extends AsyncTask<String, Void, Integer> {

		@Override
		protected Integer doInBackground(String... params) {
			int position = -1;
//			if(params.length==0){
//				for(int i=0;i<mSectionList.size();i++){
//					if(mItemLists.get(i).objects==null){
//						mItemLists.set(i, mRestClient.getItemList(mSectionList.get(i).url));
//						mItemLists.get(i).slug = mSectionList.get(i).slug;
//						mItemLists.get(i).title = mSectionList.get(i).title;
//					}
//				}
//			} else {
			String slug = params[0];
			String url = params[1];
			
			for(int i=0; i<mItemLists.size();i++){
				if(slug.equals(mItemLists.get(i).slug)){
					ItemList itemList = mRestClient.getItemList(url);
					if(itemList==null) {
						continue;
					}
					itemList.slug = mSectionList.get(i).slug;
					itemList.title = mSectionList.get(i).title;
					mItemLists.set(i, itemList);
					position = i;
					break;
				}
			}
//			}
			return position;
		}

		@Override
		protected void onPostExecute(Integer position) {
			if(position!=-1) {
				mItemListScrollView.updateSection(mItemLists.get(position), position);
			}
		}
		
	}
	
	private OnSectionPrepareListener mOnSectionPrepareListener = new OnSectionPrepareListener() {
		
		@Override
		public void onPrepareNeeded(int position) {
			if(mItemLists.get(position).objects==null){
				mItemListScrollView.addScrapViewsToSection(position, 0);
				new GetItemListTask().execute(mItemLists.get(position).slug, mSectionList.get(position).url);
			} else {
				mItemListScrollView.updateSection(mItemLists.get(position), position);
			}
		}
	};
	
	private OnSectionSelectChangedListener mOnSectionSelectChangedListener = new OnSectionSelectChangedListener() {
		
		@Override
		public void onSectionSelectChanged(int index) {
			if(mItemLists.get(index).objects!=null){
				mItemListScrollView.updateSection(mItemLists.get(index), index);
			} else {
				mItemListScrollView.addScrapViewsToSection(index, 0);
				new GetItemListTask().execute(mItemLists.get(index).slug, mSectionList.get(index).url);
			}
			mItemListScrollView.jumpToSection(index);
		}
	};
	
	private OnColumnChangeListener mOnColumnChangeListener = new OnColumnChangeListener() {
		
		@Override
		public void onColumnChanged(int position, int column, int totalColumn) {
			int percentage = (int)((float)(column+1)/(float)totalColumn*100f);
			mScrollableSectionList.setPercentage(position, percentage);
		}
	};
	
	private OnItemClickedListener mOnItemClickedListener = new OnItemClickedListener() {
		
		@Override
		public void onItemClicked(String url) {
			Intent intent = new Intent("tv.ismar.daisy.Item");
			intent.putExtra("url", url);
			startActivity(intent);
		}
	};

	@Override
	protected void onResume() {
		if(mItemListScrollView != null) {
			mItemListScrollView.setPause(false);
		}
		super.onResume();
	}

	@Override
	protected void onPause() {
		if(mItemListScrollView != null) {
			mItemListScrollView.setPause(true);
		}
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
}
