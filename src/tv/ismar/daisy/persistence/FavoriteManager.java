package tv.ismar.daisy.persistence;

import java.util.ArrayList;

import tv.ismar.daisy.models.Favorite;

public interface FavoriteManager {
	/**
	 * Add a favorite object to persistence.
	 * @param favorite ,this object should guaranty all fields not null.
	 */
	public void addFavorite(Favorite favorite);
	@Deprecated
	public void addFavorite(String title, String url, String content_model);
	public Favorite getFavoriteByUrl(String url);
	public ArrayList<Favorite> getAllFavorites();
	public void deleteFavoriteByUrl(String url);
	public void deleteAll();
}
