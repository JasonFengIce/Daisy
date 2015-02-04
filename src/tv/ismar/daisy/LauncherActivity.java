package tv.ismar.daisy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import org.json.JSONException;
import org.json.JSONObject;

import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.player.InitPlayerTool;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.webkit.URLUtil;
import android.widget.ImageButton;
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
    private static final int GETDOMAIN = 0x06;
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
	private String mLocalPath;
	private String mRemoteUrl;
	private long readSize = 0;
	private static final int READY_BUFF = 2000 * 1024;
	private static final int CACHE_BUFF = 500 * 1024;

	private boolean isready = false;
	private boolean iserror = false;
	private int errorCnt = 0;
	private int curPosition = 0;
	private long mediaLength = 0;

	private final static int VIDEO_STATE_UPDATE = 0;
	private final static int CACHE_VIDEO_READY = 1;
	private final static int CACHE_VIDEO_UPDATE = 2;
	private final static int CACHE_VIDEO_END = 3;
	private boolean isfinished = false;
	String url = "cord.tvxio.com/v2_0/A21/dto";
	private SimpleRestClient simpleRestClient;
	String sn;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_launcher);
		initViews();
		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		int height = metric.heightPixels; // 屏幕高度（像素）
		int densityDpi = metric.densityDpi; // 屏幕密度DPI（120 / 160 / 240）
		Log.i("zjq", "densityDpi==" + densityDpi + "heightPixels=" + height);
		float rate = (float) densityDpi / (float) 160;
		VodApplication.rate = rate;
		String domain = DaisyUtils.getVodApplication(this).getPreferences().getString("domain", "");
		Log.i("qqq", "application oncreate");
		if("".equals(domain))
		    register();
		else{
			SimpleRestClient.root_url = domain;
			SimpleRestClient.sRoot_url = domain;
			mainHandler.sendEmptyMessage(GETDOMAIN);
		}
	}

	private void register(){
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				 sn = Build.SERIAL;
			     if(sn==null||(sn!=null&&sn.equals("unknown"))){
			        	sn = VodApplication.getDeviceId(LauncherActivity.this);
			        }
			     String responseCode = SimpleRestClient.readContentFromPost("register", sn);
			     if(responseCode!=null&&responseCode.equals("200")){
			    	 active();
			    	 mainHandler.sendEmptyMessage(GETDOMAIN);
			     }
			}
		}).start();
	}
	private void active(){
		String content = SimpleRestClient.readContentFromPost("active", sn);
		if(!"".equals(content)){
			try {
				JSONObject json = new JSONObject(content);
				String domain = json.getString("domain");
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				SimpleRestClient.root_url = "http://"+domain;
				SimpleRestClient.sRoot_url = "http://"+domain;
				SimpleRestClient.ad_domain = "http://"+json.getString("ad_domain");
				DaisyUtils.getVodApplication(LauncherActivity.this).getEditor().putString("domain", SimpleRestClient.root_url);
				DaisyUtils.getVodApplication(LauncherActivity.this).getEditor().putString("ad_domain", SimpleRestClient.ad_domain);
				DaisyUtils.getVodApplication(LauncherActivity.this).save();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
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
	    intent = new Intent();
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		String content = (String) view.getTag();
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
	    intent = new Intent();
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		String values[] = view.getTag().toString().split(",");
		if (values[1].equals("item")){
			intent.setClassName("tv.ismar.daisy",
					"tv.ismar.daisy.ItemDetailActivity");
			intent.putExtra("url", values[0]);
			startActivity(intent);
		}

		else{
			InitPlayerTool tool = new InitPlayerTool(LauncherActivity.this);
			tool.initClipInfo(InitPlayerTool.FLAG_URL, values[0]);
		}
	}

	private Intent intent;
	private void setFrontPage(String content) {
		Log.i("hqiguai", content);
		try {
			Gson gson = new Gson();
			FrontPageEntity frontBeans = gson.fromJson(content.toString(),
					FrontPageEntity.class);
			final Uri uri = Uri.parse(frontBeans.getVideos().get(0)
					.getVideo_url());
			mRemoteUrl = frontBeans.getVideos().get(0).getVideo_url();
			// mRemoteUrl = "http://192.168.1.185:8099/shipinkefu/22.mp4";
			int position = mRemoteUrl.lastIndexOf("/");
			String fileName = mRemoteUrl.substring(position + 1,
					mRemoteUrl.length());
			String realname = fileName.substring(0, fileName.lastIndexOf("?"));
			mLocalPath = Environment.getExternalStorageDirectory()
					.getAbsolutePath() + "/VideoCache/" + realname;
			// videoView.setVideoPath(mLocalPath);
			// videoView.start();
			playvideo();
			videoView.setKeepScreenOn(true);

			videoView
					.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

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
							// videoView.setVideoURI(uri);
							// videoView.start();
						}
					});
			videoView.setOnErrorListener(new OnErrorListener() {

				public boolean onError(MediaPlayer mediaplayer, int i, int j) {
					iserror = true;
					errorCnt++;
					videoView.pause();
					return true;
				}
			});
		} catch (Exception e) {

		}

	}

	FileOutputStream out = null;
	InputStream is = null;

	private void playvideo() {
		if (!URLUtil.isNetworkUrl(this.mRemoteUrl)) {
			videoView.setVideoPath(this.mRemoteUrl);
			videoView.start();
			return;
		}
		new Thread(new Runnable() {

			@Override
			public void run() {

				try {
					File cacheFile = new File(mLocalPath);
					if (!cacheFile.exists()) {
						cacheFile.getParentFile().mkdirs();
						String mLocalDir = Environment
								.getExternalStorageDirectory()
								.getAbsolutePath()
								+ "/VideoCache";
						File Dir = new File(mLocalDir);
						for (String s : Dir.list()) {
							File f = new File(s);
							if (f.exists()) {
								f.delete();
							}
						}
						cacheFile.createNewFile();
					} else {
						long TotalFile = DaisyUtils
								.getVodApplication(LauncherActivity.this)
								.getPreferences().getLong("TotalSize", 0);
						long filesize = cacheFile.length();
						if (filesize >= TotalFile && filesize > 0) {
							mHandler.sendEmptyMessage(CACHE_VIDEO_END);
							return;
						}
					}
					readSize = cacheFile.length();
					URL url = new URL(mRemoteUrl);
					HttpURLConnection httpConnection = (HttpURLConnection) url
							.openConnection();
					System.out.println("localUrl: " + mLocalPath);
					out = new FileOutputStream(cacheFile, true);

					httpConnection.setRequestProperty("User-Agent", "NetFox");
					httpConnection.setRequestProperty("RANGE", "bytes="
							+ readSize + "-");
					is = httpConnection.getInputStream();
					String contentrange = httpConnection
							.getHeaderField("Content-Range");
					long Totalsize = Long.parseLong(contentrange.substring(
							contentrange.lastIndexOf("/") + 1,
							contentrange.length()));
					DaisyUtils.getVodApplication(LauncherActivity.this)
							.getEditor().putLong("TotalSize", Totalsize);
					DaisyUtils.getVodApplication(LauncherActivity.this).save();
					mediaLength = httpConnection.getContentLength();
					if (mediaLength == -1) {
						return;
					}

					mediaLength += readSize;

					byte buf[] = new byte[4 * 1024];
					int size = 0;
					long lastReadSize = 0;
						while ((size = is.read(buf)) != -1 && !isfinished) {
							try {
								out.write(buf, 0, size);
								Log.i("zjq", "write byte==" + size);
								readSize += size;
							} catch (Exception e) {
								e.printStackTrace();
							}

							if (!isready) {
								if ((readSize - lastReadSize) > READY_BUFF) {
									lastReadSize = readSize;
									mHandler.sendEmptyMessage(CACHE_VIDEO_READY);
								}
							} else {
								if ((readSize - lastReadSize) > CACHE_BUFF
										* (errorCnt + 1)) {
									lastReadSize = readSize;
									mHandler.sendEmptyMessage(CACHE_VIDEO_UPDATE);
								}
							}
						}
					Log.i("zjq", "download finish");
					if (!isfinished)
						mHandler.sendEmptyMessage(CACHE_VIDEO_END);
				} catch (Exception e) {
					if (!isfinished) {
						iserror = true;
						mHandler.sendEmptyMessage(CACHE_VIDEO_END);
					}
					e.printStackTrace();
				} finally {
					if (out != null) {
						try {
							out.close();
						} catch (IOException e) {
							//
						}
					}

					if (is != null) {
						try {
							is.close();
						} catch (IOException e) {
							//
						}
					}
				}

			}
		}).start();
	}

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case VIDEO_STATE_UPDATE:

				break;

			case CACHE_VIDEO_READY:
				isready = true;
				videoView.setVideoPath(mLocalPath);
				videoView.start();
				break;

			case CACHE_VIDEO_UPDATE:
				if (iserror) {
					videoView.setVideoPath(mLocalPath);
					videoView.start();
					iserror = false;
				}
				break;

			case CACHE_VIDEO_END:
				if (iserror || !videoView.isPlaying()) {
					videoView.setVideoPath(mLocalPath);
					videoView.start();
					iserror = false;
				}
				break;
			}

			// super.handleMessage(msg);
		}
	};

	private void setChannels(String content) {
		Gson gson = new Gson();
		ChannelEntity[] channelBeans = gson.fromJson(content.toString(),
				ChannelEntity[].class);
		for (int i = 0; i < channelBeans.length; i++) {
			channelTexts[i].setText(channelBeans[i].getName());
			channelTexts[i].setTextColor(Color.WHITE);
			TextPaint tp = channelTexts[i].getPaint();
			tp.setFakeBoldText(true);
			// HashMap<Integer, String> hashMap = new HashMap<Integer,
			// String>();
			// hashMap.put(1, channelBeans[i].getName());
			// hashMap.put(2, channelBeans[i].getUrl());
			// hashMap.put(3, channelBeans[i].getChannel());
			JSONObject jsonObject = new JSONObject();
			try {
				jsonObject.put("name", channelBeans[i].getName());
				jsonObject.put("url", channelBeans[i].getUrl());
				jsonObject.put("channel", channelBeans[i].getChannel());
			} catch (JSONException e) {
				if (e != null)
					e.printStackTrace();
			}
			channelImtes[i].setTag(jsonObject.toString());
			channelImtes[i].setOnClickListener(LauncherActivity.this);
		}
		if (channelBeans.length < channelTexts.length) {
			JSONObject jsonObject = new JSONObject();
			try {
				jsonObject.put("name", "");
				jsonObject.put("url", "");
				jsonObject.put("channel", "search");
			} catch (JSONException e) {
				if (e != null)
					e.printStackTrace();
			}
			channelTexts[channelBeans.length].setText("搜索");
			channelTexts[channelBeans.length].setTextColor(Color.WHITE);
			channelImtes[channelBeans.length].setTag(jsonObject.toString());
			channelImtes[channelBeans.length]
					.setOnClickListener(LauncherActivity.this);
		}
		if (channelBeans.length == channelTexts.length) {
			JSONObject jsonObject = new JSONObject();
			try {
				jsonObject.put("name", "");
				jsonObject.put("url", "");
				jsonObject.put("channel", "search");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			channelTexts[channelBeans.length - 1].setText("搜索");
			channelTexts[channelBeans.length - 1].setTextColor(Color.WHITE);
			channelImtes[channelBeans.length - 1].setTag(jsonObject.toString());
			channelImtes[channelBeans.length - 1]
					.setOnClickListener(LauncherActivity.this);
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
		    intent = new Intent();
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			String[] values = view.getTag().toString().split(",");
			if (values[1].equals("item")){
				intent.setClassName("tv.ismar.daisy",
						"tv.ismar.daisy.ItemDetailActivity");
				intent.putExtra("url", values[0]);
				startActivity(intent);
			}
			else{
				InitPlayerTool tool = new InitPlayerTool(LauncherActivity.this);
				tool.initClipInfo(values[0], InitPlayerTool.FLAG_URL);
			}
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
			case GETDOMAIN:
				DaisyUtils.getVodApplication(LauncherActivity.this).getNewContentModel();
				getFrontPage();
				getTvHome();
				getChannels();
				getLatest();
				fetchWeather();
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
					URL getUrl = new URL(SimpleRestClient.root_url
							+ "/api/tv/channels/");
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
					if (e != null)
						System.err.println(e.getMessage());
				} catch (IOException e) {
					if (e != null)
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
					Log.i("qqq", "url==" + SimpleRestClient.root_url);
					URL getUrl = new URL(SimpleRestClient.root_url
							+ "/api/tv/frontpage/");
					Log.i("hqiguai", "url==" + SimpleRestClient.root_url
							+ "/api/tv/frontpage/");
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
					if (e != null)
						System.err.println(e.getMessage());
				} catch (IOException e) {
					if (e != null)
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
					URL getUrl = new URL(SimpleRestClient.root_url
							+ "/api/tv/section/tvhome/");
					HttpURLConnection connection = (HttpURLConnection) getUrl
							.openConnection();
					connection.setReadTimeout(9000);
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
					if (e != null)
						System.err.println(e.getMessage());
				} catch (IOException e) {
					if (e != null)
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
					URL getUrl = new URL(SimpleRestClient.root_url
							+ "/api/tv/section/xinpianshangxian/");
					HttpURLConnection connection = (HttpURLConnection) getUrl
							.openConnection();
					connection.setReadTimeout(19000);
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
					if (e != null)
						System.err.println(e.getMessage());
				} catch (IOException e) {
					// System.err.println(e.getMessage());
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
					if (e != null)
						System.err.println(e.getMessage());
				} catch (IOException e) {
					if (e != null)
						System.err.println(e.getMessage());
				}
			}

		}.start();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
			if (keyCode == KeyEvent.KEYCODE_BACK) {
				isfinished = true;
				videoView.stopPlayback();
				mHandler.removeCallbacksAndMessages(null);
				finish();
			}
		return super.onKeyDown(keyCode, event);
	}
}
