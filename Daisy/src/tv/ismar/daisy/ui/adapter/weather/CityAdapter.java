package tv.ismar.daisy.ui.adapter.weather;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;
import tv.ismar.daisy.R;
import tv.ismar.daisy.data.table.location.CityTable;

import java.util.List;

/**
 * Created by huaijie on 7/13/15.
 */
public class CityAdapter extends BaseAdapter implements AdapterView.OnClickListener, View.OnFocusChangeListener {
    private List<CityTable> locationTableList;
    private Context context;

    private OnItemListener onItemListener;

    public CityAdapter(Context context, List<CityTable> locationTableList) {
        this.context = context;
        this.locationTableList = locationTableList;
    }

    public void setOnItemListener(OnItemListener itemListener) {
        this.onItemListener = itemListener;
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
        viewHolder.provinceTextView.setTag(position);
        viewHolder.provinceTextView.setOnFocusChangeListener(this);
        viewHolder.provinceTextView.setOnClickListener(this);
		viewHolder.provinceTextView
				.setOnHoverListener(new View.OnHoverListener() {

					@Override
					public boolean onHover(View v, MotionEvent event) {
						int what = event.getAction();
						switch (what) {
						case MotionEvent.ACTION_HOVER_MOVE:
						case MotionEvent.ACTION_HOVER_ENTER:
							v.requestFocus();
							break;
						}
						return false;
					}
				});
        return convertView;
    }

    @Override
    public void onClick(View v) {
        if (null != onItemListener) {
            onItemListener.onClick(v, (Integer) v.getTag());
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (null != onItemListener) {
            onItemListener.onFocusChange(v, hasFocus);
        }
    }

    private class ViewHolder {
        private TextView provinceTextView;
    }

    public List<CityTable> getList() {
        return locationTableList;
    }

    public interface OnItemListener {
        void onClick(View view, int position);

        void onFocusChange(View v, boolean hasFocus);
    }
}
