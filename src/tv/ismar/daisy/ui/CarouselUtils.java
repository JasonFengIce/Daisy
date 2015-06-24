package tv.ismar.daisy.ui;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.VideoView;
import com.activeandroid.query.Select;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import tv.ismar.daisy.core.client.DownloadClient;
import tv.ismar.daisy.core.client.DownloadThreadPool;
import tv.ismar.daisy.core.client.IsmartvUrlClient;
import tv.ismar.daisy.data.HomePagerEntity;
import tv.ismar.daisy.data.table.DownloadTable;
import tv.ismar.daisy.utils.HardwareUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by huaijie on 6/15/15.
 */
public class CarouselUtils {

    private static final String TAG = "CarouselUtils";

    private String tag;

    private int currentPosition = 0;

    private Context context;
    private LoopList loopList;
    private VideoView videoView;
    private ImageView imageView;
    public CarouselFocusChangeListener listener;
    public ScaleFocusChangeListener scaleListener;
    private ImageIndicatorCallback callback;

    private MessageHandler messageHandler;

    public CarouselUtils() {
        listener = new CarouselFocusChangeListener();
        scaleListener = new ScaleFocusChangeListener();
    }


    public void loopCarousel(String tag, Context context, ArrayList<HomePagerEntity.Carousel> carousels, final VideoView videoView, final ImageView imageView) {
        Log.d(TAG, "loopCarousel is starting!!!");
        this.tag = tag;
        this.context = context;
        this.videoView = videoView;
        this.imageView = imageView;
        this.messageHandler = new MessageHandler();

        loopList = new LoopList(carousels);
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playCarousel();
            }
        });
        playCarousel();
    }

    public void loopCarousel(String tag, Context context, ArrayList<HomePagerEntity.Carousel> carousels, final VideoView videoView) {
        Log.d(TAG, "loopCarousel is starting!!!");
        this.tag = tag;
        this.context = context;
        this.videoView = videoView;
        this.messageHandler = new MessageHandler();

        loopList = new LoopList(carousels);
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playCarousel();
            }
        });
        playCarousel();
    }

//    public void loopCarousel(Context context, ArrayList<HomePagerEntity.Carousel> carousels, final ImageView imageView) {
//        Log.d(TAG, "loopCarousel is starting!!!");
//        this.context = context;
//        this.imageView = imageView;
//        this.messageHandler = new MessageHandler();
//
//        loopList = new LoopList(carousels);
//        playCarousel();
//    }

    public void loopCarousel(Context context, ArrayList<HomePagerEntity.Carousel> carousels, final ImageView imageView, ImageIndicatorCallback callback) {
        Log.d(TAG, "loopCarousel is starting!!!");
        this.context = context;
        this.imageView = imageView;
        this.messageHandler = new MessageHandler();
        this.callback = callback;
        loopList = new LoopList(carousels);
        playCarousel();
    }


    private void playCarousel() {
        HomePagerEntity.Carousel carousel = loopList.next();
        String url;
        if (imageView == null) {
            url = carousel.getVideo_url();
        } else if (videoView == null) {
            url = carousel.getVideo_image();
        } else {
            if (HardwareUtils.isExternalStorageMounted()) {
                if (TextUtils.isEmpty(carousel.getVideo_url())) {
                    url = carousel.getVideo_image();
                } else {
                    url = carousel.getVideo_url();
                }
            } else {
                url = carousel.getVideo_image();
            }
        }


        String fileType = null;
        try {
            fileType = getFileTypeByUrl(url);
        } catch (MalformedURLException e) {
            Log.e(TAG, e.getMessage());
        }

        if (fileType.equalsIgnoreCase("mp4")) {
            try {
                playVideo(carousel);
            } catch (MalformedURLException e) {
                Log.e(TAG, e.getMessage());
            }

        } else if (fileType.equalsIgnoreCase("jpg")) {
            playImage(carousel);
        }

    }

    private void playVideo(HomePagerEntity.Carousel carousel) throws MalformedURLException {
        String playPath;
        if (videoView.getVisibility() != View.VISIBLE) {
            imageView.setVisibility(View.GONE);
            videoView.setVisibility(View.VISIBLE);
        }
        URL videoUrl = new URL(carousel.getVideo_url());
        DownloadTable downloadTable = new Select().from(DownloadTable.class).where(DownloadTable.URL + "=?", videoUrl.toString()).executeSingle();

        if (downloadTable == null) {
            playPath = videoUrl.toString();
            String savePath = HardwareUtils.getCachePath(context) + "/" + tag + "/";
            DownloadClient client = new DownloadClient(playPath, savePath);
            DownloadThreadPool.getInstance().add(client);
        } else {
            File localVideoFile = new File(downloadTable.download_path);
            Log.d(TAG, "local video path: " + localVideoFile.getAbsolutePath());

            if (localVideoFile.exists()) {
                String fileMd5Code = localVideoFile.getName().split("\\.")[0];
                Log.d(TAG, "local file md5: " + downloadTable.md5);
                Log.d(TAG, "url md5: " + fileMd5Code);
                if (downloadTable.md5.equalsIgnoreCase(fileMd5Code)) {
                    playPath = localVideoFile.getAbsolutePath();
                } else {
                    playPath = videoUrl.toString();
                }
            } else {
                playPath = videoUrl.toString();
                String savePath = HardwareUtils.getCachePath(context) + "/" + tag + "/";
                DownloadClient client = new DownloadClient(playPath, savePath);
                DownloadThreadPool.getInstance().add(client);
            }
        }
        Log.d(TAG, "setVideoPath: " + playPath);
        videoView.setVideoPath(playPath);
        videoView.start();
    }

    private void playImage(final HomePagerEntity.Carousel carousel) {
        if (imageView.getVisibility() != View.VISIBLE) {
            videoView.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
        }
        final int pauseTime = Integer.parseInt(carousel.getPause_time());
        Picasso.with(context).load(carousel.getVideo_image()).into(imageView, new Callback() {
            @Override
            public void onSuccess() {
                messageHandler.sendEmptyMessageDelayed(0, pauseTime * 1000);
            }

            @Override
            public void onError() {
                messageHandler.sendEmptyMessageDelayed(0, pauseTime * 1000);
            }
        });

    }


    private String getFileTypeByUrl(String url) throws MalformedURLException {
        URL mUrl = new URL(url);
        String[] strs = mUrl.getFile().split("/");
        String fileName = strs[strs.length - 1];

        String[] strs2 = fileName.split("\\.");
        String fileType = strs2[strs2.length - 1];

        return fileType;

    }

    public void setCurrentPosition(Integer position) {
        if (null != position && loopList != null) {
            loopList.setCurrent(position);
            messageHandler.removeMessages(0);
            playCarousel();
        }
    }

    private class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            playCarousel();
        }
    }

    private class LoopList {
        ArrayList<HomePagerEntity.Carousel> list;
        private int next;

        public LoopList(ArrayList<HomePagerEntity.Carousel> list) {
            this.list = list;
        }

        public void setCurrent(int position) {
            if (position >= list.size()) {
                throw new IllegalArgumentException("position maybe illegal");
            } else {
                next = position;
            }
        }

        public HomePagerEntity.Carousel next() {
            if (next == list.size()) {
                if (null != callback) {
                    callback.indicatorChanged(currentPosition, 0);
                    currentPosition = 0;
                }

                HomePagerEntity.Carousel carousel = list.get(0);
                next = 1;
                return carousel;
            } else {
                if (null != callback) {
                    callback.indicatorChanged(currentPosition, next);
                    currentPosition = next;
                }
                HomePagerEntity.Carousel carousel = list.get(next);
                next = next + 1;
                return carousel;
            }
        }

    }

    private class CarouselFocusChangeListener implements View.OnFocusChangeListener {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                Integer position = (Integer) v.getTag();
                if (null != position) {
                    setCurrentPosition(position);
                }

            }
        }
    }

    private class ScaleFocusChangeListener implements View.OnFocusChangeListener {
        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            if (hasFocus) {
                Integer position = (Integer) view.getTag();
                if (null != position) {
                    setCurrentPosition(position);
                }
                AnimationSet animationSet = new AnimationSet(true);
                ScaleAnimation scaleAnimation = new ScaleAnimation(1, 1, 1, 1.53f,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);
                scaleAnimation.setDuration(200);
                animationSet.addAnimation(scaleAnimation);
                animationSet.setFillAfter(true);
                view.startAnimation(animationSet);
            } else {
                AnimationSet animationSet = new AnimationSet(true);
                ScaleAnimation scaleAnimation = new ScaleAnimation(1, 1f, 1.53f, 1f,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);
                scaleAnimation.setDuration(200);
                animationSet.addAnimation(scaleAnimation);
                animationSet.setFillAfter(true);
                view.startAnimation(animationSet);
            }

        }
    }

    public interface ImageIndicatorCallback {
        void indicatorChanged(int hide, int show);
    }

}
