package tv.ismar.daisy.ui.activity.newvip;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import cn.ismartv.tvhorizontalscrollview.TvHorizontalScrollView;
import retrofit2.Callback;
import retrofit2.Response;
import tv.ismar.daisy.BaseActivity;
import tv.ismar.daisy.R;
import tv.ismar.daisy.core.client.NewVipHttpApi;
import tv.ismar.daisy.core.client.NewVipHttpManager;
import tv.ismar.daisy.data.http.newvip.Expense_item;
import tv.ismar.daisy.data.http.newvip.PayLayerEntity;
import tv.ismar.daisy.data.http.newvip.Vip;
import tv.ismar.daisy.models.Expense;
import tv.ismar.daisy.models.Item;
import tv.ismar.daisy.utils.ViewScaleUtil;
import tv.ismar.daisy.views.PaymentDialog;

/**
 * Created by huaijie on 4/11/16.
 */
public class PayActivity extends BaseActivity implements View.OnHoverListener, View.OnFocusChangeListener {
    private ImageView leftArrow;
    private ImageView rightArrow;
    private LinearLayout scrollViewLayout;
    private TvHorizontalScrollView mTvHorizontalScrollView;
    private ImageView tmp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newvip_pay);
        initViews();
        payLayer();
    }

    private void initViews() {
        leftArrow = (ImageView) findViewById(R.id.left_arrow);
        rightArrow = (ImageView) findViewById(R.id.right_arrow);
        tmp = (ImageView) findViewById(R.id.tmp);
        scrollViewLayout = (LinearLayout) findViewById(R.id.pay_scrollview);
        mTvHorizontalScrollView = (TvHorizontalScrollView) findViewById(R.id.tvhorizontalscrollview);
        mTvHorizontalScrollView.setLeftArrow(leftArrow);
        mTvHorizontalScrollView.setRightArrow(rightArrow);
    }

    //675305
    //675302
    //675300
    public void payLayer() {
        NewVipHttpManager.getInstance().resetAdapter_SKY.create(NewVipHttpApi.PayLayer.class).doRequest("675300").enqueue(new Callback<PayLayerEntity>() {
            @Override
            public void onResponse(Response<PayLayerEntity> response) {
                fillLayout(response.body());
            }

            @Override
            public void onFailure(Throwable t) {

            }
        });
    }

    private void fillLayout(PayLayerEntity payLayerEntity) {
        scrollViewLayout.removeAllViews();
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int margin = (int) getResources().getDimension(R.dimen.newip_payitem_margin);
        layoutParams.setMargins(margin, 0, margin, 0);
        Vip vip = payLayerEntity.getVip();
        if (vip != null) {
            RelativeLayout vipItem = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.item_newvip_pay, null);
            ImageView imageView = (ImageView) vipItem.findViewById(R.id.item_newvip_pay_img);
            TextView title = (TextView) vipItem.findViewById(R.id.title);
            title.setText(vip.getTitle());
            TextView price = (TextView) vipItem.findViewById(R.id.price);
            price.setText(String.valueOf(vip.getPrice()));
            Picasso.with(this).load("http://res.tvxio.com/media/upload/20140922/bg1.jpg").into(imageView);
            vipItem.setOnHoverListener(this);
            vipItem.setOnFocusChangeListener(this);
            scrollViewLayout.addView(vipItem, layoutParams);
        }

        final Expense_item expenseItem = payLayerEntity.getExpense_item();
        if (expenseItem != null) {
            RelativeLayout item = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.item_newvip_pay, null);
            ImageView imageView = (ImageView) item.findViewById(R.id.item_newvip_pay_img);
            Picasso.with(this).load(expenseItem.getVertical_url()).into(imageView);
            TextView title = (TextView) item.findViewById(R.id.title);
            title.setText(expenseItem.getTitle());
            TextView price = (TextView) item.findViewById(R.id.price);
            price.setText(String.valueOf(expenseItem.getPrice()));
            item.setOnHoverListener(this);
            item.setOnFocusChangeListener(this);
            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    buyVideo(expenseItem.getPk(), expenseItem.getType(), expenseItem.getPrice());
                }
            });
            scrollViewLayout.addView(item, layoutParams);
        }

        tv.ismar.daisy.data.http.newvip.Package newVipPackage = payLayerEntity.getPkage();
        if (newVipPackage != null) {
            RelativeLayout item = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.item_newvip_pay, null);
            ImageView imageView = (ImageView) item.findViewById(R.id.item_newvip_pay_img);
            Picasso.with(this).load(newVipPackage.getVertical_url()).into(imageView);
            TextView title = (TextView) item.findViewById(R.id.title);
            title.setText(newVipPackage.getTitle());
            TextView price = (TextView) item.findViewById(R.id.price);
            price.setText(String.valueOf(newVipPackage.getPrice()));
            item.setOnHoverListener(this);
            item.setOnFocusChangeListener(this);
            scrollViewLayout.addView(item, layoutParams);
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
        PaymentDialog dialog = new PaymentDialog(this, R.style.PaymentDialog, ordercheckListener);
        Item mItem = new Item();
        mItem.pk = pk;
        Expense expense = new Expense();
        expense.price = price;
        mItem.expense = expense;
        mItem.model_name = type;
        dialog.setItem(mItem);
        dialog.show();
    }

    private PaymentDialog.OrderResultListener ordercheckListener = new PaymentDialog.OrderResultListener() {

        @Override
        public void payResult(boolean result) {
            if (result) {
                finish();
            }
        }
    };
}
