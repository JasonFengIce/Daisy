package tv.ismar.sakura.data.http;

import java.util.ArrayList;

/**
 * Created by huaijie on 2015/4/9.
 */
public class CdnListEntity {
    private ArrayList<CdnEntity> cdn_list;
    private String retcode;
    private String retmsg;

    public ArrayList<CdnEntity> getCdn_list() {
        return cdn_list;
    }

    public void setCdn_list(ArrayList<CdnEntity> cdn_list) {
        this.cdn_list = cdn_list;
    }

    public String getRetcode() {
        return retcode;
    }

    public void setRetcode(String retcode) {
        this.retcode = retcode;
    }

    public String getRetmsg() {
        return retmsg;
    }

    public void setRetmsg(String retmsg) {
        this.retmsg = retmsg;
    }

    class CdnEntity {
        private String cdnID;
        private String flag;
        private String name;
        private String route_trace;
        private String url;
        private String ping;

        private String nick;

        public String getPing() {
            return ping;
        }

        public void setPing(String ping) {
            this.ping = ping;
        }

        public void setNick(String nick) {
            this.nick = nick;
        }

        public int getSpeed() {
            return speed;
        }

        public void setSpeed(int speed) {
            this.speed = speed;
        }

        private int speed;

        public String getTestFile() {
            return "http://" + getUrl() + "/cdn/speedtest.ts";
        }

        public String getCdnID() {
            return cdnID;
        }

        public void setCdnID(String cdnID) {
            this.cdnID = cdnID;
        }

        public String getFlag() {
            return flag;
        }

        public void setFlag(String flag) {
            this.flag = flag;
        }

        public String getName() {
            return name.replace("|", "-").split("-")[0];
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRoute_trace() {
            return route_trace;
        }

        public void setRoute_trace(String route_trace) {
            this.route_trace = route_trace;
        }

        public String getUrl() {
            return url.replace("|", "-").split("-")[0];
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getNick() {
            return name.replace("|", "-").split("-")[1];
        }
    }
}
