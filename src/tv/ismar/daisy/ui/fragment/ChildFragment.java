package tv.ismar.daisy.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.media.Image;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.*;
import com.squareup.picasso.Picasso;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import tv.ismar.daisy.R;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.client.ClientApi;
import tv.ismar.daisy.data.HomePagerEntity;

import java.util.ArrayList;

import static tv.ismar.daisy.core.client.ClientApi.restAdapter_SKYTEST_TVXIO;

/**
 * Created by huaijie on 5/18/15.
 */
public class ChildFragment extends Fragment {
    private static final String TAG = "ChildFragment";

    private Context context;
    private LinearLayout leftLayout;
    private LinearLayout bottomLayout;
    private LinearLayout rightLayout;
    private ImageView imageSwitcher;
    private ImageView[] indicatorImgs;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.context = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_child, null);
        leftLayout = (LinearLayout) mView.findViewById(R.id.left_layout);
        bottomLayout = (LinearLayout) mView.findViewById(R.id.bottom_layout);
        rightLayout = (LinearLayout) mView.findViewById(R.id.right_layout);
        imageSwitcher = (ImageView) mView.findViewById(R.id.image_switcher);
        indicatorImgs = new ImageView[]{
                (ImageView) mView.findViewById(R.id.indicator_1),
                (ImageView) mView.findViewById(R.id.indicator_2),
                (ImageView) mView.findViewById(R.id.indicator_3)
        };


        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        fetchChild();
    }

    private void fetchChild() {
        ClientApi.Child client = restAdapter_SKYTEST_TVXIO.create(ClientApi.Child.class);
        client.excute("dZTLzDpmhmDbeGYs0euiGWjTY70nbAMABnkVUR1vtnc%3D", new Callback<HomePagerEntity>() {
            @Override
            public void success(HomePagerEntity homePagerEntity, Response response) {
                ArrayList<HomePagerEntity.Poster> posters = homePagerEntity.getPosters();
                ArrayList<HomePagerEntity.Carousel> carousels = homePagerEntity.getCarousels();

                Log.d(TAG, "posters size: " + posters.size());
                Log.d(TAG, "carousels size: " + carousels.size());

                initPosters(posters);
                initCarousel(carousels);
            }

            @Override
            public void failure(RetrofitError retrofitError) {

            }
        });
    }


    private void initPosters(ArrayList<HomePagerEntity.Poster> posters) {
        LinearLayout.LayoutParams verticalParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        verticalParams.weight = 1;
        verticalParams.setMargins(20, 20, 20, 20);

        LinearLayout.LayoutParams horizontalParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        horizontalParams.weight = 1;
        horizontalParams.setMargins(20, 20, 20, 20);


        for (int i = 0; i < 7; i++) {
            View itemContainer = LayoutInflater.from(context).inflate(R.layout.item_comic_fragment, null);
            ImageView itemImg = (ImageView) itemContainer.findViewById(R.id.item_img);
            TextView itemText = (TextView) itemContainer.findViewById(R.id.item_title);
            itemText.setText(posters.get(i).getTitle());
            Picasso.with(context).load(posters.get(i).getCustom_image()).into(itemImg);
            if (i >= 0 && i < 3) {
                itemContainer.setLayoutParams(verticalParams);
                leftLayout.addView(itemContainer);
            }


            if (i >= 3 && i < 5) {
                itemContainer.setLayoutParams(horizontalParams);
                bottomLayout.addView(itemContainer);
            }


            if (i >= 5 && i < 7) {
                itemContainer.setLayoutParams(verticalParams);
                rightLayout.addView(itemContainer);
            }
        }
        ImageView imageView = new ImageView(context);
        imageView.setImageResource(R.drawable.selector_child_more);
        imageView.setLayoutParams(verticalParams);
        rightLayout.addView(imageView);
    }

    private void initCarousel(ArrayList<HomePagerEntity.Carousel> carousels) {
        for (int i = 0; i < 3; i++) {
            indicatorImgs[i].setTag(i);
            indicatorImgs[i].setOnFocusChangeListener(new IndicatorFocusChangeListener(carousels));
            Picasso.with(context).load(carousels.get(i).getThumb_image()).into(indicatorImgs[i]);
        }
    }


    class IndicatorFocusChangeListener implements View.OnFocusChangeListener {
        private ArrayList<HomePagerEntity.Carousel> carousels;

        public IndicatorFocusChangeListener(ArrayList<HomePagerEntity.Carousel> carousels) {
            this.carousels = carousels;
        }

        @Override
        public void onFocusChange(View view, boolean focus) {

            if (focus) {
                int position = (Integer) view.getTag();
                AnimationSet animationSet = new AnimationSet(true);
                ScaleAnimation scaleAnimation = new ScaleAnimation(1, 1, 1, 1.8f,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);
                scaleAnimation.setDuration(200);
                animationSet.addAnimation(scaleAnimation);
                animationSet.setFillAfter(true);
                view.startAnimation(animationSet);
                Picasso.with(context).load(carousels.get(position).getVideo_image()).into(imageSwitcher);

            } else {
                AnimationSet animationSet = new AnimationSet(true);
                ScaleAnimation scaleAnimation = new ScaleAnimation(1, 1f, 1.8f, 1f,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);
                scaleAnimation.setDuration(200);
                animationSet.addAnimation(scaleAnimation);
                animationSet.setFillAfter(true);
                view.startAnimation(animationSet);
            }

        }
    }
}
