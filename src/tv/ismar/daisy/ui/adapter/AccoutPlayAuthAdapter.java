package tv.ismar.daisy.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import tv.ismar.daisy.R;
import tv.ismar.daisy.data.usercenter.AccountPlayAuthEntity;

import java.util.ArrayList;

/**
 * Created by huaijie on 7/3/15.
 */
public class AccoutPlayAuthAdapter extends BaseAdapter {
    ArrayList<AccountPlayAuthEntity.PlayAuth> mList;
    Context mContext;
    ViewHolder holder;

    public AccoutPlayAuthAdapter(Context context, ArrayList<AccountPlayAuthEntity.PlayAuth> list) {
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.privilege_listview_item, null);
            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.title_txt);
            holder.buydate_txt = (TextView) convertView.findViewById(R.id.buydate_txt);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        AccountPlayAuthEntity.PlayAuth item = mList.get(position);
        holder.title.setText(item.getTitle());
        holder.buydate_txt.setText(item.getExpiry_date());
        return convertView;
    }

    public static class ViewHolder {
        TextView title;
        TextView buydate_txt;
    }
}
