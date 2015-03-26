package tv.ismar.daisy;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.EventProperty;
import tv.ismar.daisy.core.ImageUtils;
import tv.ismar.daisy.core.NetworkUtils;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.SimpleRestClient.HttpPostRequestInterface;
import tv.ismar.daisy.exception.ItemOfflineException;
import tv.ismar.daisy.exception.NetworkException;
import tv.ismar.daisy.models.Attribute;
import tv.ismar.daisy.models.Clip;
import tv.ismar.daisy.models.ContentModel;
import tv.ismar.daisy.models.Favorite;
import tv.ismar.daisy.models.History;
import tv.ismar.daisy.models.Item;
import tv.ismar.daisy.player.InitPlayerTool;
import tv.ismar.daisy.player.InitPlayerTool.onAsyncTaskHandler;
import tv.ismar.daisy.utils.Util;
import tv.ismar.daisy.views.AsyncImageView;
import tv.ismar.daisy.views.AsyncImageView.OnImageViewLoadListener;
import tv.ismar.daisy.views.DetailAttributeContainer;
import tv.ismar.daisy.views.LoadingDialog;
import tv.ismar.daisy.views.PaymentDialog;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
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
import com.google.gson.JsonSyntaxException;

public class ItemDetailActivity extends Activity implements
		OnImageViewLoadListener {

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
	// private Button mBtnLeft;
	// private Button mBtnRight;
	// private Button mBtnFill;
	// private Button mBtnFavorite;
	// private Button mBtnLeftBuy;
	// private Button mBtnFillBuy;
	private LinearLayout mDetailRightContainer;
	private LinearLayout mRelatedVideoContainer;
	private Button mMoreContent;
	private TextView detail_price_txt;
	private TextView detail_duration_txt;
	private DetailAttributeContainer mDetailAttributeContainer;

	private LoadingDialog mLoadingDialog;

	private boolean isInitialized = false;

	private History mHistory;

	private ImageView mDetailQualityLabel;

	private GetItemTask mGetItemTask;
	private GetRelatedTask mGetRelatedTask;

	private HashMap<String, Object> mDataCollectionProperties = new HashMap<String, Object>();

	private HashMap<AsyncImageView, Boolean> mLoadingImageQueue = new HashMap<AsyncImageView, Boolean>();

	private String mSection = "";

	private Button mLeftBtn;
	private Button mMiddleBtn;
	private Button mRightBtn;
	private final String COLLECT_VIDEO = "collect";
	private final String BUY_VIDEO = "buy";
	private final String PREVIEW_VIDEO = "preview";
	private final String PLAY_VIDEO = "play";
	private final String DRAMA_VIDEO = "drama";
	private boolean isBuy = false;
	private int remainDay = -1;
	private String identify = "";
	private Button mCollectBtn;
	private ImageView isbuy_label;
	private void initViews() {
		isbuy_label = (ImageView)findViewById(R.id.isbuy_label);
		mDetailLeftContainer = (RelativeLayout) findViewById(R.id.detail_left_container);
		mDetailAttributeContainer = (DetailAttributeContainer) findViewById(R.id.detail_attribute_container);
		mDetailTitle = (TextView) findViewById(R.id.detail_title);
		mDetailIntro = (TextView) findViewById(R.id.detail_intro);
		mDetailPreviewImg = (AsyncImageView) findViewById(R.id.detail_preview_img);
		mDetailPreviewImg.setOnImageViewLoadListener(this);
		mDetailQualityLabel = (ImageView) findViewById(R.id.detail_quality_label);
		mLeftBtn = (Button) findViewById(R.id.btn_left);
		mMiddleBtn = (Button) findViewById(R.id.middle_btn);
		mRightBtn = (Button) findViewById(R.id.btn_right);
		// mBtnFill = (Button) findViewById(R.id.btn_fill);
		// mBtnFavorite = (Button) findViewById(R.id.btn_favorite);
		// mBtnFillBuy = (Button)findViewById(R.id.btn_fill_buy);
		mDetailRightContainer = (LinearLayout) findViewById(R.id.detail_right_container);
		mRelatedVideoContainer = (LinearLayout) findViewById(R.id.related_video_container);
		mMoreContent = (Button) findViewById(R.id.more_content);
		detail_price_txt = (TextView) findViewById(R.id.detail_price_txt);
		detail_duration_txt = (TextView) findViewById(R.id.detail_duration_txt);
		mMoreContent.setOnFocusChangeListener(mRelatedOnFocusChangeListener);
		// mBtnLeft.setOnFocusChangeListener(mLeftElementFocusChangeListener);
		// mBtnRight.setOnFocusChangeListener(mLeftElementFocusChangeListener);
		// mBtnFill.setOnFocusChangeListener(mLeftElementFocusChangeListener);
		// mBtnFill.setOnFocusChangeListener(mLeftElementFocusChangeListener);
		// mBtnFavorite.setOnFocusChangeListener(mLeftElementFocusChangeListener);
		//
		mLeftBtn.setOnClickListener(mIdOnClickListener);
		mMiddleBtn.setOnClickListener(mIdOnClickListener);
		mRightBtn.setOnClickListener(mIdOnClickListener);
		mMoreContent.setOnClickListener(mIdOnClickListener);
		// mBtnFavorite.setOnClickListener(mIdOnClickListener);
		// mBtnFillBuy.setOnClickListener(mIdOnClickListener);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.item_detail_layout);
		mSimpleRestClient = new SimpleRestClient();
		// Log.e("START", System.currentTimeMillis()+"");
		mLoadingDialog = new LoadingDialog(this, getResources().getString(
				R.string.vod_loading));
		mLoadingDialog.setOnCancelListener(mLoadingCancelListener);
		mLoadingDialog.show();

		initViews();
		Intent intent = getIntent();
		if (intent != null) {
			if (intent.getSerializableExtra("item") != null) {
				mItem = (Item) intent.getSerializableExtra("item");
				if (mItem != null) {
					try {
						// initLayout();
						if (!isFree())
							isbuy();
						else
							initLayout();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else {
				String url = intent.getStringExtra("url");
				mSection = intent.getStringExtra(EventProperty.SECTION);
				if (url == null) {
					url = SimpleRestClient.sRoot_url + "/api/item/96538/";
				}
				mGetItemTask = new GetItemTask();
				mGetItemTask.execute(url);
			}
		}

		DaisyUtils.getVodApplication(this).addActivityToPool(this.toString(),
				this);
	}

	@Override
	protected void onResume() {
		if (isInitialized) {
			if (isDrama()) {
				String url = mItem.item_url == null ? mSimpleRestClient.root_url
						+ "/api/item/" + mItem.pk + "/"
						: mItem.item_url;
				mHistory = DaisyUtils.getHistoryManager(this).getHistoryByUrl(
						url);
			}
		}
		for (HashMap.Entry<AsyncImageView, Boolean> entry : mLoadingImageQueue
				.entrySet()) {
			if (!entry.getValue()) {
				entry.getKey().setPaused(true);
				entry.setValue(true);
			}
		}
		super.onResume();
	}

	@Override
	protected void onPause() {
		if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
			mLoadingDialog.dismiss();
		}
		for (HashMap.Entry<AsyncImageView, Boolean> entry : mLoadingImageQueue
				.entrySet()) {
			if (entry.getValue()) {
				entry.getKey().setPaused(true);
				entry.setValue(false);
			}
		}
		if (mItem != null) {
			final HashMap<String, Object> properties = new HashMap<String, Object>();
			properties.putAll(mDataCollectionProperties);
			new NetworkUtils.DataCollectionTask().execute(
					NetworkUtils.VIDEO_DETAIL_OUT, properties);
			mDataCollectionProperties.put(EventProperty.TITLE, mItem.title);
			mDataCollectionProperties.put(EventProperty.ITEM, mItem.pk);
			mDataCollectionProperties.put(EventProperty.TO, "return");
			mDataCollectionProperties.remove(EventProperty.SUBITEM);
		}
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		if (mGetItemTask != null
				&& mGetItemTask.getStatus() != AsyncTask.Status.FINISHED) {
			mGetItemTask.cancel(true);
		}
		if (mGetRelatedTask != null
				&& mGetItemTask.getStatus() != AsyncTask.Status.FINISHED) {
			mGetItemTask.cancel(true);
		}

		final HashMap<AsyncImageView, Boolean> loadingImageQueue = new HashMap<AsyncImageView, Boolean>();
		loadingImageQueue.putAll(mLoadingImageQueue);
		for (HashMap.Entry<AsyncImageView, Boolean> entry : loadingImageQueue
				.entrySet()) {
			entry.getKey().stopLoading();
		}
		loadingImageQueue.clear();
		mLoadingImageQueue = null;
		mLoadingDialog = null;
		mRelatedItem = null;
		DaisyUtils.getVodApplication(this).removeActivtyFromPool(
				this.toString());
		super.onDestroy();
	}

	class GetItemTask extends AsyncTask<String, Void, Void> {

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
				exceptionProperties.put(EventProperty.CODE, "nodetail");
				exceptionProperties.put(EventProperty.CONTENT,
						"no detail error : " + e.getUrl());
				exceptionProperties.put(EventProperty.ITEM, id);
				NetworkUtils.SaveLogToLocal(NetworkUtils.DETAIL_EXCEPT,
						exceptionProperties);
				e.printStackTrace();
			} catch (JsonSyntaxException e) {
				HashMap<String, Object> exceptionProperties = new HashMap<String, Object>();
				exceptionProperties.put(EventProperty.CODE, "parsejsonerror");
				exceptionProperties.put(EventProperty.CONTENT, e.getMessage()
						+ " : " + url);
				exceptionProperties.put(EventProperty.ITEM, id);
				NetworkUtils.SaveLogToLocal(NetworkUtils.DETAIL_EXCEPT,
						exceptionProperties);
				e.printStackTrace();
			} catch (NetworkException e) {
				HashMap<String, Object> exceptionProperties = new HashMap<String, Object>();
				exceptionProperties.put(EventProperty.CODE, "networkconnerror");
				exceptionProperties.put(EventProperty.CONTENT, e.getMessage()
						+ " : " + e.getUrl());
				exceptionProperties.put(EventProperty.ITEM, id);
				NetworkUtils.SaveLogToLocal(NetworkUtils.DETAIL_EXCEPT,
						exceptionProperties);
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (mItem != null) {
				try {
					if (!isFree())
						isbuy();
					else
						initLayout();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}

	private boolean isFree() {
		if (mItem.expense != null) {
			return false;
		}
		return true;
	}

	private void isbuy() {
		SimpleRestClient simpleRestClient = new SimpleRestClient();
		simpleRestClient.doSendRequest("/api/order/check/", "post",
				"device_token=" + SimpleRestClient.device_token
						+ "&access_token=" + SimpleRestClient.access_token
						+ "&item=" + mItem.pk, new HttpPostRequestInterface() {
					// subitem=214277
					@Override
					public void onSuccess(String info) {
						// TODO Auto-generated method stub
						if ("0".equals(info)) {
							isBuy = false;
						} else {
							JSONArray jsonArray;
							try {
								jsonArray = new JSONArray(info);
								JSONObject json = jsonArray.getJSONObject(0);
								if (json.has("max_expiry_date")) {
									// 电视剧部分购买
									isBuy = false;// 暂时无法处理
								} else {
									// 电影或者电视剧整部购买
									try {
										remainDay = Util.daysBetween(
												Util.getTime(), info);
										if (remainDay == 0) {
											isBuy = false;// 过期了。认为没购买
											remainDay = -1;
										} else
											isBuy = true;// 购买了，剩余天数大于0
									} catch (ParseException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								info = info.substring(1, info.length() - 1);
								try {
									remainDay = Util.daysBetween(
											Util.getTime(), info);
									if (remainDay == 0) {
										isBuy = false;// 过期了。认为没购买
										remainDay = -1;
									} else
										isBuy = true;// 购买了，剩余天数大于0
								} catch (ParseException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
								e.printStackTrace();
							}
						}
						initLayout();
					}

					@Override
					public void onPrepare() {
						// TODO Auto-generated method stub
					}

					@Override
					public void onFailed(String error) {
						// TODO Auto-generated method stub
						isBuy = false;
						initLayout();
					}
				});
	}

	private boolean isDrama() {
		if (mItem.subitems == null || mItem.subitems.length == 0) {
			isDrama = false;
		} else {
			isDrama = true;
		}
		return isDrama;
	}

	/*
	 * Init layout elements when all data has been fetched.
	 */
	private void initLayout() {
		mDataCollectionProperties.put(EventProperty.ITEM, mItem.pk);
		mDataCollectionProperties.put(EventProperty.TITLE, mItem.title);
		String subItemUrl = SimpleRestClient.root_url + "/api/subitem/"
				+ mItem.pk + "/";
		SimpleRestClient simpleRestClient = new SimpleRestClient();
		Item subItem;
		try {
			subItem = simpleRestClient.getItem(subItemUrl);
			if (subItem != null) {
				mDataCollectionProperties
						.put(EventProperty.SUBITEM, subItem.pk);
			}
		} catch (JsonSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ItemOfflineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NetworkException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		new NetworkUtils.DataCollectionTask().execute(
				NetworkUtils.VIDEO_DETAIL_IN, mDataCollectionProperties);
		/*
		 * if this item is a drama , the button should split to two. otherwise.
		 * use one button.
		 */
		setExpenseStatus();
		if (isDrama()) {
			String url = mItem.item_url == null ? SimpleRestClient.sRoot_url
					+ "/api/item/" + mItem.pk + "/" : mItem.item_url;
			mHistory = DaisyUtils.getHistoryManager(this).getHistoryByUrl(url);
		}

		mDetailTitle.setText(mItem.title);
		/*
		 * Build detail attributes list using a given order according to
		 * ContentModel's define. we also need to add some common attributes
		 * which are defined in ContentModel.
		 */
		if (mItem.attributes != null && mItem.attributes.map != null) {

			for (ContentModel m : DaisyUtils.getVodApplication(this).mContentModel) {
				if (m.content_model.equals(mItem.content_model)) {
					mContentModel = m;
				}
			}
			if (mContentModel.attributes.get("genre") == null) {
				mContentModel.attributes.put("genre",
						getResources().getString(R.string.genre));
			}
			if (mContentModel.attributes.get("vendor") == null) {
				mContentModel.attributes.put("vendor", getResources()
						.getString(R.string.vendor));
			}
			if (isDrama) {
				if (mContentModel.attributes.get("episodes") == null) {
					mContentModel.attributes.put("episodes", getResources()
							.getString(R.string.episodes));
				}
			}
			if (mContentModel.attributes.get("length") == null) {
				mContentModel.attributes.put("length", getResources()
						.getString(R.string.length));
			}
			// Used to store Attribute name and value from Item.attributes.map
			LinkedHashMap<String, String> attributeMap = new LinkedHashMap<String, String>();
			attributeMap.put("genre", null);
			attributeMap.put("vendor", null);
			attributeMap.put("length", null);
			for (String key : mContentModel.attributes.keySet()) {
				attributeMap.put(key, null);
			}
			attributeMap.put("vendor", mItem.vendor);
			if (isDrama()) {
				attributeMap.put("episodes", getEpisodes(mItem));
			}
			attributeMap.put("length", getClipLength(mItem.clip));// modify by
																	// zjq
			Iterator iter = mItem.attributes.map.keySet().iterator();
			while (iter.hasNext()) {
				String key = (String) iter.next();
				Object value = mItem.attributes.map.get(key);
				if (value != null) {
					if (value.getClass().equals(String.class)) {
						attributeMap.put(key, (String) value);
					} else if (value.getClass().equals(Attribute.Info.class)) {
						attributeMap.put(key, ((Attribute.Info) value).name);
					} else if (value.getClass().equals(Attribute.Info[].class)) {
						StringBuffer sb = new StringBuffer();
						for (Attribute.Info info : (Attribute.Info[]) value) {
							sb.append(info.name);
							sb.append(",");
						}
						attributeMap.put(key, sb.substring(0, sb.length() - 1));
					}
				}
			}
			mDetailAttributeContainer.addAttribute(attributeMap, mContentModel);
		}
		// Set the content to Introduction View
		mDetailIntro.setText(mItem.description);
		// Set the favorite button's label.
		if (isFavorite()) {
			mCollectBtn.setBackgroundResource(R.drawable.collected_btn_bg_selector);
		} else {
			mCollectBtn.setBackgroundResource(R.drawable.collect_btn_bg_selector);
		}

		if (mItem.poster_url != null) {
			mDetailPreviewImg.setTag(mItem.poster_url);
			mDetailPreviewImg.setUrl(mItem.poster_url);
		}

		mGetRelatedTask = new GetRelatedTask();
		mGetRelatedTask.execute();
		// label_uhd and label_hd has worry name. which label_uhd presents hd.
		switch (mItem.quality) {
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
		if (clip != null) {
			if (clip.length > 120) {
				return clip.length / 60
						+ getResources().getString(R.string.minute);
			} else {
				return clip.length + getResources().getString(R.string.second);
			}
		} else {
			return null;
		}
	}

	private String getEpisodes(Item item) {
		if (item.subitems.length > 0) {
			String update_to_episode = getResources().getString(
					R.string.update_to_episode);
			return item.episode + "("
					+ String.format(update_to_episode, item.subitems.length)
					+ ")";
		} else {
			return null;
		}
	}

	/*
	 * get the favorite status of the item.
	 */
	private boolean isFavorite() {
		if (mItem != null) {
			String url = mItem.item_url;
			if (url == null && mItem.pk != 0) {
				url = SimpleRestClient.sRoot_url + "/api/item/" + mItem.pk
						+ "/";
			}
			Favorite favorite = null;
			if(!SimpleRestClient.isLogin()){
				 favorite = DaisyUtils.getFavoriteManager(this)
						.getFavoriteByUrl(url,"no");
			}
			else{
				 favorite = DaisyUtils.getFavoriteManager(this)
							.getFavoriteByUrl(url,"yes");
			}
			if (favorite != null) {
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
			return ImageUtils.getBitmapFromInputStream(
					NetworkUtils.getInputStream(url), 476, 267);
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			if (result != null) {
				imageView.setImageBitmap(result);
			} else {

			}
		}

	}

	class GetRelatedTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			try {
				mRelatedItem = mSimpleRestClient
						.getRelatedItem("/api/tv/relate/" + mItem.pk + "/");
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (mRelatedItem != null && mRelatedItem.length > 0) {
				buildRelatedList();
			}
			if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
				mLoadingDialog.dismiss();
				mDetailLeftContainer.setVisibility(View.VISIBLE);
				mDetailRightContainer.setVisibility(View.VISIBLE);
			}
		}

	}

	private void buildRelatedList() {
		for (int i = 0; i < 4 && i < mRelatedItem.length; i++) {
			RelativeLayout relatedHolder = (RelativeLayout) LayoutInflater
					.from(ItemDetailActivity.this).inflate(
							R.layout.related_item_layout, null);

			LinearLayout.LayoutParams layoutParams;
			layoutParams = new LinearLayout.LayoutParams(getResources()
					.getDimensionPixelSize(R.dimen.item_detail_related_W),
					getResources().getDimensionPixelSize(
							R.dimen.item_detail_related_H));
			relatedHolder.setLayoutParams(layoutParams);
			TextView titleView = (TextView) relatedHolder
					.findViewById(R.id.related_title);
			AsyncImageView imgView = (AsyncImageView) relatedHolder
					.findViewById(R.id.related_preview_img);
			imgView.setOnImageViewLoadListener(this);
			TextView focusView = (TextView) relatedHolder
					.findViewById(R.id.related_focus);
			ImageView qualityLabel = (ImageView) relatedHolder
					.findViewById(R.id.related_quality_label);
			TextView related_price_txt = (TextView) relatedHolder
					.findViewById(R.id.related_price_txt);
			TextView ItemBeanScore = (TextView) relatedHolder
					.findViewById(R.id.ItemBeanScore);
			if (mRelatedItem[i].bean_score > 0) {
				ItemBeanScore.setText(mRelatedItem[i].bean_score + "");
				ItemBeanScore.setVisibility(View.VISIBLE);
			}
			if (mRelatedItem[i].expense != null) {
				related_price_txt.setVisibility(View.VISIBLE);
				related_price_txt.setText("￥" + mRelatedItem[i].expense.price);
			}
			// if (mRelatedItem[i].quality == 3) {
			// qualityLabel.setImageResource(R.drawable.label_hd_small);
			// } else if (mRelatedItem[i].quality == 4
			// || mRelatedItem[i].quality == 5) {
			// qualityLabel.setImageResource(R.drawable.label_uhd_small);
			// }
			imgView.setTag(mRelatedItem[i].adlet_url);
			imgView.setUrl(mRelatedItem[i].adlet_url);
			titleView.setText(mRelatedItem[i].title);
			focusView.setText(mRelatedItem[i].focus);
			relatedHolder.setTag(mRelatedItem[i].item_url);
			mRelatedVideoContainer.addView(relatedHolder);
			relatedHolder
					.setOnFocusChangeListener(mRelatedOnFocusChangeListener);
			relatedHolder.setOnClickListener(mRelatedClickListener);
		}
	}

	private OnFocusChangeListener mRelatedOnFocusChangeListener = new OnFocusChangeListener() {

		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (v.getParent() == mRelatedVideoContainer) {
				if (hasFocus) {
					TextView title = (TextView) v
							.findViewById(R.id.related_title);
					title.setTextColor(0xFFF8F8FF);
					TextView focus = (TextView) v
							.findViewById(R.id.related_focus);
					focus.setTextColor(0xFFF8F8FF);
					title.setSelected(true);
					focus.setSelected(true);
				} else {
					TextView title = (TextView) v
							.findViewById(R.id.related_title);
					title.setTextColor(0xFFF8F8FF);
					TextView focus = (TextView) v
							.findViewById(R.id.related_focus);
					focus.setTextColor(0xFFF8F8FF);
					title.setSelected(false);
					focus.setSelected(false);
				}
			}
			if (hasFocus) {
				mDetailRightContainer
						.setBackgroundResource(android.R.color.transparent);
				mDetailLeftContainer
						.setBackgroundResource(R.drawable.left_bg_unfocused);
			}
		}
	};

	private OnFocusChangeListener mLeftElementFocusChangeListener = new OnFocusChangeListener() {

		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (hasFocus) {
				mDetailRightContainer
						.setBackgroundResource(R.drawable.right_bg_normal);
				mDetailLeftContainer
						.setBackgroundResource(android.R.color.transparent);
				((Button) v).setTextColor(0xFFF8F8FF);
			} else {
				((Button) v).setTextColor(0xffbbbbbb);
			}
		}
	};

	private OnClickListener mRelatedClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			String url = (String) v.getTag();
			final Item[] relatedItem = mRelatedItem;
			for (Item item : relatedItem) {
				if (url.equals(item.item_url)) {
					HashMap<String, Object> properties = new HashMap<String, Object>();
					properties.put(EventProperty.ITEM, mItem.pk);
					properties.put(EventProperty.TO_ITEM, item.pk);
					properties.put(EventProperty.TO_TITLE, item.title);
					properties.put(EventProperty.TO, "relate");
					new NetworkUtils.DataCollectionTask().execute(
							NetworkUtils.VIDEO_RELATE, properties);
					break;
				}
			}
			Intent intent = new Intent();
			intent.setAction(action);
			intent.putExtra("url", url);
			startActivity(intent);

		}
	};

	private void addFavorite() {
		if (isFavorite()) {
			String url = SimpleRestClient.sRoot_url + "/api/item/" + mItem.pk
					+ "/";
			String isnet = "";
			if(SimpleRestClient.isLogin()){
				isnet = "yes";
				deleteFavoriteByNet();
			}
			else{
				isnet = "no";
			}
			DaisyUtils.getFavoriteManager(ItemDetailActivity.this)
					.deleteFavoriteByUrl(url,isnet);
			showToast(getResources().getString(
					R.string.vod_bookmark_remove_success));
		} else {
			String url = SimpleRestClient.sRoot_url + "/api/item/" + mItem.pk
					+ "/";
			Favorite favorite = new Favorite();
			favorite.title = mItem.title;
			favorite.adlet_url = mItem.adlet_url;
			favorite.content_model = mItem.content_model;
			favorite.url = url;
			favorite.quality = mItem.quality;
			favorite.is_complex = mItem.is_complex;
			if(SimpleRestClient.isLogin()){
				favorite.isnet = "yes";
				createFavoriteByNet();
			}
			else{
				favorite.isnet = "no";
			}
			DaisyUtils.getFavoriteManager(ItemDetailActivity.this).addFavorite(
					favorite,favorite.isnet);
			showToast(getResources().getString(
					R.string.vod_bookmark_add_success));
		}
	}

	private void buyVideo() {
		PaymentDialog dialog = new PaymentDialog(ItemDetailActivity.this,
				R.style.PaymentDialog, ordercheckListener);
		mItem.model_name = "item";
		dialog.setItem(mItem);
		dialog.show();
	}

	private void startDramaListActivity() {
		Intent intent = new Intent();
		mDataCollectionProperties.put("to", "list");
		intent.setClass(ItemDetailActivity.this, DramaListActivity.class);
		intent.putExtra("item", mItem);
		startActivity(intent);
	}

	private OnClickListener mIdOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			try {
				int id = v.getId();
				Intent intent = new Intent();
				intent.putExtra(EventProperty.SECTION, mSection);
				InitPlayerTool tool = new InitPlayerTool(
						ItemDetailActivity.this);
				tool.setonAsyncTaskListener(new onAsyncTaskHandler() {

					@Override
					public void onPreExecute(Intent intent) {
						// TODO Auto-generated method stub
						mLoadingDialog.show();
					}

					@Override
					public void onPostExecute() {
						// TODO Auto-generated method stub
						mLoadingDialog.dismiss();
					}
				});
				switch (id) {
				case R.id.btn_left:
					String subUrl = null;
					if (isDrama()) {
						int sub_id = 0;
						String title = mItem.title;
						if (mHistory != null && mHistory.is_continue) {
							subUrl = mHistory.sub_url;
							for (Item item : mItem.subitems) {
								if (item.url.equals(subUrl)) {
									sub_id = item.pk;
									title += "(" + item.episode + ")";
									break;
								}
							}
						} else {
							subUrl = mItem.subitems[0].url;
							sub_id = mItem.subitems[0].pk;
							title += "(" + mItem.subitems[0].episode + ")";
						}
						mDataCollectionProperties.put(EventProperty.TITLE,
								title);
						mDataCollectionProperties.put(EventProperty.SUBITEM,
								sub_id);
					}
					mDataCollectionProperties.put(EventProperty.TO, "play");
					identify = (String) v.getTag();
					if (identify.equals(PREVIEW_VIDEO)) {
						// 预告
						if(isDrama()){
							tool.initClipInfo(subUrl, InitPlayerTool.FLAG_URL);
						}
						else{
							tool.initClipInfo(mItem, InitPlayerTool.FLAG_ITEM, true);
						}
					} else if (identify.equals(PLAY_VIDEO)) {
						// 播放
						if (isDrama())
							tool.initClipInfo(subUrl, InitPlayerTool.FLAG_URL);
						else
							tool.initClipInfo(mItem, InitPlayerTool.FLAG_ITEM);
					}

					// tool.initClipInfo(subUrl,InitPlayerTool.FLAG_URL);
					break;
				case R.id.middle_btn:
					identify = (String) v.getTag();
					if (identify.equals(BUY_VIDEO)) {
						// 购买
						if (isDrama()) {
							startDramaListActivity();
						} else {
							buyVideo();
						}
					} else if (identify.equals(COLLECT_VIDEO)) {
						addFavorite();
						if (isFavorite()) {
							v.setBackgroundResource(R.drawable.collected_btn_bg_selector);
						} else {
							v.setBackgroundResource(R.drawable.collect_btn_bg_selector);
						}
					}
					break;
				case R.id.btn_right:
					identify = (String) v.getTag();
					if (identify.equals(COLLECT_VIDEO)) {
						addFavorite();
						if (isFavorite()) {
							v.setBackgroundResource(R.drawable.collected_btn_bg_selector);
						} else {
							v.setBackgroundResource(R.drawable.collect_btn_bg_selector);
						}
					} else if (identify.equals(DRAMA_VIDEO)) {
						mDataCollectionProperties.put("to", "list");
						intent.setClass(ItemDetailActivity.this,
								DramaListActivity.class);
						intent.putExtra("item", mItem);
						startActivity(intent);
					}
					break;
				// case R.id.btn_fill:
				// mDataCollectionProperties.put(EventProperty.TO, "play");
				//
				// // intent.setAction("tv.ismar.daisy.Play");
				// // intent.putExtra("item", mItem);
				//
				// // intent.setClass(ItemDetailActivity.this,
				// // QiYiPlayActivity.class);
				// // startActivity(intent);
				// tool.initClipInfo(mItem,InitPlayerTool.FLAG_ITEM);
				// break;
				// case R.id.btn_favorite:
				// if (isFavorite()) {
				// String url = SimpleRestClient.sRoot_url + "/api/item/"
				// + mItem.pk + "/";
				// DaisyUtils.getFavoriteManager(ItemDetailActivity.this)
				// .deleteFavoriteByUrl(url);
				// showToast(getResources().getString(
				// R.string.vod_bookmark_remove_success));
				// } else {
				// String url = SimpleRestClient.sRoot_url + "/api/item/"
				// + mItem.pk + "/";
				// Favorite favorite = new Favorite();
				// favorite.title = mItem.title;
				// favorite.adlet_url = mItem.adlet_url;
				// favorite.content_model = mItem.content_model;
				// favorite.url = url;
				// favorite.quality = mItem.quality;
				// favorite.is_complex = mItem.is_complex;
				// DaisyUtils.getFavoriteManager(ItemDetailActivity.this)
				// .addFavorite(favorite);
				// // mFavoriteManager.addFavorite(mItem.title, url,
				// // mItem.content_model);
				// showToast(getResources().getString(
				// R.string.vod_bookmark_add_success));
				// }
				// if (isFavorite()) {
				// mBtnFavorite.setText(getResources().getString(
				// R.string.favorited));
				// } else {
				// mBtnFavorite.setText(getResources().getString(
				// R.string.favorite));
				// }
				// break;
				case R.id.more_content:
					if (mRelatedItem != null && mRelatedItem.length > 0) {
						intent.putExtra("related_item", new ArrayList<Item>(
								Arrays.asList(mRelatedItem)));
					}
					mDataCollectionProperties.put(EventProperty.TO, "relate");

					intent.putExtra("item", mItem);
					intent.setClass(ItemDetailActivity.this,
							RelatedActivity.class);
					startActivity(intent);
					break;
				// case R.id.btn_fill_buy:
				//
				// PaymentDialog dialog = new
				// PaymentDialog(ItemDetailActivity.this,
				// R.style.PaymentDialog);
				// dialog.setItem(mItem);
				// //dialog.show();
				//
				// tool.initClipInfo(mItem,InitPlayerTool.FLAG_ITEM,true);
				// break;
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
		if (mLoadingImageQueue != null) {
			mLoadingImageQueue.put(imageView, true);
		}
	}

	@Override
	public void onLoadingEnded(AsyncImageView imageView, Bitmap image) {
		if (mLoadingImageQueue != null) {
			mLoadingImageQueue.remove(imageView);
		}
	}

	@Override
	public void onLoadingFailed(AsyncImageView imageView, Throwable throwable) {
		if (mLoadingImageQueue != null) {
			mLoadingImageQueue.remove(imageView);
		}
	}

	private void showToast(String text) {
		LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(R.layout.simple_toast,
				(ViewGroup) findViewById(R.id.simple_toast_root));
		TextView toastText = (TextView) layout.findViewById(R.id.toast_text);
		toastText.setText(text);
		Toast toast = new Toast(getApplicationContext());
		toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setView(layout);
		toast.show();
	}

	private void setExpenseStatus() {
		/*
		 * if this item is a drama , the button should split to two. otherwise.
		 * use one button.
		 */
		if (isFree()) {
			// 免费
			if (!isDrama()) {
				// 电影
				mLeftBtn.setBackgroundResource(R.drawable.play_btn_bg_selector);
				mLeftBtn.setTag(PLAY_VIDEO);
				mMiddleBtn
						.setBackgroundResource(R.drawable.collect_btn_bg_selector);
				mMiddleBtn.setTag(COLLECT_VIDEO);
				mRightBtn.setVisibility(View.GONE);
			} else {
				// 电视剧
				mLeftBtn.setBackgroundResource(R.drawable.play_btn_bg_selector);
				mLeftBtn.setTag(PLAY_VIDEO);
				mMiddleBtn
						.setBackgroundResource(R.drawable.collect_btn_bg_selector);
				mMiddleBtn.setTag(COLLECT_VIDEO);
				mRightBtn
						.setBackgroundResource(R.drawable.drama_btn_bg_selector);
				mRightBtn.setTag(DRAMA_VIDEO);
			}
			mCollectBtn = mMiddleBtn;
		} else {
			// 收费
			if (!isBuy) {
				// 未购买
				if (!isDrama()) {
					// 电影
					mLeftBtn.setBackgroundResource(R.drawable.preview_video_btn_bg_selector);
					mLeftBtn.setTag(PREVIEW_VIDEO);
					mMiddleBtn
							.setBackgroundResource(R.drawable.buy_video_btn_bg_selector);
					mMiddleBtn.setTag(BUY_VIDEO);
					mRightBtn
							.setBackgroundResource(R.drawable.collect_btn_bg_selector);
					mRightBtn.setTag(COLLECT_VIDEO);
				} else {
					// 电视剧
					mLeftBtn.setBackgroundResource(R.drawable.preview_video_btn_bg_selector);
					mLeftBtn.setTag(PREVIEW_VIDEO);
					mMiddleBtn
							.setBackgroundResource(R.drawable.buy_video_btn_bg_selector);
					mMiddleBtn.setTag(BUY_VIDEO);
					mRightBtn
							.setBackgroundResource(R.drawable.collect_btn_bg_selector);
					mRightBtn.setTag(COLLECT_VIDEO);
				}
				detail_price_txt.setText("￥" + mItem.expense.price);
				detail_duration_txt.setText("有效期" + mItem.expense.duration
						+ "天");
				detail_price_txt.setVisibility(View.VISIBLE);
				detail_duration_txt.setVisibility(View.VISIBLE);
				remainDay = mItem.expense.duration;
				mCollectBtn = mRightBtn;
			} else {
				// 已经购买
				isbuy_label.setVisibility(View.VISIBLE);
				mDetailQualityLabel.setVisibility(View.GONE);
				if (!isDrama()) {
					// 电影
					mLeftBtn.setBackgroundResource(R.drawable.play_btn_bg_selector);
					mLeftBtn.setTag(PLAY_VIDEO);
					mMiddleBtn
							.setBackgroundResource(R.drawable.collect_btn_bg_selector);
					mMiddleBtn.setTag(COLLECT_VIDEO);
					mRightBtn.setVisibility(View.GONE);
					// mRightBtn.setBackgroundResource(R.drawable.collect_btn_bg_selector);
					// mRightBtn.setTag(COLLECT_VIDEO);
				} else {
					// 电视剧
					mLeftBtn.setBackgroundResource(R.drawable.play_btn_bg_selector);
					mLeftBtn.setTag(PLAY_VIDEO);
					mMiddleBtn
							.setBackgroundResource(R.drawable.collect_btn_bg_selector);
					mMiddleBtn.setTag(COLLECT_VIDEO);
					mRightBtn
							.setBackgroundResource(R.drawable.drama_btn_bg_selector);
					mRightBtn.setTag(COLLECT_VIDEO);
				}
				detail_price_txt.setText("已付费");
				detail_duration_txt.setText("剩余" + remainDay + "天");
				detail_price_txt.setVisibility(View.VISIBLE);
				detail_duration_txt.setVisibility(View.VISIBLE);
				detail_duration_txt
						.setBackgroundResource(R.drawable.vod_detail_already_payment_duration);
				detail_price_txt
						.setBackgroundResource(R.drawable.vod_detail_already_payment_price);
				mCollectBtn = mMiddleBtn;
			}
		}
	}

	private PaymentDialog.OrderResultListener ordercheckListener = new PaymentDialog.OrderResultListener() {

		@Override
		public void payResult(boolean result) {
			isBuy = true;
			setExpenseStatus();
		}

	};
	
	private void deleteFavoriteByNet(){
		mSimpleRestClient.doSendRequest("/api/bookmark/remove/", "post", "access_token="+
	    SimpleRestClient.access_token+"&device_token="+SimpleRestClient.device_token+"&item="+mItem.pk, new HttpPostRequestInterface() {
			
			@Override
			public void onSuccess(String info) {
				// TODO Auto-generated method stub
				if("200".equals(info)){
					mCollectBtn.setBackgroundResource(R.drawable.collect_btn_bg_selector);
//					showToast(getResources().getString(
//							R.string.vod_bookmark_remove_success));
				}
			}
			
			@Override
			public void onPrepare() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onFailed(String error) {
				// TODO Auto-generated method stub
				mCollectBtn.setBackgroundResource(R.drawable.collected_btn_bg_selector);
//				showToast(getResources().getString(
//						R.string.vod_bookmark_remove_unsuccess));
			}
		});
	}
	private void createFavoriteByNet(){
		mSimpleRestClient.doSendRequest("/api/bookmarks/create/", "post", "access_token="+SimpleRestClient.access_token+"&device_token="+SimpleRestClient.device_token+"&item="+mItem.pk, new HttpPostRequestInterface() {
			
			@Override
			public void onSuccess(String info) {
				// TODO Auto-generated method stub
				mCollectBtn.setBackgroundResource(R.drawable.collected_btn_bg_selector);
//				showToast(getResources().getString(
//						R.string.vod_bookmark_add_success));
			}
			
			@Override
			public void onPrepare() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onFailed(String error) {
				// TODO Auto-generated method stub
				mCollectBtn.setBackgroundResource(R.drawable.collect_btn_bg_selector);
//				showToast(getResources().getString(
//						R.string.vod_bookmark_add_unsuccess));
			}
		});
	}
}
