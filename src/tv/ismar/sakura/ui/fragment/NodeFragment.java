package tv.ismar.sakura.ui.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import com.activeandroid.ActiveAndroid;
import com.activeandroid.content.ContentProvider;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import tv.ismar.daisy.R;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.sakura.core.CdnCacheLoader;
import tv.ismar.sakura.core.SakuraClientAPI;
import tv.ismar.sakura.data.http.BindedCdnEntity;
import tv.ismar.sakura.data.http.CdnListEntity;
import tv.ismar.sakura.data.http.Empty;
import tv.ismar.sakura.data.table.CdnCacheTable;
import tv.ismar.sakura.ui.adapter.NodeListAdapter;
import tv.ismar.sakura.ui.widget.SakuraButton;
import tv.ismar.sakura.ui.widget.SakuraListView;

import static tv.ismar.sakura.core.SakuraClientAPI.restAdapter_WX_API_TVXIO;

/**
 * Created by huaijie on 2015/4/8.
 */
public class NodeFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        AdapterView.OnItemClickListener, View.OnClickListener {
    private static final String TAG = "NodeFragment";

    private SakuraListView nodeListView;
    private NodeListAdapter nodeListAdapter;
    private TextView currentNodeTextView;
    private SakuraButton unbindButton;

    private PopupWindow selectNodePup;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fetchCdnList();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sakura_fragment_node, null);
        currentNodeTextView = (TextView) view.findViewById(R.id.current_node_text);
        unbindButton = (SakuraButton) view.findViewById(R.id.unbind_node);
        unbindButton.setOnClickListener(this);
        nodeListView = (SakuraListView) view.findViewById(R.id.node_list);
        nodeListView.setOnItemClickListener(this);
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
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        if (null != selectNodePup) {
            selectNodePup.dismiss();
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.unbind_node:
                unbindNode(SimpleRestClient.sn_token);
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        showSelectNodePop((Integer) view.getTag());
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
        updateCurrentNode();
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
                new Delete().from(CdnCacheTable.class).execute();
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

                fetchBindedCdn(SimpleRestClient.sn_token);
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.e(TAG, retrofitError.getMessage());
            }
        });
    }

    /**
     * fetchBindedCdn
     *
     * @param snCode
     */
    private void fetchBindedCdn(final String snCode) {
        SakuraClientAPI.GetBindCdn client = restAdapter_WX_API_TVXIO.create(SakuraClientAPI.GetBindCdn.class);
        client.excute(snCode, new Callback<BindedCdnEntity>() {
            @Override
            public void success(BindedCdnEntity bindedCdnEntity, Response response) {
                if (BindedCdnEntity.NO_RECORD.equals(bindedCdnEntity.getRetcode())) {
                    clearCheck();
                } else {
                    updateCheck(Integer.parseInt(bindedCdnEntity.getSncdn().getCdnid()));
                }
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.e(TAG, retrofitError.getMessage());
            }
        });
    }

    private void bindCdn(final String snCode, final int cdnId) {
        SakuraClientAPI.BindCdn client = restAdapter_WX_API_TVXIO.create(SakuraClientAPI.BindCdn.class);
        client.excute(snCode, cdnId, new Callback<Empty>() {
            @Override
            public void success(Empty empty, Response response) {
                Toast.makeText(getActivity(), R.string.node_bind_success, Toast.LENGTH_LONG).show();
                fetchBindedCdn(snCode);
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.e(TAG, retrofitError.getMessage());
            }
        });

    }

    private void unbindNode(final String snCode) {
        SakuraClientAPI.UnbindNode client = restAdapter_WX_API_TVXIO.create(SakuraClientAPI.UnbindNode.class);
        client.excute(snCode, new Callback<Empty>() {
            @Override
            public void success(Empty empty, Response response) {
                fetchBindedCdn(snCode);
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.e(TAG, retrofitError.getMessage());
            }
        });
    }

    /**
     * updateCheck
     *
     * @param cdnId
     */
    public static void updateCheck(int cdnId) {
        ActiveAndroid.beginTransaction();

        try {
            CdnCacheTable checkedItem = new Select().from(CdnCacheTable.class).where("checked = ?", true).executeSingle();
            if (null != checkedItem) {
                checkedItem.checked = false;
                checkedItem.save();
            }

            CdnCacheTable cdnCacheTable = new Select().from(CdnCacheTable.class).where("cdn_id = ?", cdnId).executeSingle();
            if (null != cdnCacheTable) {
                cdnCacheTable.checked = true;
                cdnCacheTable.save();
            }

            ActiveAndroid.setTransactionSuccessful();
        } finally {
            ActiveAndroid.endTransaction();
        }
    }

    /**
     * clearCheck
     */
    private void clearCheck() {
        CdnCacheTable checkedItem = new Select().from(CdnCacheTable.class).where("checked = ?", true).executeSingle();
        if (null != checkedItem) {
            checkedItem.checked = false;
            checkedItem.save();
        }
    }

    private void updateCurrentNode() {
        CdnCacheTable cdnCacheTable = new Select().from(CdnCacheTable.class).where("checked = ?", true).executeSingle();
        if (cdnCacheTable != null) {
            currentNodeTextView.setText(getText(R.string.current_node) + cdnCacheTable.cdn_nick);
            unbindButton.setText(R.string.switch_to_auto);
            unbindButton.setEnabled(true);
        } else {
            currentNodeTextView.setText(getText(R.string.current_node) + getString(R.string.auto_fetch));
            unbindButton.setText(R.string.already_to_auto);
            unbindButton.setEnabled(false);
        }

    }

    private void showSelectNodePop(final int cndId) {
        View contentView = LayoutInflater.from(getActivity())
                .inflate(R.layout.sakura_popup_select_node, null);
        contentView.setBackgroundResource(R.drawable.sakura_bg_popup);
        selectNodePup = new PopupWindow(contentView, 600, 180);
        selectNodePup.setFocusable(true);
        selectNodePup.showAtLocation(nodeListView, Gravity.CENTER, 0, 0);

        SakuraButton confirmButton = (SakuraButton) contentView.findViewById(R.id.confirm_btn);
        SakuraButton cancleButton = (SakuraButton) contentView.findViewById(R.id.cancle_btn);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bindCdn(SimpleRestClient.sn_token, cndId);
                selectNodePup.dismiss();
            }
        });

        cancleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectNodePup.dismiss();
            }
        });

    }



}
