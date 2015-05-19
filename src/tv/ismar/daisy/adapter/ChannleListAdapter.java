package tv.ismar.daisy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import tv.ismar.daisy.R;

import java.util.ArrayList;

/**
 * Created by huaijie on 5/19/15.
 */
public class ChannleListAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<String> channels;

    public ChannleListAdapter(Context context, ArrayList<String> channels) {
        this.context = context;
        this.channels = channels;
    }

    @Override
    public int getCount() {
        return channels.size();
    }

    @Override
    public Object getItem(int position) {
        return channels.get(position);
    }

    @Override
    public long getItemId(int id) {
        return id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder holder;
        if (convertView != null) {
            holder = (ViewHolder) convertView.getTag();
        } else {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_channel_, null);
            holder = new ViewHolder();
            holder.channelName = (TextView) convertView.findViewById(R.id.channel_name);
            convertView.setTag(holder);
        }
        holder.channelName.setText(channels.get(position));
        return convertView;
    }

    private class ViewHolder {
        TextView channelName;
    }
}
