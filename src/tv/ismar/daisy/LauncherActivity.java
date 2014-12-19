package tv.ismar.daisy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextPaint;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.ismartv.launcher.data.ChannelEntity;
import com.ismartv.launcher.data.FrontPageEntity;
import com.ismartv.launcher.data.VideoEntity;
import com.ismartv.launcher.data.WeatherEntity;
import com.ismartv.launcher.ui.widget.IsmartvVideoView;
import com.squareup.picasso.Picasso;

/**
 * Created by <huaijiefeng@gmail.com> on 9/9/14.
 */
public class LauncherActivity extends Activity implements View.OnClickListener {
	private static final String TAG = "LauncherActivity";

	private static final int NAME = 1;
	private static final int URL = 2;
	private static final int CHANNEL = 3;
	private static final int FETCHCHANNEL = 0x01;
	private static final int FETCHFRONTPAGE = 0x02;
	private static final int FETCHWEATHER = 0x03;
	private static final int FETCHLATEST = 0x04;
	private static final int FETCHTVHOME = 0x05;

	private IsmartvVideoView videoView;

	private ImageView[] homeImages;
	private TextView[] homeTitles;

	private ImageView[] channelImages;

	private TextView[] channelTexts;

	private RelativeLayout[] channelImtes;

	private ImageView[] recomends;

	private ImageView weatherIcon;
	private TextView weatherTmp;
	private TextView todayWeatherDetail;

	private TextView tomorrowDetail;

	private RelativeLayout[] homeItems;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_launcher);
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

		homeItems = new RelativeLayout[] {
				(RelativeLayout) findViewById(R.id.home_item_1),
				(RelativeLayout) findViewById(R.id.home_item_2),
				(RelativeLayout) findViewById(R.id.home_item_3),
				(RelativeLayout) findViewById(R.id.home_item_4),
				(RelativeLayout) findViewById(R.id.home_item_5),
				(RelativeLayout) findViewById(R.id.home_item_6) };

		homeImages = new ImageView[] { (ImageView) findViewById(R.id.home_1),
				(ImageView) findViewById(R.id.home_2),
				(ImageView) findViewById(R.id.home_3),
				(ImageView) findViewById(R.id.home_4),
				(ImageView) findViewById(R.id.home_5),
				(ImageView) findViewById(R.id.home_6) };

		homeTitles = new TextView[] {
				(TextView) findViewById(R.id.home_title_1),
				(TextView) findViewById(R.id.home_title_2),
				(TextView) findViewById(R.id.home_title_3),
				(TextView) findViewById(R.id.home_title_4),
				(TextView) findViewById(R.id.home_title_5),
				(TextView) findViewById(R.id.home_title_6) };

		channelImages = new ImageView[] {
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
				(ImageView) findViewById(R.id.channel_iv_12) };
		channelTexts = new TextView[] {
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
				(TextView) findViewById(R.id.channel_tv_12) };

		channelImtes = new RelativeLayout[] {
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
				(RelativeLayout) findViewById(R.id.channel_item_12), };
		recomends = new ImageView[] { (ImageView) findViewById(R.id.image_1),
				(ImageView) findViewById(R.id.image_2),
				(ImageView) findViewById(R.id.image_3) };

	}

	@Override
	public void onClick(View view) {
		Intent intent = new Intent();
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		String content =(String) view
				.getTag();
		JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(content);
		intent.putExtra("title", jsonObject.getString("name"));
		intent.putExtra("url", jsonObject.getString("url"));
		intent.putExtra("channel", jsonObject.getString("channel"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		intent.setClassName("tv.ismar.daisy",
				"tv.ismar.daisy.ChannelListActivity");
		this.startActivity(intent);
	}

	public void pickHomeItem(RelativeLayout view) {
		Intent intent = new Intent();
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		String values[] = view.getTag().toString().split(",");
		if (values[1].equals("item"))
			intent.setClassName("tv.ismar.daisy",
					"tv.ismar.daisy.ItemDetailActivity");
		else
			intent.setClassName("tv.ismar.daisy",
					"tv.ismar.daisy.PlayerActivity");

		intent.putExtra("url", values[0]);
		startActivity(intent);
	}

	private void setFrontPage(String content) {
		Gson gson = new Gson();
		FrontPageEntity frontBeans = gson.fromJson(content.toString(),
				FrontPageEntity.class);
		final Uri uri = Uri.parse(frontBeans.getVideos().get(0).getVideo_url());
		videoView.setVideoURI(uri);
		videoView.start();
		videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

			@Override
			public void onPrepared(MediaPlayer mp) {
				mp.start();
				mp.setLooping(true);
			}
		});
		videoView
				.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

					@Override
					public void onCompletion(MediaPlayer mp) {
						videoView.setVideoURI(uri);
						videoView.start();
					}
				});

	}

	private void setChannels(String content) {
		Gson gson = new Gson();
		ChannelEntity[] channelBeans = gson.fromJson(content.toString(),
				ChannelEntity[].class);
		for (int i = 0; i < channelBeans.length; i++) {
			channelTexts[i].setText(channelBeans[i].getName());
			channelTexts[i].setTextColor(Color.WHITE);
			TextPaint tp = channelTexts[i].getPaint();
			tp.setFakeBoldText(true);
//			HashMap<Integer, String> hashMap = new HashMap<Integer, String>();
//			hashMap.put(1, channelBeans[i].getName());
//			hashMap.put(2, channelBeans[i].getUrl());
//			hashMap.put(3, channelBeans[i].getChannel());
			JSONObject jsonObject = new JSONObject();
			try {
				jsonObject.put("name", channelBeans[i].getName());
				jsonObject.put("url", channelBeans[i].getUrl());
				jsonObject.put("channel", channelBeans[i].getChannel());
			} catch (JSONException e) {
				e.printStackTrace();
			}
			channelImtes[i].setTag(jsonObject.toString());
			channelImtes[i].setOnClickListener(LauncherActivity.this);
		}
	}

	private void setTvHome(String content) {
		Gson gson = new Gson();
		VideoEntity tvHome = gson.fromJson(content.toString(),
				VideoEntity.class);
		for (int i = 0; i < 6; i++) {
			homeTitles[i].setText(tvHome.getObjects().get(i).getTitle());
			Picasso.with(LauncherActivity.this)
					.load(tvHome.getObjects().get(i).getImage())
					.placeholder(R.drawable.preview).error(R.drawable.preview)
					.into(homeImages[i]);
			boolean is_complex = tvHome.getObjects().get(i).isIs_complex();
			if (is_complex)
				homeItems[i].setTag(tvHome.getObjects().get(i).getItem_url()
						+ "," + "item");
			else
				homeItems[i].setTag(tvHome.getObjects().get(i).getItem_url()
						+ "," + "play");
			homeItems[i].setOnClickListener(viewItemClickListener);
		}

	}

	private void setLatest(String content) {
		Gson gson = new Gson();
		VideoEntity videoEntity = gson.fromJson(content.toString(),
				VideoEntity.class);
		for (int i = 0; i < 3; i++) {

			Picasso.with(LauncherActivity.this)
					.load(videoEntity.getObjects().get(i).getImage())
					.placeholder(R.drawable.preview).error(R.drawable.preview)
					.into(recomends[i]);
			boolean is_complex = videoEntity.getObjects().get(i).isIs_complex();
			if (is_complex)
				recomends[i].setTag(videoEntity.getObjects().get(i)
						.getItem_url()
						+ ",item");
			else
				recomends[i].setTag(videoEntity.getObjects().get(i)
						.getItem_url()
						+ ",play");

			recomends[i].setOnClickListener(viewItemClickListener);
		}
	}

	private void setWeather(String content) {
		Gson gson = new Gson();
		WeatherEntity weatherEntity = gson.fromJson(content.toString(),
				WeatherEntity.class);
		weatherTmp.setText(weatherEntity.getName() + "  "
				+ weatherEntity.getToday().getTemperature() + " ℃");
		todayWeatherDetail.setText(weatherEntity.getToday().getPhenomenon()
				+ "  " + weatherEntity.getToday().getWind_direction());
		tomorrowDetail.setText(weatherEntity.getTomorrow().getTemperature()
				+ " ℃ " + weatherEntity.getTomorrow().getPhenomenon() + " "
				+ weatherEntity.getTomorrow().getWind_direction());

	}

	View.OnClickListener viewItemClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			Intent intent = new Intent();
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			String[] values = view.getTag().toString().split(",");
			if (values[1].equals("item"))
				intent.setClassName("tv.ismar.daisy",
						"tv.ismar.daisy.ItemDetailActivity");
			else
				intent.setClassName("tv.ismar.daisy",
						"tv.ismar.daisy.PlayerActivity");

			intent.putExtra("url", values[0]);
			startActivity(intent);
		}
	};

	private Handler mainHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			int type = msg.what;
			Bundle dataBundle = msg.getData();
			switch (type) {
			case FETCHCHANNEL:
				setChannels(dataBundle.getString("content"));
				break;
			case FETCHFRONTPAGE:
				setFrontPage(dataBundle.getString("content"));
				break;
			case FETCHWEATHER:
				setWeather(dataBundle.getString("content"));
				break;
			case FETCHLATEST:
				setLatest(dataBundle.getString("content"));
				break;
			case FETCHTVHOME:
				setTvHome(dataBundle.getString("content"));
				break;
			}
		}
	};

	private void getChannels() {
		new Thread() {
			@Override
			public void run() {
				super.run();
				StringBuffer content = new StringBuffer();
				try {
					URL getUrl = new URL(
							"http://cord.tvxio.com/api/tv/channels/");
					HttpURLConnection connection = (HttpURLConnection) getUrl
							.openConnection();
					connection.setReadTimeout(4000);
					connection.connect();
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(connection.getInputStream()));
					String lines;
					while ((lines = reader.readLine()) != null) {
						content.append(lines);
					}
					Message message = new Message();
					Bundle data = new Bundle();
					data.putString("content", content.toString());
					message.setData(data);
					message.what = FETCHCHANNEL;
					mainHandler.sendMessage(message);
				} catch (MalformedURLException e) {
					System.err.println(e.getMessage());
				} catch (IOException e) {
					System.err.println(e.getMessage());
				}
			}

		}.start();
	}

	private void getFrontPage() {
		new Thread() {
			@Override
			public void run() {
				super.run();
				StringBuffer content = new StringBuffer();
				try {
					URL getUrl = new URL(
							"http://cord.tvxio.com/api/tv/frontpage/");
					HttpURLConnection connection = (HttpURLConnection) getUrl
							.openConnection();
					connection.setReadTimeout(4000);
					connection.connect();
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(connection.getInputStream()));
					String lines;
					while ((lines = reader.readLine()) != null) {
						content.append(lines);
					}
					Message message = new Message();
					Bundle data = new Bundle();
					data.putString("content", content.toString());
					message.setData(data);
					message.what = FETCHFRONTPAGE;
					mainHandler.sendMessage(message);
				} catch (MalformedURLException e) {
					System.err.println(e.getMessage());
				} catch (IOException e) {
					System.err.println(e.getMessage());
				}
			}

		}.start();
	}

	private void getTvHome() {
		new Thread() {
			@Override
			public void run() {
				super.run();
				StringBuffer content = new StringBuffer();
				try {
					URL getUrl = new URL(
							"http://cord.tvxio.com/api/tv/section/tvhome/");
					HttpURLConnection connection = (HttpURLConnection) getUrl
							.openConnection();
					connection.setReadTimeout(4000);
					connection.connect();
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(connection.getInputStream()));
					String lines;
					while ((lines = reader.readLine()) != null) {
						content.append(lines);
					}
					Message message = new Message();
					Bundle data = new Bundle();
					data.putString("content", content.toString());
					message.setData(data);
					message.what = FETCHTVHOME;
					mainHandler.sendMessage(message);
				} catch (MalformedURLException e) {
					System.err.println(e.getMessage());
				} catch (IOException e) {
					System.err.println(e.getMessage());
				}
			}

		}.start();
	}

	private void getLatest() {
		new Thread() {
			@Override
			public void run() {
				super.run();
				StringBuffer content = new StringBuffer();
				try {
					URL getUrl = new URL(
							"http://cord.tvxio.com/api/tv/section/xinpianshangxian/");
					HttpURLConnection connection = (HttpURLConnection) getUrl
							.openConnection();
					connection.setReadTimeout(4000);
					connection.connect();
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(connection.getInputStream()));
					String lines;
					while ((lines = reader.readLine()) != null) {
						content.append(lines);
					}
					Message message = new Message();
					Bundle data = new Bundle();
					data.putString("content", content.toString());
					message.setData(data);
					message.what = FETCHLATEST;
					mainHandler.sendMessage(message);
				} catch (MalformedURLException e) {
					System.err.println(e.getMessage());
				} catch (IOException e) {
					System.err.println(e.getMessage());
				}
			}

		}.start();
	}

	private void fetchWeather() {
		new Thread() {
			@Override
			public void run() {
				super.run();
				StringBuffer content = new StringBuffer();
				try {
					URL getUrl = new URL(
							"http://media.lily.tvxio.com/101010100.json");
					HttpURLConnection connection = (HttpURLConnection) getUrl
							.openConnection();
					connection.setReadTimeout(4000);
					connection.connect();
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(connection.getInputStream()));
					String lines;
					while ((lines = reader.readLine()) != null) {
						content.append(lines);
					}
					Message message = new Message();
					Bundle data = new Bundle();
					data.putString("content", content.toString());
					message.setData(data);
					message.what = FETCHWEATHER;
					mainHandler.sendMessage(message);
				} catch (MalformedURLException e) {
					System.err.println(e.getMessage());
				} catch (IOException e) {
					System.err.println(e.getMessage());
				}
			}

		}.start();
	}
}
