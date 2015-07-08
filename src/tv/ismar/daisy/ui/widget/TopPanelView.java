package tv.ismar.daisy.ui.widget;

import tv.ismar.daisy.R;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TopPanelView extends LinearLayout implements View.OnFocusChangeListener {

    private TextView channelName;
    private TextView searchview;
    private TextView messageview;
    private TextView usercenterview;
    private TextView favoriteview;
    private TextView historyview;
    private View guide_channelsecondname;
    private View secondline;

    public TopPanelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater mInflater = LayoutInflater.from(context);
        View myView = mInflater.inflate(R.layout.top_panel, null);
        addView(myView);
        secondline = findViewById(R.id.secondline);
        guide_channelsecondname = findViewById(R.id.guide_channelsecondname);
        channelName = (TextView) findViewById(R.id.guide_channelname);
        searchview = (TextView) findViewById(R.id.guide_search);
        messageview = (TextView) findViewById(R.id.guide_message);
        usercenterview = (TextView) findViewById(R.id.guide_user_center);
        favoriteview = (TextView) findViewById(R.id.guide_my_favorite);
        historyview = (TextView) findViewById(R.id.guide_play_history);
        searchview.setOnClickListener(viewClickListener);
        messageview.setOnClickListener(viewClickListener);
        usercenterview.setOnClickListener(viewClickListener);
        favoriteview.setOnClickListener(viewClickListener);
        historyview.setOnClickListener(viewClickListener);

        historyview.setOnFocusChangeListener(this);
        favoriteview.setOnFocusChangeListener(this);
        usercenterview.setOnFocusChangeListener(this);
        searchview.setOnFocusChangeListener(this);
    }


    private View.OnClickListener viewClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            switch (v.getId()) {
                case R.id.guide_search:
                    intent.setClassName("tv.ismar.daisy",
                            "tv.ismar.daisy.SearchActivity");
                    break;
                case R.id.guide_message:
                    intent.setClassName("tv.ismar.daisy",
                            "tv.ismar.daisy.SearchActivity");
                    break;
                case R.id.guide_user_center:
                    intent.setClassName("tv.ismar.daisy",
                            "tv.ismar.daisy.ui.activity.UserCenterActivity");
                    break;
                case R.id.guide_my_favorite:
                    intent.setClassName("tv.ismar.daisy",
                            "tv.ismar.daisy.ChannelListActivity");
                    intent.putExtra("channel", "$bookmarks");
                    break;
                case R.id.guide_play_history:
                    intent.setClassName("tv.ismar.daisy",
                            "tv.ismar.daisy.ChannelListActivity");
                    intent.putExtra("channel", "$histories");
                    break;
            }
            getContext().startActivity(intent);
        }
    };

    public void setChannelName(String channeltitle) {
        channelName.setText(channeltitle);
    }

    public void setSecondChannelVisable() {
        guide_channelsecondname.setVisibility(View.VISIBLE);
        secondline.setVisibility(View.VISIBLE);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            ((TextView) v).setTextColor(getResources().getColor(R.color.association_focus));
        } else {
            ((TextView) v).setTextColor(getResources().getColor(R.color.association_normal));
        }
    }
}
