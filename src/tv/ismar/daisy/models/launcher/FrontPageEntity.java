package tv.ismar.daisy.models.launcher;

import java.util.List;

/**
 * Created by <huaijiefeng@gmail.com> on 9/2/14.
 */
public class FrontPageEntity {
    private String effective_date;
    private List<ImageBean> images;
    private String switch_time;
    private List<VideoBean> videos;
    private String title;

    public String getEffective_date() {
        return effective_date;
    }

    public void setEffective_date(String effective_date) {
        this.effective_date = effective_date;
    }

    public List<ImageBean> getImages() {
        return images;
    }

    public void setImages(List<ImageBean> images) {
        this.images = images;
    }

    public String getSwitch_time() {
        return switch_time;
    }

    public void setSwitch_time(String switch_time) {
        this.switch_time = switch_time;
    }

    public List<VideoBean> getVideos() {
        return videos;
    }

    public void setVideos(List<VideoBean> videos) {
        this.videos = videos;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public static class ImageBean {
        private String channel_title;
        private String title;
        private String url;
        private String content_model;
        private String template;
        private String image_url;
        private String channel_url;
        private String model_name;
        private String channel;

        public String getChannel_title() {
            return channel_title;
        }

        public void setChannel_title(String channel_title) {
            this.channel_title = channel_title;
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

        public String getContent_model() {
            return content_model;
        }

        public void setContent_model(String content_model) {
            this.content_model = content_model;
        }

        public String getTemplate() {
            return template;
        }

        public void setTemplate(String template) {
            this.template = template;
        }

        public String getImage_url() {
            return image_url;
        }

        public void setImage_url(String image_url) {
            this.image_url = image_url;
        }

        public String getChannel_url() {
            return channel_url;
        }

        public void setChannel_url(String channel_url) {
            this.channel_url = channel_url;
        }

        public String getModel_name() {
            return model_name;
        }

        public void setModel_name(String model_name) {
            this.model_name = model_name;
        }

        public String getChannel() {
            return channel;
        }

        public void setChannel(String channel) {
            this.channel = channel;
        }
    }

    public static class VideoBean {
        private String channel_title;
        private String video_url;
        private String title;
        private String url;
        private String video_id;
        private String content_model;
        private String template;
        private String channel_url;
        private String model_name;
        private String channel;

        public String getChannel_title() {
            return channel_title;
        }

        public void setChannel_title(String channel_title) {
            this.channel_title = channel_title;
        }

        public String getVideo_url() {
            return video_url;
        }

        public void setVideo_url(String video_url) {
            this.video_url = video_url;
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

        public String getVideo_id() {
            return video_id;
        }

        public void setVideo_id(String video_id) {
            this.video_id = video_id;
        }

        public String getContent_model() {
            return content_model;
        }

        public void setContent_model(String content_model) {
            this.content_model = content_model;
        }

        public String getTemplate() {
            return template;
        }

        public void setTemplate(String template) {
            this.template = template;
        }

        public String getChannel_url() {
            return channel_url;
        }

        public void setChannel_url(String channel_url) {
            this.channel_url = channel_url;
        }

        public String getModel_name() {
            return model_name;
        }

        public void setModel_name(String model_name) {
            this.model_name = model_name;
        }

        public String getChannel() {
            return channel;
        }

        public void setChannel(String channel) {
            this.channel = channel;
        }
    }
}
