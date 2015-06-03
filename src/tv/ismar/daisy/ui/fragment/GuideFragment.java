package tv.ismar.daisy.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.squareup.picasso.Picasso;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import tv.ismar.daisy.R;
import tv.ismar.daisy.core.client.ClientApi;
import tv.ismar.daisy.core.client.IsmartvFileClient;
import tv.ismar.sakura.data.http.HomePagerEntity;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_guide, null);
        guideRecommmendList = (LinearLayout) mView.findViewById(R.id.recommend_list);
        carouselLayout = (LinearLayout) mView.findViewById(R.id.carousel_layout);
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
            if (i != 7) {
                params.setMargins(0, 0, 25, 0);
            }
            ImageView itemView = new ImageView(getActivity());
            Picasso.with(getActivity()).load(posters.get(i).getCustom_url()).into(itemView);
            itemView.setScaleType(ImageView.ScaleType.FIT_XY);
            itemView.setLayoutParams(params);
            guideRecommmendList.addView(itemView);
        }
    }

    private void initCarousel(ArrayList<HomePagerEntity.Carousel> carousels) {

        new IsmartvFileClient(getActivity(), carousels.get(0).getVideo_url(), new IsmartvFileClient.CallBack() {
            @Override
            public void onSuccess(String result) {

            }

            @Override
            public void onFailed(Exception exception) {

            }
        }).start();

        for (int i = 0; i < 3; i++) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
            params.weight = 1;
            ImageView itemView = new ImageView(getActivity());
            Picasso.with(getActivity()).load(carousels.get(i).getCustom_url()).into(itemView);
            itemView.setScaleType(ImageView.ScaleType.FIT_XY);
            itemView.setLayoutParams(params);
            carouselLayout.addView(itemView);


        }

    }
}



