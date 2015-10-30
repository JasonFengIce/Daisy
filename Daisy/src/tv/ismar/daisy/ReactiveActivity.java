package tv.ismar.daisy;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class ReactiveActivity extends Activity {
	private LinearLayout layout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reactive);
		WindowManager.LayoutParams lp=getWindow().getAttributes();
		lp.dimAmount=0.75f;
		getWindow().setAttributes(lp);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
//		layout = (LinearLayout) findViewById(R.id.rea);
//		layout.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				Toast.makeText(getApplicationContext(), "提示：点击窗口外部关闭窗口！",
//						Toast.LENGTH_SHORT).show();
//			}
//		});
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		finish();
		return true;
	}

	public void exitbutton1(View v) {
		this.finish();
	}

	public void exitbutton0(View v) {
		this.finish();
	}
}
