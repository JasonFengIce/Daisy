package tv.ismar.daisy.data.http.newvip.paylayer;

/**
 * Created by huaijie on 4/11/16.
 */
public class Vip {
    private String title;

    private int price;

    private String duration;

    private String vertical_url;

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getPrice() {
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

}
