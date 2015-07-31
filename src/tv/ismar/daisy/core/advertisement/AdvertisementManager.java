package tv.ismar.daisy.core.advertisement;

import android.content.Context;
import android.util.Log;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import tv.ismar.daisy.data.LaunchAdvertisementEntity;
import tv.ismar.daisy.data.table.AdvertisementTable;
import tv.ismar.daisy.utils.FileUtils;
import tv.ismar.daisy.utils.HardwareUtils;

import java.io.File;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import static tv.ismar.daisy.data.table.AdvertisementTable.*;

/**
 * Created by huaijie on 7/31/15.
 */
public class AdvertisementManager {
    private static final String TAG = "AdvertisementManager";

    public static final String LAUNCH_APP_ADVERTISEMENT = "launch_app";

    private static AdvertisementManager instance;
    private static Context mContext;

    private AdvertisementManager() {

    }

    public static AdvertisementManager getInstance(Context context) {
        if (instance == null) {
            instance = new AdvertisementManager();
            mContext = context;
        }
        return instance;
    }


    public void updateAppLaunchAdvertisement(LaunchAdvertisementEntity launchAdvertisementEntity) {
        String type = LAUNCH_APP_ADVERTISEMENT;
        LaunchAdvertisementEntity.AdvertisementData[] advertisementDatas = launchAdvertisementEntity.getAds().getKaishi();
        for (LaunchAdvertisementEntity.AdvertisementData advertisementData : advertisementDatas) {
            String mediaUrl = advertisementData.getMedia_url();
            String md5 = advertisementData.getMd5();

            AdvertisementTable advertisementTable = new Select()
                    .from(AdvertisementTable.class)
                    .where(URL + "=?", mediaUrl)
                    .where(MD5 + "=?", md5)
                    .executeSingle();


            if (advertisementTable == null) {
                advertisementTable = new AdvertisementTable();
                advertisementTable.title = advertisementData.getTitle();
                advertisementTable.start_time = Timestamp.valueOf(advertisementData.getStart_date() + " " + advertisementData.getStart_time()).getTime();
                advertisementTable.end_time = Timestamp.valueOf(advertisementData.getEnd_date() + " " + advertisementData.getEnd_time()).getTime();
                advertisementTable.url = advertisementData.getMedia_url();
                advertisementTable.location = FileUtils.getFileByUrl(advertisementData.getMedia_url());
                advertisementTable.md5 = advertisementData.getMd5();
                advertisementTable.type = type;
                advertisementTable.save();
            }
        }

        List<AdvertisementTable> advertisementTables = new Select().from(AdvertisementTable.class).execute();
        for (AdvertisementTable advertisementTable : advertisementTables) {
            String downlaodUrl = advertisementTable.url;
            File localFile = new File(mContext.getFilesDir() + "/" + advertisementTable.location);
            String location = advertisementTable.location;
            String md5Code = advertisementTable.md5;
            if (!localFile.exists()) {
                //download advertisement
                AdvertisementDownload downloadTask = new AdvertisementDownload(mContext, downlaodUrl, location);
                new Thread(downloadTask).start();
            } else {
                //compare md5 code
                if (!md5Code.equalsIgnoreCase(HardwareUtils.getMd5ByFile(localFile))) {
                    AdvertisementDownload downloadTask = new AdvertisementDownload(mContext, downlaodUrl, location);
                    new Thread(downloadTask).start();
                }
            }
        }
    }


    public String getAppLaunchAdvertisement() {
        long todayDateTime = new Date().getTime();
        List<AdvertisementTable> advertisementTables = new Select().from(AdvertisementTable.class)
                .where(START_TIME + " < ?", todayDateTime)
                .where(END_TIME + " > ?", todayDateTime)
                .execute();
        if (advertisementTables != null && !advertisementTables.isEmpty()) {
            return "file://" + mContext.getFilesDir() + "/" + advertisementTables.get(0).location;
        } else {
            return "file:///android_asset/poster.png";
        }
    }
}
