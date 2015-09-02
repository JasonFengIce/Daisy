package tv.ismar.daisy.ui.fragment.launcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
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
import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import tv.ismar.daisy.R;
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
import tv.ismar.daisy.ui.widget.HomeItemContainer;
import tv.ismar.daisy.utils.HardwareUtils;
import tv.ismar.daisy.views.LabelImageView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by huaijie on 5/18/15.
 */
public class FilmFragment extends ChannelBaseFragment implements PlaybackService.Client.Callback,
        PlaybackService.Callback {
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


    private SurfaceView mSurfaceView;
    private PlaybackServiceActivity.Helper mHelper;
    private PlaybackService mService;

    private int mCurrentCarouselIndex = -1;
    private CarouselRepeatType mCarouselRepeatType = CarouselRepeatType.All;

    private String mChannelName;


    @Override
    public void onConnected(PlaybackService service) {
        mService = service;
        if(mCarousels == null){
        fetchHomePage(channelEntity.getHomepage_url());
        }else{
        	playCarousel();
        }
//        mHandler.sendEmptyMessage(START_PLAYBACK);
    }

    @Override
    public void onDisconnected() {
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
                mHandler.sendEmptyMessage(CAROUSEL_NEXT);
                break;
        }

    }

    @Override
    public void onMediaIndexChange(MediaWrapperList mediaWrapperList, int position) {

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHelper = new PlaybackServiceActivity.Helper(mContext, this);
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
        intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
        intentFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
        intentFilter.addDataScheme("file");
        mContext.registerReceiver(externalStorageReceiver, intentFilter);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = LayoutInflater.from(mContext).inflate(R.layout.fragment_film, null);
        guideRecommmendList = (LinearLayout) mView.findViewById(R.id.film_recommend_list);
        carouselLayout = (LinearLayout) mView.findViewById(R.id.film_carousel_layout);
        mSurfaceView = (SurfaceView) mView.findViewById(R.id.film_linked_video);
        film_lefttop_image = (LabelImageView) mView.findViewById(R.id.film_lefttop_image);
        film_post_layout = (HomeItemContainer) mView.findViewById(R.id.film_post_layout);
        linkedVideoImage = (ImageView) mView.findViewById(R.id.film_linked_image);
        film_linked_title = (TextView) mView.findViewById(R.id.film_linked_title);

        return mView;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }

    @Override
    public void onResume() {
        super.onResume();
        mHelper.onStart();
//        if(focusView!=null){
//
//            focusView.setDrawBorder(true);
//            focusView.invalidate();
//        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeMessages(CAROUSEL_NEXT);
        stopPlayback();
        mHelper.onStop();

    }

    @Override
    public void onStop() {
        super.onStop();
        mContext.unregisterReceiver(externalStorageReceiver);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (datafetch != null && datafetch.isAlive())
            datafetch.interrupt();
    }

    private void fetchHomePage(String url) {
        mChannelName = getChannelEntity().getChannel();
        datafetch = new IsmartvUrlClient();
        datafetch.doRequest(url, new IsmartvUrlClient.CallBack() {
            @Override
            public void onSuccess(String result) {
                HomePagerEntity homePagerEntity = new Gson().fromJson(result, HomePagerEntity.class);
                ArrayList<HomePagerEntity.Poster> posters = homePagerEntity.getPosters();
                ArrayList<HomePagerEntity.Carousel> carousels = homePagerEntity.getCarousels();

                Log.d(TAG, "posters size: " + posters.size());
                Log.d(TAG, "carousels size: " + carousels.size());

                initPosters(posters);
                initCarousel(carousels);
            }

            @Override
            public void onFailed(Exception exception) {
                Log.e(TAG, exception.getMessage());
            }
        });

    }
    private HomeItemContainer focusView;
    private void initPosters(ArrayList<HomePagerEntity.Poster> posters) {
        guideRecommmendList.removeAllViews();
        film_lefttop_image.setUrl(posters.get(0).getCustom_image());
        film_lefttop_image.setFocustitle(posters.get(0).getIntroduction());
        film_lefttop_image.setOnClickListener(ItemClickListener);
        film_lefttop_image.setTag(posters.get(0));
        for (int i = 1; i <= posters.size(); i++) {
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
            ImageView itemView = new ImageView(mContext);
//            itemView.setBackgroundResource(R.drawable.launcher_selector);
            itemView.setFocusable(true);
            itemView.setLayoutParams(params);
            itemView.setOnClickListener(ItemClickListener);
            if (i <= 7) {
                tv.ismar.daisy.ui.widget.HomeItemContainer frameLayout = (tv.ismar.daisy.ui.widget.HomeItemContainer) LayoutInflater.from(mContext).inflate(R.layout.item_poster, null);
                ImageView postitemView = (ImageView) frameLayout.findViewById(R.id.poster_image);
                TextView textView = (TextView) frameLayout.findViewById(R.id.poster_title);
                if (StringUtils.isNotEmpty(posters.get(i).getIntroduction())) {
                    textView.setText(posters.get(i).getIntroduction());
                    textView.setVisibility(View.VISIBLE);
                }
                textView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {
                            ((HomeItemContainer) v.getParent())
                                    .setDrawBorder(true);
                            ((HomeItemContainer) v.getParent()).invalidate();
                            focusView = ((HomeItemContainer) v.getParent());
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
                guideRecommmendList.addView(frameLayout);



            } else {
                params.width = 206;
                params.height = 277;
                params.setMargins(0, 0, 0, 0);
                tv.ismar.daisy.ui.widget.HomeItemContainer morelayout = (tv.ismar.daisy.ui.widget.HomeItemContainer) LayoutInflater.from(
                        mContext).inflate(R.layout.toppagelistmorebutton,
                        null);
                morelayout.setLayoutParams(params);
                View view = morelayout.findViewById(R.id.listmore);
                view.setOnClickListener(ItemClickListener);
                guideRecommmendList.addView(morelayout);
            }
        }
    }


    private void initCarousel(final ArrayList<HomePagerEntity.Carousel> carousels) {
        carouselLayout.removeAllViews();
        allItem = new ArrayList<LabelImageView>();
        mCarousels = carousels;

        for (int i = 0; i < carousels.size(); i++) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(206, 86);
            if (i == 0)
                params.topMargin = 0;
            else
                params.topMargin = 17;
            LabelImageView itemView = new LabelImageView(mContext);
            itemView.setFocusable(true);
            Picasso.with(mContext).load(carousels.get(i).getThumb_image()).memoryPolicy(MemoryPolicy.NO_STORE)
                    .into(itemView);
            itemView.setScaleType(ImageView.ScaleType.FIT_XY);
            itemView.setLayoutParams(params);
            itemView.setTag(i);
            itemView.setTag(R.drawable.launcher_selector, carousels.get(i));
            itemView.setOnClickListener(ItemClickListener);
            itemView.setOnFocusChangeListener(itemFocusChangeListener);
            int shadowcolor = mContext.getResources().getColor(R.color.carousel_focus);
            itemView.setFrontcolor(shadowcolor);
            allItem.add(itemView);
            carouselLayout.addView(itemView);
        }
        playCarousel();
    }


    private void startPlayback() {
        Log.d(TAG, "startPlayback is invoke...");
        IVLCVout vlcVout = mService.getVLCVout();
        vlcVout.setVideoView(mSurfaceView);
        vlcVout.attachViews();
        mService.addCallback(this);
        switchVideo();
        mService.play();
    }

    private void stopPlayback() {
        if (mService != null) {
            mService.removeCallback(this);
            IVLCVout vlcVout = mService.getVLCVout();
            vlcVout.detachViews();
            mService.stop();
        }
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
            film_post_layout.setTag(R.drawable.launcher_selector, mCarousels.get(i));
            LabelImageView imageView = allItem.get(i);
            if (mCurrentCarouselIndex != i) {
                imageView.setCustomfocus(false);
            } else {
                imageView.setCustomfocus(true);
            }
        }

        if (!TextUtils.isEmpty(mCarousels.get(mCurrentCarouselIndex).getVideo_url())) {
            if (externalStorageIsEnable()) {
                playVideo();
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


        if (mService != null && mService.isVideoPlaying()) {
            stopPlayback();
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

    private void playVideo() {
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
        mHandler.sendEmptyMessage(START_PLAYBACK);
    }


    private void switchVideo() {
        String videoUrl = CacheManager.getInstance().doRequest(mCarousels.get(mCurrentCarouselIndex).getVideo_url(),
                mChannelName + "_" + mCurrentCarouselIndex + ".mp4", DownloadClient.StoreType.External);
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

    private BroadcastReceiver externalStorageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            playCarousel();
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

            // all view not focus
            if (focusFlag) {
                mCarouselRepeatType = CarouselRepeatType.All;
            } else {
                if (hasFocus) {
                    stopPlayback();
                    int position = (Integer) v.getTag();
                    mCarouselRepeatType = CarouselRepeatType.Once;
                    mCurrentCarouselIndex = position;
                    playCarousel();
                }
            }
        }
    };

}

