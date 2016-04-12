package tv.ismar.daisy.data.http.newvip.paylayervip;

import java.util.List;

/**
 * Created by huaijie on 4/12/16.
 */
public class PayLayerVipEntity {
    private List<Vip_list> vip_list;

    private String type;

    private String cpname;

    private int cpid;

    public void setVip_list(List<Vip_list> vip_list) {
        this.vip_list = vip_list;
    }

    public List<Vip_list> getVip_list() {
        return this.vip_list;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

    public void setCpname(String cpname) {
        this.cpname = cpname;
    }

    public String getCpname() {
        return this.cpname;
    }

    public void setCpid(int cpid) {
        this.cpid = cpid;
    }

    public int getCpid() {
        return this.cpid;
    }
}
