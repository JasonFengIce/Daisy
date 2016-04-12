package tv.ismar.daisy.ui.activity.newvip;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;

import cn.ismartv.tvhorizontalscrollview.TvHorizontalScrollView;
import retrofit2.Callback;
import retrofit2.Response;
import tv.ismar.daisy.R;
import tv.ismar.daisy.core.client.NewVipHttpApi;
import tv.ismar.daisy.core.client.NewVipHttpManager;
import tv.ismar.daisy.data.http.newvip.PayLayerEntity;

/**
 * Created by huaijie on 4/11/16.
 */
public class PayActivity extends Activity {
    private ImageView leftArrow;
    private ImageView rightArrow;
    private LinearLayout scrollViewLayout;
    private TvHorizontalScrollView mTvHorizontalScrollView;

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
        scrollViewLayout = (LinearLayout) findViewById(R.id.pay_scrollview);
        mTvHorizontalScrollView = (TvHorizontalScrollView) findViewById(R.id.tvhorizontalscrollview);
        mTvHorizontalScrollView.setLeftArrow(leftArrow);
        mTvHorizontalScrollView.setRightArrow(rightArrow);
    }


    public void payLayer() {
        NewVipHttpManager.getInstance().resetAdapter_SKY.create(NewVipHttpApi.PayLayer.class).doRequest("675305").enqueue(new Callback<PayLayerEntity>() {
            @Override
            public void onResponse(Response<PayLayerEntity> response) {

            }

            @Override
            public void onFailure(Throwable t) {

            }
        });
    }

    private void fillLayout(PayLayerEntity payLayerEntity) {
        scrollViewLayout.removeAllViews();


    }
}
