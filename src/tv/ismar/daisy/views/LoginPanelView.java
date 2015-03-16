package tv.ismar.daisy.views;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tv.ismar.daisy.R;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.SimpleRestClient.HttpPostRequestInterface;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class LoginPanelView extends LinearLayout {
    
	private Button identifyCodeBtn;
	private SimpleRestClient mSimpleRestClient; 
	public LoginPanelView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		 LayoutInflater mInflater = LayoutInflater.from(context);
		 View myView = mInflater.inflate(R.layout.person_center_login, null);
		 setOrientation(LinearLayout.VERTICAL);
		 addView(myView);
         initView();
         mSimpleRestClient = new SimpleRestClient();
	}

    private void initView(){
    	identifyCodeBtn = (Button)findViewById(R.id.identifyCodeBtn);
    	identifyCodeBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				callback.onSuccess("123");
				IsmartCountTimer timeCount = new IsmartCountTimer(identifyCodeBtn, R.drawable.btn_normal_bg, R.drawable.btn_disabled_bg);//������������ɫֵ
				//timeCount.start();
				mSimpleRestClient.post("/accounts/auth/", "device_token=="+SimpleRestClient.device_token+"&username=13120689235",new HttpPostRequestInterface() {
					
					@Override
					public void onSuccess(String info) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onPrepare() {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onFailed(String error) {
						// TODO Auto-generated method stub
						
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

}
