package tv.ismar.daisy.core._enum;

/**
 * Created by huaijie on 6/9/15.
 */
public enum ChannelEnum {
    chinesemovie(""),
    TAG_CHINESEMOVIE("chinesemovie"),
    TAG_COMIC("comic"),
    TAG_OVERSEAS("overseas"),
    TAG_TELEPLAY("teleplay"),
    TAG_VARIETY("variety"),
    TAG_SPORT("sport");

    private String channelName;

    ChannelEnum(String channelName) {
        this.channelName = channelName;
    }

    public String getChannelName() {
        return channelName;
    }
}

