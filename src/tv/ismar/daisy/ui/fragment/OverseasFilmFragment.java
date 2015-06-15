package tv.ismar.daisy.ui.fragment;

import static tv.ismar.daisy.core.client.ClientApi.restAdapter_SKYTEST_TVXIO;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import tv.ismar.daisy.R;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.client.ClientApi;
import tv.ismar.daisy.core.client.ClientApi.Overseas;
import tv.ismar.daisy.core.client.IsmartvFileClient;
import tv.ismar.daisy.data.HomePagerEntity;
import tv.ismar.daisy.data.HomePagerEntity.Poster;
import tv.ismar.daisy.ui.CarouselUtils;
import tv.ismar.daisy.ui.widget.DaisyVideoView;
import tv.ismar.daisy.utils.DeviceUtils;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.VideoView;

import com.squareup.picasso.Picasso;

/**
 * Created by huaijie on 5/18/15.
 */
public class OverseasFilmFragment extends Fragment {
    private static final String TAG = "FilmFragment";

    private LinearLayout guideRecommmendList;
    private LinearLayout carouselLayout;
    private DaisyVideoView linkedVideoView;
    private ImageView linkedVideoImage;

    private Context context;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.context = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mView = LayoutInflater.from(getActivity()).inflate(
                R.layout.fragment_film, null);
        guideRecommmendList = (LinearLayout) mView
                .findViewById(R.id.film_recommend_list);
        carouselLayout = (LinearLayout) mView
                .findViewById(R.id.film_carousel_layout);
        linkedVideoView = (DaisyVideoView) mView
                .findViewById(R.id.film_linked_video);
        linkedVideoImage = (ImageView)mView.findViewById(R.id.film_linked_image);
        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fetchHomePage(SimpleRestClient.access_token,
                SimpleRestClient.device_token);
    }

    private void fetchHomePage(String accessToken, String deviceToken) {
        ClientApi.Overseas client = restAdapter_SKYTEST_TVXIO
                .create(Overseas.class);
        client.excute(accessToken, deviceToken,
                new Callback<HomePagerEntity>() {
                    @Override
                    public void success(HomePagerEntity homePagerEntity,
                                        Response response) {
                        ArrayList<HomePagerEntity.Carousel> carousels = homePagerEntity
                                .getCarousels();
                        ArrayList<HomePagerEntity.Poster> posters = homePagerEntity
                                .getPosters();
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
        for (int i = 0; i < posters.size(); i++) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            params.weight = 1;
            if (i != 7) {
                params.setMargins(0, 0, 25, 0);
            }
            ImageView itemView = new ImageView(getActivity());
            itemView.setBackgroundResource(R.drawable.launcher_selector);
            itemView.setFocusable(true);
            itemView.setOnClickListener(ItemClickListener);
            if (i <= 6) {
                Picasso.with(getActivity())
                        .load(posters.get(i).getCustom_image()).into(itemView);
                itemView.setScaleType(ImageView.ScaleType.FIT_XY);
                itemView.setLayoutParams(params);
                itemView.setTag(posters.get(i));
            } else {
                itemView.setImageResource(R.color.channel_more);
                itemView.setScaleType(ImageView.ScaleType.FIT_XY);
                itemView.setLayoutParams(params);
            }
            guideRecommmendList.addView(itemView);
        }
    }

    private void initCarousel(ArrayList<HomePagerEntity.Carousel> carousels) {
        ArrayList<String> arrayList = new ArrayList<String>();
        if (DeviceUtils.isExternalStorageMounted()) {
            for (HomePagerEntity.Carousel carousel : carousels) {
                String url;
                if (TextUtils.isEmpty(carousel.getVideo_url())) {
                    url = carousel.getVideo_image();
                } else {
                    url = carousel.getVideo_url();
                }
                arrayList.add(url);
            }
        } else {
            for (HomePagerEntity.Carousel carousel : carousels) {
                arrayList.add(carousel.getVideo_image());
            }
        }

        CarouselUtils.getInstance().loopCarousel(context, arrayList,linkedVideoView, linkedVideoImage);
        for (int i = 0; i < carousels.size(); i++) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 0);
            params.weight = 1;
            ImageView itemView = new ImageView(getActivity());
            itemView.setBackgroundResource(R.drawable.launcher_selector);
            itemView.setFocusable(true);
            Picasso.with(getActivity()).load(carousels.get(i).getThumb_image())
                    .into(itemView);
            itemView.setScaleType(ImageView.ScaleType.FIT_XY);
            itemView.setLayoutParams(params);
            carouselLayout.addView(itemView);
        }
        // downloadCarouselVideo(carousels);

    }

    private void downloadCarouselVideo(
            ArrayList<HomePagerEntity.Carousel> carousels) {
        new IsmartvFileClient(context, carousels).start();
    }

    private void playVideo(final LoopList loopList) {
        HashMap<String, String> hashMap = loopList.next();
        String url = hashMap.get("url");
        File file = new File(hashMap.get("path"));

        if (file.exists()) {
            String md5 = hashMap.get("md5");
            Log.i(TAG, "md5 is: " + DeviceUtils.getMd5ByFile(file));
            if (DeviceUtils.getMd5ByFile(file).equals(md5)) {
                linkedVideoView.setVideoPath(file.getAbsolutePath());
                Log.i(TAG, "video path is: " + file.getAbsolutePath());
            } else {
                linkedVideoView.setVideoPath(url);
                Log.i(TAG, "video path is: " + url);
            }
        } else {
            linkedVideoView.setVideoPath(url);
            Log.i(TAG, "video path is: " + url);
        }
        linkedVideoView.start();
        linkedVideoView
                .setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        playVideo(loopList);
                    }
                });
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

    private View.OnClickListener ItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Poster poster = (Poster) view.getTag();
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (poster == null) {
                intent.putExtra("title", "华语电影");
                intent.putExtra("url",
                        "http://skytest.tvxio.com/v2_0/A21/dto/api/tv/sections/overseas/");
                intent.putExtra("channel", "chinesemovie");
                intent.setClassName("tv.ismar.daisy",
                        "tv.ismar.daisy.ChannelListActivity");
                getActivity().startActivity(intent);
            } else {
                if ("item".equals(poster.getModel_name())) {
                    intent.setClassName("tv.ismar.daisy",
                            "tv.ismar.daisy.ItemDetailActivity");
                    intent.putExtra("url", poster.getUrl());
                    getActivity().startActivity(intent);
                } else if ("topic".equals(poster.getModel_name())) {

                } else if ("section".equals(poster.getModel_name())) {

                } else if ("package".equals(poster.getModel_name())) {

                }
            }
        }
    };
}
