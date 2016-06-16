package tv.ismar.daisy.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import tv.ismar.daisy.R;
import tv.ismar.daisy.data.http.ItemEntity;


/**
 * Created by huibin on 6/13/16.
 */
public class EpisodeFragment extends Fragment {

    private RecyclerView mEpisodeLayout;
    private ItemEntity.SubItem[] mSubItems;
    private EpisodeListAdapter mEpisodeListAdapter;
    private Type mType;

    private int mCurrentPosition;

    private OnItemSelectedListener mOnItemSelectedListener;

    public void setOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener) {
        mOnItemSelectedListener = onItemSelectedListener;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_episode, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mEpisodeLayout = (RecyclerView) view.findViewById(R.id.episode_list);

        mEpisodeListAdapter = new EpisodeListAdapter();
        mEpisodeLayout.setAdapter(mEpisodeListAdapter);

    }

    public void setData(ItemEntity itemEntity, Type type) {
        mSubItems = itemEntity.getSubitems();
        mType = type;
        switch (type) {
            case Entertainment:
                mEpisodeLayout.setLayoutManager(new GridLayoutManager(getContext(), 1, GridLayoutManager.VERTICAL, false));
                break;
            case Teleplay:
                mEpisodeLayout.setLayoutManager(new GridLayoutManager(getContext(), 4, GridLayoutManager.VERTICAL, false));
                break;
        }

        mEpisodeListAdapter.notifyDataSetChanged();
    }

    public void setCurrentItem(int position){
        mCurrentPosition = position;
        mEpisodeListAdapter.notifyDataSetChanged();
    }


    class EpisodeListAdapter extends RecyclerView.Adapter<EpisodeListAdapter.EpisodeListHolder> implements View.OnClickListener {
        @Override
        public EpisodeListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = null;
            switch (mType){
                case Teleplay:
                    itemView = LayoutInflater.from(getContext()).inflate(R.layout.item_episode, parent, false);
                    break;
                case Entertainment:
                    itemView = LayoutInflater.from(getContext()).inflate(R.layout.item_episode_entertainment, parent, false);
                    break;
            }

            itemView.setOnClickListener(this);
            EpisodeListHolder holder = new EpisodeListHolder(itemView);
            return holder;
        }

        @Override
        public void onBindViewHolder(EpisodeListHolder holder, int position) {
            if (mSubItems != null && mSubItems.length != 0) {
                holder.itemView.setTag(mSubItems[position]);
                switch (mType){
                    case Teleplay:
                        holder.title.setText( String.valueOf(position + 1));
                        if (position == mCurrentPosition){
                            holder.title.setSelected(true);
                        }else {
                            holder.title.setSelected(false);
                        }
                        break;
                    case Entertainment:
                        holder.title.setText(mSubItems[position].getTitle());
                        if (position == mCurrentPosition){
                            holder.title.setTextColor(getResources().getColor(R.color._5994ee));
                        }else {
                            holder.title.setTextColor(getResources().getColor(R.color.white));
                        }
                        break;
                }
            }
        }

        @Override
        public int getItemCount() {
            if (mSubItems != null) {
                return mSubItems.length;
            }
            return 0;
        }

        @Override
        public void onClick(View v) {
            ItemEntity.SubItem subItem = (ItemEntity.SubItem) v.getTag();
            if (mOnItemSelectedListener!= null){
                mOnItemSelectedListener.onEpisodeItemSelected(subItem);
            }
        }

        class EpisodeListHolder extends RecyclerView.ViewHolder {
            TextView title;

            View itemView;

            public EpisodeListHolder(View view) {
                super(view);
                title = (TextView) view.findViewById(R.id.title);
                itemView = view;
            }
        }
    }

    public enum Type {
        Entertainment,
        Teleplay
    }

    public   interface OnItemSelectedListener{
        void onEpisodeItemSelected(ItemEntity.SubItem subItem);
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }
}
