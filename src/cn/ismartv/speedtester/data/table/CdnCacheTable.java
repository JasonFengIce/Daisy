package cn.ismartv.speedtester.data.table;

import com.activeandroid.Cache;
import com.activeandroid.Model;
import com.activeandroid.TableInfo;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

/**
 * Created by huaijie on 3/10/15.
 */

@Table(name = "cdn_list")
public class CdnCacheTable extends Model {
    public static final String CDN_ID = "cdn_id";
    public static final String CDN_NAME = "cdn_name";
    public static final String CDN_NICK = "cdn_nick";
    public static final String FLAG = "flag";
    public static final String URL = "url";
    public static final String ROUTE_TRACE = "route_trace";
    public static final String AREA = "area";
    public static final String ISP = "isp";
    public static final String SPEED = "speed";
    public static final String CHECKED = "checked";
    public static final String RUNNING = "running";


    @Column(name = CDN_ID, uniqueGroups = {"group"}, onUniqueConflicts = {Column.ConflictAction.IGNORE})
    public long cdnId = 0;

    @Column(name = CDN_NAME)
    public String cdnName = "";

    @Column(name = CDN_NICK)
    public String cdnNick = "";

    @Column(name = FLAG)
    public int flag = 0;

    @Column(name = URL)
    public String url = "";

    @Column(name = ROUTE_TRACE)
    public int routeTrace = 0;

    @Column(name = AREA)
    public int area = 0;

    @Column(name = ISP)
    public int isp = 0;

    @Column(name = SPEED)
    public int speed = 0;

    @Column(name = CHECKED)
    public boolean checked = false;

    @Column(name = RUNNING)
    public boolean running = false;

    public static <T extends Model> T loadByCdnId(Class<T> type, int cdnId) {
        TableInfo tableInfo = Cache.getTableInfo(type);
        return (T) new Select().from(type).where(CDN_ID + "=?", cdnId).executeSingle();
    }
}
