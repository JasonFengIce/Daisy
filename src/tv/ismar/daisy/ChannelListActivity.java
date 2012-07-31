package tv.ismar.daisy;

import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.models.Item;
import tv.ismar.daisy.models.ItemList;
import tv.ismar.daisy.models.Section;
import tv.ismar.daisy.models.SectionList;
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
	
	private ItemList mCurrentItemList;
	private ItemList mNextItemList;
	private ItemList mPrevItemList;
	
	HorizontalScrollView mScrollView;
	LinearLayout mContainer;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_view);
		mScrollView = (HorizontalScrollView) findViewById(R.id.scroll_container);
		mContainer = (LinearLayout) findViewById(R.id.inner_holder);
		Intent intent = getIntent();
		if(intent!=null){
			String url = intent.getStringExtra("url");
			if(url!=null) {
				new InitTask().execute(url);
			}
		}
		
		
		
	}
	
	
	class InitTask extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			String url = params[0];
			mSectionList = mRestClient.getSections(url);
			if(mSectionList.size()>0){
				Section section0 = mSectionList.get(0);
				mCurrentItemList = mRestClient.getItemList(section0.url);
				mCurrentItemList.slug = section0.slug;
				if(mSectionList.size()>1) {
					Section section1 = mSectionList.get(1);
					mNextItemList = mRestClient.getItemList(section1.url);
					mNextItemList.slug= section1.slug;
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			LinearLayout linear1 = new LinearLayout(ChannelListActivity.this);
			linear1.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
			
			for(int i=0; i< mCurrentItemList.count; i++) {
				Item item = mCurrentItemList.objects.get(i);
				Button button = new Button(ChannelListActivity.this);
				button.setText(item.title);
				button.setLayoutParams( new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
				button.setTag(mCurrentItemList.slug);
				button.setOnFocusChangeListener(mFocusChangeListener);
				linear1.addView(button,i);
			}
			mContainer.addView(linear1, 0);
			
			LinearLayout linear2 = new LinearLayout(ChannelListActivity.this);
			linear2.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
			
			
			for(int i=0; i< mNextItemList.count; i++) {
				Item item = mNextItemList.objects.get(i);
				Button button = new Button(ChannelListActivity.this);
				button.setLayoutParams( new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
				button.setText(item.title);
				button.setTag(mNextItemList.slug);
				button.setOnFocusChangeListener(mFocusChangeListener);
				linear2.addView(button, i);
			}
			mContainer.addView(linear2, 1);
			super.onPostExecute(result);
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
