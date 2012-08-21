package tv.ismar.daisy.persistence;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;

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
			mDBHelper.insert(cv, DBFields.HistroyTable.TABLE_NAME);
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

}
