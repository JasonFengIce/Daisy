package tv.ismar.daisy.ui.activity.newvip;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnHoverListener;
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
import tv.ismar.daisy.data.http.newvip.paylayervip.PayLayerVipEntity;
import tv.ismar.daisy.data.http.newvip.paylayervip.Vip_list;
import tv.ismar.daisy.models.Expense;
import tv.ismar.daisy.models.Item;
import tv.ismar.daisy.utils.PicassoUtils;
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
    private TextView vipDescriptionTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paylayervip);
        initViews();
        Intent intent = getIntent();
        String cpid = intent.getStringExtra("cpid");
        String itemId = intent.getStringExtra("item_id");
        payLayerVip(cpid, itemId);
    }

    private void initViews() {
        mTvHorizontalScrollView = (TvHorizontalScrollView) findViewById(R.id.scroll_view);
        scrollViewLayout = (LinearLayout) findViewById(R.id.scroll_layout);
        leftArrow = (ImageView) findViewById(R.id.left_arrow);
        rightArrow = (ImageView) findViewById(R.id.right_arrow);

        vipDescriptionTextView = (TextView)findViewById(R.id.vip_des_text);
        mTvHorizontalScrollView.setLeftArrow(leftArrow);
        mTvHorizontalScrollView.setRightArrow(rightArrow);
        mTvHorizontalScrollView.setCoverOffset(10);
        tmp = (ImageView) findViewById(R.id.tmp);
    }


    private void payLayerVip(String cpid, String itemId) {
        NewVipHttpManager.getInstance().resetAdapter_SKY.create(NewVipHttpApi.PayLayerVip.class).doRequest(cpid, itemId, SimpleRestClient.device_token).enqueue(new Callback<PayLayerVipEntity>() {
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
//        if (payLayerVipEntity.getCpname().equals("iqiyi")) {
//            vipDescriptionTextView.setText(R.string.iqiyi_vip_des_content);
//        } else if (payLayerVipEntity.getCpname().equals("ismartv")) {
//            vipDescriptionTextView.setText(R.string.ismartv_vip_des_content);
//        }
        if (!payLayerVipEntity.getVip_list().isEmpty()) {
            vipDescriptionTextView.setText(payLayerVipEntity.getVip_list().get(0).getDescription());
        }
        scrollViewLayout.removeAllViews();
        int margin = (int) getResources().getDimension(R.dimen.newvip_paylayervip_margin);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(margin, 0, margin, 0);
        for (final Vip_list vipList : payLayerVipEntity.getVip_list()) {
            RelativeLayout itemView = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.item_paylayervip, null);
            ForegroundImageView imageView = (ForegroundImageView) itemView.findViewById(R.id.image);
            if (TextUtils.isEmpty(vipList.getVertical_url())) {
                Picasso.with(this).load(R.drawable.preview).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).into(imageView);
            } else {
                Picasso.with(this).load(vipList.getVertical_url()).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).error(R.drawable.error_ver).into(imageView);
            }

            TextView title = (TextView) itemView.findViewById(R.id.title);
//            title.setText(vipList.getTitle());
            TextView price = (TextView) itemView.findViewById(R.id.price);
//            price.setText((int)(vipList.getPrice()) + "元");
            TextView duration = (TextView) itemView.findViewById(R.id.duration);
//            duration.setText(vipList.getDuration() + "天");
            itemView.setTag(vipList);
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
        if (hasFocus) {
            Vip_list vipList = (Vip_list) v.getTag();
            vipDescriptionTextView.setText(vipList.getDescription());
        }
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
}
