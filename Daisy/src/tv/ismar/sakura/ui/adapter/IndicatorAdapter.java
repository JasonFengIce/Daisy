package tv.ismar.sakura.ui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import tv.ismar.sakura.ui.widget.indicator.IconPagerAdapter;

import java.util.ArrayList;

/**
 * Created by huaijie on 2015/4/8.
 */
public class IndicatorAdapter extends FragmentPagerAdapter implements IconPagerAdapter {
    ArrayList<Fragment> fragments;

    public IndicatorAdapter(FragmentManager fm, ArrayList<Fragment> fragments) {
        super(fm);
        this.fragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getIconResId(int index) {
        return 0;
    }

    @Override
    public int getCount() {
        return fragments.size();
    }
}
