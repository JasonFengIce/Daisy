package tv.ismar.daisy.core.observer;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

/**
 * Created by huaijie on 9/7/15.
 */
public class DownloadDatabaseChangeObserver extends ContentObserver {
    private static final String TAG = "DatabaseChangeObserver";

    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    public DownloadDatabaseChangeObserver(Handler handler) {
        super(handler);
    }


    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);




    }
}
