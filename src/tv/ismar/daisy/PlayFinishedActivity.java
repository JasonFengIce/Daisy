package tv.ismar.daisy;

import java.io.InputStream;

import tv.ismar.daisy.adapter.PlayFinishedAdapter;
import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.ImageUtils;
import tv.ismar.daisy.core.NetworkUtils;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.models.Favorite;
import tv.ismar.daisy.models.Item;
import tv.ismar.daisy.persistence.FavoriteManager;
import tv.ismar.daisy.views.AlertDialogFragment;
import tv.ismar.daisy.views.LoadingDialog;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PlayFinishedActivity extends Activity implements OnFocusChangeListener, OnItemClickListener, OnClickListener {
	private Item item = new Item();
	private InputStream input;
	private Bitmap bitmap;
	LinearLayout linearLeft;
	LinearLayout linearRight;
	TextView tvVodName;
	ImageView imageBackgroud;
	ImageView imageVodLabel;
	Button btnReplay;
	Button btnFavorites;
	GridView gridview;
	PlayFinishedAdapter playAdapter;
	private Item[] items;
	private static final int UPDATE = 1;
	private static final int UPDATE_BITMAP = 2;
	private static final int NETWORK_EXCEPTION = -1;
	private LoadingDialog loadDialog;
	final SimpleRestClient simpleRest = new SimpleRestClient();
	private FavoriteManager mFavoriteManager;
	private static int leftCover = R.drawable.cover_left;
	private static int rightCover = R.drawable.cover_right;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.play_finished);

		initViews();
		mFavoriteManager = DaisyUtils.getFavoriteManager(this);
		loadDialogShow();
		try {
			Intent intent = getIntent();
			if (null != intent) {
				item = (Item) intent.getExtras().get("item");
			}
		} catch (Exception e) {
			e.printStackTrace();
			finish();
			return;
		}
		// 实际这些已经封装好了
		new Thread(mBitmapTask).start();
		// 实际这些已经封装好了
		new Thread(mRelatedTask).start();
	}

	private Runnable mBitmapTask = new Runnable() {
		@Override
		public void run() {
			input = NetworkUtils.getInputStream(item.poster_url);
			bitmap = ImageUtils.getBitmapFromInputStream(input, 480, 270);
			if (bitmap == null) {

			} else {
				mHandle.sendEmptyMessage(UPDATE_BITMAP);
			}
		}
	};

	private Runnable mRelatedTask = new Runnable() {
		@Override
		public void run() {
			items = simpleRest.getRelatedItem("/api/tv/relate/" + item.item_pk);
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
		linearLeft = (LinearLayout) findViewById(R.id.linear_left);
		linearRight = (LinearLayout) findViewById(R.id.linear_right);
		tvVodName = (TextView) findViewById(R.id.tv_vodie_name);
		imageBackgroud = (ImageView) findViewById(R.id.image_vodie_backgroud);
		imageVodLabel = (ImageView) findViewById(R.id.image_vod_label);
		btnReplay = (Button) findViewById(R.id.btn_replay);
		btnReplay.setOnClickListener(this);
		btnReplay.setOnFocusChangeListener(this);
		btnFavorites = (Button) findViewById(R.id.btn_favorites);
		btnFavorites.setOnClickListener(this);
		btnFavorites.setOnFocusChangeListener(this);
		gridview = (GridView) findViewById(R.id.gridview_related);
		gridview.setOnFocusChangeListener(this);
		gridview.setOnItemClickListener(this);
		gridview.setNumColumns(3);
		gridview.setVerticalSpacing(40);
		gridview.setHorizontalSpacing(100);
		loadDialog = new LoadingDialog(this, getString(R.string.vod_loading));

	}

	private Handler mHandle = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case UPDATE:
				playAdapter = new PlayFinishedAdapter(PlayFinishedActivity.this, items, R.layout.playfinish_gridview_item);
				gridview.setAdapter(playAdapter);
				break;
			case UPDATE_BITMAP:
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
				imageBackgroud.setImageBitmap(bitmap);
				loadDialogShow();
				break;
			case NETWORK_EXCEPTION:
				showDialog(AlertDialogFragment.NETWORK_EXCEPTION_DIALOG, mRelatedTask);
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
					linearRight.setBackgroundResource(0);
					linearLeft.setBackgroundResource(leftCover);
				} else {
					linearLeft.setBackgroundResource(0);
					linearRight.setBackgroundResource(rightCover);
				}
			} catch (Exception e) {
			}
			break;
		case R.id.btn_replay:
			try {
				if (hasFocus) {
					btnReplay.setTextColor(getResources().getColor(R.color.play_finished));
				} else {
					btnReplay.setTextColor(getResources().getColor(R.color.search_color));
				}
				break;
			} catch (Exception e) {
			}
		case R.id.btn_favorites:
			try {
				if (hasFocus) {
					btnFavorites.setTextColor(getResources().getColor(R.color.play_finished));
				} else {
					btnFavorites.setTextColor(getResources().getColor(R.color.search_color));
				}
				break;
			} catch (Exception e) {
			}

		default:
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long postions) {
		Intent intent = new Intent();
		intent.putExtra("url", items[position].item_url);
		intent.setAction("tv.ismar.daisy.Item");
		try {
			startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_replay:
			Intent intent = new Intent();
			intent.putExtra("item", item);
			intent.setAction("tv.ismar.daisy.Play");
			try {
				startActivity(intent);
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case R.id.btn_favorites:
			if (isFavorite()) {
				String url = simpleRest.root_url + "/api/item/" + item.pk + "/";
				mFavoriteManager.deleteFavoriteByUrl(url);
			} else {
				String url = simpleRest.root_url + "/api/item/" + item.pk + "/";
				Favorite favorite = new Favorite();
				favorite.title = item.title;
				favorite.adlet_url = item.adlet_url;
				favorite.content_model = item.content_model;
				favorite.url = url;
				favorite.quality = item.quality;
				favorite.is_complex = item.is_complex;
				mFavoriteManager.addFavorite(favorite);
				// mFavoriteManager.addFavorite(item.title, url, mItem.content_model);
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
			Favorite favorite = mFavoriteManager.getFavoriteByUrl(url);
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
}
