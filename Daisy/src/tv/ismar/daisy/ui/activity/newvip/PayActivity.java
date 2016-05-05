package tv.ismar.daisy.ui.activity.newvip;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.foregroundimageview.ForegroundImageView;
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
import tv.ismar.daisy.data.http.newvip.paylayer.Expense_item;
import tv.ismar.daisy.data.http.newvip.paylayer.Package;
import tv.ismar.daisy.data.http.newvip.paylayer.PayLayerEntity;
import tv.ismar.daisy.data.http.newvip.paylayer.Vip;
import tv.ismar.daisy.models.Expense;
import tv.ismar.daisy.models.Item;
import tv.ismar.daisy.utils.PicassoUtils;
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
        Intent intent = getIntent();
        String itemId = intent.getStringExtra("item_id");
        payLayer(itemId);
//        payLayer("675300");
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
    //675322
    public void payLayer(String itemId) {
        NewVipHttpManager.getInstance().resetAdapter_SKY.create(NewVipHttpApi.PayLayer.class).doRequest(itemId, SimpleRestClient.device_token).enqueue(new Callback<PayLayerEntity>() {
            @Override
            public void onResponse(Response<PayLayerEntity> response) {
                if (response.errorBody() == null)
                    fillLayout(response.body());
            }

            @Override
            public void onFailure(Throwable t) {

            }
        });
    }

    private void fillLayout(final PayLayerEntity payLayerEntity) {
        scrollViewLayout.removeAllViews();
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int margin = (int) getResources().getDimension(R.dimen.newip_payitem_margin);
        layoutParams.setMargins(margin, 0, margin, 0);
        Vip vip = payLayerEntity.getVip();
        if (vip != null) {
            RelativeLayout vipItem;
            if(payLayerEntity.getCpname().startsWith("ismar")) {
                vipItem = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.item_daisy_vip_pay, null);
            }else{
                vipItem = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.item_newvip_pay, null);
            }
            ForegroundImageView imageView = (ForegroundImageView) vipItem.findViewById(R.id.item_newvip_pay_img);
            TextView title = (TextView) vipItem.findViewById(R.id.title);
            title.setText(vip.getTitle());
            TextView price = (TextView) vipItem.findViewById(R.id.price);
            price.setText((int)vip.getPrice() + "元/" + vip.getDuration() + "天");
            if (TextUtils.isEmpty(vip.getVertical_url())) {
                Picasso.with(this).load(R.drawable.preview).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).into(imageView);
            } else {

                Picasso.with(this).load(vip.getVertical_url()).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).error(R.drawable.error_ver).into(imageView);
            }
            vipItem.setOnHoverListener(this);
            vipItem.setOnFocusChangeListener(this);
            vipItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.putExtra("cpid", String.valueOf(payLayerEntity.getCpid()));
                    intent.setClass(PayActivity.this, PayLayerVipActivity.class);
                    startActivityForResult(intent,20);
                }
            });
            scrollViewLayout.addView(vipItem, layoutParams);
        }

        final Expense_item expenseItem = payLayerEntity.getExpense_item();
        if (expenseItem != null) {
            RelativeLayout item = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.item_newvip_pay, null);
            ImageView imageView = (ImageView) item.findViewById(R.id.item_newvip_pay_img);
            if (TextUtils.isEmpty(expenseItem.getVertical_url())) {
                Picasso.with(this).load(R.drawable.preview).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).into(imageView);
            } else {

                Picasso.with(this).load(expenseItem.getVertical_url()).error(R.drawable.error_ver).into(imageView);
            }
            TextView title = (TextView) item.findViewById(R.id.title);
            title.setText(expenseItem.getTitle());
            TextView price = (TextView) item.findViewById(R.id.price);
            price.setText((int)expenseItem.getPrice()+ "元/" + expenseItem.getDuration() + "天");
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

        final Package newVipPackage = payLayerEntity.getPkage();
        if (newVipPackage != null) {
            RelativeLayout item = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.item_newvip_pay, null);
            ImageView imageView = (ImageView) item.findViewById(R.id.item_newvip_pay_img);
            if (TextUtils.isEmpty(newVipPackage.getVertical_url())) {
                Picasso.with(this).load(R.drawable.preview).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).into(imageView);
            } else {
                Picasso.with(this).load(newVipPackage.getVertical_url()).error(R.drawable.error_ver).into(imageView);
            }
            TextView title = (TextView) item.findViewById(R.id.title);
            title.setText(newVipPackage.getTitle());
            TextView price = (TextView) item.findViewById(R.id.price);
            price.setText((int)newVipPackage.getPrice() + "元/" + newVipPackage.getDuration() + "天");
            item.setOnHoverListener(this);
            item.setOnFocusChangeListener(this);
            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.putExtra("package_id", String.valueOf(newVipPackage.getPackage_pk()));
                    intent.setClass(PayActivity.this, PayLayerPackageActivity.class);
                    startActivityForResult(intent,20);
                }
            });
            scrollViewLayout.addView(item, layoutParams);
        }

        scrollViewLayout.getChildAt(0).requestFocus();
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
//        if (hasFocus) {
//            ViewScaleUtil.zoomin_1_15(v);
//        } else {
//            ViewScaleUtil.zoomout_1_15(v);
//        }
    }

    private void buyVideo(int pk, String type, float price) {
        PaymentDialog dialog = new PaymentDialog(this, R.style.PaymentDialog, new PaymentDialog.OrderResultListener() {
            @Override
            public void payResult(boolean result) {
                if (result) {
                    Intent data = new Intent();
                    data.putExtra("result", true);
                    setResult(20, data);
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



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if (resultCode == 20) {
            setResult(20, data);
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
