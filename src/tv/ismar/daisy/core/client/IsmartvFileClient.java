package tv.ismar.daisy.core.client;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import tv.ismar.sakura.core.Timer;
import tv.ismar.sakura.utils.DeviceUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by huaijie on 5/28/15.
 */
public class IsmartvFileClient extends Thread {
    private static final String TAG = "IsmartvClient";


    private static final int SUCCESS = 0x0001;
    private static final int FAILURE = 0x0002;
    private static final int DEFAULT_TIMEOUT = 10000;
    private Context context;
    private String url;
    private CallBack callback;
    private MessageHandler messageHandler = new MessageHandler();

    public IsmartvFileClient(Context context, String url, CallBack callback) {
        this.callback = callback;
        this.url = url;
        this.context = context;
    }


    @Override
    public void run() {
        Message message = messageHandler.obtainMessage();
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        com.squareup.okhttp.Response response = null;
        try {
            response = client.newCall(request).execute();
            InputStream inputStream = response.body().byteStream();
            Log.d(TAG, "url is: " + url);
            File defaultCacheFile = new File(getCachePath(), "hello.mp4");

//            FileOutputStream outputStream = context.openFileOutput("hello.mp4", Context.MODE_WORLD_WRITEABLE);


            FileOutputStream fileOutputStream = new FileOutputStream(defaultCacheFile);

            byte[] buffer = new byte[1024];
            int byteRead = 0;
            while ((byteRead = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, byteRead);
            }
            fileOutputStream.flush();
            fileOutputStream.close();
            inputStream.close();
            message.what = SUCCESS;
        } catch (IOException e) {
            message.what = FAILURE;
            message.obj = e;
        }
        messageHandler.sendMessage(message);
    }


    public interface CallBack {
        void onSuccess(String result);

        void onFailed(Exception exception);
    }


    class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SUCCESS:
                    callback.onSuccess("success");
                    break;
                case FAILURE:
                    callback.onFailed((Exception) msg.obj);
                    break;
                default:
                    break;
            }
        }
    }

    private File getCachePath() {
//        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Log.i(TAG, "path is: " + Environment.getExternalStorageDirectory().getAbsolutePath());
            return Environment.getExternalStorageDirectory();
//        } else {
//            return null;
//        }
    }
}
