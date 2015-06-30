package tv.ismar.daisy.ui.fragment;

import java.util.ArrayList;

import android.util.Log;
import android.widget.*;
import tv.ismar.daisy.R;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.data.HomePagerEntity;
import tv.ismar.daisy.data.HomePagerEntity.Carousel;
import tv.ismar.daisy.data.HomePagerEntity.Poster;
import tv.ismar.daisy.exception.NetworkException;
import tv.ismar.daisy.models.SportsGame;
import tv.ismar.daisy.models.SportsGameList;
import tv.ismar.daisy.views.LabelImageView;
import tv.ismar.daisy.views.LoadingDialog;
import android.R.integer;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by huaijie on 5/18/15.
 */
public class SportFragment extends ChannelBaseFragment implements ListView.OnScrollListener {
    private static final String TAG = "SportFragment";

    private final int IMAGE_SWITCH_KEY = 0X11;
    private SimpleRestClient mRestClient = new SimpleRestClient();
    private LoadingDialog mLoadingDialog;
    private HomePagerEntity entity;
    private SportsGameList games;
    private ListView sec_one_list;
    private ScheduleAdapter adapter;
    private LabelImageView sport_card1;
    private LabelImageView sport_card2;
    private LabelImageView sport_card3;
    private LabelImageView sportspost;
    private LabelImageView sport_channel1_image;
    private TextView sport_channel1_subtitle;
    private LabelImageView sport_channel2_image;
    private TextView sport_channel2_subtitle;
    private LabelImageView sport_channel3_image;
    private TextView sport_channel3_subtitle;
    private LabelImageView sport_channel4_image;
    private TextView sport_channel4_subtitle;
    private TextView sport_channel5;

    private ImageView arrowUp;
    private ImageView arrowDown;

    private ArrayList<String> looppost = new ArrayList<String>();
    private int loopindex = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sport, null);
        sec_one_list = (ListView) view.findViewById(R.id.sec_one_list_2);
        mLoadingDialog = new LoadingDialog(getActivity(), getResources()
                .getString(R.string.loading));
        sport_card1 = (LabelImageView) view.findViewById(R.id.sport_card1);
        sport_card2 = (LabelImageView) view.findViewById(R.id.sport_card2);
        sport_card3 = (LabelImageView) view.findViewById(R.id.sport_card3);
        sportspost = (LabelImageView) view.findViewById(R.id.sportspost);
        sport_channel1_image = (LabelImageView) view
                .findViewById(R.id.sport_channel1_image);
        sport_channel1_subtitle = (TextView) view
                .findViewById(R.id.sport_channel1_subtitle);
        sport_channel2_image = (LabelImageView) view
                .findViewById(R.id.sport_channel2_image);
        sport_channel2_subtitle = (TextView) view
                .findViewById(R.id.sport_channel2_subtitle);
        sport_channel3_image = (LabelImageView) view
                .findViewById(R.id.sport_channel3_image);
        sport_channel3_subtitle = (TextView) view
                .findViewById(R.id.sport_channel3_subtitle);
        sport_channel4_image = (LabelImageView) view
                .findViewById(R.id.sport_channel4_image);
        sport_channel4_subtitle = (TextView) view
                .findViewById(R.id.sport_channel4_subtitle);
        sport_channel5 = (TextView) view
                .findViewById(R.id.listmore);

        arrowUp = (ImageView) view.findViewById(R.id.sec_one_list_1_arrowup);
        arrowDown = (ImageView) view.findViewById(R.id.sec_one_list_1_arrowdown);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        new FetchDataTask().execute();
        games = new SportsGameList();
        adapter = new ScheduleAdapter();
        adapter.setmData(games);
        sec_one_list.setAdapter(adapter);
        sec_one_list.setOnScrollListener(this);
        mLoadingDialog.setOnCancelListener(mLoadingCancelListener);
    }

    private OnCancelListener mLoadingCancelListener = new OnCancelListener() {
        @Override
        public void onCancel(DialogInterface dialog) {
            getActivity().finish();
            dialog.dismiss();
        }
    };

    private void fillData(SportsGameList carousellist,
                          ArrayList<Carousel> carousels, ArrayList<Poster> postlist) {
        adapter.setmData(carousellist);
        adapter.notifyDataSetChanged();
        sport_card1.setUrl(carousels.get(0).getThumb_image());
        sport_card1.setFocustitle(carousels.get(0).getIntroduction());
        sport_card1.setTag(carousels.get(0).getVideo_image());
        sport_card1.setOnFocusChangeListener(ItemOnFocusListener);
        sport_card2.setUrl(carousels.get(1).getThumb_image());
        sport_card2.setFocustitle(carousels.get(1).getIntroduction());
        sport_card2.setTag(carousels.get(1).getVideo_image());
        sport_card2.setOnFocusChangeListener(ItemOnFocusListener);
        sport_card3.setUrl(carousels.get(2).getThumb_image());
        sport_card3.setFocustitle(carousels.get(2).getIntroduction());
        sport_card3.setTag(carousels.get(2).getVideo_image());
        sport_card3.setOnFocusChangeListener(ItemOnFocusListener);
        looppost.add(carousels.get(0).getVideo_image());
        looppost.add(carousels.get(1).getVideo_image());
        looppost.add(carousels.get(2).getVideo_image());
        imageswitch.sendEmptyMessage(IMAGE_SWITCH_KEY);
        sport_channel1_image.setUrl(postlist.get(0).getCustom_image());
        sport_channel1_image.setFocustitle(postlist.get(0).getIntroduction());
        sport_channel1_subtitle.setText(postlist.get(0).getTitle());
        sport_channel2_image.setUrl(postlist.get(1).getCustom_image());
        sport_channel2_image.setFocustitle(postlist.get(1).getIntroduction());
        sport_channel2_subtitle.setText(postlist.get(1).getTitle());
        sport_channel3_image.setUrl(postlist.get(2).getCustom_image());
        sport_channel3_image.setFocustitle(postlist.get(2).getIntroduction());
        sport_channel3_subtitle.setText(postlist.get(2).getTitle());
        sport_channel4_image.setUrl(postlist.get(3).getCustom_image());
        sport_channel4_image.setFocustitle(postlist.get(3).getIntroduction());
        sport_channel4_subtitle.setText(postlist.get(3).getTitle());
        sport_channel1_image.setOnClickListener(ItemClickListener);
        sport_channel1_image.setTag(postlist.get(0));
        sport_channel2_image.setOnClickListener(ItemClickListener);
        sport_channel2_image.setTag(postlist.get(1));
        sport_channel3_image.setOnClickListener(ItemClickListener);
        sport_channel3_image.setTag(postlist.get(2));
        sport_channel4_image.setOnClickListener(ItemClickListener);
        sport_channel4_image.setTag(postlist.get(3));
        sport_channel5.setOnClickListener(ItemClickListener);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        int lastVisibleItem = firstVisibleItem + visibleItemCount - 1;
        Log.d(TAG, "firstVisibleItem: " + firstVisibleItem);
        if (firstVisibleItem == 0) {
            arrowUp.setVisibility(View.GONE);
        } else {
            if (arrowUp.getVisibility() != View.VISIBLE) {
                arrowUp.setVisibility(View.VISIBLE);
            }
        }

        if (lastVisibleItem == (totalItemCount - 1)) {
            arrowDown.setVisibility(View.GONE);
        } else {
            if (arrowDown.getVisibility() != View.VISIBLE) {
                arrowDown.setVisibility(View.VISIBLE);
            }
        }

    }

    private class ScheduleAdapter extends BaseAdapter {
        private SportsGameList mData;
        private LayoutInflater mInflater;

        public ScheduleAdapter() {
            mInflater = (LayoutInflater) getActivity().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public SportsGame getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = mInflater.inflate(
                        R.layout.fragment_sport_listitem, null);
                holder = new ViewHolder();
                holder.view = (LabelImageView) convertView
                        .findViewById(R.id.sportschedule);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.view.setUrl(mData.get(position).poster_url);
            holder.view.setFocustitle((mData.get(position).name));
            holder.view.setModetype(5);
            return convertView;
        }

        public SportsGameList getmData() {
            return mData;
        }

        public void setmData(SportsGameList mData) {
            this.mData = mData;
        }

    }

    public static class ViewHolder {
        public LabelImageView view;
    }

    class FetchDataTask extends AsyncTask<String, Void, Integer> {
        private final static int RESUTL_CANCELED = -2;
        private final static int RESULT_SUCCESS = 0;

        @Override
        protected void onPreExecute() {
            if (mLoadingDialog != null && !mLoadingDialog.isShowing()) {
                mLoadingDialog.show();
            }
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(String... params) {
            try {
                entity = mRestClient.getSportHome(channelEntity.getHomepage_url());
                games = mRestClient.getSportGames();
            } catch (NetworkException e) {
                e.printStackTrace();
            }
            if (isCancelled()) {
                return RESUTL_CANCELED;
            } else {
                return RESULT_SUCCESS;
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
                mLoadingDialog.dismiss();
            }
            if (result != RESULT_SUCCESS) {
                return;
            } else {
                ArrayList<Poster> postlist = entity.getPosters();
                ArrayList<Carousel> carousels = entity.getCarousels();
                fillData(games, carousels, postlist);
            }
        }
    }

    private View.OnClickListener ItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Poster poster = (Poster) view.getTag();
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (poster == null) {
                intent.putExtra("title", channelEntity.getName());
                intent.putExtra("url", channelEntity.getUrl());
                intent.putExtra("channel", channelEntity.getChannel());
                intent.setClassName("tv.ismar.daisy",
                        "tv.ismar.daisy.ChannelListActivity");
                getActivity().startActivity(intent);
            } else {
                if ("item".equals(poster.getModel_name())) {
                    intent.setClassName("tv.ismar.daisy",
                            "tv.ismar.daisy.ItemDetailActivity");
                    intent.putExtra("url", poster.getUrl());
                    getActivity().startActivity(intent);
                } else if ("topic".equals(poster.getModel_name())) {

                } else if ("section".equals(poster.getModel_name())) {

                } else if ("package".equals(poster.getModel_name())) {

                }
            }
        }
    };

    private View.OnFocusChangeListener ItemOnFocusListener = new View.OnFocusChangeListener() {

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                String url = v.getTag().toString();
                sportspost.setUrl(url);
                imageswitch.removeMessages(IMAGE_SWITCH_KEY);
            } else {
                imageswitch.sendEmptyMessageDelayed(IMAGE_SWITCH_KEY, 6000);
            }
        }
    };

    private Handler imageswitch = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            sportspost.setUrl(looppost.get(loopindex++));
            if (loopindex >= 2)
                loopindex = 0;
            imageswitch.sendEmptyMessageDelayed(IMAGE_SWITCH_KEY, 6000);
            // pendingView.requestFocus();
        }
    };
}
