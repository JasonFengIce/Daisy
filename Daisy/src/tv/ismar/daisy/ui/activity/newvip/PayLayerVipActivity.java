package tv.ismar.daisy.ui.activity.newvip;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnHoverListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.squareup.picasso.Picasso;

import cn.ismartv.tvhorizontalscrollview.TvHorizontalScrollView;
import retrofit2.Callback;
import retrofit2.Response;
import tv.ismar.daisy.BaseActivity;
import tv.ismar.daisy.R;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.client.NewVipHttpApi;
import tv.ismar.daisy.core.client.NewVipHttpManager;
import tv.ismar.daisy.data.http.newvip.paylayervip.PayLayerVipEntity;
import tv.ismar.daisy.data.http.newvip.paylayervip.Vip_list;
import tv.ismar.daisy.models.Expense;
import tv.ismar.daisy.models.Item;
import tv.ismar.daisy.utils.ViewScaleUtil;
import tv.ismar.daisy.views.PaymentDialog;

/**
 * Created by huaijie on 4/12/16.
 */
public class PayLayerVipActivity extends BaseActivity implements OnHoverListener, View.OnFocusChangeListener {
    private ImageView tmp;
    private TvHorizontalScrollView mTvHorizontalScrollView;
    private LinearLayout scrollViewLayout;
    private ImageView leftArrow;
    private ImageView rightArrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paylayervip);
        initViews();
        Intent intent = getIntent();
        String cpid = intent.getStringExtra("cpid");
        payLayerVip(cpid);
    }

    private void initViews() {
        mTvHorizontalScrollView = (TvHorizontalScrollView) findViewById(R.id.scroll_view);
        scrollViewLayout = (LinearLayout) findViewById(R.id.scroll_layout);
        leftArrow = (ImageView) findViewById(R.id.left_arrow);
        rightArrow = (ImageView) findViewById(R.id.right_arrow);
        mTvHorizontalScrollView.setLeftArrow(leftArrow);
        mTvHorizontalScrollView.setRightArrow(rightArrow);
        tmp = (ImageView) findViewById(R.id.tmp);
    }


    private void payLayerVip(String cpid) {
        NewVipHttpManager.getInstance().resetAdapter_SKY.create(NewVipHttpApi.PayLayerVip.class).doRequest(cpid, SimpleRestClient.device_token).enqueue(new Callback<PayLayerVipEntity>() {
            @Override
            public void onResponse(Response<PayLayerVipEntity> response) {
                if (response.errorBody() == null)
                    fillLayout(response.body());
            }

            @Override
            public void onFailure(Throwable t) {

            }
        });
    }


    private void fillLayout(final PayLayerVipEntity payLayerVipEntity) {
        scrollViewLayout.removeAllViews();
        int margin = (int) getResources().getDimension(R.dimen.newvip_paylayervip_margin);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(margin, 0, margin, 0);
        for (final Vip_list vipList : payLayerVipEntity.getVip_list()) {
            RelativeLayout itemView = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.item_paylayervip, null);
            ImageView imageView = (ImageView) itemView.findViewById(R.id.image);
            Picasso.with(this).load(vipList.getVertical_url()).into(imageView);
            itemView.setOnFocusChangeListener(this);
            itemView.setOnHoverListener(this);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    buyVideo(vipList.getPk(), payLayerVipEntity.getType(), vipList.getPrice());
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
}