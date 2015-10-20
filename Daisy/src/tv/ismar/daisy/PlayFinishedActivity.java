package tv.ismar.daisy;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import org.sakuratya.horizontal.ui.ZGridView;
import tv.ismar.daisy.adapter.PlayFinishedAdapter;
import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.SimpleRestClient.HttpPostRequestInterface;
import tv.ismar.daisy.exception.NetworkException;
import tv.ismar.daisy.models.Favorite;
import tv.ismar.daisy.models.History;
import tv.ismar.daisy.models.Item;
import tv.ismar.daisy.persistence.FavoriteManager;
import tv.ismar.daisy.persistence.HistoryManager;
import tv.ismar.daisy.player.InitPlayerTool;
import tv.ismar.daisy.player.InitPlayerTool.onAsyncTaskHandler;
import tv.ismar.daisy.views.AlertDialogFragment;
import tv.ismar.daisy.views.AsyncImageView;
import tv.ismar.daisy.views.LoadingDialog;

public class PlayFinishedActivity extends BaseActivity implements OnFocusChangeListener, OnItemClickListener, OnClickListener {
	
	private static final String TAG = "PlayFinishedActivity";
	private Item item = new Item();
	//	private Bitmap bitmap;
	LinearLayout linearLeft;
	LinearLayout linearRight;
	TextView tvVodName;
	AsyncImageView imageBackgroud;
	ImageView imageVodLabel;
	Button btnReplay;
	Button btnFavorites;
	ZGridView gridview;
	PlayFinishedAdapter playAdapter;
	private Item[] items;
	private static final int UPDATE = 1;
	private static final int UPDATE_BITMAP = 2;
	private static final int NETWORK_EXCEPTION = -1;
	private LoadingDialog loadDialog;
	final SimpleRestClient simpleRest = new SimpleRestClient();
	private FavoriteManager mFavoriteManager;
	private HistoryManager mHistorymanager;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.play_finished);

		initViews();
		mFavoriteManager = DaisyUtils.getFavoriteManager(this);
		mHistorymanager = DaisyUtils.getHistoryManager(this);
		loadDialogShow();
		try {
			Intent intent = getIntent();
			if (null != intent) {
				DaisyUtils.getVodApplication(this).addActivityToPool(this.toString(), this);
				item = (Item) intent.getExtras().get("item");
			}
		} catch (Exception e) {
			e.printStackTrace();
			finish();
			return;
		}
		// 实际这些已经封装好了
//		new Thread(mBitmapTask).start();
		// 实际这些已经封装好了
		new Thread(mRelatedTask).start();
		initLayout();
	}

//	private Runnable mBitmapTask = new Runnable() {
//		@Override
//		public void run() {
//			input = NetworkUtils.getInputStream(item.poster_url);
//			bitmap = ImageUtils.getBitmapFromInputStream(input, 480, 270);
//			if (bitmap == null) {
//
//			} else {
//				mHandle.sendEmptyMessage(UPDATE_BITMAP);
//			}
//		}
//	};

	private Runnable mRelatedTask = new Runnable() {
		@Override
		public void run() {
			try {
				items = simpleRest.getRelatedItem("/api/tv/relate/" + item.item_pk+"/");
                Log.i("09876tgbvfredc","relate=="+item.item_pk);
            } catch (NetworkException e) {
				e.printStackTrace();
			}
			if (items == null || items.length == 0) {
				mHandle.sendEmptyMessage(NETWORK_EXCEPTION);
			} else {
				mHandle.sendEmptyMessage(UPDATE);
			}
		}
	};

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (isFavorite()) {
			btnFavorites.setText(getResources().getString(R.string.favorited));
		} else {
			btnFavorites.setText(getResources().getString(R.string.favorite));
		}
	}

	private void initViews() {
        View background = findViewById(R.id.large_layout);
        DaisyUtils.setbackground(R.drawable.main_bg,background);
		linearLeft = (LinearLayout) findViewById(R.id.linear_left);
		linearRight = (LinearLayout) findViewById(R.id.linear_right);
		tvVodName = (TextView) findViewById(R.id.tv_vodie_name);
		imageBackgroud = (AsyncImageView) findViewById(R.id.image_vodie_backgroud);
		imageVodLabel = (ImageView) findViewById(R.id.image_vod_label);
		btnReplay = (Button) findViewById(R.id.btn_replay);
		btnReplay.setOnClickListener(this);
		btnReplay.setOnFocusChangeListener(this);
		btnFavorites = (Button) findViewById(R.id.btn_favorites);
		btnFavorites.setOnClickListener(this);
		btnFavorites.setOnFocusChangeListener(this);
		gridview = (ZGridView) findViewById(R.id.gridview_related);
		gridview.setOnFocusChangeListener(this);
		gridview.setOnItemClickListener(this);
//		gridview.setNumColumns(3);
//		int H = DaisyUtils.getVodApplication(this).getheightPixels(this);
//		if(H==720){
//			gridview.setVerticalSpacing(20);
//			gridview.setHorizontalSpacing(50);
//		}
//		else{
//			gridview.setVerticalSpacing(40);
//			gridview.setHorizontalSpacing(100);
//		}
		loadDialog = new LoadingDialog(this, getString(R.string.vod_loading));

	}

	private Handler mHandle = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case UPDATE:
				playAdapter = new PlayFinishedAdapter(PlayFinishedActivity.this, items, R.layout.playfinish_gridview_item);


				gridview.setAdapter(playAdapter);
				gridview.setFocusable(true);
				//gridview.setHorizontalFadingEdgeEnabled(true);
				//gridview.setFadingEdgeLength(144);
				break;
//			case UPDATE_BITMAP:
//				tvVodName.setText(item.title);
//				switch (item.quality) {
//				case 3:
//					imageVodLabel.setBackgroundResource(R.drawable.label_uhd);
//					break;
//				case 4:
//					imageVodLabel.setBackgroundResource(R.drawable.label_hd);
//					break;
//				default:
//					imageVodLabel.setVisibility(View.GONE);
//					break;
//				}
//				imageBackgroud.setImageBitmap(bitmap);
//				loadDialogShow();
//				break;
			case NETWORK_EXCEPTION:
//				showDialog(AlertDialogFragment.NETWORK_EXCEPTION_DIALOG, mRelatedTask);
				break;
			}
		}
	};

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		switch (v.getId()) {
		case R.id.gridview_related:
			try {
				if (hasFocus) {
					//linearRight.setBackgroundResource(0);
					//linearLeft.setBackgroundResource(leftCover);
				} else {
					//linearLeft.setBackgroundResource(0);
					//linearRight.setBackgroundResource(rightCover);
				}
			} catch (Exception e) {
			}
			break;
//		case R.id.btn_replay:
//			try {
//				if (hasFocus) {
//					btnReplay.setTextColor(getResources().getColor(R.color.play_finished));
//				} else {
//					btnReplay.setTextColor(getResources().getColor(R.color.search_color));
//				}
//				break;
//			} catch (Exception e) {
//			}
//		case R.id.btn_favorites:
//			try {
//				if (hasFocus) {
//					btnFavorites.setTextColor(getResources().getColor(R.color.play_finished));
//				} else {
//					btnFavorites.setTextColor(getResources().getColor(R.color.search_color));
//				}
//				break;
//			} catch (Exception e) {
//			}

		default:
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long postions) {
		Intent intent = new Intent();

		//intent.setAction("tv.ismar.daisy.Item");
		if (items[position].expense != null) {
			intent.setAction("tv.ismar.daisy.Item");
			intent.putExtra("url", items[position].item_url);
			try {
				startActivity(intent);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			DaisyUtils.gotoSpecialPage(PlayFinishedActivity.this,
					items[position].content_model, items[position].item_url,
					"unknown");
		}
//		try {
//			startActivity(intent);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_replay:
			if(item!=null) {
				String url = SimpleRestClient.root_url + "/api/item/" + item.pk + "/";
                History history = null;
                if(SimpleRestClient.isLogin())
				    history = mHistorymanager.getHistoryByUrl(url,"yes");
                else
                    history = mHistorymanager.getHistoryByUrl(url,"no");
				if(history!=null) {
					history.last_position = 0;
                    if(SimpleRestClient.isLogin())
					    mHistorymanager.addHistory(history,"yes");
                    else
                        mHistorymanager.addHistory(history,"no");
					if(history.sub_url!=null) {
						url = history.sub_url;
					}
				}			
				try {
					InitPlayerTool tool = new InitPlayerTool(PlayFinishedActivity.this);
					tool.setonAsyncTaskListener(new onAsyncTaskHandler() {
						
						@Override
						public void onPreExecute(Intent intent) {
							// TODO Auto-generated method stub
							loadDialog.show();
						}
						
						@Override
						public void onPostExecute() {
							// TODO Auto-generated method stub
							loadDialog.dismiss();
							finish();
						}
					});
					tool.initClipInfo(url, InitPlayerTool.FLAG_URL);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			break;
		case R.id.btn_favorites:
			String isnet = "";
			if(SimpleRestClient.isLogin()){
				isnet = "yes";
			}
			else{
				isnet = "no";
			}
			if (isFavorite()) {
				String url = SimpleRestClient.root_url + "/api/item/" + item.pk + "/";
				if(SimpleRestClient.isLogin()){
					deleteFavoriteByNet();
					mFavoriteManager.deleteFavoriteByUrl(url,"yes");
				}
				else{
					mFavoriteManager.deleteFavoriteByUrl(url,"no");
				}

				showToast(getResources().getString(R.string.vod_bookmark_remove_success));
			} else {
				String url = SimpleRestClient.root_url + "/api/item/" + item.pk + "/";
				Favorite favorite = new Favorite();
				favorite.title = item.title;
				favorite.adlet_url = item.adlet_url;
				favorite.content_model = item.content_model;
				favorite.url = url;
				favorite.quality = item.quality;
				favorite.is_complex = item.is_complex;
				favorite.isnet = isnet;
				if(isnet.equals("yes")){
					createFavoriteByNet();
				}
				mFavoriteManager.addFavorite(favorite,isnet);
				// mFavoriteManager.addFavorite(item.title, url, mItem.content_model);
				showToast(getResources().getString(R.string.vod_bookmark_add_success));
			}
			if (isFavorite()) {
				btnFavorites.setText(getResources().getString(R.string.favorited));
			} else {
				btnFavorites.setText(getResources().getString(R.string.favorite));
			}
			break;
		default:
			break;
		}

	}
	private void deleteFavoriteByNet(){
		simpleRest.doSendRequest("/api/bookmark/remove/", "post", "access_token="+
	    SimpleRestClient.access_token+"&device_token="+SimpleRestClient.device_token+"&item="+item.pk, new HttpPostRequestInterface() {
			
			@Override
			public void onSuccess(String info) {
				// TODO Auto-generated method stub
				if("200".equals(info)){
	
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
	private void createFavoriteByNet(){
		simpleRest.doSendRequest("/api/bookmarks/create/", "post", "access_token="+SimpleRestClient.access_token+"&device_token="+SimpleRestClient.device_token+"&item="+item.pk, new HttpPostRequestInterface() {
			
			@Override
			public void onSuccess(String info) {
				// TODO Auto-generated method stub
		
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
	/**
	 * 显示自定义Dialog
	 */
	private void loadDialogShow() {
		if (loadDialog.isShowing()) {
			loadDialog.dismiss();
		} else {
			loadDialog.show();
		}
	}

	/*
	 * get the favorite status of the item.
	 */
	private boolean isFavorite() {
		if (item != null) {
			String url = item.item_url;
			if (url == null && item.pk != 0) {
				url = simpleRest.root_url + "/api/item/" + item.pk + "/";
			}
			Favorite favorite;
			if(SimpleRestClient.isLogin()){
				favorite = mFavoriteManager.getFavoriteByUrl(url,"yes");
			}
			else{
				favorite = mFavoriteManager.getFavoriteByUrl(url,"no");
			}
			if (favorite != null) {
				return true;
			}
		}
		return false;
	}

	public void showDialog(int dialogType, final Runnable task) {
		AlertDialogFragment newFragment = AlertDialogFragment.newInstance(dialogType);
		newFragment.setPositiveListener(new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				new Thread(task).start();
				dialog.dismiss();
			}
		});
		newFragment.setNegativeListener(new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				PlayFinishedActivity.this.finish();
				dialog.dismiss();
			}
		});
		newFragment.show(getFragmentManager(), "dialog");
	}
	
	private void initLayout() {
		tvVodName.setText(item.title);
		switch (item.quality) {
		case 3:
			imageVodLabel.setBackgroundResource(R.drawable.label_uhd);
			break;
		case 4:
			imageVodLabel.setBackgroundResource(R.drawable.label_hd);
			break;
		default:
			imageVodLabel.setVisibility(View.GONE);
			break;
		}
		imageBackgroud.setUrl(item.poster_url);
		loadDialogShow();
	}

	@Override
	protected void onPause() {
		if(loadDialog!=null && loadDialog.isShowing()) {
			loadDialog.dismiss();
		}
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		loadDialog = null;
		playAdapter = null;
		mFavoriteManager = null;
		DaisyUtils.getVodApplication(this).removeActivtyFromPool(this.toString());
		super.onDestroy();
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
