package tv.ismar.daisy.models.launcher;

/**
 * Created by huaijie on 2015/3/25.
 */
public class AttributeEntity {
    private Attributes attributes;
    private long start_time;
    private long end_time;
    private String model_name;

    public Attributes getAttributes() {
        return attributes;
    }

    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }

    public long getStart_time() {
        return start_time;
    }

    public void setStart_time(long start_time) {
        this.start_time = start_time;
    }

    public long getEnd_time() {
        return end_time;
    }

    public void setEnd_time(long end_time) {
        this.end_time = end_time;
    }

    public String getModel_name() {
        return model_name;
    }

    public void setModel_name(String model_name) {
        this.model_name = model_name;
    }

    public class Attributes {
        private String title;
        private String url;
        private boolean is_complex;
        private String adlet_url;
        private String poster_url;
        private long pk;
        private String model_name;

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

        public boolean isIs_complex() {
            return is_complex;
        }

        public void setIs_complex(boolean is_complex) {
            this.is_complex = is_complex;
        }

        public String getAdlet_url() {
            return adlet_url;
        }

        public void setAdlet_url(String adlet_url) {
            this.adlet_url = adlet_url;
        }

        public String getPoster_url() {
            return poster_url;
        }

        public void setPoster_url(String poster_url) {
            this.poster_url = poster_url;
        }

        public long getPk() {
            return pk;
        }

        public void setPk(long pk) {
            this.pk = pk;
        }

        public String getModel_name() {
            return model_name;
        }

        public void setModel_name(String model_name) {
            this.model_name = model_name;
        }
    }

}
