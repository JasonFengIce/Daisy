package tv.ismar.daisy.update;

import java.util.List;

/**
 * Created by huaijie on 3/9/15.
 */
public class VersionInfoEntity {
    private String version;
    private String mandatory;
    private String homepage;
    private String md5;
    private String downloadurl;
    private String update_title;
    private List<String> update_msg;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getMandatory() {
        return mandatory;
    }

    public void setMandatory(String mandatory) {
        this.mandatory = mandatory;
    }

    public String getHomepage() {
        return homepage;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getDownloadurl() {
        return downloadurl;
    }

    public void setDownloadurl(String downloadurl) {
        this.downloadurl = downloadurl;
    }

    public String getUpdate_title() {
        return update_title;
    }

    public void setUpdate_title(String update_title) {
        this.update_title = update_title;
    }

    public List<String> getUpdate_msg() {
        return update_msg;
    }

    public void setUpdate_msg(List<String> update_msg) {
        this.update_msg = update_msg;
    }
}
