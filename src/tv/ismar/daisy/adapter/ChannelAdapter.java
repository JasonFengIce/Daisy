package tv.ismar.daisy.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import org.sakuratya.horizontal.adapter.HGridAdapter;
import tv.ismar.daisy.R;
import tv.ismar.daisy.core.ImageLabelUtils;
import tv.ismar.daisy.data.ChannelEntity;
import tv.ismar.daisy.models.MovieBean;
import tv.ismar.daisy.ui.ItemViewFocusChangeListener;
import tv.ismar.daisy.views.AsyncImageView;
import tv.ismar.daisy.views.AsyncImageView.OnImageViewLoadListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class ChannelAdapter extends HGridAdapter<ChannelEntity>  {

	private Context mContext;
	// 搜索集合
	private List<ChannelEntity> movieList;
	// layout ID
	private int sourceid;
	private LayoutInflater mLayoutInflater;
	// 背景图ID
	private final int backgroudID = R.drawable.list_item_preview_bg;
	private final int backType = R.drawable.iv_type_comic;
    private HashMap<String, TextView> channelHashMap;
	// private HashMap<Integer, HashMap<String, Object>> cacheMap = new HashMap<Integer, HashMap<String, Object>>();
	public ChannelAdapter(Context context, int sourceid) {
		this.mContext = context;
		this.sourceid = sourceid;
		this.mLayoutInflater = LayoutInflater.from(context);
	}

	public ChannelAdapter(Context context, List<ChannelEntity> movieList, int sourceid) {
		this.mContext = context;
		this.movieList = movieList;
		this.sourceid = sourceid;
		this.mLayoutInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return movieList.size();
	}

	@Override
	public ChannelEntity getItem(int position) {
		return movieList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
    private View.OnClickListener listener;
    public void setOnClickListener(View.OnClickListener l){
        listener = l;
    }
    public void setMap(HashMap<String, TextView> map){
      channelHashMap = map;
    }
	private ChannelEntity movieBean;
	ViewHolder holder;

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// startTime = System.nanoTime();
		movieBean = (ChannelEntity) getItem(position);
		if (null == convertView) {
			holder = new ViewHolder();
			convertView = mLayoutInflater.inflate(sourceid, null);
            holder.channelBtn = (TextView)convertView.findViewById(R.id.channel_item);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
        convertView.setBackgroundResource(R.drawable.channel_item_normal);
        channelHashMap.put(movieBean.getChannel(), holder.channelBtn);
        holder.channelBtn.setText(movieBean.getName());
        holder.channelBtn.setTag(position);
      //  convertView.setOnFocusChangeListener(new ItemViewFocusChangeListener());
//        holder.channelBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if(listener!=null)
//                    listener.onClick(view);
//            }
//        });
		return convertView;
	}


	public static class ViewHolder {
		public TextView channelBtn;

	}

	@Override
	public int getSectionIndex(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getSectionCount(int sectionIndex) {
		// TODO Auto-generated method stub
		return movieList.size();
	}

	@Override
	public String getLabelText(int sectionIndex) {
		// TODO Auto-generated method stub
		return "";
	}
	@Override
	public boolean hasSection() {
		return false;
	}
}
