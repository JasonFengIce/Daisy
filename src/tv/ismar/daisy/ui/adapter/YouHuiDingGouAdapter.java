package tv.ismar.daisy.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import tv.ismar.daisy.R;
import tv.ismar.daisy.data.usercenter.YouHuiDingGouEntity;
import tv.ismar.daisy.views.AsyncImageView;

import java.util.ArrayList;

/**
 * Created by huaijie on 7/3/15.
 */
public class YouHuiDingGouAdapter extends BaseAdapter {
    ArrayList<YouHuiDingGouEntity.Object> mList;
    Context mContext;
    ViewHolder holder;

    public YouHuiDingGouAdapter(Context context, ArrayList<YouHuiDingGouEntity.Object> list) {
        this.mList = list;
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int id) {
        return id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.package_gridlist_item, null);
            holder = new ViewHolder();
            holder.title = (TextView) convertView
                    .findViewById(R.id.package_grid_title);
            holder.icon = (AsyncImageView) convertView
                    .findViewById(R.id.package_grid_image);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        YouHuiDingGouEntity.Object item = mList.get(position);
        holder.title.setText(item.getTitle());
        holder.icon.setUrl(item.getPoster_url());
        return convertView;
    }

    public static class ViewHolder {
        AsyncImageView icon;
        TextView title;
    }
}
