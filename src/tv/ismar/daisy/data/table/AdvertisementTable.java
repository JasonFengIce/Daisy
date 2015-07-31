package tv.ismar.daisy.data.table;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.sql.Timestamp;

/**
 * Created by huaijie on 7/31/15.
 */
@Table(name = "advertisement", id = "_id")
public class AdvertisementTable extends Model {
    public static final String TITLE = "title";
    public static final String START_TIME = "start_time";
    public static final String END_TIME = "end_time";
    public static final String URL = "url";
    public static final String LOCATION = "location";
    public static final String MD5 = "md5";
    public static final String TYPE = "type";

    @Column
    public String title;

    @Column
    public Timestamp start_time;

    @Column
    public Timestamp end_time;

    @Column
    public String url;

    @Column
    public String location;

    @Column
    public String md5;

    @Column
    public String type;
}
