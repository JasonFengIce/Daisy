package tv.ismar.daisy.ui.widget;

import android.content.Context;
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
public class GuideItemView extends LinearLayout {
    private Context context;

    public GuideItemView(Context context) {
        super(context);
        this.context = context;
    }

    public GuideItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public GuideItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }


    public void setAdapter(ArrayList<AttributeEntity> attributeEntities) {
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(5,5,5,5);
        layoutParams.weight = 1;
        for (int i = 0; i < attributeEntities.size(); i++) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_linkedvideo, null);
            TextView textView = (TextView) view.findViewById(R.id.video_title);
            ImageView imageView = (ImageView) view.findViewById(R.id.video_image);
            textView.setText(attributeEntities.get(i).getAttributes().getTitle());
            Picasso.with(context).load(attributeEntities.get(i).getAttributes().getPoster_url()).into(imageView);
            addView(view, layoutParams);
        }


        requestLayout();
    }

}
