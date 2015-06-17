package tv.ismar.daisy.ui.fragment;

import android.app.Activity;
import android.content.Context;
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
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import tv.ismar.daisy.R;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.client.ClientApi;
import tv.ismar.daisy.core.client.IsmartvFileClient;
import tv.ismar.daisy.core.preferences.SimpleClientPreferences;
import tv.ismar.daisy.data.HomePagerEntity;
import tv.ismar.daisy.ui.CarouselUtils;
import tv.ismar.daisy.ui.ItemViewFocusChangeListener;

import java.util.ArrayList;

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
        fetchHomePage(SimpleClientPreferences.getInstance(context).getDeviceToken());
    }


    public void fetchHomePage(String deviceToken) {
        Log.d(TAG, "fetchHomePage: " + SimpleRestClient.device_token);
        ClientApi.Homepage client = restAdapter_SKYTEST_TVXIO.create(Homepage.class);
        client.excute(deviceToken, new Callback<HomePagerEntity>() {
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
            itemView.setOnFocusChangeListener(carouselUtils.listener);
            carouselLayout.addView(itemView);
        }
        downloadCarouselVideo(carousels);

    }

    private void downloadCarouselVideo(ArrayList<HomePagerEntity.Carousel> carousels) {
        new IsmartvFileClient(context, carousels).start();
    }

}



