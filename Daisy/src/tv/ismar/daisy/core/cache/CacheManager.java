package tv.ismar.daisy.core.cache;

import android.content.Context;

import cn.ismartv.injectdb.library.query.Select;
import tv.ismar.daisy.core.client.DownloadClient;
import tv.ismar.daisy.core.client.DownloadThreadPool;
import tv.ismar.daisy.data.table.DownloadTable;
import tv.ismar.daisy.utils.FileUtils;
import tv.ismar.daisy.utils.HardwareUtils;

import java.io.File;

/**
 * Created by huaijie on 8/25/15.
 */
public class CacheManager {
    private static final String TAG = "CacheManager";

    private static CacheManager instance;
    private static Context mContext;

    public static void initialize(Context context) {
        mContext = context;
    }

    public static CacheManager getInstance() {
        if (instance == null) {
            instance = new CacheManager();
        }
        return instance;
    }

    public String doRequest(String url, String saveName, DownloadClient.StoreType storeType) {

        File downloadFile = null;
        switch (storeType) {
            case Internal:
                downloadFile = mContext.getFileStreamPath(saveName);
                break;
            case External:
                downloadFile = new File(HardwareUtils.getSDCardCachePath(), saveName);
                break;
        }

        DownloadTable downloadTable = new Select().from(DownloadTable.class).where(DownloadTable.DOWNLOAD_PATH + " =? ", downloadFile.getAbsolutePath()).executeSingle();
        if (downloadTable == null) {
            DownloadThreadPool.getInstance().add(new DownloadClient(mContext, url, saveName, storeType));
            return url;
        } else {
            String serverMD5 = FileUtils.getFileByUrl(url).split("\\.")[0];
            String localMD5 = downloadTable.local_md5;
            if (serverMD5.equalsIgnoreCase(localMD5)) {
                File file = new File(downloadTable.download_path);
                if (file.exists()){
                    return "file://" + downloadTable.download_path;
                }else {
                    return url;
                }
                
            } else {
                if (downloadTable.download_state.equals(DownloadClient.DownloadState.run.name())) {
                    //--------
                } else if (downloadTable.download_state.equals(DownloadClient.DownloadState.complete.name())) {
                    downloadTable.delete();
                    DownloadThreadPool.getInstance().add(new DownloadClient(mContext, url, saveName, storeType));
                }
                return url;
            }
        }
    }


}
