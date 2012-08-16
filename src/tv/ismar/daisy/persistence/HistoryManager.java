package tv.ismar.daisy.persistence;

import java.util.ArrayList;

import tv.ismar.daisy.models.History;

/**
 * An History Manager.
 * @author bob
 *
 */
public interface HistoryManager {
	/**
	 * Add a history. Called by player when user exits player.Note that url may be an field of {@link Item}.
	 * Both {@link Item.item_url} and {@link Item.url} is OK. This depends on the category of you {@link Item} object. Commonly, an subitem contains url field,
	 * but a item contains item_url field.
	 * This method guarantees if target url exists in history. it will auto update its status instead of adding a duplicated one.
	 * @param title  the title of current playing item.
	 * @param url  the url representing the item.
	 * @param currentPosition  the played position when user exits player. if playback is finished. pass 0 instead of the played position.
	 */
	public void addHistory(String title, String url, long currentPosition);
	
	/**
	 * Get a history object according given url.
	 * @param url, the url which can get an item contain this.
	 * @return a {@link History} object.
	 */
	public History getHistoryByUrl(String url);
	
	/**
	 * Get all histories.
	 * @return an ArrayList of History.
	 */
	public ArrayList<History> getAllHistories();
}
