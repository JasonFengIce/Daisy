package tv.ismar.daisy;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.EventProperty;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.SimpleRestClient.HttpPostRequestInterface;
import tv.ismar.daisy.models.Item;
import tv.ismar.daisy.models.PrivilegeItem;
import tv.ismar.daisy.player.InitPlayerTool;
import tv.ismar.daisy.player.InitPlayerTool.onAsyncTaskHandler;
import tv.ismar.daisy.ui.adapter.PrivilegeAdapter;
import tv.ismar.daisy.ui.widget.DaisyButton;
import tv.ismar.daisy.utils.Util;
import tv.ismar.daisy.views.LoadingDialog;
import tv.ismar.daisy.views.LoginPanelView;
import tv.ismar.daisy.views.PaymentDialog;
import tv.ismar.daisy.views.LoginPanelView.LoginInterface;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

public class PersonCenterActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "PersonCenterActivity";

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
    private Button client_service_btn;
	private boolean isLogin = false;
	private  SimpleRestClient mSimpleRestClient;
	private ArrayList<PrivilegeItem> mList;
	private LoadingDialog mLoadingDialog;
	private PrivilegeAdapter mAdapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.person_center_layout);
        initView();
        mSimpleRestClient = new SimpleRestClient();
		mLoadingDialog = new LoadingDialog(this, getResources().getString(
				R.string.vod_loading));
		mLoadingDialog.show();
        getBalance();
        if( DaisyUtils.getVodApplication(this).getPreferences().getString(VodApplication.AUTH_TOKEN, "").equals("")){
        	//login out
        	loadDataLoginOut();
        	mPersoninfoLayout.setVisibility(View.VISIBLE);
        	login_layout.setVisibility(View.GONE);
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
		DaisyUtils.getVodApplication(this).addActivityToPool(this.toString(), this);
	}
	public static boolean isMobileNumber(String mobiles){
		Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");
		Matcher m = p.matcher(mobiles);
		return m.matches();
	}
	private void showToast(){
		Toast.makeText(this, "onsuccess", Toast.LENGTH_SHORT).show();
	}
	private void getBalance(){
		mSimpleRestClient.doSendRequest("/accounts/balance/", "get", "", new HttpPostRequestInterface() {

			@Override
			public void onSuccess(String info) {
				// TODO Auto-generated method stub
				try {
					JSONObject json = new JSONObject(info);
					remain_money_value.setText(json.getString("balance"));
					getPrivilegeData();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					remain_money_value.setText("0");
					e.printStackTrace();
				}
			}

			@Override
			public void onPrepare() {
				// TODO Auto-generated method stub

			}

			@Override
			public void onFailed(String error) {
				// TODO Auto-generated method stub
				remain_money_value.setText("0");
				getPrivilegeData();
			}
		});
	}
	private PaymentDialog.OrderResultListener ordercheckListener = new PaymentDialog.OrderResultListener() {

		@Override
		public void payResult(boolean result) {
		}

	};
	private void buyVideo(Item item) {
		PaymentDialog dialog = new PaymentDialog(PersonCenterActivity.this,
				R.style.PaymentDialog, ordercheckListener);
		item.model_name = "item";
		dialog.setItem(item);
		dialog.show();
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
        client_service_btn = (Button)findViewById(R.id.client_service_btn);
        client_service_btn.setOnClickListener(this);
         privilegelist.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position,
					long id) {
				// TODO Auto-generated method stub
				final PrivilegeItem privilegeitem = mList.get(position);
				if(privilegeitem!=null){
					if(privilegeitem.getType().equals("package")){
							Intent intent = new Intent();
							intent.setAction("tv.ismar.daisy.packageitem");
							intent.putExtra("url", SimpleRestClient.root_url+"/api/package/"+privilegeitem.getItem_pk()+"/");
							startActivity(intent);
					}
					else{
						if(privilegeitem.getType().equals("item")){
							final String url = "/api/item/"+privilegeitem.getItem_pk()+"/";
							mSimpleRestClient.doSendRequest(url, "get", "", new HttpPostRequestInterface() {

								@Override
								public void onSuccess(String info) {
									// TODO Auto-generated method stub
									Item item = mSimpleRestClient.getItemRecord(info);
									if(item!=null){
										Intent intent = new Intent();
										if(item.is_complex) {
											intent.setAction("tv.ismar.daisy.Item");
											intent.putExtra("url", SimpleRestClient.root_url + url);
											startActivity(intent);
										} else {
											if(privilegeitem.isIseffective()){
												InitPlayerTool tool = new InitPlayerTool(PersonCenterActivity.this);
												tool.setonAsyncTaskListener(new onAsyncTaskHandler() {

													@Override
													public void onPreExecute(Intent intent) {
														// TODO Auto-generated method stub
											            mLoadingDialog.show();
													}

													@Override
													public void onPostExecute() {
														// TODO Auto-generated method stub
														mLoadingDialog.dismiss();
													}
												});
												tool.initClipInfo(item.url, InitPlayerTool.FLAG_URL);
											}
											else{
												//
											   item.model_name = "item";
                                               buyVideo(item);
											}
										}
									}
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
						else if(privilegeitem.getType().equals("subitem")){
							String subitem_url = "/api/subitem/"+privilegeitem.getItem_pk()+"/";
							mSimpleRestClient.doSendRequest(subitem_url, "get", "", new HttpPostRequestInterface() {

								@Override
								public void onSuccess(String info) {
									// TODO Auto-generated method stub
									final Item subitem = mSimpleRestClient.getItemRecord(info);
									if(subitem!=null){
										if(subitem.is_complex){
											Intent intent = new Intent();
											intent.setAction("tv.ismar.daisy.Item");
											String url = "/api/item/"+subitem.item_pk+"/";
											intent.putExtra("url", SimpleRestClient.root_url+url);
											startActivity(intent);
										}
										else{
											if(privilegeitem.isIseffective()){
												InitPlayerTool tool = new InitPlayerTool(PersonCenterActivity.this);
												tool.setonAsyncTaskListener(new onAsyncTaskHandler() {

													@Override
													public void onPreExecute(Intent intent) {
														// TODO Auto-generated method stub
											            mLoadingDialog.show();
													}

													@Override
													public void onPostExecute() {
														// TODO Auto-generated method stub
														mLoadingDialog.dismiss();
													}
												});
												tool.initClipInfo(subitem, InitPlayerTool.FLAG_ITEM);
											}
											else{
												//
												String item_url = "/api/item/"+subitem.item_pk+"/";
												mSimpleRestClient.doSendRequest(item_url, "get", "", new HttpPostRequestInterface() {

													@Override
													public void onSuccess(String info) {
														// TODO Auto-generated method stub
														Item item = mSimpleRestClient.getItemRecord(info);
														if(item!=null){
															subitem.model_name = "subitem";
															subitem.expense.subprice = subitem.expense.subprice;
															buyVideo(subitem);
														}
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
										}
									}
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
					}
				}
			}


		});
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
				showExitPopup(v);


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
		privilegelist = (ListView)findViewById(R.id.privilegelist);

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
    /**
     * showExitPopup
     *
     * @param view
     */
    private void showExitPopup(View view) {
        final Context context = this;
        View contentView = LayoutInflater.from(context)
                .inflate(R.layout.popupview_exit, null);
        final PopupWindow  exitPopupWindow = new PopupWindow(null, 1400, 500);
        exitPopupWindow.setContentView(contentView);
        exitPopupWindow.setFocusable(true);
        exitPopupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        TextView promt =(TextView) contentView.findViewById(R.id.prompt_txt);
        promt.setText("您是否确定退出当前账户");
        Button confirmExit = (Button) contentView.findViewById(R.id.confirm_exit);
        Button cancelExit = (Button) contentView.findViewById(R.id.cancel_exit);

        confirmExit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
            	getBalance();
				mPersoninfoLayout.setVisibility(View.VISIBLE);
				login_layout.setVisibility(View.GONE);
				login_layout.clearLayout();
				SimpleRestClient.access_token = "";
				SimpleRestClient.mobile_number = "";
				DaisyUtils.getVodApplication(PersonCenterActivity.this).getEditor().putString(VodApplication.AUTH_TOKEN, "");
				DaisyUtils.getVodApplication(PersonCenterActivity.this).getEditor().putString(VodApplication.MOBILE_NUMBER, "");
				DaisyUtils.getVodApplication(PersonCenterActivity.this).save();
				loadDataLoginOut();
				isLogin = false;
				login_or_out_btn.setEnabled(true);
				login_or_out_btn.setBackgroundResource(R.drawable.person_btn_selector);
                exitPopupWindow.dismiss();

            }
        });

        cancelExit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                exitPopupWindow.dismiss();
            }
        });
    }
	private void getPrivilegeData(){

		mSimpleRestClient.doSendRequest("/accounts/orders/", "get", "", new HttpPostRequestInterface() {

			@Override
			public void onSuccess(String info) {
				// TODO Auto-generated method stub
				try {
					JSONObject json = new JSONObject(info);
					mList = new ArrayList<PrivilegeItem>();
					JSONArray ineffective = json.getJSONArray("ineffective");
					JSONArray effective = json.getJSONArray("effective");
					if(effective.length()>0){
						int length = effective.length();
						for(int i=0;i<length;i++){
							PrivilegeItem item = new PrivilegeItem();
							String title = effective.getJSONArray(i).getString(0);
							item.setTitle(title);
							String buydate = effective.getJSONArray(i).getString(1);
							item.setBuydate(buydate);
							String exceedDate = effective.getJSONArray(i).getString(2);
							item.setExceeddate(exceedDate);
							String type = effective.getJSONArray(i).getString(3);
							item.setType(type);
							int item_pk = effective.getJSONArray(i).getInt(4);
							item.setItem_pk(item_pk);
							item.setIseffective(true);
							mList.add(item);
						}
					}
					if(ineffective.length()>0){
						int length = ineffective.length();
						for(int i=0;i<length;i++){
							PrivilegeItem item = new PrivilegeItem();
							String title = ineffective.getJSONArray(i).getString(0);
							item.setTitle(title);
							String buydate = ineffective.getJSONArray(i).getString(1);
							item.setBuydate(buydate);
							String exceedDate = ineffective.getJSONArray(i).getString(2);
							item.setExceeddate(exceedDate);
							String type = ineffective.getJSONArray(i).getString(3);
							item.setType(type);
							int item_pk = ineffective.getJSONArray(i).getInt(4);
							item.setItem_pk(item_pk);
							item.setIseffective(false);
							mList.add(item);
						}
					}
					mAdapter= new PrivilegeAdapter(PersonCenterActivity.this,mList);
					privilegelist.setAdapter(mAdapter);

					if(mAdapter.getCount()>0){
						no_privilegelist_txt.setVisibility(View.GONE);
						privilegelist.setVisibility(View.VISIBLE);
						privilege_txt.setVisibility(View.VISIBLE);
					}
					else{
						no_privilegelist_txt.setVisibility(View.VISIBLE);
						privilegelist.setVisibility(View.GONE);
						privilege_txt.setVisibility(View.GONE);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				mLoadingDialog.dismiss();
			}

			@Override
			public void onPrepare() {
				// TODO Auto-generated method stub

			}

			@Override
			public void onFailed(String error) {
				// TODO Auto-generated method stub
				mLoadingDialog.dismiss();
			}
		});
		//curl "http://sky.tvxio.com/accounts/orders/?device_token=6xeQV4Vpb4khaTvDSvVJ2Q=="
//		PrivilegeAdapter adapter= new PrivilegeAdapter(this,list);
//		mListView.setAdapter(adapter);
	}
	private void loadDataLogin(){
		login_or_out_btn.setVisibility(View.VISIBLE);
		//no_privilegelist_txt.setVisibility(View.VISIBLE);
		//privilegelist.setVisibility(View.GONE);
		mobile_or_sn_txt.setText("手机号:");
		mobile_or_sn_value.setText(SimpleRestClient.mobile_number);
		warn_info_txt.setVisibility(View.GONE);
		sn_txt_value.setText(SimpleRestClient.sn_token);
		sn_txt.setVisibility(View.VISIBLE);
		sn_txt_value.setVisibility(View.VISIBLE);
		exit_btn.setVisibility(View.VISIBLE);

	}
	private void loadDataLoginOut(){
		//no_privilegelist_txt.setVisibility(View.VISIBLE);
		//privilegelist.setVisibility(View.GONE);
		mobile_or_sn_txt.setText("SN:");
		mobile_or_sn_value.setText(SimpleRestClient.sn_token);
		warn_info_txt.setVisibility(View.VISIBLE);
		sn_txt_value.setVisibility(View.GONE);
		sn_txt.setVisibility(View.GONE);
		exit_btn.setVisibility(View.GONE);
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode==KeyEvent.KEYCODE_BACK){
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		DaisyUtils.getVodApplication(this).removeActivtyFromPool(this.toString());
		super.onDestroy();
	}

    private void startSakura(){
        if (AppConstant.DEBUG)
            Log.d(TAG, "install vod service invoke...");
        try {
          ApplicationInfo applicationInfo =  getPackageManager().getApplicationInfo(
                    "cn.ismartv.speedtester", 0);
            if(null!= applicationInfo){
                Intent intent = new Intent();
                intent.setClassName("cn.ismartv.speedtester", "cn.ismartv.speedtester.ui.activity.MenuActivity");
                startActivity(intent);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Uri uri = Uri.parse("file://" + getFileStreamPath("Sakura.apk").getAbsolutePath());
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.client_service_btn:
                startSakura();
                break;
            default:
                break;
        }
    }
}
