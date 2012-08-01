package tv.ismar.daisy.views;

import tv.ismar.daisy.R;
import android.app.Dialog;
import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class LoadingDialog extends Dialog {
	Context context;
	private ImageView imageLoading;
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
		// TODO Auto-generated method stub
		super.onStart();
		initViews();
	}

	private void initViews() {
		imageLoading = (ImageView) findViewById(R.id.iv_loading);
		tvLoading = (TextView) findViewById(R.id.tv_loading);
		if (null != tvTextName) {
			tvLoading.setText(tvTextName);
		}
		Animation myAnimation = AnimationUtils.loadAnimation(context, R.anim.rotate_loading);
		imageLoading.startAnimation(myAnimation);
	}
	// 使用AnimationUtils类的静态方法loadAnimation()来加载XML中的动画XML文件
}
