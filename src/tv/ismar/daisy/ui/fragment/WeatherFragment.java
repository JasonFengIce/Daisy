package tv.ismar.daisy.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.gson.Gson;
import tv.ismar.daisy.R;
import tv.ismar.daisy.core.client.IsmartvUrlClient;
import tv.ismar.daisy.data.weather.WeatherEntity;
import tv.ismar.daisy.ui.fragment.usercenter.LocationFragment;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by huaijie on 2015/7/14.
 */
public class WeatherFragment extends Fragment implements View.OnClickListener, View.OnFocusChangeListener {
    private static final String TAG = "WeatherFragment";

    private Context mContext;

    private static final int[] INDICATOR_RES_LIST = {
            R.string.guide_play_history,
            R.string.guide_my_favorite,
            R.string.guide_user_center,
            R.string.guide_search
    };


    private TextView titleTextView;
    private TextView subTitleTextView;
    private TextView weatherInfoTextView;

    private ImageView dividerImage;

    private LinearLayout guideLayout;

    private SharedPreferences locationSharedPreferences;

    private SharedPreferences.OnSharedPreferenceChangeListener changeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Log.d(TAG, "onSharedPreferenceChanged");
            String geoId = locationSharedPreferences.getString(LocationFragment.LOCATION_PREFERENCE_GEOID, "101020100");
            fetchWeatherInfo(geoId);
        }
    };


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weather, null);
        titleTextView = (TextView) view.findViewById(R.id.title);
        subTitleTextView = (TextView) view.findViewById(R.id.sub_title);
        weatherInfoTextView = (TextView) view.findViewById(R.id.weather_info);
        guideLayout = (LinearLayout) view.findViewById(R.id.indicator_layout);
        dividerImage = (ImageView) view.findViewById(R.id.divider);

        titleTextView.setText(R.string.app_name);
        subTitleTextView.setText(R.string.front_page);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        locationSharedPreferences = mContext.getSharedPreferences(LocationFragment.LOCATION_PREFERENCE_NAME, Context.MODE_PRIVATE);
        locationSharedPreferences.registerOnSharedPreferenceChangeListener(changeListener);
        createGuideIndicator();

        String geoId = locationSharedPreferences.getString(LocationFragment.LOCATION_PREFERENCE_GEOID, "101020100");
        fetchWeatherInfo(geoId);
    }


    private void createGuideIndicator() {
        for (int res : INDICATOR_RES_LIST) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_weather_indicator, null);
            TextView textView = (TextView) view.findViewById(R.id.weather_indicator);
            textView.setOnClickListener(this);
            textView.setOnFocusChangeListener(this);
            textView.setText(res);
            textView.setId(res);
            guideLayout.addView(view);
        }
    }

    public void setTitle(String title) {
        titleTextView.setText(title);
    }

    public void setSubTitle(String subTitle) {
        subTitleTextView.setText(subTitle);
    }

    public void hideSubTitle() {
        dividerImage.setVisibility(View.GONE);
        subTitleTextView.setVisibility(View.GONE);
    }

    private void fetchWeatherInfo(String geoId) {
        String api = "http://media.lily.tvxio.com/" + geoId + ".json";
        new IsmartvUrlClient().doRequest(api, new IsmartvUrlClient.CallBack() {
            @Override
            public void onSuccess(String result) {
                WeatherEntity weatherEntity = new Gson().fromJson(result, WeatherEntity.class);
                WeatherEntity.Detail todayDetail = weatherEntity.getToday();

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                ParsePosition pos = new ParsePosition(0);
                Date date = formatter.parse(todayDetail.getDate(), pos);

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);

                if (isAdded()) {
                    weatherInfoTextView.setText("");
                    weatherInfoTextView.append("   " + calendar.get(Calendar.YEAR) + getText(R.string.year).toString() +
                            calendar.get(Calendar.MONTH) + getText(R.string.month).toString() +
                            calendar.get(Calendar.DATE) + getText(R.string.day).toString() + "   ");

                    weatherInfoTextView.append(todayDetail.getPhenomenon() + "   ");
                    weatherInfoTextView.append(todayDetail.getTemperature() + getText(R.string.degree) + "   ");
                    weatherInfoTextView.append(todayDetail.getWind_direction());
                }
            }

            @Override
            public void onFailed(Exception exception) {
                Log.e(TAG, exception.getMessage());
            }
        });
    }


    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        switch (v.getId()) {
            case R.string.guide_play_history:
                intent.setClassName("tv.ismar.daisy",
                        "tv.ismar.daisy.ChannelListActivity");
                intent.putExtra("channel", "$histories");
                break;
            case R.string.guide_my_favorite:
                intent.setClassName("tv.ismar.daisy",
                        "tv.ismar.daisy.ChannelListActivity");
                intent.putExtra("channel", "$bookmarks");
                break;
            case R.string.guide_user_center:
                intent.setClassName("tv.ismar.daisy",
                        "tv.ismar.daisy.ui.activity.UserCenterActivity");
                break;
            case R.string.guide_search:
                intent.setClassName("tv.ismar.daisy",
                        "tv.ismar.daisy.SearchActivity");
                break;
        }
        mContext.startActivity(intent);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            ((TextView) v).setTextColor(getResources().getColor(R.color.association_focus));
        } else {
            ((TextView) v).setTextColor(getResources().getColor(R.color.association_normal));
        }
    }
}
