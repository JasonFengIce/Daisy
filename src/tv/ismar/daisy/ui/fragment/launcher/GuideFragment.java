package tv.ismar.daisy.ui.fragment.launcher;

import android.app.Activity;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.gson.Gson;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import org.apache.commons.lang3.StringUtils;
import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import tv.ismar.daisy.R;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.cache.CacheManager;
import tv.ismar.daisy.core.client.DownloadClient;
import tv.ismar.daisy.core.client.IsmartvUrlClient;
import tv.ismar.daisy.core.vlc.MediaWrapper;
import tv.ismar.daisy.core.vlc.MediaWrapperList;
import tv.ismar.daisy.core.vlc.PlaybackService;
import tv.ismar.daisy.core.vlc.PlaybackServiceActivity;
import tv.ismar.daisy.data.HomePagerEntity;
import tv.ismar.daisy.data.HomePagerEntity.Carousel;
import tv.ismar.daisy.ui.fragment.ChannelBaseFragment;
import tv.ismar.daisy.ui.listener.ItemDetailClickListener;
import tv.ismar.daisy.ui.widget.DaisyViewContainer;
import tv.ismar.daisy.ui.widget.HomeItemContainer;
import tv.ismar.daisy.views.LabelImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by huaijie on 5/18/15.
 */
public class GuideFragment extends ChannelBaseFragment implements PlaybackService.Client.Callback,
        PlaybackService.Callback {
    private String TAG = "GuideFragment";

    private static final int START_PLAYBACK = 0x0000;
    private static final int CAROUSEL_NEXT = 0x0010;

    private DaisyViewContainer guideRecommmendList;
    private DaisyViewContainer carouselLayout;


    private ArrayList<String> allVideoUrl;
    private ArrayList<LabelImageView> allItem;

    private ArrayList<Carousel> mCarousels;
    private LabelImageView toppage_carous_imageView1;
    private LabelImageView toppage_carous_imageView2;
    private LabelImageView toppage_carous_imageView3;

    private IsmartvUrlClient datafetch;

    private SurfaceView mSurfaceView;


    private PlaybackServiceActivity.Helper mHelper;
    private PlaybackService mService;
    private List<MediaWrapper> mWrapperList;
    private int mCurrentCarouselIndex = -1;
    private CarouselRepeatType mCarouselRepeatType = CarouselRepeatType.All;


    @Override
    public void onConnected(PlaybackService service) {
        mService = service;
        IVLCVout vlcVout = mService.getVLCVout();
        vlcVout.setVideoView(mSurfaceView);
        vlcVout.attachViews();
//        mHandler.sendEmptyMessage(START_PLAYBACK);
        if (mCarousels == null) {
            fetchHomePage();
        } else {
            playCarousel();
        }
    }

    @Override
    public void onDisconnected() {
        IVLCVout vlcVout = mService.getVLCVout();
        vlcVout.detachViews();
        mService = null;
    }


    @Override
    public void update() {

    }

    @Override
    public void updateProgress() {

    }

    @Override
    public void onMediaEvent(Media.Event event) {

    }

    @Override
    public void onMediaPlayerEvent(MediaPlayer.Event event) {
        switch (event.type) {
            case MediaPlayer.Event.EndReached:
                stopPlayback();
//                mHelper.onStop();
                mHandler.sendEmptyMessage(CAROUSEL_NEXT);
                break;
        }
    }

    @Override
    public void onMediaIndexChange(MediaWrapperList mediaWrapperList, int position) {
//        Log.d(TAG, "onMediaIndexChange position: " + position);
//        for (int i = 0; i < allItem.size(); i++) {
//            LabelImageView imageView = allItem.get(i);
//            if (position != i) {
//                imageView.setCustomfocus(false);
//            } else {
//                imageView.setCustomfocus(true);
//            }
//        }
//
//        HashMap<String, String> hashMap = new HashMap<String, String>();
//        hashMap.put(ItemDetailClickListener.MODEL, mCarousels.get(position).getModel_name());
//        hashMap.put(ItemDetailClickListener.URL, mCarousels.get(position).getUrl());
//        hashMap.put(ItemDetailClickListener.TITLE, mCarousels.get(position).getTitle());
//        mSurfaceView.setTag(hashMap);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    /**
     * vlc
     */


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHelper = new PlaybackServiceActivity.Helper(mContext, this);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = LayoutInflater.from(mContext).inflate(R.layout.fragment_guide, null);
        guideRecommmendList = (DaisyViewContainer) mView.findViewById(R.id.recommend_list);
        carouselLayout = (DaisyViewContainer) mView.findViewById(R.id.carousel_layout);
        toppage_carous_imageView1 = (LabelImageView) mView.findViewById(R.id.toppage_carous_imageView1);
        toppage_carous_imageView2 = (LabelImageView) mView.findViewById(R.id.toppage_carous_imageView2);
        toppage_carous_imageView3 = (LabelImageView) mView.findViewById(R.id.toppage_carous_imageView3);

        mSurfaceView = (SurfaceView) mView.findViewById(R.id.linked_video);
        mSurfaceView.setOnClickListener(new ItemDetailClickListener(mContext));
        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mHelper.onStart();
    }


    @Override
    public void onPause() {
        super.onPause();
        stopPlayback();
        mHelper.onStop();
    }


    @Override
    public void onDetach() {
        super.onDetach();
        if (datafetch != null && datafetch.isAlive())
            datafetch.interrupt();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public void fetchHomePage() {
        String api = SimpleRestClient.root_url + "/api/tv/homepage/top/";
        datafetch = new IsmartvUrlClient();
        datafetch.doRequest(api, new IsmartvUrlClient.CallBack() {
            @Override
            public void onSuccess(String result) {
                HomePagerEntity homePagerEntity = new Gson().fromJson(result,
                        HomePagerEntity.class);
                ArrayList<HomePagerEntity.Carousel> carousels = homePagerEntity
                        .getCarousels();
                ArrayList<HomePagerEntity.Poster> posters = homePagerEntity
                        .getPosters();
                if (!carousels.isEmpty()) {
                    initCarousel(carousels);
                }

                if (!posters.isEmpty()) {
                    initPosters(posters);
                }
                mSurfaceView.requestFocus();
            }

            @Override
            public void onFailed(Exception exception) {
                Log.e(TAG, exception.getMessage());
            }
        });
    }

    private void initPosters(ArrayList<HomePagerEntity.Poster> posters) {
        guideRecommmendList.removeAllViews();
        ArrayList<FrameLayout> imageViews = new ArrayList<FrameLayout>();
        for (int i = 0; i < 8; i++) {
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
            textView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        ((HomeItemContainer) v.getParent())
                                .setDrawBorder(true);
                        ((HomeItemContainer) v.getParent()).invalidate();
                    } else {
                        ((HomeItemContainer) v.getParent())
                                .setDrawBorder(false);
                        ((HomeItemContainer) v.getParent()).invalidate();
                    }
                }
            });
            Picasso.with(mContext).load(posters.get(i).getCustom_image()).memoryPolicy(MemoryPolicy.NO_STORE)
                    .into(itemView);
            textView.setTag(posters.get(i));
            frameLayout.setTag(posters.get(i));
            imageViews.add(frameLayout);
        }
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

        Picasso.with(mContext).load(carousels.get(1).getThumb_image()).memoryPolicy(MemoryPolicy.NO_STORE).into(toppage_carous_imageView2);
        toppage_carous_imageView2.setTag(1);
        toppage_carous_imageView2.setTag(R.drawable.launcher_selector, carousels.get(1));
        toppage_carous_imageView2.setOnClickListener(ItemClickListener);
        toppage_carous_imageView2.setOnFocusChangeListener(itemFocusChangeListener);

        Picasso.with(mContext).load(carousels.get(2).getThumb_image()).memoryPolicy(MemoryPolicy.NO_STORE).into(toppage_carous_imageView3);
        toppage_carous_imageView3.setTag(2);
        toppage_carous_imageView3.setTag(R.drawable.launcher_selector, carousels.get(2));
        toppage_carous_imageView3.setOnClickListener(ItemClickListener);
        toppage_carous_imageView3.setOnFocusChangeListener(itemFocusChangeListener);

        allItem.add(toppage_carous_imageView1);
        allItem.add(toppage_carous_imageView2);
        allItem.add(toppage_carous_imageView3);
        allVideoUrl.add(carousels.get(0).getVideo_url());
        allVideoUrl.add(carousels.get(1).getVideo_url());
        allVideoUrl.add(carousels.get(2).getVideo_url());

        playCarousel();

    }

    private void playCarousel() {
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

        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put(ItemDetailClickListener.MODEL, mCarousels.get(mCurrentCarouselIndex).getModel_name());
        hashMap.put(ItemDetailClickListener.URL, mCarousels.get(mCurrentCarouselIndex).getUrl());
        hashMap.put(ItemDetailClickListener.TITLE, mCarousels.get(mCurrentCarouselIndex).getTitle());
        mSurfaceView.setTag(hashMap);

//        mHelper.onStart();
        mHandler.sendEmptyMessage(START_PLAYBACK);


    }

    private void switchVideo() {
        String videoUrl = CacheManager.getInstance().doRequest(mCarousels.get(mCurrentCarouselIndex).getVideo_url(),
                "guide_" + mCurrentCarouselIndex + ".mp4", DownloadClient.StoreType.Internal);
        Log.d(TAG, "play video: " + videoUrl);
        MediaWrapper mediaWrapper = new MediaWrapper(Uri.parse(videoUrl));
        mediaWrapper.removeFlags(MediaWrapper.MEDIA_FORCE_AUDIO);
        mediaWrapper.addFlags(MediaWrapper.MEDIA_VIDEO);
        mService.load(mediaWrapper);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case START_PLAYBACK:
                    startPlayback();
                    break;
                case CAROUSEL_NEXT:
                    playCarousel();
                    break;
            }

        }
    };


    private void startPlayback() {
        Log.d(TAG, "startPlayback is invoke...");

        mService.addCallback(this);
        switchVideo();
        mService.play();
    }

    private void stopPlayback() {
        mService.removeCallback(this);

        mService.stop();
    }


    private View.OnFocusChangeListener itemFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            boolean focusFlag = true;
            for (ImageView imageView : allItem) {
                focusFlag = focusFlag && (!imageView.isFocused());
            }

            // all view not focus
            if (focusFlag) {
                mCarouselRepeatType = CarouselRepeatType.All;
            } else {
                if (hasFocus) {
                    stopPlayback();
//                    mHelper.onStop();
                    int position = (Integer) v.getTag();
                    mCarouselRepeatType = CarouselRepeatType.Once;
                    mCurrentCarouselIndex = position;
                    playCarousel();
                }
            }
        }
    };

    enum CarouselRepeatType {
        All,
        Once
    }
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



