package tv.ismar.daisy.ui.adapter;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

import tv.ismar.daisy.R;
import tv.ismar.daisy.data.usercenter.AccountPlayAuthEntity;
import tv.ismar.daisy.utils.Util;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * Created by huaijie on 7/3/15.
 */
public class AccoutPlayAuthAdapter extends BaseAdapter implements View.OnFocusChangeListener {
    ArrayList<AccountPlayAuthEntity.PlayAuth> mList;
    Context mContext;
    ViewHolder holder;

    public AccoutPlayAuthAdapter(Context context,
                                 ArrayList<AccountPlayAuthEntity.PlayAuth> list) {
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

        convertView.setOnFocusChangeListener(this);

        String remainday = mContext.getResources().getString(
                R.string.personcenter_orderlist_item_remainday);
        AccountPlayAuthEntity.PlayAuth item = mList.get(position);
        holder.title.setText(item.getTitle());
        holder.buydate_txt.setText(String.format(remainday, remaindDay(item.getExpiry_date())));
        return convertView;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        TextView titleTextView = (TextView) v.findViewById(R.id.title_txt);
        TextView remaindDay = (TextView)v.findViewById(R.id.buydate_txt);
        if (hasFocus) {
            titleTextView.setTextColor(mContext.getResources().getColor(R.color.location_text_focus));
            titleTextView.setTextSize(mContext.getResources().getDimension(R.dimen.userinfo_playauth_item_focus_textsize));
            remaindDay.setTextColor(mContext.getResources().getColor(R.color.location_text_focus));
            remaindDay.setTextSize(mContext.getResources().getDimension(R.dimen.userinfo_playauth_item_focus_textsize));
        } else {
            titleTextView.setTextColor(mContext.getResources().getColor(R.color.white));
            titleTextView.setTextSize(mContext.getResources().getDimension(R.dimen.userinfo_playauth_item_normal_textsize));
            remaindDay.setTextColor(mContext.getResources().getColor(R.color.white));
            remaindDay.setTextSize(mContext.getResources().getDimension(R.dimen.userinfo_playauth_item_normal_textsize));
        }
    }

    public static class ViewHolder {
        TextView title;
        TextView buydate_txt;
    }

    public ArrayList<AccountPlayAuthEntity.PlayAuth> getList() {
        return mList;
    }

    private int remaindDay(String exprieTime) {
        try {
            return Util.daysBetween(Util.getTime(), exprieTime) + 1;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
