package tv.ismar.daisy.core.client;

import android.text.TextUtils;
import android.util.Log;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by huaijie on 8/3/15.
 */
public class JavaHttpClient {
    private static final String TAG = "JavaHttpClient";

    private String mApi;
    private String mParams;
    private Callback mCallback;


    public interface Callback {
        void onSuccess(HttpResponseMessage result);

        void onFailed(HttpResponseMessage error);
    }

    public void doRequest(HttpMethod method, String api, HashMap<String, String> params, Callback callback) {
        Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();
        StringBuffer stringBuffer = new StringBuffer();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            String key = entry.getKey();
            String value = entry.getValue();
            stringBuffer.append(key).append("=").append(value).append("&");
        }
        stringBuffer.deleteCharAt(stringBuffer.length() - 1);

        this.mApi = api;
        this.mParams = stringBuffer.toString();
        this.mCallback = callback;

        switch (method) {
            case GET:
                doGet();
                break;
            case POST:
                break;
        }
    }

    public void doRequest(String api, Callback callback) {
        this.mApi = api;
        this.mCallback = callback;

        doGet();
    }


    private void doGet() {
        String api;
        try {
            if (TextUtils.isEmpty(mParams)) {
                api = mApi;
            } else {
                api = mApi + "?" + mParams;
            }

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
            handleResponse(response.code(), result);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void handleResponse(int responseCode, String responseResult) {
        HttpResponseMessage message = new HttpResponseMessage();

        if (responseCode >= 400 && responseCode < 500) {
            message.responseCode = responseCode;
            message.responseResult = responseResult;
            mCallback.onFailed(message);
        } else if (responseCode >= 500) {
            message.responseCode = responseCode;
            message.responseResult = responseResult;
            mCallback.onFailed(message);
        } else {
            message.responseCode = responseCode;
            message.responseResult = responseResult;
            mCallback.onSuccess(message);
        }
    }
}
