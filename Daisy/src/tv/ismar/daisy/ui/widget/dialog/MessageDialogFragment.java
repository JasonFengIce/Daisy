package tv.ismar.daisy.ui.widget.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import tv.ismar.daisy.R;

/**
 * Created by huaijie on 10/15/15.
 */
public class MessageDialogFragment extends PopupWindow implements View.OnClickListener {
    private Button confirmBtn;
    private Button cancelBtn;
    private TextView firstMessage;
    private TextView secondMessage;
    private ConfirmListener confirmListener;
    private CancelListener cancleListener;

    private String mFirstLineMessage;
    private String mSecondLineMessage;

    private Context mContext;

    public interface CancelListener {
        void cancelClick(View view);
    }

    public interface ConfirmListener {
        void confirmClick(View view);
    }


    public MessageDialogFragment(Context context, String line1Message, String line2Message) {
        mFirstLineMessage = line1Message;
        mSecondLineMessage = line2Message;
        mContext = context;
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        int screenWidth = wm.getDefaultDisplay().getWidth();
        int screenHeight = wm.getDefaultDisplay().getHeight();


        int width = (int) (context.getResources().getDimension(R.dimen.pop_width));
        int height = (int) (context.getResources().getDimension(R.dimen.pop_height));

        setWidth(screenWidth);
        setHeight(screenHeight);

        View contentView = LayoutInflater.from(context).inflate(R.layout.popup_message, null);
        confirmBtn = (Button) contentView.findViewById(R.id.confirm_btn);
        cancelBtn = (Button) contentView.findViewById(R.id.cancel_btn);
        confirmBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
        firstMessage = (TextView) contentView.findViewById(R.id.first_text_info);
        secondMessage = (TextView) contentView.findViewById(R.id.pop_second_text);
        firstMessage.setText(mFirstLineMessage);

        RelativeLayout frameLayout = new RelativeLayout(mContext);
        RelativeLayout.LayoutParams layoutParams;

        if (TextUtils.isEmpty(mSecondLineMessage)) {
            layoutParams = new RelativeLayout.LayoutParams(width, height);
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);

        } else {
            int doubleLineHeight = (int) (context.getResources().getDimension(R.dimen.pop_double_line_height));
            layoutParams = new RelativeLayout.LayoutParams(width, doubleLineHeight);
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            secondMessage.setVisibility(View.VISIBLE);
            secondMessage.setText(mSecondLineMessage);
        }


//        frameLayout.addView(contentView, frameLayout);
        frameLayout.addView(contentView, layoutParams);
//        frameLayout.setBackgroundResource(R.drawable.popwindow_bg);
        setContentView(frameLayout);
        setBackgroundDrawable(context.getResources().getDrawable(R.drawable.pop_bg_drawable));
        setFocusable(true);

    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.confirm_btn:
                if (confirmListener != null) {
                    confirmListener.confirmClick(v);
                }
                break;
            case R.id.cancel_btn:
                if (cancleListener != null) {
                    cancleListener.cancelClick(v);
                }
                break;
        }
    }


    public void showAtLocation(View parent, int gravity, ConfirmListener confirmListener,
                               CancelListener cancleListener) {
        if (confirmListener == null) {
            confirmBtn.setVisibility(View.GONE);
        }

        if (cancleListener == null) {
            cancelBtn.setVisibility(View.GONE);
        }
        this.confirmListener = confirmListener;
        this.cancleListener = cancleListener;
        super.showAtLocation(parent, gravity, 0, 0);
    }
}