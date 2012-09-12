package tv.ismar.daisy.views;

import tv.ismar.daisy.R;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class AlertDialogFragment extends DialogFragment {
	
	public static final int NETWORK_EXCEPTION_DIALOG = 1;
	
	private DialogInterface.OnClickListener mPositiveListener;
	private DialogInterface.OnClickListener mNegativeListener;
	
	public void setPositiveListener(DialogInterface.OnClickListener listener) {
		mPositiveListener = listener;
	}
	
	public void setNegativeListener(DialogInterface.OnClickListener listener) {
		mNegativeListener = listener;
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setStyle(R.style.MyDialog, android.R.style.Theme_Dialog);
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		int dialogType = getArguments().getInt("dialogType");
		Dialog dialog = null;
		switch(dialogType) {
		case NETWORK_EXCEPTION_DIALOG:
			new CustomDialog.Builder(getActivity())
			.setMessage(R.string.vod_get_data_error)
			.setPositiveButton(R.string.vod_retry, mPositiveListener)
			.setNegativeButton(R.string.vod_ok, mNegativeListener).create();
			break;
		}
		
		return dialog;
	}
	
	public static AlertDialogFragment newInstance(int dialogType) {
		AlertDialogFragment f = new AlertDialogFragment();
		Bundle args = new Bundle();
		args.putInt("dialogType", dialogType);
		f.setArguments(args);
		return f;
	}
	
}
