package cn.ismartv.tvplayer.mvvm.model;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

/**
 * Created by huibin on 5/31/16.
 */
public class ClipInfoEntity {

    //爱奇艺clip info
    private String iqiyi_4_0;
    private boolean is_vip;

    public String getIqiyi_4_0() {
        return iqiyi_4_0;
    }

    public void setIqiyi_4_0(String iqiyi_4_0) {
        this.iqiyi_4_0 = iqiyi_4_0;
    }

    public boolean is_vip() {
        return is_vip;
    }

    public void setIs_vip(boolean is_vip) {
        this.is_vip = is_vip;
    }

    //视云clip info
    private String medium;

    private String normal;

    private String blueray;

    private String high;

    private String low;

    private String adaptive;

    private String ultra;

    @SerializedName("4k")
    private String _4k;

    public void setMedium(String medium) {
        this.medium = medium;
    }

    public String getMedium() {
        return this.medium;
    }

    public void setNormal(String normal) {
        this.normal = normal;
    }

    public String getNormal() {
        return this.normal;
    }

    public void setBlueray(String blueray) {
        this.blueray = blueray;
    }

    public String getBlueray() {
        return this.blueray;
    }

    public void setHigh(String high) {
        this.high = high;
    }

    public String getHigh() {
        return this.high;
    }

    public void setLow(String low) {
        this.low = low;
    }

    public String getLow() {
        return this.low;
    }

    public void setAdaptive(String adaptive) {
        this.adaptive = adaptive;
    }

    public String getAdaptive() {
        return this.adaptive;
    }

    public void setUltra(String ultra) {
        this.ultra = ultra;
    }

    public String getUltra() {
        return this.ultra;
    }

    public String get_4k() {
        return _4k;
    }

    public void set_4k(String _4k) {
        this._4k = _4k;
    }

    public String getBestUrl() {
        if (!TextUtils.isEmpty(_4k)) {
            return _4k;
        }
        if (!TextUtils.isEmpty(blueray)) {
            return blueray;
        }
        if (!TextUtils.isEmpty(ultra)) {
            return ultra;
        }
        if (!TextUtils.isEmpty(high)) {
            return high;
        }
        if (!TextUtils.isEmpty(medium)) {
            return medium;
        }
        if (!TextUtils.isEmpty(normal)) {
            return normal;
        }
        if (!TextUtils.isEmpty(low)) {
            return low;
        }
        return adaptive;
    }

    //detail Not found
    private String detail;

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }
}
