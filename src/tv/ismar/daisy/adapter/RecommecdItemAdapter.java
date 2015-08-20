package tv.ismar.daisy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import tv.ismar.daisy.R;
import tv.ismar.daisy.models.VideoEntity;
import tv.ismar.daisy.models.VideoEntity.Objects;
import tv.ismar.daisy.views.AsyncImageView;

import java.util.ArrayList;
import java.util.List;

public class RecommecdItemAdapter extends BaseAdapter {

	VideoEntity mTvHome;
	Context mContext;
	List<Objects> mList;
	public RecommecdItemAdapter(Context context,VideoEntity data){
		mContext = context;
		mTvHome = data;
		List<Objects> tmp = mTvHome.getObjects();
		mList = new ArrayList<VideoEntity.Objects>();
		if(tmp.size()>6){
			int count = 0;
			for(Objects obj: tmp){
				mList.add(obj);
				count++;
				if(count==6)
					break;
			}
		}
		else{
			mList = mTvHome.getObjects();
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
        holder.previewImage.setTag(position);
		holder.title.setText(mList.get(position).getTitle());
		holder.previewImage.setUrl(mList.get(position).getImage());
//        holder.previewImage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                int index = (Integer) view.getTag();
//                if(mList.get(index).isIs_complex()){
//                    DaisyUtils.gotoSpecialPage(view.getContext(), mList.get(index).getContent_model(), mList.get(index).getItem_url());
//                }
//                else{
//                    InitPlayerTool tool = new InitPlayerTool(view.getContext());
//                    tool.initClipInfo(mList.get(index).getItem_url(), InitPlayerTool.FLAG_URL);
//                }
//            }
//        });
		return convertView;
	}
	public static class ViewHolder {
		TextView title;
		AsyncImageView previewImage;
	}
}