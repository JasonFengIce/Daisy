package tv.ismar.daisy.ui.fragment.usercenter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import org.w3c.dom.Text;
import tv.ismar.daisy.R;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.client.IsmartvUrlClient;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by huaijie on 7/3/15.
 */
public class LoginFragment extends Fragment implements View.OnClickListener {

    private Context mContext;

    private EditText phoneNumberEdit;
    private EditText verificationEdit;

    private Button fetchVerificationBtn;
    private Button submitBtn;

    private TextView phoneNumberPrompt;
    private TextView verificationPrompt;

    private int count;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mContext = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, null);
        phoneNumberEdit = (EditText) view.findViewById(R.id.login_phone_edit);
        verificationEdit = (EditText) view.findViewById(R.id.login_verification_edit);
        fetchVerificationBtn = (Button) view.findViewById(R.id.fetch_verification_btn);
        fetchVerificationBtn.setOnClickListener(this);
        submitBtn = (Button) view.findViewById(R.id.submit_btn);
        submitBtn.setOnClickListener(this);
        phoneNumberPrompt = (TextView) view.findViewById(R.id.phone_number_prompt);
        verificationPrompt = (TextView) view.findViewById(R.id.verification_prompt);

        return view;
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
                countDown();
            }

            @Override
            public void onFailed(Exception exception) {

            }
        });

    }

    private void login() {
        String phoneNumber = phoneNumberEdit.getText().toString();
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

    }

    private void countDown() {
//        fetchVerificationBtn.setEnabled(false);
        for (count = 60; count > 0; count--) {
            fetchVerificationBtn.setText(count + "秒");
        }
        fetchVerificationBtn.setText(R.string.association_fetch_verification);
    }
}
