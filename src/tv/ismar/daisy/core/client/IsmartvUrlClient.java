package tv.ismar.daisy.core.client;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.squareup.okhttp.*;
import tv.ismar.daisy.BaseActivity;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.preferences.AccountSharedPrefs;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by huaijie on 5/28/15.
 */
public class IsmartvUrlClient extends Thread {
    private static final String TAG = "IsmartvClient:";
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static final int SUCCESS = 0x0001;
    private static final int FAILURE = 0x0002;

    private static final int FAILURE_4XX = 0x0004;
    private static final int FAILURE_5XX = 0x0005;

    private String url;
    private String params;
    private CallBack callback;
    private Method method;
    private MessageHandler messageHandler = new MessageHandler();

    private static Context mContext;

    public static void initializeWithContext(Context context) {
        mContext = context;
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
                    sendConnectErrorBroadcast(((Exception) msg.obj).getMessage());
                    break;
                case FAILURE_4XX:
                    callback.onFailed((Exception) msg.obj);
                    break;
                case FAILURE_5XX:
                    callback.onFailed((Exception) msg.obj);
                    sendConnectErrorBroadcast(((Exception) msg.obj).getMessage());
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

    public void doAdvertisementRequest(Method method, String api, HashMap<String, String> hashMap, CallBack callback) {

        hashMap.put("channel", " ");
        hashMap.put("section", " ");
        hashMap.put("itemid", " ");
        hashMap.put("topic", " ");
        hashMap.put("source", "power");
        hashMap.put("genre", " ");
        hashMap.put("content_model", " ");
        hashMap.put("director", " ");
        hashMap.put("actor", " ");
        hashMap.put("clipid", " ");
        hashMap.put("live_video", " ");
        hashMap.put("vendor", " ");
        hashMap.put("expense", " ");
        hashMap.put("length", " ");
        hashMap.put("modelName", Build.MODEL.replace(" ", "_"));
        hashMap.put("sn", SimpleRestClient.sn_token);
        hashMap.put("access_token", SimpleRestClient.access_token);
        hashMap.put("device_token", SimpleRestClient.device_token);
        hashMap.put("version", String.valueOf(SimpleRestClient.appVersion));
        hashMap.put("province", AccountSharedPrefs.getInstance(mContext).getSharedPrefs(AccountSharedPrefs.PROVINCE_PY));
        hashMap.put("city", "");
        hashMap.put("app", "sky");
        hashMap.put("resolution", SimpleRestClient.screenWidth + "," + SimpleRestClient.screenHeight);
        hashMap.put("dpi", String.valueOf(SimpleRestClient.densityDpi));


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


    public enum Method {
        GET,
        POST
    }

    private void doGet() {
        Message message = messageHandler.obtainMessage();
        try {
            String api = url + "?" + params;
            OkHttpClient client = new OkHttpClient();
            client.setConnectTimeout(10, TimeUnit.SECONDS);
            Request request = new Request.Builder()
                    .url(api)
                    .build();

            Response response;
            response = client.newCall(request).execute();
            String result = response.body().string();
            Log.i(TAG, "---> BEGIN\n" +
                            "\t<--- Request URL: " + "\t" + api + "\n" +
                            "\t<--- Request Method: " + "\t" + "GET" + "\n" +
                            "\t<--- Response Code: " + "\t" + response.code() + "\n" +
                            "\t<--- Response Result: " + "\t" + result + "\n" +
                            "\t---> END"
            );
            if (response.code() >= 400 && response.code() < 500) {
                message.what = FAILURE_4XX;
                message.obj = new IOException(response.message());
            } else if (response.code() >= 500) {
                message.what = FAILURE_5XX;
                message.obj = new IOException(response.message());

            } else {
                message.what = SUCCESS;
                message.obj = result;
            }
        } catch (Exception e) {
            message.what = FAILURE;
            message.obj = e;
        }
        messageHandler.sendMessage(message);
    }

    private void doPost() {
        Message message = messageHandler.obtainMessage();
        try {
            OkHttpClient client = new OkHttpClient();
            client.setConnectTimeout(10, TimeUnit.SECONDS);
            RequestBody body = RequestBody.create(JSON, params);

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .header("Accept-Encoding", "gzip")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .build();
            Response response;
            response = client.newCall(request).execute();
            String result = response.body().string();
            Log.i(TAG, "---> BEGIN\n" +
                            "\t<--- Request URL: " + "\t" + url + "\n" +
                            "\t<--- Request Method: " + "\t" + "POST" + "\n" +
                            "\t<--- Request Params: " + "\t" + params + "\n" +
                            "\t<--- Response Code: " + "\t" + response.code() + "\n" +
                            "\t<--- Response Result: " + "\t" + result + "\n" +
                            "\t---> END"
            );

            if (response.code() >= 400 && response.code() < 500) {
                message.what = FAILURE_4XX;
                message.obj = new IOException(response.message());
            } else if (response.code() >= 500) {
                message.what = FAILURE_5XX;
                message.obj = new IOException(response.message());

            } else {
                message.what = SUCCESS;
                message.obj = result;
            }
        } catch (Exception e) {
            message.what = FAILURE;
            message.obj = e;
        }
        messageHandler.sendMessage(message);
    }

    private void sendConnectErrorBroadcast(String msg) {
        if (mContext != null) {
            Intent intent = new Intent();
            intent.putExtra("data", msg);
            intent.setAction(BaseActivity.ACTION_CONNECT_ERROR);
            mContext.sendBroadcast(intent);
        }
    }
}
