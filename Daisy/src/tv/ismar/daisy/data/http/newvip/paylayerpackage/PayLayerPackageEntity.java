package tv.ismar.daisy.data.http.newvip.paylayerpackage;

import java.util.List;

/**
 * Created by huaijie on 4/12/16.
 */
public class PayLayerPackageEntity {
    private List<Item_list> item_list;

    private String description;

    private String title;

    private float price;

    private String duration;

    private int pk;

    private String type;

    public void setItem_list(List<Item_list> item_list) {
        this.item_list = item_list;
    }

    public List<Item_list> getItem_list() {
        return this.item_list;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }

    public void setPrice(double price) {
        this.price = (float) price;
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
