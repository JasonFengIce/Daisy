package tv.ismar.daisy;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import tv.ismar.daisy.adapter.DaramAdapter;
import tv.ismar.daisy.core.ImageUtils;
import tv.ismar.daisy.core.NetworkUtils;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.models.Item;
import tv.ismar.daisy.models.Subitem;
import tv.ismar.daisy.views.LoadingDialog;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class DramaListActivity extends Activity implements OnItemSelectedListener, OnItemClickListener, OnFocusChangeListener {
	private Item item = new Item();
	private List<Subitem> list = new ArrayList<Subitem>();
	private DaramAdapter daram;
	private Subitem subitems;
	private GridView daramView;

	private ImageView imageBackgroud;
	private TextView tvDramaName;
	private TextView tvDramaAll;
	private TextView tvDramaType;

	private InputStream input;
	private Bitmap bitmap;

	private LoadingDialog loadDialog;
	private static final int UPDATE = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.drama_list_main);
		initViews();
		loadDialogShow();
		
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		item  = (Item) bundle.get("item");
		for (int i = 0; i < item.subitems.length; i++) {
			subitems = item.subitems[i];
			list.add(subitems);
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				input = NetworkUtils.getInputStream(item.poster_url);
				bitmap = ImageUtils.getBitmapFromInputStream(input, 480, 270);
				mHandle.sendEmptyMessage(UPDATE);
			}
		}) {
		}.start();
	}

	private void initViews() {
		daramView = (GridView) findViewById(R.id.drama_gridview);
		daramView.setOnItemSelectedListener(this);
		daramView.setOnItemClickListener(this);
		daramView.setNumColumns(10);
		daramView.setVerticalSpacing(50);

		imageBackgroud = (ImageView) findViewById(R.id.image_daram_back);
		tvDramaName = (TextView) findViewById(R.id.tv_drama_name);
		tvDramaAll = (TextView) findViewById(R.id.tv_daram_all);
		tvDramaType = (TextView) findViewById(R.id.tv_daram_type);
		loadDialog = new LoadingDialog(this, getString(R.string.vod_loading));
	}

	private Handler mHandle = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case UPDATE:
				// 名称
				tvDramaName.setText(item.title);
				// 集数
				tvDramaAll.setText(item.episode + getString(R.string.daram_ji) + getString(R.string.daram_all) + "  /");
				// 显示图片
				imageBackgroud.setImageBitmap(bitmap);
				daram = new DaramAdapter(DramaListActivity.this, list, R.layout.drama_gridview_item);
				daramView.setAdapter(daram);
				loadDialogShow();
				break;
			}
		}
	};

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onItemSelected(AdapterView<?> adapterView, View view, int postion, long arg3) {
		subitems = list.get(postion);
		// 分类
		tvDramaType.setText(subitems.title);
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int postion, long postions) {
		subitems = list.get(postion);
		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		bundle.putInt("itemPK", subitems.pk);
		bundle.putInt("subItemPK", subitems.item_pk);
		intent.setAction("com.ismartv.vod.play");
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
	private void loadDialogShow() {
		if (loadDialog.isShowing()) {
			loadDialog.dismiss();
		} else {
			loadDialog.show();
		}
	}

}
