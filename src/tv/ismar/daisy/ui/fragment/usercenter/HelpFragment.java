package tv.ismar.daisy.ui.fragment.usercenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import tv.ismar.daisy.R;
import tv.ismar.sakura.LauncherActivity;

/**
 * Created by huaijie on 7/3/15.
 */
public class HelpFragment extends Fragment implements View.OnClickListener {
    private ImageButton ismartvIcon;

    private Context mContext;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mContext = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_help, null);
        ismartvIcon = (ImageButton) view.findViewById(R.id.ismartv_icon);
        ismartvIcon.setOnClickListener(this);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ismartv_icon:
                startSakura();
                break;
        }
    }


    private void startSakura() {
        Intent intent = new Intent();
        intent.setClass(mContext, LauncherActivity.class);
        startActivity(intent);
    }
}
