package tv.ismar.daisy.ui.widget.image_indicator;

import android.support.v4.view.ViewPager;

/**
 * Created by huaijie on 2015/4/8.
 */
public interface PagerImageIndicator extends ViewPager.OnPageChangeListener {


    void setViewPager(ViewPager viewPager);

    void setCurrentItem(int item);


}
