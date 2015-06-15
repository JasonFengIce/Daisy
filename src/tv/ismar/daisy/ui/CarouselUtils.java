package tv.ismar.daisy.ui;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.VideoView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import tv.ismar.daisy.utils.DeviceUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by huaijie on 6/15/15.
 */
public class CarouselUtils {

    private static final String TAG = "CarouselUtils";

    private static CarouselUtils carouselUtils;

    private Context context;
    private LinkedList<HashMap<String, String>> linkedList;
    private VideoView videoView;
    private ImageView imageView;

    private MessageHandler messageHandler;

    private CarouselUtils() {

    }

    public static CarouselUtils getInstance() {
        if (carouselUtils == null) {
            carouselUtils = new CarouselUtils();
        }
        return carouselUtils;
    }


    public void loopCarousel(Context context, ArrayList<String> arrayList, final VideoView videoView, final ImageView imageView) {
        Log.d(TAG, "loopCarousel is starting!!!");
        this.context = context;
        this.videoView = videoView;
        this.imageView = imageView;
        this.messageHandler = new MessageHandler();

        final LinkedList<HashMap<String, String>> linkedList = new LinkedList<HashMap<String, String>>();

        for (String str : arrayList) {
            URL videoUrl = null;
            try {
                videoUrl = new URL(str);
            } catch (MalformedURLException e) {
                Log.e(TAG, e.getMessage());
            }
            String videoLocalPath = new File(Environment.getExternalStorageDirectory(), "/Daisy" + videoUrl.getFile()).getAbsolutePath();

            HashMap<String, String> hashMap = new HashMap<String, String>();
            hashMap.put("url", str);
            hashMap.put("path", videoLocalPath);
            hashMap.put("image", "");
            linkedList.addLast(hashMap);
            this.linkedList = linkedList;


        }

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playCarousel();
            }
        });

        playCarousel();

    }

    private void playCarousel() {
        HashMap<String, String> hashMap = linkedList.removeFirst();
        linkedList.addLast(hashMap);

        String url = hashMap.get("url");
        String fileType = null;
        try {
            fileType = getFileTypeByUrl(url);
        } catch (MalformedURLException e) {
            Log.e(TAG, e.getMessage());
        }

        if (fileType.equalsIgnoreCase("mp4")) {
            playVideo(hashMap);

        } else if (fileType.equalsIgnoreCase("jpg")) {
            playImage(url);
        }

    }

    private void playVideo(HashMap<String, String> hashMap) {
        if (videoView.getVisibility() != View.VISIBLE){
            imageView.setVisibility(View.GONE);
            videoView.setVisibility(View.VISIBLE);
        }
        String playPath;
        File localVideoFile = new File(hashMap.get("path"));

        if (localVideoFile.exists()) {
            String fileMd5Code = localVideoFile.getName().split("\\.")[0];
            if (DeviceUtils.getMd5ByFile(localVideoFile).equalsIgnoreCase(fileMd5Code)) {
                playPath = localVideoFile.getAbsolutePath();
            } else {
                playPath = hashMap.get("url");
            }
        } else {
            playPath = hashMap.get("url");
        }
        Log.d(TAG, "play path is: " + playPath);
        videoView.setVideoPath(playPath);
        videoView.start();
    }

    private void playImage(String path) {
        if (imageView.getVisibility() != View.VISIBLE){
            videoView.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
        }
        Picasso.with(context).load(path).into(imageView, new Callback() {
            @Override
            public void onSuccess() {
                messageHandler.sendEmptyMessageDelayed(0, 3000);
            }

            @Override
            public void onError() {
                messageHandler.sendEmptyMessageDelayed(0, 3000);
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

    private class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            playCarousel();
        }
    }
}
