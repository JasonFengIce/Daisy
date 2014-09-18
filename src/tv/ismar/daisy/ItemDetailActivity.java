package tv.ismar.daisy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.JsonSyntaxException;

import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.ImageUtils;
import tv.ismar.daisy.core.NetworkUtils;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.exception.ItemOfflineException;
import tv.ismar.daisy.exception.NetworkException;
import tv.ismar.daisy.models.Attribute;
import tv.ismar.daisy.models.Clip;
import tv.ismar.daisy.models.ContentModel;
import tv.ismar.daisy.models.Favorite;
import tv.ismar.daisy.models.History;
import tv.ismar.daisy.models.Item;
import tv.ismar.daisy.persistence.FavoriteManager;
import tv.ismar.daisy.persistence.HistoryManager;
import tv.ismar.daisy.views.AsyncImageView;
import tv.ismar.daisy.views.DetailAttributeContainer;
import tv.ismar.daisy.views.LoadingDialog;
import tv.ismar.daisy.views.AsyncImageView.OnImageViewLoadListener;
import android.app.Activity;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


public class ItemDetailActivity extends Activity implements OnImageViewLoadListener {
	
	private static final String TAG = "ItemDetailActivity";
	
	public static final String action = "tv.ismar.daisy.Item";
	
	private SimpleRestClient mSimpleRestClient;
	
	private ContentModel mContentModel;
	
	private Item mItem;
	
	private Item[] mRelatedItem;
	
	private boolean isDrama = false;
	
	private RelativeLayout mDetailLeftContainer;
	private TextView mDetailTitle;
	private TextView mDetailIntro;
	private AsyncImageView mDetailPreviewImg;
	private Button mBtnLeft;
	private Button mBtnRight;
	private Button mBtnFill;
	private Button mBtnFavorite;
	private LinearLayout mDetailRightContainer;
	private LinearLayout mRelatedVideoContainer;
	private Button mMoreContent;

	private DetailAttributeContainer mDetailAttributeContainer;

	private LoadingDialog mLoadingDialog;

	private boolean isInitialized = false;

	private History mHistory;

	private ImageView mDetailQualityLabel;
	
	private GetItemTask mGetItemTask;
	private GetRelatedTask mGetRelatedTask;
	
	private HashMap<String, Object> mDataCollectionProperties = new HashMap<String, Object>();
	
	private HashMap<AsyncImageView, Boolean> mLoadingImageQueue = new HashMap<AsyncImageView, Boolean>();
	
	private void initViews() {
		mDetailLeftContainer = (RelativeLayout)findViewById(R.id.detail_left_container);
		mDetailAttributeContainer = (DetailAttributeContainer) findViewById(R.id.detail_attribute_container);
		mDetailTitle = (TextView) findViewById(R.id.detail_title);
		mDetailIntro = (TextView) findViewById(R.id.detail_intro);
		mDetailPreviewImg = (AsyncImageView)findViewById(R.id.detail_preview_img);
		mDetailPreviewImg.setOnImageViewLoadListener(this);
		mDetailQualityLabel = (ImageView)findViewById(R.id.detail_quality_label);
		mBtnLeft = (Button) findViewById(R.id.btn_left);
		mBtnRight = (Button) findViewById(R.id.btn_right);
		mBtnFill = (Button) findViewById(R.id.btn_fill);
		mBtnFavorite = (Button) findViewById(R.id.btn_favorite);
		mDetailRightContainer = (LinearLayout) findViewById(R.id.detail_right_container);
		mRelatedVideoContainer = (LinearLayout) findViewById(R.id.related_video_container);
		mMoreContent = (Button) findViewById(R.id.more_content);
		
		mMoreContent.setOnFocusChangeListener(mRelatedOnFocusChangeListener);
		mBtnLeft.setOnFocusChangeListener(mLeftElementFocusChangeListener);
		mBtnRight.setOnFocusChangeListener(mLeftElementFocusChangeListener);
		mBtnFill.setOnFocusChangeListener(mLeftElementFocusChangeListener);
		mBtnFill.setOnFocusChangeListener(mLeftElementFocusChangeListener);
		mBtnFavorite.setOnFocusChangeListener(mLeftElementFocusChangeListener);
		
		mBtnLeft.setOnClickListener(mIdOnClickListener);
		mBtnRight.setOnClickListener(mIdOnClickListener);
		mBtnFill.setOnClickListener(mIdOnClickListener);
		mBtnFavorite.setOnClickListener(mIdOnClickListener);
		mMoreContent.setOnClickListener(mIdOnClickListener);
	}

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.item_detail_layout);
		mSimpleRestClient = new SimpleRestClient();
//		Log.e("START", System.currentTimeMillis()+"");
		mLoadingDialog = new LoadingDialog(this, getResources().getString(R.string.vod_loading));
		mLoadingDialog.setOnCancelListener(mLoadingCancelListener);
		mLoadingDialog.show();
		
		initViews();
		
		Intent intent = getIntent();
		if(intent!=null){
			if(intent.getSerializableExtra("item")!=null){
				mItem = (Item) intent.getSerializableExtra("item");
				if(mItem!=null) {
					try {
						initLayout();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else {
				String url = intent.getStringExtra("url");
				if(url==null){
					url = SimpleRestClient.sRoot_url + "/api/item/96538/";
				}
				mGetItemTask = new GetItemTask();
				mGetItemTask.execute(url);
			}
		}
		DaisyUtils.getVodApplication(this).addActivityToPool(this.toString(), this);
	}
	
	@Override
	protected void onResume() {
		if(isInitialized) {
			if(isFavorite()){
				mBtnFavorite.setText(getResources().getString(R.string.favorited));
			} else {
				mBtnFavorite.setText(getResources().getString(R.string.favorite));
			} 
			if(isDrama) {
				String url = mItem.item_url==null ? mSimpleRestClient.root_url + "/api/item/" + mItem.pk + "/": mItem.item_url;
				mHistory = DaisyUtils.getHistoryManager(this).getHistoryByUrl(url);
			}
		}
		for(HashMap.Entry<AsyncImageView, Boolean> entry: mLoadingImageQueue.entrySet()) {
			if(!entry.getValue()) {
				entry.getKey().setPaused(true);
				entry.setValue(true);
			}
		}
		super.onResume();
	}

	@Override
	protected void onPause() {
		if(mLoadingDialog!=null && mLoadingDialog.isShowing()) {
			mLoadingDialog.dismiss();
		}
		for(HashMap.Entry<AsyncImageView, Boolean> entry: mLoadingImageQueue.entrySet()) {
			if(entry.getValue()) {
				entry.getKey().setPaused(true);
				entry.setValue(false);
			}
		}
		if(mItem!=null) {
			final HashMap<String, Object> properties = new HashMap<String, Object>();
			properties.putAll(mDataCollectionProperties);
			new NetworkUtils.DataCollectionTask().execute(NetworkUtils.VIDEO_DETAIL_OUT, properties);
			mDataCollectionProperties.put("title", mItem.title);
			mDataCollectionProperties.put("item", mItem.pk);
			mDataCollectionProperties.put("to", "return");
			mDataCollectionProperties.remove("subitem");
		}
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		if(mGetItemTask!=null && mGetItemTask.getStatus()!=AsyncTask.Status.FINISHED) {
			mGetItemTask.cancel(true);
		}
		if(mGetRelatedTask!=null && mGetItemTask.getStatus()!=AsyncTask.Status.FINISHED) {
			mGetItemTask.cancel(true);
		}
		
		final HashMap<AsyncImageView, Boolean> loadingImageQueue = new HashMap<AsyncImageView, Boolean>();
		loadingImageQueue.putAll(mLoadingImageQueue);
		for(HashMap.Entry<AsyncImageView, Boolean> entry: loadingImageQueue.entrySet()) {
			entry.getKey().stopLoading();
		}
		loadingImageQueue.clear();
		mLoadingImageQueue = null;
		mLoadingDialog = null;
		mRelatedItem = null;
		DaisyUtils.getVodApplication(this).removeActivtyFromPool(this.toString());
		super.onDestroy();
	}
	
	class GetItemTask extends AsyncTask<String, Void, Void>{
		
		int id = 0;
		String url = null;
		@Override
		protected Void doInBackground(String... params) {
			try {
				url = params[0];
				id = SimpleRestClient.getItemId(url, new boolean[1]);
				mItem = mSimpleRestClient.getItem(url);
			} catch (ItemOfflineException e) {
				HashMap<String, Object> exceptionProperties = new HashMap<String, Object>();
				exceptionProperties.put("code", "nodetail");
				exceptionProperties.put("content", "no detail error : " + e.getUrl());
				exceptionProperties.put("item", id);
				NetworkUtils.LogSender(NetworkUtils.DETAIL_EXCEPT, exceptionProperties);
				e.printStackTrace();
			} catch (JsonSyntaxException e) {
				HashMap<String, Object> exceptionProperties = new HashMap<String, Object>();
				exceptionProperties.put("code", "parsejsonerror");
				exceptionProperties.put("content", e.getMessage() + " : "+url );
				exceptionProperties.put("item", id);
				NetworkUtils.LogSender(NetworkUtils.DETAIL_EXCEPT, exceptionProperties);
				e.printStackTrace();
			} catch (NetworkException e) {
				HashMap<String, Object> exceptionProperties = new HashMap<String, Object>();
				exceptionProperties.put("code", "networkconnerror");
				exceptionProperties.put("content", e.getMessage() + " : " + e.getUrl());
				exceptionProperties.put("item", id);
				NetworkUtils.LogSender(NetworkUtils.DETAIL_EXCEPT, exceptionProperties);
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if(mItem!=null){
				try {
					initLayout();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		
	}
	
	/*
	 * Init layout elements when all data has been fetched.
	 */
	private void initLayout() {
		mDataCollectionProperties.put("item", mItem.pk);
		mDataCollectionProperties.put("title", mItem.title);
		new NetworkUtils.DataCollectionTask().execute(NetworkUtils.VIDEO_DETAIL_IN, mDataCollectionProperties);
		/*
		 * if this item is a drama , the button should split to two. otherwise. use one button.
		 */
		if(mItem.subitems==null || mItem.subitems.length==0) {
			isDrama = false;
			mBtnFill.setVisibility(View.VISIBLE);
			mBtnLeft.setVisibility(View.GONE);
			mBtnRight.setVisibility(View.INVISIBLE);
			mBtnFill.requestFocus();
		} else {
			isDrama = true;
			mBtnFill.setVisibility(View.GONE);
			mBtnLeft.setVisibility(View.VISIBLE);
			mBtnRight.setVisibility(View.VISIBLE);
			mBtnLeft.requestFocus();
		}
		
		if(isDrama) {
			String url = mItem.item_url==null ? SimpleRestClient.sRoot_url + "/api/item/" + mItem.pk + "/": mItem.item_url;
			mHistory = DaisyUtils.getHistoryManager(this).getHistoryByUrl(url);
		}
		
		mDetailTitle.setText(mItem.title);
		
		/*
		 * Build detail attributes list using a given order according to ContentModel's define.
		 * we also need to add some common attributes which are defined in ContentModel.
		 */
		if(mItem.attributes!=null && mItem.attributes.map!=null){
			
			for(ContentModel m:DaisyUtils.getVodApplication(this).mContentModel){
				if(m.content_model.equals(mItem.content_model)){
					mContentModel = m;
				}
			}
			if(mContentModel.attributes.get("genre")==null){
				mContentModel.attributes.put("genre", getResources().getString(R.string.genre));
			}
			if(mContentModel.attributes.get("vendor")==null) {
				mContentModel.attributes.put("vendor", getResources().getString(R.string.vendor));
			}
			if(isDrama){
				if(mContentModel.attributes.get("episodes")==null) {
					mContentModel.attributes.put("episodes", getResources().getString(R.string.episodes));
				}
			}
			if(mContentModel.attributes.get("length")==null) {
				mContentModel.attributes.put("length", getResources().getString(R.string.length));
			}
			//Used to store Attribute name and value from Item.attributes.map
			LinkedHashMap<String, String> attributeMap = new LinkedHashMap<String, String>();
			attributeMap.put("genre", null);
			attributeMap.put("vendor", null);
			attributeMap.put("length", null);
			for(String key:mContentModel.attributes.keySet()){
				attributeMap.put(key, null);
			}
			attributeMap.put("vendor", mItem.vendor);
			if(isDrama){
				attributeMap.put("episodes", getEpisodes(mItem));
			}
			attributeMap.put("length", getClipLength(mItem.clip));//modify by zjq
			Iterator iter = mItem.attributes.map.keySet().iterator();
			while(iter.hasNext()){
				String key = (String) iter.next();
				Object value = mItem.attributes.map.get(key);
				if(value!=null) {
					if(value.getClass().equals(String.class)){
						attributeMap.put(key, (String)value);
					} else if(value.getClass().equals(Attribute.Info.class)){
						attributeMap.put(key, ((Attribute.Info)value).name);
					} else if(value.getClass().equals(Attribute.Info[].class)){
						StringBuffer sb = new StringBuffer();
						for(Attribute.Info info: (Attribute.Info[])value){
							sb.append(info.name);
							sb.append(",");
						}
						attributeMap.put(key, sb.substring(0, sb.length()-1));
					}
				}
			}
			mDetailAttributeContainer.addAttribute(attributeMap, mContentModel);
		}
		//Set the content to Introduction View
		mDetailIntro.setText(mItem.description);
		//Set the favorite button's label.
		if(isFavorite()){
			mBtnFavorite.setText(getResources().getString(R.string.favorited));
		} else {
			mBtnFavorite.setText(getResources().getString(R.string.favorite));
		}
		
		if(mItem.poster_url!=null){
			mDetailPreviewImg.setTag(mItem.poster_url);
			mDetailPreviewImg.setUrl(mItem.poster_url);
		}
		
		mGetRelatedTask = new GetRelatedTask();
		mGetRelatedTask.execute();
		//label_uhd and label_hd has worry name. which label_uhd presents hd.
		switch(mItem.quality) {
		case 3:
			mDetailQualityLabel.setImageResource(R.drawable.label_uhd);
			break;
		case 4:
		case 5:
			mDetailQualityLabel.setImageResource(R.drawable.label_hd);
			break;
		default:
			mDetailQualityLabel.setVisibility(View.GONE);
		}
		isInitialized = true;
	}
	
	private String getClipLength(Clip clip) {
		if(clip!=null){
			if(clip.length>120) {
				return clip.length/60 + getResources().getString(R.string.minute);
			} else {
				return clip.length + getResources().getString(R.string.second);
			}
		} else {
			return null;
		}
	}
	
	private String getEpisodes(Item item) {
		if(item.subitems.length>0) {
			return item.episode+"("+getResources().getString(R.string.update_to_episode).replace("#", ""+item.subitems.length)+")";
		} else {
			return null;
		}
	}

	/*
	 * get the favorite status of the item.
	 */
	private boolean isFavorite() {
		if(mItem!=null) {
			String url = mItem.item_url;
			if(url==null && mItem.pk != 0) {
				url = SimpleRestClient.sRoot_url + "/api/item/" + mItem.pk + "/";
			}
			Favorite favorite = DaisyUtils.getFavoriteManager(this).getFavoriteByUrl(url);
			if(favorite!=null) {
				return true;
			}
		}
		
		return false;
	}
	
	class GetImageTask extends AsyncTask<ImageView, Void, Bitmap> {
		
		private ImageView imageView;

		@Override
		protected Bitmap doInBackground(ImageView... params) {
			String url = (String) params[0].getTag();
			imageView = params[0];
			return ImageUtils.getBitmapFromInputStream(NetworkUtils.getInputStream(url), 476, 267);
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			if(result!=null){
				imageView.setImageBitmap(result);
			} else {
				
			}
		}
		
	}
	
	class GetRelatedTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			try {
				mRelatedItem = mSimpleRestClient.getRelatedItem("/api/tv/relate/"+mItem.pk+"/");
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if(mRelatedItem!=null && mRelatedItem.length>0){
				buildRelatedList();
			}
			if(mLoadingDialog.isShowing()){				
				mLoadingDialog.dismiss();
				mDetailLeftContainer.setVisibility(View.VISIBLE);
				mDetailRightContainer.setVisibility(View.VISIBLE);
			}
		}
		
		
	}
	
	private void buildRelatedList() {
		for(int i=0; i<4&&i<mRelatedItem.length; i++) {
			RelativeLayout relatedHolder = (RelativeLayout) LayoutInflater.from(ItemDetailActivity.this).inflate(R.layout.related_item_layout, null);
			
			int H = DaisyUtils.getVodApplication(ItemDetailActivity.this).getheightPixels(ItemDetailActivity.this);
			LinearLayout.LayoutParams layoutParams;
			if(H==720)
				layoutParams = new LinearLayout.LayoutParams(426, 119);
			else
			    layoutParams = new LinearLayout.LayoutParams(638, 178);
				relatedHolder.setLayoutParams(layoutParams);
			
			TextView titleView = (TextView) relatedHolder.findViewById(R.id.related_title);
			AsyncImageView imgView = (AsyncImageView) relatedHolder.findViewById(R.id.related_preview_img);
			imgView.setOnImageViewLoadListener(this);
			TextView focusView = (TextView) relatedHolder.findViewById(R.id.related_focus);
			ImageView qualityLabel = (ImageView) relatedHolder.findViewById(R.id.related_quality_label);
			if(mRelatedItem[i].quality==3) {
				qualityLabel.setImageResource(R.drawable.label_hd_small);
			} else if(mRelatedItem[i].quality==4 || mRelatedItem[i].quality==5) {
				qualityLabel.setImageResource(R.drawable.label_uhd_small);
			}
			imgView.setTag(mRelatedItem[i].adlet_url);
			imgView.setUrl(mRelatedItem[i].adlet_url);
			titleView.setText(mRelatedItem[i].title);
			focusView.setText(mRelatedItem[i].focus);
			relatedHolder.setTag(mRelatedItem[i].item_url);
			mRelatedVideoContainer.addView(relatedHolder);
			relatedHolder.setOnFocusChangeListener(mRelatedOnFocusChangeListener);
			relatedHolder.setOnClickListener(mRelatedClickListener);
		}
	}
	
	private OnFocusChangeListener mRelatedOnFocusChangeListener = new OnFocusChangeListener() {
		
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if(v.getParent()==mRelatedVideoContainer){
				if(hasFocus) {
					TextView title = (TextView) v.findViewById(R.id.related_title);
					title.setTextColor(0xFFF8F8FF);
					TextView focus = (TextView) v.findViewById(R.id.related_focus);
					focus.setTextColor(0xFFF8F8FF);
					title.setSelected(true);
					focus.setSelected(true);
				} else {
					TextView title = (TextView) v.findViewById(R.id.related_title);
					title.setTextColor(0xFFF8F8FF);
					TextView focus = (TextView) v.findViewById(R.id.related_focus);
					focus.setTextColor(0xFFF8F8FF);
					title.setSelected(false);
					focus.setSelected(false);
				}
			}
			if(hasFocus) {
				mDetailRightContainer.setBackgroundResource(android.R.color.transparent);
				mDetailLeftContainer.setBackgroundResource(R.drawable.left_bg_unfocused);
			}
		}
	};
	
	private OnFocusChangeListener mLeftElementFocusChangeListener = new OnFocusChangeListener() {
		
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if(hasFocus) {
				mDetailRightContainer.setBackgroundResource(R.drawable.right_bg_normal);
				mDetailLeftContainer.setBackgroundResource(android.R.color.transparent);
				((Button)v).setTextColor(0xFFF8F8FF);
			} else {
				((Button)v).setTextColor(0xffbbbbbb);
			}
		}
	};
	
	private OnClickListener mRelatedClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			String url = (String) v.getTag();
			final Item[] relatedItem = mRelatedItem;
			for(Item item: relatedItem) {
				if(url.equals(item.item_url)) {
					HashMap<String, Object> properties = new HashMap<String, Object>();
					properties.put("item", mItem.pk);
					properties.put("to_item", item.pk);
					properties.put("to_title", item.title);
					properties.put("to", "relate");
					new NetworkUtils.DataCollectionTask().execute(NetworkUtils.VIDEO_RELATE, properties);
					break;
				}
			}
			Intent intent = new Intent();
			intent.setAction(action);
			intent.putExtra("url", url);
			startActivity(intent);
		}
	};
	
	private OnClickListener mIdOnClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			try {
				int id = v.getId();
				Intent intent = new Intent();
				switch(id){
				case R.id.btn_left:
					String subUrl = null;
					int sub_id = 0;
					String title = mItem.title;
					if(mHistory!=null && mHistory.is_continue) {
						subUrl = mHistory.sub_url;
						for(Item item: mItem.subitems) {
							if(item.url.equals(subUrl)) {
								sub_id = item.pk;
								title += "("+item.episode+")";
								break;
							}
						}
					} else {
						subUrl = mItem.subitems[0].url;
						sub_id = mItem.subitems[0].pk;
						title += "("+mItem.subitems[0].episode+")";
					}
					mDataCollectionProperties.put("title", title);
					mDataCollectionProperties.put("subitem", sub_id);
					mDataCollectionProperties.put("to", "play");
					
					intent.setAction("tv.ismar.daisy.Play");
					intent.putExtra("url", subUrl);
					startActivity(intent);
					break;
				case R.id.btn_right:
					mDataCollectionProperties.put("to", "list");
					intent.setClass(ItemDetailActivity.this, DramaListActivity.class);
					intent.putExtra("item", mItem);
					startActivity(intent);
					break;
				case R.id.btn_fill:
					mDataCollectionProperties.put("to", "play");
					
					intent.setAction("tv.ismar.daisy.Play");
					intent.putExtra("item", mItem);
					startActivity(intent);
					break;
				case R.id.btn_favorite:
					if(isFavorite()) {
						String url = SimpleRestClient.sRoot_url + "/api/item/"+mItem.pk+"/";
						DaisyUtils.getFavoriteManager(ItemDetailActivity.this).deleteFavoriteByUrl(url);
						showToast(getResources().getString(R.string.vod_bookmark_remove_success));
					} else {
						String url = SimpleRestClient.sRoot_url + "/api/item/"+mItem.pk+"/";
						Favorite favorite = new Favorite();
						favorite.title = mItem.title;
						favorite.adlet_url = mItem.adlet_url;
						favorite.content_model = mItem.content_model;
						favorite.url = url;
						favorite.quality = mItem.quality;
						favorite.is_complex = mItem.is_complex;
						DaisyUtils.getFavoriteManager(ItemDetailActivity.this).addFavorite(favorite);
//					mFavoriteManager.addFavorite(mItem.title, url, mItem.content_model);
						showToast(getResources().getString(R.string.vod_bookmark_add_success));
					}
					if(isFavorite()) {
						mBtnFavorite.setText(getResources().getString(R.string.favorited));
					} else {
						mBtnFavorite.setText(getResources().getString(R.string.favorite));
					}
					break;
				case R.id.more_content:
					if(mRelatedItem!=null && mRelatedItem.length >0 ){
						intent.putExtra("related_item", new ArrayList<Item>(Arrays.asList(mRelatedItem)));
					}
					mDataCollectionProperties.put("to", "relate");
					
					intent.putExtra("item", mItem);
					intent.setClass(ItemDetailActivity.this, RelatedActivity.class);
					startActivity(intent);
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};
	
	private OnCancelListener mLoadingCancelListener = new OnCancelListener() {
		
		@Override
		public void onCancel(DialogInterface dialog) {
			ItemDetailActivity.this.finish();
			dialog.dismiss();
		}
	};

	@Override
	public void onLoadingStarted(AsyncImageView imageView) {
		if(mLoadingImageQueue!=null) {
			mLoadingImageQueue.put(imageView, true);
		}
	}


	@Override
	public void onLoadingEnded(AsyncImageView imageView, Bitmap image) {
		if(mLoadingImageQueue != null) {
			mLoadingImageQueue.remove(imageView);
		}
	}


	@Override
	public void onLoadingFailed(AsyncImageView imageView, Throwable throwable) {
		if(mLoadingImageQueue != null) {
			mLoadingImageQueue.remove(imageView);
		}
	}
	
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
