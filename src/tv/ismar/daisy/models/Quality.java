package tv.ismar.daisy.models;

import android.database.Cursor;
import tv.ismar.daisy.dao.DBHelper.DBFields;

import java.io.Serializable;

public class Quality implements Serializable {
	

	private static final long serialVersionUID = -4711625124872482152L;
	public long id;
	public String url;
	public int quality;
	
	public Quality() {
		super();
	}
	
	public Quality(long id, String url,int quality) {
		super();
		this.id = id;
		this.url = url;
		this.quality = quality;
		
	}
	public Quality(Cursor c) {
		id = c.getLong(c.getColumnIndex(DBFields.QualityTable._ID));
		url = c.getString(c.getColumnIndex(DBFields.QualityTable.URL));
		quality = c.getInt(c.getColumnIndex(DBFields.QualityTable.QUALITY));
	}
	
}
