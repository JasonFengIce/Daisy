package tv.ismar.daisy.adapter;

import java.util.ArrayList;
import tv.ismar.daisy.R;
import tv.ismar.daisy.data.HomePagerEntity;
import tv.ismar.daisy.views.AsyncImageView;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class RecommecdItemAdapter extends BaseAdapter {

	Context mContext;
    ArrayList<HomePagerEntity.Poster> mList;
	public RecommecdItemAdapter(Context context, ArrayList<HomePagerEntity.Poster> data){
		mContext = context;
        ArrayList<HomePagerEntity.Poster> tmp = data;
		mList = new ArrayList<HomePagerEntity.Poster>();
		if(tmp.size()>6){
			int count = 0;
			for(HomePagerEntity.Poster obj: tmp){
				mList.add(obj);
				count++;
				if(count==6)
					break;
			}
		}
		else{
			mList = data;
		}
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mList.get(position);
	}

	@Override
	public long getItemId(int id) {
		// TODO Auto-generated method stub
		return id;
	}
	ViewHolder holder;

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if(convertView==null){
			convertView = LayoutInflater.from(mContext).inflate(R.layout.list_recommend_item,null);				
			holder = new ViewHolder();
			holder.previewImage = (AsyncImageView)convertView.findViewById(R.id.ItemImage);
			holder.title = (TextView)convertView.findViewById(R.id.ItemText);
			convertView.setTag(holder);
		}
		else
			holder = (ViewHolder) convertView.getTag();
		
		holder.title.setText(mList.get(position).getIntroduction());
		holder.previewImage.setUrl(mList.get(position).getPoster_url());
		return convertView;
	}
	public static class ViewHolder {
		TextView title;
		AsyncImageView previewImage;
	}
}
