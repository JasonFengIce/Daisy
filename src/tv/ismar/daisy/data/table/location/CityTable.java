package tv.ismar.daisy.data.table.location;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by huaijie on 8/3/15.
 */
@Table(name = "app_city", id = "_id")
public class CityTable extends Model {

    public static final String GEO_ID = "geo_id";
    public static final String CITY = "city";
    public static final String PROVINCE_ID = "province_id";


    @Column(unique = true, onUniqueConflict = Column.ConflictAction.IGNORE)
    public long geo_id;

    @Column
    public String city;

    @Column
    public String province_id;
}