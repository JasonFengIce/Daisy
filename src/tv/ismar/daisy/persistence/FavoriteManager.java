package tv.ismar.daisy.persistence;

import java.util.ArrayList;

import tv.ismar.daisy.models.Favorite;

public interface FavoriteManager {
	public void addFavorite(String title, String url, String content_model);
	public Favorite getFavoriteByUrl(String url);
	public ArrayList<Favorite> getAllFavorites();
	public void deleteFavoriteByUrl(String url);
	public void deleteAll();
}
