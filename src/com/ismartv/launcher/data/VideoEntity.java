package com.ismartv.launcher.data;

import java.util.List;

/**
 * Created by <huaijiefeng@gmail.com> on 9/2/14.
 */
public class VideoEntity {
    private String count;
    private List<Object> objects;
    private String num_pages;

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public List<Object> getObjects() {
        return objects;
    }

    public void setObjects(List<Object> objects) {
        this.objects = objects;
    }

    public String getNum_pages() {
        return num_pages;
    }

    public void setNum_pages(String num_pages) {
        this.num_pages = num_pages;
    }

    public static class Object {
        private String image;
        private String title;
        private String item_url;
        private boolean is_complex;
        public boolean isIs_complex() {
			return is_complex;
		}

		public void setIs_complex(boolean is_complex) {
			this.is_complex = is_complex;
		}

		public String getItem_url() {
            return item_url;
        }

        public void setItem_url(String item_url) {
            this.item_url = item_url;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }
}
