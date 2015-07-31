package tv.ismar.daisy.core.advertisement;

import com.activeandroid.query.Select;
import tv.ismar.daisy.data.LaunchAdvertisementEntity;
import tv.ismar.daisy.data.table.AdvertisementTable;

import java.util.List;

import static tv.ismar.daisy.data.table.AdvertisementTable.MD5;
import static tv.ismar.daisy.data.table.AdvertisementTable.URL;

/**
 * Created by huaijie on 7/31/15.
 */
public class AdvertisementManager {
    public static final String LAUNCH_APP_ADVERTISEMENT = "launch_app";

    private static AdvertisementManager instance;

    private AdvertisementManager() {

    }

    public static AdvertisementManager getInstance() {
        if (instance == null) {
            instance = new AdvertisementManager();
        }
        return instance;
    }


    public void updateAppLaunchAdvertisement(LaunchAdvertisementEntity launchAdvertisementEntity) {
        String type = LAUNCH_APP_ADVERTISEMENT;
        LaunchAdvertisementEntity.AdvertisementData[] advertisementDatas = launchAdvertisementEntity.getAds().getKaishi();
        for (LaunchAdvertisementEntity.AdvertisementData advertisementData : advertisementDatas) {
            String mediaUrl = advertisementData.getMedia_url();
            String md5 = advertisementData.getMd5();

            AdvertisementTable advertisementTables = new Select()
                    .from(AdvertisementTable.class)
                    .where(URL + "=?", mediaUrl)
                    .where(MD5 + "=?", md5)
                    .executeSingle();

        }
    }

}
