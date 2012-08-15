package tv.ismar.daisy.models;

import java.io.Serializable;

import tv.ismar.daisy.dao.DBHelper.DBFields;
import android.database.Cursor;

public class Favorite implements Serializable {
	
	private static final long serialVersionUID = -3481459533007774584L;
	
	public long id;
	public String title;
	public String url;
	public String content_model;
	
	public Favorite() {
		super();
	}
	
	public Favorite(long id, String title, String url, String content_model) {
		super();
		this.id = id;
		this.title = title;
		this.url = url;
		this.content_model = content_model;
	}
	
	public Favorite(Cursor c) {
		id = c.getLong(c.getColumnIndex(DBFields.FavoriteTable._ID));
		title = c.getString(c.getColumnIndex(DBFields.FavoriteTable.TITLE));
		url = c.getString(c.getColumnIndex(DBFields.FavoriteTable.URL));
		content_model = c.getString(c.getColumnIndex(DBFields.FavoriteTable.CONTENT_MODEL));
	}
	
}
