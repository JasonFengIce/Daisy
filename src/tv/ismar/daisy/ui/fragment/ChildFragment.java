package tv.ismar.daisy.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import tv.ismar.daisy.R;
import tv.ismar.daisy.core.client.IsmartvUrlClient;
import tv.ismar.daisy.data.HomePagerEntity;
import tv.ismar.daisy.ui.CarouselUtils;

import java.util.ArrayList;

/**
 * Created by huaijie on 5/18/15.
 */
public class ChildFragment extends ChannelBaseFragment {
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

        fetchChild(channelEntity.getHomepage_url());
    }

    private void fetchChild(String url) {
        new IsmartvUrlClient(context).doRequest(url, new IsmartvUrlClient.CallBack() {
            @Override
            public void onSuccess(String result) {
                HomePagerEntity homePagerEntity = new Gson().fromJson(result, HomePagerEntity.class);
                ArrayList<HomePagerEntity.Poster> posters = homePagerEntity.getPosters();
                ArrayList<HomePagerEntity.Carousel> carousels = homePagerEntity.getCarousels();

                Log.d(TAG, "posters size: " + posters.size());
                Log.d(TAG, "carousels size: " + carousels.size());

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
        int marginLR = (int) context.getResources().getDimension(R.dimen.child_fragment_item_margin_lr);
        int marginTP = (int) getResources().getDimension(R.dimen.child_fragment_item_margin_tp);
        Log.d(TAG, "margin lr: " + marginLR);

        for (int i = 0; i < 7; i++) {
            View itemContainer = LayoutInflater.from(context).inflate(R.layout.item_comic_fragment, null);
            itemContainer.setBackgroundResource(R.drawable.selector_child_item);
            itemContainer.setFocusable(true);
            itemContainer.setFocusableInTouchMode(true);
            itemContainer.setClickable(true);
            itemContainer.setOnClickListener(ItemClickListener);
            itemContainer.setTag(posters);

            ImageView itemImg = (ImageView) itemContainer.findViewById(R.id.item_img);
            TextView itemText = (TextView) itemContainer.findViewById(R.id.item_title);

            Picasso.with(context).load(posters.get(i).getCustom_image()).into(itemImg);
            itemText.setText(posters.get(i).getTitle());
            if (i >= 0 && i < 3) {
                LinearLayout.LayoutParams verticalParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
                verticalParams.weight = 1;
                if (i == 2) {
                    verticalParams.setMargins(marginLR, marginTP, 0, marginTP);
                } else {
                    verticalParams.setMargins(marginLR, marginTP, 0, 0);
                }
                itemContainer.setLayoutParams(verticalParams);
                leftLayout.addView(itemContainer);
            }


            if (i >= 3 && i < 5) {
                LinearLayout.LayoutParams horizontalParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
                horizontalParams.weight = 1;
                if (i == 3) {
                    horizontalParams.setMargins(0, marginTP, (int) getResources().getDimension(R.dimen.child_fragment_bottomlayout_margin), 0);
                } else if (i == 4) {
                    horizontalParams.setMargins(0, marginTP, 0, 0);
                }

                itemContainer.setLayoutParams(horizontalParams);
                bottomLayout.addView(itemContainer);
            }


            if (i >= 5 && i < 7) {
                LinearLayout.LayoutParams verticalParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
                verticalParams.weight = 1;

                verticalParams.setMargins(0, marginTP, marginLR, 0);
                itemContainer.setLayoutParams(verticalParams);
                rightLayout.addView(itemContainer);
            }
        }
        ImageView imageView = new ImageView(context);
        imageView.setFocusable(true);
        imageView.setFocusableInTouchMode(true);
        imageView.setClickable(true);
        imageView.setImageResource(R.drawable.selector_child_more);
        imageView.setOnClickListener(ItemClickListener);
        LinearLayout.LayoutParams verticalParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        verticalParams.weight = 1;
        verticalParams.setMargins(0, 0, marginLR, marginTP);
        imageView.setLayoutParams(verticalParams);
        rightLayout.addView(imageView);
        rightLayout.requestLayout();
    }

    private void initCarousel(ArrayList<HomePagerEntity.Carousel> carousels) {
        CarouselUtils carouselUtils = new CarouselUtils();
        carouselUtils.loopCarousel(context, carousels, imageSwitcher, new CarouselUtils.ImageIndicatorCallback() {
            @Override
            public void indicatorChanged(int hide, int show) {
                zoomIn(indicatorImgs[hide]);
                zoomOut(indicatorImgs[show]);

            }
        });
        for (int i = 0; i < 3; i++) {
            indicatorImgs[i].setTag(i);
            indicatorImgs[i].setOnFocusChangeListener(carouselUtils.scaleListener);
            Picasso.with(context).load(carousels.get(i).getThumb_image()).into(indicatorImgs[i]);
        }
    }


    private void zoomIn(View view) {
        AnimationSet animationSet = new AnimationSet(true);
        ScaleAnimation scaleAnimation = new ScaleAnimation(1, 1f, 1.53f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(200);
        animationSet.addAnimation(scaleAnimation);
        animationSet.setFillAfter(true);
        view.startAnimation(animationSet);
    }

    private void zoomOut(View view) {
        AnimationSet animationSet = new AnimationSet(true);
        ScaleAnimation scaleAnimation = new ScaleAnimation(1, 1, 1, 1.53f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(200);
        animationSet.addAnimation(scaleAnimation);
        animationSet.setFillAfter(true);
        view.startAnimation(animationSet);
    }

    private View.OnClickListener ItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String url = null;
            String contentMode = null;
            String title = null;
            if (view.getTag() instanceof HomePagerEntity.Poster) {
                HomePagerEntity.Poster new_name = (HomePagerEntity.Poster) view.getTag();
                contentMode = new_name.getModel_name();
                url = new_name.getUrl();
                title = new_name.getTitle();
            } else if (view.getTag(R.drawable.launcher_selector) instanceof HomePagerEntity.Carousel) {
                HomePagerEntity.Carousel new_name = (HomePagerEntity.Carousel) view.getTag(R.drawable.launcher_selector);
                contentMode = new_name.getModel_name();
                url = new_name.getUrl();
                title = new_name.getTitle();
            }
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (url == null) {
                intent.putExtra("title", "华语电影");
                intent.putExtra("url",
                        channelEntity.getUrl());
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


