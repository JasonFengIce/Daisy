package tv.ismar.daisy.ui.adapter.weather;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import tv.ismar.daisy.R;
import tv.ismar.daisy.data.table.location.ProvinceTable;

import java.util.List;

/**
 * Created by huaijie on 7/13/15.
 */
public class ProvinceAdapter extends BaseAdapter implements View.OnFocusChangeListener, View.OnClickListener {
    private List<ProvinceTable> provinceTableList;
    private Context context;

    private OnItemListener onItemListener;

    public ProvinceAdapter(Context context, List<ProvinceTable> provinceTableList) {
        this.context = context;
        this.provinceTableList = provinceTableList;
    }

    public void setOnItemListener(OnItemListener itemListener) {
        this.onItemListener = itemListener;
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
        viewHolder.provinceTextView.setTag(position);
        viewHolder.provinceTextView.setOnFocusChangeListener(this);
        viewHolder.provinceTextView.setOnClickListener(this);
        viewHolder.provinceTextView.setOnHoverListener(new View.OnHoverListener() {
			
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
    public void onFocusChange(View v, boolean hasFocus) {
        if (onItemListener != null)
            onItemListener.onFocusChange(v, hasFocus);
    }

    @Override
    public void onClick(View v) {
        if (onItemListener!= null)
            onItemListener.onClick(v, (Integer)v.getTag());

    }

    private class ViewHolder {
        private TextView provinceTextView;
    }

    public List<ProvinceTable> getList() {
        return provinceTableList;
    }


    public interface OnItemListener {
        void onClick(View view, int position);

        void onFocusChange(View v, boolean hasFocus);
    }
}
