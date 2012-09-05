package tv.ismar.daisy.dao;

import java.util.ArrayList;

import tv.ismar.daisy.models.Favorite;
import tv.ismar.daisy.models.History;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class DBHelper extends SQLiteOpenHelper {
	
	/**
	 * database version. this may changed with future update.
	 */
	private static final int DATABASE_VERSION = 1;
	
	/**
	 * database file name.
	 */
	private static final String DATABASE_NAME = "daisy.db";
	
	private SQLiteDatabase db;
	
	/**
	 * SQLite command string. use to create history_table and favorite_table.
	 */
	private static final String CREATE_HISTORY_TABLE = "CREATE TABLE IF NOT EXISTS 'history_table' " +
			"('_id' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 'title' TEXT NOT NULL, 'url' TEXT NOT NULL, 'adlet_url' TEXT, " +
			"'last_played_time' INTEGER DEFAULT(0), 'last_position' INTEGER DEFAULT(0), 'content_model' TEXT NOT NULL, 'quality' INTEGER DEFAULT(1), " +
			"'last_quality' INTEGER DEFAULT(1), 'is_complex' INTEGER DEFAULT(0), 'is_continue' INTEGER DEFAULT(0), 'sub_url' TEXT)";
	private static final String CREATE_FAVORITE_TABLE = "CREATE TABLE IF NOT EXISTS 'favorite_table' " +
			"('_id' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 'title' TEXT NOT NULL, 'url' TEXT NOT NULL, 'content_model' TEXT, " +
			"'adlet_url' TEXT, 'quality' INTEGER DEFAULT(1), 'is_complex' INTEGER DEFAULT(0))";
	
	public static interface DBFields {
		/**
		 * Table columns about the history table.
		 */
		public static interface HistroyTable extends BaseColumns {
			public static final String TABLE_NAME = "history_table";
			public static final String TITLE = "title";
			public static final String URL = "url";
			public static final String ADLET_URL = "adlet_url";
			public static final String CONTENT_MODEL = "content_model";
			public static final String QUALITY = "quality";
			public static final String LAST_QUALITY = "last_quality";
			public static final String IS_COMPLEX = "is_complex";
			public static final String IS_CONTINUE = "is_continue";
			public static final String LAST_PLAY_TIME = "last_played_time";
			public static final String LAST_POSITION = "last_position";
			public static final String SUB_URL = "sub_url";
		}
		
		public static interface FavoriteTable extends BaseColumns {
			public static final String TABLE_NAME = "favorite_table";
			public static final String TITLE = "title";
			public static final String URL = "url";
			public static final String ADLET_URL = "adlet_url";
			public static final String IS_COMPLEX = "is_complex";
			public static final String QUALITY = "quality";
			public static final String CONTENT_MODEL = "content_model";
		}
	}
	
	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		db = getWritableDatabase();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_HISTORY_TABLE);
		db.execSQL(CREATE_FAVORITE_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}
	
	/**
	 * Use to query all history records. sorted by last_played_time column.
	 * @return an ArrayList contains all History objects.
	 */
	public ArrayList<History> getAllHistories() {
		ArrayList<History> historyList = new ArrayList<History>();
		Cursor cur = db.query(DBFields.HistroyTable.TABLE_NAME, null, null, null, null, null, " last_played_time desc");
		if(cur!=null) {
			if(cur.moveToFirst()) {
				do {
					historyList.add(new History(cur));
				} while(cur.moveToNext());
			}
			cur.close();
			cur = null;
		}
		return historyList;
	}
	
	/**
	 * Use to query all favorite record.
	 * @return an ArrayList contains all Favorite objects. 
	 */
	public ArrayList<Favorite> getAllFavorites() {
		ArrayList<Favorite> favoriteList = new ArrayList<Favorite>();
		Cursor cur = db.query(DBFields.FavoriteTable.TABLE_NAME, null, null, null, null, null, " _id desc");
		if(cur!=null) {
			if(cur.moveToFirst()) {
				do {
					favoriteList.add(new Favorite(cur));
				} while(cur.moveToNext());
			}
			cur.close();
			cur = null;
		}
		return favoriteList;
	}
	
	/**
	 * Only use to insert a new row to given table. Generally, you do not need to use this method directly.  
	 * @param cv  wrapper data to insert.
	 * @param table the table name which you want to insert to.
	 * @return the row ID of the newly inserted row, or -1 if an error occurred
	 */
	public long insert(ContentValues cv, String table) {
		return db.insert(table, null, cv);
	}
	
	/**
	 * Use to update a record of history_table.
	 * @param history the history object. id must not be zero.
	 */
	public void updateHistory(History history) {
		ContentValues cv = new ContentValues();
		cv.put(DBFields.HistroyTable._ID, history.id);
		cv.put(DBFields.HistroyTable.TITLE, history.title);
		cv.put(DBFields.HistroyTable.URL, history.url);
		cv.put(DBFields.HistroyTable.ADLET_URL, history.adlet_url);
		cv.put(DBFields.HistroyTable.CONTENT_MODEL, history.content_model);
		cv.put(DBFields.HistroyTable.QUALITY, history.quality);
		cv.put(DBFields.HistroyTable.LAST_QUALITY, history.last_quality);
		cv.put(DBFields.HistroyTable.IS_COMPLEX, history.is_complex?1:0);
		cv.put(DBFields.HistroyTable.IS_CONTINUE, history.is_continue?1:0);
		cv.put(DBFields.HistroyTable.LAST_PLAY_TIME, history.last_played_time);
		cv.put(DBFields.HistroyTable.LAST_POSITION, history.last_position);
		cv.put(DBFields.HistroyTable.SUB_URL, history.sub_url);
		db.update(DBFields.HistroyTable.TABLE_NAME, cv, "_id = ?", new String[]{String.valueOf(history.id)});
	}
	
	public void updateFavorite(Favorite favorite) {
		ContentValues cv = new ContentValues();
		cv.put(DBFields.FavoriteTable._ID, favorite.id);
		cv.put(DBFields.FavoriteTable.TITLE, favorite.title);
		cv.put(DBFields.FavoriteTable.URL, favorite.url);
		cv.put(DBFields.FavoriteTable.ADLET_URL, favorite.adlet_url);
		cv.put(DBFields.FavoriteTable.QUALITY, favorite.quality);
		cv.put(DBFields.FavoriteTable.IS_COMPLEX, favorite.is_complex?1:0);
		cv.put(DBFields.FavoriteTable.CONTENT_MODEL, favorite.content_model);
		
		db.update(DBFields.FavoriteTable.TABLE_NAME, cv, " _id = ?", new String[]{String.valueOf(favorite.id)});
	}
	
	public History queryHistoryByUrl(String url) {
		History history = null;
		Cursor cur = db.query(DBFields.HistroyTable.TABLE_NAME, null, DBFields.HistroyTable.URL + " = ?", new String[]{url}, null, null, " _id desc");
		if(cur!=null) {
			if(cur.moveToFirst()) {
				history = new History(cur);
			}
			cur.close();
			cur = null;
		}
		return history;
	}
	
	public Favorite queryFavoriteByUrl(String url) {
		Favorite favorite = null;
		Cursor cur = db.query(DBFields.FavoriteTable.TABLE_NAME, null, DBFields.FavoriteTable.URL + " = ?", new String[]{url}, null, null, " _id desc");
		if(cur!=null) {
			if(cur.moveToFirst()) {
				favorite = new Favorite(cur);
			}
			cur.close();
			cur = null;
		}
		return favorite;
	}
	
	public int delete(String table, String url) {
		if(url==null) {
			return db.delete(table, null, null);
		}
		return db.delete(table, " url = ?", new String[]{url});
	}
	
	public void releaseDB() {
		db.close();
		db = null;
	}
}
