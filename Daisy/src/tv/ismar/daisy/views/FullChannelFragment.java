package tv.ismar.daisy.views;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import tv.ismar.daisy.FullChannelActivity;
import tv.ismar.daisy.R;
import tv.ismar.daisy.ui.fragment.ChannelBaseFragment;

/**
 * Created by admin on 2016/6/6.
 */
public class FullChannelFragment extends ChannelBaseFragment implements View.OnClickListener {

    private int[] channels = new int[]{R.drawable.full_channel_top, R.drawable.full_channel_chinese, R.drawable.full_channel_overseas, R.drawable.full_channel_tv, R.drawable.full_channel_entertain, R.drawable.full_channel_sport, R.drawable.full_channel_life, R.drawable.full_channel_children, R.drawable.full_channel_music, R.drawable.full_channel_game};
    private GridView full_channel;
    private ImageView full_channel_arrow_back;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_full_channel, null);
        full_channel_arrow_back = (ImageView)view.findViewById(R.id.full_channel_arrow_back);
        full_channel_arrow_back.setOnClickListener(this);
        full_channel = (GridView)view.findViewById(R.id.full_channel);
        FullChannelAdapter adapter=new FullChannelAdapter();
        full_channel.setAdapter(adapter);
        full_channel.setOnItemClickListener(onItemClickListener);
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        mContext=(FullChannelActivity)activity;
        super.onAttach(activity);
    }

    @Override
    public void onClick(View v) {
        getActivity().finish();
    }

//    @Override
//    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        Intent intent=new Intent();
//        if(position==0){
//            intent.setAction("tv.ismar.daisy.tvguide");
//        }else{
//
//        }
//        switch (position){
//            case 0:
//
//                break;
//            case 1:
//
//                break;
//            case 2:
//                break;
//            case 3:
//                break;
//            case 4:
//                break;
//            case 5:
//                break;
//            case 6:
//                break;
//            case 7:
//                break;
//            case 8:
//                break;
//            case 9:
//                break;
//        }
//    }

    private class FullChannelAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return channels.length;
        }

        @Override
        public Object getItem(int position) {
            return channels[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView=new ImageView(getContext());
            convertView.setBackgroundResource(channels[position]);
            return convertView;
        }
    }
}
