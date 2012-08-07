package tv.ismar.daisy;

import java.io.InputStream;

import tv.ismar.daisy.adapter.PlayFinishedAdapter;
import tv.ismar.daisy.core.ImageUtils;
import tv.ismar.daisy.core.NetworkUtils;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.models.Item;
import tv.ismar.daisy.views.LoadingDialog;
import android.app.Activity;
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
	private LoadingDialog loadDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.play_finished);
		initViews();
		final SimpleRestClient simpleRest = new SimpleRestClient();
		loadDialogShow();
		Intent intent = getIntent();
		if (null != intent) {
			item = (Item) intent.getExtras().get("item");
		}
		// 实际这些已经封装好了
		new Thread(new Runnable() {
			@Override
			public void run() {
				input = NetworkUtils.getInputStream(item.poster_url);
				bitmap = ImageUtils.getBitmapFromInputStream(input, 480, 270);
				mHandle.sendEmptyMessage(UPDATE_BITMAP);
			}
		}) {
		}.start();
		// 实际这些已经封装好了
		new Thread(new Runnable() {
			@Override
			public void run() {
				items = simpleRest.getRelatedItem("/api/tv/relate/" + item.pk);
				mHandle.sendEmptyMessage(UPDATE);
			}
		}) {
		}.start();
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
					linearLeft.setBackgroundResource(R.drawable.cover_left);
				} else {
					linearLeft.setBackgroundResource(0);
					linearRight.setBackgroundResource(R.drawable.cover_right);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case R.id.btn_replay:
			if (hasFocus) {
				btnReplay.setTextColor(getResources().getColor(R.color.play_finished));
			} else {
				btnReplay.setTextColor(getResources().getColor(R.color.search_color));
			}
			break;
		case R.id.btn_favorites:
			if (hasFocus) {
				btnFavorites.setTextColor(getResources().getColor(R.color.play_finished));
			} else {
				btnFavorites.setTextColor(getResources().getColor(R.color.search_color));
			}
			break;

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
}
