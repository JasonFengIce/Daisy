package tv.ismar.daisy.models;

import java.io.Serializable;


public class Subitem implements Serializable {
	
	private static final long serialVersionUID = 3860669688542975566L;
	
	public String adlet_url;
	public Attribute attriutes;
	public Clip clip;
	public String content_model;
	public int counting_count;
	public String description;
	public int episode;
	public String focus;
	public boolean is_complex;
	public int item_pk;
	public int pk;
	public Point points;
	public int position;
	public String poster_url;
	public String publish_date;
	public int quality;
	public float rating_average;
	public int rating_count;
	public String tags[];
	public String thumb_url;
	public String title;
	public String url;
}
