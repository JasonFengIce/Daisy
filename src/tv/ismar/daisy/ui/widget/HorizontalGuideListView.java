package tv.ismar.daisy.ui.widget;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.ismartv.launcher.data.VideoEntity;
import com.squareup.picasso.Picasso;
import tv.ismar.daisy.R;

/**
 * Created by huaijie on 2015/3/26.
 */
public class HorizontalGuideListView extends LinearLayout {


    private Context context;

    public HorizontalGuideListView(Context context) {
        super(context);
        this.context = context;
    }

    public HorizontalGuideListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public HorizontalGuideListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }


    public void setAdapter(VideoEntity videoEntity) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(5, 5, 5, 5);
        layoutParams.weight = 1;

        ItemClickListener clickListener = new ItemClickListener();

        for (int i = 0; i < videoEntity.getObjects().size(); i++) {
            ContainerLayout container = (ContainerLayout) LayoutInflater.from(context).inflate(R.layout.item_linkedvideo, null);
            container.setFocusable(true);
            container.setFocusableInTouchMode(true);
            container.setClickable(true);
            container.setBackgroundResource(R.drawable.launcher_selector);
            container.setTag(videoEntity.getObjects().get(i));

            TextView textView = (TextView) container.findViewById(R.id.video_title);
            ImageView imageView = (ImageView) container.findViewById(R.id.video_image);
            textView.setText(videoEntity.getObjects().get(i).getTitle());
            Picasso.with(context).load(videoEntity.getObjects().get(i).getImage()).into(imageView);
            container.setOnClickListener(clickListener);
            container.setLayoutParams(layoutParams);

            addView(container);
        }
        requestLayout();
    }


    private class ItemClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            VideoEntity.Objects objects = (VideoEntity.Objects) view.getTag();
            intent.setClassName("tv.ismar.daisy", "tv.ismar.daisy.ItemDetailActivity");
            intent.putExtra("url", objects.getItem_url());
            context.startActivity(intent);
        }
    }

}
