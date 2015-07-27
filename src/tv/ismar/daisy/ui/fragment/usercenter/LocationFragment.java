package tv.ismar.daisy.ui.fragment.usercenter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.GridView;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import tv.ismar.daisy.R;
import tv.ismar.daisy.data.table.weather.LocationTable;
import tv.ismar.daisy.data.table.weather.ProvinceTable;
import tv.ismar.daisy.ui.adapter.weather.CityAdapter;
import tv.ismar.daisy.ui.adapter.weather.CityAdapter.OnItemListener;
import tv.ismar.daisy.ui.adapter.weather.ProvinceAdapter;

import java.util.List;

/**
 * Created by huaijie on 7/13/15.
 */
public class LocationFragment extends Fragment implements ProvinceAdapter.OnItemListener {
    public static final String LOCATION_PREFERENCE_NAME = "location";

    public static final String LOCATION_PREFERENCE_GEOID = "geo_id";
    public static final String LOCATION_PREFERENCE_PROVINCE = "province";


    private Context mContext;

    private GridView provinceListView;
    private ProvinceAdapter provinceAdapter;

    private PopupWindow areaPopup;

    private View fragmentView;

    private SharedPreferences locationSharedPreferences;

    private TextView currentPostion;

    private SharedPreferences.OnSharedPreferenceChangeListener changeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            String stringRes = mContext.getString(R.string.location_current_position);
            String geoId = sharedPreferences.getString(LOCATION_PREFERENCE_GEOID, "101020100");
            LocationTable locationTable = new Select().from(LocationTable.class).where("geo_id=?", geoId).executeSingle();
            if (null != locationTable) {
                currentPostion.setText(String.format(stringRes, locationTable.city));
            }
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
        locationSharedPreferences = mContext.getSharedPreferences(LOCATION_PREFERENCE_NAME, Context.MODE_PRIVATE);
        locationSharedPreferences.registerOnSharedPreferenceChangeListener(changeListener);
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
        String geoId = locationSharedPreferences.getString(LOCATION_PREFERENCE_GEOID, "101020100");
        LocationTable locationTable = new Select().from(LocationTable.class).where("geo_id=?", geoId).executeSingle();
        if (null != locationTable) {
            currentPostion.setText(String.format(stringRes, locationTable.city));
        }

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
        areaPopup.showAtLocation(fragmentView, Gravity.CENTER, 170, 0);

        final List<LocationTable> locationTableList = new Select().from(LocationTable.class).where("province_id=?", provinceId).execute();
        CityAdapter cityAdapter = new CityAdapter(mContext, locationTableList);
        cityAdapter.setOnItemListener(new OnItemListener() {
            @Override
            public void onClick(View view, int position) {
                SharedPreferences.Editor editor = locationSharedPreferences.edit();
                editor.putString(LOCATION_PREFERENCE_GEOID, String.valueOf(locationTableList.get(position).geo_id));
                editor.putString(LOCATION_PREFERENCE_PROVINCE, provinceTable.province_name);
                editor.apply();
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
        String provinceId = provinceAdapter.getList().get(position).province_id;
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
