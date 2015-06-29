package tv.ismar.daisy.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import tv.ismar.daisy.data.HomePagerEntity.Poster;
import tv.ismar.daisy.data.table.DownloadTable;
import tv.ismar.daisy.ui.widget.DaisyVideoView;
import tv.ismar.daisy.utils.HardwareUtils;
import tv.ismar.daisy.views.LabelImageView;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by huaijie on 5/18/15.
 */
public class FilmFragment extends ChannelBaseFragment implements Flag.ChangeCallback {
    private static final String TAG = "FilmFragment";

    private LinearLayout guideRecommmendList;
    private LinearLayout carouselLayout;
    private DaisyVideoView linkedVideoView;
    private ImageView linkedVideoImage;
    private LabelImageView film_lefttop_image;
    private Context context;


    private ArrayList<Carousel> carousels;
    private ArrayList<ImageView> allItem;

    private Flag flag;

    private MessageHandler messageHandler;


    private MediaPlayer.OnCompletionListener loopAllListener;

    private MediaPlayer.OnCompletionListener loopCurrentListener;

    private boolean focusFlag = true;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.context = activity;
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
        linkedVideoImage = (ImageView) mView.findViewById(R.id.film_linked_image);

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

        View.OnClickListener viewClickListener = new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String url = carousels.get(flag.getPosition()).getUrl();
                String model = carousels.get(flag.getPosition()).getModel_name();
                String title = carousels.get(flag.getPosition()).getTitle();
                Intent intent = new Intent();
                if ("item".equals(model)) {
                    intent.setClassName("tv.ismar.daisy",
                            "tv.ismar.daisy.ItemDetailActivity");
                    intent.putExtra("url", url);
                } else if ("topic".equals(model)) {
                    intent.putExtra("url",
                            url);
                    intent.setClassName("tv.ismar.daisy",
                            "tv.ismar.daisy.TopicActivity");
                } else if ("section".equals(model)) {
                    intent.putExtra("title", title);
                    intent.putExtra("itemlistUrl",
                            url);
                    intent.putExtra("lableString",
                            title);
                    intent.setClassName("tv.ismar.daisy",
                            "tv.ismar.daisy.PackageListDetailActivity");
                }
                context.startActivity(intent);
            }
        };

        linkedVideoView.setOnCompletionListener(loopAllListener);
        linkedVideoView.setOnClickListener(viewClickListener);
        linkedVideoImage.setOnClickListener(viewClickListener);

        return mView;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fetchHomePage(channelEntity.getHomepage_url());
    }

    private void fetchHomePage(String url) {
        new IsmartvUrlClient(context).doRequest(url, new IsmartvUrlClient.CallBack() {
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
        for (int i = 1; i <= posters.size(); i++) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            params.weight = 1;
//            if (i != 7) {
            params.setMargins(0, 0, 25, 0);
//            }
            ImageView itemView = new ImageView(context);
            itemView.setBackgroundResource(R.drawable.launcher_selector);
            itemView.setFocusable(true);
            itemView.setOnClickListener(ItemClickListener);
            if (i <= 7) {
                Picasso.with(context)
                        .load(posters.get(i).getCustom_image()).into(itemView);
                itemView.setScaleType(ImageView.ScaleType.FIT_XY);
                itemView.setLayoutParams(params);
                itemView.setTag(posters.get(i));
                guideRecommmendList.addView(itemView);
            } else {
                params.setMargins(0, 0, 30, 0);
                LinearLayout morelayout = (LinearLayout) LayoutInflater.from(
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
        allItem = new ArrayList<ImageView>();
        this.carousels = carousels;
        final String tag = getChannelEntity().getChannel();

        deleteFile(carousels, tag);


        for (int i = 0; i < carousels.size(); i++) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 0);
            params.weight = 1;
            ImageView itemView = new ImageView(context);
            itemView.setBackgroundResource(R.drawable.launcher_selector);
            itemView.setFocusable(true);
            Picasso.with(context).load(carousels.get(i).getThumb_image())
                    .into(itemView);
            itemView.setScaleType(ImageView.ScaleType.FIT_XY);
            itemView.setLayoutParams(params);
            itemView.setTag(i);
            itemView.setOnClickListener(ItemClickListener);

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
        playCarousel();


    }


    private void playImage() {
        if (linkedVideoView.getVisibility() == View.VISIBLE) {
            linkedVideoView.setVisibility(View.GONE);
        }

        if (linkedVideoImage.getVisibility() == View.GONE) {
            linkedVideoImage.setVisibility(View.VISIBLE);
        }

        String url = carousels.get(flag.getPosition()).getVideo_image();


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

    private View.OnClickListener ItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String url = null;
            String contentMode = null;
            String title = null;
            if (view.getTag() instanceof Poster) {
                Poster new_name = (Poster) view.getTag();
                contentMode = new_name.getModel_name();
                url = new_name.getUrl();
                title = new_name.getTitle();
            } else if (view.getTag(R.drawable.launcher_selector) instanceof Carousel) {
                Carousel new_name = (Carousel) view.getTag(R.drawable.launcher_selector);
                contentMode = new_name.getModel_name();
                url = new_name.getUrl();
                title = new_name.getTitle();
            }
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (url == null) {
                intent.putExtra("title", channelEntity.getName());
                intent.putExtra("url",
                        channelEntity.getUrl());
                intent.putExtra("channel", channelEntity.getChannel());
                intent.putExtra("portraitflag", channelEntity.getSytle());
                intent.setClassName("tv.ismar.daisy",
                        "tv.ismar.daisy.ChannelListActivity");
                context.startActivity(intent);
            } else {
                if ("item".equals(contentMode)) {
                    intent.setClassName("tv.ismar.daisy",
                            "tv.ismar.daisy.ItemDetailActivity");
                    intent.putExtra("url", url);
                    context.startActivity(intent);
                } else if ("topic".equals(contentMode)) {
                    intent.putExtra("url",
                            url);
                    intent.setClassName("tv.ismar.daisy",
                            "tv.ismar.daisy.TopicActivity");
                    context.startActivity(intent);
                } else if ("section".equals(contentMode)) {
                    intent.putExtra("title", title);
                    intent.putExtra("itemlistUrl",
                            url);
                    intent.putExtra("lableString",
                            title);
                    intent.setClassName("tv.ismar.daisy",
                            "tv.ismar.daisy.PackageListDetailActivity");
                    context.startActivity(intent);
                } else if ("package".equals(contentMode)) {

                }
            }
        }
    };


    @Override
    public void change(int position) {
        Log.d(TAG, "changed position: " + position);
        for (int i = 0; i < allItem.size(); i++) {
            ImageView imageView = allItem.get(i);
            if (position != i) {
                if (imageView.getAlpha() == 1) {
                    imageView.setAlpha((float) 0.5);
                }
            } else {
                imageView.setAlpha((float) 1);
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

