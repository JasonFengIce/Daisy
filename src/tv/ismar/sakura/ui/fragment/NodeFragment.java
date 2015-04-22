package tv.ismar.sakura.ui.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.activeandroid.ActiveAndroid;
import com.activeandroid.content.ContentProvider;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import tv.ismar.daisy.R;
import tv.ismar.sakura.core.CdnCacheLoader;
import tv.ismar.sakura.core.SakuraClientAPI;
import tv.ismar.sakura.data.http.BindedCdnEntity;
import tv.ismar.sakura.data.http.CdnListEntity;
import tv.ismar.sakura.data.table.CdnCacheTable;
import tv.ismar.sakura.ui.adapter.NodeListAdapter;
import tv.ismar.sakura.ui.widget.SakuraListView;

import static tv.ismar.sakura.core.SakuraClientAPI.restAdapter_WX_API_TVXIO;

/**
 * Created by huaijie on 2015/4/8.
 */
public class NodeFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "NodeFragment";

    private SakuraListView nodeListView;
    private NodeListAdapter nodeListAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fetchCdnList();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sakura_fragment_node, null);
        nodeListView = (SakuraListView) view.findViewById(R.id.node_list);
        nodeListAdapter = new NodeListAdapter(getActivity(), null, true);
        nodeListView.setAdapter(nodeListAdapter);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getLoaderManager().initLoader(0, null, this);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    /**
     * @param flag
     * @param bundle
     * @return
     */
    @Override
    public Loader<Cursor> onCreateLoader(int flag, Bundle bundle) {
        String selection1 = "area" + "=? and " + "isp" + "=?" + " or flag  <> ?" + " ORDER BY isp,speed DESC limit 5";
        String selection2 = "area" + "=? and " + "isp" + " in (?, ?)" + " or flag  <> ?" + " ORDER BY isp,speed DESC limit 5";
        CdnCacheLoader cacheLoader = new CdnCacheLoader(getActivity(), ContentProvider.createUri(CdnCacheTable.class, null),
                null,
                null, null, null);
//        switch (flag) {
//            case 0:
//                cacheLoader.setSelection(selection1);
//                cacheLoader.setSelectionArgs(selectionArgs);
//                break;
//            case 1:
//                cacheLoader.setSelection(selection2);
//                cacheLoader.setSelectionArgs(selectionArgs);
//                break;
//            default:
//                break;
//        }
        return cacheLoader;
    }

    /**
     * @param loader
     * @param cursor
     */

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.d(TAG, "cursor count: " + cursor.getCount());
        nodeListAdapter.swapCursor(cursor);
    }

    /**
     * @param loader
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        nodeListAdapter.swapCursor(null);
    }

    /**
     * fetchCdnList
     */
    private void fetchCdnList() {
        SakuraClientAPI.CdnList client = restAdapter_WX_API_TVXIO.create(SakuraClientAPI.CdnList.class);
        client.execute(new Callback<CdnListEntity>() {
            @Override
            public void success(CdnListEntity cdnListEntity, Response response) {
                ActiveAndroid.beginTransaction();
                try {
                    for (CdnListEntity.CdnEntity cdnEntity : cdnListEntity.getCdn_list()) {
                        CdnCacheTable cdnCacheTable = new CdnCacheTable();
                        cdnCacheTable.cdn_id = cdnEntity.getCdnID();
                        cdnCacheTable.cdn_name = cdnEntity.getName();
                        cdnCacheTable.cdn_nick = cdnEntity.getNick();
                        cdnCacheTable.cdn_flag = cdnEntity.getFlag();
                        cdnCacheTable.area = cdnEntity.getArea();
                        cdnCacheTable.isp = cdnEntity.getIsp();
                        cdnCacheTable.route_trace = cdnEntity.getRoute_trace();
                        cdnCacheTable.save();
                    }
                    ActiveAndroid.setTransactionSuccessful();
                } finally {
                    ActiveAndroid.endTransaction();
                }

            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.e(TAG, retrofitError.getMessage());
            }
        });
    }


    private void fetchBindedCdn(final String snCode) {
        SakuraClientAPI.GetBindCdn client = restAdapter_WX_API_TVXIO.create(SakuraClientAPI.GetBindCdn.class);
        client.excute(snCode, new Callback<BindedCdnEntity>() {
            @Override
            public void success(BindedCdnEntity bindedCdnEntity, Response response) {
                if (BindedCdnEntity.NO_RECORD.equals(bindedCdnEntity.getRetcode())) {
                    return;
                } else {

                }
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.e(TAG, retrofitError.getMessage());
            }
        });
    }

    private void bindCdn(final String cdnId){


    }


}
