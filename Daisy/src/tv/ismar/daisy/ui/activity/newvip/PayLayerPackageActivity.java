package tv.ismar.daisy.ui.activity.newvip;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import cn.ismartv.tvhorizontalscrollview.TvHorizontalScrollView;
import retrofit2.Callback;
import retrofit2.Response;
import tv.ismar.daisy.BaseActivity;
import tv.ismar.daisy.R;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.client.NewVipHttpApi;
import tv.ismar.daisy.core.client.NewVipHttpManager;
import tv.ismar.daisy.data.http.newvip.paylayerpackage.Item_list;
import tv.ismar.daisy.data.http.newvip.paylayerpackage.PayLayerPackageEntity;
import tv.ismar.daisy.models.Expense;
import tv.ismar.daisy.models.Item;
import tv.ismar.daisy.utils.ViewScaleUtil;
import tv.ismar.daisy.views.PaymentDialog;

/**
 * Created by huaijie on 4/12/16.
 */
public class PayLayerPackageActivity extends BaseActivity implements View.OnHoverListener, OnFocusChangeListener {

    private static final String TAG = "PayLayerPackageActivity";

    private ImageView tmp;
    private TvHorizontalScrollView mTvHorizontalScrollView;
    private LinearLayout scrollViewLayout;
    private ImageView leftArrow;
    private ImageView rightArrow;

    private TextView title;
    private TextView price;
    private TextView duration;
    private TextView decription;

    private PayLayerPackageEntity entity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paylayerpackage);
        initViews();
        Intent intent = getIntent();
        String packageId = intent.getStringExtra("package_id");
        payLayerPackage(packageId);
    }

    private void initViews() {
        mTvHorizontalScrollView = (TvHorizontalScrollView) findViewById(R.id.scroll_view);
        scrollViewLayout = (LinearLayout) findViewById(R.id.scroll_layout);
        leftArrow = (ImageView) findViewById(R.id.left_arrow);
        rightArrow = (ImageView) findViewById(R.id.right_arrow);
        mTvHorizontalScrollView.setLeftArrow(leftArrow);
        mTvHorizontalScrollView.setRightArrow(rightArrow);
        tmp = (ImageView) findViewById(R.id.tmp);

        title = (TextView) findViewById(R.id.title);
        price = (TextView) findViewById(R.id.price);
        duration = (TextView) findViewById(R.id.duration);
        decription = (TextView) findViewById(R.id.description);
    }

    private void payLayerPackage(String packageId) {

        NewVipHttpManager.getInstance().resetAdapter_SKY.create(NewVipHttpApi.PayLayerPack.class).doRequest(packageId, SimpleRestClient.device_token).enqueue(new Callback<PayLayerPackageEntity>() {
            @Override
            public void onResponse(Response<PayLayerPackageEntity> response) {
                if (response.errorBody() == null) {
                    entity = response.body();
                    title.setText("名称 : " + entity.getTitle());
                    price.setText("金额 : " + entity.getPrice() + "元");
                    duration.setText("有效期 : " + entity.getDuration() + "天");
                    decription.setText("说明 : " + entity.getDescription());
                    fillLayout(entity);
                }
            }

            @Override
            public void onFailure(Throwable t) {

            }
        });
    }


    private void fillLayout(final PayLayerPackageEntity packageEntity) {
        scrollViewLayout.removeAllViews();
        int margin = (int) getResources().getDimension(R.dimen.newvip_paylayervip_margin);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(margin, 0, margin, 0);
        for (final Item_list itemList : packageEntity.getItem_list()) {
            RelativeLayout itemView = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.item_paylayervip, null);
            ImageView imageView = (ImageView) itemView.findViewById(R.id.image);
            Picasso.with(this).load(itemList.getVertical_url()).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).into(imageView);
            itemView.setOnFocusChangeListener(this);
            itemView.setOnHoverListener(this);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    buyVideo(packageEntity.getPk(), packageEntity.getType(), packageEntity.getPrice());
                }
            });
            scrollViewLayout.addView(itemView, layoutParams);
        }
    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_HOVER_ENTER:
            case MotionEvent.ACTION_HOVER_MOVE:
                v.requestFocusFromTouch();
                v.requestFocus();
                break;
            case MotionEvent.ACTION_HOVER_EXIT:
                tmp.requestFocus();
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            ViewScaleUtil.zoomin_1_15(v);
        } else {
            ViewScaleUtil.zoomout_1_15(v);
        }
    }

    private void buyVideo(int pk, String type, float price) {
        PaymentDialog dialog = new PaymentDialog(this, R.style.PaymentDialog, new PaymentDialog.OrderResultListener() {
            @Override
            public void payResult(boolean result) {
                if (result) {
                    finish();
                }
            }
        });
        Item mItem = new Item();
        mItem.pk = pk;
        Expense expense = new Expense();
        expense.price = price;
        mItem.expense = expense;
        mItem.model_name = type;
        dialog.setItem(mItem);
        dialog.show();
    }

    public void buyPackage(View view) {
        buyVideo(entity.getPk(), entity.getType(), entity.getPrice());
    }
}
