package tv.ismar.daisy.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

/**
 * Created by huaijie on 12/2/15.
 */
public class BitmapDecoder extends Thread {
    private static final int DECODE_SUCCESS = 0x0000;
    private Callback mCallback;
    private Context mContext;
    private int mResId;

    public void decode(Context context, int resId, Callback callback) {
        mCallback = callback;
        mContext = context;
        mResId = resId;
        start();
    }

    @Override
    public void run() {
//        BitmapFactory.Options opt = new BitmapFactory.Options();
//        opt.inPreferredConfig = Bitmap.Config.ALPHA_8;
//        opt.inPurgeable = true;
//        opt.inInputShareable = true;
//        InputStream is = mContext.getResources().openRawResource(mResId);
//        Bitmap bm = BitmapFactory.decodeStream(is, null, opt);
        Bitmap bitmap = null;
        try {
            bitmap = Picasso.with(mContext).load(mResId).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        BitmapDrawable bd = new BitmapDrawable(mContext.getResources(), bitmap);
        MessageHandler messageHandler = new MessageHandler(this);
        Message message = new Message();
        message.obj = bd;
        message.what = DECODE_SUCCESS;
        messageHandler.sendMessage(message);
    }

    private static class MessageHandler extends Handler {
        WeakReference<BitmapDecoder> weakReference;

        private MessageHandler(BitmapDecoder bitmapDecoder) {
            super(Looper.getMainLooper());
            weakReference = new WeakReference<BitmapDecoder>(bitmapDecoder);
        }

        @Override
        public void handleMessage(Message msg) {
            BitmapDecoder decoder = weakReference.get();
            if (decoder != null) {
                switch (msg.what) {
                    case DECODE_SUCCESS:
                        BitmapDrawable drawable = (BitmapDrawable) msg.obj;
                        decoder.mCallback.onSuccess(drawable);
                        break;
                }
            }
        }
    }

    public interface Callback {
        void onSuccess(BitmapDrawable bitmapDrawable);
    }
}
