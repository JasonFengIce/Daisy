package tv.ismar.sakura.data.http;

import java.util.ArrayList;

/**
 * Created by huaijie on 6/3/15.
 */
public class HomePagerEntity {
    private ArrayList<Carousel> carousels;
    private ArrayList<Poster> posters;

    public ArrayList<Carousel> getCarousels() {
        return carousels;
    }

    public void setCarousels(ArrayList<Carousel> carousels) {
        this.carousels = carousels;
    }

    public ArrayList<Poster> getPosters() {
        return posters;
    }

    public void setPosters(ArrayList<Poster> posters) {
        this.posters = posters;
    }

    public class Carousel {
        private String video_url;
        private String title;
        private String introduction;
        private String link_url;
        private String custom_url;
        private int pause_time;
        private String link_type;

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

        public String getIntroduction() {
            return introduction;
        }

        public void setIntroduction(String introduction) {
            this.introduction = introduction;
        }

        public String getLink_url() {
            return link_url;
        }

        public void setLink_url(String link_url) {
            this.link_url = link_url;
        }

        public String getCustom_url() {
            return custom_url;
        }

        public void setCustom_url(String custom_url) {
            this.custom_url = custom_url;
        }

        public int getPause_time() {
            return pause_time;
        }

        public void setPause_time(int pause_time) {
            this.pause_time = pause_time;
        }

        public String getLink_type() {
            return link_type;
        }

        public void setLink_type(String link_type) {
            this.link_type = link_type;
        }
    }


    public class Poster {
        private String vertical_url;
        private String title;
        private String introduction;
        private String link_url;
        private String custom_url;
        private String poster_url;
        private String link_type;

        public String getVertical_url() {
            return vertical_url;
        }

        public void setVertical_url(String vertical_url) {
            this.vertical_url = vertical_url;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getIntroduction() {
            return introduction;
        }

        public void setIntroduction(String introduction) {
            this.introduction = introduction;
        }

        public String getLink_url() {
            return link_url;
        }

        public void setLink_url(String link_url) {
            this.link_url = link_url;
        }

        public String getCustom_url() {
            return custom_url;
        }

        public void setCustom_url(String custom_url) {
            this.custom_url = custom_url;
        }

        public String getPoster_url() {
            return poster_url;
        }

        public void setPoster_url(String poster_url) {
            this.poster_url = poster_url;
        }

        public String getLink_type() {
            return link_type;
        }

        public void setLink_type(String link_type) {
            this.link_type = link_type;
        }
    }

}
