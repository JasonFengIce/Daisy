package tv.ismar.daisy;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import tv.ismar.daisy.views.FullChannelFragment;

/**
 * Created by admin on 2016/6/7.
 */
public class FullChannelActivity extends BaseActivity {

    private View contentView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_channel);
        contentView = LayoutInflater.from(this).inflate(R.layout.activity_full_channel, null);
        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
        FullChannelFragment fullChannelFragment=new FullChannelFragment();
        transaction.replace(R.id.full_channel_container,fullChannelFragment).commit();
    }

}
