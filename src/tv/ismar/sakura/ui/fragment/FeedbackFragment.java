package tv.ismar.sakura.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import tv.ismar.daisy.R;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.sakura.core.FeedbackProblem;
import tv.ismar.sakura.core.IpLookUpCache;
import tv.ismar.sakura.core.SakuraClientAPI;
import tv.ismar.sakura.core.UploadFeedback;
import tv.ismar.sakura.data.http.ChatMsgEntity;
import tv.ismar.sakura.data.http.FeedBackEntity;
import tv.ismar.sakura.data.http.ProblemEntity;
import tv.ismar.sakura.ui.adapter.FeedbackListAdapter;
import tv.ismar.sakura.ui.widget.FeedBackListView;
import tv.ismar.sakura.ui.widget.MessageSubmitButton;
import tv.ismar.sakura.ui.widget.SakuraEditText;

import java.util.List;

import static tv.ismar.sakura.core.SakuraClientAPI.restAdapter_IRIS_TVXIO;

/**
 * Created by huaijie on 2015/4/8.
 */
public class FeedbackFragment extends Fragment implements RadioGroup.OnCheckedChangeListener,
        View.OnClickListener {
    private static final String TAG = "FeedbackFragment";


    private int problemTextFlag = 6;
    private RadioGroup problemType;
    private TextView snCodeTextView;
    private FeedBackListView feedBackListView;
    private MessageSubmitButton submitButton;

    private SakuraEditText phoneNumberText;
    private SakuraEditText descriptioinText;

    private String snCode = TextUtils.isEmpty(SimpleRestClient.sn_token) ? "sn is null" : SimpleRestClient.sn_token;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sakura_fragment_feedback, null);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        problemType = (RadioGroup) view.findViewById(R.id.problem_options);
        problemType.setOnCheckedChangeListener(this);
        snCodeTextView = (TextView) view.findViewById(R.id.sn_code);
        snCodeTextView.append(snCode);
        feedBackListView = (FeedBackListView) view.findViewById(R.id.feedback_list);
        submitButton = (MessageSubmitButton) view.findViewById(R.id.submit_btn);
        submitButton.setOnClickListener(this);
        phoneNumberText = (SakuraEditText) view.findViewById(R.id.phone_number_edit);
        descriptioinText = (SakuraEditText) view.findViewById(R.id.description_edit);

    }

    @Override
    public void onResume() {
        super.onResume();
        createProblemsRadio(FeedbackProblem.getInstance().getCache());
        fetchFeedback(snCode, "5");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.submit_btn:
                uploadFeedback();
                break;
        }
    }

    private void createProblemsRadio(List<ProblemEntity> problemEntities) {
        RadioButton mRadioButton = null;
        for (int i = 0; i < problemEntities.size(); i++) {
            RadioButton radioButton = new RadioButton(getActivity());
            RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(
                    RadioGroup.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, (int) getResources().getDimension(R.dimen.feedback_radiogroup_margin), 0);
            radioButton.setLayoutParams(params);
            radioButton.setTextSize(22);
            radioButton.setText(problemEntities.get(i).getPoint_name());
            radioButton.setId(problemEntities.get(i).getPoint_id());

            if (i == 0)
                mRadioButton = radioButton;
            problemType.addView(radioButton);
        }

        if (null != mRadioButton)
            mRadioButton.setChecked(true);
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        problemTextFlag = i;
    }


    private void fetchFeedback(String sn, String top) {
        SakuraClientAPI.Feedback client = restAdapter_IRIS_TVXIO.create(SakuraClientAPI.Feedback.class);
        client.excute(sn, top, new Callback<ChatMsgEntity>() {
            @Override
            public void success(ChatMsgEntity chatMsgEntities, Response response) {
                feedBackListView.setAdapter(new FeedbackListAdapter(getActivity(), chatMsgEntities.getData()));
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.e(TAG, "fetchFeedback: " + retrofitError.getMessage());
            }
        });
    }

    private void uploadFeedback() {
        IpLookUpCache ipLookUpCache = IpLookUpCache.getInstance(getActivity());
        FeedBackEntity feedBack = new FeedBackEntity();
        feedBack.setDescription(descriptioinText.getText().toString());
        feedBack.setPhone(phoneNumberText.getText().toString());
        feedBack.setOption(problemTextFlag);
        feedBack.setCity(ipLookUpCache.getUserCity());
        feedBack.setIp(ipLookUpCache.getUserIp());
        feedBack.setIsp(ipLookUpCache.getUserIsp());
        feedBack.setLocation(ipLookUpCache.getUserProvince());
        UploadFeedback.getInstance().excute(feedBack, snCode, new UploadFeedback.Callback() {
            @Override
            public void success(String msg) {
                Log.d(TAG, "uploadFeedback: " + msg);
            }

            @Override
            public void failure(String msg) {
                Log.d(TAG, "uploadFeedback: " + msg);
            }
        });
    }
}
