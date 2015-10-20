package tv.ismar.daisy.views;

import tv.ismar.daisy.R;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class ExitDialog extends Dialog {

	private Activity mycontext;
	private int width;
	private int height;
	private TextView exitprompt;
	private Button btn1 = null, btn2 = null;
	
	public ExitDialog(Context context) {
		super(context);
	}

	public ExitDialog(Activity context, int theme) {
		super(context, theme);
		WindowManager wm = (WindowManager) getContext().getSystemService(
				Context.WINDOW_SERVICE);
		width = wm.getDefaultDisplay().getWidth();
		height = wm.getDefaultDisplay().getHeight();
		mycontext = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.popup_2btn);
		initView();
		resizeWindow();
	}

	private void initView() {
		btn1 = (Button) findViewById(R.id.confirm_exit);
		btn1.setText(R.string.vod_ok);
		btn2 = (Button) findViewById(R.id.cancel_exit);
		btn2.setText(R.string.vod_cancel);
		exitprompt = (TextView)findViewById(R.id.exit_prompt_text);
		exitprompt.setText(R.string.exit_prompt);
		btn1.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				dismiss();
				mycontext.finish();
			}
		});
		btn2.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				dismiss();
			}
		});
		
	}

	private void resizeWindow() {
		Window dialogWindow = getWindow();
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		lp.width = ((int) (width * 0.457));
		lp.height = ((int) (height * 0.28));
	}

}
