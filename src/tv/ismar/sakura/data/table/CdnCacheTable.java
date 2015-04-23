package tv.ismar.sakura.data.table;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by huaijie on 2015/4/9.
 */

@Table(name = "cdn_cache", id = "_id")
public class CdnCacheTable extends Model {
    @Column(name = "cdn_id", unique = true, onUniqueConflict = Column.ConflictAction.IGNORE)
    public long cdn_id;

    @Column(name = "cdn_name")
    public String cdn_name = "";

    @Column(name = "cdn_nick")
    public String cdn_nick = "";

    @Column(name = "cdn_flag")
    public int cdn_flag = 0;

    @Column(name = "area")
    public int area = 0;

    @Column(name = "isp")
    public int isp = 0;

    @Column(name = "route_trace")
    public int route_trace = 0;

    @Column(name = "speed")
    public int speed = 0;

    @Column(name = "checked")
    public boolean checked = false;

}
