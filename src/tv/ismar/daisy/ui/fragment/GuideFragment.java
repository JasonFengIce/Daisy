package tv.ismar.daisy.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.VideoView;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import tv.ismar.daisy.R;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.client.IsmartvFileClient;
import tv.ismar.daisy.core.client.IsmartvUrlClient;
import tv.ismar.daisy.data.HomePagerEntity;
import tv.ismar.daisy.data.HomePagerEntity.Carousel;
import tv.ismar.daisy.data.HomePagerEntity.Poster;
import tv.ismar.daisy.ui.CarouselUtils;
import tv.ismar.daisy.ui.ItemViewFocusChangeListener;
import tv.ismar.daisy.utils.HardwareUtils;

import java.util.ArrayList;

/**
 * Created by huaijie on 5/18/15.
 */
public class GuideFragment extends Fragment {
    private String TAG = "GuideFragment";
    private LinearLayout guideRecommmendList;
    private LinearLayout carouselLayout;
    private VideoView linkedVideoView;

    private CarouselUtils carouselUtils;

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
        fetchHomePage();
    }


    public void fetchHomePage() {
        String api = SimpleRestClient.root_url + "/api/tv/homepage/top/";
        new IsmartvUrlClient(context).doRequest(api, new IsmartvUrlClient.CallBack() {
            @Override
            public void onSuccess(String result) {
                HomePagerEntity homePagerEntity = new Gson().fromJson(result, HomePagerEntity.class);
                ArrayList<HomePagerEntity.Carousel> carousels = homePagerEntity.getCarousels();
                ArrayList<HomePagerEntity.Poster> posters = homePagerEntity.getPosters();
                initPosters(posters);
                initCarousel(carousels);
            }

            @Override
            public void onFailed(Exception exception) {
                Log.e(TAG, exception.getMessage());
            }
        });
    }

    private void initPosters(ArrayList<HomePagerEntity.Poster> posters) {
        for (int i = 0; i < 8; i++) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
            params.weight = 1;
            int marginLF = (int) getResources().getDimension(R.dimen.guide_fragment_poser_margin_lf);

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
            itemView.setOnClickListener(ItemClickListener);
            itemView.setTag(posters.get(i));
            itemView.setOnFocusChangeListener(new ItemViewFocusChangeListener());
            guideRecommmendList.addView(itemView);
        }
    }

    private void initCarousel(final ArrayList<HomePagerEntity.Carousel> carousels) {
        carouselUtils = new CarouselUtils();
        getView().postDelayed(new Runnable() {
            @Override
            public void run() {
                carouselUtils.loopCarousel(context, carousels, linkedVideoView);
            }
        }, 3000);

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
            itemView.setTag(i);
            itemView.setTag(R.drawable.launcher_selector, carousels.get(i));
            itemView.setOnClickListener(ItemClickListener);
            itemView.setOnFocusChangeListener(carouselUtils.listener);
            carouselLayout.addView(itemView);
        }
        downloadCarouselVideo(carousels);

    }

    private void downloadCarouselVideo(ArrayList<HomePagerEntity.Carousel> carousels) {
        new IsmartvFileClient(context, carousels, HardwareUtils.getCachePath(context) + "/guide/").start();
    }

    private View.OnClickListener ItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String url = null;
            String contentMode = null;
            String title = null;
            if (view.getTag() instanceof Poster) {
                Poster new_name = (Poster) view.getTag();
                contentMode = new_name.getModel_name();
                url = new_name.getUrl();
                title = new_name.getTitle();
            } else if (view.getTag(R.drawable.launcher_selector) instanceof Carousel) {
                Carousel new_name = (Carousel) view.getTag(R.drawable.launcher_selector);
                contentMode = new_name.getModel_name();
                url = new_name.getUrl();
                title = new_name.getTitle();
            }
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (url == null) {
                intent.putExtra("title", "华语电影");
                intent.putExtra("url",
                        "http://skytest.tvxio.com/v2_0/A21/dto/api/tv/sections/chinesemovie/");
                intent.putExtra("channel", "chinesemovie");
                intent.setClassName("tv.ismar.daisy",
                        "tv.ismar.daisy.ChannelListActivity");
                getActivity().startActivity(intent);
            } else {
                if ("item".equals(contentMode)) {
                    intent.setClassName("tv.ismar.daisy",
                            "tv.ismar.daisy.ItemDetailActivity");
                    intent.putExtra("url", url);
                    getActivity().startActivity(intent);
                } else if ("topic".equals(contentMode)) {
                    intent.putExtra("url",
                            url);
                    intent.setClassName("tv.ismar.daisy",
                            "tv.ismar.daisy.TopicActivity");
                    getActivity().startActivity(intent);
                } else if ("section".equals(contentMode)) {
                    intent.putExtra("title", title);
                    intent.putExtra("itemlistUrl",
                            url);
                    intent.putExtra("lableString",
                            title);
                    intent.setClassName("tv.ismar.daisy",
                            "tv.ismar.daisy.PackageListDetailActivity");
                    getActivity().startActivity(intent);
                } else if ("package".equals(contentMode)) {

                }
            }
        }
    };
}



