package tv.ismar.daisy.ui.fragment.usercenter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.activeandroid.query.Select;
import tv.ismar.daisy.R;
import tv.ismar.daisy.core.preferences.AccountSharedPrefs;
import tv.ismar.daisy.data.table.location.CityTable;
import tv.ismar.daisy.data.table.location.ProvinceTable;
import tv.ismar.daisy.ui.adapter.weather.CityAdapter;
import tv.ismar.daisy.ui.adapter.weather.CityAdapter.OnItemListener;
import tv.ismar.daisy.ui.adapter.weather.ProvinceAdapter;

import java.util.List;

/**
 * Created by huaijie on 7/13/15.
 */
public class LocationFragment extends Fragment implements ProvinceAdapter.OnItemListener {


    private Context mContext;

    private GridView provinceListView;
    private ProvinceAdapter provinceAdapter;

    private PopupWindow areaPopup;

    private View fragmentView;


    private TextView currentPostion;

    private SharedPreferences.OnSharedPreferenceChangeListener changeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            String stringRes = mContext.getString(R.string.location_current_position);
//            String geoId = sharedPreferences.getString(LOCATION_PREFERENCE_GEOID, "101020100");
//
//            LocationTable locationTable = new Select().from(LocationTable.class).where("geo_id=?", geoId).executeSingle();
//            if (null != locationTable) {
            String cityName = AccountSharedPrefs.getInstance(mContext).getSharedPrefs(AccountSharedPrefs.CITY);
            currentPostion.setText(String.format(stringRes, cityName));
//            }
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AccountSharedPrefs.getInstance(mContext).getSharedPreferences().registerOnSharedPreferenceChangeListener(changeListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        fragmentView = inflater.inflate(R.layout.fragment_location, null);
        currentPostion = (TextView) fragmentView.findViewById(R.id.current_position);
        provinceListView = (GridView) fragmentView.findViewById(R.id.province_list);

        return fragmentView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String stringRes = mContext.getString(R.string.location_current_position);
        String cityName = AccountSharedPrefs.getInstance(mContext).getSharedPrefs(AccountSharedPrefs.CITY);

//        CityTable cityTable = new Select().from(CityTable.class).where(CityTable.CITY + " = ?", cityName).executeSingle();
//        if (null != cityTable) {
        currentPostion.setText(String.format(stringRes, cityName));
//        }

    }

    @Override
    public void onResume() {
        super.onResume();
        createLocationView();
    }


    private void createLocationView() {
        List<ProvinceTable> provinceTables = new Select().from(ProvinceTable.class).execute();
        if (provinceTables != null && !provinceTables.isEmpty()) {
            provinceAdapter = new ProvinceAdapter(mContext, provinceTables);
            provinceAdapter.setOnItemListener(this);
            provinceListView.setAdapter(provinceAdapter);
        }
    }


    private void showAreaPopup(final ProvinceTable provinceTable) {
        String provinceId = provinceTable.province_id;
        View popupLayout = LayoutInflater.from(mContext).inflate(R.layout.popup_area, null);
        GridView gridView = (GridView) popupLayout.findViewById(R.id.area_grid);

        int width = (int) mContext.getResources().getDimension(R.dimen.location_area_pop_width);
        int height = (int) mContext.getResources().getDimension(R.dimen.location_area_pop_height);
        areaPopup = new PopupWindow(popupLayout, width, height);
        areaPopup.setFocusable(true);
        areaPopup.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.transparent));
        int xOffset = (int) mContext.getResources().getDimension(R.dimen.locationFragment_areaPop_xOffset);
        int yOffset = (int) mContext.getResources().getDimension(R.dimen.locationFragment_areaPop_yOffset);
        areaPopup.showAtLocation(fragmentView, Gravity.CENTER, xOffset, yOffset);

        final List<CityTable> locationTableList = new Select().from(CityTable.class).where(CityTable.PROVINCE_ID + " = ?", provinceId).execute();
        CityAdapter cityAdapter = new CityAdapter(mContext, locationTableList);
        cityAdapter.setOnItemListener(new OnItemListener() {
            @Override
            public void onClick(View view, int position) {
                String city = locationTableList.get(position).city;
                AccountSharedPrefs accountSharedPrefs = AccountSharedPrefs.getInstance(mContext);
                accountSharedPrefs.setSharedPrefs(AccountSharedPrefs.CITY, city);
                accountSharedPrefs.setSharedPrefs(AccountSharedPrefs.PROVINCE, provinceTable.province_name);

                CityTable cityTable = new Select().from(CityTable.class).where(CityTable.CITY + " = ?", city).executeSingle();
                if (cityTable != null) {
                    accountSharedPrefs.setSharedPrefs(AccountSharedPrefs.GEO_ID, String.valueOf(cityTable.geo_id));
                }
                areaPopup.dismiss();
            }

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                TextView textView = (TextView) v;
                if (hasFocus) {
                    textView.setTextColor(mContext.getResources().getColor(R.color.location_text_focus));
                    textView.setTextSize(mContext.getResources().getDimension(R.dimen.h1_text_size));
                } else {
                    textView.setTextColor(mContext.getResources().getColor(R.color.white));
                    textView.setTextSize(mContext.getResources().getDimension(R.dimen.h2_text_size));
                }
            }
        });
        gridView.setAdapter(cityAdapter);
    }

    @Override
    public void onDestroyView() {
        if (areaPopup != null && areaPopup.isShowing()) {
            areaPopup.dismiss();
        }
        super.onDestroyView();
    }


    @Override
    public void onClick(View view, int position) {
        ProvinceTable provinceTable = provinceAdapter.getList().get(position);
        showAreaPopup(provinceTable);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        TextView textView = (TextView) v;
        if (hasFocus) {
            textView.setTextColor(mContext.getResources().getColor(R.color.location_text_focus));
            textView.setTextSize(mContext.getResources().getDimension(R.dimen.h1_text_size));

        } else {
            textView.setTextColor(mContext.getResources().getColor(R.color.white));
            textView.setTextSize(mContext.getResources().getDimension(R.dimen.h2_text_size));
        }
    }
}
