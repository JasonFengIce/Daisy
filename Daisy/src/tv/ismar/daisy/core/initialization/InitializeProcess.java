package tv.ismar.daisy.core.initialization;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.baidu.location.*;
import com.google.gson.Gson;
import tv.ismar.daisy.R;
import tv.ismar.daisy.core.client.HttpMethod;
import tv.ismar.daisy.core.client.HttpResponseMessage;
import tv.ismar.daisy.core.client.JavaHttpClient;
import tv.ismar.daisy.core.preferences.AccountSharedPrefs;
import tv.ismar.daisy.data.http.CdnListEntity;
import tv.ismar.daisy.data.http.IpLookUpEntity;
import tv.ismar.daisy.data.table.location.*;
import tv.ismar.daisy.utils.HardwareUtils;
import tv.ismar.daisy.utils.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * Created by huaijie on 8/3/15.
 */
public class InitializeProcess implements Runnable {
    private static final String TAG = "InitializeProcess";

    private static final int[] PROVINCE_STRING_ARRAY_RES = {
            R.array.china_north,
            R.array.china_east,
            R.array.china_south,
            R.array.china_center,
            R.array.china_southwest,
            R.array.china_northwest,
            R.array.china_northeast
    };

    private Context mContext;

    private final String[] mDistrictArray;
    private final String[] mIspArray;


    public InitializeProcess(Context context) {
        this.mContext = context;
        mDistrictArray = mContext.getResources().getStringArray(R.array.district);
        mIspArray = mContext.getResources().getStringArray(R.array.isp);
    }

    @Override
    public void run() {
        initializeDistrict();
        initializeProvince();
        initalizeCity();
        initializeIsp();
        fetchCdnList();
        if (TextUtils.isEmpty(AccountSharedPrefs.getInstance(mContext).getSharedPrefs(AccountSharedPrefs.CITY))) {
            fetchLocationByIP();
        }

    }


    private void initializeDistrict() {
        if (new Select().from(DistrictTable.class).executeSingle() == null) {
            ActiveAndroid.beginTransaction();
            try {

                for (String district : mDistrictArray) {
                    DistrictTable districtTable = new DistrictTable();
                    districtTable.district_id = StringUtils.getMd5Code(district);
                    districtTable.district_name = district;
                    districtTable.save();
                }
                ActiveAndroid.setTransactionSuccessful();
            } finally {
                ActiveAndroid.endTransaction();
            }
        }
    }

    private void initializeProvince() {
        if (new Select().from(ProvinceTable.class).executeSingle() == null) {

            ActiveAndroid.beginTransaction();
            try {
                for (int i = 0; i < mDistrictArray.length; i++) {
                    String[] provinceArray = mContext.getResources().getStringArray(PROVINCE_STRING_ARRAY_RES[i]);
                    for (String province : provinceArray) {
                        ProvinceTable provinceTable = new ProvinceTable();
                        String[] strs = province.split(",");
                        String provinceName = strs[0];
                        String provincePinYin = strs[1];

                        provinceTable.province_name = provinceName;
                        provinceTable.pinyin = provincePinYin;

                        provinceTable.province_id = StringUtils.getMd5Code(provinceName);
                        provinceTable.district_id = StringUtils.getMd5Code(mDistrictArray[i]);
                        provinceTable.save();
                    }
                }
                ActiveAndroid.setTransactionSuccessful();
            } finally {
                ActiveAndroid.endTransaction();
            }
        }
    }

    private void initalizeCity() {
        if (new Select().from(CityTable.class).executeSingle() == null) {
            ActiveAndroid.beginTransaction();
            try {
                InputStream inputStream = mContext.getResources().getAssets().open("location.txt");
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String s;
                while ((s = bufferedReader.readLine()) != null) {
                    if (null != s && !s.equals("")) {
                        String[] strings = s.split("\\,");
                        Long geoId = Long.parseLong(strings[0]);
                        String area = strings[1];
                        String city = strings[2];
                        String province = strings[3];
                        String provinceId = HardwareUtils.getMd5ByString(province);

                        if (area.equals(city)) {
                            CityTable cityTable = new CityTable();
                            cityTable.geo_id = geoId;
                            cityTable.province_id = provinceId;
                            cityTable.city = city;
                            cityTable.save();
                        }
                    }
                }
                ActiveAndroid.setTransactionSuccessful();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                ActiveAndroid.endTransaction();
            }
        }
    }


    private void initializeIsp() {
        if (new Select().from(IspTable.class).executeSingle() == null) {
            ActiveAndroid.beginTransaction();
            try {
                for (String isp : mIspArray) {
                    IspTable ispTable = new IspTable();
                    ispTable.isp_id = StringUtils.getMd5Code(isp);
                    ispTable.isp_name = isp;
                    ispTable.save();
                }
                ActiveAndroid.setTransactionSuccessful();
            } finally {
                ActiveAndroid.endTransaction();
            }
        }
    }

    private void fetchCdnList() {
        String api = "http://wx.api.tvxio.com/shipinkefu/getCdninfo";
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("actiontype", "getcdnlist");
        new JavaHttpClient().doRequest(HttpMethod.GET, api, params, new JavaHttpClient.Callback() {
            @Override
            public void onSuccess(HttpResponseMessage result) {
                CdnListEntity cdnListEntity = new Gson().fromJson(result.responseResult, CdnListEntity.class);
                initializeCdnTable(cdnListEntity);
            }

            @Override
            public void onFailed(HttpResponseMessage error) {
                Log.e(TAG, "fetchCdnList error");
            }
        });
    }

    private void initializeCdnTable(CdnListEntity cdnListEntity) {
        new Delete().from(CdnTable.class).execute();
        ActiveAndroid.beginTransaction();
        try {
            for (CdnListEntity.CdnEntity cdnEntity : cdnListEntity.getCdn_list()) {
                CdnTable cdnTable = new CdnTable();
                cdnTable.cdn_id = cdnEntity.getCdnID();
                cdnTable.cdn_name = cdnEntity.getName();
                cdnTable.cdn_nick = cdnEntity.getNick();
                cdnTable.cdn_flag = cdnEntity.getFlag();
                cdnTable.cdn_ip = cdnEntity.getUrl();
                cdnTable.district_id = getDistrictId(cdnEntity.getNick());
                cdnTable.isp_id = getIspId(cdnEntity.getNick());
                cdnTable.route_trace = cdnEntity.getRoute_trace();
                cdnTable.speed = 0;
                cdnTable.checked = false;
                cdnTable.save();
            }
            ActiveAndroid.setTransactionSuccessful();
        } finally {
            ActiveAndroid.endTransaction();
        }
    }


    private void fetchLocationByGPS() {
        LocationClient locationClient = new LocationClient(mContext);
        locationClient.registerLocationListener(new BDLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation bdLocation) {

            }
        });

        GeofenceClient geofenceClient = new GeofenceClient(mContext);
        LocationClientOption locationClientOption = new LocationClientOption();
    }

    private void fetchLocationByIP() {
        String api = "http://lily.tvxio.com/iplookup";
        new JavaHttpClient().doRequest(api, new JavaHttpClient.Callback() {
            @Override
            public void onSuccess(HttpResponseMessage result) {
                IpLookUpEntity ipLookUpEntity = new Gson().fromJson(result.responseResult, IpLookUpEntity.class);
                initializeLocation(ipLookUpEntity);
            }

            @Override
            public void onFailed(HttpResponseMessage error) {
                Log.e(TAG, "fetchLocation: error");
            }
        });


    }

    private void initializeLocation(IpLookUpEntity ipLookUpEntity) {
        CityTable cityTable = new Select().from(CityTable.class).where(CityTable.CITY + " = ?", ipLookUpEntity.getCity()).executeSingle();

        AccountSharedPrefs accountSharedPrefs = AccountSharedPrefs.getInstance(mContext);
        accountSharedPrefs.setSharedPrefs(AccountSharedPrefs.PROVINCE, ipLookUpEntity.getProv());
        accountSharedPrefs.setSharedPrefs(AccountSharedPrefs.CITY, ipLookUpEntity.getCity());
        accountSharedPrefs.setSharedPrefs(AccountSharedPrefs.ISP, ipLookUpEntity.getIsp());
        accountSharedPrefs.setSharedPrefs(AccountSharedPrefs.IP, ipLookUpEntity.getIp());
        if (cityTable != null) {
            accountSharedPrefs.setSharedPrefs(AccountSharedPrefs.GEO_ID, String.valueOf(cityTable.geo_id));
        }

        ProvinceTable provinceTable = new Select().from(ProvinceTable.class)
                .where(ProvinceTable.PROVINCE_NAME + " = ?", ipLookUpEntity.getProv()).executeSingle();
        if (provinceTable != null) {
            accountSharedPrefs.setSharedPrefs(AccountSharedPrefs.PROVINCE_PY, provinceTable.pinyin);
        }
    }

    private String getDistrictId(String cdnNick) {
        for (String district : mDistrictArray) {
            if (cdnNick.contains(district)) {
                return StringUtils.getMd5Code(district);
            }
        }
        // 第三方节点返回 "0"
        return "0";
    }

    private String getIspId(String cdnNick) {
        for (String isp : mIspArray) {
            if (cdnNick.contains(isp)) {
                return StringUtils.getMd5Code(isp);
            }
        }
        return StringUtils.getMd5Code(mIspArray[mIspArray.length - 1]);
    }
}
