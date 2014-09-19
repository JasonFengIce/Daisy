package tv.ismar.daisy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import tv.ismar.daisy.adapter.DaramAdapter;
import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.NetworkUtils;
import tv.ismar.daisy.models.Item;
import tv.ismar.daisy.views.AsyncImageView;
import tv.ismar.daisy.views.LoadingDialog;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class DramaListActivity extends Activity implements OnItemSelectedListener, OnItemClickListener, OnFocusChangeListener {
	
	private static final String TAG = "DramaListActivity";
	
	private Item mItem = new Item();
	private List<Item> mList = new ArrayList<Item>();
	private DaramAdapter mDramaAdapter;
	private Item mSubItem;
	private GridView mDramaView;
	private AsyncImageView mImageBackground;
	private ImageView mDramaImageLabel;
	private TextView mTvDramaName;
	private TextView mTvDramaAll;
	private TextView mTvDramaType;
//	private Bitmap bitmap;
	private LoadingDialog loadDialog;
	
	private HashMap<String, Object> mDataCollectionProperties = new HashMap<String, Object>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.drama_list_main);
		initViews();
		if(loadDialog!=null && !loadDialog.isShowing()) {
			loadDialog.show();
		}
		
		Bundle bundle = getIntent().getExtras();
		if (null == bundle) 
			return;
		mItem  = (Item) bundle.get("item");
		for (int i = 0; i < mItem.subitems.length; i++) {
			mSubItem = mItem.subitems[i];
			mList.add(mSubItem);
		}
		mDataCollectionProperties.put("item", mItem.pk);
		mDataCollectionProperties.put("title", mItem.title);
		mDataCollectionProperties.put("to", "return");
		new NetworkUtils.DataCollectionTask().execute(NetworkUtils.VIDEO_DRAMALIST_IN, mDataCollectionProperties);
		DaisyUtils.getVodApplication(this).addActivityToPool(this.toString(), this);
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				bitmap = ImageUtils.getBitmapFromInputStream(NetworkUtils.getInputStream(mItem.poster_url), 480, 270);
//				mHandle.sendEmptyMessage(UPDATE);
//			}
//		}) {
//		}.start();
		initLayout();
	}

	private void initViews() {
		mDramaView = (GridView) findViewById(R.id.drama_gridview);
		mDramaView.setOnItemSelectedListener(this);
		mDramaView.setOnItemClickListener(this);
		mDramaView.setNumColumns(10);
		mDramaView.setVerticalSpacing(30);

		mImageBackground = (AsyncImageView) findViewById(R.id.image_daram_back);
		mDramaImageLabel = (ImageView) findViewById(R.id.image_daram_label);
		mTvDramaName = (TextView) findViewById(R.id.tv_drama_name);
		mTvDramaAll = (TextView) findViewById(R.id.tv_daram_all);
		mTvDramaType = (TextView) findViewById(R.id.tv_daram_type);
		loadDialog = new LoadingDialog(this, getString(R.string.vod_loading));
	}

//	private Handler mHandle = new Handler() {
//		public void handleMessage(Message msg) {
//			super.handleMessage(msg);
//			switch (msg.what) {
//			case UPDATE:
//				// 名称
//				mTvDramaName.setText(mItem.title);
//				// 集数
//				mTvDramaAll.setText(mItem.episode + getString(R.string.daram_ji) + getString(R.string.daram_all) + "  /");
//				// 显示图片
//				mImageBackground.setImageBitmap(bitmap);
//				switch (mItem.quality) {
//				case 3:
//					mDramaImageLabel.setBackgroundResource(R.drawable.label_uhd);
//					break;
//				case 4:
//					mDramaImageLabel.setBackgroundResource(R.drawable.label_hd);
//					break;
//				default:
//					mDramaImageLabel.setVisibility(View.GONE);
//					break;
//				}
//				mDramaAdapter = new DaramAdapter(DramaListActivity.this, mList, R.layout.drama_gridview_item);
//				mDramaView.setAdapter(mDramaAdapter);
//				loadDialogShow();
//				break;
//			}
//		}
//	};
	
	private void initLayout() {
		if(mItem.poster_url != null) {
			mImageBackground.setUrl(mItem.poster_url);
		}
		// 名称
		mTvDramaName.setText(mItem.title);
		// 集数
		mTvDramaAll.setText(mItem.episode + getString(R.string.daram_ji) + getString(R.string.daram_all) + "  /");
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
		mDramaAdapter = new DaramAdapter(DramaListActivity.this, mList, R.layout.drama_gridview_item);
		mDramaView.setAdapter(mDramaAdapter);
		if(loadDialog!=null && loadDialog.isShowing()) {
			loadDialog.dismiss();
		}
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		// TODO Auto-generated method stub
		mDramaView.setSelector(null);
	}

	@Override
	public void onItemSelected(AdapterView<?> adapterView, View view, int postion, long arg3) {
		mSubItem = mList.get(postion);
		// 分类
		mTvDramaType.setText(mSubItem.title);
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int postion, long postions) {
		mSubItem = mList.get(postion);
		int sub_id = mSubItem.pk;
		String title = mItem.title + "("+mSubItem.episode + ")";
		mDataCollectionProperties.put("subitem", sub_id);
		mDataCollectionProperties.put("title", title);
		mDataCollectionProperties.put("to", "play");
		Intent intent = new Intent();
		Bundle bundle = new Bundle();
//		bundle.putInt("itemPK", mSubItem.pk);
//		bundle.putInt("subItemPK", mSubItem.item_pk);
		bundle.putString("url", mSubItem.url);
		intent.setAction("tv.ismar.daisy.Play");
		intent.putExtras(bundle);
		try {
			startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 显示自定义Dialog
	 */
//	private void loadDialogShow() {
//		if (loadDialog.isShowing()) {
//			loadDialog.dismiss();
//		} else {
//			loadDialog.show();
//		}
//	}

	@Override
	protected void onDestroy() {
		mItem = null;
		mList = null;
		mDramaAdapter = null;
		mSubItem = null;
		loadDialog = null;
		DaisyUtils.getVodApplication(this).removeActivtyFromPool(this.toString());
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		if(loadDialog!=null && loadDialog.isShowing()) {
			loadDialog.dismiss();
		}
		if(mItem!=null) {
			HashMap<String, Object> properties = new HashMap<String, Object>();
			properties.putAll(mDataCollectionProperties);
			new NetworkUtils.DataCollectionTask().execute(NetworkUtils.VIDEO_DRAMALIST_OUT, properties);
			mDataCollectionProperties.put("title", mItem.title);
			mDataCollectionProperties.put("to", "return");
			mDataCollectionProperties.remove("subitem");
		}
		super.onPause();
	}

	
}
