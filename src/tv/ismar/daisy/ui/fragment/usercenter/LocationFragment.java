package tv.ismar.daisy.ui.fragment.usercenter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.PopupWindow;
import com.activeandroid.query.Select;
import tv.ismar.daisy.R;
import tv.ismar.daisy.data.table.weather.LocationTable;
import tv.ismar.daisy.data.table.weather.ProvinceTable;
import tv.ismar.daisy.ui.adapter.weather.CityAdapter;
import tv.ismar.daisy.ui.adapter.weather.ProvinceAdapter;

import java.util.List;

/**
 * Created by huaijie on 7/13/15.
 */
public class LocationFragment extends Fragment implements AdapterView.OnItemClickListener {
    private Context mContext;

    private GridView provinceListView;
    private ProvinceAdapter provinceAdapter;

    private PopupWindow areaPopup;

    private View fragmentView;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        fragmentView = inflater.inflate(R.layout.fragment_location, null);
        provinceListView = (GridView) fragmentView.findViewById(R.id.province_list);
        provinceListView.setOnItemClickListener(this);
        return fragmentView;
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
            provinceListView.setAdapter(provinceAdapter);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String provinceId = provinceAdapter.getList().get(position).province_id;
        showAreaPopup(provinceId);
    }

    private void showAreaPopup(String provinceId) {
        View popupLayout = LayoutInflater.from(mContext).inflate(R.layout.popup_area, null);
        GridView gridView = (GridView) popupLayout.findViewById(R.id.area_grid);

        int width = (int) mContext.getResources().getDimension(R.dimen.login_pop_width);
        int height = (int) mContext.getResources().getDimension(R.dimen.login_pop_height);
        areaPopup = new PopupWindow(popupLayout, width, height);
        areaPopup.setFocusable(true);
        areaPopup.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.transparent));
        areaPopup.showAtLocation(fragmentView, Gravity.CENTER, 200, 0);

        List<LocationTable> locationTableList = new Select().from(LocationTable.class).where("province_id=?", provinceId).execute();
        CityAdapter cityAdapter = new CityAdapter(mContext, locationTableList);
        gridView.setAdapter(cityAdapter);
    }

    @Override
    public void onDestroyView() {
        if (areaPopup != null && areaPopup.isShowing()) {
            areaPopup.dismiss();
        }
        super.onDestroyView();
    }
}
