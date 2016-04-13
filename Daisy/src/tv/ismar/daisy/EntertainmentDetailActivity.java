package tv.ismar.daisy;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
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
import tv.ismar.daisy.ui.widget.LaunchHeaderLayout;
import tv.ismar.daisy.utils.BitmapDecoder;
import tv.ismar.daisy.utils.Util;
import tv.ismar.daisy.views.*;

import java.text.ParseException;
import java.util.*;

/**
 * Created by zhangjiqiang on 15-7-2.
 */
public class EntertainmentDetailActivity extends BaseActivity implements AsyncImageView.OnImageViewLoadListener {

    private int subitem_show;
    private String mChannel;
    private SimpleRestClient mSimpleRestClient;
    private LoadingDialog mLoadingDialog;
    private String title;
    private View large_layout;
    private Item mItem;
    private GetItemTask mGetItemTask;
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
    private ContentModel mContentModel;
    private boolean isDrama = false;
    private LinearLayout related_video_container;
    private Item[] mRelatedItem;
    private boolean isPause = false;
    private TextView detail_price_txt;
    private TextView detail_duration_txt;
    private TextView detail_permission_txt;
    private RotateTextView detail_tag_txt;
    private TextView mDetailTitle;
    private TextView mDetailIntro;
    private TextView related_recomman_label;
    private AsyncImageView mDetailPreviewImg;
    private DetailAttributeContainer mDetailAttributeContainer;
    private LaunchHeaderLayout weatherFragment;
    private String channel;
    private String slug;
    private String fromPage;
    private BitmapDecoder bitmapDecoder;
    private InitPlayerTool tool;
    private boolean isneedpause = true;
    private void initViews() {
        large_layout = findViewById(R.id.large_layout);
        mChannel = getIntent().getStringExtra("channel");
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
        mLeftBtn = (Button) findViewById(R.id.btn_left);
        mMiddleBtn = (Button) findViewById(R.id.middle_btn);
        mRightBtn = (Button) findViewById(R.id.btn_right);
        related_recomman_label = (TextView) findViewById(R.id.related_recomman_label);
        mLeftBtn.setOnClickListener(mIdOnClickListener);
        mMiddleBtn.setOnClickListener(mIdOnClickListener);
        mRightBtn.setOnClickListener(mIdOnClickListener);
        mMoreContent.setOnClickListener(mIdOnClickListener);
        mLeftBtn.setOnHoverListener(onHoverListener);
        mMiddleBtn.setOnHoverListener(onHoverListener);
        mRightBtn.setOnHoverListener(onHoverListener);
        mMoreContent.setOnHoverListener(onHoverListener);
        detail_price_txt = (TextView) findViewById(R.id.detail_price_txt);
        detail_permission_txt = (TextView) findViewById(R.id.detail_permission_txt);
        detail_duration_txt = (TextView) findViewById(R.id.detail_duration_txt);
        detail_tag_txt = (RotateTextView) findViewById(R.id.detail_tag_txt);
        //mLeftBtn.setFocusable(false);
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
                v.setBackgroundResource(R.drawable.zycollect_focus_btn_bg);
            } else if (BUY_VIDEO.equals(identify)) {
                v.setBackgroundResource(R.drawable.buybideo_focus_btn_bg);
            } else if (PREVIEW_VIDEO.equals(identify)) {
                if (isDrama()) {
                    v.setBackgroundResource(R.drawable.zyplayvideo_focus_btn_bg);
                } else {
                    v.setBackgroundResource(R.drawable.playvideo_focus_btn_bg);
                }
            } else if (PLAY_VIDEO.equals(identify)) {
                if (isDrama())
                    v.setBackgroundResource(R.drawable.zyplayvideo_focus_btn_bg);
                else
                    v.setBackgroundResource(R.drawable.playvideo_focus_btn_bg);
            } else if (DRAMA_VIDEO.equals(identify)) {
                v.setBackgroundResource(R.drawable.zydramalist_focus_btn_bg);
            }
        } else {
            if (COLLECT_VIDEO.equals(identify)) {
                v.setBackgroundResource(R.drawable.zycollect_normal_btn_bg);
            } else if (BUY_VIDEO.equals(identify)) {
                v.setBackgroundResource(R.drawable.buyvideo_normal_btn_bg);
            } else if (PREVIEW_VIDEO.equals(identify)) {
                if (isDrama())
                    v.setBackgroundResource(R.drawable.zyplayvideo_normal_btn_bg);
                else
                    v.setBackgroundResource(R.drawable.playvideo_normal_btn_bg);
            } else if (PLAY_VIDEO.equals(identify)) {
                if (isDrama())
                    v.setBackgroundResource(R.drawable.zyplayvideo_normal_btn_bg);
                else
                    v.setBackgroundResource(R.drawable.playvideo_normal_btn_bg);
            } else if (DRAMA_VIDEO.equals(identify)) {
                v.setBackgroundResource(R.drawable.zydramalist_normal_btn_bg);
            }
        }

    }

    private View.OnClickListener mIdOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            try {
                int id = v.getId();
                Intent intent = new Intent();
                intent.putExtra(EventProperty.SECTION, "");
                tool = new InitPlayerTool(
                        EntertainmentDetailActivity.this);
                tool.slug = slug;
                tool.channel = channel;
                tool.fromPage = fromPage;
                tool.setonAsyncTaskListener(new InitPlayerTool.onAsyncTaskHandler() {

                    @Override
                    public void onPreExecute(Intent intent) {
                        // TODO Auto-generated method stub
                    	if(mLoadingDialog != null)
                        mLoadingDialog.show();
                    }

                    @Override
                    public void onPostExecute() {
                        if (mLoadingDialog!= null){
                            mLoadingDialog.dismiss();
                        }
                    }
                });
                switch (id) {
                    case R.id.btn_left:
                        String subUrl = null;
                        if (isDrama()) {
//                            int sub_id = 0;
//                            String title = mItem.title;
//                            if (mHistory != null && mHistory.is_continue) {
//                                subUrl = mHistory.sub_url;
//                                for (Item item : mItem.subitems) {
//                                    if (item.url.equals(subUrl)) {
//                                        sub_id = item.pk;
//                                        title += "(" + item.episode + ")";
//                                        break;
//                                    }
//                                }
//                            } else {
//                                subUrl = mItem.subitems[0].url;
//                                sub_id = mItem.subitems[0].pk;
//                                title += "(" + mItem.subitems[0].episode + ")";
//                            }

                            Item item = getItemByClipPk(mItem.clip.pk);
                            subUrl = item.url;
                        }
                        identify = (String) v.getTag();
                        if (identify.equals(PREVIEW_VIDEO)) {
                            // 预告
                            if (isDrama()) {
                                tool.initClipInfo(mItem.subitems[0].url, InitPlayerTool.FLAG_URL);
                            } else {
                                tool.initClipInfo(mItem, InitPlayerTool.FLAG_ITEM, true,null);
                            }
                        } else if (identify.equals(PLAY_VIDEO)) {
                            // 播放
                            if (isDrama())
                                tool.initClipInfo(subUrl, InitPlayerTool.FLAG_URL);
                            else {
                                tool.initClipInfo(mItem, InitPlayerTool.FLAG_ITEM);

                            }
                        }
                        break;
                    case R.id.middle_btn:
                        identify = (String) v.getTag();
                        if (identify.equals(BUY_VIDEO)) {
                            // 购买
                            if (isDrama()) {

                                startDramaListActivity();
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
                            startDramaListActivity();
                        }
                        break;
                    case R.id.more_content:
                        if (mRelatedItem != null && mRelatedItem.length > 0) {
                            intent.putExtra("related_item", new ArrayList<Item>(
                                    Arrays.asList(mRelatedItem)));
                        }
                        intent.putExtra("item", mItem);
                        intent.setClass(EntertainmentDetailActivity.this,
                                RelatedActivity.class);
                        startActivity(intent);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSimpleRestClient = new SimpleRestClient();
        setContentView(R.layout.entertainment_detail_view);
        final View background = findViewById(R.id.large_layout);
        bitmapDecoder = new BitmapDecoder();
        bitmapDecoder.decode(this, R.drawable.main_bg, new BitmapDecoder.Callback() {
            @Override
            public void onSuccess(BitmapDrawable bitmapDrawable) {
            	background.setBackgroundDrawable(bitmapDrawable);
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
            fromPage = getIntent().getStringExtra("fromPage");
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
        mSimpleRestClient = new SimpleRestClient();
        setContentView(R.layout.entertainment_detail_view);
        final  View background = findViewById(R.id.large_layout);
        bitmapDecoder = new BitmapDecoder();
        bitmapDecoder.decode(this, R.drawable.main_bg, new BitmapDecoder.Callback() {
            @Override
            public void onSuccess(BitmapDrawable bitmapDrawable) {
            	background.setBackgroundDrawable(bitmapDrawable);
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
            fromPage = getIntent().getStringExtra("fromPage");
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
//        if (isInitialized) {
//            if (isDrama()) {
//                String url = mItem.item_url == null ? mSimpleRestClient.root_url
//                        + "/api/item/" + mItem.pk + "/"
//                        : mItem.item_url;
//                if (SimpleRestClient.isLogin())
//                    mHistory = DaisyUtils.getHistoryManager(this).getHistoryByUrl(
//                            url, "yes");
//                else
//                    mHistory = DaisyUtils.getHistoryManager(this).getHistoryByUrl(
//                            url, "no");
//            }
//        }
        for (HashMap.Entry<AsyncImageView, Boolean> entry : mLoadingImageQueue
                .entrySet()) {
            if (!entry.getValue()) {
                entry.getKey().setPaused(true);
                entry.setValue(true);
            }
        }
        if (isPause && mCollectBtn!= null) {
            if (isFavorite()) {
                //mCollectBtn.setBackgroundResource(R.drawable.collected_btn_bg_selector);
                mCollectBtn.setText(getResources().getString(R.string.favorited));
            } else {
                //mCollectBtn.setBackgroundResource(R.drawable.collect_btn_bg_selector);
                mCollectBtn.setText(getResources().getString(R.string.favorite));
            }
            Item item = getItemByClipPk(1);
            if (item != null) {
                if (item.expense != null && isBuy)
                    mLeftBtn.setText("播放 " + item.subtitle);
                else if (item.expense == null) {
                    mLeftBtn.setText("播放 " + item.subtitle);
                }
//
//
//
//             else{
//                    mLeftBtn.setText("播放");
//                }
            }

            isPause = false;
        }
        isneedpause = true;
        super.onResume();
    }

    @Override
    protected void onPause() {
    	if(isneedpause)
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
    	if(tool != null)
    		tool.removeAsycCallback();
        if(bitmapDecoder != null && bitmapDecoder.isAlive()){
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

    private boolean isDrama() {
        if (mItem.subitems == null || mItem.subitems.length == 0) {
            isDrama = false;
        } else {
            isDrama = true;
        }
        return isDrama;
    }

    private void startDramaListActivity() {
        Intent intent = new Intent();
        mDataCollectionProperties.put("to", "list");
        subitem_show = mItem.subitem_show;
        if (subitem_show == 1) {
            intent.setClass(EntertainmentDetailActivity.this, DramaVarietyNoMonthList.class);
        } else if (subitem_show == 2) {
            intent.setClass(EntertainmentDetailActivity.this, DramaVarietyMonthList.class);
        } else {
            intent.setClass(EntertainmentDetailActivity.this, DramaListActivity.class);
        }
        intent.putExtra("title", title);
        intent.putExtra("item", mItem);
        startActivityForResult(intent, 20);
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
                View top_view_layout = findViewById(R.id.top_view_layout);
                top_view_layout.setVisibility(View.VISIBLE);
                buildRelatedList();
				mMoreContent.setVisibility(View.VISIBLE);
				related_recomman_label.setVisibility(View.VISIBLE);
            }
            if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
                mLoadingDialog.dismiss();
                large_layout.setVisibility(View.VISIBLE);
            }
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


                Log.i("1", "2");

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

    private void isbuy() {
        SimpleRestClient simpleRestClient = new SimpleRestClient();
        simpleRestClient.doSendRequest("/api/order/check/", "post",
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
                                } else {
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
                                info = info.substring(1, info.length() - 1);
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
                        //isBuy = true;
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
            DaisyUtils.getFavoriteManager(EntertainmentDetailActivity.this)
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
            if (SimpleRestClient.isLogin()) {
                favorite.isnet = "yes";
                createFavoriteByNet();
            } else {
                favorite.isnet = "no";
            }
            DaisyUtils.getFavoriteManager(EntertainmentDetailActivity.this).addFavorite(
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
//            Item item = getItemByClipPk(mItem.clip.pk);
//            if(item!=null){
//                mLeftBtn.setText("播放 "+item.subtitle);
//            }
//            else{
//                mLeftBtn.setText("播放");
//            }
//            // mLeftBtn.setText(getResources().getString(R.string.play));
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
//                Item item = getItemByClipPk(mItem.clip.pk);
//                if(item!=null){
//                    mLeftBtn.setText("播放 "+item.subtitle);
//                }
//                else
//                    mLeftBtn.setText(getResources().getString(R.string.play));
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
//        mLeftBtn.setFocusable(true);
//        mLeftBtn.requestFocus();
//    }

    private void setExpenseStatus() {
        /*
		 * if this item is a drama , the button should split to two. otherwise.
		 * use one button.
		 */
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
            Item item = getItemByClipPk(mItem.clip.pk);
            if (item != null) {
                mLeftBtn.setText("播放 " + item.subtitle);
            } else {
                mLeftBtn.setText("播放");
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mLeftBtn.getLayoutParams();
                params.width = 214;
                mLeftBtn.setLayoutParams(params);
            }
            //setLeftDrawable(drawableleftcollect, mMiddleBtn);
            mMiddleBtn.setText(getResources().getString(R.string.favorite));
            mLeftBtn.setTag(PLAY_VIDEO);
            mMiddleBtn.setTag(COLLECT_VIDEO);
            initFocusBtn(mLeftBtn, false);
            initFocusBtn(mMiddleBtn, false);
            mCollectBtn = mMiddleBtn;
        } else {
            // 收费
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
                mLeftBtn.setTag(PREVIEW_VIDEO);
                mLeftBtn.setText(getResources().getString(R.string.preview_video));
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mLeftBtn.getLayoutParams();
                params.width = 214;
                mLeftBtn.setPadding(45, mLeftBtn.getPaddingTop(), mLeftBtn.getPaddingRight(), mLeftBtn.getPaddingBottom());
                mLeftBtn.setLayoutParams(params);
                //setLeftDrawable(drawableleftbuy, mMiddleBtn);
                mMiddleBtn.setTag(BUY_VIDEO);
                mMiddleBtn.setText(getResources().getString(R.string.buy_video));
                //setLeftDrawable(drawableleftcollect, mRightBtn);
                mRightBtn.setText(getResources().getString(R.string.favorite));
                mRightBtn.setTag(COLLECT_VIDEO);
                initFocusBtn(mLeftBtn, false);
                initFocusBtn(mRightBtn, false);
                initFocusBtn(mMiddleBtn, false);
                if (mItem.expense.cpid == 3) {
                    detail_permission_txt.setVisibility(View.VISIBLE);
                } else {
                    detail_price_txt.setText("￥" + mItem.expense.price);
//                detail_duration_txt.setText("有效期" + mItem.expense.duration
//                        + "天");
                    detail_price_txt.setVisibility(View.VISIBLE);
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
                mLeftBtn.setTag(PLAY_VIDEO);
                Item item = getItemByClipPk(mItem.clip.pk);
                if (item != null) {
                    mLeftBtn.setText("播放 " + item.subtitle);
                } else {
                    mLeftBtn.setText(getResources().getString(R.string.play));
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mLeftBtn.getLayoutParams();
                    params.width = 214;
                    mLeftBtn.setPadding(45, mLeftBtn.getPaddingTop(), mLeftBtn.getPaddingRight(), mLeftBtn.getPaddingBottom());
                    mLeftBtn.setLayoutParams(params);
                }

                //setLeftDrawable(drawableleftcollect,mMiddleBtn);
                mMiddleBtn.setText(getResources().getString(R.string.favorite));
                mMiddleBtn.setTag(COLLECT_VIDEO);
                initFocusBtn(mLeftBtn, false);
                initFocusBtn(mMiddleBtn, false);
//                detail_price_txt.setText("已付费");
//                detail_duration_txt.setText("剩余" + remainDay + "天");
                detail_duration_txt.setText("有效期" + mItem.expense.duration + "天");
//                detail_price_txt.setVisibility(View.VISIBLE);
                detail_duration_txt.setVisibility(View.VISIBLE);
//                detail_duration_txt
//                        .setBackgroundResource(R.drawable.vod_detail_already_payment_duration);
//                detail_price_txt
//                        .setBackgroundResource(R.drawable.vod_detail_already_payment_price);
                mCollectBtn = mMiddleBtn;
            }
        }
        mLeftBtn.setFocusable(true);
        mLeftBtn.requestFocus();
    }

    private void buyVideo() {
        PaymentDialog dialog = new PaymentDialog(EntertainmentDetailActivity.this,
                R.style.PaymentDialog, ordercheckListener);
        mItem.model_name = "item";
        dialog.setItem(mItem);
        dialog.show();
    }

    private void buildRelatedList() {
        float rate = DaisyUtils.getVodApplication(EntertainmentDetailActivity.this).getRate(EntertainmentDetailActivity.this);
        for (int i = 0; i < 4 && i < mRelatedItem.length; i++) {
            View relatedHolder = LayoutInflater
                    .from(EntertainmentDetailActivity.this).inflate(
                            R.layout.realte_entertainment_item, null);
            LinearLayout.LayoutParams layoutParams;
            layoutParams = new LinearLayout.LayoutParams(336, 242);
            if (i != 0)
                layoutParams.leftMargin = 74;
            relatedHolder.setLayoutParams(layoutParams);
            TextView titleView = (TextView) relatedHolder
                    .findViewById(R.id.related_title);
            LabelImageView imgView = (LabelImageView) relatedHolder
                    .findViewById(R.id.related_preview_img);
            imgView.setOnImageViewLoadListener(this);
//            if (mRelatedItem[i].bean_score > 0) {
//                ItemBeanScore.setText(mRelatedItem[i].bean_score + "");
//                ItemBeanScore.setVisibility(View.VISIBLE);
//            }
//            if (mRelatedItem[i].expense != null) {
//                related_price_txt.setVisibility(View.VISIBLE);
//                related_price_txt.setText("￥" + mRelatedItem[i].expense.price);
//            }
            imgView.setTag(mRelatedItem[i].adlet_url);
//            if(StringUtils.isNotEmpty(mRelatedItem[i].list_url)){
//
//            }
//            else{
//                imgView.setUrl(mRelatedItem[i].adlet_url);
//            }
            imgView.setUrl(mRelatedItem[i].adlet_url);
            if (mRelatedItem[i].focus != null)
                imgView.setFocustitle(mRelatedItem[i].focus);
            titleView.setText(mRelatedItem[i].title);
            relatedHolder.setTag(mRelatedItem[i]);
            related_video_container.addView(relatedHolder);
            relatedHolder
                    .setOnFocusChangeListener(mRelatedOnFocusChangeListener);
            relatedHolder.setOnHoverListener(onHoverListener);
            relatedHolder.setOnClickListener(mRelatedClickListener);
        }
    }

    private View.OnFocusChangeListener mRelatedOnFocusChangeListener = new View.OnFocusChangeListener() {

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                TextView title = (TextView) v
                        .findViewById(R.id.related_title);
                LabelImageView img = (LabelImageView) v.findViewById(R.id.related_preview_img);
                title.setTextColor(0xFFF8F8FF);
                //img.setBackgroundResource(R.drawable.popup_bg_yellow);
                img.setDrawBorder(true);
                img.invalidate();
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
                    if (item.expense != null && (item.content_model.equals("variety") || item.content_model.equals("entertainment"))) {
                        item.content_model = "music";
                    }
                    break;
                }
            }
//            Intent intent = new Intent();
//            intent.setAction("tv.ismar.daisy.PFileItem");
//            intent.putExtra("url", url);
//            startActivity(intent);
            if ("launcher".equals(fromPage)) {
                fromPage = "tvhome";
            }else{
            fromPage = "related";
        }
            DaisyUtils.gotoSpecialPage(EntertainmentDetailActivity.this, itemSection.content_model, itemSection.item_url, fromPage);

        }
    };
    private DialogInterface.OnCancelListener mLoadingCancelListener = new DialogInterface.OnCancelListener() {

        @Override
        public void onCancel(DialogInterface dialog) {
            EntertainmentDetailActivity.this.finish();
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

    //    private Item getItemByClipPk(int pk) {
//        Item item = null;
//        Item[] items = mItem.subitems;
//        int count = items.length;
//        for (int i = 0; i < count; i++) {
//            if (mItem.subitems[i].clip.pk == pk) {
//                item = mItem.subitems[i];
//                break;
//            }
//        }
//        return item;
//    }
    private Item getItemByClipPk(int pk) {
        Item item = null;
        if (isDrama()) {
            String url = mItem.item_url == null ? mSimpleRestClient.root_url
                    + "/api/item/" + mItem.pk + "/"
                    : mItem.item_url;
            if (SimpleRestClient.isLogin())
                mHistory = DaisyUtils.getHistoryManager(this).getHistoryByUrl(
                        url, "yes");
            else
                mHistory = DaisyUtils.getHistoryManager(this).getHistoryByUrl(
                        url, "no");


            Item[] items = mItem.subitems;
            int count = items.length;
            if (mHistory == null) {
                for (int i = 0; i < count; i++) {
                    if (mItem.subitems[i].clip.pk == mItem.clip.pk) {
                        item = mItem.subitems[i];
                        break;
                    }
                }
            } else {
                for (int i = 0; i < count; i++) {
                    if (mItem.subitems[i].url.equals(mHistory.sub_url)) {
                        item = mItem.subitems[i];
                        break;
                    }
                }
            }
        }

        return item;
    }

    private void initLayout() {
		mDetailTitle.setVisibility(View.VISIBLE);
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
//        Item item = getnewestItem(newpk);
//        if(item!=null){
//            mLeftBtn.setText(item.title);
//        }
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
            mDetailAttributeContainer.buildAttributeListOnZY(attributeMap, mContentModel);
        }
        // Set the content to Introduction View
        mDetailIntro.setText("简介 : " + mItem.description);
        if (mItem.poster_url != null) {
            mDetailPreviewImg.setTag(mItem.poster_url);
            mDetailPreviewImg.setUrl(mItem.poster_url);
        }
        if (isFavorite()) {
            //mCollectBtn.setBackgroundResource(R.drawable.collected_btn_bg_selector);
            mCollectBtn.setText(getResources().getString(R.string.favorited));
        } else {
            //mCollectBtn.setBackgroundResource(R.drawable.collect_btn_bg_selector);
            mCollectBtn.setText(getResources().getString(R.string.favorite));
        }
        mGetRelatedTask = new GetRelatedTask();
        mGetRelatedTask.execute();

        isInitialized = true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if (resultCode == 20) {
            if (data.getBooleanExtra("result", false)) {
                isBuy = true;
                setExpenseStatus();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean ret = super.onKeyDown(keyCode, event);
		if("lcd_s3a01".equals(VodUserAgent.getModelName())){
			if(keyCode == 707 || keyCode == 774 || keyCode ==253){
				isneedpause = false;
			}
		}else{
			if(keyCode == 223 || keyCode == 499 || keyCode ==480){
				isneedpause = false;
			}
		}
		return ret;
	}

	  private View.OnHoverListener onHoverListener = new View.OnHoverListener() {

			@Override
			public boolean onHover(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_HOVER_ENTER:
				case MotionEvent.ACTION_HOVER_MOVE:
					v.setFocusable(true);
					v.setFocusableInTouchMode(true);
					v.requestFocus();
					break;
				case MotionEvent.ACTION_HOVER_EXIT:
					break;
				}
				return false;
			}
		};

}
