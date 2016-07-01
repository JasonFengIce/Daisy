package tv.ismar.daisy.views;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnHoverListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alipay.sdk.app.PayTask;
import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

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
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import cn.ismartv.activator.Activator;
import okhttp3.ResponseBody;
import retrofit2.Callback;
import retrofit2.GsonConverterFactory;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.RxJavaCallAdapterFactory;
import retrofit2.ScalarsConverterFactory;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import tv.ismar.daisy.BaseActivity;
import tv.ismar.daisy.R;
import tv.ismar.daisy.VodApplication;
import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.SimpleRestClient.HttpPostRequestInterface;
import tv.ismar.daisy.core.alipay.PayResult;
import tv.ismar.daisy.core.client.HttpAPI;
import tv.ismar.daisy.core.client.HttpManager;
import tv.ismar.daisy.core.client.NewVipHttpApi;
import tv.ismar.daisy.core.preferences.AccountSharedPrefs;
import tv.ismar.daisy.data.usercenter.AuthTokenEntity;
import tv.ismar.daisy.models.Favorite;
import tv.ismar.daisy.models.History;
import tv.ismar.daisy.models.Item;
import tv.ismar.sakura.ui.widget.MessagePopWindow;

public class PaymentDialog extends Dialog implements BaseActivity.OnLoginCallback {

    private static final String QRCODE_BASE_URL = "/api/order/create/";
    private static final String BALANCEPAY_BASE_URL = "/api/order/create/";
    private static final String GETBALANCE_BASE_URL = "/accounts/balance/";
    private static final String CARDRECHARGE_BASE_URL = "https://order.tvxio.com/api/pay/verify/";
    public static final String ALI_PAY = "/api/order/choose_way/";
    //    private static final int REFRESH_PAY_STATUS = 0x10;
    private static final int SETQRCODE_VIEW = 0x11;
    //    private static final int PURCHASE_CHECK_RESULT = 0x12;
//    private static final int ORDER_CHECK_INTERVAL = 10000;
    private static final int LOGIN_SUCESS = 0x14;
    private static final int SETDAIKOUPANELVISIBLE = 0x15;
    private static final int DAIKOU_ERROR = 0x16;
    private static final String TAG = "PaymentDialog";
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
    private LinearLayout qrcode_panel;
    private RelativeLayout daikou_panel;
    private ImageView qrcodeview;
    private MessagePopWindow loginPopup;
    private TextView payinfo_price;
    private TextView payinfo_exprice;
    private TextView package_price;
    private TextView videotitle;
    private TextView recharge_error_msg;
    private TextView welocome_tip;
    private TextView card_balance_title_label;
    private TextView panel_label;
    private EditText shiyuncard_input;

    private Bitmap qrcodeBitmap;
    private Item mItem;
    private OrderResultListener paylistener;
    private int ordercheckcount;
    private boolean flag = true;
    private SimpleRestClient mSimpleRestClient;
    private Item[] mHistoriesByNet;
    private ImageView payment_shadow_view;
    private TextView ali_price;
    private TextView ali_exprie;
    private Button alipay_submit;
    private TextView mLocalAppPayBtn;

    private String alipayInfo;

    private Subscription mOrderCheckLoopSubscription;

    public PaymentDialog(Context context) {
        super(context);
    }

    public PaymentDialog(Context context, int theme,
                         OrderResultListener paylistener) {
        super(context, theme);
        WindowManager wm = (WindowManager) getContext().getSystemService(
                Context.WINDOW_SERVICE);
        width = wm.getDefaultDisplay().getWidth();
        height = wm.getDefaultDisplay().getHeight();
        mycontext = context;
        if (StringUtils.isNotEmpty(SimpleRestClient.access_token)
                && StringUtils.isNotEmpty(SimpleRestClient.mobile_number)) {
            getBalanceByToken();
        }
        this.paylistener = paylistener;
        mSimpleRestClient = new SimpleRestClient();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.paymentselect);
        ((BaseActivity) mycontext).setLoginCallback(this);
        initView();
        resizeWindow();
        if (StringUtils.isNotEmpty(SimpleRestClient.access_token)
                && StringUtils.isNotEmpty(SimpleRestClient.mobile_number)) {
            welocome_tip.setVisibility(View.VISIBLE);
            String welocome = mycontext.getResources().getString(
                    R.string.welocome_tip);
            welocome_tip.setText(String.format(welocome,
                    SimpleRestClient.mobile_number));
            purchaseCheck(CheckType.PlayCheck);
        } else {
            welocome_tip.setVisibility(View.GONE);
        }
    }

    @Override
    public void dismiss() {
        if (urlHandler.hasMessages(SETQRCODE_VIEW))
            urlHandler.removeMessages(SETQRCODE_VIEW);
        if (urlHandler.hasMessages(LOGIN_SUCESS))
            urlHandler.removeMessages(LOGIN_SUCESS);
        if (urlHandler.hasMessages(SETDAIKOUPANELVISIBLE))
            urlHandler.removeMessages(SETDAIKOUPANELVISIBLE);
        urlHandler.removeCallbacksAndMessages(null);
        if (qrcodeBitmap != null && qrcodeBitmap.isRecycled()) {
            qrcodeBitmap.recycle();
            qrcodeBitmap = null;
        }
        super.dismiss();
    }

    public void setItem(Item item) {
        mItem = item;
    }

    private void resizeWindow() {
        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = ((int) (width * 0.83));
        lp.height = ((int) (height * 0.83));
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
        card_balance_title_label = (TextView) findViewById(R.id.card_balance_title_label);
        payment_shadow_view = (ImageView) findViewById(R.id.payment_shadow_view);
        shiyuncard_input = (EditText) findViewById(R.id.shiyuncard_input);
        panel_label = (TextView) findViewById(R.id.panel_label);
        qrcode_panel = (LinearLayout) findViewById(R.id.qrcode_panel);
        daikou_panel = (RelativeLayout) findViewById(R.id.daikou_panel);
        ali_price = (TextView) findViewById(R.id.ali_price);
        ali_exprie = (TextView) findViewById(R.id.ali_exprie);
        alipay_submit = (Button) findViewById(R.id.alipay_submit);
        alipay_submit.setOnClickListener(buttonClick);
        shiyuncard_input.setOnHoverListener(mOnHoverListener);
        yuepay_button.setOnHoverListener(mOnHoverListener);
        submit_cardpay.setOnHoverListener(mOnHoverListener);
        yueepay_canel.setOnHoverListener(mOnHoverListener);
        zhifubao_button.setOnHoverListener(mOnHoverListener);
        weixinpay_button.setOnHoverListener(mOnHoverListener);
        guanyingcard_button.setOnHoverListener(mOnHoverListener);
        submit_yuepay.setOnHoverListener(mOnHoverListener);
        setPackageInfo();
        if (StringUtils.isNotEmpty(SimpleRestClient.access_token)
                && StringUtils.isNotEmpty(SimpleRestClient.mobile_number)) {
            changeQrcodePayPanelState(true, true);
            panel_label.setVisibility(View.GONE);
        } else {
            disableButton();
        }
        login_panel.setLoginListener(loginInterFace);

        mLocalAppPayBtn = (TextView) findViewById(R.id.local_app_pay_btn);
        mLocalAppPayBtn.setOnClickListener(mOnLocalPayClickListener);
    }

    private void disableButton() {
        if (StringUtils.isEmpty(SimpleRestClient.access_token)
                && StringUtils.isEmpty(SimpleRestClient.mobile_number)) {
            weixinpay_button.setEnabled(false);
            weixinpay_button.setFocusable(false);
            weixinpay_button.setTextColor(getContext().getResources().getColor(
                    R.color.paychannel_button_disable));
            guanyingcard_button.setEnabled(false);
            guanyingcard_button.setFocusable(false);
            guanyingcard_button.setTextColor(getContext().getResources()
                    .getColor(R.color.paychannel_button_disable));
            zhifubao_button.setEnabled(false);
            zhifubao_button.setFocusable(false);
            zhifubao_button.setTextColor(getContext().getResources().getColor(
                    R.color.paychannel_button_disable));
            yuepay_button.setEnabled(false);
            yuepay_button.setFocusable(false);
            yuepay_button.setTextColor(getContext().getResources().getColor(
                    R.color.paychannel_button_disable));
            changeQrcodePayPanelState(false, false);
            changeLoginPanelState(true);
            changeYuePayPanelState(false, false);
            changeshiyuncardPanelState(false);
        }

    }

    private void enableButton() {
        weixinpay_button.setEnabled(true);
        weixinpay_button.setFocusable(true);
        weixinpay_button.setTextColor(getContext().getResources().getColor(
                R.color.white));
        guanyingcard_button.setEnabled(true);
        guanyingcard_button.setFocusable(true);
        guanyingcard_button.setTextColor(getContext().getResources().getColor(
                R.color.white));
        zhifubao_button.setEnabled(true);
        zhifubao_button.setFocusable(true);
        zhifubao_button.setTextColor(getContext().getResources().getColor(
                R.color.white));
        yuepay_button.setEnabled(true);
        yuepay_button.setFocusable(true);
        yuepay_button.setTextColor(getContext().getResources().getColor(
                R.color.white));
    }

    private View.OnClickListener buttonClick = new View.OnClickListener() {
        private Activator activator;

        @Override
        public void onClick(View view) {
            if (qrcodeBitmap != null && qrcodeBitmap.isRecycled()) {
                qrcodeBitmap.recycle();
                qrcodeBitmap = null;
            }
            switch (view.getId()) {
                case R.id.weixin:
                case R.id.videocard:
                case R.id.zhifubao:
                case R.id.balance_pay:
                case R.id.top_login:
                    if (mOrderCheckLoopSubscription != null) {
                        mOrderCheckLoopSubscription.unsubscribe();
                    }
                    break;
            }

            switch (view.getId()) {
                case R.id.weixin: {
                    qrcodeview.setVisibility(View.INVISIBLE);
                    changeQrcodePayPanelState(true, true);
                    changeLoginPanelState(false);
                    changeYuePayPanelState(false, false);
                    changeshiyuncardPanelState(false);
                    purchaseCheck(CheckType.OrderPurchase);
                    mLocalAppPayBtn.setVisibility(View.INVISIBLE);
                }
                break;
                case R.id.videocard: {
                    changeQrcodePayPanelState(false, false);
                    changeLoginPanelState(false);
                    changeYuePayPanelState(false, false);
                    changeshiyuncardPanelState(true);
                }
                break;
                case R.id.zhifubao: {
                    qrcodeview.setVisibility(View.INVISIBLE);
                    changeQrcodePayPanelState(true, false);
                    changeLoginPanelState(false);
                    changeYuePayPanelState(false, false);
                    changeshiyuncardPanelState(false);
                    purchaseCheck(CheckType.OrderPurchase);
                    mLocalAppPayBtn.setVisibility(View.VISIBLE);
                    mLocalAppPayBtn.setTag(LocalPayType.AliPay);
                    mLocalAppPayBtn.setText(R.string.already_install_alipay);
                }
                break;
                case R.id.balance_pay: {
                    changeQrcodePayPanelState(false, false);
                    changeLoginPanelState(false);
                    changeYuePayPanelState(true, false);
                    changeshiyuncardPanelState(false);
                }
                break;
                case R.id.top_login: {
                    flag = true;
                    changeQrcodePayPanelState(false, false);
                    changeLoginPanelState(true);
                    changeYuePayPanelState(false, false);
                    changeshiyuncardPanelState(false);
                    purchaseCheck(CheckType.OrderPurchase);
                }
                break;

                case R.id.shiyuncard_submit: {
                    submit_cardpay.setClickable(false);
                    String inputValue = shiyuncard_input.getText().toString();
                    if (inputValue.length() == 16 && isNumeric(inputValue)) {
                        card_recharge(inputValue);
                    } else {
                        recharge_error_msg.setVisibility(View.VISIBLE);
                        recharge_error_msg.setText("错误的观影卡密码");
                    }
                }
                break;

                case R.id.card_balance_submit: {
                    submit_yuepay.setClickable(false);
                    SimpleRestClient client = new SimpleRestClient();

                    String timestamp = System.currentTimeMillis() + "";

                    String encode = "sn=" + SimpleRestClient.sn_token
                            + "&source=sky" + "&timestamp=" + timestamp
                            + "&wares_id=" + mItem.pk + "&wares_type="
                            + mItem.model_name;
                    activator = Activator.getInstance(getContext());
                    String rsaResult = activator.PayRsaEncode(encode);
                    if (rsaResult != null && !"".equals(rsaResult)) {
                        client.doSendRequest(BALANCEPAY_BASE_URL, "post",
                                "wares_id=" + mItem.pk + "&wares_type="
                                        + mItem.model_name + "&device_token="
                                        + SimpleRestClient.device_token
                                        + "&access_token="
                                        + SimpleRestClient.access_token
                                        + "&source=sky" + "&timestamp=" + timestamp
                                        + "&sn=" + SimpleRestClient.sn_token
                                        + "&sign=" + rsaResult, balancePay);
                    }

                }
                break;

                case R.id.card_balance_cancel: {
                    doCancel();
                }
                break;
                case R.id.alipay_submit: {
                    new Thread() {
                        @Override
                        public void run() {
                            String url = alipay_submit.getTag().toString();
                            URL myFileUrl = null;
                            try {
                                myFileUrl = new URL(url);
                                HttpURLConnection connection = (HttpURLConnection) myFileUrl
                                        .openConnection();
                                connection.setRequestMethod("GET");
                                connection.setConnectTimeout(1000);
                                connection.setReadTimeout(2000);
                                connection.connect();
                                int code = connection.getResponseCode();
                                StringBuffer sb = new StringBuffer();
                                if (code == 200) {
                                    BufferedReader buff = new BufferedReader(
                                            new InputStreamReader(
                                                    connection.getInputStream(),
                                                    "UTF-8"));
                                    String line = null;
                                    while ((line = buff.readLine()) != null) {
                                        sb.append(line);
                                    }
                                    buff.close();
                                    connection.disconnect();
                                }
                                JSONObject jsonObject = new JSONObject(
                                        sb.toString());
                                String status = jsonObject.getString("status");
                                if ("T".equals(status)) {
                                    purchaseCheck(CheckType.OrderPurchase);
                                } else {
                                    String display_message = jsonObject
                                            .getString("display_message");
                                    Message m = new Message();
                                    m.what = DAIKOU_ERROR;
                                    m.obj = display_message;
                                    urlHandler.sendMessage(m);
                                }
                            } catch (Exception e) {
                                Message m = new Message();
                                m.what = DAIKOU_ERROR;
                                m.obj = "代扣失败";
                                urlHandler.sendMessage(m);
                                e.printStackTrace();
                            }
                        }
                    }.start();
                    break;
                }
            }
        }
    };

    private void changeYuePayPanelState(boolean visible, boolean needCheck) {
        if (visible) {
            getBalanceByToken();
            guanyingcard_pay_panel.setVisibility(View.VISIBLE);
        } else {
            guanyingcard_pay_panel.setVisibility(View.GONE);
        }
    }

    private void getBalanceByToken() {
        SimpleRestClient client = new SimpleRestClient();
        client.doSendRequest(GETBALANCE_BASE_URL, "get", "",
                fetchBalancerResult);
    }

    private android.os.Handler urlHandler = new android.os.Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SETQRCODE_VIEW: {
                    qrcode_panel.setVisibility(View.VISIBLE);
                    daikou_panel.setVisibility(View.GONE);
                    qrcodeview.setVisibility(View.VISIBLE);
                    qrcodeview.setImageBitmap(qrcodeBitmap);
                    if (qrcodeBitmap != null && qrcodeBitmap.isRecycled())
                        qrcodeBitmap.recycle();
                    qrcodeBitmap = null;
                    break;
                }
                case SETDAIKOUPANELVISIBLE: {
                    qrcode_panel.setVisibility(View.GONE);
                    daikou_panel.setVisibility(View.VISIBLE);
                    break;
                }
                case LOGIN_SUCESS: {
                    welocome_tip.setVisibility(View.VISIBLE);
                    // top_login_panel.setVisibility(View.GONE);
                    // ((BaseActivity)mycontext).callWGQueryQQUserInfo();
                    break;
                }
                case DAIKOU_ERROR: {
                    if (msg.obj != null) {
                        String display_message = msg.obj.toString();
                        Toast.makeText(mycontext, display_message,
                                Toast.LENGTH_LONG).show();
                    }
                }
                break;
            }
        }
    };

    private void changeQrcodePayPanelState(boolean visible,
                                           final boolean isweixin) {
        if (visible) {
            qrcodeview.setImageDrawable((new ColorDrawable(Color.WHITE)));
            qrcode_pay.setVisibility(View.VISIBLE);
            daikou_panel.setVisibility(View.GONE);
            new Thread() {

                @Override
                public void run() {
                    super.run();
                    if (isweixin) {
                        qrcodeBitmap = returnBitMap(SimpleRestClient.root_url
                                + QRCODE_BASE_URL, "POST", "wares_id="
                                + mItem.pk + "&wares_type=" + mItem.model_name
                                + "&device_token="
                                + SimpleRestClient.device_token
                                + "&access_token="
                                + SimpleRestClient.access_token
                                + "&source=weixin");
                        urlHandler.sendEmptyMessage(SETQRCODE_VIEW);
                        purchaseCheck(CheckType.OrderPurchase);
                    } else {
//						aliPayChannel(SimpleRestClient.root_url + ALI_PAY,
//								"wares_id=" + mItem.pk + "&wares_type="
//										+ mItem.model_name + "&device_token="
//										+ SimpleRestClient.device_token
//										+ "&access_token="
//										+ SimpleRestClient.access_token
//										+ "&source=alipay");
                        qrcodeBitmap = returnBitMap(SimpleRestClient.root_url
                                + QRCODE_BASE_URL, "POST", "wares_id="
                                + mItem.pk + "&wares_type=" + mItem.model_name
                                + "&device_token="
                                + SimpleRestClient.device_token
                                + "&access_token="
                                + SimpleRestClient.access_token
                                + "&source=alipay");
                        urlHandler.sendEmptyMessage(SETQRCODE_VIEW);
                        purchaseCheck(CheckType.OrderPurchase);
                    }
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

    final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {

        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    private Bitmap returnBitMap(String url, String method, String params) {
        URL myFileUrl = null;
        Bitmap bitmap = null;
        try {
            myFileUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) myFileUrl
                    .openConnection();
            if ("POST".equals(method)) {
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded");
                connection.setUseCaches(false);
                connection.setInstanceFollowRedirects(true);
            }
            connection.setRequestMethod(method);
            connection.connect();
            if ("POST".equals(method)) {
                DataOutputStream out = new DataOutputStream(
                        connection.getOutputStream());
                out.writeBytes(params);
                out.flush();
                out.close();
            }
            int code = connection.getResponseCode();
            if (code == 302) {
                String redirectlocation = connection.getHeaderField("Location");
                myFileUrl = new URL(redirectlocation);
                if (myFileUrl.getProtocol().toLowerCase().equals("https")) {
                    trustAllHosts();
                    HttpsURLConnection https = (HttpsURLConnection) myFileUrl.openConnection();
                    https.setHostnameVerifier(DO_NOT_VERIFY);
                    connection = https;
                } else {
                    connection = (HttpsURLConnection) myFileUrl.openConnection();
                }
                connection.setConnectTimeout(2000);
                connection.setRequestMethod("GET");
                connection.connect();
                code = connection.getResponseCode();
            }
            InputStream is = connection.getInputStream();
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
            opt.inPurgeable = true;
            opt.inInputShareable = true;
            // opt.inTempStorage = new byte[1024];
            if (params.contains("alipay")) {
                opt.inSampleSize = 2;
            }
            bitmap = BitmapFactory.decodeStream(is, null, opt);
            is.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * Trust every server - dont check for any certificate
     */
    private static void trustAllHosts() {
        final String TAG = "trustAllHosts";
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {

            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[]{};
            }

            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                Log.i(TAG, "checkClientTrusted");
            }

            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                Log.i(TAG, "checkServerTrusted");
            }
        }};

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void setPackageInfo() {
        videotitle.setText(mItem.title);
        String price = mycontext.getResources().getString(
                R.string.pay_payinfo_price_label);
        String exprice = mycontext.getResources().getString(
                R.string.pay_payinfo_exprice_label);
        String package_info = mycontext.getResources().getString(
                R.string.pay_package_price);
        float itemprice = 0;
        if ("subitem".equalsIgnoreCase(mItem.model_name)) {
            itemprice = mItem.expense.subprice;
        } else if ("item".equalsIgnoreCase(mItem.model_name)) {
            itemprice = mItem.expense.price;
        } else if ("package".equalsIgnoreCase(mItem.model_name)) {
            itemprice = mItem.expense.price;
        }
        payinfo_price.setText(String.format(price, itemprice));
        payinfo_exprice.setText(String.format(exprice, mItem.expense.duration));
        package_price.setText(String.format(package_info, itemprice,
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
        user = "&user=" + SimpleRestClient.mobile_number;
        user_id = "&user_id=" + user_id;
        timestamp = "&timestamp=" + timestamp;
        sid = "&sid=" + sid;
        sn = "&sn=" + sn;

        String params = card_secret + app_name + user + user_id + timestamp
                + sid + sn;
        SimpleRestClient client = new SimpleRestClient();
        client.doSendRequest(CARDRECHARGE_BASE_URL, "post", params,
                rechargeResult);
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

    private boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    private HttpPostRequestInterface fetchBalancerResult = new HttpPostRequestInterface() {
        @Override
        public void onPrepare() {
        }

        @Override
        public void onSuccess(String info) {
            try {
                JSONObject object = new JSONObject(info);
                String balance = StringUtils
                        .isNotEmpty(SimpleRestClient.access_token) ? object
                        .getString("balance") : object.getString("sn_balance");
                float balancefloat = Float.parseFloat(balance);
                String balancevalue = mycontext.getResources().getString(
                        R.string.pay_card_balance_title_label);
                card_balance_title_label.setText(String.format(balancevalue,
                        balancefloat));
                if (balancefloat < mItem.expense.price) {
                    submit_yuepay.setEnabled(false);
                    submit_yuepay.setFocusable(false);
                    submit_yuepay.setFocusableInTouchMode(false);
                    submit_yuepay.setTextColor(getContext().getResources()
                            .getColor(R.color.paychannel_button_disable));
                } else {
                    submit_yuepay.setEnabled(true);
                    submit_yuepay.setFocusable(true);
                    submit_yuepay.setFocusableInTouchMode(true);
                    submit_yuepay.setTextColor(getContext().getResources()
                            .getColor(R.color.white));
                }
                if (flag) {
                    if (balancefloat > mItem.expense.price) {
                        guanyingcard_pay_panel.setVisibility(View.VISIBLE);
                        changeQrcodePayPanelState(false, false);
                        changeLoginPanelState(false);
                        changeshiyuncardPanelState(false);
                        yuepay_button.requestFocus();
                    } else {
                        changeQrcodePayPanelState(true, true);
                        changeLoginPanelState(false);
                        changeYuePayPanelState(false, false);
                        changeshiyuncardPanelState(false);
                    }
                }
                flag = false;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailed(String error) {
        }
    };

    private HttpPostRequestInterface balancePay = new HttpPostRequestInterface() {

        @Override
        public void onPrepare() {
        }

        @Override
        public void onSuccess(String info) {
            try {
                JSONObject object = new JSONObject(info);
                String balance = object.getString("balance");
                float balancefloat = Float.parseFloat(balance);
                String balancevalue = mycontext.getResources().getString(
                        R.string.pay_card_balance_title_label);
                card_balance_title_label.setText(String.format(balancevalue,
                        balancefloat));
                purchaseCheck(CheckType.OrderPurchase);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailed(String error) {
            submit_yuepay.setClickable(true);
        }
    };

    private HttpPostRequestInterface rechargeResult = new HttpPostRequestInterface() {

        @Override
        public void onPrepare() {
        }

        @Override
        public void onSuccess(String info) {
            try {
                JSONObject object = new JSONObject(info);
                String statusString = object.getString("status");
                String errmsg = object.getString("err_desc");
                recharge_error_msg.setVisibility(View.VISIBLE);
                if ("S".equalsIgnoreCase(statusString)) {
                    recharge_error_msg.setText("充值成功,系统将自动为您购买,6s后返回");
                    changeQrcodePayPanelState(false, false);
                    changeLoginPanelState(false);
                    changeYuePayPanelState(true, true);
                    changeshiyuncardPanelState(false);
                } else if ("T".equalsIgnoreCase(statusString)) {
                    recharge_error_msg.setText("充值成功,系统将在第二天8点为您购买,10s后返回");
                } else {
                    recharge_error_msg.setText(errmsg);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            submit_cardpay.setClickable(true);
        }

        @Override
        public void onFailed(String error) {
            recharge_error_msg.setVisibility(View.VISIBLE);
            recharge_error_msg.setText(error);
            submit_cardpay.setClickable(true);
        }
    };

    private void purchaseCheck(CheckType checkType) {
        if ("package".equalsIgnoreCase(mItem.model_name)) {
            orderCheckLoop(checkType, null, String.valueOf(mItem.pk), null);
        } else if ("subitem".equalsIgnoreCase(mItem.model_name)) {
            orderCheckLoop(checkType, null, null, String.valueOf(mItem.pk));
        } else {
            orderCheckLoop(checkType, String.valueOf(mItem.pk), null, null);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                doCancel();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private LoginPanelView.LoginInterface loginInterFace = new LoginPanelView.LoginInterface() {

        @Override
        public void onSuccess(String info) {
            urlHandler.sendEmptyMessage(LOGIN_SUCESS);
            enableButton();
            panel_label.setVisibility(View.GONE);
            getBalanceByToken();
            String welocome = mycontext.getResources().getString(
                    R.string.welocome_tip);
            welocome_tip.setText(String.format(welocome,
                    SimpleRestClient.mobile_number));
            purchaseCheck(CheckType.PlayCheck);
        }

        @Override
        public void onFailed(String error) {
        }

    };

    private void doCancel() {
        paylistener.payResult(false);
        dismiss();
    }

    private String authToken;

    @Override
    public void onLoginSuccess(String result) {

        AuthTokenEntity authTokenEntity = new Gson().fromJson(result,
                AuthTokenEntity.class);
        authToken = authTokenEntity.getAuth_token();

        getBalanceByToken();

        String welocome = mycontext.getResources().getString(
                R.string.welocome_tip);
        welocome_tip.setText(String.format(welocome, nickname));
        DaisyUtils.getVodApplication(getContext()).getEditor()
                .putString(VodApplication.MOBILE_NUMBER, nickname);
        DaisyUtils.getVodApplication(getContext()).getEditor()
                .putString(VodApplication.AUTH_TOKEN, authToken);
        DaisyUtils.getVodApplication(getContext()).save();
        SimpleRestClient.mobile_number = nickname;

        SimpleRestClient.access_token = authToken;
        SimpleRestClient.zuser_token = authTokenEntity.getZuser_token();
        AccountSharedPrefs accountSharedPrefs = AccountSharedPrefs
                .getInstance();
        accountSharedPrefs.setSharedPrefs(AccountSharedPrefs.ZUSER_TOKEN, authTokenEntity.getZuser_token());
        GetFavoriteByNet();
        getHistoryByNet();
        urlHandler.sendEmptyMessage(LOGIN_SUCESS);
        enableButton();
        showLoginSuccessPopup();
    }

    @Override
    public void onLoginFailed() {

    }

    private String nickname;

    @Override
    public void oncallWGQueryQQUserInfo(String nickName) {
        nickname = nickName;

    }

    @Override
    public void onSameAccountListener() {
        Toast.makeText(getContext(), "输入相同账号!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCancelLogin() {

    }

    public interface OrderResultListener {
        public void payResult(boolean result);
    }


    private void getHistoryByNet() {
        mSimpleRestClient.doSendRequest("/api/histories/", "get", "",
                new HttpPostRequestInterface() {

                    @Override
                    public void onSuccess(String info) {
                        // TODO Auto-generated method stub
                        // Log.i(tag, msg);

                        // 解析json
                        mHistoriesByNet = mSimpleRestClient.getItems(info);
                        if (mHistoriesByNet != null) {
                            for (Item i : mHistoriesByNet) {
                                addHistory(i);
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
                        // Log.i(tag, msg);
                    }
                });
    }

    private Item[] FavoriteList;

    private void GetFavoriteByNet() {
        mSimpleRestClient.doSendRequest("/api/bookmarks/", "get", "",
                new HttpPostRequestInterface() {

                    @Override
                    public void onSuccess(String info) {
                        // TODO Auto-generated method stub
                        FavoriteList = mSimpleRestClient.getItems(info);
                        if (FavoriteList != null) {
                            // 添加记录到本地
                            for (Item i : FavoriteList) {
                                addFavorite(i);
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

    private void addHistory(Item item) {
        History history = new History();
        history.title = item.title;
        history.adlet_url = item.adlet_url;
        history.content_model = item.content_model;
        history.is_complex = item.is_complex;
        history.last_position = item.offset;
        history.last_quality = item.quality;
        if ("subitem".equals(item.model_name)) {
            history.sub_url = item.url;
            history.url = SimpleRestClient.root_url + "/api/item/"
                    + item.item_pk + "/";
        } else {
            history.url = item.url;

        }
        history.is_continue = true;
        if (SimpleRestClient.isLogin())
            DaisyUtils.getHistoryManager(getContext()).addHistory(history,
                    "yes");
        else
            DaisyUtils.getHistoryManager(getContext())
                    .addHistory(history, "no");

    }

    private boolean isFavorite(Item mItem) {
        if (mItem != null) {
            String url = mItem.item_url;
            if (url == null && mItem.pk != 0) {
                url = SimpleRestClient.sRoot_url + "/api/item/" + mItem.pk
                        + "/";
            }
            Favorite favorite = null;
            favorite = DaisyUtils.getFavoriteManager(getContext())
                    .getFavoriteByUrl(url, "yes");
            if (favorite != null) {
                return true;
            }
        }

        return false;
    }

    private void addFavorite(Item mItem) {
        if (isFavorite(mItem)) {
            String url = SimpleRestClient.sRoot_url + "/api/item/" + mItem.pk
                    + "/";
            // DaisyUtils.getFavoriteManager(getContext())
            // .deleteFavoriteByUrl(url,"yes");
        } else {
            String url = SimpleRestClient.sRoot_url + "/api/item/" + mItem.pk
                    + "/";
            Favorite favorite = new Favorite();
            favorite.title = mItem.title;
            favorite.adlet_url = mItem.adlet_url;
            favorite.content_model = mItem.content_model;
            favorite.url = url;
            favorite.quality = mItem.quality;
            favorite.is_complex = mItem.is_complex;
            favorite.isnet = "yes";
            DaisyUtils.getFavoriteManager(getContext()).addFavorite(favorite,
                    favorite.isnet);
        }
    }

    private void showLoginSuccessPopup() {
        int xOffset = (int) mycontext.getResources().getDimension(
                R.dimen.loginfragment_successPop_xOffset);
        int yOffset = (int) mycontext.getResources().getDimension(
                R.dimen.loginfragment_successPop_yOffset);
        String msg = mycontext.getText(R.string.login_success_name).toString();
        payment_shadow_view.setVisibility(View.VISIBLE);
        loginPopup = new MessagePopWindow(mycontext);
        loginPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                payment_shadow_view.setVisibility(View.GONE);
            }
        });
        loginPopup.setFirstMessage(String.format(msg,
                SimpleRestClient.mobile_number));
        loginPopup.setSecondMessage(R.string.login_success);
        loginPopup.showAtLocation(login_panel, Gravity.CENTER, xOffset,
                yOffset, new MessagePopWindow.ConfirmListener() {
                    @Override
                    public void confirmClick(View view) {
                        loginPopup.dismiss();
                        payment_shadow_view.setVisibility(View.GONE);
                    }
                }, null);
    }

    private OnHoverListener mOnHoverListener = new OnHoverListener() {
        @Override
        public boolean onHover(View v, MotionEvent keycode) {
            switch (keycode.getAction()) {
                case MotionEvent.ACTION_HOVER_ENTER:
                case MotionEvent.ACTION_HOVER_MOVE:
                    v.requestFocusFromTouch();
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
                    break;
                default:
                    break;
            }
            return false;
        }
    };

    private View.OnClickListener mOnLocalPayClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch ((LocalPayType) v.getTag()) {
                case AliPay:
                    localAlipay();
                    break;
            }
        }
    };

    enum LocalPayType {
        AliPay,
        WeixinPay
    }

    private void localAlipay() {
        fetchPayInfo();
    }


    private void fetchPayInfo() {
        final String deviceToken = SimpleRestClient.device_token;
        final String waresId = String.valueOf(mItem.pk);
        final String waresType = mItem.model_name;
        final String source = "alipay_mb";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(HttpManager.appendProtocol(SimpleRestClient.root_url))
                .client(HttpManager.getInstance().mClient)
                .build();

        final HttpAPI.OrderCreate orderCreate = retrofit.create(HttpAPI.OrderCreate.class);
        Observable.just(alipayInfo)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map(new Func1<String, String>() {
                    @Override
                    public String call(String s) {
                        if (TextUtils.isEmpty(s)) {
                            try {
                                alipayInfo = orderCreate.doRequest(deviceToken, waresId, waresType, source).execute().body().string();
                                return alipayInfo;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        return s;
                    }
                })
                .map(new Func1<String, String>() {
                    @Override
                    public String call(String s) {
                        PayTask alipay = new PayTask((Activity) mycontext);
                        return alipay.pay(alipayInfo, true);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        alipayInfo = null;
                        Log.e(TAG, "fetchPayInfo: " + e.getMessage());
                    }

                    @Override
                    public void onNext(String s) {
                        PayResult payResult = new PayResult(s);
                        String resultInfo = payResult.getResult();// 同步返回需要验证的信息
                        String resultStatus = payResult.getResultStatus();
                        // 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
                        Log.d(TAG, "result code: " + resultStatus + "  status: " + resultInfo);
                        if (TextUtils.equals(resultStatus, "9000")) {
                            Toast.makeText(getContext(), "支付成功", Toast.LENGTH_SHORT).show();
                            purchaseCheck(CheckType.OrderPurchase);
                            alipayInfo = null;
                        } else {
                            // 判断resultStatus 为非"9000"则代表可能支付失败
                            // "8000"代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
                            if (TextUtils.equals(resultStatus, "8000")) {
                                Toast.makeText(getContext(), "支付结果确认中", Toast.LENGTH_SHORT).show();
                            } else {
                                // 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
                                Toast.makeText(getContext(), "支付失败", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }


    @Override
    protected void onStop() {
        if (mOrderCheckLoopSubscription != null) {
            mOrderCheckLoopSubscription.unsubscribe();
        }
        alipayInfo = null;
        super.onStop();
    }

    private void orderCheckLoop(final CheckType checkType, final String item, final String pkg, final String subItem) {
        if (mOrderCheckLoopSubscription != null) {
            mOrderCheckLoopSubscription.unsubscribe();
        }
        mOrderCheckLoopSubscription = Observable.interval(0, 10, TimeUnit.SECONDS)
                .observeOn(Schedulers.io())
                .map(new Func1<Long, String>() {
                    @Override
                    public String call(Long aLong) {
                        Retrofit retrofit = new Retrofit.Builder()
                                .baseUrl(HttpManager.appendProtocol(SimpleRestClient.root_url))
                                .client(HttpManager.getInstance().mClient)
                                .build();
                        switch (checkType) {
                            case PlayCheck:
                                try {
                                    return retrofit.create(NewVipHttpApi.PlayCheck.class)
                                            .doRequest(item, pkg, subItem, SimpleRestClient.device_token, SimpleRestClient.access_token)
                                            .execute().body().string();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            case OrderPurchase:
                                try {
                                    return retrofit.create(NewVipHttpApi.OrderPurchase.class)
                                            .doRequest(item, pkg, subItem, SimpleRestClient.device_token, SimpleRestClient.access_token)
                                            .execute().body().string();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                        }
                        return null;
                    }
                })
                .takeUntil(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String responseBody) {
                        if (TextUtils.isEmpty(responseBody.toString())) {
                            return false;
                        } else {
                            return true;
                        }
                    }
                })
                .take(60)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {
                        Log.i(TAG, "orderCheckLoop onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "orderCheckLoop onError: " + e.getMessage());
                        purchaseCheck(CheckType.OrderPurchase);
                    }

                    @Override
                    public void onNext(String responseBody) {
                        if (responseBody != null && !"0".equals(responseBody)) {
                            paylistener.payResult(true);
                            dismiss();
                        }
                    }
                });
    }

    private enum CheckType {
        PlayCheck,
        OrderPurchase
    }
}