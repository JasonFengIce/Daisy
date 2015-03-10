package cn.ismartv.speedtester.data.json;

import java.util.List;

/**
 * Created by huaijie on 3/10/15.
 */
public class CdnListEntity {

    private List<CdnEntity> cdn_list;
    private int retcode;
    private String retmsg;

    public List<CdnEntity> getCdn_list() {
        return cdn_list;
    }

    public void setCdn_list(List<CdnEntity> cdn_list) {
        this.cdn_list = cdn_list;
    }

    public int getRetcode() {
        return retcode;
    }

    public void setRetcode(int retcode) {
        this.retcode = retcode;
    }

    public String getRetmsg() {
        return retmsg;
    }

    public void setRetmsg(String retmsg) {
        this.retmsg = retmsg;
    }

    public class CdnEntity {
        private int cdnID;
        private int flag;
        private String name;
        private int route_trace;
        private String url;

        public int getCdnID() {
            return cdnID;
        }

        public void setCdnID(int cdnID) {
            this.cdnID = cdnID;
        }

        public int getFlag() {
            return flag;
        }

        public void setFlag(int flag) {
            this.flag = flag;
        }

        public String getCdnName() {
            return name.replace("|", "-").split("-")[0];
        }

        public String getCdnNick() {
            return name.replace("|", "-").split("-")[1];
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getRoute_trace() {
            return route_trace;
        }

        public void setRoute_trace(int route_trace) {
            this.route_trace = route_trace;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
