package tv.ismar.daisy.ui.widget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import tv.ismar.daisy.R;
import tv.ismar.daisy.core.VodUserAgent;

/**
 * Created by admin on 2016/6/6.
 */
public class ListHeaderLayout extends RelativeLayout implements View.OnClickListener {

    private TextView list_channel_title;
    private ImageView list_arrow_back;
    private TextView list_channel;
    private TextView list_search;
    private Context context;

    public ListHeaderLayout(Context context) {
        this(context, null);
    }

    public ListHeaderLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ListHeaderLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
        initData();
    }

    private void initView(Context context) {
        this.context=context;
        View view=View.inflate(context, R.layout.list_top_layout,this);
        list_arrow_back = (ImageView) view.findViewById(R.id.list_arrow_back);
        list_channel_title = (TextView) view.findViewById(R.id.list_channel_title);
        list_channel = (TextView) view.findViewById(R.id.list_channel);
        list_search = (TextView)view.findViewById(R.id.list_search);

    }
    private void initData() {
        list_arrow_back.setOnClickListener(this);
        list_channel.setOnClickListener(this);
        list_search.setOnClickListener(this);
    }
    public void setChannelTitle(String title) {
        list_channel_title.setText(title);
    }

    @Override
    public void onClick(View v) {
        Intent intent=new Intent();
        switch (v.getId()){
            case R.id.list_arrow_back:
                ((Activity)context).finish();
                break;
            case R.id.list_channel:
                intent.setAction("tv.ismar.daisy.fullchannel");
                context.startActivity(intent);
                break;
            case R.id.list_search:
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if ("lcd_xxbel8a".equalsIgnoreCase(VodUserAgent.getModelName())) {
                    intent.setAction("cn.ismartv.jasmine.wordsearchactivity");
                } else {
                    intent.setAction("tv.ismar.daisy.Search");
                }
                context.startActivity(intent);
                break;
        }
    }
}
