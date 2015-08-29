package tv.ismar.daisy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import com.tencent.connect.UserInfo;
import com.tencent.connect.common.Constants;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import org.json.JSONException;
import org.json.JSONObject;
import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.client.IsmartvUrlClient;

import java.util.HashMap;

public class BaseActivity extends FragmentActivity {

    public static final String ACTION_CONNECT_ERROR = "tv.ismar.daisy.CONNECT_ERROR";

    private ConnectionErrorReceiver connectionErrorReceiver;
    private IntentFilter intentFilter;

    private PopupWindow netErrorPopupWindow;
    Tencent mTencent;
    String APP_ID = "1104828726";
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
        mTencent = Tencent.createInstance(APP_ID, getApplicationContext());
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
        void oncallWGQueryQQUserInfo(String info);

    }
    OnLoginCallback loginCallback;
    public void setLoginCallback(OnLoginCallback loginCallback) {
        this.loginCallback = loginCallback;
    }

    public void letUserLogin(String token,String refresh_token,String openid){

        String api = SimpleRestClient.root_url + "/accounts/oauth_login/";
        HashMap params = new HashMap();
        params.put("device_token", SimpleRestClient.device_token);
        params.put("app_id","1104828726");
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

    public void loginQQorWX(){
        mTencent.login(BaseActivity.this, "all", loginListener);
    }
    public void changaccount(){
        mTencent.logout(BaseActivity.this);
        mTencent.login(BaseActivity.this, "all", loginListener);

    }

    public void callWGQueryQQUserInfo() {
        UserInfo mInfo = new UserInfo(BaseActivity.this, mTencent.getQQToken());
        mInfo.getUserInfo(getUserInfoListener);
    }

    public void getWGQueryQQUserInfo(String nickname){

         if(loginCallback!=null){
             loginCallback.oncallWGQueryQQUserInfo(nickname);
         }
    }



    public  void initOpenidAndToken(JSONObject jsonObject) {
        try {
            String token = jsonObject.getString(Constants.PARAM_ACCESS_TOKEN);
            String expires = jsonObject.getString(Constants.PARAM_EXPIRES_IN);
            String openId = jsonObject.getString(Constants.PARAM_OPEN_ID);
            String paytoken="";
            if(jsonObject.has("pay_token")){
                paytoken = jsonObject.getString("pay_token");
            }
            if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(expires)
                    && !TextUtils.isEmpty(openId)) {
                mTencent.setAccessToken(token, expires);
                mTencent.setOpenId(openId);
                letUserLogin(token,paytoken,openId);
            }
        } catch(Exception e) {
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // mTencent.onActivityResult(requestCode, resultCode, data);

        // Log.d(TAG, "-->onActivityResult " + requestCode  + " resultCode=" + resultCode);
        if(loginListener!=null){
            mTencent.onActivityResultData(requestCode,resultCode,data,loginListener);
            if(requestCode == Constants.REQUEST_API) {
                if(resultCode == Constants.RESULT_LOGIN) {
                    Tencent.handleResultData(data, loginListener);
                    //  Log.d(TAG, "-->onActivityResult handle logindata");
                }
            } else if (requestCode == Constants.REQUEST_APPBAR) { //app内应用吧登录
                if (resultCode == Constants.RESULT_LOGIN) {
                    // updateUserInfo();
                    // updateLoginButton();
                    //  Util.showResultDialog(MainActivity.this, data.getStringExtra(Constants.LOGIN_INFO), "登录成功");
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    IUiListener loginListener = new BaseUiListener() {
        @Override
        protected void doComplete(Object values) {
            Log.i("zhangjiqiangfuck","getUserInfoListener");
            if(null == values){
                //获取失败
                return;
            }
            JSONObject jsonResponse = (JSONObject) values;
            if(null != jsonResponse && jsonResponse.length() == 0){
                //失败
                return;
            }
            if(jsonResponse.has(Constants.PARAM_OPEN_ID)){
                initOpenidAndToken(jsonResponse);
                Log.i("zhangjiqiangfuck", "AuthorSwitch_SDK:" + "doComplete"+"values=="+values.toString());
            }
        }
    };
    IUiListener getUserInfoListener = new BaseUiListener(){
        @Override
        protected void doComplete(Object values) {
            Log.i("zhangjiqiangfuck","getUserInfoListener");
            if(null == values){
                //获取失败
                return;
            }
            JSONObject jsonResponse = (JSONObject) values;
            if(null != jsonResponse && jsonResponse.length() == 0){
                //失败
                return;
            }
            Log.i("zhangjiqiangfuck","info=="+jsonResponse.toString());
            if(jsonResponse.has("nickname")){
                try {
                    getWGQueryQQUserInfo(jsonResponse.getString("nickname"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };
    private class BaseUiListener implements IUiListener {

        @Override
        public void onComplete(Object response) {
            Log.i("zhangjiqiangfuck","onComplete");
            doComplete(response);
        }

        protected void doComplete(Object values) {

        }

        @Override
        public void onError(UiError e) {
            Log.i("zhangjiqiangfuck","onError");
        }

        @Override
        public void onCancel() {
            Log.i("zhangjiqiangfuck","onCancel");
        }
    }


}
