package tv.ismar.daisy.core.client;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.squareup.okhttp.*;
import tv.ismar.daisy.core.SimpleRestClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by huaijie on 5/28/15.
 */
public class IsmartvUrlClient extends Thread {
    private static final String TAG = "IsmartvClient:";
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static final int SUCCESS = 0x0001;
    private static final int FAILURE = 0x0002;

    private String url;
    private String params;
    private CallBack callback;
    private Method method;
    private MessageHandler messageHandler = new MessageHandler();

    public IsmartvUrlClient() {
    }

    @Override
    public void run() {
        switch (method) {
            case GET:
                doGet();
                break;
            case POST:
                doPost();
                break;
        }
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


    public void doRequest(Method method, String api, HashMap<String, String> hashMap, CallBack callback) {
        hashMap.put("access_token", SimpleRestClient.access_token);
        hashMap.put("device_token", SimpleRestClient.device_token);
        Iterator<Map.Entry<String, String>> iterator = hashMap.entrySet().iterator();
        StringBuffer stringBuffer = new StringBuffer();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            String key = entry.getKey();
            String value = entry.getValue();
            stringBuffer.append(key).append("=").append(value).append("&");
        }
        stringBuffer.deleteCharAt(stringBuffer.length() - 1);
        this.params = stringBuffer.toString();
        this.url = api;
        this.callback = callback;
        this.method = method;
        start();
    }


    public void doRequest(String api, CallBack callback) {
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
        this.params = stringBuffer.toString();
        this.url = api;
        this.callback = callback;
        this.method = Method.GET;
        start();
    }

    public enum Method {
        GET,
        POST
    }

    private void doGet() {
        String api = url + "?" + params;
        Message message = messageHandler.obtainMessage();
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(api)
                .build();
        Response response;
        try {
            response = client.newCall(request).execute();
            String result = response.body().string();
            Log.i(TAG, "--->\n" +
                    "\turl is: " + "\t" + api + "\n" +
                    "\tresult is: " + "\t" + result + "\n");
            message.what = SUCCESS;
            message.obj = result;
        } catch (IOException e) {
            message.what = FAILURE;
            message.obj = e;
        }
        messageHandler.sendMessage(message);
    }

    private void doPost() {
        Message message = messageHandler.obtainMessage();
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(JSON, params);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response;
        try {
            response = client.newCall(request).execute();
            String result = response.body().string();
            Log.i(TAG, "--->\n" +
                    "\turl is: " + "\t" + url + "\n" +
                    "\tparams is: " + "\t" + params + "\n" +
                    "\tresult is: " + "\t" + result + "\n");
            message.what = SUCCESS;
            message.obj = result;
        } catch (IOException e) {
            message.what = FAILURE;
            message.obj = e;
        }
        messageHandler.sendMessage(message);
    }
}
