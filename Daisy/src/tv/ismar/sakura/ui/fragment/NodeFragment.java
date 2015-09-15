package tv.ismar.sakura.ui.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.activeandroid.ActiveAndroid;
import com.activeandroid.content.ContentProvider;
import com.activeandroid.query.Select;
import com.google.gson.Gson;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import tv.ismar.daisy.R;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.preferences.AccountSharedPrefs;
import tv.ismar.daisy.data.table.location.CdnTable;
import tv.ismar.daisy.data.table.location.IspTable;
import tv.ismar.daisy.data.table.location.ProvinceTable;
import tv.ismar.daisy.utils.StringUtils;
import tv.ismar.sakura.core.CdnCacheLoader;
import tv.ismar.sakura.core.HttpDownloadTask;
import tv.ismar.sakura.core.SakuraClientAPI;
import tv.ismar.sakura.data.http.BindedCdnEntity;
import tv.ismar.sakura.data.http.Empty;
import tv.ismar.sakura.data.http.SpeedLogEntity;
import tv.ismar.sakura.ui.adapter.IspSpinnerAdapter;
import tv.ismar.sakura.ui.adapter.NodeListAdapter;
import tv.ismar.sakura.ui.adapter.ProvinceSpinnerAdapter;
import tv.ismar.sakura.ui.widget.SakuraButton;
import tv.ismar.sakura.ui.widget.SakuraListView;

import java.util.ArrayList;
import java.util.List;

import static tv.ismar.sakura.core.SakuraClientAPI.restAdapter_SPEED_CALLA_TVXIO;
import static tv.ismar.sakura.core.SakuraClientAPI.restAdapter_WX_API_TVXIO;

/**
 * Created by huaijie on 2015/4/8.
 */
public class NodeFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        HttpDownloadTask.OnCompleteListener, View.OnClickListener, AdapterView.OnItemClickListener {
    private static final String TAG = "NodeFragment";

    private static String NORMAL_SELECTION = CdnTable.DISTRICT_ID + "=? and " + CdnTable.ISP_ID + "=?" + " or " + CdnTable.CDN_FLAG + "  <> ?" + " ORDER BY " + CdnTable.ISP_ID + " DESC," + CdnTable.SPEED + " DESC";
    private static String OTHER_SELECTION = CdnTable.DISTRICT_ID + "=? and " + CdnTable.ISP_ID + " in (?, ?)" + " or " + CdnTable.CDN_FLAG + "  <> ?" + " ORDER BY " + CdnTable.ISP_ID + " DESC," + CdnTable.SPEED + " DESC";

    private static final String NOT_THIRD_CDN = "0";

    private static final int NORMAL_ISP_FLAG = 01245;
    private static final int OTHER_ISP_FLAG = 3;

    private String TIE_TONG = "";

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


    private String[] selectionArgs = {"0", "0"};
    private String[] cities;

    private String mDistrictId = "";
    private String mIspId = "";

    /**
     * 传入下载中的 CDN 节点 ID
     */
    private List<Integer> cdnCollections;

    private String snCode = TextUtils.isEmpty(SimpleRestClient.sn_token) ? "sn is null" : SimpleRestClient.sn_token;


    private Context mContext;

    private HttpDownloadTask httpDownloadTask;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mContext = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TIE_TONG = StringUtils.getMd5Code("铁通");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sakura_fragment_node, null);
        currentNodeTextView = (TextView) view.findViewById(R.id.current_node_text);
        unbindButton = (SakuraButton) view.findViewById(R.id.unbind_node);
        nodeListView = (SakuraListView) view.findViewById(R.id.node_list);
        nodeListAdapter = new NodeListAdapter(mContext, null, true);
        nodeListView.setAdapter(nodeListAdapter);

        provinceSpinner = (Spinner) view.findViewById(R.id.province_spinner);
        ispSpinner = (Spinner) view.findViewById(R.id.isp_spinner);

        speedTestButton = (SakuraButton) view.findViewById(R.id.speed_test_btn);

        speedTestButton.setOnClickListener(this);
        nodeListView.setOnItemClickListener(this);
        unbindButton.setOnClickListener(this);

        nodeListView.setNextFocusDownId(nodeListView.getId());
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        List<ProvinceTable> provinceTables = new Select().from(ProvinceTable.class).execute();
        ProvinceSpinnerAdapter provinceSpinnerAdapter = new ProvinceSpinnerAdapter(mContext, provinceTables);
        provinceSpinner.setAdapter(provinceSpinnerAdapter);
        String accountProvince = AccountSharedPrefs.getInstance(mContext).getSharedPrefs(AccountSharedPrefs.PROVINCE);

        ProvinceTable provinceTable = new Select().from(ProvinceTable.class).
                where(ProvinceTable.PROVINCE_NAME + " = ?", accountProvince).executeSingle();
        if (provinceTable != null) {
            provinceSpinner.setSelection((provinceTable.getId().intValue() - 1));
        }


        List<IspTable> ispTables = new Select().from(IspTable.class).execute();
        IspSpinnerAdapter ispSpinnerAdapter = new IspSpinnerAdapter(mContext, ispTables);
        ispSpinner.setAdapter(ispSpinnerAdapter);

        String accountIsp = AccountSharedPrefs.getInstance(mContext).getSharedPrefs(AccountSharedPrefs.ISP);
        IspTable ispTable = new Select().from(IspTable.class).where(IspTable.ISP_NAME + " = ?", accountIsp).executeSingle();
        if (ispTable != null) {
            ispSpinner.setSelection(ispTable.getId().intValue() - 1);
        }
        setSpinnerItemSelectedListener();
        getLoaderManager().initLoader(NORMAL_ISP_FLAG, null, this);


    }

    @Override
    public Loader onCreateLoader(int flag, Bundle args) {
        CursorLoader cacheLoader = new CdnCacheLoader(mContext, ContentProvider.createUri(CdnTable.class, null),
                null, null, null, null);
        switch (flag) {
            case NORMAL_ISP_FLAG:
                cacheLoader.setSelection(NORMAL_SELECTION);
                cacheLoader.setSelectionArgs(selectionArgs);
                break;
            case OTHER_ISP_FLAG:
                cacheLoader.setSelection(OTHER_SELECTION);
                cacheLoader.setSelectionArgs(selectionArgs);
                break;
            default:
                break;
        }
        return cacheLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cdnCollections = cursorToList(data);
        nodeListAdapter.swapCursor(data);
        updateCurrentNode();
    }


    @Override
    public void onLoaderReset(Loader loader) {
        nodeListAdapter.swapCursor(null);
    }

    private void setSpinnerItemSelectedListener() {
        provinceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                ProvinceTable provinceTable = new Select().from(ProvinceTable.class).where("_id = ?", position + 1).executeSingle();
                if (provinceTable != null) {
                    mDistrictId = provinceTable.district_id;
                    notifiySourceChanged();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ispSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                IspTable ispTable = new Select().from(IspTable.class).where("_id = ?", position + 1).executeSingle();
                if (ispTable != null) {
                    mIspId = ispTable.isp_id;
                    notifiySourceChanged();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }


    private void notifiySourceChanged() {
        if (mIspId.equals(TIE_TONG)) {

            String unicom = StringUtils.getMd5Code("联通");
            String chinaMobile = StringUtils.getMd5Code("移动");
            selectionArgs = new String[]{mDistrictId, chinaMobile, unicom, NOT_THIRD_CDN};
            getLoaderManager().destroyLoader(NORMAL_ISP_FLAG);
            getLoaderManager().restartLoader(OTHER_ISP_FLAG, null, NodeFragment.this).forceLoad();
        } else {
            selectionArgs = new String[]{mDistrictId, mIspId, NOT_THIRD_CDN};
            getLoaderManager().destroyLoader(OTHER_ISP_FLAG);
            getLoaderManager().restartLoader(NORMAL_ISP_FLAG, null, NodeFragment.this).forceLoad();
        }

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

        super.onDestroy();
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

    //    /**
//     * fetchBindedCdn
//     *
//     * @param snCode
//     */
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
                Toast.makeText(mContext, R.string.node_bind_success, Toast.LENGTH_LONG).show();
                fetchBindedCdn(snCode);
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.e(TAG, "bindCdn error");
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
                Log.e(TAG, "unbindNode");
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
            CdnTable checkedItem = new Select().from(CdnTable.class).where(CdnTable.CHECKED + " = ?", true).executeSingle();
            if (null != checkedItem) {
                checkedItem.checked = false;
                checkedItem.save();
            }

            CdnTable cdnCacheTable = new Select().from(CdnTable.class).where(CdnTable.CDN_ID + " = ?", cdnId).executeSingle();
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
        CdnTable checkedItem = new Select().from(CdnTable.class).where("checked = ?", true).executeSingle();
        if (null != checkedItem) {
            checkedItem.checked = false;
            checkedItem.save();
        }
    }

    private void updateCurrentNode() {
        CdnTable cdnCacheTable = new Select().from(CdnTable.class).where("checked = ?", true).executeSingle();
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
        View contentView = LayoutInflater.from(mContext)
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
        cdnTestDialog = new Dialog(mContext, R.style.ProgressDialog);
        Window dialogWindow = cdnTestDialog.getWindow();
        dialogWindow.setGravity(Gravity.CENTER);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();

        lp.width = 400;
        lp.height = 150;
        View mView = LayoutInflater.from(mContext).inflate(R.layout.sakura_dialog_cdn_test_progress, null);
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
                httpDownloadTask = new HttpDownloadTask(mContext);
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


        View contentView = LayoutInflater.from(mContext)
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

        speedLog.setLocation(AccountSharedPrefs.getInstance(mContext).getSharedPrefs(AccountSharedPrefs.CITY));
        speedLog.setLocation(AccountSharedPrefs.getInstance(mContext).getSharedPrefs(AccountSharedPrefs.ISP));


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
                Log.e(TAG, "uploadCdnTestLog");
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
                Log.e(TAG, "uploadTestResult");
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

    enum Status {
        CANCEL,
        COMPLETE
    }
}
