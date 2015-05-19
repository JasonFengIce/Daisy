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

import java.util.ArrayList;

/**
 * Created by huaijie on 5/18/15.
 */
public class ChildFragment extends Fragment {
    private ViewPager viewPager;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_child, null);
        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ArrayList<ImageView> imgs = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            ImageView imageView = new ImageView(getActivity());
            imageView.setImageResource(R.drawable.ic_launcher);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imgs.add(imageView);
        }

        viewPager = (ViewPager)view.findViewById(R.id.sec_one_pager);
        PagerImageAdapter filmPagerAdapter = new PagerImageAdapter(imgs);
        viewPager.setAdapter(filmPagerAdapter);

    }
}
