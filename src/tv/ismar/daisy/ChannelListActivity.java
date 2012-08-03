package tv.ismar.daisy;

import java.util.ArrayList;

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

	
	private SimpleRestClient mRestClient = new SimpleRestClient();
	
	private SectionList mSectionList;
	
	private ArrayList<ItemList> mItemLists;
	
	private ItemListScrollView mItemListScrollView;
	
	private ScrollableSectionList mScrollableSectionList;
	
	private TextView mChannelLabel;
	
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
		if(intent!=null){
			Bundle bundle = intent.getExtras();
			if(bundle!=null){
				url =bundle.getString("url");
				
				title = bundle.getString("title");
				
				
			}
		}
		if(url==null) {
			url = "http://127.0.0.1:21098/cord/api/tv/sections/chinesemovie/";
		}
		if(title==null) {
			title = "华语电影";
		}
		mChannelLabel.setText(title);
		new InitTask().execute(url);
	}
	
	
	class InitTask extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			mItemLists = new ArrayList<ItemList>();
			String url = params[0];
			mSectionList = mRestClient.getSections(url);
			int itemsCount = 0;
			for(int i=0; i<mSectionList.size();i++) {
				ItemList itemList = null;
				if(i==0 || itemsCount<=15){
					itemList = mRestClient.getItemList(mSectionList.get(i).url);
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
			if(mSectionList!=null && mItemLists.get(0)!=null) {
				mScrollableSectionList.init(mSectionList);
				for(int i=0; i<mItemLists.size(); i++) {
					mItemListScrollView.addSection(mItemLists.get(i), i);
				}
				int totalColumnsOfSection0 = mItemListScrollView.getTotalColumnCount(0);
				mScrollableSectionList.setPercentage(0, (int)(1f/(float)totalColumnsOfSection0*100f));
			}
			new GetItemListTask().execute();
			super.onPostExecute(result);
		}
		
	}
	
	class GetItemListTask extends AsyncTask<String, Void, Integer> {

		@Override
		protected Integer doInBackground(String... params) {
			int position = -1;
			if(params.length==0){
				for(int i=0;i<mSectionList.size();i++){
					if(mItemLists.get(i).objects==null){
						mItemLists.set(i, mRestClient.getItemList(mSectionList.get(i).url));
						mItemLists.get(i).slug = mSectionList.get(i).slug;
						mItemLists.get(i).title = mSectionList.get(i).title;
					}
				}
			} else {
				String slug = params[0];
				String url = params[1];
				
				for(int i=0; i<mItemLists.size();i++){
					if(slug.equals(mItemLists.get(i).slug)){
						ItemList itemList = mRestClient.getItemList(url);
						itemList.slug = mSectionList.get(i).slug;
						itemList.title = mSectionList.get(i).title;
						mItemLists.set(i, itemList);
						position = i;
						break;
					}
				}
			}
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
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
}
