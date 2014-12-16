package com.ismartv.launcher.data;

/**
 * Created by <huaijiefeng@gmail.com> on 9/2/14.
 */
public class ChannelEntity {
    private String name;
    private String url;
    private String icon_url;
    private String icon_focus_url;
    private String chargeable;
    private String channel;
    private String template;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getIcon_url() {
        return icon_url;
    }

    public void setIcon_url(String icon_url) {
        this.icon_url = icon_url;
    }

    public String getIcon_focus_url() {
        return icon_focus_url;
    }

    public void setIcon_focus_url(String icon_focus_url) {
        this.icon_focus_url = icon_focus_url;
    }

    public String getChargeable() {
        return chargeable;
    }

    public void setChargeable(String chargeable) {
        this.chargeable = chargeable;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }
}
