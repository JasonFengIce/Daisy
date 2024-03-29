package tv.ismar.daisy.views;

import android.app.Dialog;
import android.content.Context;
import android.view.MotionEvent;
import android.widget.TextView;
import tv.ismar.daisy.R;

/**
 * 自定义Dialog
 * @author liuhao
 *
 */
public class LoadingDialog extends Dialog {
	Context context;
	private TextView tvLoading;
	String tvTextName = null;

	public LoadingDialog(Context context) {
		super(context, R.style.MyDialog);
		this.context = context;
		this.setContentView(R.layout.loading_dialog);
	}

	public LoadingDialog(Context context, String tvText) {
		super(context, R.style.MyDialog);
		this.context = context;
		this.setContentView(R.layout.loading_dialog);
		this.tvTextName = tvText;
	}

	@Override
	protected void onStart() {
		super.onStart();
		initViews();
	}

	private void initViews() {
		tvLoading = (TextView) findViewById(R.id.tv_loading);
		if (null != tvTextName) {
			tvLoading.setText(tvTextName);
		}
	}
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		return false;
	}
}
