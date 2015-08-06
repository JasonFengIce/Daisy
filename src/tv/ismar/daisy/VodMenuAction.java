package tv.ismar.daisy;

import java.util.ArrayList;

import tv.ismar.daisy.core.NetworkUtils;
import tv.ismar.daisy.core.preferences.AccountSharedPrefs;
import tv.ismar.daisy.models.AdElement;
import tv.ismar.daisy.player.CallaPlay;
import tv.ismar.daisy.player.ISTVVodMenu;
import tv.ismar.daisy.views.AsyncImageView;
import tv.ismar.daisy.views.CustomDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public abstract class VodMenuAction extends BaseActivity {
	static final int MSG_SEK_ACTION = 103;
	static final int BUFFER_COUNTDOWN_ACTION = 113;
	static final int DISMISS_AD_DIALOG = 114;
	static final int AD_COUNT_ACTION = 115;
	protected int buffercountDown = 0;
	protected static final String BUFFERING = " 正在加载 ";
	Dialog dialog = null;
	private DialogInterface.OnClickListener mPositiveListener;
	private DialogInterface.OnClickListener mNegativeListener;
	protected boolean isBuffer = true;
	protected LinearLayout bufferLayout;
	protected long bufferDuration = 0;
	protected TextView bufferText;
	protected CallaPlay callaPlay = new CallaPlay();

	public abstract boolean onVodMenuClicked(ISTVVodMenu menu, int id);

	public abstract void onVodMenuClosed(ISTVVodMenu menu);

	protected void showDialog(String str) {
		if (dialog == null) {
			mPositiveListener = new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					// TODO Auto-generated method stub
					dialog.dismiss();
				}
			};

			mNegativeListener = new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					// TODO Auto-generated method stub
					dialog.dismiss();
					VodMenuAction.this.finish();
				}
			};
			dialog = new CustomDialog.Builder(this).setMessage(str)
					.setPositiveButton(R.string.vod_cancel, mPositiveListener)
					.setNegativeButton(R.string.vod_ok, mNegativeListener)
					.create();
		}
		dialog.show();
	}

	protected void resumeItem() {
	}

	protected void showBuffer() {
	}

	protected void hideBuffer() {
	}

	protected void playMainVideo() {
	}

	protected void showAd(ArrayList<AdElement> result,String adpid) {
	}

	class GetAdDataTask extends AsyncTask<String, Void, ArrayList<AdElement>> {

		private String adpid;
		@Override
		protected void onPostExecute(ArrayList<AdElement> result) {
			showAd(result,adpid);
		}

		@Override
		protected ArrayList<AdElement> doInBackground(String... params) {
		    adpid = params[0];
			String p = params[1];
			String province = AccountSharedPrefs.getInstance(VodMenuAction.this).getSharedPrefs(AccountSharedPrefs.PROVINCE_PY);
			ArrayList<AdElement> ads = NetworkUtils.getAdByPost(adpid, p,province);
			return ads;
		}

	}

	class AdImageDialog extends Dialog {
		private int width;
		private int height;
		private String url;
		private AsyncImageView zanting_image;
		private ImageView close_btn;

		public AdImageDialog(Context context, int theme, String imageurl) {
			super(context, theme);
			WindowManager wm = (WindowManager) getContext().getSystemService(
					Context.WINDOW_SERVICE);
			width = wm.getDefaultDisplay().getWidth();
			height = wm.getDefaultDisplay().getHeight();
			url = imageurl;
		}

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			this.setContentView(R.layout.zantingguanggao);
			zanting_image = (AsyncImageView) this.findViewById(R.id.zantingguanggao);
			close_btn = (ImageView) this.findViewById(R.id.zanting_close);
			close_btn.setVisibility(View.VISIBLE);
			zanting_image.setUrl(url);
			resizeWindow();
		}

		private void resizeWindow() {
			Window dialogWindow = getWindow();
			WindowManager.LayoutParams lp = dialogWindow.getAttributes();
			lp.width = ((int) (width * 0.53));
			lp.height = ((int) (height * 0.53));
			lp.gravity = Gravity.CENTER;
			close_btn.requestFocus();
			close_btn.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					dismiss();
				}
			});
		}

		@Override
		public boolean onKeyDown(int keyCode, KeyEvent event) {
			switch (keyCode) {
			case KeyEvent.KEYCODE_BACK:
				dismiss();
				return true;
			}
			return super.onKeyDown(keyCode, event);
		}

		@Override
		public void dismiss() {
			super.dismiss();
//			resumeItem();
		}

		@Override
		public void onBackPressed() {
			super.onBackPressed();
		}

	};
}
