package tv.ismar.daisy;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tv.ismar.daisy.core.AttributeDeserializer;
import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.SimpleRestClient.HttpPostRequestInterface;
import tv.ismar.daisy.models.Attribute;
import tv.ismar.daisy.models.Item;
import tv.ismar.daisy.models.ItemList;
import tv.ismar.daisy.models.OrderListItem;
import tv.ismar.daisy.models.PrivilegeItem;
import tv.ismar.daisy.player.InitPlayerTool;
import tv.ismar.daisy.player.InitPlayerTool.onAsyncTaskHandler;
import tv.ismar.daisy.ui.adapter.OrderListAdapter;
import tv.ismar.daisy.ui.adapter.PackageGridlistAdapter;
import tv.ismar.daisy.ui.adapter.PrivilegeAdapter;
import tv.ismar.daisy.views.LoadingDialog;
import tv.ismar.daisy.views.LoginPanelView;
import tv.ismar.daisy.views.LoginPanelView.LoginInterface;
import tv.ismar.daisy.views.PaymentDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import cn.ismartv.activator.Activator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class PersonCenterActivity extends BaseActivity implements
		View.OnClickListener {
	private static final String TAG = "PersonCenterActivity";

	private ListView privilegelist;
	private ListView orderlist;
	private GridView person_center_packagelist;
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
	private Button orderlist_btn;
	private Button personal_card_btn;
	private boolean isLogin = false;
	private SimpleRestClient mSimpleRestClient;
	private ArrayList<PrivilegeItem> mList;
	private ArrayList<OrderListItem> morderList;
	private LoadingDialog mLoadingDialog;
	private PrivilegeAdapter mAdapter;
	private OrderListAdapter morderListAdapter;
	private TextView account_register;

	// 请求需要签名 sign: sn="xxx"&timestamp="xxx"
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
		if (DaisyUtils.getVodApplication(this).getPreferences()
				.getString(VodApplication.AUTH_TOKEN, "").equals("")) {
			// login out
			loadDataLoginOut();
			mPersoninfoLayout.setVisibility(View.VISIBLE);
			login_layout.setVisibility(View.GONE);
			isLogin = false;
		} else {
			loadDataLogin();
			mPersoninfoLayout.setVisibility(View.VISIBLE);
			login_layout.setVisibility(View.GONE);
			login_or_out_btn.setBackgroundResource(R.drawable.btn_disabled_bg);
			login_or_out_btn.setEnabled(false);
			isLogin = true;
		}
		DaisyUtils.getVodApplication(this).addActivityToPool(this.toString(),
				this);
	}

	public static boolean isMobileNumber(String mobiles) {
		Pattern p = Pattern
				.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");
		Matcher m = p.matcher(mobiles);
		return m.matches();
	}

	private void showToast() {
		Toast.makeText(this, "onsuccess", Toast.LENGTH_SHORT).show();
	}

	private void getBalance() {
		mSimpleRestClient.doSendRequest("/accounts/balance/", "get", "",
				new HttpPostRequestInterface() {

					@Override
					public void onSuccess(String info) {
						// TODO Auto-generated method stub
						try {
							JSONObject json = new JSONObject(info);
							remain_money_value.setText(json
									.getString("balance"));
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

	private void initView() {
		mPersoninfoLayout = (View) findViewById(R.id.info);
		orderlist = (ListView) findViewById(R.id.orderlist);
		person_center_packagelist =(GridView)findViewById(R.id.person_center_packagelist);
		privilege_txt = (TextView) mPersoninfoLayout
				.findViewById(R.id.privilege_txt);
		privilege_txt.setVisibility(View.GONE);
		privilegelist = (ListView) mPersoninfoLayout
				.findViewById(R.id.privilegelist);
		no_privilegelist_txt = (TextView) mPersoninfoLayout
				.findViewById(R.id.no_privilegelist_txt);
		mobile_or_sn_txt = (TextView) mPersoninfoLayout
				.findViewById(R.id.mobile_or_sn_txt);
		mobile_or_sn_value = (TextView) mPersoninfoLayout
				.findViewById(R.id.mobile_or_sn_value);
		sn_txt = (TextView) mPersoninfoLayout.findViewById(R.id.sn_txt);
		sn_txt_value = (TextView) mPersoninfoLayout
				.findViewById(R.id.sn_txt_value);
		warn_info_txt = (TextView) mPersoninfoLayout
				.findViewById(R.id.warn_info_txt);
		remain_money_value = (TextView) mPersoninfoLayout
				.findViewById(R.id.remain_money_value);
		exit_btn = (Button) mPersoninfoLayout.findViewById(R.id.exit_btn);
		personal_info_btn = (Button) findViewById(R.id.personal_info_btn);
		login_or_out_btn = (Button) findViewById(R.id.login_or_out_btn);
		login_layout = (LoginPanelView) findViewById(R.id.login_layout);
		client_service_btn = (Button) findViewById(R.id.client_service_btn);
		orderlist_btn = (Button) findViewById(R.id.personal_orderrecord_btn);
		personal_card_btn = (Button)findViewById(R.id.personal_card_btn);
		account_register = (TextView) findViewById(R.id.account_register);
		orderlist_btn.setOnClickListener(this);
		personal_card_btn.setOnClickListener(this);
		account_register.setOnClickListener(this);
		client_service_btn.setOnClickListener(this);
		privilegelist.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				final PrivilegeItem privilegeitem = mList.get(position);
				if (privilegeitem != null) {
					if (privilegeitem.getType().equals("package")) {
						Intent intent = new Intent();
						intent.setAction("tv.ismar.daisy.packageitem");
						intent.putExtra("url", SimpleRestClient.root_url
								+ "/api/package/" + privilegeitem.getItem_pk()
								+ "/");
						startActivity(intent);
					} else {
						if (privilegeitem.getType().equals("item")) {
							final String url = "/api/item/"
									+ privilegeitem.getItem_pk() + "/";
							mSimpleRestClient.doSendRequest(url, "get", "",
									new HttpPostRequestInterface() {

										@Override
										public void onSuccess(String info) {
											Item item = mSimpleRestClient
													.getItemRecord(info);
											if (item != null) {
												Intent intent = new Intent();
												if (item.is_complex) {
													intent.setAction("tv.ismar.daisy.Item");
													intent.putExtra(
															"url",
															SimpleRestClient.root_url
																	+ url);
													startActivity(intent);
												} else {
													if (privilegeitem
															.isIseffective()) {
														InitPlayerTool tool = new InitPlayerTool(
																PersonCenterActivity.this);
														tool.setonAsyncTaskListener(new onAsyncTaskHandler() {

															@Override
															public void onPreExecute(
																	Intent intent) {
																mLoadingDialog
																		.show();
															}

															@Override
															public void onPostExecute() {
																mLoadingDialog
																		.dismiss();
															}
														});
														tool.initClipInfo(
																item.url,
																InitPlayerTool.FLAG_URL);
													} else {
														//
														item.model_name = "item";
														buyVideo(item);
													}
												}
											}
										}

										@Override
										public void onPrepare() {
										}

										@Override
										public void onFailed(String error) {
										}
									});
						} else if (privilegeitem.getType().equals("subitem")) {
							String subitem_url = "/api/subitem/"
									+ privilegeitem.getItem_pk() + "/";
							mSimpleRestClient.doSendRequest(subitem_url, "get",
									"", new HttpPostRequestInterface() {

										@Override
										public void onSuccess(String info) {
											final Item subitem = mSimpleRestClient
													.getItemRecord(info);
											if (subitem != null) {
												if (subitem.is_complex) {
													Intent intent = new Intent();
													intent.setAction("tv.ismar.daisy.Item");
													String url = "/api/item/"
															+ subitem.item_pk
															+ "/";
													intent.putExtra(
															"url",
															SimpleRestClient.root_url
																	+ url);
													startActivity(intent);
												} else {
													if (privilegeitem
															.isIseffective()) {
														InitPlayerTool tool = new InitPlayerTool(
																PersonCenterActivity.this);
														tool.setonAsyncTaskListener(new onAsyncTaskHandler() {

															@Override
															public void onPreExecute(
																	Intent intent) {
																mLoadingDialog
																		.show();
															}

															@Override
															public void onPostExecute() {
																mLoadingDialog
																		.dismiss();
															}
														});
														tool.initClipInfo(
																subitem,
																InitPlayerTool.FLAG_ITEM);
													} else {
														//
														String item_url = "/api/item/"
																+ subitem.item_pk
																+ "/";
														mSimpleRestClient
																.doSendRequest(
																		item_url,
																		"get",
																		"",
																		new HttpPostRequestInterface() {

																			@Override
																			public void onSuccess(
																					String info) {
																				Item item = mSimpleRestClient
																						.getItemRecord(info);
																				if (item != null) {
																					subitem.model_name = "subitem";
																					subitem.expense.subprice = subitem.expense.subprice;
																					buyVideo(subitem);
																				}
																			}

																			@Override
																			public void onPrepare() {
																			}

																			@Override
																			public void onFailed(
																					String error) {
																			}
																		});
													}
												}
											}
										}

										@Override
										public void onPrepare() {

										}

										@Override
										public void onFailed(String error) {

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
				login_layout.setVisibility(View.GONE);
				mPersoninfoLayout.setVisibility(View.VISIBLE);
				login_or_out_btn
						.setBackgroundResource(R.drawable.btn_disabled_bg);
				login_or_out_btn.setEnabled(false);
				mobile_or_sn_value.setText(login_layout.getMobileNumber());
				isLogin = true;
				// load personal info
				login_layout.clearLayout();
				loadDataLogin();
			}

			@Override
			public void onFailed(String error) {
			}
		});
		exit_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showExitPopup(v);

			}
		});
		personal_info_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mPersoninfoLayout.setVisibility(View.VISIBLE);
				login_layout.setVisibility(View.GONE);
				orderlist.setVisibility(View.GONE);
				login_layout.clearLayout();
				if (isLogin)
					loadDataLogin();
				else
					loadDataLoginOut();
			}
		});
		personal_info_btn.setFocusable(true);
		personal_info_btn.requestFocus();
		personal_info_btn.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasfocus) {
				if (hasfocus) {
					v.setBackgroundResource(R.drawable.hover_btn_bg);
				} else {
					v.setBackgroundResource(R.drawable.btn_normal_bg);
				}

				// person_btn_selector
			}
		});
		login_or_out_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mPersoninfoLayout.setVisibility(View.GONE);
				orderlist.setVisibility(View.GONE);
				person_center_packagelist.setVisibility(View.GONE);
				login_layout.setVisibility(View.VISIBLE);
			}
		});
		privilegelist = (ListView) findViewById(R.id.privilegelist);

		// try {
		// Field f = AbsListView.class.getDeclaredField("mFastScroller");
		// if(!f.isAccessible()){
		// f.setAccessible(true);
		// }
		// Object o = f.get(mListView);
		// f = f.getType().getDeclaredField("mThumbDrawable");
		// f.setAccessible(true);
		// Drawable drawable=(Drawable) f.get(o);
		// drawable = getResources().getDrawable(R.drawable.listview_thumb_bg);
		// f.set(o,drawable);
		//
		// } catch (SecurityException e) {
		// e.printStackTrace();
		// } catch (NoSuchFieldException e) {
		// e.printStackTrace();
		// } catch (IllegalArgumentException e) {
		// e.printStackTrace();
		// } catch (IllegalAccessException e) {
		// e.printStackTrace();
		// }
	}

	/**
	 * showExitPopup
	 * 
	 * @param view
	 */
	private void showExitPopup(View view) {
		final Context context = this;
		View contentView = LayoutInflater.from(context).inflate(
				R.layout.popupview_exit, null);
		final PopupWindow exitPopupWindow = new PopupWindow(null, 740, 341);
		exitPopupWindow.setContentView(contentView);
		exitPopupWindow.setFocusable(true);
		exitPopupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
		TextView promt = (TextView) contentView.findViewById(R.id.prompt_txt);
		promt.setText("您是否确定退出当前账户");
		Button confirmExit = (Button) contentView
				.findViewById(R.id.confirm_exit);
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
				DaisyUtils.getVodApplication(PersonCenterActivity.this)
						.getEditor().putString(VodApplication.AUTH_TOKEN, "");
				DaisyUtils.getVodApplication(PersonCenterActivity.this)
						.getEditor()
						.putString(VodApplication.MOBILE_NUMBER, "");
				DaisyUtils.getVodApplication(PersonCenterActivity.this).save();
				loadDataLoginOut();
				isLogin = false;
				login_or_out_btn.setEnabled(true);
				login_or_out_btn
						.setBackgroundResource(R.drawable.person_btn_selector);
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

	private void getPrivilegeData() {
		long timestamp = System.currentTimeMillis();
		Activator activator = Activator.getInstance(this);
		String rsaResult = activator.PayRsaEncode("sn="
				+ SimpleRestClient.sn_token + "&timestamp=" + timestamp);
		String params = "device_token=" + SimpleRestClient.device_token
				+ "&access=" + SimpleRestClient.access_token + "&timestamp="
				+ timestamp + "&sign=" + rsaResult;
		mSimpleRestClient.doSendRequest(SimpleRestClient.root_url
				+ "/accounts/playauths/", "post", params,
				new HttpPostRequestInterface() {

					@Override
					public void onSuccess(String info) {
						// TODO Auto-generated method stub
						try {
							JSONObject json = new JSONObject(info);
							mList = new ArrayList<PrivilegeItem>();
							JSONArray sn_authlist = json
									.getJSONArray("sn_playauth_list");
							JSONArray playauth_list = json
									.getJSONArray("playauth_list");
							// 设备权限
							if (sn_authlist.length() > 0) {
								int length = sn_authlist.length();
								for (int i = 0; i < length; i++) {
									PrivilegeItem item = new PrivilegeItem();
									String title = sn_authlist.getJSONObject(i)
											.getString("title");
									item.setTitle(title);
									String exceedDate = sn_authlist
											.getJSONObject(i).getString(
													"expiry_date");
									item.setExceeddate(exceedDate);
									mList.add(item);
								}
							}
							// 账户权限
							if (playauth_list.length() > 0) {
								int length = playauth_list.length();
								for (int i = 0; i < length; i++) {
									PrivilegeItem item = new PrivilegeItem();
									String title = playauth_list.getJSONObject(
											i).getString("title");
									item.setTitle(title);
									String exceedDate = playauth_list
											.getJSONObject(i).getString(
													"expiry_date");
									item.setExceeddate(exceedDate);
									mList.add(item);
								}
							}
							mAdapter = new PrivilegeAdapter(
									PersonCenterActivity.this, mList);
							privilegelist.setAdapter(mAdapter);

							if (mAdapter.getCount() > 0) {
								no_privilegelist_txt.setVisibility(View.GONE);
								privilegelist.setVisibility(View.VISIBLE);
								privilege_txt.setVisibility(View.VISIBLE);
							} else {
								no_privilegelist_txt
										.setVisibility(View.VISIBLE);
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
		// curl
		// "http://sky.tvxio.com/accounts/orders/?device_token=6xeQV4Vpb4khaTvDSvVJ2Q=="
		// PrivilegeAdapter adapter= new PrivilegeAdapter(this,list);
		// mListView.setAdapter(adapter);
	}

	private void getOrderListData() {
		long timestamp = System.currentTimeMillis();
		Activator activator = Activator.getInstance(this);
		String rsaResult = activator.PayRsaEncode("sn="
				+ SimpleRestClient.sn_token + "&timestamp=" + timestamp);
		String params = "device_token=" + SimpleRestClient.device_token
				+ "&access=" + SimpleRestClient.access_token + "&timestamp="
				+ timestamp + "&sign=" + rsaResult;
		mSimpleRestClient.doSendRequest(SimpleRestClient.root_url
				+ "/accounts/orders/", "post", params,
				new HttpPostRequestInterface() {

					@Override
					public void onSuccess(String info) {
						morderList = new ArrayList<OrderListItem>();
						try {
							JSONObject json = new JSONObject(info);
							JSONArray order_list = json
									.getJSONArray("order_list");
							JSONArray sn_order_list = json
									.getJSONArray("sn_order_list");
							// 设备权限
							if (order_list.length() > 0) {
								int length = order_list.length();
								for (int i = 0; i < length; i++) {
									JSONObject object = order_list
											.getJSONObject(i);
									OrderListItem item = new OrderListItem();
									item.setTitle(object.getString("title"));
									item.setStart_date(object
											.getString("start_date"));
									item.setExpiry_date(object
											.getString("expiry_date"));
									item.setTotal_fee(object
											.getInt("total_fee"));
									item.setPaysource(object
											.getString("source"));
									item.setThumb_url(object
											.getString("thumb_url"));
									morderList.add(item);
								}
							}
							// 账户权限
							if (sn_order_list.length() > 0) {
								int length = sn_order_list.length();
								for (int i = 0; i < length; i++) {
									JSONObject object = sn_order_list
											.getJSONObject(i);
									OrderListItem item = new OrderListItem();
									item.setTitle(object.getString("title"));
									item.setStart_date(object
											.getString("start_date"));
									item.setExpiry_date(object
											.getString("expiry_date"));
									item.setTotal_fee(object
											.getInt("total_fee"));
									item.setPaysource(object
											.getString("source"));
									item.setThumb_url(object
											.getString("thumb_url"));
									morderList.add(item);
								}
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
						morderListAdapter = new OrderListAdapter(
								PersonCenterActivity.this, morderList);
						orderlist.setAdapter(morderListAdapter);
						orderlist.setVisibility(View.VISIBLE);
						mPersoninfoLayout.setVisibility(View.GONE);
						person_center_packagelist.setVisibility(View.GONE);
						login_layout.setVisibility(View.GONE);
					}

					@Override
					public void onPrepare() {
					}

					@Override
					public void onFailed(String error) {
						mLoadingDialog.dismiss();
					}
				});
	}

	private void getPackageList() {
		mSimpleRestClient.doSendRequest(SimpleRestClient.root_url
				+ "/api/tv/section/youhuidinggou/", "get", "",
				new HttpPostRequestInterface() {

					@Override
					public void onSuccess(String info) {
						GsonBuilder gsonBuilder = new GsonBuilder();
						gsonBuilder.registerTypeAdapter(Attribute.class,
								new AttributeDeserializer());
						Gson gson = gsonBuilder.create();
						ItemList list = gson.fromJson(info, ItemList.class);
						PackageGridlistAdapter adapter = new PackageGridlistAdapter(
								PersonCenterActivity.this, list.objects);
						person_center_packagelist.setAdapter(adapter);
						person_center_packagelist.setVisibility(View.VISIBLE);
						orderlist.setVisibility(View.GONE);
						mPersoninfoLayout.setVisibility(View.GONE);
						login_layout.setVisibility(View.GONE);
					}

					@Override
					public void onPrepare() {
					}

					@Override
					public void onFailed(String error) {
						mLoadingDialog.dismiss();
					}
				});
	}

	private void loadDataLogin() {
		login_or_out_btn.setVisibility(View.VISIBLE);
		// no_privilegelist_txt.setVisibility(View.VISIBLE);
		// privilegelist.setVisibility(View.GONE);
		mobile_or_sn_txt.setText("手机号:");
		mobile_or_sn_value.setText(SimpleRestClient.mobile_number);
		warn_info_txt.setVisibility(View.GONE);
		sn_txt_value.setText(SimpleRestClient.sn_token);
		sn_txt.setVisibility(View.VISIBLE);
		sn_txt_value.setVisibility(View.VISIBLE);
		exit_btn.setVisibility(View.VISIBLE);

	}

	private void loadDataLoginOut() {
		// no_privilegelist_txt.setVisibility(View.VISIBLE);
		// privilegelist.setVisibility(View.GONE);
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
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		DaisyUtils.getVodApplication(this).removeActivtyFromPool(
				this.toString());
		super.onDestroy();
	}

	private void startSakura() {
		Intent intent = new Intent();
		intent.setAction("cn.ismar.sakura.launcher");
		startActivity(intent);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.client_service_btn:
			startSakura();
			break;
		case R.id.account_register:
			AccountAboutDialog dialog = new AccountAboutDialog(this,
					R.style.UserinfoDialog);
			dialog.getWindow().clearFlags(
					WindowManager.LayoutParams.FLAG_DIM_BEHIND);
			dialog.show();
			break;
		case R.id.personal_orderrecord_btn:
			getOrderListData();
			break;
		case R.id.personal_card_btn:
			getPackageList();
		default:
			break;
		}
	}

	class AccountAboutDialog extends Dialog {
		private int width;
		private int height;
		private int x_coordinate;
		private int y_coordinate;

		public AccountAboutDialog(Context context, int theme) {
			super(context, theme);
			WindowManager wm = (WindowManager) getContext().getSystemService(
					Context.WINDOW_SERVICE);
			width = wm.getDefaultDisplay().getWidth();
			height = wm.getDefaultDisplay().getHeight();
		}

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			this.setContentView(R.layout.account_about);
			resizeWindow();
		}

		private void resizeWindow() {
			Window dialogWindow = getWindow();
			WindowManager.LayoutParams lp = dialogWindow.getAttributes();
			x_coordinate = ((int) (width * 0.15));
			y_coordinate = ((int) (height * 0.1));
			lp.width = ((int) (width * 0.70));
			lp.height = ((int) (height * 0.74));
			lp.x = 510;
			lp.y = 150;
			lp.gravity = Gravity.LEFT | Gravity.TOP;
		}

		@Override
		public boolean onKeyDown(int keyCode, KeyEvent event) {
			switch (keyCode) {
			case KeyEvent.KEYCODE_BACK:
				dismiss();
				return true;
			}
			return super.onKeyDown(keyCode, event);
		}
	};
}
