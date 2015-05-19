package tv.ismar.daisy.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import tv.ismar.daisy.R;
import tv.ismar.daisy.adapter.GuideContentAdapter;

import java.util.ArrayList;


/**
 * Created by huaijie on 5/18/15.
 */
public class FilmFragmentContainer extends Fragment {

    private static final String TAG = "FragmentContainer";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_container, null);
        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewPager viewPager = (ViewPager) view.findViewById(R.id.viewPager);
        ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(new FilmFragment());
        fragments.add(new HDetailFragment());
        viewPager.setAdapter(new GuideContentAdapter(getFragmentManager(), fragments));

    }
}
