package tv.ismar.daisy.ui.fragment;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.squareup.picasso.Picasso;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import tv.ismar.daisy.R;
import tv.ismar.daisy.adapter.PagerImageAdapter;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.client.ClientApi;
import tv.ismar.daisy.core.client.IsmartvUrlClient;
import tv.ismar.daisy.data.HomePagerEntity;
import tv.ismar.daisy.ui.widget.image_indicator.IconPagerIndicator;
import tv.ismar.sakura.data.http.TeleEntity;

import java.util.ArrayList;

import static tv.ismar.daisy.core.client.ClientApi.restAdapter_SKYTEST_TVXIO;

/**
 * Created by huaijie on 5/18/15.
 */
public class FilmFragment extends Fragment {
    private static final String TAG = "FilmFragment";

    private ImageView secOneImg1;
    private ImageView secOneImg2;

    private ViewPager viewPager;

    private IconPagerIndicator pagerIndicator;

    private String device_token = "";

    private String url;

    private Context context;

    private LinearLayout guideRecommmendList;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        device_token = SimpleRestClient.device_token;
        url = getArguments().getString("url");

        Log.d(TAG, "tag is: " + url);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_film, null);
        guideRecommmendList = (LinearLayout) mView.findViewById(R.id.recommend_list);
        return mView;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fetchOverSeas();

//        ArrayList<ImageView> imgs = new ArrayList<ImageView>();
//        for (int i = 0; i < 3; i++) {
//            ImageView imageView = new ImageView(getActivity());
//            imageView.setImageResource(R.drawable.ic_launcher);
//            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
//            imgs.add(imageView);
//        }
//
//
//        secOneImg1 = (ImageView) view.findViewById(R.id.sec_one_img_1);
//        secOneImg2 = (ImageView) view.findViewById(R.id.sec_one_img_2);
//        viewPager = (ViewPager) view.findViewById(R.id.sec_one_pager);
//        pagerIndicator = (IconPagerIndicator) view.findViewById(R.id.pager_indicator);
//
//        PagerImageAdapter filmPagerAdapter = new PagerImageAdapter(imgs);
//        viewPager.setAdapter(filmPagerAdapter);
    }


    private void fetchOverSeas() {
        ClientApi.OverSeas client = restAdapter_SKYTEST_TVXIO.create(ClientApi.OverSeas.class);
        client.excute("", new Callback<HomePagerEntity>() {
            @Override
            public void success(HomePagerEntity homePagerEntity, Response response) {
                ArrayList<HomePagerEntity.Carousel> carousels = homePagerEntity.getCarousels();
                ArrayList<HomePagerEntity.Poster> posters = homePagerEntity.getPosters();
                initPosters(posters);
//                initCarousel(carousels);
            }

            @Override
            public void failure(RetrofitError retrofitError) {

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
            ImageView itemView = new ImageView(context);
            String posterImgUrl = "";
            try {
                posterImgUrl = posters.get(i).getCustom_image();
            } catch (Exception e) {
                Log.e(TAG, "may be poster image is null");
            }

            if (!TextUtils.isEmpty(posterImgUrl))
                Picasso.with(context).load(posterImgUrl).into(itemView);

            itemView.setScaleType(ImageView.ScaleType.FIT_XY);
            itemView.setLayoutParams(params);
            guideRecommmendList.addView(itemView);
        }
    }

}
