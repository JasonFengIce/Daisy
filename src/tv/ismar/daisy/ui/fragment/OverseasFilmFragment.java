package tv.ismar.daisy.ui.fragment;

import static tv.ismar.daisy.core.client.ClientApi.restAdapter_SKYTEST_TVXIO;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import tv.ismar.daisy.R;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.client.ClientApi;
import tv.ismar.daisy.core.client.ClientApi.Overseas;
import tv.ismar.daisy.core.client.IsmartvFileClient;
import tv.ismar.daisy.data.HomePagerEntity;
import tv.ismar.daisy.data.HomePagerEntity.Carousel;
import tv.ismar.daisy.data.HomePagerEntity.Poster;
import tv.ismar.daisy.ui.CarouselUtils;
import tv.ismar.daisy.ui.widget.DaisyVideoView;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

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
        linkedVideoImage = (ImageView) mView.findViewById(R.id.film_linked_image);
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

    private void initCarousel(final ArrayList<HomePagerEntity.Carousel> carousels) {

       final CarouselUtils carouselUtils = new CarouselUtils();
        getView().postDelayed(new Runnable() {
            @Override
            public void run() {
                carouselUtils.loopCarousel(context, carousels, linkedVideoView, linkedVideoImage);
            }
        },1000);
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
            itemView.setTag(i);
            itemView.setTag(R.drawable.launcher_selector,carousels.get(i));
            itemView.setOnClickListener(ItemClickListener);
            itemView.setOnFocusChangeListener(carouselUtils.listener);
            carouselLayout.addView(itemView);
        }
        // downloadCarouselVideo(carousels);

    }

    private void downloadCarouselVideo(
            ArrayList<HomePagerEntity.Carousel> carousels) {
        new IsmartvFileClient(context, carousels).start();
    }


    private View.OnClickListener ItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
        	String url = null;
        	String contentMode =null;
        	String title = null;
        	if (view.getTag()  instanceof Poster) {
        		Poster new_name = (Poster) view.getTag();
        		contentMode = new_name.getModel_name();
        		url =new_name.getUrl();
        		title = new_name.getTitle();
			}else if(view.getTag(R.drawable.launcher_selector)  instanceof Carousel){
				Carousel new_name = (Carousel) view.getTag(R.drawable.launcher_selector);
        		contentMode = new_name.getModel_name();
        		url =new_name.getUrl();
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
                }  else if ("topic".equals(contentMode)) {
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
