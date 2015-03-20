package tv.ismar.daisy.views;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;

import com.alibaba.fastjson.JSONObject;

import tv.ismar.daisy.LauncherActivity;
import tv.ismar.daisy.R;
import tv.ismar.daisy.VodApplication;
import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.SimpleRestClient.HttpPostRequestInterface;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LoginPanelView extends LinearLayout {
    
	private Button identifyCodeBtn;
	private SimpleRestClient mSimpleRestClient;
	private DialogInterface.OnClickListener mPositiveListener;
	private DialogInterface.OnClickListener mNegativeListener;
	private Dialog dialog = null;
	private EditText edit_identifycode;
	private Button btn_submit;
	private EditText edit_mobile;
	private TextView count_tip;
	private IsmartCountTimer timeCount;
	private boolean suspension_window=false;
	public LoginPanelView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		TypedArray a = context.obtainStyledAttributes(attrs,R.styleable.LoginPanelView);
		 LayoutInflater mInflater = LayoutInflater.from(context);
		 View myView;
		 suspension_window = a.getBoolean(
					R.styleable.LoginPanelView_suspension_window, false);
         if(suspension_window){
        	 myView = mInflater.inflate(R.layout.pay_person_login, null);
         }
         else{
    		 myView = mInflater.inflate(R.layout.person_center_login, null);
         }
		 setOrientation(LinearLayout.VERTICAL);
		 mSimpleRestClient = new SimpleRestClient();
		 addView(myView);
         initView();
         a.recycle();
	}

    private void initView(){
    	count_tip = (TextView)findViewById(R.id.pay_count_tip);
    	edit_mobile = (EditText)findViewById(R.id.pay_edit_mobile);
    	edit_identifycode = (EditText)findViewById(R.id.pay_edit_identifycode);
    	identifyCodeBtn = (Button)findViewById(R.id.pay_identifyCodeBtn);
    	btn_submit = (Button)findViewById(R.id.pay_btn_submit);
    	btn_submit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String identifyCode = edit_identifycode.getText().toString();
				if("".equals(identifyCode)){
					//count_tip.setText("验证码不能为空!");
					setcount_tipText("验证码不能为空!");
					return;
				}
				if("".equals(edit_mobile.getText().toString())){
					setcount_tipText("手机号不能为空!");
					return;
				}
				boolean ismobile = isMobileNumber(edit_mobile.getText().toString());
				if(!ismobile){
					//count_tip.setText("不是手机号码");
					setcount_tipText("不是手机号码");
					return;
				}
				mSimpleRestClient.doSendRequest("/accounts/login/" ,"post", "device_token="+SimpleRestClient.device_token+
						"&username="+edit_mobile.getText().toString()+"&auth_number="+identifyCode, new HttpPostRequestInterface(){

							@Override
							public void onPrepare() {
								// TODO Auto-generated method stub
								
							}

							@Override
							public void onSuccess(String info) {
								// TODO Auto-generated method stub
								try {
									org.json.JSONObject json = new org.json.JSONObject(info);
									String auth_token = json.getString(VodApplication.AUTH_TOKEN);
									DaisyUtils.getVodApplication(getContext()).getEditor().putString(VodApplication.AUTH_TOKEN, auth_token);
									DaisyUtils.getVodApplication(getContext()).getEditor().putString(VodApplication.MOBILE_NUMBER,edit_mobile.getText().toString());
									DaisyUtils.getVodApplication(getContext()).save();
									SimpleRestClient.access_token = auth_token;
									SimpleRestClient.mobile_number = edit_mobile.getText().toString();
									if(callback!=null){
										callback.onSuccess(auth_token);
									}
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									callback.onFailed(e.toString());
									e.printStackTrace();
								}
							}

							@Override
							public void onFailed(String error) {
								// TODO Auto-generated method stub
								callback.onFailed(error);
								//count_tip.setText(error);
								setcount_tipText(error);
							}
					
				});
			}
		});
    	identifyCodeBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if("".equals(edit_mobile.getText().toString())){
					//count_tip.setText("请输入手机号");
					setcount_tipText("请输入手机号");
					return;
				}
				boolean ismobile = isMobileNumber(edit_mobile.getText().toString());
				if(!ismobile){
					//count_tip.setText("不是手机号码");
					setcount_tipText("不是手机号码");
					return;
				}
				timeCount = new IsmartCountTimer(identifyCodeBtn, R.drawable.btn_normal_bg, R.drawable.btn_disabled_bg);
				timeCount.start();
				count_tip.setVisibility(View.VISIBLE);
				mSimpleRestClient.doSendRequest("/accounts/auth/","post", "device_token="+SimpleRestClient.device_token+"&username="+edit_mobile.getText().toString(),new HttpPostRequestInterface(){
					
					@Override
					public void onSuccess(String info) {
						// TODO Auto-generated method stub
						//timeCount.cancel();
						//identifyCodeBtn.setEnabled(true);
						//identifyCodeBtn.setBackgroundResource(R.drawable.btn_normal_bg);
						//identifyCodeBtn.setText("获取验证码");
						//count_tip.setText("获取验证码成功，请提交!");
						setcount_tipText("获取验证码成功，请提交!");
					}
					
					@Override
					public void onPrepare() {
						// TODO Auto-generated method stub
						//count_tip.setText("60秒后可再次点击获取验证码");
						setcount_tipText("60秒后可再次点击获取验证码");
					}
					
					@Override
					public void onFailed(String error) {
						// TODO Auto-generated method stub
						timeCount.cancel();
						identifyCodeBtn.setEnabled(true);
						identifyCodeBtn.setBackgroundResource(R.drawable.btn_normal_bg);
						identifyCodeBtn.setText("获取验证码");
						//count_tip.setText("获取验证码:\n"+error);
						setcount_tipText("获取验证码:\n"+error);
						//showDialog(error);
					}
				});
			}
		});
    }
	public static boolean isMobileNumber(String mobiles){
		Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");
		Matcher m = p.matcher(mobiles);
		return m.matches();
	}
	LoginInterface callback;
	public void setLoginListener(LoginInterface callback) {
		this.callback = callback;
	}
	public interface LoginInterface{
		public void onSuccess(String info);
		public void onFailed(String error);
	}
	private void showDialog(String info){
		       mPositiveListener = new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						dialog.dismiss();
					}
				};
				mNegativeListener = new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						dialog.dismiss();
					}
				};
		

			 dialog = new CustomDialog.Builder(getContext())
				.setMessage(info)
				.setPositiveButton(R.string.vod_retry, mPositiveListener)
				.setNegativeButton(R.string.vod_ok, mNegativeListener).create();
		
		dialog.show();
	}
	
	public String getMobileNumber(){
		return edit_mobile.getText().toString();
	}
	public void clearLayout(){
		count_tip.setVisibility(View.INVISIBLE);
		edit_identifycode.setText("");
		edit_mobile.setText("");
		identifyCodeBtn.setEnabled(true);
		identifyCodeBtn.setBackgroundResource(R.drawable.btn_normal_bg);
		identifyCodeBtn.setText("获取验证码");
        if(timeCount!=null){
        	timeCount.cancel();
        }
	}
	private void setcount_tipText(String str){
		count_tip.setText(str);
		count_tip.setVisibility(View.VISIBLE);
	}
}
