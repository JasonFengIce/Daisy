package cn.ismartv.speedtester.core;

import android.util.Log;
import cn.ismartv.speedtester.data.json.CdnListEntity;
import cn.ismartv.speedtester.data.table.CdnCacheTable;
import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

/**
 * Created by huaijie on 3/10/15.
 */
public class CdnCacheManager {
    private static final String TAG = "CdnCacheManager";

    private static CdnCacheManager instance;

    private CdnCacheManager() {

    }

    public static CdnCacheManager getInstance() {
        if (instance == null)
            instance = new CdnCacheManager();
        return instance;
    }

    public void queryAll() {
        CdnCacheTable cdnCacheTable = new Select()
                .from(CdnCacheTable.class)
                .executeSingle();
        if (cdnCacheTable == null)
            Log.i(TAG, "cdn cache table is empty!!!");
    }

    public void saveCdnList(CdnListEntity cdnListEntity) {
        new Delete().from(CdnCacheTable.class).execute();
        ActiveAndroid.beginTransaction();
        try {
            for (CdnListEntity.CdnEntity cdnEntity : cdnListEntity.getCdn_list()) {
                CdnCacheTable cdnCacheTable = new CdnCacheTable();
                cdnCacheTable.cdnId = cdnEntity.getCdnID();
                cdnCacheTable.cdnName = cdnEntity.getCdnName();
                cdnCacheTable.cdnNick = cdnEntity.getCdnNick();
                cdnCacheTable.flag = cdnEntity.getFlag();
                cdnCacheTable.url = cdnEntity.getUrl();
                cdnCacheTable.routeTrace = cdnEntity.getRoute_trace();
                cdnCacheTable.area = CdnCacheUtils.getAreaCodeByCdnNick(cdnEntity.getCdnNick());
                cdnCacheTable.isp = CdnCacheUtils.getIspByCdnNick(cdnEntity.getCdnNick());
                cdnCacheTable.save();
            }
            ActiveAndroid.setTransactionSuccessful();
        } finally {
            ActiveAndroid.endTransaction();
        }
    }
}
