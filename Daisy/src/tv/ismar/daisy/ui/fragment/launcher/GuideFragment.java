package tv.ismar.daisy.ui.fragment.launcher;

import android.R.integer;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.google.gson.Gson;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang3.StringUtils;

import tv.ismar.daisy.R;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.cache.CacheManager;
import tv.ismar.daisy.core.client.CacheHttpClient;
import tv.ismar.daisy.core.client.DownloadClient;
import tv.ismar.daisy.core.client.IsmartvUrlClient;
import tv.ismar.daisy.data.HomePagerEntity;
import tv.ismar.daisy.data.HomePagerEntity.Carousel;
import tv.ismar.daisy.ui.activity.TVGuideActivity;
import tv.ismar.daisy.ui.fragment.ChannelBaseFragment;
import tv.ismar.daisy.ui.widget.DaisyViewContainer;
import tv.ismar.daisy.ui.widget.HomeItemContainer;
import tv.ismar.daisy.utils.BitmapDecoder;
import tv.ismar.daisy.views.LabelImageView;

import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by huaijie on 5/18/15.
 */
public class GuideFragment extends ChannelBaseFragment {
    private String TAG = "GuideFragment";

    private static final int START_PLAYBACK = 0x0000;
    private static final int CAROUSEL_NEXT = 0x0010;

    private DaisyViewContainer guideRecommmendList;
    private DaisyViewContainer carouselLayout;


    private ArrayList<String> allVideoUrl;
    private ArrayList<LabelImageView> allItem;
    private HomeItemContainer film_post_layout;
    private ArrayList<Carousel> mCarousels;
    private LabelImageView toppage_carous_imageView1;
    private LabelImageView toppage_carous_imageView2;
    private LabelImageView toppage_carous_imageView3;
    private HomeItemContainer lastpostview;
    private IsmartvUrlClient datafetch;
    private BitmapDecoder bitmapDecoder;
    private tv.ismar.daisy.ui.widget.DaisyVideoView mSurfaceView;


    private int mCurrentCarouselIndex = -1;
    private CarouselRepeatType mCarouselRepeatType = CarouselRepeatType.All;

    private ImageView linkedVideoLoadingImage;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View mView = LayoutInflater.from(mContext).inflate(R.layout.fragment_guide, null);
        guideRecommmendList = (DaisyViewContainer) mView.findViewById(R.id.recommend_list);
        carouselLayout = (DaisyViewContainer) mView.findViewById(R.id.carousel_layout);
        toppage_carous_imageView1 = (LabelImageView) mView.findViewById(R.id.toppage_carous_imageView1);
        toppage_carous_imageView2 = (LabelImageView) mView.findViewById(R.id.toppage_carous_imageView2);
        toppage_carous_imageView3 = (LabelImageView) mView.findViewById(R.id.toppage_carous_imageView3);
        film_post_layout = (HomeItemContainer) mView.findViewById(R.id.guide_center_layoutview);
        linkedVideoLoadingImage = (ImageView) mView.findViewById(R.id.linked_video_loading_image);

        mSurfaceView = (tv.ismar.daisy.ui.widget.DaisyVideoView) mView.findViewById(R.id.linked_video);
        mSurfaceView.setOnCompletionListener(videoPlayEndListener);
        mSurfaceView.setOnErrorListener(mVideoOnErrorListener);
        mSurfaceView.setOnPreparedListener(mOnPreparedListener);
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
        film_post_layout.setOnClickListener(ItemClickListener);

        mLeftTopView = mSurfaceView;
        mRightTopView = toppage_carous_imageView1;

        bitmapDecoder = new BitmapDecoder();
        bitmapDecoder.decode(mContext, R.drawable.guide_video_loading, new BitmapDecoder.Callback() {
            @Override
            public void onSuccess(BitmapDrawable bitmapDrawable) {
                linkedVideoLoadingImage.setBackgroundDrawable(bitmapDrawable);
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
        mHandler.removeMessages(CAROUSEL_NEXT);
        mHandler.removeMessages(START_PLAYBACK);
        guideRecommmendList.removeAllViews();
        guideRecommmendList = null;
        mSurfaceView = null;
        toppage_carous_imageView1 = null;
        toppage_carous_imageView2 = null;
        toppage_carous_imageView3 = null;
        if (linkedVideoLoadingImage != null && linkedVideoLoadingImage.getDrawingCache() != null && !linkedVideoLoadingImage.getDrawingCache().isRecycled()) {
            linkedVideoLoadingImage.getDrawingCache().recycle();
        }
        super.onDestroyView();

    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCarousels == null) {
            fetchHomePage();
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


    public void fetchHomePage() {
        String api = SimpleRestClient.root_url + "/api/tv/homepage/top/";

        new CacheHttpClient().doRequest(api, new CacheHttpClient.Callback() {
            @Override
            public void onSuccess(String result) {
                String s = result;
                if (mContext == null || guideRecommmendList == null)
                    return;
                HomePagerEntity homePagerEntity = new Gson().fromJson(result, HomePagerEntity.class);
                ArrayList<HomePagerEntity.Carousel> carousels = homePagerEntity.getCarousels();
                ArrayList<HomePagerEntity.Poster> posters = homePagerEntity.getPosters();

                if (!carousels.isEmpty()) {
                    initCarousel(carousels);
                }

                if (!posters.isEmpty()) {
                    initPosters(posters);
                }
                if (scrollFromBorder) {
                    if (isRight) {//右侧移入
//                		if(StringUtils.isNotEmpty(bottomFlag)){
                        if ("bottom".equals(bottomFlag)) {//下边界移入
                            lastpostview.findViewById(R.id.poster_title).requestFocus();
                        } else {//上边界边界移入
                            toppage_carous_imageView1.requestFocus();
                        }
//                		}
                    } else {//左侧移入
                        if (StringUtils.isNotEmpty(bottomFlag)) {
                            if ("bottom".equals(bottomFlag)) {

                            } else {

                            }
                        }
                    }
                    ((TVGuideActivity) getActivity()).resetBorderFocus();
                }
            }

            @Override
            public void onFailed(String error) {
                Log.e(TAG, error);
            }
        });

//        datafetch = new IsmartvUrlClient();
//        datafetch.doRequest(api, new IsmartvUrlClient.CallBack() {
//            @Override
//            public void onSuccess(String result) {
//                if (mContext == null || guideRecommmendList == null)
//                    return;
//                HomePagerEntity homePagerEntity = new Gson().fromJson(result,
//                        HomePagerEntity.class);
//                ArrayList<HomePagerEntity.Carousel> carousels = homePagerEntity
//                        .getCarousels();
//                ArrayList<HomePagerEntity.Poster> posters = homePagerEntity
//                        .getPosters();
//                if (!carousels.isEmpty()) {
//                    initCarousel(carousels);
//                }
//
//                if (!posters.isEmpty()) {
//                    initPosters(posters);
//                }
//                if (scrollFromBorder) {
//                    if (isRight) {//右侧移入
////                		if(StringUtils.isNotEmpty(bottomFlag)){
//                        if ("bottom".equals(bottomFlag)) {//下边界移入
//                            lastpostview.findViewById(R.id.poster_title).requestFocus();
//                        } else {//上边界边界移入
//                            toppage_carous_imageView1.requestFocus();
//                        }
////                		}
//                    } else {//左侧移入
//                        if (StringUtils.isNotEmpty(bottomFlag)) {
//                            if ("bottom".equals(bottomFlag)) {
//
//                            } else {
//
//                            }
//                        }
//                    }
//                    ((TVGuideActivity) getActivity()).resetBorderFocus();
//                }
//            }
//
//            @Override
//            public void onFailed(Exception exception) {
//                Log.e(TAG, exception.getMessage());
//            }
//        });
    }

    private void initPosters(ArrayList<HomePagerEntity.Poster> posters) {
        guideRecommmendList.removeAllViews();
        ArrayList<FrameLayout> imageViews = new ArrayList<FrameLayout>();
        for (int i = 0; i < 8; i++) {
            if (mContext == null) {
                return;
            }
            tv.ismar.daisy.ui.widget.HomeItemContainer frameLayout = (tv.ismar.daisy.ui.widget.HomeItemContainer) LayoutInflater
                    .from(mContext).inflate(R.layout.item_poster, null);
            ImageView itemView = (ImageView) frameLayout
                    .findViewById(R.id.poster_image);
            TextView textView = (TextView) frameLayout
                    .findViewById(R.id.poster_title);
            if (StringUtils.isNotEmpty(posters.get(i).getIntroduction())) {
                textView.setText(posters.get(i).getIntroduction());
                textView.setVisibility(View.VISIBLE);
            } else {
                frameLayout.setFocusable(true);
                frameLayout.setClickable(true);
            }
            textView.setOnClickListener(ItemClickListener);
            frameLayout.setOnClickListener(ItemClickListener);
            textView.setTag(R.id.poster_title, i);
            textView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    Object tagObject = v.getTag(R.id.poster_title);
                    if (hasFocus) {
                        ((HomeItemContainer) v.getParent())
                                .setDrawBorder(true);
                        ((HomeItemContainer) v.getParent()).invalidate();
                        if (tagObject != null) {
                            int tagindex = Integer.parseInt(tagObject.toString());
                            if (tagindex == 0 || tagindex == 7) {
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

            Picasso.with(mContext).load(posters.get(i).getCustom_image()).memoryPolicy(MemoryPolicy.NO_STORE)
                    .into(itemView);
            posters.get(i).setPosition(i);
            textView.setTag(posters.get(i));
            frameLayout.setTag(posters.get(i));
            if (i == 0) {
                frameLayout.setId(R.id.guidefragment_firstpost);
            }
            if (i == 7) {
                frameLayout.setId(R.id.guidefragment_lastpost);
                lastpostview = frameLayout;
            }
            imageViews.add(frameLayout);
            switch (i) {
                case 0:
                    mLeftBottomView = frameLayout;
                    break;
                case 7:
                    mRightBottomView = frameLayout;
                    break;
            }
        }

        guideRecommmendList.setFocusable(true);
        guideRecommmendList.setFocusableInTouchMode(true);
        guideRecommmendList.addAllViews(imageViews);

    }

    private void initCarousel(final ArrayList<HomePagerEntity.Carousel> carousels) {
        mCarousels = carousels;
        allItem = new ArrayList<LabelImageView>();
        allVideoUrl = new ArrayList<String>();


        Picasso.with(mContext).load(carousels.get(0).getThumb_image()).memoryPolicy(MemoryPolicy.NO_STORE).into(toppage_carous_imageView1);
        toppage_carous_imageView1.setTag(0);
        toppage_carous_imageView1.setTag(R.drawable.launcher_selector, carousels.get(0));
        toppage_carous_imageView1.setOnClickListener(ItemClickListener);
        toppage_carous_imageView1.setOnFocusChangeListener(itemFocusChangeListener);
        carousels.get(0).setPosition(0);
        Picasso.with(mContext).load(carousels.get(1).getThumb_image()).memoryPolicy(MemoryPolicy.NO_STORE).into(toppage_carous_imageView2);
        toppage_carous_imageView2.setTag(1);
        toppage_carous_imageView2.setTag(R.drawable.launcher_selector, carousels.get(1));
        toppage_carous_imageView2.setOnClickListener(ItemClickListener);
        toppage_carous_imageView2.setOnFocusChangeListener(itemFocusChangeListener);
        carousels.get(1).setPosition(1);
        Picasso.with(mContext).load(carousels.get(2).getThumb_image()).memoryPolicy(MemoryPolicy.NO_STORE).into(toppage_carous_imageView3);
        toppage_carous_imageView3.setTag(2);
        toppage_carous_imageView3.setTag(R.drawable.launcher_selector, carousels.get(2));
        toppage_carous_imageView3.setOnClickListener(ItemClickListener);
        toppage_carous_imageView3.setOnFocusChangeListener(itemFocusChangeListener);
        carousels.get(2).setPosition(2);
        allItem.add(toppage_carous_imageView1);
        allItem.add(toppage_carous_imageView2);
        allItem.add(toppage_carous_imageView3);
        allVideoUrl.add(carousels.get(0).getVideo_url());
        allVideoUrl.add(carousels.get(1).getVideo_url());
        allVideoUrl.add(carousels.get(2).getVideo_url());

        playCarousel(0);

    }

    private void playCarousel(int delay) {
        mHandler.removeMessages(CAROUSEL_NEXT);
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

        for (int i = 0; i < allItem.size(); i++) {
            LabelImageView imageView = allItem.get(i);
            if (mCurrentCarouselIndex != i) {
                imageView.setCustomfocus(false);
            } else {
                imageView.setCustomfocus(true);
            }
        }

//        HashMap<String, String> hashMap = new HashMap<String, String>();
//        hashMap.put(ItemDetailClickListener.MODEL, mCarousels.get(mCurrentCarouselIndex).getModel_name());
//        hashMap.put(ItemDetailClickListener.URL, mCarousels.get(mCurrentCarouselIndex).getUrl());
//        hashMap.put(ItemDetailClickListener.TITLE, mCarousels.get(mCurrentCarouselIndex).getTitle());
        film_post_layout.setTag(R.drawable.launcher_selector, mCarousels.get(mCurrentCarouselIndex));

//        mHelper.onStart();
        mHandler.removeMessages(START_PLAYBACK);
        mHandler.sendEmptyMessageDelayed(START_PLAYBACK, delay);


    }

//    private void switchVideo() {
//        if (mContext == null)
//            return;
//        String videoUrl = CacheManager.getInstance().doRequest(mCarousels.get(mCurrentCarouselIndex).getVideo_url(),
//                "guide_" + mCurrentCarouselIndex + ".mp4", DownloadClient.StoreType.Internal);
//        Log.d(TAG, "play video: " + videoUrl);
//        MediaWrapper mediaWrapper = new MediaWrapper(Uri.parse(videoUrl));
//        mediaWrapper.removeFlags(MediaWrapper.MEDIA_FORCE_AUDIO);
//        mediaWrapper.addFlags(MediaWrapper.MEDIA_VIDEO);
//        mService.load(mediaWrapper);
//    }

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


    private void startPlayback() {
        if (mSurfaceView == null)
            return;
        stopPlayback();
        Log.d(TAG, "startPlayback is invoke...");
        linkedVideoLoadingImage.setVisibility(View.VISIBLE);
        mSurfaceView.setFocusable(false);
        mSurfaceView.setFocusableInTouchMode(false);
        String videoName = "guide_" + mCurrentCarouselIndex + ".mp4";
        String videoPath = CacheManager.getInstance().doRequest(mCarousels.get(mCurrentCarouselIndex).getVideo_url(), videoName, DownloadClient.StoreType.Internal);
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
                if (hasFocus && !v.isHovered()) {
//                    mHelper.onStop();
                    int position = (Integer) v.getTag();
                    mCarouselRepeatType = CarouselRepeatType.Once;
                    mCurrentCarouselIndex = position;
                    playCarousel(100);
                }
            }
        }
    };


    enum CarouselRepeatType {
        All,
        Once
    }


    private android.media.MediaPlayer.OnCompletionListener videoPlayEndListener = new android.media.MediaPlayer.OnCompletionListener() {

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
            linkedVideoLoadingImage.setVisibility(View.GONE);
        }
    };

}

class Flag {

    private ChangeCallback changeCallback;

    public Flag(ChangeCallback changeCallback) {
        this.changeCallback = changeCallback;
    }

    private int position;

    public void setPosition(int position) {
        this.position = position;
        changeCallback.change(position);

    }

    public int getPosition() {
        return position;
    }

    public interface ChangeCallback {
        void change(int position);
    }
}



