package tv.ismar.daisy;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.*;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.NetworkUtils;
import tv.ismar.daisy.core.preferences.AccountSharedPrefs;
import tv.ismar.daisy.models.AdElement;
import tv.ismar.daisy.player.CallaPlay;
import tv.ismar.daisy.player.ISTVVodMenu;
import tv.ismar.daisy.views.AsyncImageView;
import tv.ismar.daisy.views.CustomDialog;

import java.io.InputStream;
import java.util.ArrayList;

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
    protected GetAdDataTask adAsyncTask;
    protected String section;
    protected String channel;
    protected String slug;
    protected String fromPage;
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
			String province = AccountSharedPrefs.getInstance().getSharedPrefs(AccountSharedPrefs.PROVINCE_PY);
			ArrayList<AdElement> ads = NetworkUtils.getAdByPost(adpid, p,province);
			return ads;
		}

	}

	class AdImageDialog extends Dialog {
		private int width;
		private int height;
		private String url;
        private int media_id;
        private long duration;
		private AsyncImageView zanting_image;
        private String title;
		private ImageView close_btn;


		public AdImageDialog(Context context, int theme, String imageurl,String title,int id ) {
			super(context, theme);
			WindowManager wm = (WindowManager) getContext().getSystemService(
					Context.WINDOW_SERVICE);
			width = wm.getDefaultDisplay().getWidth();
			height = wm.getDefaultDisplay().getHeight();
            this.title = title;
            this.media_id = id;
			url = imageurl;
		}

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			this.setContentView(R.layout.zantingguanggao);
			zanting_image = (AsyncImageView) this.findViewById(R.id.zantingguanggao);
			close_btn = (ImageView) this.findViewById(R.id.zanting_close);
			zanting_image.setOnImageViewLoadListener(new AsyncImageView.OnImageViewLoadListener() {
				
				@Override
				public void onLoadingStarted(AsyncImageView imageView) {
					callaPlay.pause_ad_download(title, media_id, url, "bestv");
				}
				
				@Override
				public void onLoadingFailed(AsyncImageView imageView, Throwable throwable) {
//					callaPlay.pause_ad_except(throwable., errorContent);					
				}
				
				@Override
				public void onLoadingEnded(AsyncImageView imageView, Bitmap image) {
					close_btn.setVisibility(View.VISIBLE);
					close_btn.requestFocus();
				}
			});
			zanting_image.setUrl(url);
			if(DaisyUtils.getImageCache(getContext()).get(url) != null){
				close_btn.setVisibility(View.VISIBLE);
				close_btn.requestFocus();
			}
            duration = System.currentTimeMillis();
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
			case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
				dismiss();
				return true;
			}
			return super.onKeyDown(keyCode, event);
		}

		@Override
		public void dismiss() {
			super.dismiss();
            duration = System.currentTimeMillis()-duration;
            callaPlay.pause_ad_play(title,media_id,url,duration,"bestv");
//			resumeItem();
		}

		@Override
		public void onBackPressed() {
			super.onBackPressed();
		}

	};

	 protected void setGesturebackground(View view,int id) {

	        BitmapFactory.Options opt = new BitmapFactory.Options();

	        opt.inPreferredConfig = Bitmap.Config.ALPHA_8;

	        opt.inPurgeable = true;

	        opt.inInputShareable = true;
	        opt.inTargetDensity = getResources().getDisplayMetrics().densityDpi;
	        opt.inDensity = getResources().getDisplayMetrics().densityDpi;

	        InputStream is = getResources().openRawResource(
	                id);

	        Bitmap bm = BitmapFactory.decodeStream(is, null, opt);

	        BitmapDrawable bd = new BitmapDrawable(getResources(), bm);
	        view.setBackgroundDrawable(bd);
	    }
}
