package tv.ismar.daisy.ui.fragment.launcher;

import java.util.ArrayList;

import tv.ismar.daisy.R;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.data.HomePagerEntity;
import tv.ismar.daisy.data.HomePagerEntity.Carousel;
import tv.ismar.daisy.data.HomePagerEntity.Poster;
import tv.ismar.daisy.exception.NetworkException;
import tv.ismar.daisy.ui.fragment.ChannelBaseFragment;
import tv.ismar.daisy.ui.widget.LinerLayoutContainer;
import tv.ismar.daisy.views.AsyncImageView;
import tv.ismar.daisy.views.LabelImageView;
import tv.ismar.daisy.views.LoadingDialog;
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

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

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
	private LinerLayoutContainer vaiety_card1;
	private LabelImageView vaiety_card1_image;
	private TextView vaiety_card1_subtitle;
	private LinerLayoutContainer vaiety_card2;
	private LabelImageView vaiety_card2_image;
	private TextView vaiety_card2_subtitle;
	private LinerLayoutContainer vaiety_card3;
	private LabelImageView vaiety_card3_image;
	private TextView vaiety_card3_subtitle;
	private LinerLayoutContainer vaiety_card4;
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
	private tv.ismar.daisy.ui.widget.HomeItemContainer vaiety_channel5;
	private HomePagerEntity entity;
	private ArrayList<String> looppost = new ArrayList<String>();
	private int loopindex = 0;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_entertainment, null);
//		mLoadingDialog = new LoadingDialog(getActivity(), getResources()
//				.getString(R.string.loading));
		vaiety_post = (AsyncImageView) view.findViewById(R.id.vaiety_post);
		vaiety_thumb1 = (AsyncImageView) view.findViewById(R.id.vaiety_thumb1);
		vaiety_thumb2 = (AsyncImageView) view.findViewById(R.id.vaiety_thumb2);
		vaiety_thumb3 = (AsyncImageView) view.findViewById(R.id.vaiety_thumb3);
		vaiety_fouce_label = (TextView) view
				.findViewById(R.id.vaiety_fouce_label);
		vaiety_card1 = (LinerLayoutContainer)view.findViewById(R.id.vaiety_card1);
		vaiety_card1_image = (LabelImageView) view
				.findViewById(R.id.vaiety_card1_image);
		vaiety_card1_subtitle = (TextView) view
				.findViewById(R.id.vaiety_card1_subtitle);
		vaiety_card2 = (LinerLayoutContainer)view.findViewById(R.id.vaiety_card2);
		vaiety_card2_image = (LabelImageView) view
				.findViewById(R.id.vaiety_card2_image);
		vaiety_card2_subtitle = (TextView) view
				.findViewById(R.id.vaiety_card2_subtitle);
		vaiety_card3 = (LinerLayoutContainer)view.findViewById(R.id.vaiety_card3);
		vaiety_card3_image = (LabelImageView) view
				.findViewById(R.id.vaiety_card3_image);
		vaiety_card3_subtitle = (TextView) view
				.findViewById(R.id.vaiety_card3_subtitle);
		vaiety_card4 = (LinerLayoutContainer)view.findViewById(R.id.vaiety_card4);
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
		vaiety_channel5 = (tv.ismar.daisy.ui.widget.HomeItemContainer) view
				.findViewById(R.id.listmore);
		vaiety_card1.setOnClickListener(ItemClickListener);
		vaiety_card2.setOnClickListener(ItemClickListener);
		vaiety_card3.setOnClickListener(ItemClickListener);
		vaiety_card4.setOnClickListener(ItemClickListener);
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
					vaiety_thumb2.setPadding(0, 22, 0, -22);
					vaiety_thumb3.setPadding(0, 22, 0, -22);
					if (v.getTag() != null) {
						Picasso.with(mContext).load(v.getTag().toString()).memoryPolicy(MemoryPolicy.NO_STORE)
								.into(vaiety_post);
						vaiety_fouce_label.setText(v.getTag(R.id.vaiety_post)
								.toString());
					}
					imageswitch.removeMessages(IMAGE_SWITCH_KEY);
				} else {
					v.setPadding(0, 22, 0, -22);
					imageswitch.sendEmptyMessageDelayed(IMAGE_SWITCH_KEY, 6000);
				}
			}
		});
		vaiety_thumb2.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					v.setPadding(0, 0, 0, 0);
					vaiety_thumb1.setPadding(0, 22, 0, -22);
					vaiety_thumb3.setPadding(0, 22, 0, -22);
					Picasso.with(mContext).load(v.getTag().toString()).memoryPolicy(MemoryPolicy.NO_STORE).memoryPolicy(MemoryPolicy.NO_STORE)
							.into(vaiety_post);
					vaiety_fouce_label.setText(v.getTag(R.id.vaiety_post)
							.toString());
					imageswitch.removeMessages(IMAGE_SWITCH_KEY);
				} else {
					v.setPadding(0, 22, 0, -22);
					imageswitch.sendEmptyMessageDelayed(IMAGE_SWITCH_KEY, 6000);
				}
			}
		});
		vaiety_thumb3.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					v.setPadding(0, 0, 0, 0);
					vaiety_thumb2.setPadding(0, 22, 0, -22);
					vaiety_thumb1.setPadding(0, 22, 0, -22);
					Picasso.with(mContext).load(v.getTag().toString()).memoryPolicy(MemoryPolicy.NO_STORE)
							.into(vaiety_post);
					vaiety_fouce_label.setText(v.getTag(R.id.vaiety_post)
							.toString());
					imageswitch.removeMessages(IMAGE_SWITCH_KEY);
				} else {
					v.setPadding(0, 22, 0, -22);
					imageswitch.sendEmptyMessageDelayed(IMAGE_SWITCH_KEY, 6000);
				}
			}
		});
//		mLoadingDialog.setOnCancelListener(mLoadingCancelListener);
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
		Picasso.with(mContext).load(carousellist.get(0).getThumb_image()).memoryPolicy(MemoryPolicy.NO_STORE).into(vaiety_thumb1);
		vaiety_thumb1.setTag(carousellist.get(0).getVideo_image());
		vaiety_thumb1.setTag(R.id.vaiety_post, carousellist.get(0).getTitle());
		vaiety_thumb1.setTag(R.drawable.launcher_selector, carousellist.get(0));
		Picasso.with(mContext).load(carousellist.get(1).getThumb_image()).memoryPolicy(MemoryPolicy.NO_STORE).into(vaiety_thumb2);
		vaiety_thumb2.setTag(carousellist.get(1).getVideo_image());
		vaiety_thumb2.setTag(R.id.vaiety_post, carousellist.get(1).getTitle());
		vaiety_thumb2.setTag(R.drawable.launcher_selector, carousellist.get(1));
		Picasso.with(mContext).load(carousellist.get(2).getThumb_image()).memoryPolicy(MemoryPolicy.NO_STORE).into(vaiety_thumb3);
		vaiety_thumb3.setTag(carousellist.get(2).getVideo_image());
		vaiety_thumb3.setTag(R.id.vaiety_post, carousellist.get(2).getTitle());
		vaiety_thumb3.setTag(R.drawable.launcher_selector, carousellist.get(2));
		looppost.add(carousellist.get(0).getVideo_image());
		looppost.add(carousellist.get(1).getVideo_image());
		looppost.add(carousellist.get(2).getVideo_image());
		imageswitch.sendEmptyMessage(IMAGE_SWITCH_KEY);
		vaiety_fouce_label.setText(carousellist.get(0).getTitle());
		Picasso.with(mContext).load(postlist.get(0).getCustom_image()).memoryPolicy(MemoryPolicy.NO_STORE).into(vaiety_card1_image);
		vaiety_card1_image.setFocustitle(postlist.get(0).getIntroduction());
		vaiety_card1.setTag(postlist.get(0));
		vaiety_card1_subtitle.setText(postlist.get(0).getTitle());
		if (postlist.get(0).getCorner() == 2) {
			vaiety_card1_image.setModetype(1);
		} else if (postlist.get(0).getCorner() == 3) {
			vaiety_card1_image.setModetype(2);
		}
		Picasso.with(mContext).load(postlist.get(1).getCustom_image()).memoryPolicy(MemoryPolicy.NO_STORE).into(vaiety_card2_image);
		vaiety_card2_image.setFocustitle(postlist.get(1).getIntroduction());
		vaiety_card2.setTag(postlist.get(1));
		vaiety_card2_subtitle.setText(postlist.get(1).getTitle());
		if (postlist.get(1).getCorner() == 2) {
			vaiety_card2_image.setModetype(1);
		} else if (postlist.get(1).getCorner() == 3) {
			vaiety_card2_image.setModetype(2);
		}
		Picasso.with(mContext).load(postlist.get(2).getCustom_image()).memoryPolicy(MemoryPolicy.NO_STORE).into(vaiety_card3_image);
		vaiety_card3_image.setFocustitle(postlist.get(2).getIntroduction());
		vaiety_card3_subtitle.setText(postlist.get(2).getTitle());
		vaiety_card3.setTag(postlist.get(2));
		if (postlist.get(2).getCorner() == 2) {
			vaiety_card3_image.setModetype(1);
		} else if (postlist.get(2).getCorner() == 3) {
			vaiety_card3_image.setModetype(2);
		}
		Picasso.with(mContext).load(postlist.get(3).getCustom_image()).memoryPolicy(MemoryPolicy.NO_STORE).into(vaiety_card4_image);
		vaiety_card4_image.setFocustitle(postlist.get(3).getIntroduction());
		vaiety_card4_subtitle.setText(postlist.get(3).getTitle());
		vaiety_card4.setTag(postlist.get(3));
		if (postlist.get(3).getCorner() == 2) {
			vaiety_card4_image.setModetype(1);
		} else if (postlist.get(3).getCorner() == 3) {
			vaiety_card4_image.setModetype(2);
		}
		Picasso.with(mContext).load(postlist.get(4).getCustom_image()).memoryPolicy(MemoryPolicy.NO_STORE).into(vaiety_channel1_image);
		vaiety_channel1_image.setFocustitle(postlist.get(4).getIntroduction());
		vaiety_channel1_subtitle.setText(postlist.get(4).getTitle());
		vaiety_channel1_image.setTag(postlist.get(4));
		if (postlist.get(4).getCorner() == 2) {
			vaiety_channel1_image.setModetype(1);
		} else if (postlist.get(4).getCorner() == 3) {
			vaiety_channel1_image.setModetype(2);
		}
		Picasso.with(mContext).load(postlist.get(5).getCustom_image()).memoryPolicy(MemoryPolicy.NO_STORE).into(vaiety_channel2_image);
		vaiety_channel2_image.setFocustitle(postlist.get(5).getIntroduction());
		vaiety_channel2_subtitle.setText(postlist.get(5).getTitle());
		vaiety_channel2_image.setTag(postlist.get(5));
		if (postlist.get(5).getCorner() == 2) {
			vaiety_channel2_image.setModetype(1);
		} else if (postlist.get(5).getCorner() == 3) {
			vaiety_channel2_image.setModetype(2);
		}
		Picasso.with(mContext).load(postlist.get(6).getCustom_image()).memoryPolicy(MemoryPolicy.NO_STORE).into(vaiety_channel3_image);
		vaiety_channel3_image.setFocustitle(postlist.get(6).getIntroduction());
		vaiety_channel3_subtitle.setText(postlist.get(6).getTitle());
		vaiety_channel3_image.setTag(postlist.get(6));
		if (postlist.get(6).getCorner() == 2) {
			vaiety_channel3_image.setModetype(1);
		} else if (postlist.get(6).getCorner() == 3) {
			vaiety_channel3_image.setModetype(2);
		}
		Picasso.with(mContext).load(postlist.get(7).getCustom_image()).memoryPolicy(MemoryPolicy.NO_STORE).into(vaiety_channel4_image);
		vaiety_channel4_image.setFocustitle(postlist.get(7).getIntroduction());
		vaiety_channel4_subtitle.setText(postlist.get(7).getTitle());
		vaiety_channel4_image.setTag(postlist.get(7));
		if (postlist.get(7).getCorner() == 2) {
			vaiety_channel4_image.setModetype(1);
		} else if (postlist.get(7).getCorner() == 3) {
			vaiety_channel4_image.setModetype(2);
		}
	}

	class FetchDataTask extends AsyncTask<String, Void, Integer> {
		private final static int RESUTL_CANCELED = -2;
		private final static int RESULT_SUCCESS = 0;

		@Override
		protected void onPreExecute() {
//			if (mLoadingDialog != null && !mLoadingDialog.isShowing()) {
//				mLoadingDialog.show();
//			}
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
//			if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
//				mLoadingDialog.dismiss();
//			}
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
			Picasso.with(mContext).load(looppost.get(++loopindex)).memoryPolicy(MemoryPolicy.NO_STORE)
					.into(vaiety_post);
			if (loopindex == 0) {
				vaiety_thumb1.setPadding(0, 0, 0, 0);
				vaiety_thumb2.setPadding(0, 22, 0, -22);
				vaiety_thumb3.setPadding(0, 22, 0, -22);
				vaiety_fouce_label.setText(vaiety_thumb1.getTag(
						R.id.vaiety_post).toString());
			} else if (loopindex == 1) {
				vaiety_thumb1.setPadding(0, 22, 0, -22);
				vaiety_thumb2.setPadding(0, 0, 0, 0);
				vaiety_thumb3.setPadding(0, 22, 0, -22);
				vaiety_fouce_label.setText(vaiety_thumb2.getTag(
						R.id.vaiety_post).toString());
			} else if (loopindex == 2) {
				vaiety_thumb1.setPadding(0, 22, 0, -22);
				vaiety_thumb2.setPadding(0, 22, 0, -22);
				vaiety_thumb3.setPadding(0, 0, 0, 0);
				vaiety_fouce_label.setText(vaiety_thumb3.getTag(
						R.id.vaiety_post).toString());
			}
			if (loopindex >= 2)
				loopindex = -1;
			if (imageswitch.hasMessages(IMAGE_SWITCH_KEY))
				imageswitch.removeMessages(IMAGE_SWITCH_KEY);
			imageswitch.sendEmptyMessageDelayed(IMAGE_SWITCH_KEY, 6000);
			// pendingView.requestFocus();
		}
	};

	@Override
	public void onDetach() {
		imageswitch.removeMessages(IMAGE_SWITCH_KEY);
		super.onDetach();
	}
}
