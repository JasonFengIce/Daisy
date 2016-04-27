package tv.ismar.daisy.ui.activity.newvip;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.foregroundimageview.ForegroundImageView;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import cn.ismartv.tvhorizontalscrollview.TvHorizontalScrollView;
import okhttp3.ResponseBody;
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
import tv.ismar.daisy.views.RotateTextView;

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

    private Button purchaseBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paylayerpackage);
        initViews();
        Intent intent = getIntent();
        String packageId = intent.getStringExtra("package_id");
        orderCheck(packageId);
        payLayerPackage(packageId);

    }

    private void initViews() {
        mTvHorizontalScrollView = (TvHorizontalScrollView) findViewById(R.id.scroll_view);
        scrollViewLayout = (LinearLayout) findViewById(R.id.scroll_layout);
        leftArrow = (ImageView) findViewById(R.id.left_arrow);
        rightArrow = (ImageView) findViewById(R.id.right_arrow);
        mTvHorizontalScrollView.setLeftArrow(leftArrow);
        mTvHorizontalScrollView.setRightArrow(rightArrow);
        mTvHorizontalScrollView.setCoverOffset(20);
        tmp = (ImageView) findViewById(R.id.tmp);

        title = (TextView) findViewById(R.id.title);
        price = (TextView) findViewById(R.id.price);
        duration = (TextView) findViewById(R.id.duration);
        decription = (TextView) findViewById(R.id.description);
        purchaseBtn = (Button) findViewById(R.id.paylayerpkg_purchase);
        purchaseBtn.setOnHoverListener(this);
        purchaseBtn.setOnFocusChangeListener(this);
        purchaseBtn.requestFocus();

        leftArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTvHorizontalScrollView.pageScroll(View.FOCUS_LEFT);
            }
        });

        rightArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTvHorizontalScrollView.pageScroll(View.FOCUS_RIGHT);
            }
        });
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
            RelativeLayout itemView = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.item_paylayerpackage, null);
            ForegroundImageView imageView = (ForegroundImageView) itemView.findViewById(R.id.image);
            TextView itemTitle = (TextView) itemView.findViewById(R.id.title);
            itemTitle.setText(itemList.getTitle());
            RotateTextView expense_txt= (RotateTextView) itemView.findViewById(R.id.expense_txt);
            expense_txt.setDegrees(315);
            if(itemList.getCptitle()!=null&&""!=itemList.getCptitle()){
                expense_txt.setText(itemList.getCptitle());
                if(itemList.getCptitle().startsWith("视云")){
                    expense_txt.setBackgroundResource(R.drawable.list_ismar);
                }else if(itemList.getCptitle().startsWith("奇异果")){
                    expense_txt.setBackgroundResource(R.drawable.list_lizhi);
                }else{
                    expense_txt.setBackgroundResource(R.drawable.list_single_buy);
                }
            }

            if (TextUtils.isEmpty(itemList.getVertical_url())) {
                Picasso.with(this).load(R.drawable.preview).into(imageView);
            } else {
                Picasso.with(this).load(itemList.getVertical_url()).into(imageView);
            }

            itemView.setOnFocusChangeListener(new OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    View itemContainer = v.findViewById(R.id.item_container);
                    View itemTitle = v.findViewById(R.id.title);
                    if (hasFocus) {
                        itemContainer.setSelected(true);
                        itemTitle.setSelected(true);
                    } else {
                        itemContainer.setSelected(false);
                        itemTitle.setSelected(false);
                    }

                }
            });
            if(!purchaseBtn.isFocusable()){
                itemView.setNextFocusUpId(R.id.pay_layer_item);
            }else{
                itemView.setNextFocusUpId(purchaseBtn.getId());
            }
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

    public void buyPackage(View view) {
        buyVideo(entity.getPk(), entity.getType(), entity.getPrice());
    }

    private void orderCheck(String pkg) {
        NewVipHttpManager.getInstance().resetAdapter_SKY.create(NewVipHttpApi.OrderCheck.class).doRequest(null, pkg, null,
                SimpleRestClient.device_token, SimpleRestClient.access_token).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Response<ResponseBody> response) {
                if (response.errorBody() == null) {
                    try {
                        String result = response.body().string();
                        if (!result.equals("0")) {
                            purchaseBtn.setText("已购买");
                            purchaseBtn.setEnabled(false);
                            purchaseBtn.setFocusable(false);

                        } else {
                            purchaseBtn.setText("购买");
                            purchaseBtn.setEnabled(true);
                            purchaseBtn.setFocusable(true);

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(TAG, "order check :" + t.getMessage());
            }
        });
    }
}
