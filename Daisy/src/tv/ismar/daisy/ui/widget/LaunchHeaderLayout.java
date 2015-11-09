package tv.ismar.daisy.ui.widget;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import tv.ismar.daisy.R;
import tv.ismar.daisy.core.client.IsmartvUrlClient;
import tv.ismar.daisy.core.preferences.AccountSharedPrefs;
import tv.ismar.daisy.core.weather.WeatherInfoHandler;
import tv.ismar.daisy.data.table.location.CityTable;
import tv.ismar.daisy.data.weather.WeatherEntity;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by huaijie on 2015/7/21.
 */
public class LaunchHeaderLayout extends FrameLayout implements View.OnClickListener, View.OnFocusChangeListener {
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
        subTitleTextView.setText(subTitle);
        if (StringUtils.isEmpty(subTitle))
            hideSubTiltle();
    }

    public void hideSubTiltle() {
        subTitleTextView.setVisibility(View.GONE);
        dividerImage.setVisibility(View.GONE);

    }

    private void fetchWeatherInfo(String geoId) {
        String api = "http://media.lily.tvxio.com/" + geoId + ".xml";
        new IsmartvUrlClient().doRequest(api, new IsmartvUrlClient.CallBack() {
            @Override
            public void onSuccess(String result) {
//                WeatherEntity weatherEntity = new Gson().fromJson(result, WeatherEntity.class);
//                WeatherEntity.Detail todayDetail = weatherEntity.getToday();
//
//                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
//                ParsePosition pos = new ParsePosition(0);
//                Date date = formatter.parse(todayDetail.getDate(), pos);
//
//                Calendar calendar = Calendar.getInstance();
//                calendar.setTime(date);
//
//                weatherInfoTextView.setText("");
//                weatherInfoTextView.append("   " + calendar.get(Calendar.YEAR) + context.getText(R.string.year).toString() +
//                        calendar.get(Calendar.MONTH) + context.getText(R.string.month).toString() +
//                        calendar.get(Calendar.DATE) + context.getText(R.string.day).toString() + "   ");
//
//                weatherInfoTextView.append(todayDetail.getPhenomenon() + "   ");
//                weatherInfoTextView.append(todayDetail.getTemperature() + context.getText(R.string.degree) + "   ");
//                weatherInfoTextView.append(todayDetail.getWind_direction());

                SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
                try {
                    SAXParser saxParser = saxParserFactory.newSAXParser();
                    XMLReader xmlReader = saxParser.getXMLReader();
                    WeatherInfoHandler weatherInfoHandler = new WeatherInfoHandler();
                    xmlReader.setContentHandler(weatherInfoHandler);
                    InputSource inputSource = new InputSource(new StringReader(result));
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


//                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
//                ParsePosition pos = new ParsePosition(0);
//                Date date = formatter.parse(todayDetail.getDate(), pos);


            }

            @Override
            public void onFailed(Exception exception) {
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

    public void hideIndicatorTable() {
        for (View textView : indicatorTableList) {
            textView.setVisibility(View.GONE);
        }
    }

    public void hideWeather() {
        weatherInfoTextView.setVisibility(View.INVISIBLE);
    }

}
