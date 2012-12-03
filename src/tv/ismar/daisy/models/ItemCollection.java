package tv.ismar.daisy.models;

import java.io.Serializable;
import java.util.ArrayList;

import android.util.SparseArray;

/**
 * This is a easy to operated model of ItemList.
 * It can remember which page of the section has been filled.
 * Remember NEVER USE {@link ItemCollection#objects} 's size() method
 * to get the total size of the ItemCollection. Use {@link ItemCollection#count} instead.
 * @author bob
 *
 */
public class ItemCollection implements Serializable {
	
	private static final long serialVersionUID = -514170357600555210L;
	
	public static final int NUM_PER_PAGE = 100;
	
	public int count;
	public int num_pages;
	public SparseArray<Item> objects;
	public String slug;
	public String title;
	
	public boolean[] hasFilledValidItem;
	
	public ItemCollection(int num_pages, int count, String slug, String title) {
		this.count = count;
		this.num_pages = num_pages;
		this.hasFilledValidItem = new boolean[num_pages];
		this.objects = new SparseArray<Item>(count);
		this.slug = slug;
		this.title = title;
	}
	
	/**
	 * Check if current index's page is already filled with valid data.
	 * @param index the index of {@link ItemCollection#objects}.
	 * @return true if valid data has been filled. otherwise return false
	 */
	public boolean isItemReady(int index) {
		// page is zero-based. but in api, it should be a nature number.
		int page = index / NUM_PER_PAGE;
		return hasFilledValidItem[page];
	}
	
	public void fillItems(int page, ArrayList<Item> itemList) {
		if(hasFilledValidItem.length < num_pages) {
			hasFilledValidItem = new boolean[num_pages];
		}
		for(int i=0; i < itemList.size(); i++) {
			objects.put(i + page*NUM_PER_PAGE , itemList.get(i));
		}
		hasFilledValidItem[page] = true;
	}
	
}
