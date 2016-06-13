package tv.ismar.daisy.data.http;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ItemEntity {

    private Clip clip;
    private String focus;
    @SerializedName("subitem_show")
    private String subitemShow;
    @SerializedName("is_3d")
    private boolean is3d;
    @SerializedName("content_model")
    private String contentModel;
    private String logo;
    @SerializedName("detail_url")
    private String detailUrl;
    private int quality;
    @SerializedName("rating_count")
    private int ratingCount;
    private String source;
    private String vendor;
    @SerializedName("adlet_url")
    private String adletUrl;
    @SerializedName("bean_score")
    private String beanScore;
    @SerializedName("poster_url")
    private String posterUrl;
    private int pk;
    @SerializedName("vertical_url")
    private String verticalUrl;
    private String description;
    private List<String> tags;
    @SerializedName("rating_average")
    private String ratingAverage;
    private SubItem[] subitems;
    private boolean finished;
    @SerializedName("live_video")
    private boolean liveVideo;
    @SerializedName("thumb_url")
    private String thumbUrl;
    @SerializedName("counting_count")
    private int countingCount;
    @SerializedName("logo_3d")
    private String logo3d;
    private int episode;
    private String title;
    private String caption;
    private List<Point> points;
    @SerializedName("publish_date")
    private String publishDate;
    @SerializedName("is_complex")
    private boolean isComplex;
    private Attributes attributes;
    @SerializedName("item_pk")
    private int itemPk;
    private String list_url;
    private String item_url;

    public String getItem_url() {
        return item_url;
    }

    public void setItem_url(String item_url) {
        this.item_url = item_url;
    }

    public String getList_url() {
        return list_url;
    }

    public void setList_url(String list_url) {
        this.list_url = list_url;
    }

    public void setClip(Clip clip) {
        this.clip = clip;
    }

    public Clip getClip() {
        return clip;
    }

    public void setFocus(String focus) {
        this.focus = focus;
    }

    public String getFocus() {
        return focus;
    }

    public void setSubitemShow(String subitemShow) {
        this.subitemShow = subitemShow;
    }

    public String getSubitemShow() {
        return subitemShow;
    }

    public void setIs3d(boolean is3d) {
        this.is3d = is3d;
    }

    public boolean getIs3d() {
        return is3d;
    }

    public void setContentModel(String contentModel) {
        this.contentModel = contentModel;
    }

    public String getContentModel() {
        return contentModel;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getLogo() {
        return logo;
    }

    public void setDetailUrl(String detailUrl) {
        this.detailUrl = detailUrl;
    }

    public String getDetailUrl() {
        return detailUrl;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public int getQuality() {
        return quality;
    }

    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSource() {
        return source;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getVendor() {
        return vendor;
    }

    public void setAdletUrl(String adletUrl) {
        this.adletUrl = adletUrl;
    }

    public String getAdletUrl() {
        return adletUrl;
    }

    public void setBeanScore(String beanScore) {
        this.beanScore = beanScore;
    }

    public String getBeanScore() {
        return beanScore;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPk(int pk) {
        this.pk = pk;
    }

    public int getPk() {
        return pk;
    }

    public void setVerticalUrl(String verticalUrl) {
        this.verticalUrl = verticalUrl;
    }

    public String getVerticalUrl() {
        return verticalUrl;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setRatingAverage(String ratingAverage) {
        this.ratingAverage = ratingAverage;
    }

    public String getRatingAverage() {
        return ratingAverage;
    }

    public SubItem[] getSubitems() {
        return subitems;
    }

    public void setSubitems(SubItem[] subitems) {
        this.subitems = subitems;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public boolean getFinished() {
        return finished;
    }

    public void setLiveVideo(boolean liveVideo) {
        this.liveVideo = liveVideo;
    }

    public boolean getLiveVideo() {
        return liveVideo;
    }

    public void setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
    }

    public String getThumbUrl() {
        return thumbUrl;
    }

    public void setCountingCount(int countingCount) {
        this.countingCount = countingCount;
    }

    public int getCountingCount() {
        return countingCount;
    }

    public void setLogo3d(String logo3d) {
        this.logo3d = logo3d;
    }

    public String getLogo3d() {
        return logo3d;
    }

    public void setEpisode(int episode) {
        this.episode = episode;
    }

    public int getEpisode() {
        return episode;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getCaption() {
        return caption;
    }

    public List<Point> getPoints() {
        return points;
    }

    public void setPoints(List<Point> points) {
        this.points = points;
    }

    public void setPublishDate(String publishDate) {
        this.publishDate = publishDate;
    }

    public String getPublishDate() {
        return publishDate;
    }

    public void setIsComplex(boolean isComplex) {
        this.isComplex = isComplex;
    }

    public boolean getIsComplex() {
        return isComplex;
    }

    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }

    public Attributes getAttributes() {
        return attributes;
    }

    public void setItemPk(int itemPk) {
        this.itemPk = itemPk;
    }

    public int getItemPk() {
        return itemPk;
    }

    public class Clip {

        private String url;
        private int pk;
        private String length;

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUrl() {
            return url;
        }

        public void setPk(int pk) {
            this.pk = pk;
        }

        public int getPk() {
            return pk;
        }

        public void setLength(String length) {
            this.length = length;
        }

        public String getLength() {
            return length;
        }

    }

    public class Attributes {

        private String[][] director;
        private String[][] genre;
        private String[][] actor;
        @SerializedName("air_date")
        private String airDate;

        public String[][] getDirector() {
            return director;
        }

        public void setDirector(String[][] director) {
            this.director = director;
        }

        public String[][] getGenre() {
            return genre;
        }

        public void setGenre(String[][] genre) {
            this.genre = genre;
        }

        public String[][] getActor() {
            return actor;
        }

        public void setActor(String[][] actor) {
            this.actor = actor;
        }

        public void setAirDate(String airDate) {
            this.airDate = airDate;
        }

        public String getAirDate() {
            return airDate;
        }

    }

    public class SubItem {
        private String subtitle;

        private Clip clip;

        private String air_date;

        private String focus;

        private String month;

        private int pk;

        private String thumb_url;

        private String title;

        private String url;

        private String adlet_url;

        private String publish_date;

        private String poster_url;

        private int position;

        public String getSubtitle() {
            return subtitle;
        }

        public void setSubtitle(String subtitle) {
            this.subtitle = subtitle;
        }

        public Clip getClip() {
            return clip;
        }

        public void setClip(Clip clip) {
            this.clip = clip;
        }

        public String getAir_date() {
            return air_date;
        }

        public void setAir_date(String air_date) {
            this.air_date = air_date;
        }

        public String getFocus() {
            return focus;
        }

        public void setFocus(String focus) {
            this.focus = focus;
        }

        public String getMonth() {
            return month;
        }

        public void setMonth(String month) {
            this.month = month;
        }

        public int getPk() {
            return pk;
        }

        public void setPk(int pk) {
            this.pk = pk;
        }

        public String getThumb_url() {
            return thumb_url;
        }

        public void setThumb_url(String thumb_url) {
            this.thumb_url = thumb_url;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getAdlet_url() {
            return adlet_url;
        }

        public void setAdlet_url(String adlet_url) {
            this.adlet_url = adlet_url;
        }

        public String getPublish_date() {
            return publish_date;
        }

        public void setPublish_date(String publish_date) {
            this.publish_date = publish_date;
        }

        public String getPoster_url() {
            return poster_url;
        }

        public void setPoster_url(String poster_url) {
            this.poster_url = poster_url;
        }

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }
    }

    public class Point {
        private String image;
        private String time;
        private String type;
        private String title;

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }
}
