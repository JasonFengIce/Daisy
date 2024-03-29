package tv.ismar.daisy.ui.fragment.launcher;

import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

import tv.ismar.daisy.R;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.data.HomePagerEntity;
import tv.ismar.daisy.data.HomePagerEntity.Carousel;
import tv.ismar.daisy.data.HomePagerEntity.Poster;
import tv.ismar.daisy.exception.NetworkException;
import tv.ismar.daisy.models.SportGame;
import tv.ismar.daisy.player.InitPlayerTool;
import tv.ismar.daisy.ui.activity.TVGuideActivity;
import tv.ismar.daisy.ui.fragment.ChannelBaseFragment;
import tv.ismar.daisy.ui.fragment.launcher.EntertainmentFragment.FetchDataTask;
import tv.ismar.daisy.utils.PicassoUtils;
import tv.ismar.daisy.views.LabelImageView;

/**
 * Created by huaijie on 5/18/15.
 */
public class SportFragment extends ChannelBaseFragment {
    private static final String TAG = "SportFragment";

    private final int IMAGE_SWITCH_KEY = 0X11;
    private SimpleRestClient mRestClient = new SimpleRestClient();
    //	private LoadingDialog mLoadingDialog;
    private HomePagerEntity entity;
    private ArrayList<SportGame> games;
    private LabelImageView sport_card1;
    private LabelImageView sport_card2;
    private LabelImageView sport_card3;
    private LabelImageView sportspost;
    private LabelImageView sports_live1;
    private LabelImageView sports_live2;
    private LabelImageView sports_live3;
    private LabelImageView sport_channel1_image;
    private TextView sport_channel1_subtitle;
    private LabelImageView sport_channel2_image;
    private TextView sport_channel2_subtitle;
    private LabelImageView sport_channel3_image;
    private TextView sport_channel3_subtitle;
    private LabelImageView sport_channel4_image;
    private TextView sport_channel4_subtitle;
    private tv.ismar.daisy.ui.widget.HomeItemContainer sport_channel5;
    private ImageView arrowUp;
    private ImageView arrowDown;
    private ArrayList<Carousel> looppost = new ArrayList<Carousel>();
    private int loopindex = -1;
    private int currentLiveIndex = 0;
    private FetchDataTask datafetch;
    private InitPlayerTool tool;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sport, null);
//		mLoadingDialog = new LoadingDialog(getActivity(), getResources()
//				.getString(R.string.loading));
        sport_card1 = (LabelImageView) view.findViewById(R.id.sport_card1);
//		sport_card1.setFocusable(false);
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
        sport_channel5 = (tv.ismar.daisy.ui.widget.HomeItemContainer) view
                .findViewById(R.id.listmore);
        arrowUp = (ImageView) view.findViewById(R.id.sec_one_list_1_arrowup);
        arrowDown = (ImageView) view
                .findViewById(R.id.sec_one_list_1_arrowdown);
        sports_live1 = (LabelImageView) view.findViewById(R.id.sports_live1);
        sports_live2 = (LabelImageView) view.findViewById(R.id.sports_live2);
        sports_live3 = (LabelImageView) view.findViewById(R.id.sports_live3);
//        arrowUp.setOnFocusChangeListener(arrowFocusChangeListener);
//        arrowDown.setOnFocusChangeListener(arrowFocusChangeListener);
        arrowUp.setOnHoverListener(onHoverListener);
        arrowDown.setOnHoverListener(onHoverListener);
        // arrowUp.setOnClickListener(arrowClickListener);
        // arrowDown.setOnClickListener(arrowClickListener);
        arrowUp.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
                if (games.size() == 6 && currentLiveIndex == 3) {
                    currentLiveIndex -= 3;
                } else {
                    currentLiveIndex -= 1;
                }
                Message msg = new Message();
                msg.arg1 = 1;
                msg.what = 0;
                test.sendMessage(msg);
			}
		});

        arrowDown.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				 if (games.size() == 6) {
                     currentLiveIndex += 3;
                 } else {
                     currentLiveIndex += 1;
                 }
                 Message msg = new Message();
                 msg.arg1 = 1;
                 msg.what = 0;
                 test.sendMessage(msg);
			}
		});
        sports_live1.setOnClickListener(arrowClickListener);
        sports_live2.setOnClickListener(arrowClickListener);
        sports_live3.setOnClickListener(arrowClickListener);
        sports_live1.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View arg0, boolean arg1) {
                if (!arg1 && arrowUp.isFocused() && !arrowUp.isHovered()) {
                    if (games.size() == 6 && currentLiveIndex == 3) {
                        currentLiveIndex -= 3;
                    } else {
                        currentLiveIndex -= 1;
                    }
                    Message msg = new Message();
                    msg.arg1 = 1;
                    msg.what = 0;
                    test.sendMessage(msg);
                }
                if (arg1) {
                    ((TVGuideActivity) (getActivity())).setLastViewTag("");
                }
            }
        });

        sports_live2.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View arg0, boolean arg1) {
                if (arg1) {
                    ((TVGuideActivity) (getActivity())).setLastViewTag("");
                }
            }
        });

        sports_live3.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View arg0, boolean arg1) {
                if (!arg1 && arrowDown.isFocused()&& !arrowDown.isHovered()) {
                    if (games.size() == 6) {
                        currentLiveIndex += 3;
                    } else {
                        currentLiveIndex += 1;
                    }
                    Message msg = new Message();
                    msg.arg1 = 1;
                    msg.what = 0;
                    test.sendMessage(msg);
                }
                if (arg1) {
                    arrowDown.setFocusable(true);
                    ((TVGuideActivity) (getActivity())).setLastViewTag("");
                }
            }
        });
        sport_channel1_image.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View arg0, boolean arg1) {
                if (arg1) {
                    ((TVGuideActivity) (getActivity())).setLastViewTag("bottom");
                }
            }
        });
        sport_channel5.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View arg0, boolean arg1) {
                if (arg1) {
                    ((TVGuideActivity) (getActivity())).setLastViewTag("bottom");
                    arrowDown.setFocusable(false);
                }
            }
        });

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if(tool != null)
        	tool.removeAsycCallback();
        imageswitch.removeMessages(IMAGE_SWITCH_KEY);
        test.removeMessages(0);
        if (datafetch != null && !datafetch.isCancelled())
            datafetch.cancel(true);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        datafetch = new FetchDataTask();
        datafetch.execute();
        games = new ArrayList<SportGame>();
//		mLoadingDialog.setOnCancelListener(mLoadingCancelListener);
    }

    private OnCancelListener mLoadingCancelListener = new OnCancelListener() {
        @Override
        public void onCancel(DialogInterface dialog) {
            getActivity().finish();
            dialog.dismiss();
        }
    };

    private void fillData(ArrayList<Carousel> carousels, ArrayList<Poster> postlist) {
    	looppost.clear();
        LabelImageView[] sportCards = {sport_card1, sport_card2, sport_card3};
        for (int i = 0; i < 3; i++) {
            PicassoUtils.load(mContext, carousels.get(i).getThumb_image(), sportCards[i]);
            carousels.get(i).setPosition(i);
            sportCards[i].setTag(R.drawable.launcher_selector, carousels.get(i));
            sportCards[i].setOnFocusChangeListener(ItemOnFocusListener);
            sportCards[i].setOnClickListener(ItemClickListener);
            looppost.add(carousels.get(i));
        }
        imageswitch.sendEmptyMessage(IMAGE_SWITCH_KEY);

        LabelImageView[] sportChannelImages = {sport_channel1_image, sport_channel2_image, sport_channel3_image, sport_channel4_image};
        TextView[] sportChannleSubtitles = {sport_channel1_subtitle, sport_channel2_subtitle, sport_channel3_subtitle, sport_channel4_subtitle};
        for (int i = 0; i < 4; i++) {
        	postlist.get(i).setPosition(i);
            PicassoUtils.load(mContext, postlist.get(i).getCustom_image(), sportChannelImages[i]);
            sportChannelImages[i].setFocustitle(postlist.get(i).getIntroduction());
            sportChannleSubtitles[i].setText(postlist.get(i).getTitle());
            sportChannelImages[i].setTag(postlist.get(i));
            sportChannelImages[i].setOnClickListener(ItemClickListener);

        }

        sport_channel5.setOnClickListener(ItemClickListener);
        sportspost.setOnClickListener(ItemClickListener);


        fillLiveData();
        if (scrollFromBorder) {
            if (isRight) {//右侧移入
//	        		if(StringUtils.isNotEmpty(bottomFlag)){
                if ("bottom".equals(bottomFlag)) {//下边界移入
                    sport_channel5.requestFocus();
                } else {//上边界边界移入
                    sports_live1.requestFocus();
                }
//	        		}
            } else {//左侧移入
//	        		if(StringUtils.isNotEmpty(bottomFlag)){
                if ("bottom".equals(bottomFlag)) {
                    sport_channel1_image.requestFocus();
                } else {
                    sport_card1.requestFocus();
                }
//	        	}
            }
            ((TVGuideActivity) getActivity()).resetBorderFocus();
        }
    }

    class FetchDataTask extends AsyncTask<String, Void, Integer> {
        private final static int RESUTL_CANCELED = -2;
        private final static int RESULT_SUCCESS = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(String... params) {
            if (mRestClient!= null){
            try {
                entity = mRestClient.getSportHome(channelEntity.getHomepage_url());
                if ("sport".equals(channelEntity.getChannel())) {
                    games = mRestClient.getSportGames("/api/tv/living_video/sport/");
                } else if ("game".equals(channelEntity.getChannel())) {
                    games = mRestClient.getSportGames("/api/tv/living_video/game/");
                }
            } catch (NetworkException e) {
                e.printStackTrace();
            }
            if (isCancelled()) {
                return RESUTL_CANCELED;
            } else {
                return RESULT_SUCCESS;
            }}

            return RESUTL_CANCELED;
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (result != RESULT_SUCCESS) {
                return;
            } else {
                if (mContext == null || entity == null)
                    return;
                ArrayList<Poster> postlist = entity.getPosters();
                ArrayList<Carousel> carousels = entity.getCarousels();
                fillData(carousels, postlist);
            }
        }
    }

    private View.OnFocusChangeListener ItemOnFocusListener = new View.OnFocusChangeListener() {

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                ((TVGuideActivity) (getActivity())).setLastViewTag("");
                Carousel carousel = (Carousel) v.getTag(R.drawable.launcher_selector);
                PicassoUtils.load(mContext, carousel.getVideo_image(), sportspost);

                sportspost.setTag(R.drawable.launcher_selector, carousel);
                if (StringUtils.isNotEmpty(carousel.getIntroduction())) {
                    sportspost.setFocustitle(carousel.getIntroduction());
                } else {
                    sportspost.setFocustitle(null);
                }
                switch (v.getId()) {
                    case R.id.sport_card1:
                        sport_card1.setCustomfocus(true);
                        sport_card2.setCustomfocus(false);
                        sport_card3.setCustomfocus(false);
                        loopindex = -1;
                        break;
                    case R.id.sport_card2:
                        sport_card1.setCustomfocus(false);
                        sport_card2.setCustomfocus(true);
                        sport_card3.setCustomfocus(false);
                        loopindex = 0;
                        break;
                    case R.id.sport_card3:
                        sport_card1.setCustomfocus(false);
                        sport_card2.setCustomfocus(false);
                        sport_card3.setCustomfocus(true);
                        loopindex = 1;
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
            if(looppost.size() < 3)
            	return;
            PicassoUtils.load(mContext, looppost.get(++loopindex).getVideo_image(), sportspost);
            sportspost.setTag(R.drawable.launcher_selector,
                    looppost.get(loopindex));
            if (StringUtils.isNotEmpty(looppost.get(loopindex)
                    .getIntroduction())) {
                sportspost.setFocustitle(looppost.get(loopindex)
                        .getIntroduction());
            } else {
                sportspost.setFocustitle(null);
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

    private View.OnClickListener arrowClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View arg0) {
            SportGame data = (SportGame) arg0.getTag();
            if(data == null)
            	return;
            if (data.isIs_complex()) {
                Intent intent = new Intent();
                intent.setAction("tv.ismar.daisy.Item");
                intent.putExtra("url", data.getUrl());
                intent.putExtra("channel", channelEntity.getChannel());
                intent.putExtra("fromPage","homepage");
                mContext.startActivity(intent);
            } else {
                tool = new InitPlayerTool(mContext);
                tool.channel=channelEntity.getChannel();
                tool.fromPage="homepage";
                tool.initClipInfo(data.getUrl(),
                        InitPlayerTool.FLAG_URL);
            }
        }
    };

    private View.OnFocusChangeListener arrowFocusChangeListener = new View.OnFocusChangeListener() {

        @Override
        public void onFocusChange(View arg0, boolean arg1) {
            arg0.bringToFront();
        }
    };

    private void fillLiveData() {
        int index = 0;

        LabelImageView[] sportLives = {sports_live1, sports_live2, sports_live3};
		if (currentLiveIndex < 0)
			currentLiveIndex = 0;
        for (int position = currentLiveIndex; position < games.size(); position++) {
//            PicassoUtils.load(mContext, games.get(position).getImageurl(), sportLives[position]);
            switch (index++) {
                case 0:
                    PicassoUtils.load(mContext, games.get(position).getImageurl(), sports_live1);
                    sports_live1.setTag(games.get(position));
                    if (games.get(position).isLiving()) {
                        sports_live1.setModetype(4);
                    } else {
                        sports_live1.setModetype(6);
                    }
                    sports_live1.setFocustitle(games.get(position).getName());
                    break;
                case 1:
                    PicassoUtils.load(mContext, games.get(position).getImageurl(), sports_live2);
                    sports_live2.setTag(games.get(position));
                    if (games.get(position).isLiving()) {
                        sports_live2.setModetype(4);
                    } else {
                        sports_live2.setModetype(6);
                    }
                    sports_live2.setFocustitle(games.get(position).getName());
                    break;
                case 2:
                    PicassoUtils.load(mContext, games.get(position).getImageurl(), sports_live3);

                    sports_live3.setTag(games.get(position));
                    if (games.get(position).isLiving()) {
                        sports_live3.setModetype(4);
                    } else {
                        sports_live3.setModetype(6);
                    }
                    sports_live3.setFocustitle(games.get(position).getName());
                    break;
            }
        }

        if (games.size() - currentLiveIndex > 3) {
            arrowDown.setVisibility(View.VISIBLE);
        } else {
            arrowDown.setVisibility(View.INVISIBLE);
        }
        if (currentLiveIndex > 0) {
            arrowUp.setVisibility(View.VISIBLE);
        } else {
            arrowUp.setVisibility(View.INVISIBLE);
        }
        if (arrowDown.getVisibility() == View.VISIBLE) {
            arrowDown.bringToFront();
        }
        if (arrowUp.getVisibility() == View.VISIBLE) {
            arrowUp.bringToFront();
        }
    }

    private Handler test = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            fillLiveData();
            if (msg.arg1 == 1) {
                sports_live1.requestFocus();
            } else {
                sports_live3.requestFocus();
            }
        }

    };
    
    private View.OnHoverListener onHoverListener = new View.OnHoverListener() {

		@Override
		public boolean onHover(View v, MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_HOVER_ENTER:
			case MotionEvent.ACTION_HOVER_MOVE:
				v.setHovered(true);
				v.setFocusable(true);
				v.setFocusableInTouchMode(true);
				v.requestFocusFromTouch();
				break;
			case MotionEvent.ACTION_HOVER_EXIT:
				v.setHovered(false);
				break;
			}
			return false;
		}
	};

	  public void refreshData(){
		    imageswitch.removeCallbacksAndMessages(null);
		    datafetch = new FetchDataTask();
	        datafetch.execute();
	    }
}
