package tv.ismar.daisy.ui.fragment.launcher;

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

import tv.ismar.daisy.R;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.data.HomePagerEntity;
import tv.ismar.daisy.data.HomePagerEntity.Carousel;
import tv.ismar.daisy.data.HomePagerEntity.Poster;
import tv.ismar.daisy.exception.NetworkException;
import tv.ismar.daisy.models.SportGame;
import tv.ismar.daisy.ui.fragment.ChannelBaseFragment;
import tv.ismar.daisy.views.LabelImageView;
import tv.ismar.daisy.views.LoadingDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by huaijie on 5/18/15.
 */
public class SportFragment extends ChannelBaseFragment implements
		ListView.OnScrollListener {
	private static final String TAG = "SportFragment";

	private final int IMAGE_SWITCH_KEY = 0X11;
	private SimpleRestClient mRestClient = new SimpleRestClient();
	private LoadingDialog mLoadingDialog;
	private HomePagerEntity entity;
	private ArrayList<SportGame> games;
	private ListView sec_one_list;
	private ScheduleAdapter adapter;
	private LabelImageView sport_card1;
	private LabelImageView sport_card2;
	private LabelImageView sport_card3;
	private LabelImageView sportspost;
	private LabelImageView sport_channel1_image;
	private TextView sport_channel1_subtitle;
	private LabelImageView sport_channel2_image;
	private TextView sport_channel2_subtitle;
	private LabelImageView sport_channel3_image;
	private TextView sport_channel3_subtitle;
	private LabelImageView sport_channel4_image;
	private TextView sport_channel4_subtitle;
	private LinearLayout sport_channel5;

	private ImageView arrowUp;
	private ImageView arrowDown;
	private TextView sportspost_title;
	private ArrayList<Carousel> looppost = new ArrayList<Carousel>();
	private int loopindex = 0;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_sport, null);
		sec_one_list = (ListView) view.findViewById(R.id.sec_one_list_2);
		mLoadingDialog = new LoadingDialog(getActivity(), getResources()
				.getString(R.string.loading));
		sport_card1 = (LabelImageView) view.findViewById(R.id.sport_card1);
		sport_card2 = (LabelImageView) view.findViewById(R.id.sport_card2);
		sport_card3 = (LabelImageView) view.findViewById(R.id.sport_card3);
		sportspost = (LabelImageView) view.findViewById(R.id.sportspost);
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
		sport_channel5 = (LinearLayout) view.findViewById(R.id.listmore);
		sportspost_title = (TextView) view.findViewById(R.id.sportspost_title);
		arrowUp = (ImageView) view.findViewById(R.id.sec_one_list_1_arrowup);
		arrowDown = (ImageView) view
				.findViewById(R.id.sec_one_list_1_arrowdown);
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		new FetchDataTask().execute();
		games = new ArrayList<SportGame>();
		adapter = new ScheduleAdapter();
		adapter.setmData(games);
		sec_one_list.setAdapter(adapter);
		sec_one_list.setOnScrollListener(this);
		mLoadingDialog.setOnCancelListener(mLoadingCancelListener);
	}

	private OnCancelListener mLoadingCancelListener = new OnCancelListener() {
		@Override
		public void onCancel(DialogInterface dialog) {
			getActivity().finish();
			dialog.dismiss();
		}
	};

	private void fillData(ArrayList<SportGame> carousellist,
			ArrayList<Carousel> carousels, ArrayList<Poster> postlist) {
		adapter.setmData(carousellist);
		adapter.notifyDataSetChanged();
		sport_card1.setUrl(carousels.get(0).getThumb_image());
		sport_card1.setFocustitle(carousels.get(0).getIntroduction());
		sport_card1.setTag(R.drawable.launcher_selector, carousels.get(0));
		sport_card1.setOnFocusChangeListener(ItemOnFocusListener);
		sport_card2.setUrl(carousels.get(1).getThumb_image());
		sport_card2.setFocustitle(carousels.get(1).getIntroduction());
		sport_card2.setTag(R.drawable.launcher_selector, carousels.get(1));
		sport_card2.setOnFocusChangeListener(ItemOnFocusListener);
		sport_card3.setUrl(carousels.get(2).getThumb_image());
		sport_card3.setFocustitle(carousels.get(2).getIntroduction());
		sport_card3.setTag(R.drawable.launcher_selector, carousels.get(2));
		sport_card3.setOnFocusChangeListener(ItemOnFocusListener);
		looppost.add(carousels.get(0));
		looppost.add(carousels.get(1));
		looppost.add(carousels.get(2));
		imageswitch.sendEmptyMessage(IMAGE_SWITCH_KEY);
		sport_channel1_image.setUrl(postlist.get(0).getCustom_image());
		sport_channel1_image.setFocustitle(postlist.get(0).getIntroduction());
		sport_channel1_subtitle.setText(postlist.get(0).getTitle());
		sport_channel2_image.setUrl(postlist.get(1).getCustom_image());
		sport_channel2_image.setFocustitle(postlist.get(1).getIntroduction());
		sport_channel2_subtitle.setText(postlist.get(1).getTitle());
		sport_channel3_image.setUrl(postlist.get(2).getCustom_image());
		sport_channel3_image.setFocustitle(postlist.get(2).getIntroduction());
		sport_channel3_subtitle.setText(postlist.get(2).getTitle());
		sport_channel4_image.setUrl(postlist.get(3).getCustom_image());
		sport_channel4_image.setFocustitle(postlist.get(3).getIntroduction());
		sport_channel4_subtitle.setText(postlist.get(3).getTitle());
		sport_channel1_image.setOnClickListener(ItemClickListener);
		sport_channel1_image.setTag(postlist.get(0));
		sport_channel2_image.setOnClickListener(ItemClickListener);
		sport_channel2_image.setTag(postlist.get(1));
		sport_channel3_image.setOnClickListener(ItemClickListener);
		sport_channel3_image.setTag(postlist.get(2));
		sport_channel4_image.setOnClickListener(ItemClickListener);
		sport_channel4_image.setTag(postlist.get(3));
		sport_channel5.setOnClickListener(ItemClickListener);
		sport_card1.setOnClickListener(ItemClickListener);
		sport_card2.setOnClickListener(ItemClickListener);
		sport_card3.setOnClickListener(ItemClickListener);
		sportspost.setOnClickListener(ItemClickListener);
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {

	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		int lastVisibleItem = firstVisibleItem + visibleItemCount - 1;
		Log.d(TAG, "firstVisibleItem: " + firstVisibleItem);
		if (firstVisibleItem == 0) {
			arrowUp.setVisibility(View.GONE);
		} else {
			if (arrowUp.getVisibility() != View.VISIBLE) {
				arrowUp.setVisibility(View.VISIBLE);
			}
		}

		if (lastVisibleItem == (totalItemCount - 1)) {
			arrowDown.setVisibility(View.GONE);
		} else {
			if (arrowDown.getVisibility() != View.VISIBLE) {
				arrowDown.setVisibility(View.VISIBLE);
			}
		}

	}

	private class ScheduleAdapter extends BaseAdapter {
		private ArrayList<SportGame> mData;
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
		public SportGame getItem(int position) {
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
			holder.view.setUrl(mData.get(position).getImageurl());
			holder.view.setFocustitle((mData.get(position).getName()));
			holder.view.setModetype(mData.get(position).getGameType());
			return convertView;
		}

		public ArrayList<SportGame> getmData() {
			return mData;
		}

		public void setmData(ArrayList<SportGame> mData) {
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
				entity = mRestClient.getSportHome(channelEntity
						.getHomepage_url());
				if ("sport".equals(channelEntity.getChannel())) {
					games = mRestClient
							.getSportGames("/api/tv/living_video/sport/");
				} else if ("game".equals(channelEntity.getChannel())) {
					games = mRestClient
							.getSportGames("/api/tv/living_video/game/");
				}
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
				ArrayList<Poster> postlist = entity.getPosters();
				ArrayList<Carousel> carousels = entity.getCarousels();
				fillData(games, carousels, postlist);
			}
		}
	}

	private View.OnFocusChangeListener ItemOnFocusListener = new View.OnFocusChangeListener() {

		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (hasFocus) {
				Carousel carousel = (Carousel) v
						.getTag(R.drawable.launcher_selector);
				Picasso.with(context).load(carousel.getVideo_image())
				.into(sportspost);
//				sportspost.setUrl(carousel.getVideo_image());
				sportspost.setTag(R.drawable.launcher_selector, carousel);
				if (StringUtils.isNotEmpty(carousel.getIntroduction())) {
					sportspost_title.setText(carousel.getIntroduction());
					sportspost_title.setVisibility(View.VISIBLE);
				} else {
					sportspost_title.setVisibility(View.GONE);
				}
				switch (v.getId()) {
				case R.id.sport_card1:
					sport_card1.setCustomfocus(true);
					sport_card2.setCustomfocus(false);
					sport_card3.setCustomfocus(false);
					loopindex =-1;
					break;
				case R.id.sport_card2:
					sport_card1.setCustomfocus(false);
					sport_card2.setCustomfocus(true);
					sport_card3.setCustomfocus(false);
					loopindex =0;
					break;
				case R.id.sport_card3:
					sport_card1.setCustomfocus(false);
					sport_card2.setCustomfocus(false);
					sport_card3.setCustomfocus(true);
					loopindex =1;
				default:
					break;
				}
				imageswitch.removeMessages(IMAGE_SWITCH_KEY);
			} else {
				imageswitch.sendEmptyMessageDelayed(IMAGE_SWITCH_KEY, 6000);
			}
		}
	};

	private Handler imageswitch = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Picasso.with(context).load(looppost.get(++loopindex).getVideo_image()).into(sportspost);
//			sportspost.setUrl(looppost.get(++loopindex).getVideo_image());
			sportspost.setTag(R.drawable.launcher_selector, looppost.get(loopindex));
			if (StringUtils.isNotEmpty(looppost.get(loopindex)
					.getIntroduction())) {
				sportspost_title.setText(looppost.get(loopindex)
						.getIntroduction());
				sportspost_title.setVisibility(View.VISIBLE);
			} else {
				sportspost_title.setVisibility(View.GONE);
			}
			if (loopindex == 0) {
				sport_card1.setCustomfocus(true);
				sport_card2.setCustomfocus(false);
				sport_card3.setCustomfocus(false);
			} else if (loopindex == 1) {
				sport_card1.setCustomfocus(false);
				sport_card2.setCustomfocus(true);
				sport_card3.setCustomfocus(false);
			} else if (loopindex == 2) {
				sport_card1.setCustomfocus(false);
				sport_card2.setCustomfocus(false);
				sport_card3.setCustomfocus(true);
			}
			if (loopindex >= 2)
				loopindex = -1;
			imageswitch.sendEmptyMessageDelayed(IMAGE_SWITCH_KEY, 6000);
			// pendingView.requestFocus();
		}
	};
}
