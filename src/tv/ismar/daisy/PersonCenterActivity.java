package tv.ismar.daisy;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import tv.ismar.daisy.views.LoginPanelView;
import tv.ismar.daisy.views.LoginPanelView.LoginInterface;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;

public class PersonCenterActivity extends Activity {
	private ListView mListView;
	private LoginPanelView login_layout;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.person_center_layout);
        initView();

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
		login_layout = (LoginPanelView)findViewById(R.id.login_layout);
		login_layout.setLoginListener(new LoginInterface() {
			
			@Override
			public void onSuccess(String info) {
				// TODO Auto-generated method stub
				showToast();
			}
			
			@Override
			public void onFailed(String error) {
				// TODO Auto-generated method stub
				
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
}
