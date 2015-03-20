package tv.ismar.daisy.views;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tv.ismar.daisy.R;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.models.Item;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PaymentDialog extends Dialog {

	private static final String QRCODE_BASE_URL = "http://sky.tvxio.com/api/order/create/";
	private static final String CARDRECHARGE_BASE_URL = "http://card.t.tvxio.com/api/pay/verify/";
	private Context mycontext;
	private int width;
	private int height;
	private Button weixinpay_button;
	private Button guanyingcard_button;
	private Button zhifubao_button;
	private Button yuepay_button;
	private Button top_loginregister_button;
	private Button submit_cardpay;
	private Button submit_yuepay;
	private Button yueepay_canel;
	private Button top_login;

	private LinearLayout guanyingcard_pay_panel;
	private LoginPanelView login_panel;
	private LinearLayout qrcode_pay;
	private LinearLayout shiyuncard_panel;
	private RelativeLayout top_login_panel;
	private ImageView qrcodeview;

	private TextView payinfo_price;
	private TextView payinfo_exprice;
	private TextView package_price;
	private TextView videotitle;
	private TextView recharge_error_msg;
	private TextView welocome_tip;

	private EditText shiyuncard_input;

	private Item mItem;

	public PaymentDialog(Context context) {
		super(context);
	}

	public PaymentDialog(Context context, int theme) {
		super(context, theme);
		WindowManager wm = (WindowManager) getContext().getSystemService(
				Context.WINDOW_SERVICE);
		width = wm.getDefaultDisplay().getWidth();
		height = wm.getDefaultDisplay().getHeight();
		mycontext = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.paymentselect);
		initView();
		resizeWindow();
	}

	public void setItem(Item item) {
		mItem = item;
	}

	private void resizeWindow() {
		Window dialogWindow = getWindow();
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		lp.width = ((int) (width * 0.8));
		lp.height = ((int) (height * 0.8));
	}

	private void initView() {
		weixinpay_button = (Button) findViewById(R.id.weixin);
		guanyingcard_button = (Button) findViewById(R.id.videocard);
		zhifubao_button = (Button) findViewById(R.id.zhifubao);
		yuepay_button = (Button) findViewById(R.id.balance_pay);
		top_loginregister_button = (Button) findViewById(R.id.top_login);
		submit_cardpay = (Button) findViewById(R.id.shiyuncard_submit);
		submit_yuepay = (Button) findViewById(R.id.card_balance_submit);
		yueepay_canel = (Button) findViewById(R.id.card_balance_cancel);
		top_login = (Button) findViewById(R.id.top_login);

		weixinpay_button.setOnClickListener(buttonClick);
		guanyingcard_button.setOnClickListener(buttonClick);
		zhifubao_button.setOnClickListener(buttonClick);
		yuepay_button.setOnClickListener(buttonClick);
		top_loginregister_button.setOnClickListener(buttonClick);
		submit_cardpay.setOnClickListener(buttonClick);
		submit_yuepay.setOnClickListener(buttonClick);
		yueepay_canel.setOnClickListener(buttonClick);
		top_login.setOnClickListener(buttonClick);

		guanyingcard_pay_panel = (LinearLayout) findViewById(R.id.guanyingcard_pay_panel);
		login_panel = (LoginPanelView) findViewById(R.id.login_panel);
		qrcode_pay = (LinearLayout) findViewById(R.id.qrcode_pay);
		shiyuncard_panel = (LinearLayout) findViewById(R.id.shiyuncard_panel);
		top_login_panel = (RelativeLayout) findViewById(R.id.top_login_panel);

		qrcodeview = (ImageView) findViewById(R.id.qrcodeview);

		payinfo_price = (TextView) findViewById(R.id.payinfo_price);
		payinfo_exprice = (TextView) findViewById(R.id.payinfo_exprice);
		package_price = (TextView) findViewById(R.id.package_price);
		videotitle = (TextView) findViewById(R.id.videotitle);
		recharge_error_msg = (TextView) findViewById(R.id.recharge_error_msg);
		welocome_tip = (TextView) findViewById(R.id.welocome_tip);

		shiyuncard_input = (EditText) findViewById(R.id.shiyuncard_input);
		if (SimpleRestClient.mobile_number != null
				&& SimpleRestClient.mobile_number.length() > 0) {
			welocome_tip.setVisibility(View.VISIBLE);
			top_login_panel.setVisibility(View.GONE);
			String welocome = mycontext.getResources().getString(
					R.string.welocome_tip);
			welocome_tip.setText(String.format(welocome,
					SimpleRestClient.mobile_number));
		}
		setPackageInfo();
		changeQrcodePayPanelState(true, true);
	}

	private View.OnClickListener buttonClick = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			switch (view.getId()) {
			case R.id.weixin: {
				changeQrcodePayPanelState(true, true);
				changeLoginPanelState(false);
				changeYuePayPanelState(false);
				changeshiyuncardPanelState(false);
			}
				break;
			case R.id.videocard: {
				changeQrcodePayPanelState(false, false);
				changeLoginPanelState(false);
				changeYuePayPanelState(false);
				changeshiyuncardPanelState(true);
			}
				break;
			case R.id.zhifubao: {
				changeQrcodePayPanelState(true, false);
				changeLoginPanelState(false);
				changeYuePayPanelState(false);
				changeshiyuncardPanelState(false);
			}
				break;
			case R.id.balance_pay: {
				changeQrcodePayPanelState(false, false);
				changeLoginPanelState(false);
				changeYuePayPanelState(true);
				changeshiyuncardPanelState(false);
			}
				break;
			case R.id.top_login: {
				changeQrcodePayPanelState(false, false);
				changeLoginPanelState(true);
				changeYuePayPanelState(false);
				changeshiyuncardPanelState(false);
			}
				break;

			case R.id.shiyuncard_submit: {
				String inputValue = shiyuncard_input.getText().toString();
				if (inputValue.length() > 16 && isNumeric(inputValue)) {
					card_recharge(inputValue);
				} else {
					recharge_error_msg.setVisibility(View.VISIBLE);
					recharge_error_msg.setText("错误的观影卡密码");
				}
			}
				break;
			}
		}
	};

	private void changeYuePayPanelState(boolean visible) {
		if (visible) {
			guanyingcard_pay_panel.setVisibility(View.VISIBLE);
		} else {
			guanyingcard_pay_panel.setVisibility(View.GONE);
		}
	}

	private Bitmap qrcodeBitmap;
	private android.os.Handler urlHandler = new android.os.Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			qrcodeview.setImageBitmap(qrcodeBitmap);
			if (qrcodeBitmap != null && qrcodeBitmap.isRecycled())
				qrcodeBitmap.recycle();
		}
	};

	private void changeQrcodePayPanelState(boolean visible,
			final boolean isweixin) {
		if (visible) {
			qrcode_pay.setVisibility(View.VISIBLE);
			new Thread() {

				@Override
				public void run() {
					super.run();
					if (isweixin) {
						qrcodeBitmap = returnBitMap(QRCODE_BASE_URL,
								"wares_id=" + mItem.pk + "&wares_type="
										+ "item" + "&device_token="
										+ SimpleRestClient.device_token
										+ "&source=weixin");
					} else {
						qrcodeBitmap = returnBitMap(QRCODE_BASE_URL,
								"wares_id=" + mItem.pk + "&wares_type="
										+ "item" + "&device_token="
										+ SimpleRestClient.device_token
										+ "&source=alipay");
					}
					urlHandler.sendEmptyMessage(0);
				}

			}.start();
		} else {
			qrcode_pay.setVisibility(View.GONE);
		}
	}

	private void changeshiyuncardPanelState(boolean visible) {
		if (visible)
			shiyuncard_panel.setVisibility(View.VISIBLE);
		else
			shiyuncard_panel.setVisibility(View.GONE);
	}

	private void changeLoginPanelState(boolean visible) {
		if (visible)
			login_panel.setVisibility(View.VISIBLE);
		else
			login_panel.setVisibility(View.GONE);
	}

	private Bitmap returnBitMap(String url, String params) {
		URL myFileUrl = null;
		Bitmap bitmap = null;
		try {
			myFileUrl = new URL(url);
			HttpURLConnection connection = (HttpURLConnection) myFileUrl
					.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod("POST");
			connection.setUseCaches(false);
			connection.setInstanceFollowRedirects(false);
			connection.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			connection.connect();
			DataOutputStream out = new DataOutputStream(
					connection.getOutputStream());
			out.writeBytes(params);
			out.flush();
			out.close();
			int code = connection.getResponseCode();
			if (code == 302) {
				String redirectlocation = connection.getHeaderField("Location");
				myFileUrl = new URL(redirectlocation);
				connection = (HttpURLConnection) myFileUrl.openConnection();
				connection.setRequestMethod("GET");
				connection.connect();
				connection.getResponseCode();
			}
			InputStream is = connection.getInputStream();
			bitmap = BitmapFactory.decodeStream(is);
			is.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bitmap;
	}

	private void setPackageInfo() {
		videotitle.setText(mItem.title);
		String price = mycontext.getResources().getString(
				R.string.pay_payinfo_price_label);
		String exprice = mycontext.getResources().getString(
				R.string.pay_payinfo_exprice_label);
		String package_info = mycontext.getResources().getString(
				R.string.pay_package_price);
		payinfo_price.setText(String.format(price, mItem.expense.price));
		payinfo_exprice.setText(String.format(exprice, mItem.expense.duration));
		package_price.setText(String.format(package_info, mItem.expense.price,
				mItem.expense.duration));
	}

	private void card_recharge(String cardNumber) {
		String pwd_prefix = cardNumber.substring(0, 10);
		String sur_prefix = cardNumber.substring(10, 16);
		String timestamp = System.currentTimeMillis() + "";
		String sid = "sid";
		String user = SimpleRestClient.mobile_number;
		String user_id = "0";
		String app_name = "sky";
		String sn = SimpleRestClient.sn_token;
		String card_secret = "";
		try {
			card_secret = SHA1(user + sur_prefix + timestamp);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		card_secret = "card_secret=" + pwd_prefix + card_secret;
		app_name = "&app_name=" + app_name;
		user = "&user=" + user;
		user_id = "&user_id=" + user_id;
		timestamp = "&timestamp=" + timestamp;
		sid = "&sid=" + sid;
		sn = "&sn=" + sn;

		String params = card_secret + app_name + user + user_id + timestamp
				+ sid + sn;
		sendRechargeRequest(CARDRECHARGE_BASE_URL, params);
	}

	private String convertToHex(byte[] data) {
		StringBuilder buf = new StringBuilder();
		for (byte b : data) {
			int halfbyte = (b >>> 4) & 0x0F;
			int two_halfs = 0;
			do {
				buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte)
						: (char) ('a' + (halfbyte - 10)));
				halfbyte = b & 0x0F;
			} while (two_halfs++ < 1);
		}
		return buf.toString();
	}

	private String SHA1(String text) throws NoSuchAlgorithmException,
			UnsupportedEncodingException {
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		md.update(text.getBytes("iso-8859-1"), 0, text.length());
		byte[] sha1hash = md.digest();
		return convertToHex(sha1hash);
	}

	private String sendRechargeRequest(String url, String params) {
		StringBuffer response = new StringBuffer();
		try {
			URL postUrl = new URL(url);
			HttpURLConnection connection = (HttpURLConnection) postUrl
					.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod("POST");
			connection.setUseCaches(false);
			connection.setInstanceFollowRedirects(true);
			connection.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			connection.setRequestProperty("Accept", "application/json");
			connection.connect();
			DataOutputStream out = new DataOutputStream(
					connection.getOutputStream());
			out.writeBytes(params);
			out.flush();
			out.close();
			int status = connection.getResponseCode();
			if (status == 200) {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(connection.getInputStream(),
								"UTF-8"));
				out.flush();
				out.close(); // flush and close
				String line;
				while ((line = reader.readLine()) != null) {
					response.append(line);
				}
				Log.v("aaaa", response.toString());
				reader.close();
				if (url.equals("register") && line == null) {
					connection.disconnect();
					return "200";
				}
			} else {
				connection.disconnect();
				return "";
			}
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
		return response.toString();
	}

	private boolean isNumeric(String str) {
		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher isNum = pattern.matcher(str);
		if (!isNum.matches()) {
			return false;
		}
		return true;
	}
}