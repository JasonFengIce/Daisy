package tv.ismar.daisy.models;

import java.io.Serializable;


public class Item implements Serializable {
    
	private static final long serialVersionUID = 5414782976396856671L;
	
	public String adlet_url;
    public Attribute attributes;
    public String caption;
    public Clip clip;
    public String content_model;
    public int counting_count;
    public String description;
    public int episode;
    public String focus;
    public boolean is_complex;
    public int pk;
    public String poster_url;
    public String publish_date;
    public int quality;
    public float rating_average;
    public int rating_count;
    public Subitem[] subitems;
    public String[] tags;
    public String thumb_url;
    public String title;
    public Expense expense;
    public Clip preview;
    public int spinoff_pk;
    public boolean is_3d;
    public String logo;
    public String logo_3d;
    public String vendor;
    public Point[] points;
    public int rated;
    //These field below may be none, when get from non "media-detail" api.
    public String model_name;
    public int item_pk;
    public String item_url;
}
