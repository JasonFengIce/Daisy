package tv.ismar.daisy.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.ismartv.launcher.data.ChannelEntity;
import org.json.JSONException;
import org.json.JSONObject;
import tv.ismar.daisy.R;

/**
 * Created by huaijie on 3/18/15.
 */
public class ChannelAdapter extends BaseAdapter {

    private Context context;
    private ChannelEntity[] channelEntities;

    int itemIconRes[] = {R.drawable.history, R.drawable.chinese, R.drawable.teleplay, R.drawable.ent, R.drawable.sport, R.drawable.life,
            R.drawable.my, R.drawable.oversea, R.drawable.child, R.drawable.vip, R.drawable.music, R.drawable.icon_toplist
    };

    public ChannelAdapter(Context context, ChannelEntity[] channelEntities) {
        this.context = context;
        this.channelEntities = channelEntities;
    }

    @Override
    public int getCount() {
        return channelEntities.length;
    }

    @Override
    public Object getItem(int position) {
        return position;
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
            convertView = LayoutInflater.from(context).inflate(R.layout.item_channelgrid, parent, false);
            holder = new ViewHolder();
            holder.itemImage = (ImageView) convertView.findViewById(R.id.channel_img);
            holder.itemTitle = (TextView) convertView.findViewById(R.id.channel_title);
            convertView.setTag(holder);
        }
        holder.itemImage.setImageResource(itemIconRes[position]);
        holder.itemTitle.setTextColor(Color.WHITE);
        TextPaint tp = holder.itemTitle.getPaint();
        tp.setFakeBoldText(true);
        holder.itemTitle.setText(channelEntities[position].getName());

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", channelEntities[position].getName());
            jsonObject.put("url", channelEntities[position].getUrl());
            jsonObject.put("channel", channelEntities[position].getChannel());
        } catch (JSONException e) {
            if (e != null)
                e.printStackTrace();
        }
        holder.itemImage.setTag(jsonObject.toString());

        return convertView;
    }

    class ViewHolder {
        private ImageView itemImage;
        private TextView itemTitle;
    }
}
