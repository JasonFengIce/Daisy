package tv.ismar.daisy.ui.adapter.weather;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import tv.ismar.daisy.R;
import tv.ismar.daisy.data.table.weather.ProvinceTable;

import java.util.List;

/**
 * Created by huaijie on 7/13/15.
 */
public class ProvinceAdapter extends BaseAdapter {
    private List<ProvinceTable> provinceTableList;
    private Context context;

    public ProvinceAdapter(Context context, List<ProvinceTable> provinceTableList) {
        this.context = context;
        this.provinceTableList = provinceTableList;
    }

    @Override
    public int getCount() {
        return provinceTableList.size();
    }

    @Override
    public Object getItem(int position) {
        return provinceTableList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_province, null);
            viewHolder.provinceTextView = (TextView) convertView.findViewById(R.id.province_text);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.provinceTextView.setText(provinceTableList.get(position).province_name);
        return convertView;
    }

    private class ViewHolder {
        private TextView provinceTextView;
    }

    public List<ProvinceTable> getList() {
        return provinceTableList;
    }
}
