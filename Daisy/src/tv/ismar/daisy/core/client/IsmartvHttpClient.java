package tv.ismar.daisy.core.client;

import android.net.Uri;
import android.os.Handler;
import android.os.Message;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import tv.ismar.daisy.VodApplication;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.jni.HttpClient;

/**
 * Created by huaijie on 9/25/15.
 */
public class IsmartvHttpClient extends Thread {
    private static final int SUCCESS = 0x0001;
    private static final int FAILURE = 0x0002;

    private static final int FAILURE_4XX = 0x0004;
    private static final int FAILURE_5XX = 0x0005;

    private String mUrl;
    private String mParams;
    private CallBack mCallback;
    private Method method;


    public interface CallBack {
        void onSuccess(String result);

        void onFailed(Exception exception);
    }


    @Override
    public void run() {
        String result = "";
        Uri uri = Uri.parse(mUrl);

        switch (method) {
            case GET:
                result = new HttpClient().doGet(uri.getHost(), uri.getPath(), mParams);
                break;
            case POST:
                result = new HttpClient().doPost(uri.getHost(), uri.getPath(), mParams);
                break;
        }
        Message message = messageHandler.obtainMessage(SUCCESS, result);
        messageHandler.sendMessage(message);
    }

    public void doRequest(Method method, String api, HashMap<String, String> hashMap, CallBack callback) {
        hashMap.put("access_token", SimpleRestClient.access_token);
        if (SimpleRestClient.device_token == null || "".equals(SimpleRestClient.device_token)) {
            VodApplication.setDevice_Token();
        }
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
        mParams = stringBuffer.toString();
        mUrl = api;
        mCallback = callback;
        this.method = method;
        start();
    }


    private Handler messageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SUCCESS:
                    mCallback.onSuccess((String) msg.obj);
                    break;
                case FAILURE:
                    break;
                default:
                    break;
            }
        }
    };


    public enum Method {
        GET,
        POST
    }


}
