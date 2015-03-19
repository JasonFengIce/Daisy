package tv.ismar.daisy;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.views.LoginPanelView;
import tv.ismar.daisy.views.LoginPanelView.LoginInterface;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class PersonCenterActivity extends Activity {
	private ListView privilegelist;
	private LoginPanelView login_layout;
	private View mPersoninfoLayout;
	private TextView no_privilegelist_txt;
	private TextView mobile_or_sn_txt;
	private TextView mobile_or_sn_value;
	private TextView sn_txt;
	private TextView sn_txt_value;
	private TextView warn_info_txt;
	private TextView remain_money_value;
	private TextView privilege_txt;
	private Button exit_btn;
	private Button personal_info_btn;
	private Button login_or_out_btn;
	private boolean isLogin = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.person_center_layout);
		DaisyUtils.getVodApplication(this).addActivityToPool(this.toString(), this);
        initView();
        if( DaisyUtils.getVodApplication(this).getPreferences().getString(VodApplication.AUTH_TOKEN, "").equals("")){
        	//login out
        	loadDataLoginOut();
        	mPersoninfoLayout.setVisibility(View.VISIBLE);
        	login_layout.setVisibility(View.GONE);
        	login_or_out_btn.setFocusable(true);
        	login_or_out_btn.requestFocus();
        	isLogin = false;
        }
        else{
        	loadDataLogin();
        	mPersoninfoLayout.setVisibility(View.VISIBLE);
        	login_layout.setVisibility(View.GONE);
        	login_or_out_btn.setBackgroundResource(R.drawable.btn_disabled_bg);
        	login_or_out_btn.setEnabled(false);
        	isLogin = true;
        }
	}
	public static int daysBetween(String startdate,String enddate) throws ParseException{
    	SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();  
        cal.setTime(sdf.parse(startdate));  
        long time1 = cal.getTimeInMillis();               
        cal.setTime(sdf.parse(enddate));  
        long time2 = cal.getTimeInMillis();       
        long between_days=(time2-time1)/(1000*3600*24);
          
       return Integer.parseInt(String.valueOf(between_days));   
    }
	public static boolean isMobileNumber(String mobiles){
		Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");
		Matcher m = p.matcher(mobiles);
		return m.matches();
	}
	private void showToast(){
		Toast.makeText(this, "onsuccess", Toast.LENGTH_SHORT).show();
	}
	private void initView(){
		mPersoninfoLayout = (View)findViewById(R.id.info);
		privilege_txt = (TextView)mPersoninfoLayout.findViewById(R.id.privilege_txt);
		privilege_txt.setVisibility(View.GONE);
		privilegelist = (ListView)mPersoninfoLayout.findViewById(R.id.privilegelist);
		no_privilegelist_txt = (TextView)mPersoninfoLayout.findViewById(R.id.no_privilegelist_txt);
		mobile_or_sn_txt = (TextView)mPersoninfoLayout.findViewById(R.id.mobile_or_sn_txt);
		mobile_or_sn_value = (TextView)mPersoninfoLayout.findViewById(R.id.mobile_or_sn_value);
		sn_txt = (TextView)mPersoninfoLayout.findViewById(R.id.sn_txt);
		sn_txt_value = (TextView)mPersoninfoLayout.findViewById(R.id.sn_txt_value);
		warn_info_txt = (TextView)mPersoninfoLayout.findViewById(R.id.warn_info_txt);
		remain_money_value = (TextView)mPersoninfoLayout.findViewById(R.id.remain_money_value);
		exit_btn = (Button)mPersoninfoLayout.findViewById(R.id.exit_btn);
		personal_info_btn = (Button)findViewById(R.id.personal_info_btn);
		login_or_out_btn = (Button)findViewById(R.id.login_or_out_btn);
		login_layout = (LoginPanelView)findViewById(R.id.login_layout);
		login_layout.setLoginListener(new LoginInterface() {
			
			@Override
			public void onSuccess(String info) {
				// TODO Auto-generated method stub
				login_layout.setVisibility(View.GONE);
				mPersoninfoLayout.setVisibility(View.VISIBLE);
				login_or_out_btn.setBackgroundResource(R.drawable.btn_disabled_bg);
				login_or_out_btn.setEnabled(false);
				mobile_or_sn_value.setText(login_layout.getMobileNumber());
				isLogin = true;
				//load personal info
				login_layout.clearLayout();
				loadDataLogin();
			}
			
			@Override
			public void onFailed(String error) {
				// TODO Auto-generated method stub
				
			}
		});
		exit_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//login out;
				mPersoninfoLayout.setVisibility(View.VISIBLE);
				login_layout.setVisibility(View.GONE);
				login_layout.clearLayout();
				SimpleRestClient.access_token = "";
				DaisyUtils.getVodApplication(PersonCenterActivity.this).getEditor().putString(VodApplication.AUTH_TOKEN, "");
				loadDataLoginOut();
				isLogin = false;
				login_or_out_btn.setEnabled(true);
				login_or_out_btn.setBackgroundResource(R.drawable.person_btn_selector);
				
			}
		});
		personal_info_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mPersoninfoLayout.setVisibility(View.VISIBLE);
				login_layout.setVisibility(View.GONE);
				login_layout.clearLayout();
				if(isLogin)
				    loadDataLogin();
				else
					loadDataLoginOut();
			}
		});
		login_or_out_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mPersoninfoLayout.setVisibility(View.GONE);
				login_layout.setVisibility(View.VISIBLE);
			}
		});
		//mListView = (ListView)findViewById(R.id.privilegelist);
		
//		  try {
//				Field f = AbsListView.class.getDeclaredField("mFastScroller");
//				if(!f.isAccessible()){
//					f.setAccessible(true);
//				}
//				Object o = f.get(mListView);
//				f = f.getType().getDeclaredField("mThumbDrawable");
//				f.setAccessible(true);
//				Drawable drawable=(Drawable) f.get(o);
//				drawable = getResources().getDrawable(R.drawable.listview_thumb_bg);
//				f.set(o,drawable);
//				
//			} catch (SecurityException e) {
//				e.printStackTrace();
//			} catch (NoSuchFieldException e) {
//				e.printStackTrace();
//			} catch (IllegalArgumentException e) {
//				e.printStackTrace();
//			} catch (IllegalAccessException e) {
//				e.printStackTrace();
//			}
	}
//	private void getData(){
//		ArrayList<PrivilegeItem> list = new ArrayList<PrivilegeItem>();
//		PrivilegeItem item1 = new PrivilegeItem();
//		PrivilegeItem item2 = new PrivilegeItem();
//		PrivilegeItem item3 = new PrivilegeItem();
//		PrivilegeItem item4 = new PrivilegeItem();
//		PrivilegeItem item5 = new PrivilegeItem();
//		PrivilegeItem item6 = new PrivilegeItem();
//		PrivilegeItem item7 = new PrivilegeItem();
//		PrivilegeItem item8 = new PrivilegeItem();
//		PrivilegeItem item9 = new PrivilegeItem();
//		PrivilegeItem item10 = new PrivilegeItem();
//		item1.setType("NBA���꿨");
//		item2.setType("NBA���꿨");
//		item3.setType("NBA���꿨");
//		item4.setType("NBA���꿨");
//		item5.setType("NBA���꿨");
//		item6.setType("NBA���꿨");
//		item7.setType("NBA���꿨");
//		item8.setType("NBA���꿨");
//		item9.setType("NBA���꿨");
//		item10.setType("NBA���꿨");
//		item1.setDuration("ʣ��289��");
//		item2.setDuration("�ѵ�������ʱ���");
//		item3.setDuration("ʣ��285��");
//		item4.setDuration("ʣ��283��");
//		item5.setDuration("�ѵ�������ʱ���");
//		item6.setDuration("ʣ��289��");
//		item7.setDuration("�ѵ�������ʱ���");
//		item8.setDuration("ʣ��285��");
//		item9.setDuration("ʣ��283��");
//		item10.setDuration("�ѵ�������ʱ���");
//		list.add(item1);
//		list.add(item2);
//		list.add(item3);
//		list.add(item4);
//		list.add(item5);
//		list.add(item6);
//		list.add(item7);
//		list.add(item8);
//		list.add(item9);
//		list.add(item10);
//		PrivilegeAdapter adapter= new PrivilegeAdapter(this,list);
//		mListView.setAdapter(adapter);
//	}
	private void loadDataLogin(){
		login_or_out_btn.setVisibility(View.VISIBLE);
		no_privilegelist_txt.setVisibility(View.VISIBLE);
		privilegelist.setVisibility(View.GONE);
		mobile_or_sn_txt.setText("手机号:");
		mobile_or_sn_value.setText(SimpleRestClient.mobile_number);
		warn_info_txt.setVisibility(View.GONE);
		sn_txt_value.setText(SimpleRestClient.sn_token);
		sn_txt.setVisibility(View.VISIBLE);
		sn_txt_value.setVisibility(View.VISIBLE);
		exit_btn.setVisibility(View.VISIBLE);
		
	}
	private void loadDataLoginOut(){
		no_privilegelist_txt.setVisibility(View.VISIBLE);
		privilegelist.setVisibility(View.GONE);
		mobile_or_sn_txt.setText("SN:");
		mobile_or_sn_value.setText(SimpleRestClient.sn_token);
		warn_info_txt.setVisibility(View.VISIBLE);
		sn_txt_value.setVisibility(View.GONE);
		sn_txt.setVisibility(View.GONE);
		exit_btn.setVisibility(View.GONE);
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		DaisyUtils.getVodApplication(this).removeActivtyFromPool(this.toString());
		super.onDestroy();
	}
}
