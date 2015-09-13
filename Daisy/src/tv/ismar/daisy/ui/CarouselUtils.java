//package tv.ismar.daisy.ui;
//
//import android.content.Context;
//import android.media.MediaPlayer;
//import android.os.Handler;
//import android.os.Message;
//import android.text.TextUtils;
//import android.util.Log;
//import android.view.View;
//import android.view.animation.Animation;
//import android.view.animation.AnimationSet;
//import android.view.animation.ScaleAnimation;
//import android.widget.ImageView;
//import android.widget.VideoView;
//import com.activeandroid.query.Delete;
//import com.activeandroid.query.Select;
//import com.squareup.picasso.Callback;
//import com.squareup.picasso.Picasso;
//import tv.ismar.daisy.core.client.DownloadClient;
//import tv.ismar.daisy.core.client.DownloadThreadPool;
//import tv.ismar.daisy.core.client.IsmartvUrlClient;
//import tv.ismar.daisy.data.HomePagerEntity;
//import tv.ismar.daisy.data.table.DownloadTable;
//import tv.ismar.daisy.utils.HardwareUtils;
//
//import java.io.File;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * Created by huaijie on 6/15/15.
// */
//public class CarouselUtils implements DownloadClient.DownloadCallback {
//
//    private static final String TAG = "CarouselUtils";
//
//    private String tag;
//
//    private int currentPosition = 0;
//
//    private Context context;
//    private LoopList loopList;
//    private VideoView videoView;
//    private ImageView imageView;
//    public CarouselFocusChangeListener listener;
//    public ScaleFocusChangeListener scaleListener;
//    private ImageIndicatorCallback callback;
//
//    private MessageHandler messageHandler;
//
//    private boolean pause = false;
//
//    private HomePagerEntity.Carousel currentCarousel;
//
//    public CarouselUtils() {
//        listener = new CarouselFocusChangeListener();
//        scaleListener = new ScaleFocusChangeListener();
//    }
//
//
//    public void loopCarousel(String tag, Context context, ArrayList<HomePagerEntity.Carousel> carousels, final VideoView videoView, final ImageView imageView) {
//        Log.d(TAG, "loopCarousel is starting!!!");
//        this.tag = tag;
//        this.context = context;
//        this.videoView = videoView;
//        this.imageView = imageView;
//        this.messageHandler = new MessageHandler();
//        deleteFile(carousels);
//
//        loopList = new LoopList(carousels);
//        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer mp) {
//                if (!pause) {
//                    playCarousel();
//                }
//            }
//        });
//        playCarousel();
//    }
//
//    public void loopCarousel(String tag, Context context, ArrayList<HomePagerEntity.Carousel> carousels, final VideoView videoView) {
//        Log.d(TAG, "loopCarousel is starting!!!");
//        this.tag = tag;
//        this.context = context;
//        this.videoView = videoView;
//        this.messageHandler = new MessageHandler();
//        deleteFile(carousels);
//
//        loopList = new LoopList(carousels);
//        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer mp) {
//                playCarousel();
//            }
//        });
//        playCarousel();
//    }
//
////    public void loopCarousel(Context context, ArrayList<HomePagerEntity.Carousel> carousels, final ImageView imageView) {
////        Log.d(TAG, "loopCarousel is starting!!!");
////        this.context = context;
////        this.imageView = imageView;
////        this.messageHandler = new MessageHandler();
////
////        loopList = new LoopList(carousels);
////        playCarousel();
////    }
//
//    public void loopCarousel(Context context, ArrayList<HomePagerEntity.Carousel> carousels, final ImageView imageView, ImageIndicatorCallback callback) {
//        Log.d(TAG, "loopCarousel is starting!!!");
//        this.context = context;
//        this.imageView = imageView;
//        this.messageHandler = new MessageHandler();
//        this.callback = callback;
//        loopList = new LoopList(carousels);
//        playCarousel();
//    }
//
//
//    private void playCarousel() {
//        HomePagerEntity.Carousel carousel = loopList.next();
//        currentCarousel = carousel;
//        String url;
//        if (imageView == null) {
//            url = carousel.getVideo_url();
//        } else if (videoView == null) {
//            url = carousel.getVideo_image();
//        } else {
//            if (HardwareUtils.isExternalStorageMounted()) {
//                if (TextUtils.isEmpty(carousel.getVideo_url())) {
//                    url = carousel.getVideo_image();
//                } else {
//                    url = carousel.getVideo_url();
//                }
//            } else {
//                url = carousel.getVideo_image();
//            }
//        }
//
//
//        String fileType = null;
//        try {
//            fileType = getFileTypeByUrl(url);
//        } catch (MalformedURLException e) {
//            Log.e(TAG, e.getMessage());
//        }
//
//        if (fileType.equalsIgnoreCase("mp4")) {
//            try {
//                playVideo(carousel);
//            } catch (MalformedURLException e) {
//                Log.e(TAG, e.getMessage());
//            }
//
//        } else if (fileType.equalsIgnoreCase("jpg")) {
//            playImage(carousel);
//        }
//
//    }
//
//    private void playVideo(HomePagerEntity.Carousel carousel) throws MalformedURLException {
//        String playPath;
//        if (videoView.getVisibility() != View.VISIBLE) {
//            imageView.setVisibility(View.GONE);
//            videoView.setVisibility(View.VISIBLE);
//        }
//        URL videoUrl = new URL(carousel.getVideo_url());
//        List<DownloadTable> downloadTables = new Select().from(DownloadTable.class).where(DownloadTable.URL + " = ?", videoUrl.toString()).execute();
//        Log.d(TAG, "downloadTables size: " + downloadTables.size());
//        if (downloadTables.isEmpty()) {
//            playPath = videoUrl.toString();
//            videoView.setVideoPath(playPath);
//            download(playPath, tag);
//        } else {
//            DownloadTable downloadTable = downloadTables.get(0);
//            File localVideoFile = new File(downloadTable.download_path);
//            Log.d(TAG, "local video path: " + localVideoFile.getAbsolutePath());
//
//            if (localVideoFile.exists()) {
//                String fileMd5Code = localVideoFile.getName().split("\\.")[0];
//                Log.d(TAG, "local file md5: " + downloadTable.md5);
//                Log.d(TAG, "url md5: " + fileMd5Code);
//                if (downloadTable.md5.equalsIgnoreCase(fileMd5Code)) {
//                    playPath = localVideoFile.getAbsolutePath();
//                    videoView.setVideoPath(playPath);
//                } else {
//                    for (DownloadTable table : downloadTables){
//                        File file = new File(table.download_path);
//                        if (file.exists()){
//                            file.delete();
//                        }
//                        table.delete();
//                    }
//                    playPath = videoUrl.toString();
//                    videoView.setVideoPath(playPath);
//                    download(playPath, tag);
//                }
//            } else {
//                playPath = videoUrl.toString();
//                videoView.setVideoPath(playPath);
//                download(playPath, tag);
//            }
//        }
//        Log.d(TAG, "setVideoPath: " + playPath);
//
//
//    }
//
//    private void playImage(final HomePagerEntity.Carousel carousel) {
//        if (imageView.getVisibility() != View.VISIBLE) {
//            videoView.setVisibility(View.GONE);
//            imageView.setVisibility(View.VISIBLE);
//        }
//        final int pauseTime = Integer.parseInt(carousel.getPause_time());
//        Picasso.with(context).load(carousel.getVideo_image()).into(imageView, new Callback() {
//            @Override
//            public void onSuccess() {
//                if (!pause) {
//                    messageHandler.sendEmptyMessageDelayed(0, pauseTime * 1000);
//                }
//            }
//
//            @Override
//            public void onError() {
//                if (!pause) {
//                    messageHandler.sendEmptyMessageDelayed(0, pauseTime * 1000);
//                }
//            }
//        });
//
//    }
//
//    public void continueLoop() {
//        pause = false;
//        messageHandler.sendEmptyMessage(0);
//    }
//
//
//    private String getFileTypeByUrl(String url) throws MalformedURLException {
//        URL mUrl = new URL(url);
//        String[] strs = mUrl.getFile().split("/");
//        String fileName = strs[strs.length - 1];
//
//        String[] strs2 = fileName.split("\\.");
//        String fileType = strs2[strs2.length - 1];
//
//        return fileType;
//
//    }
//
//    public void setCurrentPosition(Integer position) {
//        if (null != position && loopList != null) {
//            loopList.setCurrent(position);
//            messageHandler.removeMessages(0);
//            pause = true;
//            playCarousel();
//        }
//    }
//
//    @Override
//    public void onCreateFileSuccess() {
//        videoView.start();
//    }
//
//    @Override
//    public void onCreateFileFailure() {
//        if (tag.equals("guide")){
//                    videoView.start();
//        }else {
//            playImage(currentCarousel);
//        }
//
//    }
//
//    private class MessageHandler extends Handler {
//        @Override
//        public void handleMessage(Message msg) {
//            playCarousel();
//        }
//    }
//
//    private class LoopList {
//        ArrayList<HomePagerEntity.Carousel> list;
//        private int next;
//
//        public LoopList(ArrayList<HomePagerEntity.Carousel> list) {
//            this.list = list;
//        }
//
//        public void setCurrent(int position) {
//            if (position >= list.size()) {
//                throw new IllegalArgumentException("position maybe illegal");
//            } else {
//                next = position;
//            }
//        }
//
//        public HomePagerEntity.Carousel next() {
//            if (next == list.size()) {
//                if (null != callback) {
//                    callback.indicatorChanged(currentPosition, 0);
//                    currentPosition = 0;
//                }
//
//                HomePagerEntity.Carousel carousel = list.get(0);
//                next = 1;
//                return carousel;
//            } else {
//                if (null != callback) {
//                    callback.indicatorChanged(currentPosition, next);
//                    currentPosition = next;
//                }
//                HomePagerEntity.Carousel carousel = list.get(next);
//                next = next + 1;
//                return carousel;
//            }
//        }
//
//    }
//
//    private class CarouselFocusChangeListener implements View.OnFocusChangeListener {
//        @Override
//        public void onFocusChange(View v, boolean hasFocus) {
//            if (hasFocus) {
//                Integer position = (Integer) v.getTag();
//                if (null != position) {
//                    setCurrentPosition(position);
//                }
//
//            }
//        }
//    }
//
//    private class ScaleFocusChangeListener implements View.OnFocusChangeListener {
//        @Override
//        public void onFocusChange(View view, boolean hasFocus) {
//            if (hasFocus) {
//                Integer position = (Integer) view.getTag();
//                if (null != position) {
//                    setCurrentPosition(position);
//                }
//                AnimationSet animationSet = new AnimationSet(true);
//                ScaleAnimation scaleAnimation = new ScaleAnimation(1, 1, 1, 1.53f,
//                        Animation.RELATIVE_TO_SELF, 0.5f,
//                        Animation.RELATIVE_TO_SELF, 0.5f);
//                scaleAnimation.setDuration(200);
//                animationSet.addAnimation(scaleAnimation);
//                animationSet.setFillAfter(true);
//                view.startAnimation(animationSet);
//            } else {
//                AnimationSet animationSet = new AnimationSet(true);
//                ScaleAnimation scaleAnimation = new ScaleAnimation(1, 1f, 1.53f, 1f,
//                        Animation.RELATIVE_TO_SELF, 0.5f,
//                        Animation.RELATIVE_TO_SELF, 0.5f);
//                scaleAnimation.setDuration(200);
//                animationSet.addAnimation(scaleAnimation);
//                animationSet.setFillAfter(true);
//                view.startAnimation(animationSet);
//            }
//
//        }
//    }
//
//    public interface ImageIndicatorCallback {
//        void indicatorChanged(int hide, int show);
//    }
//
//
//    private void deleteFile(ArrayList<HomePagerEntity.Carousel> carousels) {
//
//        String savePath = HardwareUtils.getCachePath(context) + "/" + tag + "/";
//        ArrayList<String> exceptsPaths = new ArrayList<String>();
//
//        for (HomePagerEntity.Carousel carousel : carousels) {
//            try {
//                File file = new File(new URL(carousel.getVideo_url()).getFile());
//                exceptsPaths.add(file.getName());
//            } catch (MalformedURLException e) {
//                Log.e(TAG, e.getMessage());
//            }
//
//        }
//        HardwareUtils.deleteFiles(savePath, exceptsPaths);
//    }
//
//    private void download(String downloadUrl, String tag) {
//        String savePath = HardwareUtils.getCachePath(context) + "/" + tag + "/";
//        DownloadClient client = new DownloadClient(downloadUrl, savePath, this);
//        DownloadThreadPool.getInstance().add(client);
//    }
//}
