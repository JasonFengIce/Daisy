package tv.ismar.daisy.ui.widget.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import tv.ismar.daisy.R;

/**
 * Created by huaijie on 10/15/15.
 */
public class MessageDialogFragment extends DialogFragment implements View.OnClickListener {

    private Button confirmBtn;
    private Button cancelBtn;
    private TextView firstMessage;
    private TextView secondMessage;
    private ConfirmListener mConfirmListener;
    private CancelListener mCancleListener;
    private float density;
    private FrameLayout frameLayout;


    private int mFristMessage;
    private int mSecondMessage;

    private int mWidth;
    private int mHeight;


    public interface CancelListener {
        void cancelClick(View view);
    }

    public interface ConfirmListener {
        void confirmClick(View view);
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        density = getResources().getDisplayMetrics().density;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.popup_message, null);

        confirmBtn = (Button) dialogView.findViewById(R.id.confirm_btn);
        cancelBtn = (Button) dialogView.findViewById(R.id.cancel_btn);
        confirmBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
        firstMessage = (TextView) dialogView.findViewById(R.id.first_text_info);
        secondMessage = (TextView) dialogView.findViewById(R.id.pop_second_text);

        if (mConfirmListener == null) {
            confirmBtn.setVisibility(View.GONE);
        }

        if (mCancleListener == null) {
            cancelBtn.setVisibility(View.GONE);
        }
        mWidth = (int) (getResources().getDimension(R.dimen.pop_width) / density);
        mHeight = (int) (getResources().getDimension(R.dimen.pop_height) / density);
        firstMessage.setText(getString(mFristMessage));

        if (mSecondMessage != 0) {
            mHeight = (int) (getResources().getDimension(R.dimen.pop_double_line_height) / density);
            secondMessage.setVisibility(View.VISIBLE);
            secondMessage.setText(getString(mSecondMessage));
        }


        frameLayout = new FrameLayout(getActivity());
        frameLayout.setBackgroundResource(R.drawable.popwindow_bg);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(mWidth, mHeight);
        frameLayout.addView(dialogView, layoutParams);

        builder.setView(frameLayout);
        return builder.create();
    }

//    public void setBackgroundRes(int resId) {
//        setBackgroundDrawable(mContext.getResources().getDrawable(resId));
//    }

    public void setFirstMessage(int messageId) {
        mFristMessage = messageId;

    }


    public void setSecondMessage(int messageId) {
        mSecondMessage = messageId;
    }


    public void show(FragmentManager manager, String tag, ConfirmListener confirmListener,
                     CancelListener cancleListener) {
        mConfirmListener = confirmListener;
        mCancleListener = cancleListener;
        super.show(manager, tag);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.confirm_btn:
                if (mConfirmListener != null) {
                    mConfirmListener.confirmClick(v);
                }
                break;
            case R.id.cancel_btn:
                if (mCancleListener != null) {
                    mCancleListener.cancelClick(v);
                }
                break;
        }
    }

}
