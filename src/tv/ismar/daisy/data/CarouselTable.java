package tv.ismar.daisy.data;

import com.activeandroid.Model;
import com.activeandroid.annotation.Table;

/**
 * Created by huaijie on 6/4/15.
 */
@Table(name = "carousel", id = "_id")
public class CarouselTable extends Model {

    public String title;

    public String md5;

    public String url;

    public String path;
}
