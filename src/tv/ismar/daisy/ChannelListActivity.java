package tv.ismar.daisy;

import java.util.ArrayList;

import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.models.Item;
import tv.ismar.daisy.models.ItemList;
import tv.ismar.daisy.models.Section;
import tv.ismar.daisy.models.SectionList;
import tv.ismar.daisy.views.ItemListContainer;
import tv.ismar.daisy.views.ItemListScrollView;
import tv.ismar.daisy.views.ItemListScrollView.OnSectionPrepareListener;
import tv.ismar.daisy.views.MainItemsView;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View.OnFocusChangeListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

public class ChannelListActivity extends Activity {

	
	private SimpleRestClient mRestClient = new SimpleRestClient();
	
	private SectionList mSectionList;
	
	private ArrayList<ItemList> mItemLists;
	
	
	private ItemListScrollView mItemListScrollView;
	
	
	private void initViews() {
		mItemListScrollView = (ItemListScrollView) findViewById(R.id.itemlist_scroll_view);
		mItemListScrollView.setOnSectionPrepareListener(mOnSectionPrepareListener);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_view);
		initViews();
		Intent intent = getIntent();
		if(intent!=null){
			String url = intent.getStringExtra("url");
			if(url==null) {
				url = "http://cord.tvxio.com/api/tv/sections/chinesemovie/";
			}
			new InitTask().execute(url);
		}
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
				for(int i=0; i<mItemLists.size(); i++) {
					mItemListScrollView.addSection(mItemLists.get(i), i);
				}
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
	
	private OnFocusChangeListener mFocusChangeListener = new OnFocusChangeListener() {
		
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			String slug = (String) v.getTag();
			Log.d("Current", slug);
//			if(slug.equals(mNextItemList.slug)){
//				mContainer.snapToView(1);
//			}
		}
	};
	
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
