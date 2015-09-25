package tv.ismar.sakura.ui.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import tv.ismar.daisy.R;
import tv.ismar.daisy.core.DaisyUtils;

/**
 * Created by huaijie on 9/24/15.
 */
public class MessagePopWindow extends PopupWindow implements View.OnClickListener {
    private Button confirmBtn;
    private Button cancelBtn;
    private TextView firstMessage;
    private TextView secondMessage;
    private ConfirmListener confirmListener;
    private CancelListener cancleListener;

    private float rate;
    private Context mContext;

    public interface CancelListener {
        void cancelClick(View view);
    }

    public interface ConfirmListener {
        void confirmClick(View view);
    }


    public MessagePopWindow(Context context) {
        mContext = context;
        rate = DaisyUtils.getVodApplication(context).getRate(context);
        int width = (int) (context.getResources().getDimension(R.dimen.pop_width));
        int height = (int) (context.getResources().getDimension(R.dimen.pop_height));

        setWidth(width);
        setHeight(height);

        View contentView = LayoutInflater.from(context).inflate(R.layout.popup_message, null);
        confirmBtn = (Button) contentView.findViewById(R.id.confirm_btn);
        cancelBtn = (Button) contentView.findViewById(R.id.cancel_btn);
        confirmBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
        firstMessage = (TextView) contentView.findViewById(R.id.first_text_info);
        secondMessage = (TextView) contentView.findViewById(R.id.pop_second_text);
        setContentView(contentView);

        setBackgroundDrawable(context.getResources().getDrawable(R.drawable.popwindow_bg));
        setFocusable(true);
    }


    public void setBackgroundRes(int resId){
        setBackgroundDrawable(mContext.getResources().getDrawable(resId));
    }

    public void setFirstMessage(int messageId) {
        firstMessage.setText(messageId);
    }

    public void setFirstMessage(String message) {
        firstMessage.setText(message);
    }

    public void setSecondMessage(int messageId) {
        setHeight((int) (mContext.getResources().getDimension(R.dimen.pop_double_line_height) / rate));
        secondMessage.setVisibility(View.VISIBLE);
        secondMessage.setText(messageId);
    }

    public void setSecondMessage(String message) {
        setHeight((int) (mContext.getResources().getDimension(R.dimen.pop_double_line_height) / rate));
        secondMessage.setVisibility(View.VISIBLE);
        secondMessage.setText(message);
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


    public void showAtLocation(View parent, int gravity, int x, int y, ConfirmListener confirmListener,
                               CancelListener cancleListener) {
        if (confirmListener == null) {
            confirmBtn.setVisibility(View.GONE);
        }

        if (cancleListener == null) {
            cancelBtn.setVisibility(View.GONE);
        }
        this.confirmListener = confirmListener;
        this.cancleListener = cancleListener;
        super.showAtLocation(parent, gravity, x, y);
    }
}
