package tv.ismar.daisy;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectViews;
import butterknife.OnClick;
import com.ismartv.launcher.core.http.HttpClient;
import com.ismartv.launcher.data.ChannelEntity;
import com.ismartv.launcher.data.FrontPageEntity;
import com.ismartv.launcher.data.VideoEntity;
import com.ismartv.launcher.data.WeatherEntity;
import com.ismartv.launcher.ui.widget.IsmartvVideoView;
import com.squareup.picasso.Picasso;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.http.GET;
import tv.ismar.daisy.R;

import java.util.HashMap;
import java.util.List;

/**
 * Created by <huaijiefeng@gmail.com> on 9/9/14.
 */
public class LauncherActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "LauncherActivity";

    private static final int NAME = 1;
    private static final int URL = 2;
    private static final int CHANNEL = 3;

    private IsmartvVideoView videoView;

    private ImageView[] homeImages;

    private ImageView[] channelImages;

    private TextView[] channelTexts;

    private RelativeLayout[] channelImtes;

    private ImageView[] recomends;


    private ImageView weatherIcon;
    private TextView weatherTmp;
    private TextView todayWeatherDetail;

    private TextView tomorrowDetail;

    @InjectViews({R.id.home_title_1, R.id.home_title_2, R.id.home_title_3,
            R.id.home_title_4, R.id.home_title_5, R.id.home_title_6})
    List<TextView> homeTitles;

    @InjectViews({R.id.home_item_1,R.id.home_item_2,R.id.home_item_3,
            R.id.home_item_4,R.id.home_item_5,R.id.home_item_6})
    List<RelativeLayout> homeItems;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        ButterKnife.inject(this);
        initViews();
        getFrontPage();
        getTvHome();
        getChannels();
        getLatest();
        fetchWeather();

    }

    private void initViews() {
        weatherIcon = (ImageView) findViewById(R.id.weather_icon);
        weatherTmp = (TextView) findViewById(R.id.weather_tmp);
        todayWeatherDetail = (TextView) findViewById(R.id.today_weather_detail);
        tomorrowDetail = (TextView) findViewById(R.id.tomorrow_detail);


        videoView = (IsmartvVideoView) findViewById(R.id.video_view);
        homeImages = new ImageView[]{
                (ImageView) findViewById(R.id.home_1),
                (ImageView) findViewById(R.id.home_2),
                (ImageView) findViewById(R.id.home_3),
                (ImageView) findViewById(R.id.home_4),
                (ImageView) findViewById(R.id.home_5),
                (ImageView) findViewById(R.id.home_6)
        };

        channelImages = new ImageView[]
                {
                        (ImageView) findViewById(R.id.channel_iv_1),
                        (ImageView) findViewById(R.id.channel_iv_2),
                        (ImageView) findViewById(R.id.channel_iv_3),
                        (ImageView) findViewById(R.id.channel_iv_4),
                        (ImageView) findViewById(R.id.channel_iv_5),
                        (ImageView) findViewById(R.id.channel_iv_6),
                        (ImageView) findViewById(R.id.channel_iv_7),
                        (ImageView) findViewById(R.id.channel_iv_8),
                        (ImageView) findViewById(R.id.channel_iv_9),
                        (ImageView) findViewById(R.id.channel_iv_10),
                        (ImageView) findViewById(R.id.channel_iv_11),
                        (ImageView) findViewById(R.id.channel_iv_12)
                };
        channelTexts = new TextView[]{
                (TextView) findViewById(R.id.channel_tv_1),
                (TextView) findViewById(R.id.channel_tv_2),
                (TextView) findViewById(R.id.channel_tv_3),
                (TextView) findViewById(R.id.channel_tv_4),
                (TextView) findViewById(R.id.channel_tv_5),
                (TextView) findViewById(R.id.channel_tv_6),
                (TextView) findViewById(R.id.channel_tv_7),
                (TextView) findViewById(R.id.channel_tv_8),
                (TextView) findViewById(R.id.channel_tv_9),
                (TextView) findViewById(R.id.channel_tv_10),
                (TextView) findViewById(R.id.channel_tv_11),
                (TextView) findViewById(R.id.channel_tv_12)
        };

        channelImtes = new RelativeLayout[]{
                (RelativeLayout) findViewById(R.id.channel_item_1),
                (RelativeLayout) findViewById(R.id.channel_item_2),
                (RelativeLayout) findViewById(R.id.channel_item_3),
                (RelativeLayout) findViewById(R.id.channel_item_4),
                (RelativeLayout) findViewById(R.id.channel_item_5),
                (RelativeLayout) findViewById(R.id.channel_item_6),
                (RelativeLayout) findViewById(R.id.channel_item_7),
                (RelativeLayout) findViewById(R.id.channel_item_8),
                (RelativeLayout) findViewById(R.id.channel_item_9),
                (RelativeLayout) findViewById(R.id.channel_item_10),
                (RelativeLayout) findViewById(R.id.channel_item_11),
                (RelativeLayout) findViewById(R.id.channel_item_12),
        };
        recomends = new ImageView[]
                {
                        (ImageView) findViewById(R.id.image_1),
                        (ImageView) findViewById(R.id.image_2),
                        (ImageView) findViewById(R.id.image_3)
                };

    }


    @Override
    public void onClick(View view) {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        HashMap<Intent, String> hashMap = (HashMap<Intent, String>) view.getTag();
        intent.putExtra("title", hashMap.get(NAME));
        intent.putExtra("url", hashMap.get(URL));
        intent.putExtra("channel", hashMap.get(CHANNEL));
        intent.setClassName("tv.ismar.daisy", "tv.ismar.daisy.ChannelListActivity");
        this.startActivity(intent);
    }

    @OnClick({R.id.home_item_1,R.id.home_item_2,R.id.home_item_3,
            R.id.home_item_4,R.id.home_item_5,R.id.home_item_6})
    public void pickHomeItem(RelativeLayout view)
    {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        String values[] = view.getTag().toString().split(",");
        if(values[1].equals("item"))
        	intent.setClassName("tv.ismar.daisy", "tv.ismar.daisy.ItemDetailActivity");
        else
        	intent.setClassName("tv.ismar.daisy", "tv.ismar.daisy.PlayerActivity");
        
        intent.putExtra("url", values[0]);
        startActivity(intent);
    }

    public void getFrontPage() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint(HttpClient.HOST)
                .build();
        Client client = restAdapter.create(Client.class);
        client.excute(new Callback<FrontPageEntity>() {
                          @Override
                          public void success(FrontPageEntity frontPageBean, Response response) {
                              Log.d(TAG, "getFrontPage --> success");
                              final Uri uri = Uri.parse(frontPageBean.getVideos().get(0).getVideo_url());
                              videoView.setVideoURI(uri);
                              videoView.start();
                              videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                                  @Override
                                  public void onPrepared(MediaPlayer mp) {
                                      mp.start();
                                      mp.setLooping(true);

                                  }
                              });

                              videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                                  @Override
                                  public void onCompletion(MediaPlayer mp) {
                                      videoView.setVideoURI(uri);
                                      videoView.start();

                                  }
                              });
                          }

                          @Override
                          public void failure(RetrofitError retrofitError) {
                              Log.e(TAG, "getFrontPage --> failure " + retrofitError.getMessage());
                          }
                      }

        );

    }

    public void getTvHome() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint(HttpClient.HOST)
                .build();
        TvHome client = restAdapter.create(TvHome.class);
        client.excute(new Callback<VideoEntity>() {
            @Override
            public void success(VideoEntity tvHome, Response response) {
                Log.d(TAG, "getTvHome --> success");
                for (int i = 0; i < 6; i++) {
                    Log.d(TAG, "getTvHome --> " + tvHome.getObjects().get(i).getImage());
                    homeTitles.get(i).setText(tvHome.getObjects().get(i).getTitle());

                    Picasso.with(LauncherActivity.this)
                            .load(tvHome.getObjects().get(i).getImage())
                            .placeholder(R.drawable.preview)
                            .error(R.drawable.preview)
                            .into(homeImages[i]);
                   boolean is_complex = tvHome.getObjects().get(i).isIs_complex();
                   if(is_complex)
                     homeItems.get(i).setTag(tvHome.getObjects().get(i).getItem_url()+","+"item");
                   else
                	 homeItems.get(i).setTag(tvHome.getObjects().get(i).getItem_url()+","+"play");
                }

            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.e(TAG, "getTvHome --> failure " + retrofitError.getMessage());
            }
        });

    }

    public void getChannels() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint(HttpClient.HOST)
                .build();
        Channels channels = restAdapter.create(Channels.class);
        channels.excute(new Callback<List<ChannelEntity>>() {
            @Override
            public void success(List<ChannelEntity> channelBeans, Response response) {
                Log.d(TAG, "getFrontPage --> success");
                for (int i = 0; i < channelBeans.size(); i++) {


                    Picasso.with(LauncherActivity.this)
                            .load(channelBeans.get(i).getIcon_url())
                            .placeholder(R.drawable.preview)
                            .error(R.drawable.preview)
                            .into(channelImages[i]);
                    channelTexts[i].setText(channelBeans.get(i).getName());
                    channelTexts[i].setTextColor(Color.WHITE);
                    TextPaint tp = channelTexts[i].getPaint();
                    tp.setFakeBoldText(true);
                    HashMap<Integer, String> hashMap = new HashMap<Integer, String>();
                    hashMap.put(1, channelBeans.get(i).getName());
                    hashMap.put(2, channelBeans.get(i).getUrl());
                    hashMap.put(3, channelBeans.get(i).getChannel());
                    channelImtes[i].setTag(hashMap);
                    channelImtes[i].setOnClickListener(LauncherActivity.this);
                }

            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.e(TAG, "getFrontPage --> failure " + retrofitError.getMessage());
            }
        });
    }


    private void getLatest() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint(HttpClient.HOST)
                .build();
        Latest latest = restAdapter.create(Latest.class);
        latest.excute(new Callback<VideoEntity>() {
            @Override
            public void success(VideoEntity videoEntity, Response response) {
                Log.d(TAG, "getFrontPage --> success");
                for (int i = 0; i < 3; i++) {


                    Picasso.with(LauncherActivity.this)
                            .load(videoEntity.getObjects().get(i).getImage())
                            .placeholder(R.drawable.preview)
                            .error(R.drawable.preview)
                            .into(recomends[i]);
                    boolean is_complex = videoEntity.getObjects().get(i).isIs_complex();
                    if(is_complex)
                    	recomends[i].setTag(videoEntity.getObjects().get(i).getItem_url()+",item");
                    else
                        recomends[i].setTag(videoEntity.getObjects().get(i).getItem_url()+",play");

                    recomends[i].setOnClickListener(viewItemClickListener);

                }
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.e(TAG, "getFrontPage --> failure " + retrofitError.getMessage());
            }
        });
    }

    interface Client {
        @GET("/api/tv/frontpage/")
        void excute(
                Callback<FrontPageEntity> callback
        );
    }


    interface TvHome {
        @GET("/api/tv/section/tvhome/")
        void excute(
                Callback<VideoEntity> callback
        );
    }

    interface Channels {
        @GET("/api/tv/channels/")
        void excute(
                Callback<List<ChannelEntity>> callback
        );
    }

    interface Latest {
        @GET("/api/tv/section/xinpianshangxian/")
        void excute(
                Callback<VideoEntity> callback
        );
    }


    View.OnClickListener viewItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            String[] values = view.getTag().toString().split(",");
            if(values[1].equals("item"))
            	intent.setClassName("tv.ismar.daisy", "tv.ismar.daisy.ItemDetailActivity");
            else
                intent.setClassName("tv.ismar.daisy", "tv.ismar.daisy.PlayerActivity");

            intent.putExtra("url", values[0]);
            startActivity(intent);
        }
    };


    private void fetchWeather() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint(Weather.HOST)
                .build();
        Weather weather = restAdapter.create(Weather.class);
        weather.excute(new Callback<WeatherEntity>() {
            @Override
            public void success(WeatherEntity weatherEntity, Response response) {

                weatherTmp.setText(weatherEntity.getName() + "  " + weatherEntity.getToday().getTemperature() + " ℃");
                todayWeatherDetail.setText(weatherEntity.getToday().getPhenomenon() + "  " + weatherEntity.getToday().getWind_direction());
                tomorrowDetail.setText(weatherEntity.getTomorrow().getTemperature() + " ℃ " + weatherEntity.getTomorrow().getPhenomenon() +
                                " " + weatherEntity.getTomorrow().getWind_direction()
                );


            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    interface Weather {
        public static final String HOST = "http://media.lily.tvxio.com";

        @GET("/101010100.json")
        void excute(
                Callback<WeatherEntity> callback
        );
    }


}
