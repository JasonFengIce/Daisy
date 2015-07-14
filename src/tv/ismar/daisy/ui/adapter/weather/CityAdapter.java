package tv.ismar.daisy.ui.adapter.weather;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import tv.ismar.daisy.R;
import tv.ismar.daisy.data.table.weather.LocationTable;

import java.util.List;

/**
 * Created by huaijie on 7/13/15.
 */
public class CityAdapter extends BaseAdapter {
    private List<LocationTable> locationTableList;
    private Context context;

    public CityAdapter(Context context, List<LocationTable> locationTableList) {
        this.context = context;
        this.locationTableList = locationTableList;
    }

    @Override
    public int getCount() {
        return locationTableList.size();
    }

    @Override
    public Object getItem(int position) {
        return locationTableList.get(position);
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

        viewHolder.provinceTextView.setText(locationTableList.get(position).city);
        return convertView;
    }

    private class ViewHolder {
        private TextView provinceTextView;
    }

    public List<LocationTable> getList() {
        return locationTableList;
    }
}
