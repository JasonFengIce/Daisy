package tv.ismar.daisy;

import java.util.ArrayList;
import java.util.Arrays;
import tv.ismar.daisy.adapter.RelatedAdapter;
import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.models.Attribute;
import tv.ismar.daisy.models.Item;
import tv.ismar.daisy.models.ItemList;
import tv.ismar.daisy.models.Section;
import tv.ismar.daisy.models.SectionList;
import tv.ismar.daisy.views.LoadingDialog;
import tv.ismar.daisy.views.ScrollableSectionList;
import tv.ismar.daisy.views.ScrollableSectionList.OnSectionSelectChangedListener;
import android.app.Activity;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class RelatedActivity extends Activity implements OnSectionSelectChangedListener, OnItemClickListener {
	
	private static final String TAG = "RelatedActivity";
	
	private String mUrl;
	
	private ScrollableSectionList mSectionTabs;
	
	private GridView mItemListGrid;
	
	private ArrayList<Item> mRelatedItem;
	
	private Item mItem;
	
	private SectionList mVirtualSectionList;
	
	private SimpleRestClient mSimpleRestClient;
	
	private RelatedAdapter mAdapter;
	
	private LoadingDialog mLoadingDialog;
	
	private void initViews(){
		mSectionTabs = (ScrollableSectionList) findViewById(R.id.related_section_tabs);
		mSectionTabs.setOnSectionSelectChangeListener(this);
		mItemListGrid = (GridView) findViewById(R.id.related_list);
		mItemListGrid.setOnItemClickListener(this);
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.related_view);
		mSimpleRestClient = new SimpleRestClient();
		initViews();
		mLoadingDialog = new LoadingDialog(this, getResources().getString(R.string.vod_loading));
		mLoadingDialog.setOnCancelListener(mLoadingCancelListener);
		mLoadingDialog.show();
		Intent intent = getIntent();
		if(intent!=null) {
			Bundle bundle = intent.getExtras();
			try {
				mItem = (Item) bundle.getSerializable("item");
				mUrl = mSimpleRestClient.root_url + "/api/item/" + mItem.pk +"/";
				Object relatedlistObj = bundle.getSerializable("related_item");
				if(relatedlistObj != null) {
					mRelatedItem = (ArrayList<Item>) relatedlistObj;
				}
				if(mRelatedItem==null || mRelatedItem.size()==0) {
					new GetRelatedTask().execute(mItem.pk);
				} else {
					initLayout();
				}
				DaisyUtils.getVodApplication(this).addActivityToPool(this);
			} catch (Exception e) {
				e.printStackTrace();
				this.finish();
			}
		}
	}
	
	private void initSectionTabs() {
		mVirtualSectionList = new SectionList();
		Section firstSection = new Section();
		firstSection.title = getResources().getString(R.string.same_category_clip);
		firstSection.slug = "default";
		mVirtualSectionList.add(firstSection);
		if(mItem.attributes!=null && mItem.attributes.map != null) {
			Object actorInfoObj = mItem.attributes.map.get("actor");
			Object directorInfoObj = mItem.attributes.map.get("director");
			if(actorInfoObj!=null) {
				Attribute.Info[] actorInfo = (Attribute.Info[]) actorInfoObj;
				for(Attribute.Info actor: actorInfo) {
					Section actorSection = new Section();
					actorSection.title = actor.name;
					actorSection.slug = "actor";
					actorSection.template = actor.id;
					mVirtualSectionList.add(actorSection);
				}
			}
			if(directorInfoObj!=null) {
				Attribute.Info[] directorInfo = (Attribute.Info[]) directorInfoObj;
				for(Attribute.Info director: directorInfo) {
					Section directionSection = new Section();
					directionSection.title = director.name;
					directionSection.slug = "director";
					directionSection.template = director.id;
					mVirtualSectionList.add(directionSection);
				}
			}
		}
	}
	
	class GetRelatedTask extends AsyncTask<Integer, Void, Void> {

		@Override
		protected Void doInBackground(Integer... params) {
			int pk = params[0];
			Item[] relatedArray = mSimpleRestClient.getRelatedItem("/api/tv/relate/"+pk+"/");
			if(relatedArray!=null && relatedArray.length >0 ) {
				mRelatedItem = new ArrayList<Item>(Arrays.asList(relatedArray));
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if(mRelatedItem!=null && mRelatedItem.size()>0){
				initLayout();
			}
		}
		
	}
	
	private void initLayout() {
		initSectionTabs();
		mSectionTabs.init(mVirtualSectionList, 1681);
		buildGridView();
		if(mLoadingDialog.isShowing()){
			mLoadingDialog.dismiss();
		}
	}
	
	private void buildGridView() {
		mAdapter = new RelatedAdapter(this, mRelatedItem);
		mItemListGrid.setAdapter(mAdapter);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onPause() {
		if(mLoadingDialog!=null && mLoadingDialog.isShowing()) {
			mLoadingDialog.dismiss();
		}
		super.onPause();
	}
	
	
	@Override
	protected void onDestroy() {
		mAdapter = null;
		mVirtualSectionList = null;
		mRelatedItem = null;
		mSectionTabs = null;
		mSimpleRestClient = null;
		mLoadingDialog = null;
		DaisyUtils.getVodApplication(this).removeActivtyFromPool();
		super.onDestroy();
	}

	@Override
	public void onSectionSelectChanged(int index) {
		Section section = mVirtualSectionList.get(index);
		new GetRelatedItemByInfo().execute(section);
	}
	
	class GetRelatedItemByInfo extends AsyncTask<Section, Void, Void> {

		@Override
		protected Void doInBackground(Section... params) {
			Section section = params[0];
			String url = null;
			if(section.slug.equals("default")) {
				url = "/api/tv/relate/"+mItem.pk+"/";
				Item[] itemArray = mSimpleRestClient.getRelatedItem(url);
				if(itemArray!=null) {
					mRelatedItem = new ArrayList<Item>(Arrays.asList(itemArray));
				}
			} else {
				url = mSimpleRestClient.root_url + "/api/tv/filtrate/$" + mItem.content_model +"/" + section.slug + "*" + section.template +"/";
				ItemList itemList = mSimpleRestClient.getItemList(url);
				if(itemList!=null) {
					mRelatedItem = itemList.objects;
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if(mRelatedItem!=null) {
				mAdapter.cancel();
				buildGridView();
			}
		}
		
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Log.d(TAG, view.toString());
		Item item = mAdapter.getItem(position);
		Intent intent = new Intent("tv.ismar.daisy.Item");
		intent.putExtra("url", item.item_url);
		startActivity(intent);
	}
	
	
	private OnCancelListener mLoadingCancelListener = new OnCancelListener() {
		
		@Override
		public void onCancel(DialogInterface dialog) {
			RelatedActivity.this.finish();
		}
	};
	
}
