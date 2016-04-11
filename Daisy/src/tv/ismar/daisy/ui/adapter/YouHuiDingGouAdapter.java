package tv.ismar.daisy.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import tv.ismar.daisy.R;
import tv.ismar.daisy.data.usercenter.YouHuiDingGouEntity;

/**
 * Created by huaijie on 7/3/15.
 */
public class YouHuiDingGouAdapter extends BaseAdapter implements View.OnClickListener {
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
            holder.icon = (ImageView) convertView
                    .findViewById(R.id.package_grid_image);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        YouHuiDingGouEntity.Object item = mList.get(position);
        holder.title.setText(item.getTitle());
        Picasso.with(mContext).load(item.getPoster_url()).into(holder.icon);

        holder.icon.setTag(position);
        // holder.icon.setOnClickListener(this);

        return convertView;
    }

    @Override
    public void onClick(View v) {
        int position = (Integer) v.getTag();
        YouHuiDingGouEntity.Object o = mList.get(position);
        Intent intent = new Intent();
        intent.setAction("tv.ismar.daisy.packageitem");
        intent.putExtra("url", o.getUrl());
        mContext.startActivity(intent);

    }

    public static class ViewHolder {
        ImageView icon;
        TextView title;
    }
}
