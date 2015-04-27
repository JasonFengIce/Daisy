package tv.ismar.daisy;

import tv.ismar.daisy.player.CallaPlay;
import tv.ismar.daisy.player.ISTVVodMenu;
import tv.ismar.daisy.views.CustomDialog;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public abstract class VodMenuAction extends Activity {
	static final int MSG_SEK_ACTION = 103;
	static final int BUFFER_COUNTDOWN_ACTION = 113;
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

	protected void showBuffer() {
	}

	protected void hideBuffer() {
	}
}
