package tv.ismar.daisy.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;
import com.activeandroid.query.Select;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import tv.ismar.daisy.R;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.client.DownloadClient;
import tv.ismar.daisy.core.client.DownloadThreadPool;
import tv.ismar.daisy.core.client.IsmartvUrlClient;
import tv.ismar.daisy.data.HomePagerEntity;
import tv.ismar.daisy.data.HomePagerEntity.Carousel;
import tv.ismar.daisy.data.HomePagerEntity.Poster;
import tv.ismar.daisy.data.table.DownloadTable;
import tv.ismar.daisy.ui.ItemViewFocusChangeListener;
import tv.ismar.daisy.ui.widget.DaisyViewContainer;
import tv.ismar.daisy.utils.HardwareUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by huaijie on 5/18/15.
 */
public class GuideFragment extends ChannelBaseFragment implements Flag.ChangeCallback {
    private String TAG = "GuideFragment";
    private DaisyViewContainer guideRecommmendList;
    private DaisyViewContainer carouselLayout;
    private VideoView linkedVideoView;

    private int itemViewBoundaryMargin;


    private ArrayList<String> allVideoUrl;
    private ArrayList<ImageView> allItem;

    private ArrayList<Carousel> carousels;
    private Flag flag;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = LayoutInflater.from(context).inflate(R.layout.fragment_guide, null);
        guideRecommmendList = (DaisyViewContainer) mView.findViewById(R.id.recommend_list);
        carouselLayout = (DaisyViewContainer) mView.findViewById(R.id.carousel_layout);
        linkedVideoView = (VideoView) mView.findViewById(R.id.linked_video);
        itemViewBoundaryMargin = (int) context.getResources().getDimension(R.dimen.item_boundary_margin);
        flag = new Flag(this);


        linkedVideoView.setOnClickListener(new View.OnClickListener() {
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
        });

        return mView;
    }


    @Override
    public void onResume() {
        super.onResume();
        fetchHomePage();
    }

    public void fetchHomePage() {
        String api = SimpleRestClient.root_url + "/api/tv/homepage/top/";
        new IsmartvUrlClient().doRequest(api, new IsmartvUrlClient.CallBack() {
            @Override
            public void onSuccess(String result) {
                HomePagerEntity homePagerEntity = new Gson().fromJson(result, HomePagerEntity.class);
                ArrayList<HomePagerEntity.Carousel> carousels = homePagerEntity.getCarousels();
                ArrayList<HomePagerEntity.Poster> posters = homePagerEntity.getPosters();
                if (!carousels.isEmpty()) {
                    initCarousel(carousels);
                }

                if (!posters.isEmpty()) {
                    initPosters(posters);
                }
            }

            @Override
            public void onFailed(Exception exception) {
                Log.e(TAG, exception.getMessage());
            }
        });
    }

    private void initPosters(ArrayList<HomePagerEntity.Poster> posters) {
        guideRecommmendList.removeAllViews();
        ArrayList<FrameLayout> imageViews = new ArrayList<FrameLayout>();
        for (int i = 0; i < 8; i++) {
            FrameLayout frameLayout = (FrameLayout) LayoutInflater.from(context).inflate(R.layout.item_poster, null);
            ImageView itemView = (ImageView) frameLayout.findViewById(R.id.poster_image);
            TextView textView = (TextView) frameLayout.findViewById(R.id.poster_title);
            if (StringUtils.isNotEmpty(posters.get(i).getIntroduction())) {
                textView.setText(posters.get(i).getIntroduction());
                textView.setVisibility(View.VISIBLE);
            }
            itemView.setOnClickListener(ItemClickListener);
            Picasso.with(context).load(posters.get(i).getCustom_image()).into(itemView);
            itemView.setTag(posters.get(i));
//            itemView.setOnFocusChangeListener(new ItemViewFocusChangeListener());
            imageViews.add(frameLayout);
        }
        guideRecommmendList.addAllViews(imageViews);

    }

    private void initCarousel(final ArrayList<HomePagerEntity.Carousel> carousels) {
        this.carousels = carousels;
        String tag = "guide";
        deleteFile(carousels, tag);
        downloadVideo(carousels, tag);

        final MediaPlayer.OnCompletionListener loopAllListener = new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (flag.getPosition() + 1 >= allVideoUrl.size()) {
                    flag.setPosition(0);
                } else {
                    flag.setPosition(flag.getPosition() + 1);
                }
                setVideoPath(linkedVideoView, allVideoUrl.get(flag.getPosition()));
            }
        };

        final MediaPlayer.OnCompletionListener loopCurrentListener = new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                setVideoPath(linkedVideoView, allVideoUrl.get(flag.getPosition()));
            }
        };


        allItem = new ArrayList<ImageView>();
        allVideoUrl = new ArrayList<String>();

        carouselLayout.removeAllViews();

        ArrayList<FrameLayout> arrayList = new ArrayList<FrameLayout>();
        for (int i = 0; i < 3; i++) {
            allVideoUrl.add(carousels.get(i).getVideo_url());
            FrameLayout frameLayout = (FrameLayout) LayoutInflater.from(context).inflate(R.layout.item_loop_imageview, null);
            ImageView itemView = (ImageView) frameLayout.findViewById(R.id.loop_image);
            itemView.setBackgroundResource(R.drawable.launcher_selector);
            Picasso.with(context).load(carousels.get(i).getThumb_image()).into(itemView);
            itemView.setTag(i);
            itemView.setOnClickListener(ItemClickListener);
            allItem.add(itemView);
            arrayList.add(frameLayout);
        }
        carouselLayout.addAllViews(arrayList);


        View.OnFocusChangeListener itemFocusChangeListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                boolean focusFlag = true;
                for (ImageView imageView : allItem) {
                    focusFlag = focusFlag && (!imageView.isFocused());
                }

                //all view not focus
                if (focusFlag) {
                    linkedVideoView.setOnCompletionListener(loopAllListener);
                } else {
                    flag.setPosition((Integer) v.getTag());
                    linkedVideoView.setOnCompletionListener(loopCurrentListener);
                    setVideoPath(linkedVideoView, allVideoUrl.get(flag.getPosition()));
                }

            }
        };

        for (ImageView imageView : allItem) {
            imageView.setOnFocusChangeListener(itemFocusChangeListener);
        }

        flag.setPosition(0);
        linkedVideoView.setOnCompletionListener(loopAllListener);
        View view = getView();
        if (view != null) {
            view.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setVideoPath(linkedVideoView, carousels.get(flag.getPosition()).getVideo_url());
                }
            }, 1000);
        }
    }

    private void createCarouselView(ArrayList<Carousel> carousels) {

    }


    private void setVideoPath(VideoView videoView, String url) {
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
        videoView.setVideoPath(playPath);
        videoView.start();
    }

    private void downloadVideo(ArrayList<Carousel> carousels, String tag) {
        for (Carousel carousel : carousels) {
            String url = carousel.getVideo_url();
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


    private void download(String url, String tag) {
        String savePath = HardwareUtils.getCachePath(context) + "/" + tag + "/";
        DownloadClient downloadClient = new DownloadClient(url, savePath);
        DownloadThreadPool.getInstance().add(downloadClient);
    }

    private void deleteFile(ArrayList<HomePagerEntity.Carousel> carousels, String tag) {
        String savePath = HardwareUtils.getCachePath(context) + "/" + tag + "/";
        ArrayList<String> exceptsPaths = new ArrayList<String>();
        for (HomePagerEntity.Carousel carousel : carousels) {
            try {
                File file = new File(new URL(carousel.getVideo_url()).getFile());
                exceptsPaths.add(file.getName());
            } catch (MalformedURLException e) {
                Log.e(TAG, e.getMessage());
            }

        }
        HardwareUtils.deleteFiles(savePath, exceptsPaths);
    }

    @Override
    public void change(int position) {
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
}

class Flag {

    private ChangeCallback changeCallback;

    public Flag(ChangeCallback changeCallback) {
        this.changeCallback = changeCallback;
    }

    private int position;


    public void setPosition(int position) {
        this.position = position;
        changeCallback.change(position);

    }

    public int getPosition() {
        return position;
    }

    public interface ChangeCallback {
        void change(int position);
    }
}


