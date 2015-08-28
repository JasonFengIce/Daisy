package tv.ismar.daisy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.Toast;
import com.tencent.msdk.api.LoginRet;
import com.tencent.msdk.api.MsdkBaseInfo;
import com.tencent.msdk.api.WGPlatform;
import com.tencent.msdk.api.WGQZonePermissions;
import com.tencent.msdk.consts.CallbackFlag;
import com.tencent.msdk.consts.EPlatform;
import com.tencent.msdk.remote.api.PersonInfo;
import com.tencent.msdk.tools.Logger;
import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.client.IsmartvUrlClient;

import java.util.HashMap;

public class BaseActivity extends FragmentActivity {

    public static final String ACTION_CONNECT_ERROR = "tv.ismar.daisy.CONNECT_ERROR";

    private ConnectionErrorReceiver connectionErrorReceiver;
    private IntentFilter intentFilter;

    private PopupWindow netErrorPopupWindow;
    private boolean isFirstLogin = false;
    private boolean isinitMSDK = false;
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

        createNetErrorPopup();

    }
    public void setIsinitMSDK(boolean isInit){
        this.isinitMSDK = isInit;
    }
   public void init(){

       MsdkBaseInfo baseInfo = new MsdkBaseInfo();
       baseInfo.qqAppId = "100703379";
       baseInfo.qqAppKey = "4578e54fb3a1bd18e0681bc1c734514e";


       // baseInfo.wxAppId = "wxcde873f99466f74a";

       baseInfo.msdkKey = "5d1467a4d2866771c3b289965db335f4";
       baseInfo.offerId = "100703379";
       WGPlatform.WGSetObserver(new MsdkCallback(this));
       // 应用宝更新回调类，游戏自行实现
       WGPlatform.WGSetSaveUpdateObserver(new SaveUpdateDemoObserver());
       // 广告的回调设置
       WGPlatform.WGSetADObserver(new MsdkADCallback());
       //QQ 加群加好友回调
       WGPlatform.WGSetGroupObserver(new MsdkGroupCallback());
       // 注意：传入Initialized的activity即this，在游戏运行期间不能被销毁，否则会产生Crash
       WGPlatform.Initialized(this, baseInfo);
       // 设置拉起QQ时候需要用户授权的项
       WGPlatform.WGSetPermission(WGQZonePermissions.eOPEN_ALL);

       if (WGPlatform.wakeUpFromHall(this.getIntent())) {
           // 拉起平台为大厅
           Logger.d("LoginPlatform is Hall");
           Logger.d(this.getIntent());
       } else {
           // 拉起平台不是大厅
           Logger.d("LoginPlatform is not Hall");
           Logger.d(this.getIntent());
           WGPlatform.handleCallback(this.getIntent());
       }

       //isFirstLogin = true;
   }

//    @Override
//    protected void onResume() {
//        super.onResume();
//
//        if(isinitMSDK){
//            WGPlatform.onResume();
//
//            // TODO GAME 模拟游戏自动登录，这里需要游戏添加加载动画
//            // WGLogin是一个异步接口, 传入ePlatform_None则调用本地票据验证票据是否有效
//            // 如果从未登录过，则会立即在onLoginNotify中返回flag为eFlag_Local_Invalid，此时应该拉起授权界面
//            // 建议在此时机调用WGLogin,它应该在handlecallback之后进行调用。
//            if(isFirstLogin) {
//                isFirstLogin = false;
//                WGPlatform.WGLogin(EPlatform.ePlatform_None);
//            }
//        }
//
//        registerBroadcastReceiver();
//    }
//    @Override
//    protected void onRestart() {
//        super.onRestart();
//        if(isinitMSDK)
//            WGPlatform.onRestart();
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        if(isinitMSDK)
//          WGPlatform.onStop();
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        if(isinitMSDK)
//          WGPlatform.onPause();
//        unRegisterBroadcastReceiver();
//
//    }
//
//    // TODO GAME 在onActivityResult中需要调用WGPlatform.onActivityResult
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if(isinitMSDK)
//           WGPlatform.onActivityResult(requestCode, resultCode, data);
//    }
//
//    // TODO GAME 在onNewIntent中需要调用handleCallback将平台带来的数据交给MSDK处理
//    @Override
//    protected void onNewIntent(Intent intent) {
//        Logger.d("onNewIntent");
//        super.onNewIntent(intent);
//        if(isinitMSDK){
//            // TODO GAME 处理游戏被拉起的情况
//            // launchActivity的onCreat()和onNewIntent()中必须调用
//            // WGPlatform.handleCallback()。否则会造成微信登录无回调
//            if (WGPlatform.wakeUpFromHall(intent)) {
//                Logger.d("LoginPlatform is Hall");
//                Logger.d(intent);
//            } else {
//                Logger.d("LoginPlatform is not Hall");
//                Logger.d(intent);
//                WGPlatform.handleCallback(intent);
//            }
//        }
//
//    }
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if(isinitMSDK)
//           WGPlatform.onDestory(this);
//    }

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

    private void createNetErrorPopup() {
        View contentView = LayoutInflater.from(this).inflate(R.layout.popup_net_error, null);
        netErrorPopupWindow = new PopupWindow(null, 740, 341);
        netErrorPopupWindow.setContentView(contentView);
        netErrorPopupWindow.setFocusable(true);
        netErrorPopupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.transparent));
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

    private void showNetErrorPopup() {
        netErrorPopupWindow.showAtLocation(netErrorPopupWindow.getContentView(), Gravity.CENTER, 0, 0);

    }

    public interface OnLoginCallback {
        void onLoginSuccess(String result);
        void onLoginFailed();
        void oncallWGQueryQQUserInfo(PersonInfo info);
    }
    OnLoginCallback loginCallback;
    public void setLoginCallback(OnLoginCallback loginCallback) {
        this.loginCallback = loginCallback;
    }

    public void letUserLogin(String token,String refresh_token,String openid){

        String api = SimpleRestClient.root_url + "/accounts/oauth_login/";
        HashMap params = new HashMap();
        params.put("device_token", SimpleRestClient.device_token);
        params.put("app_id","100703379");
        params.put("open_id",openid);
        params.put("refresh_token",refresh_token);
        params.put("kind","qq");
        params.put("token",token);
        new IsmartvUrlClient().doRequest(IsmartvUrlClient.Method.POST, api, params, new IsmartvUrlClient.CallBack() {
            @Override
            public void onSuccess(String result) {

                if (loginCallback != null) {
                    loginCallback.onLoginSuccess(result);
                }
            }

            @Override
            public void onFailed(Exception exception) {
                //  verificationPrompt.setText(R.string.login_failure);
                Log.i("pangziinfo","shibai");
                if(loginCallback!=null){
                    loginCallback.onLoginFailed();
                }
            }
        });

    }

    // 获取当前登录平台
    public EPlatform getPlatform() {
        LoginRet ret = new LoginRet();
        WGPlatform.WGGetLoginRecord(ret);
        if (ret.flag == CallbackFlag.eFlag_Succ) {
            return EPlatform.getEnum(ret.platform);
        }
        return EPlatform.ePlatform_None;
    }

    public void loginQQorWX(){
        WGPlatform.WGLogin(EPlatform.ePlatform_QQ);
    }
    public void changaccount(){

            // 如已登录直接进入相应模块视图
            //startModule();
          //  WGPlatform.WGLogout();
        WGPlatform.WGLogin(EPlatform.ePlatform_QQ);

    }

    public void callWGQueryQQUserInfo() {
        WGPlatform.WGQueryQQMyInfo();
    }

    public void getWGQueryQQUserInfo(PersonInfo info){
         if(loginCallback != null){
             loginCallback.oncallWGQueryQQUserInfo(info);
         }
    }
}
