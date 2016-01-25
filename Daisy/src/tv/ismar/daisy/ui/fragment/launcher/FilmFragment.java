package tv.ismar.daisy.ui.fragment.launcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang3.StringUtils;

import tv.ismar.daisy.R;
import tv.ismar.daisy.core.cache.CacheManager;
import tv.ismar.daisy.core.client.DownloadClient;
import tv.ismar.daisy.core.client.IsmartvUrlClient;
import tv.ismar.daisy.data.HomePagerEntity;
import tv.ismar.daisy.data.HomePagerEntity.Carousel;
import tv.ismar.daisy.ui.activity.TVGuideActivity;
import tv.ismar.daisy.ui.fragment.ChannelBaseFragment;
import tv.ismar.daisy.ui.widget.HomeItemContainer;
import tv.ismar.daisy.utils.BitmapDecoder;
import tv.ismar.daisy.utils.HardwareUtils;
import tv.ismar.daisy.views.LabelImageView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by huaijie on 5/18/15.
 */
public class FilmFragment extends ChannelBaseFragment {
    private static final String TAG = "FilmFragment";
    private static final int START_PLAYBACK = 0x0000;
    private static final int CAROUSEL_NEXT = 0x0010;

    private LinearLayout guideRecommmendList;
    private LinearLayout carouselLayout;
    private HomeItemContainer film_post_layout;

    private ImageView linkedVideoImage;
    private TextView film_linked_title;
    private LabelImageView film_lefttop_image;

    private ArrayList<Carousel> mCarousels;
    private ArrayList<LabelImageView> allItem;


    private IsmartvUrlClient datafetch;


    private tv.ismar.daisy.ui.widget.DaisyVideoView mSurfaceView;

    private int mCurrentCarouselIndex = -1;
    private CarouselRepeatType mCarouselRepeatType = CarouselRepeatType.All;

    private String mChannelName;
    private HomeItemContainer morelayout;
    private HomeItemContainer firstpost;
    private LabelImageView firstcarousel;
    private BitmapDecoder bitmapDecoder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = LayoutInflater.from(mContext).inflate(R.layout.fragment_film, null);
        guideRecommmendList = (LinearLayout) mView.findViewById(R.id.film_recommend_list);
        carouselLayout = (LinearLayout) mView.findViewById(R.id.film_carousel_layout);
        mSurfaceView = (tv.ismar.daisy.ui.widget.DaisyVideoView) mView.findViewById(R.id.film_linked_video);
        mSurfaceView.setOnCompletionListener(mOnCompletionListener);
        mSurfaceView.setOnErrorListener(mVideoOnErrorListener);
        mSurfaceView.setOnPreparedListener(mOnPreparedListener);

        film_lefttop_image = (LabelImageView) mView.findViewById(R.id.film_lefttop_image);
        film_post_layout = (HomeItemContainer) mView.findViewById(R.id.film_post_layout);
        linkedVideoImage = (ImageView) mView.findViewById(R.id.film_linked_image);
        film_linked_title = (TextView) mView.findViewById(R.id.film_linked_title);
        film_post_layout.setNextFocusRightId(R.id.filmfragment_firstcarousel);
        film_post_layout.setOnClickListener(ItemClickListener);
        //film_post_layout.requestFocus();
        mSurfaceView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View arg0, boolean arg1) {
                if (arg1)
                    film_post_layout.requestFocus();
            }
        });
        mSurfaceView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                film_post_layout.performClick();
            }
        });
        bitmapDecoder = new BitmapDecoder();
        bitmapDecoder.decode(mContext, R.drawable.guide_video_loading, new BitmapDecoder.Callback() {
            @Override
            public void onSuccess(BitmapDrawable bitmapDrawable) {
                linkedVideoImage.setBackgroundDrawable(bitmapDrawable);
            }
        });
        return mView;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }


    @Override
    public void onDestroyView() {
        mHandler.removeMessages(START_PLAYBACK);
        mHandler.removeMessages(CAROUSEL_NEXT);
        film_post_layout.removeAllViews();
        guideRecommmendList.removeAllViews();
        carouselLayout.removeAllViews();
        guideRecommmendList = null;
        carouselLayout = null;
        film_post_layout = null;
        if (film_lefttop_image != null && film_lefttop_image.getDrawingCache() != null && !film_lefttop_image.getDrawingCache().isRecycled()) {
            film_lefttop_image.getDrawingCache().recycle();
            film_lefttop_image = null;
        }
        super.onDestroyView();

    }


    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
        intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
        intentFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
        intentFilter.addDataScheme("file");
        mContext.registerReceiver(externalStorageReceiver, intentFilter);

        if (mCarousels == null) {
            if (channelEntity != null)
                fetchHomePage(channelEntity.getHomepage_url());
        } else {
            if (!mSurfaceView.isPlaying()) {
                playCarousel(500);
            }
        }
    }


    @Override
    public void onPause() {

        super.onPause();


    }

    @Override
    public void onStop() {
        stopPlayback();
        mContext.unregisterReceiver(externalStorageReceiver);
        super.onStop();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (bitmapDecoder != null && bitmapDecoder.isAlive()) {
            bitmapDecoder.interrupt();
        }
        if (datafetch != null && datafetch.isAlive())
            datafetch.interrupt();
    }

    private void fetchHomePage(String url) {
        mChannelName = getChannelEntity().getChannel();
        datafetch = new IsmartvUrlClient();
        datafetch.doRequest(url, new IsmartvUrlClient.CallBack() {
            @Override
            public void onSuccess(String result) {
                if (mContext == null || guideRecommmendList == null)
                    return;
                HomePagerEntity homePagerEntity = new Gson().fromJson(result, HomePagerEntity.class);
                ArrayList<HomePagerEntity.Poster> posters = homePagerEntity.getPosters();
                ArrayList<HomePagerEntity.Carousel> carousels = homePagerEntity.getCarousels();

                Log.d(TAG, "posters size: " + posters.size());
                Log.d(TAG, "carousels size: " + carousels.size());
                initPosters(posters);
                initCarousel(carousels);
                if (scrollFromBorder) {
                    if (isRight) {//右侧移入
//                		if(StringUtils.isNotEmpty(bottomFlag)){
                        if ("bottom".equals(bottomFlag)) {//下边界移入
                            morelayout.requestFocus();
                        } else {//上边界边界移入
                            firstcarousel.requestFocus();
                        }
//                		}
                    } else {//左侧移入
//                		if(StringUtils.isNotEmpty(bottomFlag)){
                        if ("bottom".equals(bottomFlag)) {
                            firstpost.requestFocus();
                        } else {
                            film_lefttop_image.requestFocus();
                        }
//                	}
                    }
                    ((TVGuideActivity) getActivity()).resetBorderFocus();
                }
            }

            @Override
            public void onFailed(Exception exception) {
            }
        });

    }

    private HomeItemContainer focusView;

    private void initPosters(ArrayList<HomePagerEntity.Poster> posters) {
        if (guideRecommmendList == null)
            return;
        guideRecommmendList.removeAllViews();
        posters.get(0).setPosition(0);
        film_lefttop_image.setUrl(posters.get(0).getCustom_image());
        film_lefttop_image.setFocustitle(posters.get(0).getIntroduction());
        film_lefttop_image.setOnClickListener(ItemClickListener);
        film_lefttop_image.setTag(posters.get(0));
        film_lefttop_image.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View arg0, boolean arg1) {
                if (arg1) {
                    ((TVGuideActivity) (getActivity())).setLastViewTag("");
                }
            }
        });
        mLeftTopView = film_lefttop_image;
        for (int i = 1; i <= posters.size(); i++) {
			if (i > 8)
				break;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(199, 278);
//            params.weight = 1;
//            if (i != 7) {
//            params.setMargins(0, 0, 28, 0);
//            }

            if (i == 6) {
                params.setMargins(0, 0, 27, 0);
            } else if (i == 7) {
                params.setMargins(0, 0, 8, 0);
            } else {
                params.setMargins(0, 0, 28, 0);
            }
            if (mContext == null)
                return;
            ImageView itemView = new ImageView(mContext);
//            itemView.setBackgroundResource(R.drawable.launcher_selector);
            itemView.setFocusable(true);
            itemView.setLayoutParams(params);
            itemView.setOnClickListener(ItemClickListener);
            if (i <= 7) {
                posters.get(i).setPosition(i);
                tv.ismar.daisy.ui.widget.HomeItemContainer frameLayout = (tv.ismar.daisy.ui.widget.HomeItemContainer) LayoutInflater.from(mContext).inflate(R.layout.item_poster, null);
                ImageView postitemView = (ImageView) frameLayout.findViewById(R.id.poster_image);
                TextView textView = (TextView) frameLayout.findViewById(R.id.poster_title);
                if (StringUtils.isNotEmpty(posters.get(i).getIntroduction())) {
                    textView.setText(posters.get(i).getIntroduction());
                    textView.setVisibility(View.VISIBLE);
                }
                textView.setTag(R.id.poster_title, i);
                textView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        Object tagObject = v.getTag(R.id.poster_title);
                        if (hasFocus) {
                            ((HomeItemContainer) v.getParent())
                                    .setDrawBorder(true);
                            ((HomeItemContainer) v.getParent()).invalidate();
                            focusView = ((HomeItemContainer) v.getParent());
                            if (tagObject != null) {
                                int tagindex = Integer.parseInt(tagObject.toString());
                                if (tagindex == 1) {
                                    ((TVGuideActivity) (getActivity())).setLastViewTag("bottom");
                                }
                            }
                        } else {
                            ((HomeItemContainer) v.getParent())
                                    .setDrawBorder(false);
                            ((HomeItemContainer) v.getParent()).invalidate();
                        }
                    }
                });
                textView.setOnClickListener(ItemClickListener);
                textView.setTag(posters.get(i));
                frameLayout.setOnClickListener(ItemClickListener);
                Picasso.with(mContext).load(posters.get(i).getCustom_image()).memoryPolicy(MemoryPolicy.NO_STORE).into(postitemView);
//                textView.setTag(posters.get(i));
                frameLayout.setTag(posters.get(i));
                frameLayout.setLayoutParams(params);
                if (i == 1) {
                    frameLayout.setId(R.id.filmfragment_firstpost);
                }
                if (i == 2) {
                    frameLayout.setId(R.id.filmfragment_secondpost);
                }
                if (i == 3) {
                    frameLayout.setId(R.id.filmfragment_thirdpost);
                }
                guideRecommmendList.addView(frameLayout);
                if (i == 1) {
//                    mLeftBottomView = frameLayout;
                    firstpost = frameLayout;
                }

            } else {
                params.width = 206;
                params.height = 277;
                params.setMargins(0, 0, 0, 0);
                morelayout = (HomeItemContainer) LayoutInflater.from(
                        mContext).inflate(R.layout.toppagelistmorebutton,
                        null);
                morelayout.setLayoutParams(params);
                View view = morelayout.findViewById(R.id.listmore);
                view.setOnClickListener(ItemClickListener);

                mRightBottomView = morelayout;
                guideRecommmendList.addView(morelayout);
                morelayout.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View arg0, boolean arg1) {
                        if (arg1) {
                            ((TVGuideActivity) (getActivity())).setLastViewTag("bottom");
                        }
                    }
                });
            }
        }
    }


    private void initCarousel(final ArrayList<HomePagerEntity.Carousel> carousels) {
        carouselLayout.removeAllViews();
        allItem = new ArrayList<LabelImageView>();
        mCarousels = carousels;

        for (int i = 0; i < carousels.size(); i++) {


            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(206, 86);
            LabelImageView itemView = new LabelImageView(mContext);
            if (i == 0) {
                params.topMargin = 0;
                itemView.setId(R.id.filmfragment_firstcarousel);
                firstcarousel = itemView;
            } else
                params.topMargin = 17;
            if (mContext == null)
                return;
            itemView.setFocusable(true);
            Picasso.with(mContext).load(carousels.get(i).getThumb_image()).memoryPolicy(MemoryPolicy.NO_STORE)
                    .into(itemView);
            itemView.setScaleType(ImageView.ScaleType.FIT_XY);
            itemView.setLayoutParams(params);
            itemView.setTag(i);
            carousels.get(i).setPosition(i);
            itemView.setTag(R.drawable.launcher_selector, carousels.get(i));
            itemView.setOnClickListener(ItemClickListener);
            itemView.setOnFocusChangeListener(itemFocusChangeListener);
            int shadowcolor = mContext.getResources().getColor(R.color.carousel_focus);
            itemView.setFrontcolor(shadowcolor);
            allItem.add(itemView);
            carouselLayout.addView(itemView);

            if (i == 0) {
                mRightTopView = itemView;
            }
        }
        playCarousel(0);
    }


    private void startPlayback() {
        Log.d(TAG, "startPlayback is invoke...");
        linkedVideoImage.setImageResource(R.drawable.guide_video_loading);
        if (mContext != null)
            new BitmapDecoder().decode(mContext, R.drawable.guide_video_loading, new BitmapDecoder.Callback() {
                @Override
                public void onSuccess(BitmapDrawable bitmapDrawable) {
                    linkedVideoImage.setBackgroundDrawable(bitmapDrawable);
                }
            });
        linkedVideoImage.setVisibility(View.VISIBLE);
        mSurfaceView.setFocusable(false);
        mSurfaceView.setFocusableInTouchMode(false);
        String videoName = mChannelName + "_" + mCurrentCarouselIndex + ".mp4";
        String videoPath = CacheManager.getInstance().doRequest(mCarousels.get(mCurrentCarouselIndex).getVideo_url(), videoName, DownloadClient.StoreType.External);
        Log.d(TAG, "current video path ====> " + videoPath);
        mSurfaceView.setVideoPath(videoPath);
        mSurfaceView.start();
        mSurfaceView.setFocusable(true);
        mSurfaceView.setFocusableInTouchMode(true);
    }

    private void stopPlayback() {
        mSurfaceView.pause();
        mSurfaceView.stopPlayback();
    }


    private boolean externalStorageIsEnable() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            try {
                File file = new File(HardwareUtils.getSDCardCachePath(), "/text/test.mp4");
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                file.createNewFile();
                file.delete();
                return true;
            } catch (IOException e) {
                return false;
            }
        } else {
            return false;
        }
    }

    private void playCarousel(int delay) {
        mHandler.removeMessages(CAROUSEL_NEXT);
        if (film_post_layout == null)
            return;
        switch (mCarouselRepeatType) {
            case Once:

                break;
            case All:
                if (mCurrentCarouselIndex == mCarousels.size() - 1) {
                    mCurrentCarouselIndex = 0;
                } else {
                    mCurrentCarouselIndex = mCurrentCarouselIndex + 1;
                }
                break;
        }
        film_post_layout.setTag(R.drawable.launcher_selector, mCarousels.get(mCurrentCarouselIndex));
        for (int i = 0; i < allItem.size(); i++) {
            LabelImageView imageView = allItem.get(i);
            if (mCurrentCarouselIndex != i) {
                imageView.setCustomfocus(false);
            } else {
                imageView.setCustomfocus(true);
            }
        }

        if (!TextUtils.isEmpty(mCarousels.get(mCurrentCarouselIndex).getVideo_url())) {
            if (externalStorageIsEnable()) {
                playVideo(0);
            } else {
                playImage();
            }
        } else {
            playImage();
        }
    }


    private void playImage() {
        if (mSurfaceView.getVisibility() == View.VISIBLE) {
            mSurfaceView.setVisibility(View.GONE);
        }

        if (linkedVideoImage.getVisibility() == View.GONE) {
            linkedVideoImage.setVisibility(View.VISIBLE);
        }


        String url = mCarousels.get(mCurrentCarouselIndex).getVideo_image();
        String intro = mCarousels.get(mCurrentCarouselIndex).getIntroduction();
        if (StringUtils.isNotEmpty(intro)) {
            film_linked_title.setVisibility(View.VISIBLE);
            film_linked_title.setText(intro);
        } else {
            film_linked_title.setVisibility(View.GONE);
        }
        Picasso.with(mContext).load(url).memoryPolicy(MemoryPolicy.NO_STORE).into(linkedVideoImage, new Callback() {
            int pauseTime = Integer.parseInt(mCarousels.get(mCurrentCarouselIndex).getPause_time());

            @Override
            public void onSuccess() {
                mHandler.sendEmptyMessageDelayed(CAROUSEL_NEXT, pauseTime * 1000);
            }

            @Override
            public void onError() {
                mHandler.sendEmptyMessageDelayed(CAROUSEL_NEXT, pauseTime * 1000);
            }
        });
    }

    private void playVideo(int delay) {
        if (mSurfaceView.getVisibility() == View.GONE) {
            mSurfaceView.setVisibility(View.VISIBLE);
        }

        if (linkedVideoImage.getVisibility() == View.VISIBLE) {
            linkedVideoImage.setVisibility(View.GONE);
        }


        String intro = mCarousels.get(mCurrentCarouselIndex).getIntroduction();
        if (StringUtils.isNotEmpty(intro)) {
            film_linked_title.setVisibility(View.VISIBLE);
            film_linked_title.setText(intro);
        } else {
            film_linked_title.setVisibility(View.GONE);
        }
        mHandler.removeMessages(START_PLAYBACK);
        mHandler.sendEmptyMessageDelayed(START_PLAYBACK,delay);
    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case START_PLAYBACK:
                    startPlayback();
                    break;
                case CAROUSEL_NEXT:
                    playCarousel(0);
                    break;
            }

        }
    };

    private BroadcastReceiver externalStorageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            playCarousel(0);
        }
    };

    enum CarouselRepeatType {
        All,
        Once
    }

    private View.OnFocusChangeListener itemFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            boolean focusFlag = true;
            for (ImageView imageView : allItem) {
                focusFlag = focusFlag && (!imageView.isFocused());
            }
            if (hasFocus) {
                ((TVGuideActivity) (getActivity())).setLastViewTag("");
            }
            // all view not focus
            if (focusFlag) {
                mCarouselRepeatType = CarouselRepeatType.All;
            } else {
                if (hasFocus) {
                    stopPlayback();
                    int position = (Integer) v.getTag();
                    mCarouselRepeatType = CarouselRepeatType.Once;
                    mCurrentCarouselIndex = position;
                    playCarousel(0);
                }
            }
        }
    };

    private android.media.MediaPlayer.OnCompletionListener mOnCompletionListener = new android.media.MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(android.media.MediaPlayer mp) {
            stopPlayback();
            mHandler.sendEmptyMessage(CAROUSEL_NEXT);
        }
    };

    private android.media.MediaPlayer.OnErrorListener mVideoOnErrorListener = new android.media.MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(android.media.MediaPlayer mp, int what, int extra) {

            Log.e(TAG, "play video error!!!");

            return true;
        }
    };

    private android.media.MediaPlayer.OnPreparedListener mOnPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            if (bitmapDecoder != null && bitmapDecoder.isAlive()) {
                bitmapDecoder.interrupt();
            }
            linkedVideoImage.setVisibility(View.GONE);
        }
    };

    public void refreshData() {
        fetchHomePage(channelEntity.getHomepage_url());
    }
}

