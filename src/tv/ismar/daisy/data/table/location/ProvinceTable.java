package tv.ismar.daisy.data.table.location;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by huaijie on 8/3/15.
 */

@Table(name = "app_province")
public class ProvinceTable extends Model {

    @Column(unique = true, onUniqueConflict = Column.ConflictAction.IGNORE)
    public String province_id;

    @Column
    public String province_name;

    @Column
    public String district_id;
}
