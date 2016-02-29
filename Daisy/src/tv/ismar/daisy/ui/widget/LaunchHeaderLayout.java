package tv.ismar.daisy.ui.widget;

import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang3.StringUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import retrofit.Retrofit;
import tv.ismar.daisy.R;
import tv.ismar.daisy.core.client.HttpAPI;
import tv.ismar.daisy.core.client.HttpManager;
import tv.ismar.daisy.core.preferences.AccountSharedPrefs;
import tv.ismar.daisy.core.weather.WeatherInfoHandler;
import tv.ismar.daisy.data.table.location.CityTable;
import tv.ismar.daisy.data.weather.WeatherEntity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnHoverListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.squareup.okhttp.ResponseBody;

/**
 * Created by huaijie on 2015/7/21.
 */
public class LaunchHeaderLayout extends FrameLayout implements View.OnClickListener, View.OnFocusChangeListener,OnHoverListener {
    private static final String TAG = "LaunchHeaderLayout";
    private Context context;


    private TextView titleTextView;
    private TextView subTitleTextView;
    private TextView weatherInfoTextView;

    private ImageView dividerImage;

    private LinearLayout guideLayout;

//    private SharedPreferences locationSharedPreferences;

    private List<View> indicatorTableList;

    public LaunchHeaderLayout(Context context) {
        super(context);
        this.context = context;
    }

    public LaunchHeaderLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        indicatorTableList = new ArrayList<View>();

        View view = LayoutInflater.from(context).inflate(R.layout.fragment_weather, null);


        titleTextView = (TextView) view.findViewById(R.id.title);
        subTitleTextView = (TextView) view.findViewById(R.id.sub_title);
        weatherInfoTextView = (TextView) view.findViewById(R.id.weather_info);
        guideLayout = (LinearLayout) view.findViewById(R.id.indicator_layout);
        dividerImage = (ImageView) view.findViewById(R.id.divider);

        titleTextView.setText(R.string.app_name);
        subTitleTextView.setText(R.string.front_page);

//        locationSharedPreferences = context.getSharedPreferences(LocationFragment.LOCATION_PREFERENCE_NAME, Context.MODE_PRIVATE);
        AccountSharedPrefs.getInstance().getSharedPreferences().registerOnSharedPreferenceChangeListener(changeListener);
        createGuideIndicator();
        String cityName = AccountSharedPrefs.getInstance().getSharedPrefs(AccountSharedPrefs.CITY);

//        String geoId = locationSharedPreferences.getString(LocationFragment.LOCATION_PREFERENCE_GEOID, "101020100");

        CityTable cityTable = new Select().from(CityTable.class).where(CityTable.CITY + " = ?", cityName).executeSingle();
        if (cityTable != null) {
            fetchWeatherInfo(String.valueOf(cityTable.geo_id));
        }
        addView(view);
    }


    private static final int[] INDICATOR_RES_LIST = {
            R.string.guide_play_history,
            R.string.guide_my_favorite,
            R.string.guide_user_center,
            R.string.guide_search
    };


    private SharedPreferences.OnSharedPreferenceChangeListener changeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//            String geoId = locationSharedPreferences.getString(LocationFragment.LOCATION_PREFERENCE_GEOID, "101020100");

            String cityName = AccountSharedPrefs.getInstance().getSharedPrefs(AccountSharedPrefs.CITY);
            CityTable cityTable = new Select().from(CityTable.class).where(CityTable.CITY + " = ?", cityName).executeSingle();

            if (cityTable != null) {
                fetchWeatherInfo(String.valueOf(cityTable.geo_id));
            }

        }
    };


    private void createGuideIndicator() {
        int i = 0;
        indicatorTableList.clear();
        for (int res : INDICATOR_RES_LIST) {

            View view = LayoutInflater.from(context).inflate(R.layout.item_weather_indicator, null);
            TextView textView = (TextView) view.findViewById(R.id.weather_indicator);
            view.setOnClickListener(this);
            view.setOnFocusChangeListener(this);
            view.setOnHoverListener(this);
            textView.setText(res);
            view.setId(res);
            if (i == 0) {
                view.setNextFocusLeftId(view.getId());
            }
            if (i == INDICATOR_RES_LIST.length - 1) {
                view.setRight(-20);
                view.setNextFocusRightId(view.getId());
            }
            guideLayout.addView(view);
            indicatorTableList.add(view);
            i++;
        }
    }

    public void setTitle(String title) {
        titleTextView.setText(title);
    }

	public void setSubTitle(String subTitle) {
		if (StringUtils.isEmpty(subTitle)) {
			hideSubTiltle();
		} else {
			subTitleTextView.setText(subTitle.replace(" ", ""));
		}
	}

    public void hideSubTiltle() {
        subTitleTextView.setVisibility(View.GONE);
        dividerImage.setVisibility(View.GONE);

    }

    private void fetchWeatherInfo(String geoId) {
		String weather = AccountSharedPrefs.getInstance()
				.getSharedPreferences()
				.getString(AccountSharedPrefs.WEATHER_INFO, null);
		if (StringUtils.isNotEmpty(weather)) {
			parseXml(weather);
		}
		Retrofit retrofit = HttpManager.getInstance().media_lily_Retrofit;
        retrofit.create(HttpAPI.WeatherAPI.class).doRequest(geoId).enqueue(new retrofit.Callback<ResponseBody>() {
            @Override
            public void onResponse(retrofit.Response<ResponseBody> response, Retrofit retrofit) {
                try {
                	if(response.body() != null){
                    String result = response.body().string();
                    AccountSharedPrefs.getInstance().getSharedPreferences().edit().putString(AccountSharedPrefs.WEATHER_INFO, result);
                    parseXml(result);
                	}
                } catch (IOException e) {
                    Log.e(TAG, "解析天气数据失败");
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.e(TAG, "获取天气失败");
            }
        });
    }


    private void parseXml(String xml) {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        try {
            SAXParser saxParser = saxParserFactory.newSAXParser();
            XMLReader xmlReader = saxParser.getXMLReader();
            WeatherInfoHandler weatherInfoHandler = new WeatherInfoHandler();
            xmlReader.setContentHandler(weatherInfoHandler);
            InputSource inputSource = new InputSource(new StringReader(xml));
            xmlReader.parse(inputSource);

            WeatherEntity weatherEntity = weatherInfoHandler.getWeatherEntity();

            Date now = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日");//可以方便地修改日期格式
            String todayTime = dateFormat.format(now);

            weatherInfoTextView.setText("");
//                    weatherInfoTextView.append("   " + calendar.get(Calendar.YEAR) + context.getText(R.string.year).toString() +
//                            calendar.get(Calendar.MONTH) + context.getText(R.string.month).toString() +
//                            calendar.get(Calendar.DATE) + context.getText(R.string.day).toString() + "   ");
            weatherInfoTextView.append("   " + todayTime + "   ");

            weatherInfoTextView.append(weatherEntity.getToday().getCondition() + "   ");
            if (weatherEntity.getToday().getTemplow().equals(weatherEntity.getToday().getTemphigh())) {
                weatherInfoTextView.append(weatherEntity.getToday().getTemplow() + context.getText(R.string.degree));
            } else {
                weatherInfoTextView.append(weatherEntity.getToday().getTemplow() + " ~ " + weatherEntity.getToday().getTemphigh() + context.getText(R.string.degree));
            }


        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        switch (v.getId()) {
            case R.string.guide_play_history:
                intent.setClassName("tv.ismar.daisy",
                        "tv.ismar.daisy.ChannelListActivity");
                intent.putExtra("channel", "histories");
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
        context.startActivity(intent);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        ImageView imageView = (ImageView) v.findViewById(R.id.indicator_image);
        TextView textView = (TextView) v.findViewById(R.id.weather_indicator);
        if (hasFocus) {
            textView.setTextColor(getResources().getColor(R.color._ff9c3c));

            imageView.setVisibility(View.VISIBLE);

        } else {
            textView.setTextColor(getResources().getColor(R.color.association_normal));
            imageView.setVisibility(View.INVISIBLE);
        }
    }

	@Override
	public boolean onHover(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		ImageView imageView = (ImageView) v.findViewById(R.id.indicator_image);
		TextView textView = (TextView) v.findViewById(R.id.weather_indicator);
		if (event.getAction() == MotionEvent.ACTION_HOVER_ENTER) {
			textView.setTextColor(getResources().getColor(R.color._ff9c3c));
			imageView.setVisibility(View.VISIBLE);
			v.requestFocus();
		} else if (event.getAction() == MotionEvent.ACTION_HOVER_MOVE) {
			textView.setTextColor(getResources().getColor(R.color._ff9c3c));
			imageView.setVisibility(View.VISIBLE);
		} else {
			textView.setTextColor(getResources().getColor(
					R.color.association_normal));
			imageView.setVisibility(View.INVISIBLE);
		}
		return false;
	}

    public void hideIndicatorTable() {
        for (View textView : indicatorTableList) {
            textView.setVisibility(View.GONE);
        }
    }

    public void hideWeather() {
        weatherInfoTextView.setVisibility(View.INVISIBLE);
    }

}
