package tv.ismar.sakura.data.table;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by huaijie on 11/27/14.
 */
@Table(name = "city")
public class CityTable extends Model {
    public static final String FLAG = "flag";
    public static final String NAME = "name";
    public static final String NICK = "nick";
    public static final String AREA_NAME = "area_name";
    public static final String AREA_FLAG = "area_flag";

    @Column(name = FLAG, uniqueGroups = {"group1"}, onUniqueConflicts = {Column.ConflictAction.IGNORE})
    public int flag = -1;

    @Column(name = NAME)
    public String name = "";

    @Column(name = NICK)
    public String nick = "";

    @Column(name = AREA_NAME)
    public String areaName = "";

    @Column(name = AREA_FLAG)
    public int areaFlag = -1;
}
