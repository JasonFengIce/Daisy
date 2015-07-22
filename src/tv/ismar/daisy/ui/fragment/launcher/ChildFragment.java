package tv.ismar.daisy.ui.fragment.launcher;

import java.util.ArrayList;

import tv.ismar.daisy.R;
import tv.ismar.daisy.core.client.IsmartvUrlClient;
import tv.ismar.daisy.data.HomePagerEntity;
import tv.ismar.daisy.ui.fragment.ChannelBaseFragment;
import tv.ismar.daisy.ui.widget.child.ChildThumbImageView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

/**
 * Created by huaijie on 5/18/15.
 */
public class ChildFragment extends ChannelBaseFragment implements Flag.ChangeCallback {
    private static final String TAG = "ChildFragment";

    private LinearLayout leftLayout;
    private LinearLayout bottomLayout;
    private LinearLayout rightLayout;
    private ImageView imageSwitcher;
    private ChildThumbImageView[] indicatorImgs;
    private TextView indicatorTitle;

    private boolean focusFlag = true;

    private Flag flag;

    private ArrayList<HomePagerEntity.Carousel> carousels;

    private MessageHandler messageHandler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_child, null);
        leftLayout = (LinearLayout) mView.findViewById(R.id.left_layout);
        bottomLayout = (LinearLayout) mView.findViewById(R.id.bottom_layout);
        rightLayout = (LinearLayout) mView.findViewById(R.id.right_layout);
        imageSwitcher = (ImageView) mView.findViewById(R.id.image_switcher);
        indicatorImgs = new ChildThumbImageView[]{
                (ChildThumbImageView) mView.findViewById(R.id.indicator_1),
                (ChildThumbImageView) mView.findViewById(R.id.indicator_2),
                (ChildThumbImageView) mView.findViewById(R.id.indicator_3)
        };
        indicatorTitle = (TextView) mView.findViewById(R.id.indicator_title);

        flag = new Flag(this);
        messageHandler = new MessageHandler();
        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        fetchChild(channelEntity.getHomepage_url());
    }

    private void fetchChild(String url) {
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
        int marginLR = (int) context.getResources().getDimension(R.dimen.child_fragment_item_margin_lr);
        int marginTP = (int) context.getResources().getDimension(R.dimen.child_fragment_item_margin_tp);
        Log.d(TAG, "margin lr: " + marginLR);

        for (int i = 0; i < 7; i++) {
            View itemContainer = LayoutInflater.from(context).inflate(R.layout.item_comic_fragment, null);
            itemContainer.setBackgroundResource(R.drawable.selector_child_item);
            itemContainer.setFocusable(true);
            itemContainer.setFocusableInTouchMode(true);
            itemContainer.setClickable(true);
            itemContainer.setOnClickListener(ItemClickListener);
            itemContainer.setTag(posters);

            ImageView itemImg = (ImageView) itemContainer.findViewById(R.id.item_img);
            TextView itemText = (TextView) itemContainer.findViewById(R.id.item_title);

            Picasso.with(context).load(posters.get(i).getCustom_image()).into(itemImg);
            itemText.setText(posters.get(i).getTitle());
            if (i >= 0 && i < 3) {
                LinearLayout.LayoutParams verticalParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
                verticalParams.weight = 1;
                if (i == 2) {
                    verticalParams.setMargins(marginLR, marginTP, 0, marginTP);
                } else {
                    verticalParams.setMargins(marginLR, marginTP, 0, 0);
                }
                itemContainer.setLayoutParams(verticalParams);
                leftLayout.addView(itemContainer);
            }


            if (i >= 3 && i < 5) {
                LinearLayout.LayoutParams horizontalParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
                horizontalParams.weight = 1;
                if (i == 3) {
                    horizontalParams.setMargins(0, marginTP, (int) context.getResources().getDimension(R.dimen.child_fragment_bottomlayout_margin), 0);
                } else if (i == 4) {
                    horizontalParams.setMargins(0, marginTP, 0, 0);
                }

                itemContainer.setLayoutParams(horizontalParams);
                bottomLayout.addView(itemContainer);
            }


            if (i >= 5 && i < 7) {
                LinearLayout.LayoutParams verticalParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
                verticalParams.weight = 1;

                verticalParams.setMargins(0, marginTP, marginLR, 0);
                itemContainer.setLayoutParams(verticalParams);
                rightLayout.addView(itemContainer);
            }
        }
        ImageView imageView = new ImageView(context);
        imageView.setFocusable(true);
        imageView.setFocusableInTouchMode(true);
        imageView.setClickable(true);
        imageView.setImageResource(R.drawable.selector_child_more);
        imageView.setOnClickListener(ItemClickListener);
        LinearLayout.LayoutParams verticalParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        verticalParams.weight = 1;
        verticalParams.setMargins(0, 0, marginLR, marginTP);
        imageView.setLayoutParams(verticalParams);
        rightLayout.addView(imageView);
        rightLayout.requestLayout();
    }

    private void initCarousel(ArrayList<HomePagerEntity.Carousel> carousels) {

        this.carousels = carousels;
//

        View.OnFocusChangeListener itemFocusChangeListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                focusFlag = true;
                for (ChildThumbImageView imageView : indicatorImgs) {
                    focusFlag = focusFlag && (!imageView.isFocused());
                }

                if (hasFocus) {
                    int position = (Integer) v.getTag();
                    flag.setPosition(position);
                    playCarousel();
                }
            }
        };


        for (int i = 0; i < 3; i++) {
            indicatorImgs[i].setTag(i);
            indicatorImgs[i].setOnFocusChangeListener(itemFocusChangeListener);
            indicatorImgs[i].setOnClickListener(ItemClickListener);
            indicatorImgs[i].setTag(R.drawable.launcher_selector, carousels.get(i));
            Picasso.with(context).load(carousels.get(i).getThumb_image()).into(indicatorImgs[i]);
        }

        flag.setPosition(0);
        playCarousel();

    }

    private void playCarousel() {
        messageHandler.removeMessages(0);
        Picasso.with(context).load(carousels.get(flag.getPosition()).getVideo_image()).into(imageSwitcher, new Callback() {
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

    @Override
    public void change(int position) {
        for (int i = 0; i < indicatorImgs.length; i++) {
            ChildThumbImageView imageView = indicatorImgs[i];
            if (position != i) {
                if (imageView.getAlpha() == 1) {
                    imageView.zoomNormalImage();
//                    imageView.setAlpha((float) 0.5);


                }
            } else {
                imageView.zoomInImage();
                imageView.setAlpha((float) 1);
                indicatorTitle.setText(carousels.get(flag.getPosition()).getTitle());

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

}


