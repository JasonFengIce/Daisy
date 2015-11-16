package tv.ismar.daisy.core.advertisement;

import android.content.Context;
import android.util.Log;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.google.gson.Gson;

import tv.ismar.daisy.core.preferences.LogSharedPrefs;
import tv.ismar.daisy.data.LaunchAdvertisementEntity;
import tv.ismar.daisy.data.table.AdvertisementTable;
import tv.ismar.daisy.utils.FileUtils;
import tv.ismar.daisy.utils.HardwareUtils;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static tv.ismar.daisy.data.table.AdvertisementTable.*;

/**
 * Created by huaijie on 7/31/15.
 */
public class AdvertisementManager {
    private static final String TAG = "AdvertisementManager";

    public static final String LAUNCH_APP_ADVERTISEMENT = "launch_app";

    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");


    private static Context mContext;


    public static void initialize(Context context) {
        mContext = context;
    }


    public void updateAppLaunchAdvertisement(LaunchAdvertisementEntity launchAdvertisementEntity) {
        String type = LAUNCH_APP_ADVERTISEMENT;
        LaunchAdvertisementEntity.AdvertisementData[] advertisementDatas = launchAdvertisementEntity.getAds().getKaishi();

        new Delete().from(AdvertisementTable.class).execute();
        for (LaunchAdvertisementEntity.AdvertisementData advertisementData : advertisementDatas) {
            AdvertisementTable advertisementTable = new AdvertisementTable();
            advertisementTable.title = advertisementData.getTitle();
            try {
                advertisementTable.start_date = dateFormat.parse(advertisementData.getStart_date()).getTime();
                advertisementTable.end_date = dateFormat.parse(advertisementData.getEnd_date()).getTime();
                advertisementTable.everyday_time_from = timeFormat.parse(advertisementData.getStart_time()).getTime();
                advertisementTable.everyday_time_to = timeFormat.parse(advertisementData.getEnd_time()).getTime();
            } catch (ParseException e) {
                Log.d(TAG, "updateAppLaunchAdvertisement: " + e.getMessage());
            }

            advertisementTable.url = advertisementData.getMedia_url();
            advertisementTable.location = FileUtils.getFileByUrl(advertisementData.getMedia_url());
            advertisementTable.md5 = advertisementData.getMd5();
            advertisementTable.type = type;
            advertisementTable.save();
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
                LogSharedPrefs.getInstance().setSharedPrefs(LogSharedPrefs.LAUNCH_APP_ADV_ENTITY, new Gson().toJson(advertisementDatas));
            } else {
                //compare md5 code
                if (!md5Code.equalsIgnoreCase(HardwareUtils.getMd5ByFile(localFile))) {
                    AdvertisementDownload downloadTask = new AdvertisementDownload(mContext, downlaodUrl, location);
                    new Thread(downloadTask).start();
                    LogSharedPrefs.getInstance().setSharedPrefs(LogSharedPrefs.LAUNCH_APP_ADV_ENTITY, new Gson().toJson(advertisementDatas));
                }
            }
        }
    }


    public String getAppLaunchAdvertisement() {
        Date todayDate = new Date();
        long todayDateTime = todayDate.getTime();
        long todayHour = 0;
        try {
            todayHour = timeFormat.parse(timeFormat.format(todayDate)).getTime();
        } catch (ParseException e) {
            Log.e(TAG, "getAppLaunchAdvertisement: " + e.getMessage());
        }


        List<AdvertisementTable> advertisementTables = new Select().from(AdvertisementTable.class)
                .where(START_DATE + " < ?", todayDateTime)
                .where(END_DATE + " > ?", todayDateTime)
                .where(EVERYDAY_TIME_FROM + " < ?", todayHour)
                .where(EVERYDAY_TIME_TO + " > ?", todayHour)
                .execute();
        if (advertisementTables != null && !advertisementTables.isEmpty()) {
            return "file://" + mContext.getFilesDir() + "/" + advertisementTables.get(0).location;
        } else {
            return "file:///android_asset/poster.png";
        }
    }
}
