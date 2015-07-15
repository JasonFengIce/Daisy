package tv.ismar.daisy.ui.fragment.launcher;

import java.util.ArrayList;

import tv.ismar.daisy.R;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.data.HomePagerEntity;
import tv.ismar.daisy.data.HomePagerEntity.Carousel;
import tv.ismar.daisy.data.HomePagerEntity.Poster;
import tv.ismar.daisy.exception.NetworkException;
import tv.ismar.daisy.ui.fragment.ChannelBaseFragment;
import tv.ismar.daisy.views.AsyncImageView;
import tv.ismar.daisy.views.LabelImageView;
import tv.ismar.daisy.views.LoadingDialog;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by huaijie on 5/18/15.
 */
public class EntertainmentFragment extends ChannelBaseFragment {

	private final int IMAGE_SWITCH_KEY = 0X11;
	private SimpleRestClient mRestClient = new SimpleRestClient();
	private LoadingDialog mLoadingDialog;

	private AsyncImageView vaiety_post;
	private AsyncImageView vaiety_thumb1;
	private AsyncImageView vaiety_thumb2;
	private AsyncImageView vaiety_thumb3;
	private TextView vaiety_fouce_label;
	private LabelImageView vaiety_card1_image;
	private TextView vaiety_card1_subtitle;
	private LabelImageView vaiety_card2_image;
	private TextView vaiety_card2_subtitle;
	private LabelImageView vaiety_card3_image;
	private TextView vaiety_card3_subtitle;
	private LabelImageView vaiety_card4_image;
	private TextView vaiety_card4_subtitle;
	private LabelImageView vaiety_channel1_image;
	private TextView vaiety_channel1_subtitle;
	private LabelImageView vaiety_channel2_image;
	private TextView vaiety_channel2_subtitle;
	private LabelImageView vaiety_channel3_image;
	private TextView vaiety_channel3_subtitle;
	private LabelImageView vaiety_channel4_image;
	private TextView vaiety_channel4_subtitle;
	private TextView vaiety_channel5;
	private HomePagerEntity entity;
	private ArrayList<String> looppost = new ArrayList<String>();
	private int loopindex = 0;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_entertainment, null);
		mLoadingDialog = new LoadingDialog(getActivity(), getResources()
				.getString(R.string.loading));
		vaiety_post = (AsyncImageView) view.findViewById(R.id.vaiety_post);
		vaiety_thumb1 = (AsyncImageView) view.findViewById(R.id.vaiety_thumb1);
		vaiety_thumb2 = (AsyncImageView) view.findViewById(R.id.vaiety_thumb2);
		vaiety_thumb3 = (AsyncImageView) view.findViewById(R.id.vaiety_thumb3);
		vaiety_fouce_label = (TextView) view
				.findViewById(R.id.vaiety_fouce_label);
		vaiety_card1_image = (LabelImageView) view
				.findViewById(R.id.vaiety_card1_image);
		vaiety_card1_subtitle = (TextView) view
				.findViewById(R.id.vaiety_card1_subtitle);
		vaiety_card2_image = (LabelImageView) view
				.findViewById(R.id.vaiety_card2_image);
		vaiety_card2_subtitle = (TextView) view
				.findViewById(R.id.vaiety_card2_subtitle);
		vaiety_card3_image = (LabelImageView) view
				.findViewById(R.id.vaiety_card3_image);
		vaiety_card3_subtitle = (TextView) view
				.findViewById(R.id.vaiety_card3_subtitle);
		vaiety_card4_image = (LabelImageView) view
				.findViewById(R.id.vaiety_card4_image);
		vaiety_card4_subtitle = (TextView) view
				.findViewById(R.id.vaiety_card4_subtitle);
		vaiety_channel1_image = (LabelImageView) view
				.findViewById(R.id.vaiety_channel1_image);
		vaiety_channel1_subtitle = (TextView) view
				.findViewById(R.id.vaiety_channel1_subtitle);
		vaiety_channel2_image = (LabelImageView) view
				.findViewById(R.id.vaiety_channel2_image);
		vaiety_channel2_subtitle = (TextView) view
				.findViewById(R.id.vaiety_channel2_subtitle);
		vaiety_channel3_image = (LabelImageView) view
				.findViewById(R.id.vaiety_channel3_image);
		vaiety_channel3_subtitle = (TextView) view
				.findViewById(R.id.vaiety_channel3_subtitle);
		vaiety_channel4_image = (LabelImageView) view
				.findViewById(R.id.vaiety_channel4_image);
		vaiety_channel4_subtitle = (TextView) view
				.findViewById(R.id.vaiety_channel4_subtitle);
		vaiety_channel5 = (TextView) view.findViewById(R.id.listmore);
		vaiety_card1_image.setOnClickListener(ItemClickListener);
		vaiety_card2_image.setOnClickListener(ItemClickListener);
		vaiety_card3_image.setOnClickListener(ItemClickListener);
		vaiety_card4_image.setOnClickListener(ItemClickListener);
		vaiety_channel1_image.setOnClickListener(ItemClickListener);
		vaiety_channel2_image.setOnClickListener(ItemClickListener);
		vaiety_channel3_image.setOnClickListener(ItemClickListener);
		vaiety_channel4_image.setOnClickListener(ItemClickListener);
		vaiety_channel5.setOnClickListener(ItemClickListener);
		vaiety_thumb1.setOnClickListener(ItemClickListener);
		vaiety_thumb2.setOnClickListener(ItemClickListener);
		vaiety_thumb3.setOnClickListener(ItemClickListener);
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		new FetchDataTask().execute();
		vaiety_thumb1.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					v.setPadding(0, 0, 0, 0);
					vaiety_thumb2.setPadding(0, -22, 0, 0);
					vaiety_thumb3.setPadding(0, -22, 0, 0);
					if (v.getTag() != null) {
						vaiety_post.setUrl(v.getTag().toString());
						vaiety_fouce_label.setText(v.getTag(R.id.vaiety_post)
								.toString());
					}
					imageswitch.removeMessages(IMAGE_SWITCH_KEY);
				} else {
					v.setPadding(0, -22, 0, 0);
					imageswitch.sendEmptyMessageDelayed(IMAGE_SWITCH_KEY, 6000);
				}
			}
		});
		vaiety_thumb2.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					v.setPadding(0, 0, 0, 0);
					vaiety_thumb1.setPadding(0, -22, 0, 0);
					vaiety_thumb3.setPadding(0, -22, 0, 0);
					vaiety_post.setUrl(v.getTag().toString());
					vaiety_fouce_label.setText(v.getTag(R.id.vaiety_post)
							.toString());
					imageswitch.removeMessages(IMAGE_SWITCH_KEY);
				} else {
					v.setPadding(0, -22, 0, 0);
					imageswitch.sendEmptyMessageDelayed(IMAGE_SWITCH_KEY, 6000);
				}
			}
		});
		vaiety_thumb3.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					v.setPadding(0, 0, 0, 0);
					vaiety_thumb2.setPadding(0, -22, 0, 0);
					vaiety_thumb1.setPadding(0, -22, 0, 0);
					vaiety_post.setUrl(v.getTag().toString());
					vaiety_fouce_label.setText(v.getTag(R.id.vaiety_post)
							.toString());
					imageswitch.removeMessages(IMAGE_SWITCH_KEY);
				} else {
					v.setPadding(0, -22, 0, 0);
					imageswitch.sendEmptyMessageDelayed(IMAGE_SWITCH_KEY, 6000);
				}
			}
		});
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
		// vaiety_post.setUrl(carousellist.get(0).getVideo_image());
		vaiety_thumb1
				.setUrl(carousellist.get(0).getThumb_image());
		vaiety_thumb1.setTag(carousellist.get(0).getVideo_image());
		vaiety_thumb1.setTag(R.id.vaiety_post, carousellist.get(0).getTitle());
		vaiety_thumb1.setTag(R.drawable.launcher_selector,carousellist.get(0));
		vaiety_thumb2.setUrl(carousellist.get(1).getThumb_image());
		vaiety_thumb2.setTag(carousellist.get(1).getVideo_image());
		vaiety_thumb2.setTag(R.id.vaiety_post, carousellist.get(1).getTitle());
		vaiety_thumb2.setTag(R.drawable.launcher_selector,carousellist.get(1));
		vaiety_thumb3.setUrl(carousellist.get(2).getThumb_image());
		vaiety_thumb3.setTag(carousellist.get(2).getVideo_image());
		vaiety_thumb3.setTag(R.id.vaiety_post, carousellist.get(2).getTitle());
		vaiety_thumb3.setTag(R.drawable.launcher_selector,carousellist.get(2));
		looppost.add(carousellist.get(0).getVideo_image());
		looppost.add(carousellist.get(1).getVideo_image());
		looppost.add(carousellist.get(2).getVideo_image());
		imageswitch.sendEmptyMessage(IMAGE_SWITCH_KEY);
		vaiety_fouce_label.setText(carousellist.get(0).getTitle());
		vaiety_card1_image.setUrl(postlist.get(0).getCustom_image());
		vaiety_card1_image.setFocustitle(postlist.get(0).getIntroduction());
		vaiety_card1_image.setTag(postlist.get(0));
		vaiety_card1_subtitle.setText(postlist.get(0).getTitle());
		vaiety_card2_image.setUrl(postlist.get(1).getCustom_image());
		vaiety_card2_image.setFocustitle(postlist.get(1).getIntroduction());
		vaiety_card2_image.setTag(postlist.get(1));
		vaiety_card2_subtitle.setText(postlist.get(1).getTitle());
		vaiety_card3_image.setUrl(postlist.get(2).getCustom_image());
		vaiety_card3_image.setFocustitle(postlist.get(2).getIntroduction());
		vaiety_card3_subtitle.setText(postlist.get(2).getTitle());
		vaiety_card3_image.setTag(postlist.get(2));
		vaiety_card4_image.setUrl(postlist.get(3).getCustom_image());
		vaiety_card4_image.setFocustitle(postlist.get(3).getIntroduction());
		vaiety_card4_subtitle.setText(postlist.get(3).getTitle());
		vaiety_card4_image.setTag(postlist.get(3));
		vaiety_channel1_image.setUrl(postlist.get(4).getCustom_image());
		vaiety_channel1_image.setFocustitle(postlist.get(4).getIntroduction());
		vaiety_channel1_subtitle.setText(postlist.get(4).getTitle());
		vaiety_channel1_image.setTag(postlist.get(4));
		vaiety_channel2_image.setUrl(postlist.get(5).getCustom_image());
		vaiety_channel2_image.setFocustitle(postlist.get(5).getIntroduction());
		vaiety_channel2_subtitle.setText(postlist.get(5).getTitle());
		vaiety_channel2_image.setTag(postlist.get(5));
		vaiety_channel3_image.setUrl(postlist.get(6).getCustom_image());
		vaiety_channel3_image.setFocustitle(postlist.get(6).getIntroduction());
		vaiety_channel3_subtitle.setText(postlist.get(6).getTitle());
		vaiety_channel3_image.setTag(postlist.get(6));
		vaiety_channel4_image.setUrl(postlist.get(7).getCustom_image());
		vaiety_channel4_image.setFocustitle(postlist.get(7).getIntroduction());
		vaiety_channel4_subtitle.setText(postlist.get(7).getTitle());
		vaiety_channel4_image.setTag(postlist.get(7));
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
				entity = mRestClient.getVaietyHome(channelEntity
						.getHomepage_url());
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

	private Handler imageswitch = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			vaiety_post.setUrl(looppost.get(++loopindex));
			if (loopindex == 0) {
				vaiety_thumb1.setPadding(0, 0, 0, 0);
				vaiety_thumb2.setPadding(0, 22, 0, 0);
				vaiety_thumb3.setPadding(0, 22, 0, 0);
				vaiety_fouce_label.setText(vaiety_thumb1.getTag(
						R.id.vaiety_post).toString());
			} else if (loopindex == 1) {
				vaiety_thumb1.setPadding(0, 22, 0, 0);
				vaiety_thumb2.setPadding(0, 0, 0, 0);
				vaiety_thumb3.setPadding(0, 22, 0, 0);
				vaiety_fouce_label.setText(vaiety_thumb2.getTag(
						R.id.vaiety_post).toString());
			} else if (loopindex == 2) {
				vaiety_thumb1.setPadding(0, 22, 0, 0);
				vaiety_thumb2.setPadding(0, 22, 0, 0);
				vaiety_thumb3.setPadding(0, 0, 0, 0);
				vaiety_fouce_label.setText(vaiety_thumb3.getTag(
						R.id.vaiety_post).toString());
			}
			if (loopindex >= 2)
				loopindex = -1;
			imageswitch.sendEmptyMessageDelayed(IMAGE_SWITCH_KEY, 6000);
			// pendingView.requestFocus();
		}
	};
}
