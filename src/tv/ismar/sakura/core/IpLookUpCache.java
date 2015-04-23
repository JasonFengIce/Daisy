package tv.ismar.sakura.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.activeandroid.query.Select;
import tv.ismar.daisy.AppConstant;
import tv.ismar.daisy.R;
import tv.ismar.sakura.data.http.IpLookUpEntity;
import tv.ismar.sakura.data.table.CityTable;

/**
 * Created by huaijie on 2015/4/23.
 */
public class IpLookUpCache {
    private static final String NAME = "ip_lookup";

    public static final String USER_PROVINCE = "user_province";
    public static final String USER_IP = "user_ip";
    public static final String USER_ISP = "user_isp";
    public static final String USER_CITY = "user_city";

    private Context mContext;
    private static IpLookUpCache instance;

    private IpLookUpCache(Context context) {
    this.mContext= context;
    }

    public static IpLookUpCache getInstance(Context context) {

        if (null == instance) {
            instance = new IpLookUpCache(context);
        }
        return instance;
    }


    public String getUserProvince() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        String[] array = mContext.getResources().getStringArray(R.array.citys);
        int position = sharedPreferences.getInt(USER_PROVINCE, 0);

        if (position == -1)
            position = 0;

        return array[position];
    }

    public String getUserIp() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(NAME, Context.MODE_PRIVATE);

        return sharedPreferences.getString(USER_IP, "0.0.0.0");
    }

    public String getUserIsp() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        String[] array = mContext.getResources().getStringArray(R.array.isps);
        int position = sharedPreferences.getInt(USER_ISP, 0);

        if (position == -1)
            position = 0;
        return array[position];
    }

    public String getUserCity() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(USER_CITY, "");
    }

    public void updateIpLookUpCache(IpLookUpEntity ipLookUpEntity) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        int provincePosition = getProvincePositionByName(ipLookUpEntity.getProv());
        int ispPosition = getIspPositionByName(ipLookUpEntity.getIsp());
        String ipAddress = ipLookUpEntity.getIp();
        editor.putString(USER_CITY, ipLookUpEntity.getCity());
        editor.putInt(USER_PROVINCE, provincePosition);
        editor.putInt(USER_ISP, ispPosition);
        editor.putString(USER_IP, ipAddress);
        editor.apply();

    }

    public int getProvincePositionByName(String provinceName) {
        CityTable cityTable = new Select()
                .from(CityTable.class)
                .where(CityTable.NICK + " = ? ", provinceName)
                .executeSingle();
        return cityTable.flag - 1;
    }

    public int getIspPositionByName(String ispName) {
        String[] isps = mContext.getResources().getStringArray(R.array.isps);
        for (int i = 0; i < isps.length; ++i) {
            if (ispName.equals(isps[i])) {
                return i;
            }
        }
        return -1;
    }
}
