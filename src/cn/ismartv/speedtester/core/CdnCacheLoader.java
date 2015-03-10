package cn.ismartv.speedtester.core;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.CursorLoader;

/**
 * Created by huaijie on 3/10/15.
 */
public class CdnCacheLoader extends CursorLoader {
    private static final String TAG = "CdnCacheLoader";

    public CdnCacheLoader(Context context) {
        super(context);
    }

    public CdnCacheLoader(Context context, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        super(context, uri, projection, selection, selectionArgs, sortOrder);
    }

    @Override
    public Cursor loadInBackground() {
        return super.loadInBackground();
    }
}
