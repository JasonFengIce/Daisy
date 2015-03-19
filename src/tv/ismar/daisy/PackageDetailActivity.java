package tv.ismar.daisy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.sakuratya.horizontal.ui.ZGridView;

import com.google.gson.JsonSyntaxException;

import tv.ismar.daisy.adapter.DaramAdapter.ViewHolder;
import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.EventProperty;
import tv.ismar.daisy.core.NetworkUtils;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.exception.ItemOfflineException;
import tv.ismar.daisy.exception.NetworkException;
import tv.ismar.daisy.models.Expense;
import tv.ismar.daisy.models.Item;
import tv.ismar.daisy.player.InitPlayerTool;
import tv.ismar.daisy.player.InitPlayerTool.onAsyncTaskHandler;
import tv.ismar.daisy.views.AsyncImageView;
import tv.ismar.daisy.views.CustomDialog;
import tv.ismar.daisy.views.AsyncImageView.OnImageViewLoadListener;
import tv.ismar.daisy.views.LoadingDialog;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PackageDetailActivity extends Activity implements OnItemClickListener{
 
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
	private RelativeLayout detail_left_container;
	private LinearLayout detail_right_container;
	Dialog dialog = null;
	private DialogInterface.OnClickListener mPositiveListener;
	private DialogInterface.OnClickListener mNegativeListener;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.payment_package_detail_layout);
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
	private void initView(){
		detail_left_container = (RelativeLayout)findViewById(R.id.detail_left_container);
		detail_right_container = (LinearLayout)findViewById(R.id.detail_right_container);
		mLoadingDialog = new LoadingDialog(this, getResources().getString(R.string.loading));
		mLoadingDialog.setOnCancelListener(mLoadingCancelListener);
		vod_payment_pacakge_title = (TextView) findViewById(R.id.vod_payment_pacakge_title);
		vod_payment_packageDescribe_content = (TextView)findViewById(R.id.vod_payment_packageDescribe_content);
		mRelatedVideoContainer = (LinearLayout)findViewById(R.id.related_video_container);
		vod_payment_item_of_package_container = (ZGridView)findViewById(R.id.vod_payment_item_of_package_container);
		vod_payment_item_of_package_container.setOnItemClickListener(this);
		vod_payment_poster = (AsyncImageView)findViewById(R.id.vod_payment_poster);
		vod_payment_price = (TextView)findViewById(R.id.vod_payment_price);
		vod_payment_duration = (TextView)findViewById(R.id.vod_payment_duration);
		vod_payment_item_more = (Button)findViewById(R.id.vod_payment_item_more);
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
			relatedHolder.setTag(mRelatedItem[i].url);
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
				e.printStackTrace();
				mLoadingDialog.dismiss();
				showDialog(new GetRelatedTask(), params);
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
				e.printStackTrace();
			} catch (ItemOfflineException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NetworkException e) {
				// TODO Auto-generated catch block
				mLoadingDialog.dismiss();
				showDialog(new GetItemTask(),params);
			    cancel(true);
				e.printStackTrace();
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
					vod_payment_duration.setText("有效期"+mItem.expense.duration+"天");
					vod_payment_price.setText("￥"+mItem.expense.price+"元");
					vod_payment_duration.setBackgroundResource(R.drawable.vod_detail_unpayment_duration);
					vod_payment_price.setBackgroundResource(R.drawable.vod_detail_unpayment_price);
				}
				vod_payment_poster.setUrl(mItem.adlet_url);
				//mLoadingDialog.dismiss();
				new GetRelatedTask().execute();
			}
		}
	}
	ViewHolder holder;
	public class ItemAdapter extends BaseAdapter {
		ArrayList<Item> items;
		ArrayList<Item> tmpItems;
		Context mContext;
		private HashSet<AsyncImageView> mAsyncImageList = new HashSet<AsyncImageView>();
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
			holder.previewImage.setUrl(items.get(position).adlet_url);
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
			Intent intent = new Intent();
			if(item.is_complex) {
				Expense f = item.expense;
				intent.setAction("tv.ismar.daisy.Item");
				intent.putExtra("url", item.url);
				startActivity(intent);
			} else {
//				InitPlayerTool tool = new InitPlayerTool(PackageDetailActivity.this);
//				tool.setonAsyncTaskListener(new onAsyncTaskHandler() {
//					
//					@Override
//					public void onPreExecute(Intent intent) {
//						// TODO Auto-generated method stub
//						intent.putExtra(EventProperty.SECTION, s.slug);
//			            mLoadingDialog.show();
//					}
//					
//					@Override
//					public void onPostExecute() {
//						// TODO Auto-generated method stub
//						mLoadingDialog.dismiss();
//					}
//				});
//				tool.initClipInfo(item.url, InitPlayerTool.FLAG_URL);
			}
		}
	}
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
			intent.setAction("tv.ismar.daisy.packageitem");
			intent.putExtra("url", url);
			startActivity(intent);
			
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
}
