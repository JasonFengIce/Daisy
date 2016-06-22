package tv.ismar.daisy.data.http.newvip.paylayervip;

/**
 * Created by huaijie on 4/12/16.
 */
public class Vip_list {
    private String description;

    private String duration;

    private int pk;

    private float price;

    private String vertical_url;

    private String title;

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getDuration() {
        return this.duration;
    }

    public void setPk(int pk) {
        this.pk = pk;
    }

    public int getPk() {
        return this.pk;
    }

    public void setPrice(double price) {
        this.price = (float) price;
    }

    public float getPrice() {
        return this.price;
    }

    public void setVertical_url(String vertical_url) {
        this.vertical_url = vertical_url;
    }

    public String getVertical_url() {
        return this.vertical_url;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
