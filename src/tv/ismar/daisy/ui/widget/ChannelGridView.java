package tv.ismar.daisy.ui.widget;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.ismartv.launcher.data.ChannelEntity;
import tv.ismar.daisy.R;

/**
 * Created by huaijie on 2015/3/26.
 */
public class ChannelGridView extends LinearLayout {

    private static final int itemIconRes[] = {
            R.drawable.history, R.drawable.chinese, R.drawable.teleplay,
            R.drawable.ent, R.drawable.sport, R.drawable.life,
            R.drawable.my, R.drawable.oversea, R.drawable.child,
            R.drawable.vip, R.drawable.music, R.drawable.icon_toplist
    };

    private Context context;

    public ChannelGridView(Context context) {
        super(context);
        this.context = context;
    }

    public ChannelGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public ChannelGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
    }

    /**
     * set adapter
     *
     * @param channelEntities data
     */
    public void setAdapter(ChannelEntity[] channelEntities) {

        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(5, 5, 5, 5);
        layoutParams.weight = 1;


        LayoutParams lineParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        lineParams.weight = 1;


        int count = 0;

        ItemClickListener itemClickListener = new ItemClickListener();

        for (int i = 1; i <= 2; i++) {
            LinearLayout lineLayout = new LinearLayout(context);
            for (int j = 0; j < 6; j++) {
                View itemView = LayoutInflater.from(context).inflate(R.layout.item_channel, null);
                RelativeLayout container = new RelativeLayout(context);

                container.setFocusable(true);
                container.setFocusableInTouchMode(true);
                container.setClickable(true);
                container.setBackgroundResource(R.drawable.content_selector);
                container.setTag(channelEntities[count]);
                container.setOnClickListener(itemClickListener);

                ImageView channelImage = (ImageView) itemView.findViewById(R.id.channel_img);
                TextView channelTitle = (TextView) itemView.findViewById(R.id.channel_title);
                channelImage.setImageResource(itemIconRes[count]);
                channelTitle.setText(channelEntities[count].getName());
                itemView.setLayoutParams(lineParams);
                container.addView(itemView);
                container.setLayoutParams(layoutParams);
                lineLayout.addView(container);
                count++;
            }
            addView(lineLayout, lineParams);
        }
        requestLayout();
    }

    class ItemClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ChannelEntity channelEntity = (ChannelEntity) view.getTag();
            intent.putExtra("title", channelEntity.getName());
            intent.putExtra("url", channelEntity.getUrl());
            intent.putExtra("channel", channelEntity.getChannel());
            intent.setClassName("tv.ismar.daisy",
                    "tv.ismar.daisy.ChannelListActivity");
            context.startActivity(intent);
        }
    }
}
