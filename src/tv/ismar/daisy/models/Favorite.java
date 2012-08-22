package tv.ismar.daisy.models;

import java.io.Serializable;

import tv.ismar.daisy.dao.DBHelper.DBFields;
import android.database.Cursor;

public class Favorite implements Serializable {
	
	private static final long serialVersionUID = -3481459533007774584L;
	
	public long id;
	public String title;
	public String url;
	public String adlet_url;
	public int quality;
	public boolean is_complex;
	public String content_model;
	
	public Favorite() {
		super();
	}
	
	public Favorite(long id, String title, String url, String adlet_url,
			int quality, boolean is_complex, String content_model) {
		super();
		this.id = id;
		this.title = title;
		this.url = url;
		this.adlet_url = adlet_url;
		this.quality = quality;
		this.is_complex = is_complex;
		this.content_model = content_model;
	}



	public Favorite(Cursor c) {
		id = c.getLong(c.getColumnIndex(DBFields.FavoriteTable._ID));
		title = c.getString(c.getColumnIndex(DBFields.FavoriteTable.TITLE));
		url = c.getString(c.getColumnIndex(DBFields.FavoriteTable.URL));
		adlet_url = c.getString(c.getColumnIndex(DBFields.FavoriteTable.ADLET_URL));
		quality = c.getInt(c.getColumnIndex(DBFields.FavoriteTable.QUALITY));
		is_complex = c.getInt(c.getColumnIndex(DBFields.FavoriteTable.IS_COMPLEX))==0 ? false: true; 
		content_model = c.getString(c.getColumnIndex(DBFields.FavoriteTable.CONTENT_MODEL));
	}
	
}
