package tv.ismar.daisy.ui.fragment.launcher;

import android.R.integer;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.google.gson.Gson;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import tv.ismar.daisy.R;
import tv.ismar.daisy.core.client.DownloadClient;
import tv.ismar.daisy.core.client.DownloadThreadPool;
import tv.ismar.daisy.core.client.IsmartvUrlClient;
import tv.ismar.daisy.data.HomePagerEntity;
import tv.ismar.daisy.data.HomePagerEntity.Carousel;
import tv.ismar.daisy.data.table.DownloadTable;
import tv.ismar.daisy.player.InitPlayerTool;
import tv.ismar.daisy.ui.fragment.ChannelBaseFragment;
import tv.ismar.daisy.ui.widget.DaisyVideoView;
import tv.ismar.daisy.ui.widget.HomeItemContainer;
import tv.ismar.daisy.utils.HardwareUtils;
import tv.ismar.daisy.views.LabelImageView;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by huaijie on 5/18/15.
 */
public class FilmFragment extends ChannelBaseFragment implements Flag.ChangeCallback {
    private static final String TAG = "FilmFragment";

    private LinearLayout guideRecommmendList;
    private LinearLayout carouselLayout;
    private FrameLayout film_post_layout;
    private DaisyVideoView linkedVideoView;
    private ImageView linkedVideoImage;
    private TextView film_linked_title;
    private LabelImageView film_lefttop_image;

    private ArrayList<Carousel> carousels;
    private ArrayList<LabelImageView> allItem;

    private Flag flag;

    private MessageHandler messageHandler;


    private MediaPlayer.OnCompletionListener loopAllListener;

    private MediaPlayer.OnCompletionListener loopCurrentListener;

    private boolean focusFlag = true;

    private BroadcastReceiver externalStorageReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        externalStorageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (linkedVideoView.getVisibility() == View.VISIBLE) {
                    if (flag.getPosition() + 1 >= carousels.size()) {
                        flag.setPosition(0);
                    } else {
                        flag.setPosition(flag.getPosition() + 1);
                    }
                    playCarousel();
                }
            }
        };

    }

    @Override
    public void onStart() {
        super.onStart();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
        intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
        intentFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
        intentFilter.addDataScheme("file");
        context.registerReceiver(externalStorageReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        super.onStop();
        context.unregisterReceiver(externalStorageReceiver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mView = LayoutInflater.from(context).inflate(
                R.layout.fragment_film, null);
        guideRecommmendList = (LinearLayout) mView
                .findViewById(R.id.film_recommend_list);
        carouselLayout = (LinearLayout) mView
                .findViewById(R.id.film_carousel_layout);
        linkedVideoView = (DaisyVideoView) mView
                .findViewById(R.id.film_linked_video);
        film_lefttop_image = (LabelImageView) mView
                .findViewById(R.id.film_lefttop_image);
        film_post_layout = (HomeItemContainer) mView.findViewById(R.id.film_post_layout);
        linkedVideoImage = (ImageView) mView.findViewById(R.id.film_linked_image);
        film_linked_title = (TextView) mView.findViewById(R.id.film_linked_title);
        flag = new Flag(this);

        messageHandler = new MessageHandler();

        loopAllListener = new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (flag.getPosition() + 1 >= carousels.size()) {
                    flag.setPosition(0);
                } else {
                    flag.setPosition(flag.getPosition() + 1);
                }
                playCarousel();
            }
        };

        loopCurrentListener = new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playCarousel();
            }
        };

        View.OnClickListener viewClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = carousels.get(flag.getPosition()).getUrl();
                String model = carousels.get(flag.getPosition()).getModel_name();
                String title = carousels.get(flag.getPosition()).getTitle();
                Intent intent = new Intent();
                Log.d(TAG, "item click:\n "
                        + "model: " + model);
                if ("item".equals(model)) {
                    intent.setClassName("tv.ismar.daisy", "tv.ismar.daisy.ItemDetailActivity");
                    intent.putExtra("url", url);
                    context.startActivity(intent);
                } else if ("topic".equals(model)) {
                    intent.putExtra("url", url);
                    intent.setClassName("tv.ismar.daisy", "tv.ismar.daisy.TopicActivity");
                    context.startActivity(intent);
                } else if ("section".equals(model)) {
                    intent.putExtra("title", title);
                    intent.putExtra("itemlistUrl", url);
                    intent.putExtra("lableString", title);
                    intent.setClassName("tv.ismar.daisy", "tv.ismar.daisy.PackageListDetailActivity");
                    context.startActivity(intent);
                }else if ("package".equals(model)) {
                    intent.setAction("tv.ismar.daisy.packageitem");
                    intent.putExtra("url", url);
                    context.startActivity(intent);
                } else if ("clip".equals(model)) {
                    InitPlayerTool tool = new InitPlayerTool(context);
                    tool.initClipInfo(url, InitPlayerTool.FLAG_URL);
                }

            }
        };

        linkedVideoView.setOnCompletionListener(loopAllListener);
        film_post_layout.setOnClickListener(viewClickListener);

        return mView;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fetchHomePage(channelEntity.getHomepage_url());

    }

    private void fetchHomePage(String url) {
        new IsmartvUrlClient().doRequest(url, new IsmartvUrlClient.CallBack() {
            @Override
            public void onSuccess(String result) {
                HomePagerEntity homePagerEntity = new Gson().fromJson(result, HomePagerEntity.class);
                ArrayList<HomePagerEntity.Poster> posters = homePagerEntity.getPosters();
                ArrayList<HomePagerEntity.Carousel> carousels = homePagerEntity.getCarousels();

                Log.d(TAG, "posters size: " + posters.size());
                Log.d(TAG, "carousels size: " + carousels.size());

                initPosters(posters);
                initCarousel(carousels);
            }

            @Override
            public void onFailed(Exception exception) {
                Log.e(TAG, exception.getMessage());
            }
        });

    }

    private void initPosters(ArrayList<HomePagerEntity.Poster> posters) {
        film_lefttop_image.setUrl(posters.get(0).getCustom_image());
        film_lefttop_image.setFocustitle(posters.get(0).getIntroduction());
        film_lefttop_image.setOnClickListener(ItemClickListener);
        film_lefttop_image.setTag(posters.get(0));
        for (int i = 1; i <= posters.size(); i++) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(199, 278);
//            params.weight = 1;
//            if (i != 7) {
//            params.setMargins(0, 0, 28, 0);
//            }
            if (i == 6) {
                params.setMargins(0, 0, 27, 0);
            } else if (i == 7) {
                params.setMargins(0, 0, 8, 0);
            } else {
                params.setMargins(0, 0, 28, 0);
            }
            ImageView itemView = new ImageView(context);
//            itemView.setBackgroundResource(R.drawable.launcher_selector);
            itemView.setFocusable(true);
            itemView.setLayoutParams(params);
            itemView.setOnClickListener(ItemClickListener);
            if (i <= 7) {
                tv.ismar.daisy.ui.widget.HomeItemContainer frameLayout = (tv.ismar.daisy.ui.widget.HomeItemContainer) LayoutInflater.from(context).inflate(R.layout.item_poster, null);
                ImageView postitemView = (ImageView) frameLayout.findViewById(R.id.poster_image);
                TextView textView = (TextView) frameLayout.findViewById(R.id.poster_title);
                if (StringUtils.isNotEmpty(posters.get(i).getIntroduction())) {
                    textView.setText(posters.get(i).getIntroduction());
                    textView.setVisibility(View.VISIBLE);
                }
                textView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {
                            ((HomeItemContainer) v.getParent())
                                    .setDrawBorder(true);
                            ((HomeItemContainer) v.getParent()).invalidate();
                        } else {
                            ((HomeItemContainer) v.getParent())
                                    .setDrawBorder(false);
                            ((HomeItemContainer) v.getParent()).invalidate();
                        }
                    }
                });
                textView.setOnClickListener(ItemClickListener);
                frameLayout.setOnClickListener(ItemClickListener);
                Picasso.with(context).load(posters.get(i).getCustom_image()).into(postitemView);
//                textView.setTag(posters.get(i));
                frameLayout.setTag(posters.get(i));
                frameLayout.setLayoutParams(params);
                guideRecommmendList.addView(frameLayout);
            } else {
                params.width = 206;
                params.setMargins(0, 0, 0, 0);
                tv.ismar.daisy.ui.widget.HomeItemContainer morelayout = (tv.ismar.daisy.ui.widget.HomeItemContainer) LayoutInflater.from(
                        context).inflate(R.layout.toppagelistmorebutton,
                        null);
                morelayout.setLayoutParams(params);
                View view = morelayout.findViewById(R.id.listmore);
                view.setOnClickListener(ItemClickListener);
                guideRecommmendList.addView(morelayout);
            }
        }
    }


    private void playCarousel() {
        Log.d(TAG, "focus flag: " + focusFlag);
        messageHandler.removeMessages(0);
        String videoUrl = carousels.get(flag.getPosition()).getVideo_url();

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            if (!TextUtils.isEmpty(videoUrl)) {
                try {
                    File file = new File(HardwareUtils.getSDCardCachePath(), "/text/text.mp4");
                    if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();
                    }
                    file.createNewFile();
                    file.delete();
                    playVideo();
                } catch (IOException e) {
                    playImage();
                }
            } else {
                playImage();
            }
        } else {
            playImage();
        }
    }


    private void initCarousel(final ArrayList<HomePagerEntity.Carousel> carousels) {
        allItem = new ArrayList<LabelImageView>();
        this.carousels = carousels;
        final String tag = getChannelEntity().getChannel();

        deleteFile(carousels, tag);
        downloadVideo(carousels, tag);


        for (int i = 0; i < carousels.size(); i++) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    206, 86);
//            params.weight = 1;
            if (i == 0)
                params.topMargin = 0;
            else
                params.topMargin = 17;
            LabelImageView itemView = new LabelImageView(context);
//            itemView.setBackgroundResource(R.drawable.launcher_selector);
            itemView.setFocusable(true);
            Picasso.with(context).load(carousels.get(i).getThumb_image())
                    .into(itemView);
            itemView.setScaleType(ImageView.ScaleType.FIT_XY);
            itemView.setLayoutParams(params);
            itemView.setTag(i);
            itemView.setTag(R.drawable.launcher_selector, carousels.get(i));
            itemView.setOnClickListener(ItemClickListener);
            int shadowcolor = getActivity().getResources().getColor(R.color.carousel_focus);
            itemView.setFrontcolor(shadowcolor);
            allItem.add(itemView);
            carouselLayout.addView(itemView);
        }


        View.OnFocusChangeListener itemFocusListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                focusFlag = true;
                for (ImageView imageView : allItem) {
                    focusFlag = focusFlag && (!imageView.isFocused());
                }
                //all view not focus
                if (focusFlag) {
                    linkedVideoView.setOnCompletionListener(loopAllListener);
                } else {
                    flag.setPosition((Integer) v.getTag());
                    linkedVideoView.setOnCompletionListener(loopCurrentListener);
                    playCarousel();
                }

            }
        };

        for (ImageView imageView : allItem) {
            imageView.setOnFocusChangeListener(itemFocusListener);
        }

        flag.setPosition(0);
        View view = getView();
        if (view != null) {
            view.postDelayed(new Runnable() {
                @Override
                public void run() {
                    playCarousel();
                }
            }, 1000);
        }

    }


    private void playImage() {
        if (linkedVideoView.getVisibility() == View.VISIBLE) {
            linkedVideoView.setVisibility(View.GONE);
        }

        if (linkedVideoImage.getVisibility() == View.GONE) {
            linkedVideoImage.setVisibility(View.VISIBLE);
        }

        String url = carousels.get(flag.getPosition()).getVideo_image();

        String intro = carousels.get(flag.getPosition()).getIntroduction();
        if (StringUtils.isNotEmpty(intro)) {
            film_linked_title.setVisibility(View.VISIBLE);
            film_linked_title.setText(intro);
        } else {
            film_linked_title.setVisibility(View.GONE);
        }
        Picasso.with(context).load(url).into(linkedVideoImage, new Callback() {
            int pauseTime = Integer.parseInt(carousels.get(flag.getPosition()).getPause_time());

            @Override
            public void onSuccess() {
                messageHandler.sendEmptyMessageDelayed(0, pauseTime * 1000);
            }

            @Override
            public void onError() {
                messageHandler.sendEmptyMessageDelayed(0, pauseTime * 1000);
            }
        });
    }


    private void playVideo() {
        if (linkedVideoView.getVisibility() == View.GONE) {
            linkedVideoView.setVisibility(View.VISIBLE);
        }

        if (linkedVideoImage.getVisibility() == View.VISIBLE) {
            linkedVideoImage.setVisibility(View.GONE);
        }

        String url = carousels.get(flag.getPosition()).getVideo_url();
        String playPath;
        DownloadTable downloadTable = new Select().from(DownloadTable.class).where(DownloadTable.URL + " = ?", url).executeSingle();
        if (downloadTable == null) {
            playPath = url;
        } else {
            File localVideoFile = new File(downloadTable.download_path);
            String fileMd5Code = localVideoFile.getName().split("\\.")[0];
            if (fileMd5Code.equalsIgnoreCase(downloadTable.md5)) {
                playPath = localVideoFile.getAbsolutePath();
            } else {
                playPath = url;
            }
        }
        Log.d(TAG, "set video path: " + playPath);
        linkedVideoView.setVideoPath(playPath);
        linkedVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });
    }

    @Override
    public void change(int position) {
        Log.d(TAG, "changed position: " + position);
        for (int i = 0; i < allItem.size(); i++) {
            LabelImageView imageView = allItem.get(i);
            if (position != i) {
                imageView.setCustomfocus(false);
            } else {
                imageView.setCustomfocus(true);
            }
        }
    }


    private class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (focusFlag) {
                if (flag.getPosition() + 1 >= carousels.size()) {
                    flag.setPosition(0);
                } else {
                    flag.setPosition(flag.getPosition() + 1);
                }
            }
            playCarousel();
        }
    }


    private void downloadVideo(ArrayList<Carousel> carousels, String tag) {
        for (Carousel carousel : carousels) {
            String url = carousel.getVideo_url();

            if (!TextUtils.isEmpty(url)) {
                List<DownloadTable> downloadTables = new Select().from(DownloadTable.class).where(DownloadTable.URL + " = ?", carousel.getVideo_url()).execute();
                if (!downloadTables.isEmpty()) {
                    String localFilePath = downloadTables.get(0).download_path;
                    File localFile = new File(localFilePath);
                    String fileMd5Code = localFile.getName().split("\\.")[0];
                    if (!fileMd5Code.equalsIgnoreCase(downloadTables.get(0).md5)) {
                        for (DownloadTable downloadTable : downloadTables) {
                            File file = new File(downloadTable.download_path);
                            if (file.exists()) {
                                file.delete();
                            }
                            downloadTable.delete();
                        }
                        // download file
                        download(url, tag);
                    } else {
                        for (DownloadTable downloadTable : downloadTables) {
                            File file = new File(downloadTable.download_path);
                            if (!file.exists()) {
                                downloadTable.delete();
                                download(url, tag);
                            } else {
                                //nothing file already download
                            }
                        }

                    }
                    // if table is empty download video
                } else {
                    download(url, tag);
                }
            }
        }
    }


    private void download(String url, String tag) {
        String savePath = HardwareUtils.getCachePath(context) + "/" + tag + "/";
        DownloadClient downloadClient = new DownloadClient(url, savePath);
        DownloadThreadPool.getInstance().add(downloadClient);
    }

    private void deleteFile(ArrayList<HomePagerEntity.Carousel> carousels, String tag) {
        String savePath = HardwareUtils.getCachePath(context) + "/" + tag + "/";
        ArrayList<String> exceptsPaths = new ArrayList<String>();
        for (HomePagerEntity.Carousel carousel : carousels) {
            String url = carousel.getVideo_url();
            if (!TextUtils.isEmpty(url)) {
                try {
                    File file = new File(new URL(url).getFile());
                    exceptsPaths.add(file.getName());
                } catch (MalformedURLException e) {
                    Log.e(TAG, "deleteFile: " + e.getMessage());
                }
            }

        }
        HardwareUtils.deleteFiles(savePath, exceptsPaths);
    }

}

