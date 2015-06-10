package tv.ismar.daisy.core.client;

import android.net.UrlQuerySanitizer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.webkit.URLUtil;
import com.alibaba.fastjson.JSON;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by huaijie on 5/28/15.
 */
public class IsmartvUrlClient extends Thread {
    private static final String TAG = "IsmartvClient";

    private static final int SUCCESS = 0x0001;
    private static final int FAILURE = 0x0002;
    private String url;
    private CallBack callback;
    private MessageHandler messageHandler = new MessageHandler();

    public IsmartvUrlClient(String url, CallBack callback) {
        this.callback = callback;
        this.url = url;
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
            String result = response.body().string();
            Log.i(TAG, "--->\n" +
                    "\turl is: " + "\t" + url + "\n" +
                    "\tresult is: " + "\t" + result + "\n");
            message.what = SUCCESS;
            message.obj = result;
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
                    callback.onSuccess((String) msg.obj);
                    break;
                case FAILURE:
                    callback.onFailed((Exception) msg.obj);
                    break;
                default:
                    break;
            }
        }
    }
}
