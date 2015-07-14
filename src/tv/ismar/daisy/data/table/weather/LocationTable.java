package tv.ismar.daisy.data.table.weather;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by huaijie on 7/13/15.
 */
@Table(id = "_id", name = "location")
public class LocationTable extends Model {
    @Column(unique = true, onUniqueConflict = Column.ConflictAction.IGNORE)
    public long geo_id;

    @Column
    public String province_id;

    @Column
    public String city;
}
