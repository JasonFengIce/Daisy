package tv.ismar.daisy.views;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import tv.ismar.daisy.R;
import tv.ismar.daisy.models.Item;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PaymentDialog extends Dialog {

	private Context mycontext;
	private int width;
	private int height;
	private Button weixinpay_button;
	private Button guanyingcard_button;
	private Button zhifubao_button;
	private Button yuepay_button;
	private Button top_loginregister_button;
	private Button getcheckmsg;
	private Button submit_login;
	private Button submit_cardpay;
	private Button submit_yuepay;
	private Button yueepay_canel;

	private LinearLayout guanyingcard_pay_panel;
	private LinearLayout login_panel;
	private LinearLayout qrcode_pay;
	private LinearLayout shiyuncard_panel;
	private ImageView qrcodeview;

	private TextView payinfo_price;
	private TextView payinfo_exprice;
	private TextView package_price;

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
		getcheckmsg = (Button) findViewById(R.id.pay_identifyCodeBtn);
		submit_login = (Button) findViewById(R.id.pay_btn_submit);
		submit_cardpay = (Button) findViewById(R.id.shiyuncard_submit);
		submit_yuepay = (Button) findViewById(R.id.card_balance_submit);
		yueepay_canel = (Button) findViewById(R.id.card_balance_cancel);

		weixinpay_button.setOnClickListener(buttonClick);
		guanyingcard_button.setOnClickListener(buttonClick);
		zhifubao_button.setOnClickListener(buttonClick);
		yuepay_button.setOnClickListener(buttonClick);
		top_loginregister_button.setOnClickListener(buttonClick);
		getcheckmsg.setOnClickListener(buttonClick);
		submit_login.setOnClickListener(buttonClick);
		submit_cardpay.setOnClickListener(buttonClick);
		submit_yuepay.setOnClickListener(buttonClick);
		yueepay_canel.setOnClickListener(buttonClick);

		guanyingcard_pay_panel = (LinearLayout) findViewById(R.id.guanyingcard_pay_panel);
		login_panel = (LinearLayout) findViewById(R.id.login_panel);
		qrcode_pay = (LinearLayout) findViewById(R.id.qrcode_pay);
		shiyuncard_panel = (LinearLayout) findViewById(R.id.shiyuncard_panel);

		qrcodeview = (ImageView) findViewById(R.id.qrcodeview);

		payinfo_price = (TextView) findViewById(R.id.payinfo_price);
		payinfo_exprice = (TextView) findViewById(R.id.payinfo_exprice);
		package_price =(TextView) findViewById(R.id.package_price);
		setPackageInfo();
		changeQrcodePayPanelState(true,true);
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
			}
		}
	};

	private void changeLoginPanelState(boolean visible) {
		if (visible)
			login_panel.setVisibility(View.VISIBLE);
		else
			login_panel.setVisibility(View.GONE);
	}

	private void changeYuePayPanelState(boolean visible) {
		if (visible)
			guanyingcard_pay_panel.setVisibility(View.VISIBLE);
		else
			guanyingcard_pay_panel.setVisibility(View.GONE);
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
			urlHandler.sendEmptyMessage(0);
			new Thread() {

				@Override
				public void run() {
					super.run();
					if (isweixin) {
						qrcodeBitmap = returnBitMap("http://cmstest.tvxio.com/page/qrcode/display/weixin/?username=15300320422&user_id=48519&sid=20338998aa36495a92aff4730bb8a587&wares_type=package&wares_id=33&devide_id=");
					} else {
						qrcodeBitmap = returnBitMap("http://tfsimg.alipay.com/images/mobilecodec/T19tNfXo0nXXXXXXXX");
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

	private Bitmap returnBitMap(String url) {
		URL myFileUrl = null;
		Bitmap bitmap = null;
		try {
			myFileUrl = new URL(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		try {
			HttpURLConnection conn = (HttpURLConnection) myFileUrl
					.openConnection();
			conn.setDoInput(true);
			// conn.setRequestMethod();
			conn.connect();
			InputStream is = conn.getInputStream();
			bitmap = BitmapFactory.decodeStream(is);
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bitmap;
	}

	private void setPackageInfo(){
		String price=mycontext.getResources().getString(R.string.pay_payinfo_price_label);
		String exprice=mycontext.getResources().getString(R.string.pay_payinfo_exprice_label);
		String package_info =mycontext.getResources().getString(R.string.pay_package_price);
		payinfo_price.setText(String.format(price,mItem.expense.price));
		payinfo_exprice.setText(String.format(exprice,mItem.expense.duration));
		package_price.setText(String.format(package_info,mItem.expense.price,mItem.expense.duration));
	}
}