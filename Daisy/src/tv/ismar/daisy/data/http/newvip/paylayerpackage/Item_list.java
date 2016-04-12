package tv.ismar.daisy.data.http.newvip.paylayerpackage;

/**
 * Created by huaijie on 4/12/16.
 */
public class Item_list {
    private int item_id;

    private String vertical_url;

    private String cptitle;

    private String title;

    public void setItem_id(int item_id) {
        this.item_id = item_id;
    }

    public int getItem_id() {
        return this.item_id;
    }

    public void setVertical_url(String vertical_url) {
        this.vertical_url = vertical_url;
    }

    public String getVertical_url() {
        return this.vertical_url;
    }

    public void setCptitle(String cptitle) {
        this.cptitle = cptitle;
    }

    public String getCptitle() {
        return this.cptitle;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }

}
