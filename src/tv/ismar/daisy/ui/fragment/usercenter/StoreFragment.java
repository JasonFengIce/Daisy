package tv.ismar.daisy.ui.fragment.usercenter;

import tv.ismar.daisy.R;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.client.IsmartvUrlClient;
import tv.ismar.daisy.data.usercenter.YouHuiDingGouEntity;
import tv.ismar.daisy.ui.adapter.YouHuiDingGouAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.google.gson.Gson;

/**
 * Created by huaijie on 7/3/15.
 */
public class StoreFragment extends Fragment {
	private static final String TAG = "StoreFragment";

	private GridView youHuiDingGouGridView;
	private Context mContext;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.mContext = activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_store, null);
		youHuiDingGouGridView = (GridView) view
				.findViewById(R.id.person_center_packagelist);
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		fetchStoreInfo();
	}

	private void fetchStoreInfo() {
		String api = SimpleRestClient.root_url
				+ "/api/tv/section/youhuidinggou/";
		new IsmartvUrlClient().doRequest(api, new IsmartvUrlClient.CallBack() {
			@Override
			public void onSuccess(String result) {
				Log.d(TAG, "fetchStoreInfo: " + result);
				final YouHuiDingGouEntity youHuiDingGouEntity = new Gson().fromJson(
						result, YouHuiDingGouEntity.class);
				YouHuiDingGouAdapter youHuiDingGouAdapter = new YouHuiDingGouAdapter(
						mContext, youHuiDingGouEntity.getObjects());
				youHuiDingGouGridView.setAdapter(youHuiDingGouAdapter);
				youHuiDingGouGridView
						.setOnItemClickListener(new OnItemClickListener() {

							@Override
							public void onItemClick(AdapterView<?> parent,
									View view, int position, long id) {
								YouHuiDingGouEntity.Object o = youHuiDingGouEntity.getObjects().get(position);
								Intent intent = new Intent();
			                    intent.putExtra("lableString", o.getTitle());
			                    intent.putExtra("itemlistUrl",
			                            o.getUrl());
			                    intent.setClassName("tv.ismar.daisy",
			                            "tv.ismar.daisy.PackageListDetailActivity");
			                    mContext.startActivity(intent);
							}
						});
			}

			@Override
			public void onFailed(Exception exception) {

			}
		});
	}
}
