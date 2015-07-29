package tv.ismar.sakura.ui.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.activeandroid.ActiveAndroid;
import com.activeandroid.content.ContentProvider;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.google.gson.Gson;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import tv.ismar.daisy.R;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.sakura.core.CdnCacheLoader;
import tv.ismar.sakura.core.HttpDownloadTask;
import tv.ismar.sakura.core.SakuraClientAPI;
import tv.ismar.sakura.data.http.*;
import tv.ismar.sakura.data.table.CdnCacheTable;
import tv.ismar.sakura.data.table.CityTable;
import tv.ismar.sakura.ui.adapter.NodeListAdapter;
import tv.ismar.sakura.ui.widget.SakuraButton;
import tv.ismar.sakura.ui.widget.SakuraListView;
import tv.ismar.sakura.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static tv.ismar.sakura.core.SakuraClientAPI.*;

/**
 * Created by huaijie on 2015/4/8.
 */
public class NodeFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        AdapterView.OnItemClickListener, View.OnClickListener, HttpDownloadTask.OnCompleteListener,
        SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "NodeFragment";

    private SakuraListView nodeListView;
    private NodeListAdapter nodeListAdapter;
    private TextView currentNodeTextView;
    private SakuraButton unbindButton;
    private Spinner provinceSpinner;
    private Spinner ispSpinner;
    private SakuraButton speedTestButton;

    private PopupWindow selectNodePup;
    private Dialog cdnTestDialog;
    private PopupWindow cdnTestCompletedPop;

    private int provincesPosition;
    private int ispPosition;

    private String[] selectionArgs;
    private String[] cities;
    /**
     * 传入下载中的 CDN 节点 ID
     */
    private List<Integer> cdnCollections;
    private HttpDownloadTask httpDownloadTask;

    private String snCode = TextUtils.isEmpty(SimpleRestClient.sn_token) ? "sn is null" : SimpleRestClient.sn_token;

    SharedPreferences ipLookupPreferences;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ipLookupPreferences = getActivity().getSharedPreferences("user_location_info", Context.MODE_PRIVATE);
        ipLookupPreferences.registerOnSharedPreferenceChangeListener(this);
        cities = getResources().getStringArray(R.array.citys);
        fetchCdnList();
        fetchIpLookup();
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

        provinceSpinner = (Spinner) view.findViewById(R.id.province_spinner);
        ispSpinner = (Spinner) view.findViewById(R.id.isp_spinner);

        speedTestButton = (SakuraButton) view.findViewById(R.id.speed_test_btn);
        speedTestButton.setOnClickListener(this);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getLoaderManager().initLoader(0, null, this);

        ArrayAdapter<CharSequence> provinceSpinnerAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.citys, R.layout.sakura_spinner_text);
        provinceSpinnerAdapter.setDropDownViewResource(R.layout.sakura_item_spinner_dropdown);
        provinceSpinner.setAdapter(provinceSpinnerAdapter);

        ArrayAdapter<CharSequence> operatorSpinnerAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.isps, R.layout.sakura_spinner_text);
        operatorSpinnerAdapter.setDropDownViewResource(R.layout.sakura_item_spinner_dropdown);
        ispSpinner.setAdapter(operatorSpinnerAdapter);
        setSpinnerItemSelectedListener();
    }

    private void setSpinnerItemSelectedListener() {
        provinceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                provincesPosition = position;
                notifiySourceChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ispSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                ispPosition = position + 1;
                notifiySourceChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
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
        if (null != cdnTestCompletedPop) {
            cdnTestCompletedPop.dismiss();
        }
        if (null != cdnTestDialog) {
            cdnTestDialog.dismiss();
        }

        if (null != ipLookupPreferences) {
            ipLookupPreferences.unregisterOnSharedPreferenceChangeListener(this);
        }
        super.onDestroy();
    }


    private void notifiySourceChanged() {
        if (ispPosition == 4) {
            selectionArgs = new String[]{String.valueOf(StringUtils.getAreaCodeByProvince(cities[provincesPosition])),
                    String.valueOf(2), String.valueOf(3), "0"};
            getLoaderManager().destroyLoader(0);
            getLoaderManager().restartLoader(1, null, this).forceLoad();
        } else {
            selectionArgs = new String[]{String.valueOf(StringUtils.getAreaCodeByProvince(cities[provincesPosition])),
                    String.valueOf(ispPosition), "0"};
            getLoaderManager().destroyLoader(1);
            getLoaderManager().restartLoader(0, null, this).forceLoad();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.unbind_node:
                unbindNode(SimpleRestClient.sn_token);
                break;
            case R.id.speed_test_btn:
                showCdnTestDialog();
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
        String selection1 = "area" + "=? and " + "isp" + "=?" + " or cdn_flag  <> ?" + " ORDER BY isp,speed DESC";
        String selection2 = "area" + "=? and " + "isp" + " in (?, ?)" + " or cdn_flag  <> ?" + " ORDER BY isp,speed DESC";
        CdnCacheLoader cacheLoader = new CdnCacheLoader(getActivity(), ContentProvider.createUri(CdnCacheTable.class, null),
                null,
                null, null, null);
        switch (flag) {
            case 0:
                cacheLoader.setSelection(selection1);
                cacheLoader.setSelectionArgs(selectionArgs);
                break;
            case 1:
                cacheLoader.setSelection(selection2);
                cacheLoader.setSelectionArgs(selectionArgs);
                break;
            default:
                break;
        }
        return cacheLoader;
    }

    /**
     * @param loader
     * @param cursor
     */

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.d(TAG, "cursor count: " + cursor.getCount());
        cdnCollections = cursorToList(cursor);
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
                        cdnCacheTable.cdn_ip = cdnEntity.getUrl();
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

    /**
     * showSelectNodePop
     *
     * @param cndId
     */
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


    private void showCdnTestDialog() {
        cdnTestDialog = new Dialog(getActivity(), R.style.ProgressDialog);
        Window dialogWindow = cdnTestDialog.getWindow();
        dialogWindow.setGravity(Gravity.CENTER);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();

        lp.width = 400;
        lp.height = 150;
        View mView = LayoutInflater.from(getActivity()).inflate(R.layout.sakura_dialog_cdn_test_progress, null);
        cdnTestDialog.setContentView(mView, lp);
        cdnTestDialog.setCanceledOnTouchOutside(false);

        cdnTestDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_BACK:
                    case KeyEvent.KEYCODE_ESCAPE:
                        dialog.dismiss();
                        showCdnTestCompletedPop(Status.CANCEL);
                        return true;
                }
                return false;
            }
        });

        cdnTestDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                /**
                 * 开始测速
                 */
                httpDownloadTask = new HttpDownloadTask(getActivity());
                httpDownloadTask.setCompleteListener(NodeFragment.this);
                httpDownloadTask.execute(cdnCollections);
            }
        });
        cdnTestDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                httpDownloadTask.cancel(true);
                httpDownloadTask = null;
            }
        });
        cdnTestDialog.show();
    }


    private void showCdnTestCompletedPop(final Status status) {
        int titleRes;
        switch (status) {
            case COMPLETE:
                titleRes = R.string.test_complete_text;
                break;
            case CANCEL:
                titleRes = R.string.test_interupt;
                break;
            default:
                titleRes = R.string.test_complete_text;
                break;
        }


        View contentView = LayoutInflater.from(getActivity())
                .inflate(R.layout.sakura_popup_cdn_test_complete, null);
        /**
         * 标题
         */
        TextView title = (TextView) contentView.findViewById(R.id.complete_title);
        title.setText(titleRes);
        contentView.setBackgroundResource(R.drawable.sakura_bg_popup);
        cdnTestCompletedPop = new PopupWindow(null, 500, 150);
        cdnTestCompletedPop.setContentView(contentView);
        cdnTestCompletedPop.setFocusable(true);

        cdnTestCompletedPop.showAtLocation(nodeListView, Gravity.CENTER, 0, 0);

        SakuraButton cancleButton = (SakuraButton) contentView.findViewById(R.id.test_c_confirm_btn);
        cancleButton.requestFocus();
        cancleButton.requestFocusFromTouch();


        cancleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cdnTestCompletedPop.dismiss();
                switch (status) {
                    case CANCEL:
                        break;
                    case COMPLETE:
                        break;
                }

            }
        });
    }

    @Override
    public void onSingleComplete(String cndId, String nodeName, String speed) {
        SpeedLogEntity speedLog = new SpeedLogEntity();
        speedLog.setCdn_id(cndId);
        speedLog.setCdn_name(nodeName);
        speedLog.setSpeed(speed);

        SharedPreferences preferences = getActivity().getSharedPreferences("user_location_info", Context.MODE_PRIVATE);
        speedLog.setLocation(preferences.getString("user_default_city", ""));
        speedLog.setLocation(preferences.getString("user_default_isp", ""));


        Gson gson = new Gson();
        String data = gson.toJson(speedLog, SpeedLogEntity.class);
        String base64Data = Base64.encodeToString(data.getBytes(), Base64.DEFAULT);


        uploadTestResult(cndId, speed);
        uploadCdnTestLog(base64Data, snCode, Build.MODEL);
    }

    @Override
    public void onAllComplete() {
        if (cdnTestDialog != null) {
            cdnTestDialog.dismiss();
        }
        showCdnTestCompletedPop(Status.COMPLETE);

    }

    @Override
    public void onCancel() {

    }


    private void uploadCdnTestLog(String data, String snCode, String model) {
        SakuraClientAPI.DeviceLog client = restAdapter_SPEED_CALLA_TVXIO.create(SakuraClientAPI.DeviceLog.class);
        client.execute(data, snCode, model, new Callback<Empty>() {
            @Override
            public void success(Empty empty, Response response) {
                Log.d(TAG, "uploadCdnTestLog success");
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.e(TAG, "uploadCdnTestLog: " + retrofitError.getMessage());
            }
        });
    }


    public void uploadTestResult(String cdnId, String speed) {
        SakuraClientAPI.UploadResult client = restAdapter_WX_API_TVXIO.create(SakuraClientAPI.UploadResult.class);
        client.excute(SakuraClientAPI.UploadResult.ACTION_TYPE, snCode, cdnId, speed, new Callback<Empty>() {
            @Override
            public void success(Empty empty, Response response) {
                Log.i(TAG, "uploadTestResult success");
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.e(TAG, "uploadTestResult: " + retrofitError.getMessage());
            }
        });
    }

    /**
     * 将 cursor 转为 list, 因为 在使用 cursor 的时候,可能已经关闭了
     */
    public static List<Integer> cursorToList(Cursor cursor) {
        List<Integer> cdnCollections = new ArrayList<Integer>();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            cdnCollections.add(cursor.getInt(cursor.getColumnIndex("cdn_id")));
        }
        return cdnCollections;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        Log.d(TAG, "onSharedPreferenceChanged");

        int provincePosition = getProvincePositionByName(sharedPreferences.getString("user_default_city", ""));
        int ispPosition = getIspPositionByName(sharedPreferences.getString("user_default_isp", ""));

        provinceSpinner.setSelection(provincePosition);
        ispSpinner.setSelection(ispPosition);
    }


    enum Status {
        CANCEL,
        COMPLETE
    }

    private void fetchIpLookup() {
        SakuraClientAPI.IpLookUp client = restAdapter_LILY_TVXIO_HOST.create(SakuraClientAPI.IpLookUp.class);
        client.execute(new Callback<IpLookUpEntity>() {
            @Override
            public void success(IpLookUpEntity ipLookUpEntity, Response response) {
                SharedPreferences.Editor editor = ipLookupPreferences.edit();
                editor.putString("user_default_city", ipLookUpEntity.getCity());
                editor.putString("user_default_isp", ipLookUpEntity.getIsp());
                editor.apply();
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.e(TAG, "fetchIpLookup: " + retrofitError.getMessage());
            }
        });
    }

    public int getProvincePositionByName(String provinceName) {
        CityTable cityTable = new Select().from(CityTable.class).where(CityTable.NICK + " = ? ", provinceName).executeSingle();
        return cityTable.flag - 1;
    }

    public int getIspPositionByName(String ispName) {
        String[] isps = getResources().getStringArray(R.array.isps);
        for (int i = 0; i < isps.length; ++i) {
            if (ispName.equals(isps[i])) {
                return i;
            }
        }
        return -1;
    }
}
