package tv.ismar.daisy;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import com.google.gson.JsonSyntaxException;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sakuratya.horizontal.ui.ZGridView;
import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.EventProperty;
import tv.ismar.daisy.core.NetworkUtils;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.SimpleRestClient.HttpPostRequestInterface;
import tv.ismar.daisy.exception.ItemOfflineException;
import tv.ismar.daisy.exception.NetworkException;
import tv.ismar.daisy.models.Item;
import tv.ismar.daisy.player.InitPlayerTool;
import tv.ismar.daisy.utils.BitmapDecoder;
import tv.ismar.daisy.utils.StringUtils;
import tv.ismar.daisy.utils.Util;
import tv.ismar.daisy.views.AsyncImageView;
import tv.ismar.daisy.views.CustomDialog;
import tv.ismar.daisy.views.LoadingDialog;
import tv.ismar.daisy.views.PaymentDialog;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

public class PackageDetailActivity extends BaseActivity implements OnItemClickListener{
 
	private TextView vod_payment_pacakge_title;
	private TextView vod_payment_packageDescribe_content;
	private LoadingDialog mLoadingDialog;
	private Item mItem;
	private SimpleRestClient mSimpleRestClient;
	private ZGridView vod_payment_item_of_package_container;
	private Item[] mRelatedItem;
	private LinearLayout mRelatedVideoContainer;
	private AsyncImageView vod_payment_poster;
	private TextView vod_payment_price;
	private TextView vod_payment_duration;
	private Button vod_payment_item_more;
	private Button vod_payment_buyButton;
	private RelativeLayout detail_left_container;
	private LinearLayout detail_right_container;
	Dialog dialog = null;
	private DialogInterface.OnClickListener mPositiveListener;
	private DialogInterface.OnClickListener mNegativeListener;
	private int remainDay = -1;
	private ImageView isbuy_label;
	private ImageView mDetailQualityLabel;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.payment_package_detail_layout);
       final View background = findViewById(R.id.large_layout);

		new BitmapDecoder().decode(this, R.drawable.main_bg, new BitmapDecoder.Callback() {
			@Override
			public void onSuccess(BitmapDrawable bitmapDrawable) {
				background.setBackgroundDrawable(bitmapDrawable);
			}
		});
		mSimpleRestClient = new SimpleRestClient();
		DaisyUtils.getVodApplication(this).addActivityToPool(this.toString(), this);
		initView();
		getData();
	}
	@Override
	protected void onDestroy() {
		DaisyUtils.getVodApplication(this).removeActivtyFromPool(this.toString());
		super.onDestroy();
	}
	private void buyVideo(){
		PaymentDialog dialog = new PaymentDialog(this,
        R.style.PaymentDialog,ordercheckListener);
		mItem.model_name="package";
        dialog.setItem(mItem);
        dialog.show();
	}
	private void initView(){
		//mDetailQualityLabel = (ImageView)findViewById(R.id.quality_label);
		isbuy_label = (ImageView)findViewById(R.id.isbuy_label);
		detail_left_container = (RelativeLayout)findViewById(R.id.detail_left_container);
		detail_right_container = (LinearLayout)findViewById(R.id.detail_right_container);
		mLoadingDialog = new LoadingDialog(this, getResources().getString(R.string.loading));
		mLoadingDialog.setOnCancelListener(mLoadingCancelListener);
		vod_payment_pacakge_title = (TextView) findViewById(R.id.vod_payment_pacakge_title);
		vod_payment_packageDescribe_content = (TextView)findViewById(R.id.vod_payment_packageDescribe_content);
		mRelatedVideoContainer = (LinearLayout)findViewById(R.id.related_video_container);
		vod_payment_item_of_package_container = (ZGridView)findViewById(R.id.vod_payment_item_of_package_container);
		vod_payment_item_of_package_container.setOnItemClickListener(this);
		vod_payment_item_of_package_container.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus){
					vod_payment_item_of_package_container.setSelection(0);
				}
			}
		});
		vod_payment_poster = (AsyncImageView)findViewById(R.id.vod_payment_poster);
		vod_payment_price = (TextView)findViewById(R.id.vod_payment_price);
		vod_payment_duration = (TextView)findViewById(R.id.vod_payment_duration);
		vod_payment_buyButton = (Button)findViewById(R.id.vod_payment_buyButton);
		vod_payment_item_more = (Button)findViewById(R.id.vod_payment_item_more);
		vod_payment_buyButton.setOnHoverListener(onHoverListener);
		vod_payment_item_more.setOnHoverListener(onHoverListener);
        vod_payment_buyButton.setFocusable(true);
        vod_payment_buyButton.requestFocus();
		vod_payment_buyButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				buyVideo();
			}
		});
		vod_payment_item_more.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(mItem!=null){
					Intent intent = new Intent();
					intent.setAction("tv.ismar.daisy.packagelist");
					intent.putExtra("pk", mItem.pk);
					startActivity(intent);
				}
			}
		});
	}
	private void getData(){
		String url = getIntent().getStringExtra("url");
		mLoadingDialog.show();
		new GetItemTask().execute(url);
	}
	private OnCancelListener mLoadingCancelListener = new OnCancelListener() {
		
		@Override
		public void onCancel(DialogInterface dialog) {
			finish();
			dialog.dismiss();
		}
	};
	private void buildRelatedList() {
		for (int i = 0; i < 4 && i < mRelatedItem.length; i++) {
			RelativeLayout relatedHolder = (RelativeLayout) LayoutInflater
					.from(PackageDetailActivity.this).inflate(
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
			TextView focusView = (TextView) relatedHolder
					.findViewById(R.id.related_focus);
			ImageView qualityLabel = (ImageView) relatedHolder
					.findViewById(R.id.related_quality_label);
			TextView related_price_txt = (TextView)relatedHolder.findViewById(R.id.related_price_txt);
			if(mRelatedItem[i].expense!=null){
				related_price_txt.setVisibility(View.VISIBLE);
				related_price_txt.setText("￥"+mRelatedItem[i].expense.price);
			}
			if (mRelatedItem[i].quality == 3) {
				qualityLabel.setImageResource(R.drawable.label_hd_small);
			} else if (mRelatedItem[i].quality == 4
					|| mRelatedItem[i].quality == 5) {
				qualityLabel.setImageResource(R.drawable.label_uhd_small);
			}
			imgView.setTag(mRelatedItem[i].adlet_url);
			imgView.setUrl(mRelatedItem[i].adlet_url);
			titleView.setText(mRelatedItem[i].title);
			focusView.setText(mRelatedItem[i].focus);
			relatedHolder.setTag(mRelatedItem[i]);
			relatedHolder.setOnHoverListener(onHoverListener);
			mRelatedVideoContainer.addView(relatedHolder);
//			relatedHolder
//					.setOnFocusChangeListener(mRelatedOnFocusChangeListener);
			relatedHolder.setOnClickListener(mRelatedClickListener);
		}
	}
	class GetRelatedTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			try {
				mRelatedItem = mSimpleRestClient
						.getRelatedItem("/api/package/relate/" + mItem.pk + "/");
			} catch (Exception e) {
				Message msg = new Message();
				msg.what = GETRELATED_FAIL;
				msg.obj = params;
				mainHandler.sendMessage(msg);
				cancel(true);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (mRelatedItem != null && mRelatedItem.length > 0) {
				buildRelatedList();
			}
			if (mLoadingDialog!=null&&mLoadingDialog.isShowing()) {
				mLoadingDialog.dismiss();
				detail_left_container.setVisibility(View.VISIBLE);
				detail_right_container.setVisibility(View.VISIBLE);
			}
		}

	}
	public static final int GETITEM_FAIL = 1;
	public static final int GETRELATED_FAIL = 2;
	private Handler mainHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
		    switch (msg.what) {
			case GETITEM_FAIL:
				showDialog(new GetItemTask(), (Object[]) msg.obj);
				break;
			case GETRELATED_FAIL:
				detail_left_container.setVisibility(View.VISIBLE);
				showDialog(new GetRelatedTask(), (Object[]) msg.obj);
				break;
			}
			}
		};
	class GetItemTask extends AsyncTask<String, Void, Void>{

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}
		@Override
		protected Void doInBackground(String... params) {
			// TODO Auto-generated method stub
			try {
				mItem = mSimpleRestClient.getItem(params[0]);
				
			} catch (JsonSyntaxException e) {
				// TODO Auto-generated catch block
				Message msg = new Message();
				msg.what = GETITEM_FAIL;
				msg.obj = params;
				mainHandler.sendMessage(msg);
				cancel(true);
			} catch (ItemOfflineException e) {
				// TODO Auto-generated catch block
				Message msg = new Message();
				msg.what = GETITEM_FAIL;
				msg.obj = params;
				mainHandler.sendMessage(msg);
				cancel(true);
			} catch (NetworkException e) {
				// TODO Auto-generated catch block
				Message msg = new Message();
				msg.what = GETITEM_FAIL;
				msg.obj = params;
				mainHandler.sendMessage(msg);
				cancel(true);
			}
			return null;
		}
		@Override
		protected void onPostExecute(Void result){
			if(mItem!=null){
				vod_payment_pacakge_title.setText(mItem.title);
				vod_payment_packageDescribe_content.setText(mItem.description);
				ItemAdapter adaptet= new ItemAdapter(PackageDetailActivity.this,mItem.items);
				vod_payment_item_of_package_container.setAdapter(adaptet);
				vod_payment_item_of_package_container.setFocusable(true);
				if(mItem.expense!=null){
					//收费
                    isbuy();
				}
				vod_payment_poster.setUrl(mItem.adlet_url);
				new GetRelatedTask().execute();
			}
		}
	}
	ViewHolder holder;
	  private void isbuy(){
		  SimpleRestClient simpleRestClient = new SimpleRestClient();
		  simpleRestClient.doSendRequest("/api/order/check/","post", "device_token="+SimpleRestClient.device_token+"&access_token="
		  +SimpleRestClient.access_token+"&package="+ mItem.pk, new HttpPostRequestInterface() {
			//subitem=214277
			@Override
			public void onSuccess(String info) {
				// TODO Auto-generated method stub
				if("0".equals(info)){
					//未购买
				    remainDay = -1;
					vod_payment_duration.setText("有效期"+mItem.expense.duration+"天");
					vod_payment_price.setText("￥"+mItem.expense.price+"元");
					vod_payment_duration.setBackgroundResource(R.drawable.vod_detail_unpayment_duration);
					vod_payment_price.setBackgroundResource(R.drawable.vod_detail_unpayment_price);
				}
				else{
					JSONArray jsonArray;
					try {
						jsonArray = new JSONArray(info);
						JSONObject json = jsonArray.getJSONObject(0);
						if(json.has("max_expiry_date")){
	                      //电视剧部分购买
							//暂时无法处理
						}
						else{
							//电影或者电视剧或者产品包整部购买
							try {
								remainDay = Util.daysBetween(Util.getTime(), info)+1;	
								if(remainDay==0){//过期了。认为没购买
								    remainDay = -1;
									vod_payment_duration.setText("有效期"+mItem.expense.duration+"天");
									vod_payment_price.setText("￥"+mItem.expense.price+"元");
									vod_payment_duration.setBackgroundResource(R.drawable.vod_detail_unpayment_duration);
									vod_payment_price.setBackgroundResource(R.drawable.vod_detail_unpayment_price);
								}
								else{
									//购买了，剩余天数大于0
									vod_payment_duration.setText("剩余"+remainDay+"天");
									vod_payment_price.setText("已付费");
									vod_payment_duration.setBackgroundResource(R.drawable.vod_detail_already_payment_duration);
									vod_payment_price.setBackgroundResource(R.drawable.vod_detail_already_payment_price);
									//isbuy_label.setVisibility(View.VISIBLE);
                                    vod_payment_buyButton.setEnabled(false);
                                    vod_payment_buyButton.setFocusable(false);
                                    vod_payment_buyButton.setText("已购买");
								}
							} catch (ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						//电影或者电视剧或者产品包整部购买
						try {
							info = info.substring(1, info.length()-1);
							remainDay = Util.daysBetween(Util.getTime(), info)+1;	
							if(remainDay<=0){//过期了。认为没购买
							    remainDay = -1;
								vod_payment_duration.setText("有效期"+mItem.expense.duration+"天");
								vod_payment_price.setText("￥"+mItem.expense.price+"元");
								vod_payment_duration.setBackgroundResource(R.drawable.vod_detail_unpayment_duration);
								vod_payment_price.setBackgroundResource(R.drawable.vod_detail_unpayment_price);
							}
							else{
								//购买了，剩余天数大于0
								vod_payment_duration.setText("剩余"+remainDay+"天");
								vod_payment_price.setText("已付费");
								vod_payment_duration.setBackgroundResource(R.drawable.vod_detail_already_payment_duration);
								vod_payment_price.setBackgroundResource(R.drawable.vod_detail_already_payment_price);
								vod_payment_buyButton.setEnabled(false);
								vod_payment_buyButton.setText("已购买");
								vod_payment_buyButton.setFocusable(false);
								vod_payment_buyButton.setBackgroundResource(R.drawable.button_disable);
								//vod_payment_buyButton.setVisibility(View.INVISIBLE);
								//isbuy_label.setVisibility(View.VISIBLE);
							}
						} catch (ParseException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}
			}
			
			@Override
			public void onPrepare() {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onFailed(String error) {
				// TODO Auto-generated method stub
			}
		});
	  }
	public class ItemAdapter extends BaseAdapter {
		ArrayList<Item> items;
		ArrayList<Item> tmpItems;
		Context mContext;
		public ItemAdapter(Context context,ArrayList<Item> dataItems){
			this.tmpItems = dataItems;
			int count = 0;
			items = new ArrayList<Item>();
			for(Item item:this.tmpItems){
				items.add(item);
				count++;
				if(count==3){
					break;
				}
			}
			mContext = context;
		}
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return this.items.size();
		}

		@Override
		public Item getItem(int position) {
			// TODO Auto-generated method stub
			return this.items.get(position);
		}

		@Override
		public long getItemId(int id) {
			// TODO Auto-generated method stub
			return id;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			if(convertView==null){
				convertView = LayoutInflater.from(mContext).inflate(R.layout.list_package_item,null);				
				holder = new ViewHolder();
				holder.previewImage = (AsyncImageView)convertView.findViewById(R.id.ItemImage);
				holder.title = (TextView)convertView.findViewById(R.id.ItemText);
				convertView.setTag(holder);
			}
			else
				holder = (ViewHolder) convertView.getTag();
			
			holder.title.setText(items.get(position).title);
			if(TextUtils.isEmpty(items.get(position).adlet_url)){
				holder.previewImage.setImageResource(R.drawable.preview);
			}else {
				Picasso.with(PackageDetailActivity.this).load(items.get(position).adlet_url).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).error(R.drawable.error_hor).into(holder.previewImage);

			}
			return convertView;
		}
		
	}
	public static class ViewHolder {
		TextView title;
		AsyncImageView previewImage;
	}
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		Item item = mItem.items.get(position);
		if(item!=null){
			if (item.is_complex) {
	            if (item.content_model.equals("variety") || item.content_model.equals("entertainment")) {
	                item.content_model = "music";
	            }
	            DaisyUtils.gotoSpecialPage(this, item.content_model, item.item_url, "related");
	        }
		}
	}
	private OnClickListener mRelatedClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {			
			Item itemSection = (Item) v.getTag();
			final Item[] relatedItem = mRelatedItem;
			for (Item item : relatedItem) {
				if (itemSection.url.equals(item.item_url)) {
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
            DaisyUtils.gotoSpecialPage(PackageDetailActivity.this,"package",itemSection.url,"unknown");
			
		}
	};
	private void showDialog(final AsyncTask task, final Object[] params){
		if(dialog==null){
		       mPositiveListener = new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						dialog.dismiss();
						task.execute(params);
					}
				};
				mNegativeListener = new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						dialog.dismiss();
						PackageDetailActivity.this.finish();
					}
				};
			 dialog = new CustomDialog.Builder(this)
				.setMessage(R.string.vod_get_data_error)
				.setPositiveButton(R.string.vod_retry, mPositiveListener)
				.setNegativeButton(R.string.vod_ok, mNegativeListener).create();
		}
		dialog.show();
	}
//private void  setquality(Item item){
//	switch (item.quality) {
//	case 3:
//		mDetailQualityLabel.setImageResource(R.drawable.label_uhd);
//		break;
//	case 4:
//	case 5:
//		mDetailQualityLabel.setImageResource(R.drawable.label_hd);
//		break;
//	default:
//		mDetailQualityLabel.setVisibility(View.GONE);
//	}
//}
	private PaymentDialog.OrderResultListener ordercheckListener = new PaymentDialog.OrderResultListener() {

		@Override
		public void payResult(boolean result) {
			if(result==true){
				vod_payment_duration.setText("剩余"+mItem.expense.duration+"天");
				vod_payment_price.setText("已付费");
				vod_payment_duration.setBackgroundResource(R.drawable.vod_detail_already_payment_duration);
				vod_payment_price.setBackgroundResource(R.drawable.vod_detail_already_payment_price);
				vod_payment_buyButton.setEnabled(false);
				vod_payment_buyButton.setFocusable(true);
				vod_payment_buyButton.setText("已购买");
				//vod_payment_buyButton.setVisibility(View.INVISIBLE);
				isbuy_label.setVisibility(View.INVISIBLE);
				vod_payment_item_of_package_container.requestFocus();
			}
		}

	};

	  private View.OnHoverListener onHoverListener = new View.OnHoverListener() {

			@Override
			public boolean onHover(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_HOVER_ENTER:
				case MotionEvent.ACTION_HOVER_MOVE:
					v.setFocusable(true);
					v.setFocusableInTouchMode(true);
					v.requestFocus();
					break;
				case MotionEvent.ACTION_HOVER_EXIT:
					break;
				}
				return false;
			}
		};

}
