package tv.ismar.daisy.core.initialization;

import tv.ismar.daisy.core.client.HttpResponseMessage;
import tv.ismar.daisy.core.client.JavaHttpClient;

/**
 * Created by huaijie on 8/14/15.
 */
public class GuideVideoInitializeProcess {
    private static final String TAG = "GuideVideoInitializeProcess";

    private String mApiHost;

    private static GuideVideoInitializeProcess instance;

    private GuideVideoInitializeProcess(String apiHost) {
        mApiHost = apiHost;
    }


    public static GuideVideoInitializeProcess getInstance(String apiHost) {
        if (null == instance) {
            instance = new GuideVideoInitializeProcess(apiHost);
        }
        return instance;
    }




    private void fetchHomeVideo() {
        String api = mApiHost + "/api/tv/homepage/top/";
        new JavaHttpClient().doRequest(api, new JavaHttpClient.Callback() {
            @Override
            public void onSuccess(HttpResponseMessage result) {

            }

            @Override
            public void onFailed(HttpResponseMessage error) {

            }
        });
    }


}
