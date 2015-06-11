package tv.ismar.daisy.ui.fragment;

import java.util.ArrayList;

import tv.ismar.daisy.R;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.data.HomePagerEntity;
import tv.ismar.daisy.data.HomePagerEntity.Carousel;
import tv.ismar.daisy.data.HomePagerEntity.Poster;
import tv.ismar.daisy.exception.NetworkException;
import tv.ismar.daisy.views.LabelImageView;
import tv.ismar.daisy.views.LoadingDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Created by huaijie on 5/18/15.
 */
public class SportFragment extends Fragment {
	private SimpleRestClient mRestClient = new SimpleRestClient();
	private LoadingDialog mLoadingDialog;
	private HomePagerEntity entity;
	private ListView sec_one_list_1;
	private ListView sec_one_list_2;
	private ScheduleAdapter adapter;
	private ArrayList<Carousel> yingchao;
	private ArrayList<Carousel> nba;
	private LabelImageView sport_channel1_image;
	private TextView sport_channel1_subtitle;
	private LabelImageView sport_channel2_image;
	private TextView sport_channel2_subtitle;
	private LabelImageView sport_channel3_image;
	private TextView sport_channel3_subtitle;
	private LabelImageView sport_channel4_image;
	private TextView sport_channel4_subtitle;
	private ImageView sport_channel5;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_sport, null);
		sec_one_list_1 = (ListView) view.findViewById(R.id.sec_one_list_1);
		sec_one_list_2 = (ListView) view.findViewById(R.id.sec_one_list_2);
		mLoadingDialog = new LoadingDialog(getActivity(), getResources()
				.getString(R.string.loading));
		sport_channel1_image = (LabelImageView) view
				.findViewById(R.id.sport_channel1_image);
		sport_channel1_subtitle = (TextView) view
				.findViewById(R.id.sport_channel1_subtitle);
		sport_channel2_image = (LabelImageView) view
				.findViewById(R.id.sport_channel2_image);
		sport_channel2_subtitle = (TextView) view
				.findViewById(R.id.sport_channel2_subtitle);
		sport_channel3_image = (LabelImageView) view
				.findViewById(R.id.sport_channel3_image);
		sport_channel3_subtitle = (TextView) view
				.findViewById(R.id.sport_channel3_subtitle);
		sport_channel4_image = (LabelImageView) view
				.findViewById(R.id.sport_channel4_image);
		sport_channel4_subtitle = (TextView) view
				.findViewById(R.id.sport_channel4_subtitle);
		sport_channel5 = (ImageView) view
				.findViewById(R.id.sport_channel5_image);
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		new FetchDataTask().execute();
		yingchao = new ArrayList<Carousel>();
		nba = new ArrayList<Carousel>();
		adapter = new ScheduleAdapter();
		adapter.setmData(yingchao);
		sec_one_list_1.setAdapter(adapter);
		sec_one_list_2.setAdapter(adapter);
		mLoadingDialog.setOnCancelListener(mLoadingCancelListener);
	}

	private OnCancelListener mLoadingCancelListener = new OnCancelListener() {
		@Override
		public void onCancel(DialogInterface dialog) {
			getActivity().finish();
			dialog.dismiss();
		}
	};

	private void fillData(ArrayList<Carousel> carousellist,
			ArrayList<Poster> postlist) {
		adapter.setmData(carousellist);
		adapter.notifyDataSetChanged();
		sport_channel1_image.setUrl(postlist.get(4).getCustom_image());
		sport_channel1_image.setFocustitle(postlist.get(4).getIntroduction());
		sport_channel1_subtitle.setText(postlist.get(4).getTitle());
		sport_channel2_image.setUrl(postlist.get(5).getCustom_image());
		sport_channel2_image.setFocustitle(postlist.get(5).getIntroduction());
		sport_channel2_subtitle.setText(postlist.get(5).getTitle());
		sport_channel3_image.setUrl(postlist.get(6).getCustom_image());
		sport_channel3_image.setFocustitle(postlist.get(6).getIntroduction());
		sport_channel3_subtitle.setText(postlist.get(6).getTitle());
		sport_channel4_image.setUrl(postlist.get(7).getCustom_image());
		sport_channel4_image.setFocustitle(postlist.get(7).getIntroduction());
		sport_channel4_subtitle.setText(postlist.get(7).getTitle());
		sport_channel1_image.setOnClickListener(ItemClickListener);
		sport_channel1_image.setTag(postlist.get(4));
		sport_channel2_image.setOnClickListener(ItemClickListener);
		sport_channel2_image.setTag(postlist.get(5));
		sport_channel3_image.setOnClickListener(ItemClickListener);
		sport_channel3_image.setTag(postlist.get(6));
		sport_channel4_image.setOnClickListener(ItemClickListener);
		sport_channel4_image.setTag(postlist.get(7));
		sport_channel5.setOnClickListener(ItemClickListener);
	}

	private class ScheduleAdapter extends BaseAdapter {
		private ArrayList<Carousel> mData;
		private LayoutInflater mInflater;

		public ScheduleAdapter() {
			mInflater = (LayoutInflater) getActivity().getSystemService(
					Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return mData.size();
		}

		@Override
		public Carousel getItem(int position) {
			return mData.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = mInflater.inflate(
						R.layout.fragment_sport_listitem, null);
				holder = new ViewHolder();
				holder.view = (LabelImageView) convertView
						.findViewById(R.id.sportschedule);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.view.setUrl(mData.get(position).getVideo_image());
			holder.view.setFocustitle((mData.get(position).getIntroduction()));
			return convertView;
		}

		public ArrayList<Carousel> getmData() {
			return mData;
		}

		public void setmData(ArrayList<Carousel> mData) {
			this.mData = mData;
		}

	}

	public static class ViewHolder {
		public LabelImageView view;
	}

	class FetchDataTask extends AsyncTask<String, Void, Integer> {
		private final static int RESUTL_CANCELED = -2;
		private final static int RESULT_SUCCESS = 0;

		@Override
		protected void onPreExecute() {
			if (mLoadingDialog != null && !mLoadingDialog.isShowing()) {
				mLoadingDialog.show();
			}
			super.onPreExecute();
		}

		@Override
		protected Integer doInBackground(String... params) {
			try {
				entity = mRestClient.getVaietyHome();
			} catch (NetworkException e) {
				e.printStackTrace();
			}
			if (isCancelled()) {
				return RESUTL_CANCELED;
			} else {
				return RESULT_SUCCESS;
			}
		}

		@Override
		protected void onPostExecute(Integer result) {
			if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
				mLoadingDialog.dismiss();
			}
			if (result != RESULT_SUCCESS) {
				return;
			} else {
				ArrayList<Carousel> carousellist = entity.getCarousels();
				ArrayList<Poster> postlist = entity.getPosters();
				fillData(carousellist, postlist);
			}
		}
	}

	private View.OnClickListener ItemClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			Poster poster = (Poster) view.getTag();
			Intent intent = new Intent();
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			if (poster == null) {
				intent.putExtra("title", "华语电影");
				intent.putExtra("url",
						"http://skytest.tvxio.com/v2_0/A21/dto/api/tv/sections/sport/");
				intent.putExtra("channel", "chinesemovie");
				intent.setClassName("tv.ismar.daisy",
						"tv.ismar.daisy.ChannelListActivity");
				getActivity().startActivity(intent);
			} else {
				if ("item".equals(poster.getModel_name())) {
					intent.setClassName("tv.ismar.daisy",
							"tv.ismar.daisy.ItemDetailActivity");
					intent.putExtra("url", poster.getUrl());
					getActivity().startActivity(intent);
				} else if ("topic".equals(poster.getModel_name())) {

				} else if ("section".equals(poster.getModel_name())) {

				} else if ("package".equals(poster.getModel_name())) {

				}
			}
		}
	};
}
