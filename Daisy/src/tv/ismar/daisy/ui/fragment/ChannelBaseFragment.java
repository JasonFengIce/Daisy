package tv.ismar.daisy.ui.fragment;

import java.io.InputStream;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import tv.ismar.daisy.R;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.data.ChannelEntity;
import tv.ismar.daisy.data.HomePagerEntity.Carousel;
import tv.ismar.daisy.data.HomePagerEntity.Poster;
import tv.ismar.daisy.player.CallaPlay;
import tv.ismar.daisy.player.InitPlayerTool;
import tv.ismar.daisy.ui.activity.TVGuideActivity;

public class ChannelBaseFragment extends Fragment {
    protected ChannelEntity channelEntity;
    protected TVGuideActivity mContext;
    protected boolean scrollFromBorder;
//    protected PlaybackService mService;

    protected View mLeftTopView;
    protected View mLeftBottomView;
    protected View mRightTopView;
    protected View mRightBottomView;
    protected boolean isRight;
    protected String bottomFlag;
    protected Animation scaleSmallAnimation;
    protected Animation scaleBigAnimation;


    
    public void setRight(boolean isRight) {
		this.isRight = isRight;
	}

	public void setBottomFlag(String bottomFlag) {
		this.bottomFlag = bottomFlag;
	}

	public ChannelEntity getChannelEntity() {
        return channelEntity;
    }

    public void setScrollFromBorder(boolean scrollFromBorder) {
		this.scrollFromBorder = scrollFromBorder;
	}

	public void setChannelEntity(ChannelEntity channelEntity) {
        this.channelEntity = channelEntity;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = (TVGuideActivity) activity;
		if (scaleSmallAnimation == null) {
			scaleSmallAnimation = AnimationUtils.loadAnimation(mContext, R.anim.anim_scale_small);
		}
		if (scaleBigAnimation == null) {
			scaleBigAnimation = AnimationUtils.loadAnimation(mContext, R.anim.anim_scale_big);
		}
//        mService = mContext.getService();

    }

    @Override
    public void onDetach() {
        mContext = null;
        mLeftTopView = null;
        mLeftBottomView = null;
        mRightTopView = null;
        mRightBottomView = null;
        ItemClickListener = null;
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (null != channelEntity
                && !TextUtils.isEmpty(channelEntity.getChannel()))
            mContext.channelRequestFocus(channelEntity.getChannel());
    }

    protected View.OnClickListener ItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String url = null;
            String contentMode = null;
            String title = null;
            String mode_name = null;
            String channel = "";
            String type;
            int pk;
            boolean expense = false;
            int position = -1;
            Intent intent = new Intent();
            if (view.getTag() instanceof Poster) {
                Poster new_name = (Poster) view.getTag();
                contentMode = new_name.getContent_model();
                url = new_name.getUrl();
                title = new_name.getTitle();
                mode_name = new_name.getModel_name();
                expense = new_name.isExpense();
                position = new_name.getPosition();
                intent.putExtra("fromPage", "tvhome");
            } else if (view.getTag(R.drawable.launcher_selector) instanceof Carousel) {
                Carousel new_name = (Carousel) view
                        .getTag(R.drawable.launcher_selector);
                contentMode = new_name.getContent_model();
                url = new_name.getUrl();
                title = new_name.getTitle();
                mode_name = new_name.getModel_name();
                expense = new_name.isExpense();
                position = new_name.getPosition();
                intent.putExtra("fromPage", "live");
            }
            type = mode_name;
            
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (url == null) {
                intent.setAction("tv.ismar.daisy.Channel");
                title = channelEntity.getName();
                channel = "列表";
                pk = SimpleRestClient.getItemId(channelEntity.getUrl(), new boolean[1]);

                intent.putExtra("title", channelEntity.getName());
                intent.putExtra("url", channelEntity.getUrl());
                intent.putExtra("channel", channelEntity.getChannel());
                intent.putExtra("portraitflag", channelEntity.getStyle());

                mContext.startActivity(intent);
            } else {

                pk = SimpleRestClient.getItemId(url, new boolean[1]);
                if ("item".equals(mode_name)) {
                    //DaisyUtils.gotoSpecialPage(mContext,contentMode,url);


                    if (("variety".equals(contentMode) || "entertainment".equals(contentMode)) && !expense) {
                        channel = "娱乐综艺";
                        intent.setAction("tv.ismar.daisy.EntertainmentItem");
                        intent.putExtra("title", "娱乐综艺");
                    } else if ("movie".equals(contentMode)) {
                        channel = "电影";
                        intent.setAction("tv.ismar.daisy.PFileItem");
                        intent.putExtra("title", "电影");
                    } else if ("package".equals(contentMode)) {
                        channel = "礼包详情";
                        intent.setAction("tv.ismar.daisy.packageitem");
                        intent.putExtra("title", "礼包详情");
                    } else {
                        channel = "详情";
                        intent.setClassName("tv.ismar.daisy",
                                "tv.ismar.daisy.ItemDetailActivity");
                    }
                    intent.putExtra("url", url);
                    mContext.startActivity(intent);


                } else if ("topic".equals(mode_name)) {
                    channel = "专题";
                    intent.putExtra("url", url);
                    intent.setAction("tv.ismar.daisy.Topic");
                    mContext.startActivity(intent);
                } else if ("section".equals(mode_name)) {
                    channel = "礼包列表";
                    intent.putExtra("title", title);
                    intent.putExtra("itemlistUrl", url);
                    intent.putExtra("lableString", title);
                    intent.setAction("tv.ismar.daisy.packagelist");
                    mContext.startActivity(intent);
                } else if ("package".equals(mode_name)) {
                    channel = "礼包详情";
                    intent.setAction("tv.ismar.daisy.packageitem");
                    intent.putExtra("url", url);
                    mContext.startActivity(intent);
                } else if ("clip".equals(mode_name)) {
                    channel = "播放";
                    InitPlayerTool tool = new InitPlayerTool(mContext);
                    tool.initClipInfo(url, InitPlayerTool.FLAG_URL);
                }
            }
            CallaPlay play = new CallaPlay();
            play.homepage_vod_click(pk, title, channel, position, type);
        }
    };
    
    protected void setbackground(final View rootView,int id) {


        BitmapFactory.Options opt = new BitmapFactory.Options();

        opt.inPreferredConfig = Bitmap.Config.ALPHA_8;

        opt.inPurgeable = true;

        opt.inInputShareable = true;
        opt.inTargetDensity = getResources().getDisplayMetrics().densityDpi;
        opt.inDensity = getResources().getDisplayMetrics().densityDpi;

        InputStream is = getResources().openRawResource(

                id);

        Bitmap bm = BitmapFactory.decodeStream(is, null, opt);

        BitmapDrawable bd = new BitmapDrawable(getResources(), bm);
        rootView.setBackgroundDrawable(bd);
    }

}
