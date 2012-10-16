package tv.ismar.daisy.persistence;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.dao.DBHelper;
import tv.ismar.daisy.dao.DBHelper.DBFields;
import tv.ismar.daisy.models.History;

public class LocalHistoryManager implements HistoryManager {
	
	/**
	 * store history records.
	 */
	private ArrayList<History> mHistories;
	
	private DBHelper mDBHelper;
	
	public LocalHistoryManager(Context context) {
		mDBHelper = DaisyUtils.getDBHelper(context);
		mHistories = mDBHelper.getAllHistories();
	}
	
	private int mTotalEntriesLimit = 50;

	@Override
	public void addHistory(String title, String url, long currentPosition) {
		if(url==null || title==null) {
			throw new RuntimeException("url or title can not be null");
		}
		
		long currentTimeMillis = System.currentTimeMillis();
		if(mHistories != null && mHistories.size()>0) {
			for(History history: mHistories) {
				if(url.equals(history.url) && history.id!=0) {
					history.last_position = currentPosition;
					history.last_played_time = currentTimeMillis;
					mDBHelper.updateHistory(history);
					break;
				}
			}
		} else {
			ContentValues cv = new ContentValues();
			cv.put(DBFields.HistroyTable.TITLE, title);
			cv.put(DBFields.HistroyTable.URL, url);
			cv.put(DBFields.HistroyTable.LAST_PLAY_TIME, currentTimeMillis);
			cv.put(DBFields.HistroyTable.LAST_POSITION, currentPosition);
			mDBHelper.insert(cv, DBFields.HistroyTable.TABLE_NAME, mTotalEntriesLimit);
			mHistories = mDBHelper.getAllHistories();
		}
	}

	@Override
	public History getHistoryByUrl(String url) {
		if(url==null){
			throw new RuntimeException("url cannot be null");
		}
		History history = null;
		if(mHistories == null) {
			mHistories = new ArrayList<History>();
		} else {
			for(History h: mHistories) {
				if(url.equals(h.url)) {
					history = h;
					break;
				}
			}
		}
		if(history == null) {
			history = mDBHelper.queryHistoryByUrl(url);
			if(history!=null) {
				mHistories.add(history);
			}
		}
		return history;
	}

	@Override
	public ArrayList<History> getAllHistories() {
		if(mHistories==null) {
			mHistories = new ArrayList<History>();
		}
		return mHistories;
	}

	@Override
	public void addFavorite(History history) {
		if(history==null || history.title==null || history.content_model==null || history.url==null) {
			throw new RuntimeException("history or history's field should not be null");
		}
		
		long currentTimeMillis = System.currentTimeMillis();
		History h = getHistoryByUrl(history.url);
		if(h!=null) {
			h.last_position = history.last_position;
			h.last_played_time = currentTimeMillis;
			h.title = history.title;
			h.adlet_url = history.adlet_url;
			h.content_model = history.content_model;
			h.quality = history.quality;
			h.last_quality = history.last_quality;
			h.is_complex = history.is_complex;
			h.is_continue = history.is_continue;
			h.sub_url = history.sub_url;
			mDBHelper.updateHistory(h);
		} else {
			ContentValues cv = new ContentValues();
			cv.put(DBFields.HistroyTable.TITLE, history.title);
			cv.put(DBFields.HistroyTable.URL, history.url);
			cv.put(DBFields.HistroyTable.LAST_PLAY_TIME, currentTimeMillis);
			cv.put(DBFields.HistroyTable.LAST_POSITION, history.last_position);
			cv.put(DBFields.HistroyTable.ADLET_URL, history.adlet_url);
			cv.put(DBFields.HistroyTable.CONTENT_MODEL, history.content_model);
			cv.put(DBFields.HistroyTable.QUALITY, history.quality);
			cv.put(DBFields.HistroyTable.LAST_QUALITY, history.last_quality);
			cv.put(DBFields.HistroyTable.IS_COMPLEX, history.is_complex?1:0);
			cv.put(DBFields.HistroyTable.IS_CONTINUE, history.is_continue?1:0);
			cv.put(DBFields.HistroyTable.SUB_URL, history.sub_url);
			mDBHelper.insert(cv, DBFields.HistroyTable.TABLE_NAME, mTotalEntriesLimit);
			mHistories = mDBHelper.getAllHistories();
		}
	}

	@Override
	public void deleteHistory(String url) {
		if(url==null) {
			throw new RuntimeException("url should not be null");
		}
		int rowsAffected = mDBHelper.delete(DBFields.HistroyTable.TABLE_NAME, url);
		Log.d("LocalHistoryManager", rowsAffected + "records delete");
		mHistories = mDBHelper.getAllHistories();
	}

	@Override
	public void deleteAll() {
		mDBHelper.delete(DBFields.HistroyTable.TABLE_NAME, null);
		mHistories.clear();
	}

}
