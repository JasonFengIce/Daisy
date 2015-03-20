package tv.ismar.daisy.core.advertisement;

import java.sql.Timestamp;

/**
 * Created by huaijie on 3/12/15.
 */
public class AdvertisementInfoEntity {
    private int customer;
    private String end_date;
    private String url;
    private String start_time;
    private long id;
    private String end_time;
    private String start_date;
    private String md5;

    private Timestamp endTimeStamp;
    private Timestamp startTimeStamp;

    public Timestamp getEndTimeStamp() {
        return Timestamp.valueOf(end_date + " " + end_time);

    }

    public void setEndTimeStamp(Timestamp endTimeStamp) {
        this.endTimeStamp = endTimeStamp;


    }

    public Timestamp getStartTimeStamp() {
        return Timestamp.valueOf(start_date + " " + start_time);
    }

    public void setStartTimeStamp(Timestamp startTimeStamp) {
        this.startTimeStamp = startTimeStamp;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public int getCustomer() {
        return customer;
    }

    public void setCustomer(int customer) {
        this.customer = customer;
    }

    public String getEnd_date() {
        return end_date;
    }

    public void setEnd_date(String end_date) {
        this.end_date = end_date;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }

    public String getStart_date() {
        return start_date;
    }

    public void setStart_date(String start_date) {
        this.start_date = start_date;
    }
}
