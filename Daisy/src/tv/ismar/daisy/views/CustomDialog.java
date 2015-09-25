package tv.ismar.daisy.views;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import tv.ismar.daisy.R;

public class CustomDialog extends Dialog {

	public CustomDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public CustomDialog(Context context, int theme) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public static class Builder {
		private Context mContext;
		private String mMessage;
		private String mPositiveButtonText;
		private String mNegativeButtonText;
		private DialogInterface.OnClickListener mPositiveListener;
		private DialogInterface.OnClickListener mNegativeListener;
		
		public Builder(Context context){
			mContext = context;
		}
		
		public Builder setMessage(int resId) {
			setMessage(mContext.getResources().getString(resId));
			return this;
		}
		
		public Builder setMessage(String message){
			mMessage = message;
			return this;
		}
		
		public Builder setPositiveButton(int textResId, DialogInterface.OnClickListener listener){
			setPositiveButton(mContext.getResources().getString(textResId), listener);
			return this;
		}
		
		public Builder setPositiveButton(String text, DialogInterface.OnClickListener listener){
			mPositiveButtonText = text;
			mPositiveListener = listener;
			return this;
		}
		
		public Builder setNegativeButton(int textResId, DialogInterface.OnClickListener listener){
			setNegativeButton(mContext.getResources().getString(textResId), listener);
			return this;
		}
		
		public Builder setNegativeButton(String text, DialogInterface.OnClickListener listener){
			mNegativeButtonText = text;
			mNegativeListener = listener;
			return this;
		}
		
		public CustomDialog create(){

			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final CustomDialog dialog = new CustomDialog(mContext, R.style.Dialog);

			View layout = inflater.inflate(R.layout.popup_neterror_message, null);
			dialog.addContentView(layout, new RelativeLayout.LayoutParams(877,305));
			if(mPositiveButtonText!=null){
				((Button)layout.findViewById(R.id.positive_button)).setText(mPositiveButtonText);
			}
			((Button)layout.findViewById(R.id.positive_button)).setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					mPositiveListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
				}
			});
			if(mNegativeButtonText!=null){
				((Button)layout.findViewById(R.id.negative_btn)).setText(mNegativeButtonText);
			}
			if(mNegativeListener!=null){
				((Button)layout.findViewById(R.id.negative_btn)).setOnClickListener(new View.OnClickListener() {
					
					public void onClick(View v) {
						mNegativeListener.onClick(dialog, BUTTON_NEGATIVE);
					}
				});
			}
			if(mMessage!=null){
				((TextView)layout.findViewById(R.id.alert_info_text)).setText(mMessage);
			}
		//	dialog.setContentView(layout);
			return dialog;
		}
	}
}
