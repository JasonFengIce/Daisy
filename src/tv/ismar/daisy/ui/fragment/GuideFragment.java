package tv.ismar.daisy.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import tv.ismar.daisy.R;

/**
 * Created by huaijie on 5/18/15.
 */
public class GuideFragment extends Fragment {
    private LinearLayout guideRecommmendList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_guide, null);
        guideRecommmendList = (LinearLayout) mView.findViewById(R.id.recommend_list);
        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        for (int i = 0; i < 8; i++) {

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
            params.weight = 1;
            if (i != 7) {
                params.setMargins(0, 0, 25, 0);
            }
            ImageView itemView = new ImageView(getActivity());
            itemView.setImageResource(R.drawable.ic_launcher);
            itemView.setScaleType(ImageView.ScaleType.FIT_XY);
            itemView.setLayoutParams(params);

            guideRecommmendList.addView(itemView);
        }
    }

}



