package tv.ismar.daisy.core.client;

import com.google.gson.Gson;

import java.io.IOException;
import java.text.ParseException;

import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.utils.Util;

/**
 * Created by huibin on 7/4/16.
 */
public class PlayCheckManager {

    private static PlayCheckManager mInstance;

    public static PlayCheckManager getInstance() {
        if (mInstance == null) {
            mInstance = new PlayCheckManager();
        }
        return mInstance;
    }

    public void check(String item, final Callback callback) {
        String deviceToken = SimpleRestClient.device_token;
        String accessToken = SimpleRestClient.access_token;
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(HttpManager.appendProtocol(SimpleRestClient.root_url))
                .client(HttpManager.getInstance().mClient)
                .build();
        NewVipHttpApi.PlayCheck playCheck = retrofit.create(NewVipHttpApi.PlayCheck.class);
        playCheck.doRequest(item, null, null, deviceToken, accessToken).enqueue(new retrofit2.Callback<ResponseBody>() {
            @Override
            public void onResponse(Response<ResponseBody> response) {
                if (response.errorBody() != null) {
                    callback.onFailure();
                } else {
                    try {
                        handlePlaycheck(response.body().string(), callback);
                    } catch (IOException e) {
                        callback.onFailure();
                    }

                }
            }

            @Override
            public void onFailure(Throwable t) {
                callback.onFailure();
            }
        });
    }

    private class PlayCheckEntity {
        private String expiry_date;
        private String iqiyi_code;

        public String getExpiry_date() {
            return expiry_date;
        }

        public void setExpiry_date(String expiry_date) {
            this.expiry_date = expiry_date;
        }

        public String getIqiyi_code() {
            return iqiyi_code;
        }

        public void setIqiyi_code(String iqiyi_code) {
            this.iqiyi_code = iqiyi_code;
        }
    }

    private void handlePlaycheck(String info, Callback callback) {
        boolean isBuy = false;
        switch (info) {
            case "0":
                break;
            default:
                PlayCheckEntity playCheckEntity = new Gson().fromJson(info, PlayCheckEntity.class);
                int remainDay = 0;
                try {
                    remainDay = Util.daysBetween(Util.getTime(), playCheckEntity.getExpiry_date()) + 1;
                } catch (ParseException e) {
                    callback.onFailure();
                }
                if (remainDay == 0) {
                    isBuy = false;// 过期了。认为没购买
                } else
                    isBuy = true;// 购买了，剩余天数大于0
                break;
        }

        callback.onSuccess(isBuy);
    }

    public interface Callback {
        void onSuccess(boolean isBuy);

        void onFailure();
    }
}
