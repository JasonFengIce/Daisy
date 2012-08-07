package tv.ismar.daisy;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import tv.ismar.daisy.core.ImageUtils;
import tv.ismar.daisy.core.NetworkUtils;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.models.Attribute;
import tv.ismar.daisy.models.Clip;
import tv.ismar.daisy.models.ContentModel;
import tv.ismar.daisy.models.Item;
import tv.ismar.daisy.views.LoadingDialog;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class ItemDetailActivity extends Activity {
	
	public static final String action = "tv.ismar.daisy.Item";
	
	private SimpleRestClient mSimpleRestClient;
	
	private VodApplication mApplication;
	
	private ContentModel mContentModel;
	
	private Item mItem;
	
	private Item[] mRelatedItem;
	
	private boolean isDrama = false;
	
	private RelativeLayout mDetailLeftContainer;
	private TextView mDetailTitle;
	private TextView mDetailIntro;
	private ImageView mDetailPreviewImg;
	private Button mBtnLeft;
	private Button mBtnRight;
	private Button mBtnFill;
	private Button mBtnFavorite;
	private LinearLayout mDetailRightContainer;
	private LinearLayout mRelatedVideoContainer;
	private Button mMoreContent;

	private LinearLayout mDetailAttributeContainer;

	private LoadingDialog mLoadingDialog;
	
	private void initViews() {
		mDetailLeftContainer = (RelativeLayout)findViewById(R.id.detail_left_container);
		mDetailAttributeContainer = (LinearLayout) findViewById(R.id.detail_attribute_container);
		mDetailTitle = (TextView) findViewById(R.id.detail_title);
		mDetailIntro = (TextView) findViewById(R.id.detail_intro);
		mDetailPreviewImg = (ImageView)findViewById(R.id.detail_preview_img);
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
		mApplication = (VodApplication) getApplication();
//		Log.e("START", System.currentTimeMillis()+"");
		mLoadingDialog = new LoadingDialog(this);
		
		initViews();
		
		Intent intent = getIntent();
		if(intent!=null){
			if(intent.getSerializableExtra("item")!=null){
				mItem = (Item) intent.getSerializableExtra("item");
				if(mItem!=null) {
					initLayout();
				}
			} else {
				String url = intent.getStringExtra("url");
				if(url==null){
					url = "http://cord.tvxio.com/api/item/96538/";
				}
				new GetItemTask().execute(url);
			}
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mLoadingDialog.show();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
	class GetItemTask extends AsyncTask<String, Void, Void>{

		@Override
		protected Void doInBackground(String... params) {
			mItem = mSimpleRestClient.getItem(params[0]);
			if(mItem.subitems!=null && mItem.subitems.length>0 && mItem.subitems[0].item_pk==0){
				mItem.subitems[0] = mSimpleRestClient.getItem(mItem.subitems[0].url);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if(mItem!=null){
				initLayout();
			}
		}
		
		
	}
	
	/*
	 * Init layout elements when all data has been fetched.
	 */
	private void initLayout() {
		/*
		 * if this item is a drama , the button should split to two. otherwise. use one button.
		 */
		if(mItem.subitems==null || mItem.subitems.length==0) {
			isDrama = false;
			mBtnFill.setVisibility(View.VISIBLE);
			mBtnLeft.setVisibility(View.GONE);
			mBtnRight.setVisibility(View.GONE);
		} else {
			isDrama = true;
			mBtnFill.setVisibility(View.GONE);
			mBtnLeft.setVisibility(View.VISIBLE);
			mBtnRight.setVisibility(View.VISIBLE);
		}
		
		mDetailTitle.setText(mItem.title);
		
		/*
		 * Build detail attributes list using a given order according to ContentModel's define.
		 * we also need to add some common attributes which are defined in ContentModel.
		 */
		if(mItem.attributes!=null && mItem.attributes.map!=null){
			
			for(ContentModel m:mApplication.mContentModel){
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
			attributeMap.put("length", getClipLength(mItem.clip));
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
			buildAttributeList(attributeMap);
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
			new GetImageTask().execute(mDetailPreviewImg);
		}
		
		new GetRelatedTask().execute();
		if(mLoadingDialog.isShowing()){
			mLoadingDialog.dismiss();
		}
	}
	
	private String getClipLength(Clip clip) {
		if(clip!=null){
			return clip.length/60 + getResources().getString(R.string.minute);
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
	
	private void buildAttributeList(LinkedHashMap<String, String> attrMap){
		for(Map.Entry<String, String> entry: attrMap.entrySet()){
			if(entry.getValue()==null || mContentModel.attributes.get(entry.getKey())==null){
				continue;
			}
			LinearLayout infoLine = new LinearLayout(ItemDetailActivity.this);
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(471,LinearLayout.LayoutParams.WRAP_CONTENT);
			layoutParams.topMargin = 20;
			
			infoLine.setLayoutParams(layoutParams);
			infoLine.setOrientation(LinearLayout.HORIZONTAL);
			TextView itemName = new TextView(ItemDetailActivity.this);
			itemName.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
			itemName.setTextColor(0xff999999);
			itemName.setTextSize(30f);
			itemName.setText(mContentModel.attributes.get(entry.getKey())+":");
			infoLine.addView(itemName);
			TextView itemValue = new TextView(ItemDetailActivity.this);
			itemValue.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
			itemValue.setTextColor(0xffbbbbbb);
			itemValue.setTextSize(30f);
			itemValue.setText(entry.getValue());
			infoLine.addView(itemValue);
			
			mDetailAttributeContainer.addView(infoLine);
		}
		
		
	}
	
	/*
	 * get the favorite status of the item.
	 */
	private boolean isFavorite() {
		//TODO: Need to be completed.
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
			mRelatedItem = mSimpleRestClient.getRelatedItem("/api/tv/relate/"+mItem.pk+"/");
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if(mRelatedItem!=null && mRelatedItem.length>0){
				buildRelatedList();
			}
		}
		
		
	}
	
	private void buildRelatedList() {
		for(int i=0; i<4&&i<mRelatedItem.length; i++) {
			RelativeLayout relatedHolder = (RelativeLayout) LayoutInflater.from(ItemDetailActivity.this).inflate(R.layout.related_item_layout, null);
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(638, 178);
			layoutParams.leftMargin = 1;
			relatedHolder.setLayoutParams(layoutParams);
			TextView titleView = (TextView) relatedHolder.findViewById(R.id.related_title);
			ImageView imgView = (ImageView) relatedHolder.findViewById(R.id.related_preview_img);
			TextView focusView = (TextView) relatedHolder.findViewById(R.id.related_focus);
			imgView.setTag(mRelatedItem[i].adlet_url);
			new GetImageTask().execute(imgView);
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
					title.setTextColor(0xff000000);
					TextView focus = (TextView) v.findViewById(R.id.related_focus);
					focus.setTextColor(0xff000000);
				} else {
					TextView title = (TextView) v.findViewById(R.id.related_title);
					title.setTextColor(0xffBBBBBB);
					TextView focus = (TextView) v.findViewById(R.id.related_focus);
					focus.setTextColor(0xff999999);
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
				((Button)v).setTextColor(0xff000000);
			} else {
				((Button)v).setTextColor(0xffbbbbbb);
			}
		}
	};
	
	private OnClickListener mRelatedClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			String url = (String) v.getTag();
			Intent intent = new Intent();
			intent.setAction(action);
			intent.putExtra("url", url);
			startActivity(intent);
		}
	};
	
	private OnClickListener mIdOnClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			int id = v.getId();
			Intent intent = new Intent();
			switch(id){
			case R.id.btn_left:
				intent.setAction("tv.ismar.daisy.Play");
				intent.putExtra("item", mItem.subitems[0]);
				startActivity(intent);
				break;
			case R.id.btn_right:
				intent.setClass(ItemDetailActivity.this, DramaListActivity.class);
				intent.putExtra("item", mItem);
				startActivity(intent);
				break;
			case R.id.btn_fill:
				intent.setAction("tv.ismar.daisy.Play");
				intent.putExtra("item", mItem);
				startActivity(intent);
				break;
			case R.id.btn_favorite:
				break;
			case R.id.more_content:
				break;
			}
		}
	};
	
}
