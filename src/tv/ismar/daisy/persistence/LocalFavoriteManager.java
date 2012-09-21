package tv.ismar.daisy.persistence;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;

import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.dao.DBHelper;
import tv.ismar.daisy.dao.DBHelper.DBFields;
import tv.ismar.daisy.models.Favorite;

public class LocalFavoriteManager implements FavoriteManager {
	
	private ArrayList<Favorite> mFavorites;
	
	private DBHelper mDBHelper;
	public LocalFavoriteManager(Context context) {
		mDBHelper = DaisyUtils.getDBHelper(context);
		mFavorites = mDBHelper.getAllFavorites();
	}

	@Override
	public void addFavorite(String title, String url, String content_model) {
		if(url==null || title==null) {
			throw new RuntimeException("title and url cannot be null");
		}
		if(mFavorites != null && mFavorites.size()>0) {
			for(Favorite favorite: mFavorites) {
				if(url.equals(favorite.url) && favorite.id!=0) {
					favorite.title = title;
					favorite.content_model = content_model;
					mDBHelper.updateFavorite(favorite);
				}
			}
		} else {
			ContentValues cv = new ContentValues();
			cv.put(DBFields.FavoriteTable.TITLE, title);
			cv.put(DBFields.FavoriteTable.URL, url);
			cv.put(DBFields.FavoriteTable.CONTENT_MODEL, content_model);
			mDBHelper.insert(cv, DBFields.FavoriteTable.TABLE_NAME, 0);
			mFavorites = mDBHelper.getAllFavorites();
		}
	}

	@Override
	public Favorite getFavoriteByUrl(String url) {
		if(url==null) {
			throw new RuntimeException("url cannot be null");
		}
		Favorite favorite = null;
		if(mFavorites==null) {
			mFavorites = new ArrayList<Favorite>();
		} else {
			for(Favorite f: mFavorites) {
				if(url.equals(f.url)) {
					favorite = f;
					break;
				}
			}
		}
		if(favorite==null) {
			favorite = mDBHelper.queryFavoriteByUrl(url);
			if(favorite!=null) {
				mFavorites.add(favorite);
			}
		}
		return favorite;
	}

	@Override
	public ArrayList<Favorite> getAllFavorites() {
		if(mFavorites == null) {
			mFavorites = new ArrayList<Favorite>();
		}
		return mFavorites;
	}

	@Override
	public void deleteFavoriteByUrl(String url) {
		if(url==null) {
			throw new RuntimeException("url cannot be null");
		}
		mDBHelper.delete(DBFields.FavoriteTable.TABLE_NAME, url);
		mFavorites = mDBHelper.getAllFavorites();
	}

	@Override
	public void deleteAll() {
		mDBHelper.delete(DBFields.FavoriteTable.TABLE_NAME, null);
		mFavorites.clear();
	}

	@Override
	public void addFavorite(Favorite favorite) {
		if(favorite==null || favorite.url==null || favorite.title==null || favorite.content_model==null) {
			throw new RuntimeException("favorite or favorite fields cannot be null");
		}
		Favorite f = getFavoriteByUrl(favorite.url);
		if(f!=null) {
			f.title = favorite.title;
			f.content_model = favorite.content_model;
			f.adlet_url = favorite.adlet_url;
			f.quality = favorite.quality;
			f.is_complex = favorite.is_complex;
			mDBHelper.updateFavorite(favorite);
		}  else {
			ContentValues cv = new ContentValues();
			cv.put(DBFields.FavoriteTable.TITLE, favorite.title);
			cv.put(DBFields.FavoriteTable.URL, favorite.url);
			cv.put(DBFields.FavoriteTable.CONTENT_MODEL, favorite.content_model);
			cv.put(DBFields.FavoriteTable.ADLET_URL, favorite.adlet_url);
			cv.put(DBFields.FavoriteTable.QUALITY, favorite.quality);
			cv.put(DBFields.FavoriteTable.IS_COMPLEX, favorite.is_complex?1:0);
			mDBHelper.insert(cv, DBFields.FavoriteTable.TABLE_NAME, 0);
			mFavorites = mDBHelper.getAllFavorites();
		}
	}

}
