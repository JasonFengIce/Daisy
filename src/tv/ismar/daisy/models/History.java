package tv.ismar.daisy.models;

import java.io.Serializable;

import tv.ismar.daisy.dao.DBHelper.DBFields;
import android.database.Cursor;

public class History implements Serializable {
	
	private static final long serialVersionUID = -921551037735535764L;
	
	public long id;
	public String title;
	public String url;
	public long last_played_time;
	public long last_position;
	
	public History() {
		super();
	}
	
	public History(int id, String title, String url, long last_played_time,
			long last_position) {
		super();
		this.id = id;
		this.title = title;
		this.url = url;
		this.last_played_time = last_played_time;
		this.last_position = last_position;
	}
	
	public History(Cursor c) {
		id = c.getLong(c.getColumnIndex(DBFields.HistroyTable._ID));
		title = c.getString(c.getColumnIndex(DBFields.HistroyTable.TITLE));
		url = c.getString(c.getColumnIndex(DBFields.HistroyTable.URL));
		last_played_time = c.getLong(c.getColumnIndex(DBFields.HistroyTable.LAST_PLAY_TIME));
		last_position = c.getLong(c.getColumnIndex(DBFields.HistroyTable.LAST_POSITION));
	}
}
