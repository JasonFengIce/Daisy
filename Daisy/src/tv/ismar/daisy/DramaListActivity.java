package tv.ismar.daisy;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.graphics.drawable.BitmapDrawable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sakuratya.horizontal.ui.ZGridView;
import org.sakuratya.horizontal.ui.ZGridView.OnScrollListener;

import tv.ismar.daisy.adapter.DaramAdapter;
import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.EventProperty;
import tv.ismar.daisy.core.NetworkUtils;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.SimpleRestClient.HttpPostRequestInterface;
import tv.ismar.daisy.models.Item;
import tv.ismar.daisy.player.InitPlayerTool;
import tv.ismar.daisy.player.InitPlayerTool.onAsyncTaskHandler;
import tv.ismar.daisy.utils.BitmapDecoder;
import tv.ismar.daisy.utils.Util;
import tv.ismar.daisy.views.AsyncImageView;
import tv.ismar.daisy.views.LoadingDialog;
import tv.ismar.daisy.views.PaymentDialog;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class DramaListActivity extends BaseActivity implements
		OnItemSelectedListener, OnItemClickListener {

	private static final String TAG = "DramaListActivity";
	public static String ORDER_CHECK_BASE_URL ="/api/play/check/";
    public final static int visableItems = 30;
    private final static int DISABLE_ORDER_ALL_DRANA = 0x10;
    private final static int ORDER_ALL_DRANA_SUCCESS = 0x11;

	private Item mItem = new Item();
	private List<Item> mList = new ArrayList<Item>();
	private DaramAdapter mDramaAdapter;
	private Item mSubItem;
	private ZGridView mDramaView;
	private AsyncImageView mImageBackground;
	private ImageView mDramaImageLabel;
	private TextView mTvDramaName;
	private TextView mTvDramaAll;
	private TextView mTvDramaType;
	private TextView one_drama_order_info;
	private Button orderAll_drama;
    private Button down_btn;
    private Button up_btn;
	// private Bitmap bitmap;
	private LoadingDialog loadDialog;
	private boolean paystatus = false;
	private InitPlayerTool tool;
//    private boolean orderAlldrama = false;

	private HashMap<String, Object> mDataCollectionProperties = new HashMap<String, Object>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.drama_list_main);
        final  View vv = findViewById(R.id.large_layout);
		new BitmapDecoder().decode(this, R.drawable.main_bg, new BitmapDecoder.Callback() {
			@Override
			public void onSuccess(BitmapDrawable bitmapDrawable) {
				vv.setBackgroundDrawable(bitmapDrawable);
			}
		});

		initViews();
		if (loadDialog != null && !loadDialog.isShowing()) {
			loadDialog.show();
		}
		Bundle bundle = getIntent().getExtras();
		if (null == bundle)
			return;
		mItem = (Item) bundle.get("item");
		for (int i = 0; i < mItem.subitems.length; i++) {
			mSubItem = mItem.subitems[i];
			mList.add(mSubItem);
		}
		mDataCollectionProperties.put(EventProperty.ITEM, mItem.pk);
		mDataCollectionProperties.put("title", mItem.title);
		mDataCollectionProperties.put("to", "return");
		NetworkUtils.SaveLogToLocal(
				NetworkUtils.VIDEO_DRAMALIST_IN, mDataCollectionProperties);
		DaisyUtils.getVodApplication(this).addActivityToPool(this.toString(),
				this);
		// new Thread(new Runnable() {
		// @Override
		// public void run() {
		// bitmap =
		// ImageUtils.getBitmapFromInputStream(NetworkUtils.getInputStream(mItem.poster_url),
		// 480, 270);
		// mHandle.sendEmptyMessage(UPDATE);
		// }
		// }) {
		// }.start();
		initLayout();
		down_btn = (Button)findViewById(R.id.down_btn);
		up_btn = (Button)findViewById(R.id.up_btn);
		mDramaView.setUpView(up_btn);
		mDramaView.setDownView(down_btn);
		mDramaView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
				} else {
//					mDramaView.setSelection(AdapterView.INVALID_POSITION);
				}
			}
		});

		mDramaView.setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(ZGridView view, int scrollState) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onScroll(ZGridView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub
				if (firstVisibleItem == 0) {
					View convertView = view.getChildAt(0);
					if (convertView != null) {
						Button btn = (Button) convertView
								.findViewById(R.id.btn_count);
						btn.requestFocus();
					}
				}

				 if(visibleItemCount>=totalItemCount){
					 down_btn.setVisibility(View.INVISIBLE);
				 }
				 if(firstVisibleItem == 0){
					 up_btn.setVisibility(View.INVISIBLE);
				 }
				 if(firstVisibleItem == 0 && firstVisibleItem+30 <totalItemCount){
					 down_btn.setVisibility(View.VISIBLE);
				 }
				 if(firstVisibleItem != 0 && firstVisibleItem + 30 >= totalItemCount){
					 down_btn.setVisibility(View.INVISIBLE);
					 up_btn.setVisibility(View.VISIBLE);
				 }
			}
		});
		down_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mDramaView.pageScroll(View.FOCUS_DOWN);
			}
		});
		up_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mDramaView.pageScroll(View.FOCUS_UP);
			}
		});
		down_btn.setOnHoverListener(onHoverListener);
		up_btn.setOnHoverListener(onHoverListener);
		up_btn.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus && lastdaramView != null){
					lastdaramView.setSelected(false);
				}
			}
		});
		down_btn.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus && lastdaramView != null){
//					View butvView = lastdaramView.findViewById(R.id.btn_count);
					lastdaramView.setSelected(false);
//					butvView.setBackgroundResource(R.drawable.daram_grid_selector);
				}
			}
		});
	}

	@Override
	public void onResume(){
		super.onResume();
		if (mItem.expense != null)
			orderCheck();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode==KeyEvent.KEYCODE_BACK){
			 Intent data=new Intent();  
	         data.putExtra("result", paystatus);
	         setResult(20, data);
			 finish();
		}
		return super.onKeyDown(keyCode, event);
	}
	private void initViews() {
		View v = (View)findViewById(R.id.drama_gridview);
		mDramaView = (ZGridView) v.findViewById(R.id.drama_zgridview);
		mDramaView.setOnItemSelectedListener(this);
		mDramaView.setOnItemClickListener(this);
		mDramaView.setNumColumns(10);
		mDramaView.setVerticalSpacing(30);

		mImageBackground = (AsyncImageView) findViewById(R.id.image_daram_back);
		mDramaImageLabel = (ImageView) findViewById(R.id.image_daram_label);
		mTvDramaName = (TextView) findViewById(R.id.tv_drama_name);
		mTvDramaAll = (TextView) findViewById(R.id.tv_daram_all);
		mTvDramaType = (TextView) findViewById(R.id.tv_daram_type);
		one_drama_order_info = (TextView) findViewById(R.id.one_drama_order_info);
		orderAll_drama = (Button) findViewById(R.id.orderAll_drama);
		orderAll_drama.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PaymentDialog dialog = new PaymentDialog(
						DramaListActivity.this, R.style.PaymentDialog,
						ordercheckListener);
				mItem.model_name = "item";
				dialog.setItem(mItem);
				dialog.show();
			}
		});
		orderAll_drama.setOnHoverListener(onHoverListener);
		orderAll_drama.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus && lastdaramView != null){
//					View butvView = lastdaramView.findViewById(R.id.btn_count);
//					lastdaramView.setSelected(false);
//					butvView.setBackgroundResource(R.drawable.daram_grid_selector);
				}
			}
		});
		loadDialog = new LoadingDialog(this, getString(R.string.vod_loading));
	}

	private Handler myHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch(msg.what){
			case DISABLE_ORDER_ALL_DRANA :{
				orderAll_drama.setEnabled(false);
				orderAll_drama.setFocusable(false);
				orderAll_drama.setText("已购买");
			}
			case ORDER_ALL_DRANA_SUCCESS :{
				orderAll_drama.setEnabled(false);
				orderAll_drama.setFocusable(false);
				orderAll_drama.setText("已购买");
				for (Item item : mList) {
					item.remainDay = mItem.expense.duration+1;
					mDramaAdapter.notifyDataSetChanged();
				}
			}
			}
		}

	};

	// private Handler mHandle = new Handler() {
	// public void handleMessage(Message msg) {
	// super.handleMessage(msg);
	// switch (msg.what) {
	// case UPDATE:
	// // 名称
	// mTvDramaName.setText(mItem.title);
	// // 集数
	// mTvDramaAll.setText(mItem.episode + getString(R.string.daram_ji) +
	// getString(R.string.daram_all) + "  /");
	// // 显示图片
	// mImageBackground.setImageBitmap(bitmap);
	// switch (mItem.quality) {
	// case 3:
	// mDramaImageLabel.setBackgroundResource(R.drawable.label_uhd);
	// break;
	// case 4:
	// mDramaImageLabel.setBackgroundResource(R.drawable.label_hd);
	// break;
	// default:
	// mDramaImageLabel.setVisibility(View.GONE);
	// break;
	// }
	// mDramaAdapter = new DaramAdapter(DramaListActivity.this, mList,
	// R.layout.drama_gridview_item);
	// mDramaView.setAdapter(mDramaAdapter);
	// loadDialogShow();
	// break;
	// }
	// }
	// };

	private void initLayout() {
		if (mItem.expense != null) {
			String expensevalue = getResources().getString(
					R.string.one_drama_order_info);
			one_drama_order_info.setText(String.format(expensevalue,
					mItem.expense.subprice, mItem.expense.duration));
			one_drama_order_info.setVisibility(View.VISIBLE);
//			orderAll_drama.setVisibility(View.VISIBLE);
		}
		if (mItem.poster_url != null) {
			mImageBackground.setUrl(mItem.poster_url);
		}
		// 名称
		mTvDramaName.setText(mItem.title);


        if (mItem.subitems.length > 0){
            String update_to_episode = getResources().getString(
                    R.string.update_to_episode);
            mTvDramaAll.setText(
                    String.format(update_to_episode, mItem.subitems.length)
                    );
        }

		// 集数
//		mTvDramaAll.setText(mItem.episode + getString(R.string.daram_ji)
//				+ getString(R.string.daram_all) + "  /");
		// 显示图片
		switch (mItem.quality) {
		case 3:
			mDramaImageLabel.setBackgroundResource(R.drawable.label_uhd);
			break;
		case 4:
			mDramaImageLabel.setBackgroundResource(R.drawable.label_hd);
			break;
		default:
			mDramaImageLabel.setVisibility(View.GONE);
			break;
		}
		mDramaAdapter = new DaramAdapter(DramaListActivity.this, mList, mItem,R.layout.drama_gridview_item);
		mDramaView.setAdapter(mDramaAdapter);
	//	mDramaView.setFocusable(true);
		mDramaAdapter.mTvDramaType = mTvDramaType;
		if(mDramaAdapter.getCount()<=mDramaView.getCount()){
			down_btn.setVisibility(View.INVISIBLE);
		}
		if (loadDialog != null && loadDialog.isShowing()) {
			loadDialog.dismiss();
		}
	}
//	@Override
//	public void onFocusChange(View v, boolean hasFocus) {
//		// TODO Auto-generated method stub
//		mDramaView.setSelector(null);
//	}

	private View lastdaramView;
	@Override
	public void onItemSelected(AdapterView<?> adapterView, View view,
			int postion, long arg3) {
//		if (lastdaramView != null)
//			lastdaramView.findViewById(R.id.btn_count).setSelected(false);
//		if (view != null)
//			view.findViewById(R.id.btn_count).setSelected(true);
//
//		lastdaramView = view;
		if(down_btn != null && !down_btn.isFocusable()){
			down_btn.setFocusable(true);
			down_btn.setFocusableInTouchMode(true);
		}
		mSubItem = mList.get(postion);
		// 分类
		mTvDramaType.setText(mSubItem.title);
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int postion,
			long postions) {
		mSubItem = mList.get(postion);
		int sub_id = mSubItem.pk;
		String title = mItem.title + "(" + mSubItem.episode + ")";
		mDataCollectionProperties.put("subitem", sub_id);
		mDataCollectionProperties.put("title", title);
		mDataCollectionProperties.put("to", "play");
		try {
			tool = new InitPlayerTool(DramaListActivity.this);
			tool.setonAsyncTaskListener(new onAsyncTaskHandler() {

				@Override
				public void onPreExecute(Intent intent) {
					// TODO Auto-generated method stub
					if (loadDialog != null)
					loadDialog.show();
				}

				@Override
				public void onPostExecute() {
					// TODO Auto-generated method stub
					if (loadDialog != null)
						loadDialog.dismiss();
				}
			});
			tool.fromPage = mItem.fromPage;
			tool.initClipInfo(mSubItem.url, InitPlayerTool.FLAG_URL);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 显示自定义Dialog
	 */
	// private void loadDialogShow() {
	// if (loadDialog.isShowing()) {
	// loadDialog.dismiss();
	// } else {
	// loadDialog.show();
	// }
	// }

	@Override
	protected void onDestroy() {
		if(tool != null){
			tool.removeAsycCallback();
		}
		mDramaAdapter.removeAsycCallback();
		mItem = null;
		mList = null;
		mDramaAdapter = null;
		mSubItem = null;
		loadDialog = null;
		DaisyUtils.getVodApplication(this).removeActivtyFromPool(
				this.toString());
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		if (loadDialog != null && loadDialog.isShowing()) {
			loadDialog.dismiss();
		}
		if (mItem != null) {
			HashMap<String, Object> properties = new HashMap<String, Object>();
			properties.putAll(mDataCollectionProperties);
			new NetworkUtils.DataCollectionTask().execute(
					NetworkUtils.VIDEO_DRAMALIST_OUT, properties);
			mDataCollectionProperties.put("title", mItem.title);
			mDataCollectionProperties.put("to", "return");
			mDataCollectionProperties.remove("subitem");
		}
		super.onPause();
	}

	private PaymentDialog.OrderResultListener ordercheckListener = new PaymentDialog.OrderResultListener() {

		@Override
		public void payResult(boolean result) {
			paystatus = result;
			if(result){
			myHandler.sendEmptyMessage(ORDER_ALL_DRANA_SUCCESS);
			}
//			orderCheck();
		}
	};

	public void orderCheck() {
		SimpleRestClient client = new SimpleRestClient();
		String typePara = "&item=" + mItem.pk;
		client.doSendRequest(ORDER_CHECK_BASE_URL, "post", "device_token="
				+ SimpleRestClient.device_token + "&access_token="
				+ SimpleRestClient.access_token + typePara, orderCheck);
	}

	private HttpPostRequestInterface orderCheck = new HttpPostRequestInterface() {

		@Override
		public void onPrepare() {
		}

		@Override
		public void onSuccess(String info) {
			orderAll_drama.setVisibility(View.VISIBLE);
			if(mList == null)
				return;
			if (info != null && "0".equals(info)) {
			} else {
				String currentDayString = Util.getTime();
				int remainDay;
				try {
					JSONArray array = new JSONArray(info);
					HashMap<Integer, String> element = new HashMap<Integer, String>();
					for (int i = 0; i < array.length(); i++) {
						JSONObject seria = array.getJSONObject(i);
						int pk = seria.getInt("wares_id");
						String expireDay = seria.getString("max_expiry_date");
						element.put(pk, expireDay);
					}
					for (Item item : mList) {
						if (element.containsKey(item.pk)) {
							remainDay = Util.daysBetween(currentDayString,
									element.get(item.pk))+1;
							item.remainDay = remainDay;
						}
					}
				} catch (JSONException e) {
//					orderAlldrama = true;
					paystatus = true;
					myHandler.sendEmptyMessage(DISABLE_ORDER_ALL_DRANA);
					try {
						remainDay = Util.daysBetween(currentDayString, info.replace("\"", ""));
						for (Item item : mList) {
							item.remainDay = remainDay+1;
						}
					} catch (ParseException e1) {
					}
				} catch (ParseException e1) {
					e1.printStackTrace();
				}
				mDramaAdapter.notifyDataSetChanged();
			}
		}

		@Override
		public void onFailed(String error) {
			
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
					v.requestFocusFromTouch();
//					if(lastdaramView != null){
//						lastdaramView.findViewById(R.id.btn_count).setBackgroundResource(R.drawable.daram_grid_selector);
//					}
					break;
				case MotionEvent.ACTION_HOVER_EXIT:
					break;
				}
				return false;
			}
		};

}
