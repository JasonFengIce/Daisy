package tv.ismar.daisy.persistence;

import tv.ismar.daisy.models.History;
import tv.ismar.daisy.models.Item;
import tv.ismar.daisy.models.Quality;

import java.util.ArrayList;

/**
 * An History Manager.
 * @author bob
 *
 */
public interface HistoryManager {
	
	/**
	 * Add a history.
	 * @param history
	 */
	public void addHistory(History history,String isnet);
	/**
	 * Add a history. Called by player when user exits player.Note that url may be an field of {@link Item}.
	 * Both {@link Item.item_url} and {@link Item.url} is OK. This depends on the category of you {@link Item} object. Commonly, an subitem contains url field,
	 * but a item contains item_url field.
	 * This method guarantees if target url exists in history. it will auto update its status instead of adding a duplicated one.
	 * @param title  the title of current playing item.
	 * @param url  the url representing the item.
	 * @param currentPosition  the played position when user exits player. if playback is finished. pass 0 instead of the played position.
	 */
	@Deprecated
	public void addHistory(String title, String url, long currentPosition,String isnet);
	
	/**
	 * Get a history object according given url.
	 * @param url, the url which can get an item contain this.
	 * @return a {@link History} object.
	 */
	public History getHistoryByUrl(String url,String isnet);
	
	/**
	 * Get all histories.
	 * @return an ArrayList of History.
	 */
	public ArrayList<History> getAllHistories(String isnet);
	
	/**
	 * Delete a history entry in database by url
	 * @param url
	 */
	public void deleteHistory(String url,String isnet);
	/**
	 * Delete all histories in database
	 */
	public void deleteAll(String isnet);
	
	
	/**
	 * Add or update only one Quality
	 * 
	 */
	public void addOrUpdateQuality(Quality quality);
	
	/**
	 * get user Quality
	 * 
	 */
	public Quality getQuality();
	
}