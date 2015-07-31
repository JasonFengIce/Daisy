package tv.ismar.daisy.data.table;

import com.activeandroid.Model;

import java.sql.Timestamp;

/**
 * Created by huaijie on 7/31/15.
 */
public class AdvertisementTable extends Model {
    public static final String TITLE = "title";
    public static final String START_TIME = "start_time";
    public static final String END_TIME = "end_time";
    public static final String URL = "url";
    public static final String LOCATION = "location";
    public static final String MD5 = "md5";
    public static final String TYPE = "type";


    public String title;

    public Timestamp start_time;

    public Timestamp end_time;

    public String url;

    public String location;

    public String md5;

    public String type;
}
