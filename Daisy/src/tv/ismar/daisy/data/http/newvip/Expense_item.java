package tv.ismar.daisy.data.http.newvip;

/**
 * Created by huaijie on 4/11/16.
 */
public class Expense_item {
    private String title;

    private float price;

    private String duration;

    private String vertical_url;

    private int pk;

    private String type;

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public float getPrice() {
        return this.price;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getDuration() {
        return this.duration;
    }

    public void setVertical_url(String vertical_url) {
        this.vertical_url = vertical_url;
    }

    public String getVertical_url() {
        return this.vertical_url;
    }

    public void setPk(int pk) {
        this.pk = pk;
    }

    public int getPk() {
        return this.pk;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

}