package tv.ismar.daisy.ui.fragment.launcher;

import android.app.Activity;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;
import com.activeandroid.query.Select;
import com.google.gson.Gson;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import org.apache.commons.lang3.StringUtils;
import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import tv.ismar.daisy.R;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.client.DownloadClient;
import tv.ismar.daisy.core.client.DownloadThreadPool;
import tv.ismar.daisy.core.client.IsmartvUrlClient;
import tv.ismar.daisy.core.vlc.MediaWrapper;
import tv.ismar.daisy.core.vlc.MediaWrapperList;
import tv.ismar.daisy.core.vlc.PlaybackService;
import tv.ismar.daisy.core.vlc.PlaybackServiceActivity;
import tv.ismar.daisy.data.HomePagerEntity;
import tv.ismar.daisy.data.HomePagerEntity.Carousel;
import tv.ismar.daisy.data.table.DownloadTable;
import tv.ismar.daisy.ui.fragment.ChannelBaseFragment;
import tv.ismar.daisy.ui.widget.DaisyViewContainer;
import tv.ismar.daisy.ui.widget.HomeItemContainer;
import tv.ismar.daisy.utils.HardwareUtils;
import tv.ismar.daisy.views.LabelImageView;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by huaijie on 5/18/15.
 */
public class GuideFragment extends ChannelBaseFragment implements Flag.ChangeCallback, PlaybackService.Client.Callback,
        PlaybackService.Callback {
    private String TAG = "GuideFragment";

    private static final int START_PLAYBACK = 0x0000;

    private DaisyViewContainer guideRecommmendList;
    private DaisyViewContainer carouselLayout;

    private int itemViewBoundaryMargin;

    private ArrayList<String> allVideoUrl;
    private ArrayList<LabelImageView> allItem;

    private ArrayList<Carousel> carousels;
    private Flag flag;
    private LabelImageView toppage_carous_imageView1;
    private LabelImageView toppage_carous_imageView2;
    private LabelImageView toppage_carous_imageView3;

    private IsmartvUrlClient datafetch;

    private SurfaceView mSurfaceView;


    private PlaybackServiceActivity.Helper mHelper;
    private PlaybackService mService;


    @Override
    public void onConnected(PlaybackService service) {
        mService = service;
        mHandler.sendEmptyMessage(START_PLAYBACK);
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
//        switch (event.type) {
//            case MediaPlayer.Event.PositionChanged:
//                Log.d(TAG, "PositionChanged: " + event.getPositionChanged());
//                break;
//
//        }
    }

    @Override
    public void onMediaIndexChange(MediaWrapperList mediaWrapperList, int position) {
        Log.d(TAG, "onMediaIndexChange position: " + position);

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


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = LayoutInflater.from(mContext).inflate(R.layout.fragment_guide, null);
        guideRecommmendList = (DaisyViewContainer) mView.findViewById(R.id.recommend_list);
        carouselLayout = (DaisyViewContainer) mView.findViewById(R.id.carousel_layout);
        itemViewBoundaryMargin = (int) mContext.getResources().getDimension(R.dimen.item_boundary_margin);
        toppage_carous_imageView1 = (LabelImageView) mView.findViewById(R.id.toppage_carous_imageView1);
        toppage_carous_imageView2 = (LabelImageView) mView.findViewById(R.id.toppage_carous_imageView2);
        toppage_carous_imageView3 = (LabelImageView) mView.findViewById(R.id.toppage_carous_imageView3);

        mSurfaceView = (SurfaceView) mView.findViewById(R.id.linked_video);

        flag = new Flag(this);

//        linkedVideoView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String url = carousels.get(flag.getPosition()).getUrl();
//                String model = carousels.get(flag.getPosition())
//                        .getModel_name();
//                String title = carousels.get(flag.getPosition()).getTitle();
//                Intent intent = new Intent();
//                if ("item".equals(model)) {
//                    intent.setClassName("tv.ismar.daisy", "tv.ismar.daisy.ItemDetailActivity");
//                    intent.putExtra("url", url);
//                    mContext.startActivity(intent);
//                } else if ("topic".equals(model)) {
//                    intent.putExtra("url", url);
//                    intent.setClassName("tv.ismar.daisy",
//                            "tv.ismar.daisy.TopicActivity");
//                    mContext.startActivity(intent);
//                } else if ("section".equals(model)) {
//                    intent.putExtra("title", title);
//                    intent.putExtra("itemlistUrl", url);
//                    intent.putExtra("lableString", title);
//                    intent.setClassName("tv.ismar.daisy", "tv.ismar.daisy.PackageListDetailActivity");
//                    mContext.startActivity(intent);
//                } else if ("package".equals(model)) {
//                    intent.setAction("tv.ismar.daisy.packageitem");
//                    intent.putExtra("url", url);
//                    mContext.startActivity(intent);
//                } else if ("clip".equals(model)) {
//                    InitPlayerTool tool = new InitPlayerTool(mContext);
//                    tool.initClipInfo(url, InitPlayerTool.FLAG_URL);
//                }
//
//            }
//        });
//        linkedVideoView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (hasFocus) {
//                    ((HomeItemContainer) v.getParent())
//                            .setDrawBorder(true);
//                    ((HomeItemContainer) v.getParent()).invalidate();
//                } else {
//                    ((HomeItemContainer) v.getParent())
//                            .setDrawBorder(false);
//                    ((HomeItemContainer) v.getParent()).invalidate();
//                }
//            }
//        });


        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchHomePage();
    }


    @Override
    public void onPause() {
        super.onPause();
    }


    @Override
    public void onDetach() {
        super.onDetach();
//        loopMessageHandler.removeMessages(0);
        if (datafetch != null && datafetch.isAlive())
            datafetch.interrupt();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopPlayback();

        mHelper.onStop();
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

    private void initCarousel(
            final ArrayList<HomePagerEntity.Carousel> carousels) {
        this.carousels = carousels;
        String tag = "guide";
        deleteFile(carousels, tag);
        downloadVideo(carousels, tag);

        mHelper = new PlaybackServiceActivity.Helper(mContext, this);
        mHelper.onStart();


//        loopAllListener = new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer mp) {
//                if (flag.getPosition() + 1 >= allVideoUrl.size()) {
//                    flag.setPosition(0);
//                } else {
//                    flag.setPosition(flag.getPosition() + 1);
//                }
//                Log.d(TAG, "loopAllListener: setVideoPath");
//                setVideoPath(linkedVideoView, allVideoUrl.get(flag.getPosition()));
//            }
//        };
//
//        loopCurrentListener = new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer mp) {
//                Log.d(TAG, "loopCurrentListener: setVideoPath");
//                setVideoPath(linkedVideoView, allVideoUrl.get(flag.getPosition()));
//            }
//        };

        allItem = new ArrayList<LabelImageView>();
        allVideoUrl = new ArrayList<String>();
//
//        View.OnFocusChangeListener itemFocusChangeListener = new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                boolean focusFlag = true;
//                for (ImageView imageView : allItem) {
//                    focusFlag = focusFlag && (!imageView.isFocused());
//                }
//
//                // all view not focus
//                if (focusFlag) {
//                    linkedVideoView.setOnCompletionListener(null);
//                    linkedVideoView.setOnCompletionListener(loopAllListener);
//                } else {
//                    if (hasFocus) {
//                        flag.setPosition((Integer) v.getTag());
//                        linkedVideoView.setOnCompletionListener(null);
//                        linkedVideoView.setOnCompletionListener(loopCurrentListener);
//                        Log.d(TAG, "flag position: " + flag.getPosition());
//                        Log.d(TAG, "itemFocusChangeListener: setVideoPath");
//
//                        setVideoPath(linkedVideoView, allVideoUrl.get(flag.getPosition()));
//                    }
//                }
//
//            }
//        };


        Picasso.with(mContext).load(carousels.get(0).getThumb_image()).memoryPolicy(MemoryPolicy.NO_STORE).into(toppage_carous_imageView1);
        toppage_carous_imageView1.setTag(0);
        toppage_carous_imageView1.setTag(R.drawable.launcher_selector, carousels.get(0));
        toppage_carous_imageView1.setOnClickListener(ItemClickListener);
//        toppage_carous_imageView1.setOnFocusChangeListener(itemFocusChangeListener);

        Picasso.with(mContext).load(carousels.get(1).getThumb_image()).memoryPolicy(MemoryPolicy.NO_STORE).into(toppage_carous_imageView2);
        toppage_carous_imageView2.setTag(1);
        toppage_carous_imageView2.setTag(R.drawable.launcher_selector, carousels.get(1));
        toppage_carous_imageView2.setOnClickListener(ItemClickListener);
//        toppage_carous_imageView2.setOnFocusChangeListener(itemFocusChangeListener);

        Picasso.with(mContext).load(carousels.get(2).getThumb_image()).memoryPolicy(MemoryPolicy.NO_STORE).into(toppage_carous_imageView3);
        toppage_carous_imageView3.setTag(2);
        toppage_carous_imageView3.setTag(R.drawable.launcher_selector, carousels.get(2));
        toppage_carous_imageView3.setOnClickListener(ItemClickListener);
//        toppage_carous_imageView3.setOnFocusChangeListener(itemFocusChangeListener);

        allItem.add(toppage_carous_imageView1);
        allItem.add(toppage_carous_imageView2);
        allItem.add(toppage_carous_imageView3);
        allVideoUrl.add(carousels.get(0).getVideo_url());
        allVideoUrl.add(carousels.get(1).getVideo_url());
        allVideoUrl.add(carousels.get(2).getVideo_url());
        flag.setPosition(0);
//        linkedVideoView.setOnCompletionListener(loopAllListener);
//        setVideoPath(linkedVideoView, carousels.get(flag.getPosition()).getVideo_url());

    }

    private void setVideoPath(final VideoView videoView, final String url) {
        new Thread() {
            @Override
            public void run() {
                String playPath;
                DownloadTable downloadTable = new Select().from(DownloadTable.class).where(DownloadTable.URL + " = ?", url).executeSingle();
                if (downloadTable == null) {
                    playPath = url;
                } else {
                    File localVideoFile = new File(downloadTable.download_path);
                    String fileMd5Code = HardwareUtils.getMd5ByFile(localVideoFile);
                    Log.d(TAG, "local md5: " + fileMd5Code + " | " + "server md5: " + downloadTable.server_md5);
                    if (fileMd5Code.equalsIgnoreCase(downloadTable.server_md5)) {
                        playPath = localVideoFile.getAbsolutePath();
                    } else {
                        playPath = url;
                    }
                }
                Log.d(TAG, "set video path: " + playPath);
                Message message = new Message();
                message.what = 0;
                message.obj = playPath;
//                loopMessageHandler.sendMessage(message);
            }
        }.start();
    }

    private void downloadVideo(ArrayList<Carousel> carousels, String tag) {
        for (Carousel carousel : carousels) {
            String url = carousel.getVideo_url();
            List<DownloadTable> downloadTables = new Select()
                    .from(DownloadTable.class)
                    .where(DownloadTable.URL + " = ?", carousel.getVideo_url())
                    .execute();
            if (!downloadTables.isEmpty()) {
                String localFilePath = downloadTables.get(0).download_path;
                File localFile = new File(localFilePath);
                String fileMd5Code = localFile.getName().split("\\.")[0];
                if (!fileMd5Code.equalsIgnoreCase(downloadTables.get(0).server_md5)) {
                    for (DownloadTable downloadTable : downloadTables) {
                        File file = new File(downloadTable.download_path);
                        if (file.exists()) {
                            file.delete();
                        }
                        downloadTable.delete();
                    }
                    // download file
                    download(url, tag);
                } else {
                    for (DownloadTable downloadTable : downloadTables) {
                        File file = new File(downloadTable.download_path);
                        if (!file.exists()) {
                            downloadTable.delete();
                            download(url, tag);
                        } else {
                            // nothing file already download
                        }
                    }

                }
                // if table is empty download video
            } else {
                download(url, tag);
            }
        }
    }

    private void download(String url, String tag) {
        String savePath = HardwareUtils.getCachePath(mContext) + "/" + tag + "/";
        DownloadClient downloadClient = new DownloadClient(url, savePath);
        DownloadThreadPool.getInstance().add(downloadClient);
    }

    private void deleteFile(ArrayList<HomePagerEntity.Carousel> carousels,
                            String tag) {
        String savePath = HardwareUtils.getCachePath(mContext) + "/" + tag + "/";
        ArrayList<String> exceptsPaths = new ArrayList<String>();
        for (HomePagerEntity.Carousel carousel : carousels) {
            try {
                File file = new File(new URL(carousel.getVideo_url()).getFile());
                exceptsPaths.add(file.getName());
            } catch (MalformedURLException e) {
                Log.e(TAG, e.getMessage());
            }

        }
        HardwareUtils.deleteFiles(savePath, exceptsPaths);
    }

    @Override
    public void change(int position) {
        for (int i = 0; i < allItem.size(); i++) {
            LabelImageView imageView = allItem.get(i);
            if (position != i) {
                imageView.setCustomfocus(false);
            } else {
                imageView.setCustomfocus(true);
            }
        }
    }

//
//    private Handler loopMessageHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            String playPath = (String) msg.obj;
//            if (linkedVideoView.isPlaying()) {
//                linkedVideoView.pause();
//                linkedVideoView.stopPlayback();
//            }
//            linkedVideoView.setVideoPath(playPath);
//            linkedVideoView.start();
//
//        }
//    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case START_PLAYBACK:
                    startPlayback();
                    break;
            }

        }
    };


    private void startPlayback() {
        Log.d(TAG, "startPlayback is invoke...");

        IVLCVout vlcVout = mService.getVLCVout();
        vlcVout.setVideoView(mSurfaceView);
        vlcVout.attachViews();

        mService.addCallback(this);

        ArrayList<MediaWrapper> mediaWrapperList = new ArrayList<MediaWrapper>();
        for (Carousel carousel : carousels) {
            MediaWrapper mediaWrapper = new MediaWrapper(Uri.parse(carousel.getVideo_url()));
            mediaWrapper.removeFlags(MediaWrapper.MEDIA_FORCE_AUDIO);
            mediaWrapper.addFlags(MediaWrapper.MEDIA_VIDEO);
            mediaWrapperList.add(mediaWrapper);
        }
        mService.load(mediaWrapperList, 0);
        mService.setRepeatType(PlaybackService.RepeatType.All);
        mService.play();
    }

    private void stopPlayback() {
        IVLCVout vlcVout = mService.getVLCVout();
        vlcVout.detachViews();
        mService.addCallback(null);
        mService.stop();
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



