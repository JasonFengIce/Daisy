package tv.ismar.daisy.core;

import tv.ismar.daisy.R;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SearchPromptDialog extends Dialog implements android.view.View.OnClickListener {

	Context context;
	Button btnDialogOK;
	Button btnDialogSetting;

	public SearchPromptDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		this.context = context;
	}

	public SearchPromptDialog(Context context, int theme) {
		super(context, theme);
		this.context = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.search_dialog);
		initViews();
	}

	private void initViews() {
		btnDialogOK = (Button) findViewById(R.id.btndialog_ok);
		btnDialogOK.setOnClickListener(SearchPromptDialog.this);
		btnDialogSetting = (Button) findViewById(R.id.btndialog_setting);
		btnDialogSetting.setOnClickListener(SearchPromptDialog.this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btndialog_ok:
			this.dismiss();
			break;

		case R.id.btndialog_setting:
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			Bundle mBundle = new Bundle();
			mBundle.putInt("curChoice", 1);
			intent.putExtras(mBundle);
			intent.setClassName("com.alpha.setting", "com.alpha.setting.AlphaSettingsActivity"); 
			context.startActivity(intent);
			this.dismiss();
			break;

		default:
			break;
		}

	}

}