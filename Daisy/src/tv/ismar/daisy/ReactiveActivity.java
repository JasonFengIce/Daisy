package tv.ismar.daisy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import tv.ismar.daisy.core.preferences.AccountSharedPrefs;

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
		AccountSharedPrefs accountSharedPrefs = AccountSharedPrefs.getInstance();
		String lock_url = accountSharedPrefs.getSharedPreferences().getString("lock_url", null);
		String lock_content_model = accountSharedPrefs.getSharedPreferences().getString("lock_content_model",null);
		if(lock_url != null && lock_content_model != null){
			Intent intent = new Intent();
			if (("variety".equals(lock_content_model) || "entertainment".equals(lock_content_model))) {
				intent.setAction("tv.ismar.daisy.EntertainmentItem");
				intent.putExtra("title", "娱乐综艺");
			} else if ("movie".equals(lock_content_model)) {
				intent.setAction("tv.ismar.daisy.PFileItem");
				intent.putExtra("title", "电影");
			}else {
				intent.setAction("tv.ismar.daisy.Item");
			}
			intent.putExtra("url", lock_url);
			intent.putExtra("fromPage", "homepage");
			startActivity(intent);
			finish();
		}
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
