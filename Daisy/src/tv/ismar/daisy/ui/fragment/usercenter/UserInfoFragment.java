package tv.ismar.daisy.ui.fragment.usercenter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

import cn.ismartv.activator.IsmartvActivator;
import tv.ismar.daisy.BaseActivity;
import tv.ismar.daisy.R;
import tv.ismar.daisy.VodApplication;
import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.account.AccountChangeListener;
import tv.ismar.daisy.core.account.AccountManager;
import tv.ismar.daisy.core.client.IsmartvUrlClient;
import tv.ismar.daisy.data.usercenter.AccountBalanceEntity;
import tv.ismar.daisy.data.usercenter.AccountPlayAuthEntity;
import tv.ismar.daisy.ui.activity.UserCenterActivity;
import tv.ismar.daisy.ui.adapter.AccoutPlayAuthAdapter;
import tv.ismar.daisy.ui.widget.dialog.MessageDialogFragment;
import tv.ismar.daisy.utils.Util;

/**
 * Created by huaijie on 7/3/15.
 */
public class UserInfoFragment extends Fragment implements View.OnClickListener, AccountChangeListener {
    private static final String TAG = "UserInfoFragment";

    private Context mContext;
    private AccoutPlayAuthAdapter accoutPlayAuthAdapter;
    private SharedPreferences sharedPreferences;

    private TextView deviceNumber;
    private TextView balanceTextView;
    private TextView deviceNameTextView;

    private LinearLayout playAuthListView;
    //    private Button associationText;
//    private Button changeButton;
    private TextView phoneNumber;
    private View fragmentView;
    private LoginFragment loginFragment;
    private LinearLayout userInfoLayout;
    private View phoneNumberLayout;
    private View snNumberLayout;
    private AccountBalanceEntity accountBalanceEntity;

    private Button exitAccountBtn;

    private AccountManager mAccountManager;


    private float rate;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mContext = activity;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rate = DaisyUtils.getVodApplication(getActivity()).getRate(getActivity());
        fragmentView = inflater.inflate(R.layout.fragment_userinfo, null);
        phoneNumber = (TextView) fragmentView.findViewById(R.id.phone_number);
        deviceNumber = (TextView) fragmentView.findViewById(R.id.device_number);
        balanceTextView = (TextView) fragmentView.findViewById(R.id.remain_money_value);
        deviceNameTextView = (TextView) fragmentView.findViewById(R.id.device_name);
        playAuthListView = (LinearLayout) fragmentView.findViewById(R.id.privilegelist);
        userInfoLayout = (LinearLayout) fragmentView.findViewById(R.id.userinfo_layout);

        //退出按钮
        exitAccountBtn = (Button) fragmentView.findViewById(R.id.exit_account);
        exitAccountBtn.setOnClickListener(this);
        exitAccountBtn.setOnHoverListener(new View.OnHoverListener() {
			
			@Override
			public boolean onHover(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_HOVER_ENTER
						|| event.getAction() == MotionEvent.ACTION_HOVER_MOVE) {
					v.requestFocus();
				}
				return false;
			}
		});
//
//        changeButton = (Button) fragmentView.findViewById(R.id.change);
        exitAccountBtn.setNextFocusUpId(exitAccountBtn.getId());


        phoneNumberLayout = fragmentView.findViewById(R.id.phone_number_layout);
        snNumberLayout = fragmentView.findViewById(R.id.sn_number_layout);

        deviceNameTextView.setText(Build.PRODUCT.replace(" ", "_"));

//        changeButton.setOnClickListener(this);

        loginFragment = new LoginFragment();
        getChildFragmentManager().beginTransaction().add(R.id.association_phone_layout, loginFragment).commit();
        getChildFragmentManager().beginTransaction().hide(loginFragment).commit();


//        fragmentView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (hasFocus) {
//                    if (!TextUtils.isEmpty(SimpleRestClient.mobile_number)) {
//                        exitAccountBtn.requestFocus();
//                    } else {
//                        if (playAuthListView.getChildAt(0) != null)
//                            playAuthListView.getChildAt(0).requestFocus();
//                    }
//
//                }
//            }
//        });
        return fragmentView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mAccountManager = AccountManager.getInstance(mContext);
        mAccountManager.addAccounChangeListener(this);
        if (mAccountManager.isLogin()) {
            phoneNumberLayout.setVisibility(View.VISIBLE);
        } else {
            phoneNumberLayout.setVisibility(View.GONE);
        }
    }

    //    public void changge() {
//
//
//        playAuthListView.setFocusable(false);
//        associationText.setFocusable(false);
//        if (!TextUtils.isEmpty(SimpleRestClient.mobile_number)) {
//            Log.i("qihuanzhanghu", "phoneNumberLayout VISIBLE");
//            phoneNumberLayout.setVisibility(View.VISIBLE);
//        }
//
//        if (isCombined) {
//            associationText.setVisibility(View.GONE);
//            associationPrompt.setVisibility(View.GONE);
//        }
//
//        fetchAccountsBalance();
//        fetchAccountsPlayauths();
//        initViewByLoginStatus();
//    }

    @Override
    public void onResume() {
        super.onResume();
        if (accountBalanceEntity == null) {
            if (mAccountManager.isLogin()) {
                phoneNumberLayout.setVisibility(View.VISIBLE);
            } else {
                phoneNumberLayout.setVisibility(View.GONE);
            }

            fetchAccountsBalance();
            fetchAccountsPlayauths();
            initViewByLoginStatus();
        }
    }

    private void fetchAccountsBalance() {
        String api = SimpleRestClient.root_url + "/accounts/balance/";
        new IsmartvUrlClient().doRequest(api, new IsmartvUrlClient.CallBack() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "fetchAccountBalance: " + result);
                accountBalanceEntity = new Gson().fromJson(result, AccountBalanceEntity.class);
                balanceTextView.setText(String.valueOf(accountBalanceEntity.getBalance() + accountBalanceEntity.getSn_balance()));
            }

            @Override
            public void onFailed(Exception exception) {

            }
        });
    }

    private void fetchAccountsPlayauths() {
        String api = SimpleRestClient.root_url + "/accounts/playauths/";

        String timestamp = String.valueOf(System.currentTimeMillis());
        IsmartvActivator activator = IsmartvActivator.getInstance(mContext);
        String sign = activator.PayRsaEncode("sn=" + SimpleRestClient.sn_token + "&timestamp=" + timestamp);

        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("timestamp", timestamp);
        params.put("sign", sign);


        new IsmartvUrlClient().doRequest(IsmartvUrlClient.Method.POST, api, params, new IsmartvUrlClient.CallBack() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "fetchAccountsPlayauths: " + result);
                AccountPlayAuthEntity accountPlayAuthEntity = new Gson().fromJson(result, AccountPlayAuthEntity.class);
                ArrayList<AccountPlayAuthEntity.PlayAuth> playAuths = new ArrayList<AccountPlayAuthEntity.PlayAuth>();
                playAuths.addAll(accountPlayAuthEntity.getSn_playauth_list());
                playAuths.addAll(accountPlayAuthEntity.getPlayauth_list());

                createPlayAuthListView(playAuths);
            }

            @Override
            public void onFailed(Exception exception) {

            }
        });
    }

    private View.OnFocusChangeListener playAuthFocusListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            TextView titleTextView = (TextView) v.findViewById(R.id.title_txt);
            TextView remaindDay = (TextView) v.findViewById(R.id.buydate_txt);
            if (hasFocus) {
                titleTextView.setTextColor(mContext.getResources().getColor(R.color.location_text_focus));
                titleTextView.setTextSize(mContext.getResources().getDimension(R.dimen.userinfo_playauth_item_focus_textsize) / rate);
                remaindDay.setTextColor(mContext.getResources().getColor(R.color.location_text_focus));
                remaindDay.setTextSize(mContext.getResources().getDimension(R.dimen.userinfo_playauth_item_focus_textsize) / rate);
            } else {
                titleTextView.setTextColor(mContext.getResources().getColor(R.color.white));
                titleTextView.setTextSize(mContext.getResources().getDimension(R.dimen.userinfo_playauth_item_normal_textsize) / rate);
                remaindDay.setTextColor(mContext.getResources().getColor(R.color.white));
                remaindDay.setTextSize(mContext.getResources().getDimension(R.dimen.userinfo_playauth_item_normal_textsize) / rate);
            }

        }
    };

    private View.OnClickListener playAuthClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AccountPlayAuthEntity.PlayAuth playAuth = (AccountPlayAuthEntity.PlayAuth) v.getTag();
//            if (!TextUtils.isEmpty(url)) {
//                InitPlayerTool tool = new InitPlayerTool(mContext);
//                tool.initClipInfo(url, InitPlayerTool.FLAG_URL);
//            }
            if(playAuth.getUrl() != null && !playAuth.getUrl().equals(""))
            DaisyUtils.gotoSpecialPage(mContext,playAuth.getContentMode(),playAuth.getUrl(),"");
        }
    };

    private void createPlayAuthListView(ArrayList<AccountPlayAuthEntity.PlayAuth> playAuths) {
        if (playAuths.size() == 0) {
            playAuthListView.setFocusable(false);
            ((ScrollView) (playAuthListView.getParent())).setFocusable(false);
            fragmentView.setFocusable(false);
        } else {
            playAuthListView.removeAllViews();
            String remainday = mContext.getResources().getString(R.string.personcenter_orderlist_item_remainday);
            for (int i = 0; i < playAuths.size(); i++) {
                View convertView = LayoutInflater.from(mContext).inflate(R.layout.privilege_listview_item, null);
                TextView title = (TextView) convertView.findViewById(R.id.title_txt);
                TextView buydate_txt = (TextView) convertView.findViewById(R.id.buydate_txt);
                convertView.setTag(playAuths.get(i));
                buydate_txt.setText(String.format(remainday, remaindDay(playAuths.get(i).getExpiry_date())));
                title.setText(playAuths.get(i).getTitle());
//                convertView.setOnFocusChangeListener(playAuthFocusListener);
                convertView.setOnClickListener(playAuthClickListener);
//                convertView.setOnHoverListener(new View.OnHoverListener() {
//
//        			@Override
//        			public boolean onHover(View v, MotionEvent event) {
//        				if (event.getAction() == MotionEvent.ACTION_HOVER_ENTER
//        						|| event.getAction() == MotionEvent.ACTION_HOVER_MOVE) {
//        					v.requestFocus();
//        				}
//        				return false;
//        			}
//        		});
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                if (i == 0) {
                    convertView.setId(R.id.userinfo_palyauth_list_first_id);
                    convertView.setNextFocusUpId(exitAccountBtn.getId());
                    exitAccountBtn.setNextFocusDownId(convertView.getId());
                }

                if (i != 0) {
                    layoutParams.setMargins(0, 51, 0, 0);

                }
                if (i == playAuths.size() - 1) {
                    if (playAuths.size() != 1) {
                        convertView.setId(R.id.userinfo_palyauth_list_last_id);
                    }
                    convertView.setNextFocusDownId(convertView.getId());
                }
                convertView.setLayoutParams(layoutParams);
                playAuthListView.addView(convertView);

            }
        }
    }


    private int remaindDay(String exprieTime) {
        try {
            return Util.daysBetween(Util.getTime(), exprieTime) + 1;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.association_button:
                if (!SimpleRestClient.isLogin()) {
                    ((BaseActivity) getActivity()).loginQQorWX();
                } else {
                    loginFragment.showAccountsCombinePopup();
                }

                break;
            case R.id.change:
                playAuthListView.setFocusable(false);
//                ((UserCenterActivity) getActivity()).setAccountListener(new UserCenterActivity.OnLoginByChangeCallback() {
//
//                    @Override
//                    public void onLoginSuccess() {
//                        Log.i("qihuanzhanghu", "onLoginSuccess");
//                        playAuthListView.setFocusable(true);
//                        onResume();
//                    }
//                });
//                ((BaseActivity) getActivity()).changaccount();
                loginFragment.setBackground(true);
                getChildFragmentManager().beginTransaction().show(loginFragment).commit();
                break;
            case R.id.exit_account:
                showExitAccountConfirmPop();
                break;
        }
    }

    private void initViewByLoginStatus() {
        deviceNumber.setText(SimpleRestClient.sn_token);
        Log.i("qihuanzhanghu", "mobile_number==" + SimpleRestClient.mobile_number);
        if (!DaisyUtils.getVodApplication(mContext).getPreferences().getString(VodApplication.AUTH_TOKEN, "").equals("")) {
            Log.i("qihuanzhanghu", "mobile_number mobile_number");
            phoneNumber.setText(SimpleRestClient.mobile_number);
        }
    }


    private void showExitAccountConfirmPop() {
        final MessageDialogFragment dialog = new MessageDialogFragment(mContext, getString(R.string.confirm_exit_account_text), null);
        dialog.showAtLocation(getView(), Gravity.CENTER, new MessageDialogFragment.ConfirmListener() {
                    @Override
                    public void confirmClick(View view) {
                        dialog.dismiss();
                        showExitAccountMessagePop();
                        ((UserCenterActivity) getActivity()).selectUserInfoIndicator();
                        mAccountManager.commonLogout();

                    }
                },
                new MessageDialogFragment.CancelListener() {
                    @Override
                    public void cancelClick(View view) {
                        dialog.dismiss();
                    }
                }

        );


    }

    private void showExitAccountMessagePop() {
        final MessageDialogFragment dialog = new MessageDialogFragment(mContext, getString(R.string.exit_account_message_text), null);
        dialog.showAtLocation(getView(), Gravity.CENTER, new MessageDialogFragment.ConfirmListener() {
                    @Override
                    public void confirmClick(View view) {
                        dialog.dismiss();

                    }
                },
                null
        );
    }


    @Override
    public void onLogin() {

    }

    @Override
    public void onLogout() {
        phoneNumberLayout.setVisibility(View.GONE);
    }
}
