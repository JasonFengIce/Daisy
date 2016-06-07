package tv.ismar.daisy.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import tv.ismar.daisy.R;
import tv.ismar.daisy.models.Item;

/**
 * Created by liucan on 2016/6/6.
 */
public class EntertainmentPopAdapter extends BaseAdapter {
    List<Item> list;
    Context mcontext;
    public EntertainmentPopAdapter(List<Item> lt,Context context){
        list=lt;
        mcontext=context;
    }
    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Viewholder viewholder = null;
        if (convertView == null) {
            convertView = View.inflate(mcontext,R.layout.enter_poplist_item, null);
            viewholder = new Viewholder();
            viewholder.textView= (TextView) convertView.findViewById(R.id.enter_text);
            viewholder.textView.setText(list.get(position).title);
            convertView.setTag(viewholder);
        } else {
            viewholder = (Viewholder) convertView.getTag();
        }
        return null;
    }
    class Viewholder {
        TextView textView;
    }
}
