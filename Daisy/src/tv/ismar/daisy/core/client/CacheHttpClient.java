package tv.ismar.daisy.core.client;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.preferences.HttpCacheSharedPrefs;
import tv.ismar.daisy.utils.HardwareUtils;

/**
 * Created by huaijie on 11/23/15.
 */
public class CacheHttpClient extends Thread {
    private static final String TAG = "CacheHttpClient";
    public static final int SUCCESS = 0x0000;
    public static final int FAILED = 0x0001;


    private String mUrlPath;
    private Callback mCallback;
    private MessageHandler messageHandler;

    private static class MessageHandler extends Handler {
        private final WeakReference<CacheHttpClient> cacheHttpClientWeakReference;

        public MessageHandler(CacheHttpClient cacheHttpClient) {
            cacheHttpClientWeakReference = new WeakReference<>(cacheHttpClient);
        }

        @Override
        public void handleMessage(Message msg) {
            CacheHttpClient cacheHttpClient = cacheHttpClientWeakReference.get();
            if (cacheHttpClient != null) {
                switch (msg.what) {
                    case SUCCESS:
                        cacheHttpClient.mCallback.onSuccess(msg.obj.toString());
                        break;
                    case FAILED:
					if (msg.obj == null)
						msg.obj = "unknow";
                        cacheHttpClient.mCallback.onFailed(msg.obj.toString());
                        break;
                }
            }
        }
    }


    public interface Callback {
        void onSuccess(String result);

        void onFailed(String error);
    }

    public void doRequest(String url, Callback callback) {
        messageHandler = new MessageHandler(this);
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("device_token", SimpleRestClient.device_token);
        hashMap.put("access_token", SimpleRestClient.access_token);
        Iterator<Map.Entry<String, String>> iterator = hashMap.entrySet().iterator();
        StringBuffer stringBuffer = new StringBuffer();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            String key = entry.getKey();
            String value = entry.getValue();
            stringBuffer.append(key).append("=").append(value).append("&");
        }
        stringBuffer.deleteCharAt(stringBuffer.length() - 1);

        String api = url + "?" + stringBuffer.toString();
        mUrlPath = api;
        mCallback = callback;

        start();

    }


    @Override
    public void run() {

        String md5Url = HardwareUtils.getMd5ByString(mUrlPath);
        //从缓存中取数据
        String cacheResult = HttpCacheSharedPrefs.getSharedPrefs(md5Url);
        if (!TextUtils.isEmpty(cacheResult)) {
            Message message = new Message();
            message.what = SUCCESS;
            message.obj = cacheResult;
            messageHandler.sendMessage(message);
        }

        try {
            OkHttpClient client = new OkHttpClient();
            client.setConnectTimeout(10, TimeUnit.SECONDS);
            Request request = new Request.Builder()
                    .url(mUrlPath)
                    .build();

            Response response;
            response = client.newCall(request).execute();
            String result = response.body().string();
            Log.i(TAG, "---> BEGIN\n" +
                    "\t<--- Request URL: " + "\t" + mUrlPath + "\n" +
                    "\t<--- Request Method: " + "\t" + "GET" + "\n" +
                    "\t<--- Response Code: " + "\t" + response.code() + "\n" +
                    "\t<--- Response Result: " + "\t" + result + "\n" +
                    "\t---> END"
            );

            if (!TextUtils.isEmpty(result)) {
                HttpCacheSharedPrefs.setSharedPrefs(md5Url, result);
                Message message = new Message();
                message.what = SUCCESS;
                message.obj = result;
                messageHandler.sendMessage(message);
            }
        } catch (IOException e) {
            Message message = new Message();
            message.what = FAILED;
            message.obj = e.getMessage();
            messageHandler.sendMessage(message);
        }

    }
}
