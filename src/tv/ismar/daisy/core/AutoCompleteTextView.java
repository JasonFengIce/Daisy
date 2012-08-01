package tv.ismar.daisy.core;

import android.content.Context;
import android.widget.EditText;

public class AutoCompleteTextView extends EditText {
	Context context;
	public AutoCompleteTextView(Context context) {
		super(context);
		this.context = context;
	}



	// @Override
	// public boolean onKeyDown(int keyCode, KeyEvent event) {
	// return super.onKeyDown(keyCode, event);
	// }

}
