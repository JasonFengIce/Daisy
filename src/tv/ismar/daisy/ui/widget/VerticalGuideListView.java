package tv.ismar.daisy.ui.widget;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import tv.ismar.daisy.R;
import tv.ismar.daisy.models.launcher.AttributeEntity;

import java.util.ArrayList;

/**
 * Created by huaijie on 2015/3/25.
 */
public class VerticalGuideListView extends LinearLayout {
    private Context context;

    public VerticalGuideListView(Context context) {
        super(context);
        this.context = context;
    }

    public VerticalGuideListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public VerticalGuideListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }


    public void setAdapter(ArrayList<AttributeEntity> attributeEntities) {
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(5, 5, 5, 5);
        layoutParams.weight = 1;

        ItemClickListener clickListener = new ItemClickListener();

        for (int i = 0; i < attributeEntities.size(); i++) {
            ContainerLayout container = (ContainerLayout) LayoutInflater.from(context).inflate(R.layout.item_linkedvideo, null);
            container.setFocusable(true);
            container.setFocusableInTouchMode(true);
            container.setClickable(true);
            container.setBackgroundResource(R.drawable.launcher_selector);
            container.setTag(attributeEntities.get(i));


            TextView textView = (TextView) container.findViewById(R.id.video_title);
            ImageView imageView = (ImageView) container.findViewById(R.id.video_image);
            textView.setText(attributeEntities.get(i).getAttributes().getTitle());
            Picasso.with(context).load(attributeEntities.get(i).getAttributes().getPoster_url()).into(imageView);
            container.setOnClickListener(clickListener);
            addView(container, layoutParams);
        }
        requestLayout();
    }


    private class ItemClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            AttributeEntity attributeEntity = (AttributeEntity) view.getTag();
            intent.setClassName("tv.ismar.daisy", "tv.ismar.daisy.ItemDetailActivity");
            intent.putExtra("url", attributeEntity.getAttributes().getUrl());
            context.startActivity(intent);
        }
    }

}
