package tv.ismar.daisy.core.cache.image_loader;

import android.content.Context;

/**
 * Created by huaijie on 11/25/15.
 */
public class RequestManager {

    private Context context;

    public RequestManager(Context context) {
        this.context = context;
    }

    public RequestBuilder load(Object model) {
        return new RequestBuilder().load(context, model.toString());
    }
}
