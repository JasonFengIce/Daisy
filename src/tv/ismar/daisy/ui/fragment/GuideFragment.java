package tv.ismar.daisy.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.VideoView;
import com.squareup.picasso.Picasso;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import tv.ismar.daisy.R;
import tv.ismar.daisy.core.client.ClientApi;
import tv.ismar.daisy.core.client.IsmartvFileClient;
import tv.ismar.daisy.data.HomePagerEntity;
import tv.ismar.daisy.ui.ItemViewFocusChangeListener;
import tv.ismar.daisy.utils.DeviceUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import static tv.ismar.daisy.core.client.ClientApi.Homepage;
import static tv.ismar.daisy.core.client.ClientApi.restAdapter_SKYTEST_TVXIO;

/**
 * Created by huaijie on 5/18/15.
 */
public class GuideFragment extends Fragment {
    private String TAG = "GuideFragment";
    private LinearLayout guideRecommmendList;
    private LinearLayout carouselLayout;
    private VideoView linkedVideoView;

    private Context context;

    private int itemViewBoundaryMargin;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = LayoutInflater.from(context).inflate(R.layout.fragment_guide, null);
        guideRecommmendList = (LinearLayout) mView.findViewById(R.id.recommend_list);
        carouselLayout = (LinearLayout) mView.findViewById(R.id.carousel_layout);
        linkedVideoView = (VideoView) mView.findViewById(R.id.linked_video);

        itemViewBoundaryMargin = (int) getResources().getDimension(R.dimen.item_boundary_margin);
        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        fetchHomePage(" ", " ");


    }


    private void fetchHomePage(String accessToken, String deviceToken) {
        ClientApi.Homepage client = restAdapter_SKYTEST_TVXIO.create(Homepage.class);
        client.excute(accessToken, deviceToken, new Callback<HomePagerEntity>() {
            @Override
            public void success(HomePagerEntity homePagerEntity, Response response) {
                ArrayList<HomePagerEntity.Carousel> carousels = homePagerEntity.getCarousels();
                ArrayList<HomePagerEntity.Poster> posters = homePagerEntity.getPosters();
                initPosters(posters);
                initCarousel(carousels);
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.e(TAG, retrofitError.getMessage());
            }
        });

    }

    private void initPosters(ArrayList<HomePagerEntity.Poster> posters) {
        for (int i = 0; i < 8; i++) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
            params.weight = 1;
            int marginLF = (int)getResources().getDimension(R.dimen.guide_fragment_poser_margin_lf);

            if (i == 0) {
                params.setMargins(itemViewBoundaryMargin, itemViewBoundaryMargin, marginLF, itemViewBoundaryMargin);
            } else if (i == 7) {
                params.setMargins(0, itemViewBoundaryMargin, itemViewBoundaryMargin, itemViewBoundaryMargin);
            } else {
                params.setMargins(0, itemViewBoundaryMargin, marginLF, itemViewBoundaryMargin);
            }
            ImageView itemView = new ImageView(context);
            Picasso.with(context).load(posters.get(i).getCustom_image()).into(itemView);
            itemView.setScaleType(ImageView.ScaleType.FIT_XY);
            itemView.setFocusable(true);
            itemView.setFocusableInTouchMode(true);
            itemView.setClickable(true);
            itemView.setBackgroundResource(R.drawable.launcher_selector);
            itemView.setLayoutParams(params);
            itemView.setOnFocusChangeListener(new ItemViewFocusChangeListener());
            guideRecommmendList.addView(itemView);
        }
    }

    private void initCarousel(ArrayList<HomePagerEntity.Carousel> carousels) {
        LoopList loopList = new LoopList();
        for (HomePagerEntity.Carousel carousel : carousels) {
            HashMap<String, String> hashMap = new HashMap<String, String>();
            hashMap.put("url", carousel.getVideo_url());
            try {
                URL mUrl = new URL(carousel.getVideo_url());
                File file = new File(DeviceUtils.getCachePath(context), mUrl.getFile());
                String fileName = file.getName().split("\\.")[0];
                hashMap.put("path", file.getAbsolutePath());
                hashMap.put("md5", fileName);
                loopList.add(hashMap);
            } catch (MalformedURLException e) {
                Log.e(TAG, "initCarousel: " + e.getMessage());
            }
        }
        playVideo(loopList);
        for (int i = 0; i < 3; i++) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
            params.weight = 1;
            params.setMargins(itemViewBoundaryMargin, itemViewBoundaryMargin / 2, itemViewBoundaryMargin, itemViewBoundaryMargin / 2);
            ImageView itemView = new ImageView(context);
            itemView.setBackgroundResource(R.drawable.launcher_selector);
            itemView.setFocusableInTouchMode(true);
            itemView.setFocusable(true);
            itemView.setClickable(true);
            Picasso.with(context).load(carousels.get(i).getThumb_image()).into(itemView);
            itemView.setScaleType(ImageView.ScaleType.FIT_XY);
            itemView.setLayoutParams(params);
            itemView.setOnFocusChangeListener(new ItemViewFocusChangeListener());
            carouselLayout.addView(itemView);
        }
        downloadCarouselVideo(carousels);

    }

    private void downloadCarouselVideo(ArrayList<HomePagerEntity.Carousel> carousels) {
        new IsmartvFileClient(context, carousels).start();
    }


    private void playVideo(final LoopList loopList) {
        class VideoMessageHandle extends Handler {
            public VideoMessageHandle(Looper looper) {
                super(looper);
            }

            @Override
            public void handleMessage(Message msg) {
                linkedVideoView.setVideoPath(msg.obj.toString());
                linkedVideoView.start();
                linkedVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        playVideo(loopList);
                    }
                });
            }
        }

        new Thread() {
            @Override
            public void run() {
                String videoPath;
                HashMap<String, String> hashMap = loopList.next();
                String url = hashMap.get("url");
                File file = new File(hashMap.get("path"));

                if (file.exists()) {
                    String md5 = hashMap.get("md5");
                    Log.i(TAG, "md5 is: " + DeviceUtils.getMd5ByFile(file));
                    if (DeviceUtils.getMd5ByFile(file).equals(md5)) {
                        videoPath = file.getAbsolutePath();
                        Log.i(TAG, "video path is: " + file.getAbsolutePath());
                    } else {
                        videoPath = url;
                        Log.i(TAG, "video path is: " + url);
                    }
                } else {
                    videoPath = url;
                    Log.i(TAG, "video path is: " + url);
                }
                VideoMessageHandle messageHandle = new VideoMessageHandle(context.getMainLooper());
                Message message = messageHandle.obtainMessage(0, videoPath);
                messageHandle.sendMessage(message);
            }
        }.start();


    }


    class LoopList {
        ArrayList<HashMap<String, String>> list;
        private int next;

        public LoopList() {
            list = new ArrayList<HashMap<String, String>>();
        }

        public void add(HashMap<String, String> hashMap) {
            list.add(hashMap);
        }

        public HashMap<String, String> next() {
            if (next == list.size()) {
                HashMap<String, String> hashMap = list.get(0);
                next = 1;
                return hashMap;
            } else {
                HashMap<String, String> hashMap = list.get(next);
                next = next + 1;
                return hashMap;
            }
        }
    }
}



