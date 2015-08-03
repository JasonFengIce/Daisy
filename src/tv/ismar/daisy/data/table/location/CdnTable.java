package tv.ismar.daisy.data.table.location;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by huaijie on 8/3/15.
 */

@Table(name = "app_cdn")
public class CdnTable extends Model {

    @Column(unique = true, onUniqueConflict = Column.ConflictAction.IGNORE)
    public long cdn_id;

    @Column
    public String cdn_name;

    @Column
    public String cdn_nick;

    @Column
    public int cdn_flag;

    @Column
    public String cdn_ip;

    @Column
    public String  district_id;

    @Column
    public String isp_id;

    @Column
    public int route_trace;

    @Column
    public int speed;

    @Column
    public boolean checked;


}
