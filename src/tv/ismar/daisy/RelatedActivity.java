package tv.ismar.daisy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import tv.ismar.daisy.adapter.RelatedAdapter;
import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.NetworkUtils;
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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class RelatedActivity extends Activity implements OnSectionSelectChangedListener, OnItemClickListener {
	
	private static final String TAG = "RelatedActivity";
	
	private ScrollableSectionList mSectionTabs;
	
	private GridView mItemListGrid;
	
	private ArrayList<Item> mRelatedItem;
	
	private Item mItem;
	
	private SectionList mVirtualSectionList;
	
	private SimpleRestClient mSimpleRestClient;
	
	private RelatedAdapter mAdapter;
	
	private LoadingDialog mLoadingDialog;
	
	private GetRelatedTask mGetRelatedTask;
	private GetRelatedItemByInfo mGetRelatedItemByInfoTask;
	
	private HashMap<String, Object> mDataCollectionProperties = new HashMap<String, Object>();
	
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
				Object relatedlistObj = bundle.getSerializable("related_item");
				if(relatedlistObj != null) {
					mRelatedItem = (ArrayList<Item>) relatedlistObj;
				}
				if(mRelatedItem==null || mRelatedItem.size()==0) {
					mGetRelatedTask = new GetRelatedTask();
					mGetRelatedTask.execute(mItem.pk);
				} else {
					initLayout();
				}
				DaisyUtils.getVodApplication(this).addActivityToPool(this.toString(), this);
			} catch (Exception e) {
				e.printStackTrace();
				showToast(getResources().getString(R.string.no_related_video));
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
			try {
				int pk = params[0];
				Item[] relatedArray = mSimpleRestClient.getRelatedItem("/api/tv/relate/"+pk+"/");
				if(relatedArray!=null && relatedArray.length >0 ) {
					mRelatedItem = new ArrayList<Item>(Arrays.asList(relatedArray));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if(mRelatedItem!=null && mRelatedItem.size()>0){
				try {
					initLayout();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				showToast(getResources().getString(R.string.no_related_video));
				RelatedActivity.this.finish();
			}
		}
		
	}
	
	private void initLayout() {
		// Data collection.
		mDataCollectionProperties.put("item", mItem.pk);
		mDataCollectionProperties.put("title", mItem.title);
		if(mItem.clip!=null) {
			mDataCollectionProperties.put("clip", mItem.clip.pk);
		} else {
			mDataCollectionProperties.put("clip", "");
		}
		new NetworkUtils.DataCollectionTask().execute(NetworkUtils.VIDEO_RELATE_IN, mDataCollectionProperties);
		
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
		final HashMap<String, Object> properties = new HashMap<String, Object>();
		properties.putAll(mDataCollectionProperties);
		new NetworkUtils.DataCollectionTask().execute(NetworkUtils.VIDEO_RELATE_OUT, properties);
		mDataCollectionProperties.remove("to_item");
		mDataCollectionProperties.remove("to_title");
		super.onPause();
	}
	
	
	@Override
	protected void onDestroy() {
		if(mGetRelatedTask!=null && mGetRelatedTask.getStatus()!=AsyncTask.Status.FINISHED) {
			mGetRelatedTask.cancel(true);
		}
		if(mGetRelatedItemByInfoTask!=null && mGetRelatedItemByInfoTask.getStatus()!=AsyncTask.Status.FINISHED) {
			mGetRelatedItemByInfoTask.cancel(true);
		}
		mGetRelatedTask = null;
		mAdapter = null;
		mVirtualSectionList = null;
		mRelatedItem = null;
		mSectionTabs = null;
		mSimpleRestClient = null;
		mLoadingDialog = null;
		DaisyUtils.getVodApplication(this).removeActivtyFromPool(this.toString());
		super.onDestroy();
	}

	@Override
	public void onSectionSelectChanged(int index) {
		Section section = mVirtualSectionList.get(index);
		if(mGetRelatedItemByInfoTask!=null && mGetRelatedItemByInfoTask.getStatus()!=AsyncTask.Status.FINISHED) {
			mGetRelatedItemByInfoTask.cancel(true);
		}
		mGetRelatedItemByInfoTask = new GetRelatedItemByInfo();
		mGetRelatedItemByInfoTask.execute(section);
	}
	
	class GetRelatedItemByInfo extends AsyncTask<Section, Void, Void> {

		@Override
		protected Void doInBackground(Section... params) {
			try {
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
			} catch (Exception e) {
				e.printStackTrace();
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
		Item item = mAdapter.getItem(position);
		mDataCollectionProperties.put("to_item", item.pk);
		mDataCollectionProperties.put("to_title", item.title);
		if(item.clip!=null) {
			mDataCollectionProperties.put("clip", item.clip);
		} else {
			mDataCollectionProperties.put("clip", "");
		}
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
	
	private void showToast(String text) {
		LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(R.layout.simple_toast, (ViewGroup) findViewById(R.id.simple_toast_root));
		TextView toastText = (TextView) layout.findViewById(R.id.toast_text);
		toastText.setText(text);
		Toast toast = new Toast(getApplicationContext());
		toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setView(layout);
		toast.show();
	}
}
