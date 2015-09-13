package tv.ismar.daisy.core.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import tv.ismar.daisy.core.preferences.AccountSharedPrefs;

/**
 * Created by huaijie on 8/11/15.
 */
public class LocationProvider extends ContentProvider {
    private static final String AUTOR = "cn.ismartv.daisy.provider.location";


    @Override
    public boolean onCreate() {
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return AccountSharedPrefs.getInstance(getContext()).getSharedPrefs(AccountSharedPrefs.GEO_ID);
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
