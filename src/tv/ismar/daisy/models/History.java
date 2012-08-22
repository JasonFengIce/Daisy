package tv.ismar.daisy.models;

import java.io.Serializable;

import tv.ismar.daisy.dao.DBHelper.DBFields;
import android.database.Cursor;

public class History implements Serializable {
	
	private static final long serialVersionUID = -921551037735535764L;
	
	public long id;
	public String title;
	public String url;
	public String adlet_url;
	public String content_model;
	public int quality = 1;
	public int last_quality = 1;
	public boolean is_complex;
	public long last_played_time;
	public long last_position;
	
	public History() {
		super();
	}
	
	
	
	public History(long id, String title, String url, String adlet_url,
			String content_model, int quality, int last_quality,
			boolean is_complex, long last_played_time, long last_position) {
		super();
		this.id = id;
		this.title = title;
		this.url = url;
		this.adlet_url = adlet_url;
		this.content_model = content_model;
		this.quality = quality;
		this.last_quality = last_quality;
		this.is_complex = is_complex;
		this.last_played_time = last_played_time;
		this.last_position = last_position;
	}



	public History(Cursor c) {
		id = c.getLong(c.getColumnIndex(DBFields.HistroyTable._ID));
		title = c.getString(c.getColumnIndex(DBFields.HistroyTable.TITLE));
		url = c.getString(c.getColumnIndex(DBFields.HistroyTable.URL));
		adlet_url = c.getString(c.getColumnIndex(DBFields.HistroyTable.ADLET_URL));
		content_model = c.getString(c.getColumnIndex(DBFields.HistroyTable.CONTENT_MODEL));
		quality = c.getInt(c.getColumnIndex(DBFields.HistroyTable.QUALITY));
		last_quality = c.getInt(c.getColumnIndex(DBFields.HistroyTable.LAST_QUALITY));
		is_complex = c.getInt(c.getColumnIndex(DBFields.HistroyTable.IS_COMPLEX))==0 ? false:true;
		last_played_time = c.getLong(c.getColumnIndex(DBFields.HistroyTable.LAST_PLAY_TIME));
		last_position = c.getLong(c.getColumnIndex(DBFields.HistroyTable.LAST_POSITION));
	}
}
