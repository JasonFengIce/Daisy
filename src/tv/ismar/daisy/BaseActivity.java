package tv.ismar.daisy;

import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.SimpleRestClient;

public class BaseActivity extends FragmentActivity {

    public static final String ACTION_CONNECT_ERROR = "tv.ismar.daisy.CONNECT_ERROR";

    private ConnectionErrorReceiver connectionErrorReceiver;
    private IntentFilter intentFilter;

    private PopupWindow netErrorPopupWindow;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (SimpleRestClient.root_url.equals("")) {
            SimpleRestClient.root_url = DaisyUtils.getVodApplication(this).getPreferences().getString(VodApplication.DOMAIN, "");
            SimpleRestClient.sRoot_url = DaisyUtils.getVodApplication(this).getPreferences().getString(VodApplication.DOMAIN, "");
            SimpleRestClient.ad_domain = DaisyUtils.getVodApplication(this).getPreferences().getString(VodApplication.ad_domain, "");
            SimpleRestClient.log_domain = DaisyUtils.getVodApplication(this).getPreferences().getString(VodApplication.LOG_DOMAIN, "");
            SimpleRestClient.device_token = DaisyUtils.getVodApplication(this).getPreferences().getString(VodApplication.DEVICE_TOKEN, "");
            SimpleRestClient.sn_token = DaisyUtils.getVodApplication(this).getPreferences().getString(VodApplication.SN_TOKEN, "");
            SimpleRestClient.mobile_number = DaisyUtils.getVodApplication(this).getPreferences().getString(VodApplication.MOBILE_NUMBER, "");
            SimpleRestClient.access_token = DaisyUtils.getVodApplication(this).getPreferences().getString(VodApplication.AUTH_TOKEN, "");
        }
        intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_CONNECT_ERROR);
        connectionErrorReceiver = new ConnectionErrorReceiver();

    }


    @Override
    protected void onResume() {
        super.onResume();
        registerBroadcastReceiver();
    }


    @Override
    protected void onPause() {
        super.onPause();
        unRegisterBroadcastReceiver();

    }

    private void registerBroadcastReceiver() {
        registerReceiver(connectionErrorReceiver, intentFilter);
    }

    private void unRegisterBroadcastReceiver() {
        unregisterReceiver(connectionErrorReceiver);
    }

    class ConnectionErrorReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            showNetErrorPopup();
        }
    }

    private void showNetErrorPopup() {
        View contentView = LayoutInflater.from(this).inflate(R.layout.popup_net_error, null);
        netErrorPopupWindow = new PopupWindow(null, 740, 341);
        netErrorPopupWindow.setContentView(contentView);
        netErrorPopupWindow.setFocusable(true);
        netErrorPopupWindow.showAtLocation(contentView, Gravity.CENTER, 0, 0);
        Button settingNetwork = (Button) contentView.findViewById(R.id.setting_network);
        Button iKnow = (Button) contentView.findViewById(R.id.i_know);

        settingNetwork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                BaseActivity.this.startActivity(intent);
            }
        });

        iKnow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                netErrorPopupWindow.dismiss();
            }
        });
    }
}
