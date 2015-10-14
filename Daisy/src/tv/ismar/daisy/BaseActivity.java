package tv.ismar.daisy;

import java.util.HashMap;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.client.IsmartvUrlClient;
import tv.ismar.daisy.exception.NetworkException;
import tv.ismar.daisy.ui.activity.TVGuideActivity;
import tv.ismar.sakura.ui.widget.MessagePopWindow;
import tv.ismar.sakura.utils.DeviceUtils;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;

import com.tencent.connect.UserInfo;
import com.tencent.connect.common.Constants;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

public class BaseActivity extends FragmentActivity {

    public static final String ACTION_CONNECT_ERROR = "tv.ismar.daisy.CONNECT_ERROR";

    private ConnectionErrorReceiver connectionErrorReceiver;
    private IntentFilter intentFilter;
    MessagePopWindow exitPopupWindow;
    private PopupWindow netErrorPopupWindow;
    Tencent mTencent;
    String APP_ID = "1104828726";
    protected String fromPage;
    protected String activityTag = "";
    private FetchBestTvAuth authTask;
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
        fromPage= getIntent().getStringExtra("fromPage");
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
        void onSameAccountListener();
        void onCancelLogin();
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
            	org.json.JSONObject json;
				try {
					json = new org.json.JSONObject(
							result);
				String auth_token = json
						.getString(VodApplication.AUTH_TOKEN);
				DaisyUtils
						.getVodApplication(getApplicationContext())
						.getEditor()
						.putString(
								VodApplication.AUTH_TOKEN,
								auth_token);
				DaisyUtils.getVodApplication(getApplicationContext())
						.save();
				SimpleRestClient.access_token = auth_token;
				} catch (JSONException e) {
					e.printStackTrace();
				}
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
        IntentFilter nFilter=new IntentFilter("com.tencent.gamestation.qrlogin");
        registerReceiver(mUserInfoReceiver, nFilter);
        mTencent.login(BaseActivity.this, "all", loginListener);
    }
    public void changaccount(){




        mTencent.logout(BaseActivity.this);

        IntentFilter nFilter=new IntentFilter("com.tencent.gamestation.qrlogin");
        registerReceiver(mUserInfoReceiver, nFilter);
        mTencent.login(BaseActivity.this, "all", loginListener);
    }

    public void callWGQueryQQUserInfo() {
        UserInfo mInfo = new UserInfo(BaseActivity.this, mTencent.getQQToken());
        mInfo.getUserInfo(getUserInfoListener);
    }

    public void getWGQueryQQUserInfo(String nickname){
          //if(!isSameAccount()){
              if(loginCallback!=null){
                  loginCallback.oncallWGQueryQQUserInfo(nickname);
              }
         // }
    }


    private String token="";
    private String openId="";
    private String paytoken="";
    public  void initOpenidAndToken(JSONObject jsonObject) {
        try {
             token = jsonObject.getString(Constants.PARAM_ACCESS_TOKEN);
            String expires = jsonObject.getString(Constants.PARAM_EXPIRES_IN);
             openId = jsonObject.getString(Constants.PARAM_OPEN_ID);

            if(jsonObject.has("pay_token")){
                paytoken = jsonObject.getString("pay_token");
            }
            if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(expires)
                    && !TextUtils.isEmpty(openId)) {
                mTencent.setAccessToken(token, expires);
                mTencent.setOpenId(openId);
               // DaisyUtils.getVodApplication(this).
//               String saveid = DaisyUtils.getVodApplication(BaseActivity.this).getPreferences().getString(VodApplication.OPENID,"");
//                if("".equals(saveid)||!saveid.equals(openId)){
//                    letUserLogin(token, paytoken, openId);
//                    DaisyUtils.getVodApplication(this).getEditor().putString(VodApplication.OPENID, openId);
//                    DaisyUtils.getVodApplication(this).save();
//                }else{
//                    if(loginCallback!=null){
//                        loginCallback.onSameAccountListener();
//                    }
//                }

            if(!isSameAccount()){
                DaisyUtils.getVodApplication(this).getEditor().putString(VodApplication.OPENID, openId);
                DaisyUtils.getVodApplication(this).save();
                letUserLogin(token, paytoken, openId);
            }else{
                    if(loginCallback!=null){
                        loginCallback.onSameAccountListener();
                    }
            }
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

            try {
                int ret = jsonResponse.getInt("ret");
                if (ret == 100030) {
                    UUID uuid = UUID.randomUUID();
                    String tmp = uuid.toString().substring(0,7);
                    getWGQueryQQUserInfo("qq用户"+tmp.replaceAll("-", ""));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if(jsonResponse.has("nickname")){
                try {
                    getWGQueryQQUserInfo(jsonResponse.getString("nickname"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };
    private boolean isSameAccount(){
        boolean isSameAccount = false;
        String saveid = DaisyUtils.getVodApplication(BaseActivity.this).getPreferences().getString(VodApplication.OPENID,"");
        if("".equals(saveid)||!saveid.equals(openId)){
            isSameAccount = false;

        }else{
            isSameAccount = true;
        }
        return isSameAccount;
    }
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
            if (loginCallback != null) {
                loginCallback.onCancelLogin();
            }
        }
    }


    public BroadcastReceiver mUserInfoReceiver=new BroadcastReceiver()
    {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            Log.e("XXXXXXXXX", intent.getAction());
            if("com.tencent.gamestation.qrlogin".equalsIgnoreCase(intent.getAction()))
            {
                String nNick=intent.getStringExtra("nick");
                String nIconUrl=intent.getStringExtra("imgurl");
                Log.e("XXXXXXXXXXXXXXXXXXXXXX", "user info nick="+nNick+" nIconUrl="+nIconUrl);
                getWGQueryQQUserInfo(nNick);
                unregisterReceiver(mUserInfoReceiver);
            }
        }

    };

    @Override
    protected void onPause() {
        super.onPause();


    }

    @Override
    protected void onResume() {
        super.onResume();
        String bindflag = DaisyUtils.getVodApplication(this)
				.getPreferences().getString(VodApplication.BESTTV_AUTH_BIND_FLAG, "");
        if (StringUtils.isEmpty(bindflag) || (StringUtils.isNotEmpty(bindflag) && "privilege".equals(bindflag))) {
			authTask = new FetchBestTvAuth();
			authTask.execute();
		}
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
      //  unregisterReceiver(mUserInfoReceiver);
    }

    @Override
    public void onBackPressed() {
		if ("launcher".equals(fromPage)
				&& StringUtils.isEmpty(activityTag)){
			showExitPopup(((ViewGroup) findViewById(android.R.id.content))
					.getChildAt(0),R.string.exit_prompt);
		}else{
			super.onBackPressed();
		}
		
    }

    private void showExitPopup(View view,int message) {
        exitPopupWindow = new MessagePopWindow(this);
        exitPopupWindow.setFirstMessage(message);
        exitPopupWindow.setSecondMessage(message);
        exitPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
			
			@Override
			public void onDismiss() {
			}
		});
        exitPopupWindow.showAtLocation(view, Gravity.CENTER, 0, 0, new MessagePopWindow.ConfirmListener() {
                    @Override
                    public void confirmClick(View view) {
                        exitPopupWindow.dismiss();
                        BaseActivity.this.finish();
                    }
                },
                new MessagePopWindow.CancelListener() {
                    @Override
                    public void cancelClick(View view) {
                        exitPopupWindow.dismiss();
                    }
                }
        );
    }

    private void showBindPopup(View view,int message) {
        exitPopupWindow = new MessagePopWindow(this);
        exitPopupWindow.setFirstMessage(message);
        exitPopupWindow.setSecondMessage(message);
        exitPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
			
			@Override
			public void onDismiss() {
			}
		});
        exitPopupWindow.showAtLocation(view, Gravity.CENTER, 0, 0, new MessagePopWindow.ConfirmListener() {
                    @Override
                    public void confirmClick(View view) {
                        exitPopupWindow.dismiss();
                        BaseActivity.this.finish();
                        Intent intent = new Intent();
        				intent.setAction("tv.ismar.daisy.usercenter");
        				intent.putExtra("flag", "login");
        				startActivity(intent);
                    }
                },
                null
        );
    }

	class FetchBestTvAuth extends AsyncTask<String, Void, Integer> {
		private final static int RESUTL_CANCELED = -2;
		private final static int RESULT_SUCCESS = 0;
        private String httpresult;
		@Override
		protected Integer doInBackground(String... params) {
			SimpleRestClient mRestClient = new SimpleRestClient();
			try {
				String mac = DeviceUtils.getLocalMacAddress(BaseActivity.this);
				mac = mac.replace("-", "").replace(":", "");
				httpresult = mRestClient.getBestTVAuthor(mac);
				httpresult = httpresult.replace("\"", "");
				DaisyUtils.getVodApplication(BaseActivity.this).getEditor().putString(VodApplication.BESTTV_AUTH_BIND_FLAG, httpresult);
			} catch (NetworkException e) {
				e.printStackTrace();
				return RESUTL_CANCELED;
			}
			if (isCancelled()) {
				return RESUTL_CANCELED;
			} else {
				return RESULT_SUCCESS;
			}
		}

		@Override
		protected void onPostExecute(Integer result) {
			if (result == RESULT_SUCCESS) {
				if (StringUtils.isNotEmpty(SimpleRestClient.mobile_number)
						&& StringUtils
								.isNotEmpty(SimpleRestClient.access_token)) {
					if("privilege".equals(httpresult)){
						//继续绑定
					}
				}else{
					showBindPopup(
							((ViewGroup) findViewById(android.R.id.content))
									.getChildAt(0),
							R.string.besttvauthbind_message);
				}
			}
		}
	}

}

