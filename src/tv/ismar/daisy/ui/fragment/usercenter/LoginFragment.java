package tv.ismar.daisy.ui.fragment.usercenter;

import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import cn.ismartv.activator.Activator;
import com.google.gson.Gson;
import tv.ismar.daisy.R;
import tv.ismar.daisy.VodApplication;
import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.client.IsmartvUrlClient;
import tv.ismar.daisy.core.receiver.TimeCountdownBroadcastSender;
import tv.ismar.daisy.data.usercenter.AuthTokenEntity;
import tv.ismar.daisy.models.Favorite;
import tv.ismar.daisy.models.Item;
import tv.ismar.daisy.ui.activity.UserCenterActivity;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by huaijie on 7/3/15.
 */
public class LoginFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "LoginFragment";

    public static final String ACCOUNT_SHARED_PREFS = "account";
    public static final String ACCOUNT_COMBINE = "combine";


    private Context mContext;

    private EditText phoneNumberEdit;
    private EditText verificationEdit;

    private Button fetchVerificationBtn;
    private Button submitBtn;

    private TextView phoneNumberPrompt;
    private TextView verificationPrompt;

    private PopupWindow loginPopup;
    private PopupWindow combineAccountPop;

    private View fragmentView;


    private OnLoginCallback loginCallback;


    private IntentFilter intentFilter;

    private SharedPreferences accountSharedPrefs;


    public interface OnLoginCallback {
        void onLoginSuccess();
    }

    public void setLoginCallback(OnLoginCallback loginCallback) {
        this.loginCallback = loginCallback;
    }

    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);
        this.mContext = activity;
        accountSharedPrefs = activity.getSharedPreferences(ACCOUNT_SHARED_PREFS, Context.MODE_PRIVATE);
    }


    @Override
    public void onResume() {
        super.onResume();
        intentFilter = new IntentFilter();
        intentFilter.addAction(TimeCountdownBroadcastSender.ACTION_TIME_COUNTDOWN);
        mContext.registerReceiver(countdownReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        mContext.unregisterReceiver(countdownReceiver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_login, null);
        phoneNumberEdit = (EditText) fragmentView.findViewById(R.id.login_phone_edit);
        verificationEdit = (EditText) fragmentView.findViewById(R.id.login_verification_edit);
        fetchVerificationBtn = (Button) fragmentView.findViewById(R.id.fetch_verification_btn);
        fetchVerificationBtn.setOnClickListener(this);
        submitBtn = (Button) fragmentView.findViewById(R.id.submit_btn);
        submitBtn.setOnClickListener(this);
        phoneNumberPrompt = (TextView) fragmentView.findViewById(R.id.phone_number_prompt);
        verificationPrompt = (TextView) fragmentView.findViewById(R.id.verification_prompt);

        return fragmentView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public static boolean isMobileNumber(String mobiles) {
        Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");
        Matcher m = p.matcher(mobiles);
        return m.matches();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fetch_verification_btn:
                fetchVerificationCode();
                break;
            case R.id.submit_btn:
                login();
                break;
        }
    }

    private void fetchVerificationCode() {
//        phoneNumberEdit.setText("15370770697");
        String phoneNumber = phoneNumberEdit.getText().toString();
        if (TextUtils.isEmpty(phoneNumber)) {
            phoneNumberPrompt.setText(mContext.getText(R.string.phone_number_not_be_null));
            return;
        }

        if (!isMobileNumber(phoneNumber)) {
            phoneNumberPrompt.setText(mContext.getText(R.string.not_phone_number));
            return;
        }

        phoneNumberPrompt.setText("");

        String api = SimpleRestClient.root_url + "/accounts/auth/";
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("device_token", SimpleRestClient.device_token);
        params.put("username", phoneNumber);
        new IsmartvUrlClient().doRequest(IsmartvUrlClient.Method.POST, api, params, new IsmartvUrlClient.CallBack() {
            @Override
            public void onSuccess(String result) {
                phoneNumberPrompt.setText(R.string.fetch_verification_success);
                TimeCountdownBroadcastSender.getInstance(mContext).start(60);
            }

            @Override
            public void onFailed(Exception exception) {
                phoneNumberPrompt.setText(R.string.fetch_verification_failure);
            }
        });

    }

    private void login() {
        final String phoneNumber = phoneNumberEdit.getText().toString();
        String verificationCode = verificationEdit.getText().toString();

        if (TextUtils.isEmpty(phoneNumber)) {
            phoneNumberPrompt.setText(mContext.getText(R.string.phone_number_not_be_null));
        }

        if (TextUtils.isEmpty(verificationCode)) {
            verificationPrompt.setText(mContext.getText(R.string.verification_not_be_null));
        }

        if (TextUtils.isEmpty(phoneNumber) || TextUtils.isEmpty(verificationCode)) {
            return;
        }


        phoneNumberPrompt.setText("");
        verificationPrompt.setText("");

        String api = SimpleRestClient.root_url + "/accounts/login/";
        HashMap params = new HashMap();
        params.put("device_token", SimpleRestClient.device_token);
        params.put("username", phoneNumber);
        params.put("auth_number", verificationCode);
        new IsmartvUrlClient().doRequest(IsmartvUrlClient.Method.POST, api, params, new IsmartvUrlClient.CallBack() {
            @Override
            public void onSuccess(String result) {
                if (loginCallback != null) {
                    loginCallback.onLoginSuccess();
                }

                AuthTokenEntity authTokenEntity = new Gson().fromJson(result, AuthTokenEntity.class);
                saveToLocal(authTokenEntity.getAuth_token(), phoneNumber);
                submitBtn.clearFocus();
                showLoginSuccessPopup();
            }

            @Override
            public void onFailed(Exception exception) {
                verificationPrompt.setText(R.string.login_failure);

            }
        });

    }


    private void accountsCombine() {
        String api = SimpleRestClient.root_url + "/accounts/combine/";
        long timestamp = System.currentTimeMillis();
        Activator activator = Activator.getInstance(mContext);
        String rsaResult = activator.PayRsaEncode("sn=" + SimpleRestClient.sn_token + "&timestamp=" + timestamp);

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("device_token", SimpleRestClient.device_token);
        params.put("access_token", SimpleRestClient.access_token);
        params.put("timestamp", String.valueOf(timestamp));
        params.put("sign", rsaResult);

        new IsmartvUrlClient().doRequest(IsmartvUrlClient.Method.POST, api, params, new IsmartvUrlClient.CallBack() {
            @Override
            public void onSuccess(String result) {
                SharedPreferences.Editor editor = accountSharedPrefs.edit();
                editor.putBoolean(ACCOUNT_COMBINE, true);
                editor.apply();

                Log.d(TAG, "accountsCombine: " + result);
            }

            @Override
            public void onFailed(Exception exception) {
                SharedPreferences.Editor editor = accountSharedPrefs.edit();
                editor.putBoolean(ACCOUNT_COMBINE, false);
                editor.apply();
                Log.e(TAG, "accountsCombine: " + exception.getMessage());
            }
        });


    }


    private void showLoginSuccessPopup() {
        View popupLayout = LayoutInflater.from(mContext).inflate(R.layout.popup_login_success, null);
        TextView textView = (TextView) popupLayout.findViewById(R.id.login_success_msg);
        String msg = mContext.getText(R.string.login_success).toString();
        String phoneNumber = phoneNumberEdit.getText().toString();
        textView.setText(String.format(msg, phoneNumber));

        Button button = (Button) popupLayout.findViewById(R.id.login_success_btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginPopup.dismiss();
                showAccountsCombinePopup();
            }
        });


        int width = (int) mContext.getResources().getDimension(R.dimen.login_pop_width);
        int height = (int) mContext.getResources().getDimension(R.dimen.login_pop_height);
        loginPopup = new PopupWindow(popupLayout, width, height);
        loginPopup.setFocusable(true);
        loginPopup.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.transparent));
        loginPopup.showAtLocation(fragmentView, Gravity.CENTER, 200, 0);
    }


    public void showAccountsCombinePopup() {
        View popupLayout = LayoutInflater.from(mContext).inflate(R.layout.popup_account_combine, null);
        int width = (int) mContext.getResources().getDimension(R.dimen.login_pop_width);
        int height = (int) mContext.getResources().getDimension(R.dimen.login_pop_height);
        combineAccountPop = new PopupWindow(popupLayout, width, height);
        combineAccountPop.setFocusable(true);
        combineAccountPop.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.transparent));
        combineAccountPop.showAtLocation(fragmentView, Gravity.CENTER, 200, 0);

        Button confirm = (Button) popupLayout.findViewById(R.id.confirm_account_combine);
        Button cancel = (Button) popupLayout.findViewById(R.id.cancel_account_combine);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accountsCombine();
                combineAccountPop.dismiss();
                ((UserCenterActivity) mContext).switchToUserInfoFragment();

            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = accountSharedPrefs.edit();
                editor.putBoolean(ACCOUNT_COMBINE, false);
                editor.apply();

                combineAccountPop.dismiss();
                ((UserCenterActivity) mContext).switchToUserInfoFragment();
            }
        });


    }


    private void saveToLocal(String authToken, String phoneNumber) {
        DaisyUtils.getVodApplication(mContext).getEditor().putString(VodApplication.AUTH_TOKEN, authToken);
        DaisyUtils.getVodApplication(mContext).getEditor().putString(VodApplication.MOBILE_NUMBER, phoneNumber);
        DaisyUtils.getVodApplication(mContext).save();
        SimpleRestClient.access_token = authToken;
        SimpleRestClient.mobile_number = phoneNumber;
        fetchFavorite();
    }


    private void fetchFavorite() {
        String api = SimpleRestClient.root_url + "/api/bookmarks/";

        new IsmartvUrlClient().doRequest(api, new IsmartvUrlClient.CallBack() {
            @Override
            public void onSuccess(String result) {
                Item[] favoriteList = new Gson().fromJson(result, Item[].class);
                for (Item item : favoriteList) {
                    addFavorite(item);
                }
            }

            @Override
            public void onFailed(Exception exception) {
                Log.e(TAG, "fetchFavorite: " + exception.getMessage());
            }
        });

    }


    //
    private void addFavorite(Item mItem) {
        if (isFavorite(mItem)) {
            String url = SimpleRestClient.sRoot_url + "/api/item/" + mItem.pk + "/";
            // DaisyUtils.getFavoriteManager(getContext())
            // .deleteFavoriteByUrl(url,"yes");
        } else {
            String url = SimpleRestClient.sRoot_url + "/api/item/" + mItem.pk + "/";
            Favorite favorite = new Favorite();
            favorite.title = mItem.title;
            favorite.adlet_url = mItem.adlet_url;
            favorite.content_model = mItem.content_model;
            favorite.url = url;
            favorite.quality = mItem.quality;
            favorite.is_complex = mItem.is_complex;
            favorite.isnet = "yes";
            DaisyUtils.getFavoriteManager(mContext).addFavorite(favorite, favorite.isnet);
        }
    }

    private boolean isFavorite(Item mItem) {
        if (mItem != null) {
            String url = mItem.item_url;
            if (url == null && mItem.pk != 0) {
                url = SimpleRestClient.sRoot_url + "/api/item/" + mItem.pk + "/";
            }
            Favorite favorite = DaisyUtils.getFavoriteManager(mContext).getFavoriteByUrl(url, "yes");
            if (favorite != null) {
                return true;
            }
        }

        return false;
    }


    public void setBackground(boolean background) {
        if (background) {
            getView().setBackgroundColor(0xAA000000);
        } else {
            getView().setBackgroundColor(0x00000000);
        }
    }

    private BroadcastReceiver countdownReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int count = intent.getIntExtra(TimeCountdownBroadcastSender.COUNT, 0);
            if (count > 0) {
                if (fetchVerificationBtn.isEnabled())
                    fetchVerificationBtn.setEnabled(false);
                fetchVerificationBtn.setText(count + "秒");
            } else {
                if (!fetchVerificationBtn.isEnabled()) {
                    fetchVerificationBtn.setEnabled(true);
                    fetchVerificationBtn.setText(R.string.association_fetch_verification);
                }
            }
        }
    };


}
