package tv.ismar.daisy;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnHoverListener;
import android.widget.*;
import com.google.gson.JsonSyntaxException;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.EventProperty;
import tv.ismar.daisy.core.NetworkUtils;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.VodUserAgent;
import tv.ismar.daisy.exception.ItemOfflineException;
import tv.ismar.daisy.exception.NetworkException;
import tv.ismar.daisy.models.*;
import tv.ismar.daisy.player.InitPlayerTool;
import tv.ismar.daisy.ui.activity.newvip.PayActivity;
import tv.ismar.daisy.ui.activity.newvip.PayLayerVipActivity;
import tv.ismar.daisy.ui.widget.LaunchHeaderLayout;
import tv.ismar.daisy.utils.BitmapDecoder;
import tv.ismar.daisy.utils.Util;
import tv.ismar.daisy.views.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by zhangjiqiang on 15-6-30.
 */
public class PFilmItemdetailActivity extends BaseActivity implements AsyncImageView.OnImageViewLoadListener {
    private Item mItem;
    private String title;
    private SimpleRestClient mSimpleRestClient;
    private LoadingDialog mLoadingDialog;
    private GetItemTask mGetItemTask;
    private TextView mDetailTitle;
    private TextView mDetailIntro;
    private AsyncImageView mDetailPreviewImg;
    private DetailAttributeContainer mDetailAttributeContainer;
    private ContentModel mContentModel;
    private boolean isDrama = false;
    private LinearLayout related_video_container;
    private Item[] mRelatedItem;
    private GetRelatedTask mGetRelatedTask;
    private HashMap<String, Object> mDataCollectionProperties = new HashMap<String, Object>();
    private HashMap<AsyncImageView, Boolean> mLoadingImageQueue = new HashMap<AsyncImageView, Boolean>();
    private Button mLeftBtn;
    private Button mMiddleBtn;
    private Button mRightBtn;
    private final String COLLECT_VIDEO = "collect";
    private final String BUY_VIDEO = "buy";
    private final String PREVIEW_VIDEO = "preview";
    private final String PLAY_VIDEO = "play";
    private final String DRAMA_VIDEO = "drama";
    private boolean isBuy = false;
    private int remainDay = -1;
    private String identify = "";
    private Button mCollectBtn;
    private boolean isInitialized = false;
    private History mHistory;
    private LinearLayout mMoreContent;
    private TextView detail_price_txt;
    private TextView detail_duration_txt;
    //    private TextView bean_score;
    private RotateTextView detail_tag_txt;
    private TextView detail_permission_txt;
    private ImageView source;
    private View top_view_layout;
    private View bottom_view_layout;
    private String channel;
    private String slug;
    private String fromPage;
    private LaunchHeaderLayout weatherFragment;
    private BitmapDecoder bitmapDecoder;
    private InitPlayerTool tool;
    private boolean isneedpause = true;
    private String toDate;
    String iqiyi_code = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.filmitem_portrait_detail_view);
        mSimpleRestClient = new SimpleRestClient();
        final View vv = findViewById(R.id.large_layout);
        bitmapDecoder = new BitmapDecoder();
        bitmapDecoder.decode(this, R.drawable.main_bg, new BitmapDecoder.Callback() {
            @Override
            public void onSuccess(BitmapDrawable bitmapDrawable) {
                vv.setBackgroundDrawable(bitmapDrawable);
            }
        });

        mLoadingDialog = new LoadingDialog(this, getResources().getString(
                R.string.vod_loading));
        mLoadingDialog.setOnCancelListener(mLoadingCancelListener);
        mLoadingDialog.show();

        initViews();
        Intent intent = getIntent();
        if (intent != null) {
            channel = intent.getStringExtra("channel");
            slug = intent.getStringExtra(EventProperty.SECTION);
            fromPage = intent.getStringExtra("fromPage");
            if (intent.getSerializableExtra("item") != null) {
                mItem = (Item) intent.getSerializableExtra("item");
                if (mItem != null) {
                    try {
                        // initLayout();
                        if (!isFree())
                            isbuy();
                        else
                            initLayout();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                String url = intent.getStringExtra("url");
                // mSection = intent.getStringExtra(EventProperty.SECTION);
                if (url == null) {
                    url = SimpleRestClient.sRoot_url + "/api/item/96538/";
                }
                mGetItemTask = new GetItemTask();
                mGetItemTask.execute(url);
            }
        }

        DaisyUtils.getVodApplication(this).addActivityToPool(this.toString(),
                this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        setContentView(R.layout.filmitem_portrait_detail_view);
        mSimpleRestClient = new SimpleRestClient();
        final View vv = findViewById(R.id.large_layout);
        bitmapDecoder = new BitmapDecoder();
        bitmapDecoder.decode(this, R.drawable.main_bg, new BitmapDecoder.Callback() {
            @Override
            public void onSuccess(BitmapDrawable bitmapDrawable) {
                vv.setBackgroundDrawable(bitmapDrawable);
            }
        });

        mLoadingDialog = new LoadingDialog(this, getResources().getString(
                R.string.vod_loading));
        mLoadingDialog.setOnCancelListener(mLoadingCancelListener);
        mLoadingDialog.show();

        initViews();
        if (intent != null) {
            channel = intent.getStringExtra("channel");
            slug = intent.getStringExtra(EventProperty.SECTION);
            fromPage = intent.getStringExtra("fromPage");
            if (intent.getSerializableExtra("item") != null) {
                mItem = (Item) intent.getSerializableExtra("item");
                if (mItem != null) {
                    try {
                        // initLayout();
                        if (!isFree())
                            isbuy();
                        else
                            initLayout();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                String url = intent.getStringExtra("url");
                // mSection = intent.getStringExtra(EventProperty.SECTION);
                if (url == null) {
                    url = SimpleRestClient.sRoot_url + "/api/item/96538/";
                }
                mGetItemTask = new GetItemTask();
                mGetItemTask.execute(url);
            }
        }

        DaisyUtils.getVodApplication(this).addActivityToPool(this.toString(),
                this);
    }

    @Override
    protected void onResume() {
        iqiyi_code = null;
        if (isInitialized) {
            if (isDrama()) {
                String url = mItem.item_url == null ? mSimpleRestClient.root_url
                        + "/api/item/" + mItem.pk + "/"
                        : mItem.item_url;
                mHistory = DaisyUtils.getHistoryManager(this).getHistoryByUrl(
                        url, "no");
            }
        }
        for (HashMap.Entry<AsyncImageView, Boolean> entry : mLoadingImageQueue
                .entrySet()) {
            if (!entry.getValue()) {
                entry.getKey().setPaused(true);
                entry.setValue(true);
            }
        }
        if (isPause && mCollectBtn != null) {
            if (isFavorite()) {
                //mCollectBtn.setBackgroundResource(R.drawable.collected_btn_bg_selector);
                mCollectBtn.setText(getResources().getString(R.string.favorited));
            } else {
                //mCollectBtn.setBackgroundResource(R.drawable.collect_btn_bg_selector);
                mCollectBtn.setText(getResources().getString(R.string.favorite));
            }
            isPause = false;
        }


        // TODO GAME 模拟游戏自动登录，这里需要游戏添加加载动画
        // WGLogin是一个异步接口, 传入ePlatform_None则调用本地票据验证票据是否有效
        // 如果从未登录过，则会立即在onLoginNotify中返回flag为eFlag_Local_Invalid，此时应该拉起授权界面
        // 建议在此时机调用WGLogin,它应该在handlecallback之后进行调用。
        isneedpause = true;
        super.onResume();
    }


    private boolean isFavorite() {
        if (mItem != null) {
            String url = mItem.item_url;
            if (url == null && mItem.pk != 0) {
                url = SimpleRestClient.sRoot_url + "/api/item/" + mItem.pk
                        + "/";
            }
            Favorite favorite = null;
            if (!SimpleRestClient.isLogin()) {
                favorite = DaisyUtils.getFavoriteManager(this)
                        .getFavoriteByUrl(url, "no");
            } else {
                favorite = DaisyUtils.getFavoriteManager(this)
                        .getFavoriteByUrl(url, "yes");
            }
            if (favorite != null) {
                return true;
            }
        }

        return false;
    }

    private void showToast(String text) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.simple_toast,
                null);
        TextView toastText = (TextView) layout.findViewById(R.id.toast_text);
        toastText.setText(text);
        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

    private void deleteFavoriteByNet() {
        mSimpleRestClient.doSendRequest("/api/bookmarks/remove/", "post", "access_token=" +
                SimpleRestClient.access_token + "&device_token=" + SimpleRestClient.device_token + "&item=" + mItem.pk, new SimpleRestClient.HttpPostRequestInterface() {

            @Override
            public void onSuccess(String info) {
                // TODO Auto-generated method stub
                if ("200".equals(info)) {
                    //mCollectBtn.setBackgroundResource(R.drawable.collect_btn_bg_selector);
//					showToast(getResources().getString(
//							R.string.vod_bookmark_remove_success));
                    mCollectBtn.setText(getResources().getString(R.string.favorite));
                }
            }

            @Override
            public void onPrepare() {
                // TODO Auto-generated method stub

            }

            @Override
            public void onFailed(String error) {
                // TODO Auto-generated method stub
                //mCollectBtn.setBackgroundResource(R.drawable.collected_btn_bg_selector);
                mCollectBtn.setText(getResources().getString(R.string.favorited));
//				showToast(getResources().getString(
//						R.string.vod_bookmark_remove_unsuccess));
            }
        });
    }

    private void addFavorite() {
        if (isFavorite()) {
            String url = SimpleRestClient.sRoot_url + "/api/item/" + mItem.pk
                    + "/";
            String isnet = "";
            if (SimpleRestClient.isLogin()) {
                isnet = "yes";
                deleteFavoriteByNet();
            } else {
                isnet = "no";
            }
            DaisyUtils.getFavoriteManager(PFilmItemdetailActivity.this)
                    .deleteFavoriteByUrl(url, isnet);
            showToast(getResources().getString(
                    R.string.vod_bookmark_remove_success));
        } else {
            String url = SimpleRestClient.sRoot_url + "/api/item/" + mItem.pk
                    + "/";
            Favorite favorite = new Favorite();
            favorite.title = mItem.title;
            favorite.adlet_url = mItem.adlet_url;
            favorite.content_model = mItem.content_model;
            favorite.url = url;
            favorite.quality = mItem.quality;
            favorite.is_complex = mItem.is_complex;
            if(mItem.expense!=null) {
                favorite.cpid = mItem.expense.cpid;
                favorite.cpname=mItem.expense.cpname;
                favorite.cptitle=mItem.expense.cptitle;
                favorite.paytype=mItem.expense.pay_type;
            }
            if (SimpleRestClient.isLogin()) {
                favorite.isnet = "yes";
                createFavoriteByNet();
            } else {
                favorite.isnet = "no";
            }
            DaisyUtils.getFavoriteManager(PFilmItemdetailActivity.this).addFavorite(
                    favorite, favorite.isnet);
            showToast(getResources().getString(
                    R.string.vod_bookmark_add_success));
        }
    }

    private void createFavoriteByNet() {
        mSimpleRestClient.doSendRequest("/api/bookmarks/create/", "post", "access_token=" + SimpleRestClient.access_token + "&device_token=" + SimpleRestClient.device_token + "&item=" + mItem.pk, new SimpleRestClient.HttpPostRequestInterface() {

            @Override
            public void onSuccess(String info) {
                // TODO Auto-generated method stub
                //mCollectBtn.setBackgroundResource(R.drawable.collected_btn_bg_selector);
                mCollectBtn.setText(getResources().getString(R.string.favorited));
//				showToast(getResources().getString(
//						R.string.vod_bookmark_add_success));
            }

            @Override
            public void onPrepare() {
                // TODO Auto-generated method stub

            }

            @Override
            public void onFailed(String error) {
                // TODO Auto-generated method stub
                //mCollectBtn.setBackgroundResource(R.drawable.collect_btn_bg_selector);
                mCollectBtn.setText(getResources().getString(R.string.favorite));
//				showToast(getResources().getString(
//						R.string.vod_bookmark_add_unsuccess));
            }
        });
    }

    private PaymentDialog.OrderResultListener ordercheckListener = new PaymentDialog.OrderResultListener() {

        @Override
        public void payResult(boolean result) {
            if (result) {
                isBuy = true;
                setExpenseStatus();
            }
//            mDetailAttributeContainer.removeAllViews();
//            isbuy();
        }

    };

//    private void setExpenseStatus() {
//        /*
//		 * if this item is a drama , the button should split to two. otherwise.
//		 * use one button.
//		 */
//        if (isFree()) {
//            // 免费
//            if (!isDrama()) {
//                // 电影
//                mRightBtn.setVisibility(View.GONE);
//            } else {
//                // 电视剧
//                mRightBtn.setTag(DRAMA_VIDEO);
//                mRightBtn.setText(getResources().getString(R.string.vod_itemepisode));
//            }
//            mLeftBtn.setText(getResources().getString(R.string.play));
//            mMiddleBtn.setText(getResources().getString(R.string.favorite));
//            mLeftBtn.setTag(PLAY_VIDEO);
//            mMiddleBtn.setTag(COLLECT_VIDEO);
//            mCollectBtn = mMiddleBtn;
//        } else {
//            // 收费
//            if (!isBuy) {
//                // 未购买
//                mLeftBtn.setTag(PREVIEW_VIDEO);
//                mLeftBtn.setText(getResources().getString(R.string.preview_video));
//                mMiddleBtn.setTag(BUY_VIDEO);
//                mMiddleBtn.setText(getResources().getString(R.string.buy_video));
//                mRightBtn.setText(getResources().getString(R.string.favorite));
//                mRightBtn.setTag(COLLECT_VIDEO);
//
//                detail_price_txt.setText("￥" + mItem.expense.price);
//                detail_duration_txt.setText("有效期" + mItem.expense.duration
//                        + "天");
//                detail_price_txt.setVisibility(View.VISIBLE);
//                detail_duration_txt.setVisibility(View.VISIBLE);
//                remainDay = mItem.expense.duration;
//                mCollectBtn = mRightBtn;
//            } else {
//                // 已经购买
//                //isbuy_label.setVisibility(View.VISIBLE);
//                //mDetailQualityLabel.setVisibility(View.GONE);
//                if (!isDrama()) {
//                    // 电影
//                    mRightBtn.setVisibility(View.GONE);
//                    // mRightBtn.setBackgroundResource(R.drawable.collect_btn_bg_selector);
//                    // mRightBtn.setTag(COLLECT_VIDEO);
//                } else {
//                    // 电视剧
//                    mRightBtn.setText(getResources().getString(R.string.vod_itemepisode));
//                    mRightBtn.setTag(DRAMA_VIDEO);
//                }
//                mLeftBtn.setTag(PLAY_VIDEO);
//                mLeftBtn.setText(getResources().getString(R.string.play));
//                mMiddleBtn.setText(getResources().getString(R.string.favorite));
//                mMiddleBtn.setTag(COLLECT_VIDEO);
//
//                detail_price_txt.setText("已付费");
//                detail_duration_txt.setText("剩余" + remainDay + "天");
//                detail_price_txt.setVisibility(View.VISIBLE);
//                detail_duration_txt.setVisibility(View.VISIBLE);
//                detail_duration_txt
//                        .setBackgroundResource(R.drawable.vod_detail_already_payment_duration);
//                detail_price_txt
//                        .setBackgroundResource(R.drawable.vod_detail_already_payment_price);
//                mCollectBtn = mMiddleBtn;
//            }
//        }
//
//        mLeftBtn.setFocusable(true);
//        mLeftBtn.requestFocus();
//    }

    private void setExpenseStatus() {
        /*
         * if this item is a drama , the button should split to two. otherwise.
		 * use one button.
		 */

        if (mItem.expense != null && mItem.expense.cplogo != null) {
            Picasso.with(this).load(mItem.expense.cplogo).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).into(source);
            source.setVisibility(View.VISIBLE);
        }
        if (isFree()) {
            // 免费
            if (!isDrama()) {
                // 电影
                mRightBtn.setVisibility(View.GONE);
            } else {
                // 电视剧
                //setLeftDrawable(drawableleftdrama, mRightBtn);
                mRightBtn.setTag(DRAMA_VIDEO);
                mRightBtn.setText(getResources().getString(R.string.vod_itemepisode));
                initFocusBtn(mRightBtn, false);
            }
            //setLeftDrawable(drawableleftplay, mLeftBtn);
            mLeftBtn.setText(getResources().getString(R.string.play));
            //setLeftDrawable(drawableleftcollect, mMiddleBtn);
            mMiddleBtn.setText(getResources().getString(R.string.favorite));
            mLeftBtn.setTag(PLAY_VIDEO);
            mMiddleBtn.setTag(COLLECT_VIDEO);
            initFocusBtn(mLeftBtn, false);
            initFocusBtn(mMiddleBtn, false);
            mCollectBtn = mMiddleBtn;
        } else {
            // 收费
            if (mItem.expense.cptitle != null && !"".equals(mItem.expense.cptitle)) {
                detail_tag_txt.setText(mItem.expense.cptitle);
                detail_tag_txt.setVisibility(View.VISIBLE);
                if (mItem.expense.pay_type == 1) {
                    detail_tag_txt.setBackgroundResource(R.drawable.single_buy);
                }else if((mItem.expense.cpname).startsWith("ismar")){
                    detail_tag_txt.setBackgroundResource(R.drawable.ismar);
                } else if ("iqiyi".equals(mItem.expense.cpname)) {
                    detail_tag_txt.setBackgroundResource(R.drawable.lizhi);

                }
                if (!isBuy) {
                    // 未购买
//				if (!isDrama()) {
//					// 电影
//					mLeftBtn.setBackgroundResource(R.drawable.preview_video_btn_bg_selector);
//					mLeftBtn.setTag(PREVIEW_VIDEO);
//					mMiddleBtn
//							.setBackgroundResource(R.drawable.buy_video_btn_bg_selector);
//					mMiddleBtn.setTag(BUY_VIDEO);
//					mRightBtn
//							.setBackgroundResource(R.drawable.collect_btn_bg_selector);
//					mRightBtn.setTag(COLLECT_VIDEO);
//				} else {
//					// 电视剧
//					mLeftBtn.setBackgroundResource(R.drawable.preview_video_btn_bg_selector);
//					mLeftBtn.setTag(PREVIEW_VIDEO);
//					mMiddleBtn
//							.setBackgroundResource(R.drawable.buy_video_btn_bg_selector);
//					mMiddleBtn.setTag(BUY_VIDEO);
//					mRightBtn
//							.setBackgroundResource(R.drawable.collect_btn_bg_selector);
//					mRightBtn.setTag(COLLECT_VIDEO);
//				}
                    //setLeftDrawable(drawableleftplay, mLeftBtn);
                    if (mItem.preview == null) {
                        mLeftBtn.setTag(PREVIEW_VIDEO);
                        mLeftBtn.setText(getResources().getString(R.string.preview_video));
                        mLeftBtn.setBackgroundResource(R.drawable.button_disable);
                        mLeftBtn.setEnabled(false);
                        mLeftBtn.setClickable(false);
                        mLeftBtn.setVisibility(View.INVISIBLE);
                    } else {
                        mLeftBtn.setTag(PREVIEW_VIDEO);
                        mLeftBtn.setText(getResources().getString(R.string.preview_video));
                    }
                    //setLeftDrawable(drawableleftbuy, mMiddleBtn);
                    mMiddleBtn.setTag(BUY_VIDEO);
                    mMiddleBtn.setText(getResources().getString(R.string.buy_video));
                    //setLeftDrawable(drawableleftcollect, mRightBtn);
                    mRightBtn.setText(getResources().getString(R.string.favorite));
                    mRightBtn.setTag(COLLECT_VIDEO);
                    initFocusBtn(mLeftBtn, false);
                    initFocusBtn(mRightBtn, false);
                    initFocusBtn(mMiddleBtn, false);
                    if (mItem.expense.pay_type == 3 || mItem.expense.pay_type == 0) {
                        detail_permission_txt.setVisibility(View.VISIBLE);
                        detail_duration_txt.setVisibility(View.GONE);
                        detail_price_txt.setVisibility(View.GONE);
                    } else {
                        detail_price_txt.setText("￥" + mItem.expense.price);
//                detail_duration_txt.setText("有效期" + mItem.expense.duratio);
                        detail_price_txt.setVisibility(View.VISIBLE);
                        detail_permission_txt.setVisibility(View.GONE);
                        detail_duration_txt.setVisibility(View.GONE);
//                detail_duration_txt.setVisibility(View.VISIBLE);
                    }
                    remainDay = mItem.expense.duration;
                    mCollectBtn = mRightBtn;
                } else {
                    // 已经购买
                    //isbuy_label.setVisibility(View.VISIBLE);
                    //mDetailQualityLabel.setVisibility(View.GONE);
                    if (!isDrama()) {
                        // 电影
                        mRightBtn.setVisibility(View.GONE);
                        // mRightBtn.setBackgroundResource(R.drawable.collect_btn_bg_selector);
                        // mRightBtn.setTag(COLLECT_VIDEO);
                    } else {
                        // 电视剧
//					mLeftBtn.setBackgroundResource(R.drawable.play_btn_bg_selector);
//					mLeftBtn.setTag(PLAY_VIDEO);
//					mMiddleBtn
//							.setBackgroundResource(R.drawable.collect_btn_bg_selector);
//					mMiddleBtn.setTag(COLLECT_VIDEO);

                        //setLeftDrawable(drawableleftdrama, mRightBtn);
                        mRightBtn.setText(getResources().getString(R.string.vod_itemepisode));
                        mRightBtn.setTag(DRAMA_VIDEO);
                        initFocusBtn(mRightBtn, false);
                    }

                    //setLeftDrawable(drawableleftplay, mLeftBtn);
                    if(mLeftBtn.getVisibility() == View.INVISIBLE){
                        mLeftBtn.setVisibility(View.VISIBLE);
                    }
                    mLeftBtn.setTag(PLAY_VIDEO);
                    mLeftBtn.setText(getResources().getString(R.string.play));
                    //setLeftDrawable(drawableleftcollect,mMiddleBtn);
                    mMiddleBtn.setText(getResources().getString(R.string.favorite));
                    mMiddleBtn.setTag(COLLECT_VIDEO);
                    initFocusBtn(mLeftBtn, false);
                    initFocusBtn(mMiddleBtn, false);
                    if (toDate != null) {
                        String[] todate = toDate.substring(0, toDate.indexOf(" ")).split("-");
                        detail_duration_txt.setText("有效期至" + todate[0] + "年" + todate[1] + "月" + todate[2] + "日");
                    } else {
                        Date date = new Date();
                        Log.e("DATE", date.getTime() + "");
                        date.setTime(date.getTime() + ((long) 3600 * 24 * 1000 * remainDay));
                        SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日");
                        detail_duration_txt.setText("有效期至" + format.format(date));
                    }
                    detail_duration_txt.setVisibility(View.VISIBLE);
                    detail_price_txt.setVisibility(View.GONE);
                    detail_permission_txt.setVisibility(View.GONE);
//                detail_duration_txt
//                        .setBackgroundResource(R.drawable.vod_detail_already_payment_duration);
//                detail_price_txt
//                        .setBackgroundResource(R.drawable.vod_detail_already_payment_price);
                    mCollectBtn = mMiddleBtn;
                }
            }
            if (mLeftBtn.isEnabled()) {
                mLeftBtn.setFocusable(true);
                mLeftBtn.requestFocus();
            } else {
                mMiddleBtn.setFocusable(true);
                mMiddleBtn.requestFocus();
            }
        }
    }

    private void buyVideo() {
        if(1 == mItem.expense.jump_to) {
            PaymentDialog dialog = new PaymentDialog(PFilmItemdetailActivity.this,
                    R.style.PaymentDialog, ordercheckListener);
            mItem.model_name = "item";
            dialog.setItem(mItem);
            dialog.show();
        }else if(0 == mItem.expense.jump_to){
            Intent intent = new Intent(PFilmItemdetailActivity.this, PayActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("item_id", String.valueOf(mItem.pk));
            startActivityForResult(intent, 20);
        }else if(2 == mItem.expense.jump_to){
            Intent intent = new Intent(PFilmItemdetailActivity.this, PayLayerVipActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("cpid", String.valueOf(mItem.expense.cpid));
            startActivityForResult(intent,20);
        }
    }

    private void setLeftDrawable(Drawable drawable, Button btn) {
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        btn.setCompoundDrawables(drawable, null, null, null);
    }

    private boolean isPause = false;

    @Override
    protected void onPause() {
        if (isneedpause)
            isPause = true;
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
        for (HashMap.Entry<AsyncImageView, Boolean> entry : mLoadingImageQueue
                .entrySet()) {
            if (entry.getValue()) {
                entry.getKey().setPaused(true);
                entry.setValue(false);
            }
        }

        if (mItem != null) {
            final HashMap<String, Object> properties = new HashMap<String, Object>();
            properties.putAll(mDataCollectionProperties);
            new NetworkUtils.DataCollectionTask().execute(
                    NetworkUtils.VIDEO_DETAIL_OUT, properties);
            mDataCollectionProperties.put(EventProperty.TITLE, mItem.title);
            mDataCollectionProperties.put(EventProperty.ITEM, mItem.pk);
            mDataCollectionProperties.put(EventProperty.TO, "return");
            mDataCollectionProperties.remove(EventProperty.SUBITEM);
        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (tool != null)
            tool.removeAsycCallback();
        if (bitmapDecoder != null && bitmapDecoder.isAlive()) {
            bitmapDecoder.interrupt();
        }
        if (mGetItemTask != null
                && mGetItemTask.getStatus() != AsyncTask.Status.FINISHED) {
            mGetItemTask.cancel(true);
        }
        if (mGetRelatedTask != null
                && mGetItemTask.getStatus() != AsyncTask.Status.FINISHED) {
            mGetItemTask.cancel(true);
        }

        final HashMap<AsyncImageView, Boolean> loadingImageQueue = new HashMap<AsyncImageView, Boolean>();
        loadingImageQueue.putAll(mLoadingImageQueue);
        for (HashMap.Entry<AsyncImageView, Boolean> entry : loadingImageQueue
                .entrySet()) {
            entry.getKey().stopLoading();
        }
        loadingImageQueue.clear();
        mLoadingImageQueue = null;
        mLoadingDialog = null;
        mRelatedItem = null;
        DaisyUtils.getVodApplication(this).removeActivtyFromPool(
                this.toString());
        super.onDestroy();
    }

    @Override
    public void onLoadingStarted(AsyncImageView imageView) {
        if (mLoadingImageQueue != null) {
            mLoadingImageQueue.put(imageView, true);
        }
    }

    @Override
    public void onLoadingEnded(AsyncImageView imageView, Bitmap image) {
        if (mLoadingImageQueue != null) {
            mLoadingImageQueue.remove(imageView);
        }
    }

    @Override
    public void onLoadingFailed(AsyncImageView imageView, Throwable throwable) {
        if (mLoadingImageQueue != null) {
            mLoadingImageQueue.remove(imageView);
        }
    }

    class GetItemTask extends AsyncTask<String, Void, Void> {

        int id = 0;
        String url = null;

        @Override
        protected Void doInBackground(String... params) {
            try {
                url = params[0];
                id = SimpleRestClient.getItemId(url, new boolean[1]);
                mItem = mSimpleRestClient.getItem(url);
            } catch (ItemOfflineException e) {
                HashMap<String, Object> exceptionProperties = new HashMap<String, Object>();
                exceptionProperties.put(EventProperty.CODE, "nodetail");
                exceptionProperties.put(EventProperty.CONTENT,
                        "no detail error : " + e.getUrl());
                exceptionProperties.put(EventProperty.ITEM, id);
                NetworkUtils.SaveLogToLocal(NetworkUtils.DETAIL_EXCEPT,
                        exceptionProperties);
                e.printStackTrace();
            } catch (JsonSyntaxException e) {
                HashMap<String, Object> exceptionProperties = new HashMap<String, Object>();
                exceptionProperties.put(EventProperty.CODE, "parsejsonerror");
                exceptionProperties.put(EventProperty.CONTENT, e.getMessage()
                        + " : " + url);
                exceptionProperties.put(EventProperty.ITEM, id);
                NetworkUtils.SaveLogToLocal(NetworkUtils.DETAIL_EXCEPT,
                        exceptionProperties);
                e.printStackTrace();
            } catch (NetworkException e) {
                HashMap<String, Object> exceptionProperties = new HashMap<String, Object>();
                exceptionProperties.put(EventProperty.CODE, "networkconnerror");
                exceptionProperties.put(EventProperty.CONTENT, e.getMessage()
                        + " : " + e.getUrl());
                exceptionProperties.put(EventProperty.ITEM, id);
                NetworkUtils.SaveLogToLocal(NetworkUtils.DETAIL_EXCEPT,
                        exceptionProperties);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (mItem != null) {
                try {
                    if (!isFree())
                        isbuy();
                    else
                        initLayout();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private boolean isFree() {
        if (mItem.expense != null) {
            return false;
        }
        return true;
    }

    private void initLayout() {
        mDetailTitle.setText(mItem.title);
        mDataCollectionProperties.put(EventProperty.ITEM, mItem.pk);
        mDataCollectionProperties.put(EventProperty.TITLE, mItem.title);
        mDataCollectionProperties.put(EventProperty.SOURCE, fromPage);


        new NetworkUtils.DataCollectionTask().execute(
                NetworkUtils.VIDEO_DETAIL_IN, mDataCollectionProperties);
        /*
		 * Build detail attributes list using a given order according to
		 * ContentModel's define. we also need to add some common attributes
		 * which are defined in ContentModel.
		 */
        setExpenseStatus();
        if (mItem.attributes != null && mItem.attributes.map != null) {

            for (ContentModel m : DaisyUtils.getVodApplication(this).mContentModel) {
                if (m.content_model.equals(mItem.content_model)) {
                    mContentModel = m;
                }
            }
            if (mContentModel.attributes.get("genre") == null) {
                mContentModel.attributes.put("genre",
                        getResources().getString(R.string.genre));
            }
            if (mContentModel.attributes.get("vendor") == null) {
                mContentModel.attributes.put("vendor", getResources()
                        .getString(R.string.vendor));
            }
            if (mContentModel.attributes.get("air_date") == null) {
                mContentModel.attributes.put("air_date", getResources()
                        .getString(R.string.air_date));
            }
            if (isDrama) {
                if (mContentModel.attributes.get("episodes") == null) {
                    mContentModel.attributes.put("episodes", getResources()
                            .getString(R.string.episodes));
                }
            }
            if (mContentModel.attributes.get("length") == null) {
                mContentModel.attributes.put("length", getResources()
                        .getString(R.string.length));
            }
            // Used to store Attribute name and value from Item.attributes.map
            LinkedHashMap<String, String> attributeMap = new LinkedHashMap<String, String>();
            attributeMap.put("genre", null);
            attributeMap.put("vendor", null);
            attributeMap.put("length", null);
            attributeMap.put("air_date", null);
            for (String key : mContentModel.attributes.keySet()) {
                attributeMap.put(key, null);
            }
            attributeMap.put("vendor", mItem.vendor);
            attributeMap.put("air_date", mItem.attributes.air_date);
//            if (isDrama()) {
//                attributeMap.put("episodes", getEpisodes(mItem));
//            }
            if (mItem.clip != null) {
                if (mItem.clip.length > 0)
                    attributeMap.put("length", getClipLength(mItem.clip));
            }

            Iterator iter = mItem.attributes.map.keySet().iterator();
            while (iter.hasNext()) {
                String key = (String) iter.next();
                Object value = mItem.attributes.map.get(key);

                if (value != null) {
                    if (value.getClass().equals(String.class)) {
                        attributeMap.put(key, (String) value);
                    } else if (value.getClass().equals(Attribute.Info.class)) {
                        attributeMap.put(key, ((Attribute.Info) value).name);
                    } else if (value.getClass().equals(Attribute.Info[].class)) {
                        StringBuffer sb = new StringBuffer();
                        for (Attribute.Info info : (Attribute.Info[]) value) {
                            sb.append(info.name);
                            sb.append(",");
                        }
                        attributeMap.put(key, sb.substring(0, sb.length() - 1));
                    }
                }
            }
            mDetailAttributeContainer.addAttributeForfilm(attributeMap, mContentModel);
            // mDetailAttributeContainer.
        }
        // Set the content to Introduction View
        mDetailIntro.setText("简介 : " + mItem.description);
//        if (mItem.bean_score > 0) {
//            bean_score.setVisibility(View.VISIBLE);
//            bean_score.setText(mItem.bean_score + "");
//        }
        // Set the favorite button's label.
//        if (isFavorite()) {
//            //mCollectBtn.setBackgroundResource(R.drawable.collected_btn_bg_selector);
//            mCollectBtn.setText(getResources().getString(R.string.favorited));
//        } else {
//            //mCollectBtn.setBackgroundResource(R.drawable.collect_btn_bg_selector);
//            mCollectBtn.setText(getResources().getString(R.string.favorite));
//        }
        if (mItem.poster_url != null) {
            mDetailPreviewImg.setTag(mItem.detail_url);
            mDetailPreviewImg.setUrl(mItem.detail_url);
        }
        if (isFavorite()) {
            //mCollectBtn.setBackgroundResource(R.drawable.collected_btn_bg_selector);
            mCollectBtn.setText(getResources().getString(R.string.favorited));
        } else {
            //mCollectBtn.setBackgroundResource(R.drawable.collect_btn_bg_selector);
            mCollectBtn.setText(getResources().getString(R.string.favorite));
        }
        if (!(mRelatedItem != null && mRelatedItem.length > 0)) {
            mGetRelatedTask = new GetRelatedTask();
            mGetRelatedTask.execute();
        }

        isInitialized = true;
    }

    class GetRelatedTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                mRelatedItem = mSimpleRestClient
                        .getRelatedItem("/api/tv/relate/" + mItem.pk + "/");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (mRelatedItem != null && mRelatedItem.length > 0) {
                buildRelatedList();
            }
            if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
                mLoadingDialog.dismiss();
                bottom_view_layout.setVisibility(View.VISIBLE);
                top_view_layout.setVisibility(View.VISIBLE);
            }
        }

    }

    private void initViews() {
        bottom_view_layout = findViewById(R.id.bottom_view_layout);
        top_view_layout = findViewById(R.id.top_view_layout);
        title = getIntent().getStringExtra("title");

        weatherFragment = (LaunchHeaderLayout) findViewById(R.id.top_column_layout);
        weatherFragment.setTitle(title);
        weatherFragment.hideSubTiltle();
        weatherFragment.hideIndicatorTable();
        weatherFragment.hideWeather();
        mDetailTitle = (TextView) findViewById(R.id.detail_title);
        mDetailIntro = (TextView) findViewById(R.id.detail_intro);
        mDetailPreviewImg = (AsyncImageView) findViewById(R.id.detail_preview_img);
        mDetailAttributeContainer = (DetailAttributeContainer) findViewById(R.id.detail_attribute_container);
        related_video_container = (LinearLayout) findViewById(R.id.related_video_container);
        mMoreContent = (LinearLayout) findViewById(R.id.more_content);
        detail_price_txt = (TextView) findViewById(R.id.detail_price_txt);
        detail_duration_txt = (TextView) findViewById(R.id.detail_duration_txt);
        detail_permission_txt = (TextView) findViewById(R.id.detail_permission_txt);
        detail_tag_txt = (RotateTextView) findViewById(R.id.detail_tag_txt);
        source = (ImageView) findViewById(R.id.source);
//        bean_score = (TextView) findViewById(R.id.bean_score);
        mLeftBtn = (Button) findViewById(R.id.btn_left);
        mMiddleBtn = (Button) findViewById(R.id.middle_btn);
        mRightBtn = (Button) findViewById(R.id.btn_right);
        mLeftBtn.setOnClickListener(mIdOnClickListener);
        mMiddleBtn.setOnClickListener(mIdOnClickListener);
        mRightBtn.setOnClickListener(mIdOnClickListener);
        mMoreContent.setOnClickListener(mIdOnClickListener);
        mLeftBtn.setOnHoverListener(mOnHoverListener);
        mMiddleBtn.setOnHoverListener(mOnHoverListener);
        mRightBtn.setOnHoverListener(mOnHoverListener);

        mLeftBtn.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                initFocusBtn(v, hasFocus);
            }
        });

        mMiddleBtn.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                initFocusBtn(v, hasFocus);
            }
        });

        mRightBtn.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                initFocusBtn(v, hasFocus);
            }
        });
    }

    private void initFocusBtn(View v, boolean hasFocus) {
        String identify = (String) v.getTag();

        if (hasFocus) {
            if (COLLECT_VIDEO.equals(identify)) {
                v.setBackgroundResource(R.drawable.filmcollect_focus_btn_bg);
            } else if (BUY_VIDEO.equals(identify)) {
                v.setBackgroundResource(R.drawable.filmbuybideo_focus_btn_bg);
            } else if (PREVIEW_VIDEO.equals(identify)) {
                v.setBackgroundResource(R.drawable.filmplayvideo_focus_btn_bg);
            } else if (PLAY_VIDEO.equals(identify)) {
                v.setBackgroundResource(R.drawable.filmplayvideo_focus_btn_bg);
            } else if (DRAMA_VIDEO.equals(identify)) {
                v.setBackgroundResource(R.drawable.zydramalist_focus_btn_bg);
            }
        } else {
            if (COLLECT_VIDEO.equals(identify)) {
                v.setBackgroundResource(R.drawable.filmcollect_normal_btn_bg);
            } else if (BUY_VIDEO.equals(identify)) {
                v.setBackgroundResource(R.drawable.filmbuyvideo_normal_btn_bg);
            } else if (PREVIEW_VIDEO.equals(identify)) {
                v.setBackgroundResource(R.drawable.filmplayvideo_normal_btn_bg);
            } else if (PLAY_VIDEO.equals(identify)) {
                v.setBackgroundResource(R.drawable.filmplayvideo_normal_btn_bg);
            } else if (DRAMA_VIDEO.equals(identify)) {
                v.setBackgroundResource(R.drawable.zydramalist_normal_btn_bg);
            }
        }

    }

    private void isbuy() {
        SimpleRestClient simpleRestClient = new SimpleRestClient();
        simpleRestClient.doSendRequest("/api/play/check/", "post",
                "device_token=" + SimpleRestClient.device_token
                        + "&access_token=" + SimpleRestClient.access_token
                        + "&item=" + mItem.pk, new SimpleRestClient.HttpPostRequestInterface() {


                    // subitem=214277
                    @Override
                    public void onSuccess(String info) {
                        // TODO Auto-generated method stub
                        if ("0".equals(info)) {
                            isBuy = false;
                        } else {
                            JSONArray jsonArray;
                            try {
                                jsonArray = new JSONArray(info);
                                JSONObject json = jsonArray.getJSONObject(0);
                                if (json.has("max_expiry_date")) {
                                    // 电视剧部分购买
                                    isBuy = false;// 暂时无法处理
                                }  else {
                                    // 电影或者电视剧整部购买
                                    try {
                                        remainDay = Util.daysBetween(
                                                Util.getTime(), info) + 1;
                                        if (remainDay == 0) {
                                            isBuy = false;// 过期了。认为没购买
                                            remainDay = -1;
                                        } else
                                            isBuy = true;// 购买了，剩余天数大于0
                                    } catch (ParseException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }
                                }
                            } catch (JSONException e) {
                                // TODO Auto-generated catch block
                                try{
                                JSONObject object = new JSONObject(info);
                                 info = object.getString("expiry_date");
                                 iqiyi_code = object.getString("iqiyi_code");
                                    if(iqiyi_code != null && !"".equals(iqiyi_code) && !"null".equals(iqiyi_code)){
                                        api_check();
                                    }
                                } catch (JSONException ee) {
                                    info = info.substring(1, info.length() - 1);
                                }
                                toDate = info;
                                try {
                                    remainDay = Util.daysBetween(
                                            Util.getTime(), info) + 1;
                                    if (remainDay == 0) {
                                        isBuy = false;// 过期了。认为没购买
                                        remainDay = -1;
                                    } else
                                        isBuy = true;// 购买了，剩余天数大于0
                                } catch (ParseException e1) {
                                    // TODO Auto-generated catch block
                                    e1.printStackTrace();
                                }
                                e.printStackTrace();
                            }
                        }
                        if(iqiyi_code ==null || (iqiyi_code!= null && "null".equals(iqiyi_code)))
                        initLayout();
                    }

                    @Override
                    public void onPrepare() {
                        // TODO Auto-generated method stub
                    }

                    @Override
                    public void onFailed(String error) {
                        // TODO Auto-generated method stub
                        isBuy = false;
                        initLayout();
                    }
                });
    }

    private boolean isDrama() {
        if (mItem.subitems == null || mItem.subitems.length == 0) {
            isDrama = false;
        } else {
            isDrama = true;
        }
        return isDrama;
    }

    private void buildRelatedList() {
        float rate = DaisyUtils.getVodApplication(this).getRate(this);
        for (int i = 0; i < 6 && i < mRelatedItem.length; i++) {
            View relatedHolder = LayoutInflater
                    .from(PFilmItemdetailActivity.this).inflate(
                            R.layout.realte_portrait_item, null);
            LinearLayout.LayoutParams layoutParams;

            layoutParams = new LinearLayout.LayoutParams(254, 401);
            if (i != 0)
                layoutParams.leftMargin = 8;
            relatedHolder.setLayoutParams(layoutParams);
            TextView titleView = (TextView) relatedHolder
                    .findViewById(R.id.related_title);
            LabelImageView imgView = (LabelImageView) relatedHolder
                    .findViewById(R.id.related_preview_img);
            TextView ItemBeanScore= (TextView)relatedHolder.findViewById(R.id.ItemBeanScore);
            RotateTextView expense_txt= (RotateTextView)relatedHolder.findViewById(R.id.expense_txt);
            expense_txt.setDegrees(315);
            imgView.setOnImageViewLoadListener(this);
            if (mRelatedItem[i].bean_score > 0) {
                ItemBeanScore.setText(mRelatedItem[i].bean_score + "");
                ItemBeanScore.setVisibility(View.VISIBLE);
            }
            if (mRelatedItem[i].expense != null) {
                if (mRelatedItem[i].expense.cptitle != null) {
                    expense_txt.setVisibility(View.VISIBLE);
                    expense_txt.setText(mRelatedItem[i].expense.cptitle);
                    if(mRelatedItem[i].expense.pay_type==1){
                        expense_txt.setBackgroundResource(R.drawable.list_single_buy);
                    }else if((mRelatedItem[i].expense.cpname).startsWith("ismar")){
                        expense_txt.setBackgroundResource(R.drawable.list_ismar);
                    }else if("iqiyi".equals(mRelatedItem[i].expense.cpname)){
                        expense_txt.setBackgroundResource(R.drawable.list_lizhi);
                    }
//                related_price_txt.setText("￥" + mRelatedItem[i].expense.price);
                }
            }
            imgView.setTag(mRelatedItem[i].adlet_url);
//            if(StringUtils.isNotEmpty(mRelatedItem[i].list_url)){
//
//            }
//            else{
//                imgView.setUrl(mRelatedItem[i].adlet_url);
//            }
            imgView.setUrl(mRelatedItem[i].list_url);
            if (mRelatedItem[i].focus != null)
                imgView.setFocustitle(mRelatedItem[i].focus);
            titleView.setText(mRelatedItem[i].title);
            relatedHolder.setTag(mRelatedItem[i]);
            related_video_container.addView(relatedHolder);
            relatedHolder
                    .setOnFocusChangeListener(mRelatedOnFocusChangeListener);

            relatedHolder.setOnClickListener(mRelatedClickListener);
            relatedHolder.setOnHoverListener(mOnHoverListener);
        }
    }

    private View.OnFocusChangeListener mRelatedOnFocusChangeListener = new View.OnFocusChangeListener() {

        @Override
        public void onFocusChange(View v, boolean hasFocus) {

            if (hasFocus) {
                TextView title = (TextView) v
                        .findViewById(R.id.related_title);
                LabelImageView img = (LabelImageView) v.findViewById(R.id.related_preview_img);
                img.setDrawBorder(true);
                img.invalidate();
                title.setTextColor(0xFFF8F8FF);
                // img.setBackgroundResource(R.drawable.popup_bg_yellow);

                title.setSelected(true);

            } else {
                TextView title = (TextView) v
                        .findViewById(R.id.related_title);
                LabelImageView img = (LabelImageView) v.findViewById(R.id.related_preview_img);
                title.setTextColor(0xFFF8F8FF);
                img.setDrawBorder(false);
                img.invalidate();
                title.setSelected(false);

            }


        }
    };
    private View.OnClickListener mIdOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            try {
                int id = v.getId();
                Intent intent = new Intent();
                intent.putExtra(EventProperty.SECTION, "");
                tool = new InitPlayerTool(
                        PFilmItemdetailActivity.this);
                tool.channel = channel;
                tool.slug = slug;
                tool.fromPage = fromPage;
                tool.setonAsyncTaskListener(new InitPlayerTool.onAsyncTaskHandler() {

                    @Override
                    public void onPreExecute(Intent intent) {
                        // TODO Auto-generated method stub
                        if (mLoadingDialog != null)
                            mLoadingDialog.show();
                    }

                    @Override
                    public void onPostExecute() {
                        // TODO Auto-generated method stub
                        if (mLoadingDialog != null)
                            mLoadingDialog.dismiss();
                    }
                });
                switch (id) {
                    case R.id.btn_left:
                        String subUrl = null;
                        if (isDrama()) {
                            int sub_id = 0;
                            String title = mItem.title;
                            if (mHistory != null && mHistory.is_continue) {
                                subUrl = mHistory.sub_url;
                                for (Item item : mItem.subitems) {
                                    if (item.url.equals(subUrl)) {
                                        sub_id = item.pk;
                                        title += "(" + item.episode + ")";
                                        break;
                                    }
                                }
                            } else {
                                subUrl = mItem.subitems[0].url;
                                sub_id = mItem.subitems[0].pk;
                                title += "(" + mItem.subitems[0].episode + ")";
                            }
                        }
                        identify = (String) v.getTag();
                        if (identify.equals(PREVIEW_VIDEO)) {
                            // 预告
                            if (isDrama()) {
                                tool.initClipInfo(mItem.subitems[0].url, InitPlayerTool.FLAG_URL);
                            } else {
                                tool.initClipInfo(mItem, InitPlayerTool.FLAG_ITEM, true, null);
                            }
                        } else if (identify.equals(PLAY_VIDEO)) {
                            // 播放
                            if (isDrama())
                                tool.initClipInfo(subUrl, InitPlayerTool.FLAG_URL);
                            else
                                tool.initClipInfo(mItem, InitPlayerTool.FLAG_ITEM);
                        }

                        // tool.initClipInfo(subUrl,InitPlayerTool.FLAG_URL);
                        break;
                    case R.id.middle_btn:
                        identify = (String) v.getTag();
                        if (identify.equals(BUY_VIDEO)) {
                            // 购买
                            if (isDrama()) {
                                // startDramaListActivity();
                            } else {
                                buyVideo();
                            }
                        } else if (identify.equals(COLLECT_VIDEO)) {
                            addFavorite();
                            if (isFavorite()) {
                                //v.setBackgroundResource(R.drawable.collected_btn_bg_selector);
                                ((Button) v).setText(getResources().getString(R.string.favorited));

                            } else {
                                //v.setBackgroundResource(R.drawable.collect_btn_bg_selector);
                                ((Button) v).setText(getResources().getString(R.string.favorite));
                            }
                        }
                        break;
                    case R.id.btn_right:
                        identify = (String) v.getTag();
                        if (identify.equals(COLLECT_VIDEO)) {
                            addFavorite();
                            if (isFavorite()) {
                                //v.setBackgroundResource(R.drawable.collected_btn_bg_selector);
                                ((Button) v).setText(getResources().getString(R.string.favorited));
                            } else {
                                //v.setBackgroundResource(R.drawable.collect_btn_bg_selector);
                                ((Button) v).setText(getResources().getString(R.string.favorite));
                            }
                        } else if (identify.equals(DRAMA_VIDEO)) {
                            intent.setClass(PFilmItemdetailActivity.this,
                                    DramaListActivity.class);
                            intent.putExtra("item", mItem);
                            startActivityForResult(intent, 11);
                        }
                        break;
                    case R.id.more_content:
                        if (mRelatedItem != null && mRelatedItem.length > 0) {
                            intent.putExtra("related_item", new ArrayList<Item>(
                                    Arrays.asList(mRelatedItem)));
                        }


                        intent.putExtra("item", mItem);
                        mItem.content_model = "movie";
                        intent.setClass(PFilmItemdetailActivity.this,
                                RelatedActivity.class);
                        startActivity(intent);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    private View.OnClickListener mRelatedClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            Item itemSection = (Item) v.getTag();
            final Item[] relatedItem = mRelatedItem;
            for (Item item : relatedItem) {
                if (itemSection.item_url.equals(item.item_url)) {
                    HashMap<String, Object> properties = new HashMap<String, Object>();
                    properties.put(EventProperty.ITEM, mItem.pk);
                    properties.put(EventProperty.TO_ITEM, item.pk);
                    properties.put(EventProperty.TO_TITLE, item.title);
                    properties.put(EventProperty.TO, "relate");
                    new NetworkUtils.DataCollectionTask().execute(
                            NetworkUtils.VIDEO_RELATE, properties);
                    break;
                }
            }
//            Intent intent = new Intent();
//            intent.setAction("tv.ismar.daisy.PFileItem");
//            intent.putExtra("url", url);
//            startActivity(intent);
            if ("launcher".equals(fromPage)) {
                fromPage = "tvhome";
            } else {
                fromPage = "related";
            }
            DaisyUtils.gotoSpecialPage(PFilmItemdetailActivity.this, itemSection.content_model, itemSection.item_url, fromPage);
        }
    };
    private DialogInterface.OnCancelListener mLoadingCancelListener = new DialogInterface.OnCancelListener() {

        @Override
        public void onCancel(DialogInterface dialog) {
            PFilmItemdetailActivity.this.finish();
            dialog.dismiss();
        }
    };

    private String getClipLength(Clip clip) {
        if (clip != null) {
            if (clip.length > 120) {
                return clip.length / 60
                        + getResources().getString(R.string.minute);
            } else {
                return clip.length + getResources().getString(R.string.second);
            }
        } else {
            return null;
        }
    }

    private String getEpisodes(Item item) {
        if (item.subitems.length > 0) {
            String update_to_episode = getResources().getString(
                    R.string.update_to_episode);
            return item.episode + "("
                    + String.format(update_to_episode, item.subitems.length)
                    + ")";
        } else {
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if (resultCode == 20) {
            if (data.getBooleanExtra("result", false)) {
                isBuy = true;
                setExpenseStatus();
                if (mLeftBtn != null)
                    mLeftBtn.setBackgroundResource(R.drawable.filmplayvideo_focus_btn_bg);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean ret = super.onKeyDown(keyCode, event);
        if ("lcd_s3a01".equals(VodUserAgent.getModelName())) {
            if (keyCode == 707 || keyCode == 774 || keyCode == 253) {
                isneedpause = false;
            }
        } else {
            if (keyCode == 223 || keyCode == 499 || keyCode == 480) {
                isneedpause = false;
            }
        }
        return ret;
    }

    private OnHoverListener mOnHoverListener = new OnHoverListener() {

        @Override
        public boolean onHover(View v, MotionEvent keycode) {
            switch (keycode.getAction()) {
                case MotionEvent.ACTION_HOVER_ENTER:
                case MotionEvent.ACTION_HOVER_MOVE:
                    if (v instanceof Button) {
                        v.requestFocus();
                        initFocusBtn(v, true);
                    } else {
                        v.requestFocus();
                        // img.setBackgroundResource(R.drawable.popup_bg_yellow);
                    }
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
                    if (v instanceof Button) {
                        initFocusBtn(v, false);
                    } else {
                        TextView title = (TextView) v
                                .findViewById(R.id.related_title);
                        LabelImageView img = (LabelImageView) v.findViewById(R.id.related_preview_img);
                        title.setTextColor(0xFFF8F8FF);
                        img.setDrawBorder(false);
                        img.invalidate();
                        title.setSelected(false);
                    }
                    break;
                default:
                    break;
            }
            return false;
        }
    };

    private  void api_check(){
        SimpleRestClient simpleRestClient = new SimpleRestClient();
        simpleRestClient.doSendRequest(SimpleRestClient.carnation_domain+"/api/check/", "post",
                "device_token=" + SimpleRestClient.device_token
                        + "&access_token=" + SimpleRestClient.access_token
                        + "&verify_code=" + iqiyi_code, new SimpleRestClient.HttpPostRequestInterface() {
                    // subitem=214277
                    @Override
                    public void onSuccess(String info) {
                        try {
                            JSONObject object = new JSONObject(info);
                            int code =object.getInt("code");
                            if(code == 1)
                            isBuy = false;
                            else
                            isBuy = true;
                            initLayout();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onPrepare() {
                        // TODO Auto-generated method stub
                    }

                    @Override
                    public void onFailed(String error) {
                        // TODO Auto-generated method stub
                        isBuy = false;
                        initLayout();
                    }
                });
    }


}
