package tv.ismar.sakura.ui.adapter;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import tv.ismar.daisy.R;
import tv.ismar.sakura.data.http.ChatMsgEntity;

import java.util.List;


/**
 * Created by huaijie on 14-10-29.
 */
public class FeedbackListAdapter extends BaseAdapter {
    private Context context;
    private List<ChatMsgEntity.Data> list;


    public FeedbackListAdapter(Context context, List<ChatMsgEntity.Data> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int id) {
        return id;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.sakura_item_feedback_list, parent, false);
            holder = new ViewHolder();
            holder.feedbackTime = (TextView) view.findViewById(R.id.feedback_time);
            holder.feedbackCustomer = (TextView) view.findViewById(R.id.feedback_customer);
            holder.feedbackIsmartv = (TextView) view.findViewById(R.id.feedback_ismartv);
            view.setTag(holder);
        }

        holder.feedbackTime.setText(list.get(position).getSubmit_time());
        holder.feedbackCustomer.setText(Build.MODEL + " : " + list.get(position).getCommont());
        holder.feedbackIsmartv.setText(context.getText(R.string.ismartv) + list.get(position).getReply());
        return view;
    }


    static class ViewHolder {
        TextView feedbackTime;

        TextView feedbackCustomer;

        TextView feedbackIsmartv;

    }
}
