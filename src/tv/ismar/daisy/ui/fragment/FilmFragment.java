package tv.ismar.daisy.ui.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import tv.ismar.daisy.R;
import tv.ismar.daisy.adapter.PagerImageAdapter;
import tv.ismar.daisy.ui.widget.image_indicator.IconPagerIndicator;

import java.util.ArrayList;

/**
 * Created by huaijie on 5/18/15.
 */
public class FilmFragment extends Fragment {
    private static final String TAG = "FilmFragment";

    private ImageView secOneImg1;
    private ImageView secOneImg2;

    private ViewPager viewPager;

    private IconPagerIndicator pagerIndicator;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_film, null);
        return mView;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ArrayList<ImageView> imgs = new ArrayList<ImageView>();
        for (int i = 0; i < 3; i++) {
            ImageView imageView = new ImageView(getActivity());
            imageView.setImageResource(R.drawable.ic_launcher);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imgs.add(imageView);
        }

        secOneImg1 = (ImageView) view.findViewById(R.id.sec_one_img_1);
        secOneImg2 = (ImageView) view.findViewById(R.id.sec_one_img_2);
        viewPager = (ViewPager) view.findViewById(R.id.sec_one_pager);
        pagerIndicator = (IconPagerIndicator) view.findViewById(R.id.pager_indicator);

        PagerImageAdapter filmPagerAdapter = new PagerImageAdapter(imgs);
        viewPager.setAdapter(filmPagerAdapter);
    }


}
