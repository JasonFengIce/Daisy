package tv.ismar.daisy.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import tv.ismar.daisy.R;
import tv.ismar.daisy.models.launcher.AttributeEntity;

import java.util.ArrayList;

/**
 * Created by huaijie on 3/18/15.
 */
public class LinkedVideoAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<AttributeEntity> attributeEntities;

    public LinkedVideoAdapter(Context context, ArrayList<AttributeEntity> attributeEntities) {
        this.context = context;
        this.attributeEntities = attributeEntities;

    }

    @Override
    public int getCount() {
        return attributeEntities.size();
    }

    @Override
    public Object getItem(int position) {
        return getItemId(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        ViewHolder holder;
        if (convertView != null) {
            holder = (ViewHolder) convertView.getTag();
        } else {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_linkedvideo, parent, false);
            holder = new ViewHolder();
            holder.videoImage = (ImageView) convertView.findViewById(R.id.video_image);
            holder.videoTitle = (TextView) convertView.findViewById(R.id.video_title);
            convertView.setTag(holder);
        }

        Picasso.with(context)
                .load(attributeEntities.get(position).getAttributes().getPoster_url())
                .into(holder.videoImage);
        holder.videoTitle.setText(attributeEntities.get(position).getAttributes().getTitle());
        return convertView;
    }

    class ViewHolder {
        private ImageView videoImage;
        private TextView videoTitle;
    }
}
