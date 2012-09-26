package tv.ismar.daisy.views;

import android.app.Fragment;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;

public class MenuFragment extends Fragment implements Menu {

	public interface OnMenuItemClickListener {
		public void onMenuItemClick();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public MenuItem add(CharSequence title) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MenuItem add(int titleRes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MenuItem add(int groupId, int itemId, int order, CharSequence title) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MenuItem add(int groupId, int itemId, int order, int titleRes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SubMenu addSubMenu(CharSequence title) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SubMenu addSubMenu(int titleRes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SubMenu addSubMenu(int groupId, int itemId, int order,
			CharSequence title) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SubMenu addSubMenu(int groupId, int itemId, int order, int titleRes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int addIntentOptions(int groupId, int itemId, int order,
			ComponentName caller, Intent[] specifics, Intent intent, int flags,
			MenuItem[] outSpecificItems) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void removeItem(int id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeGroup(int groupId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setGroupCheckable(int group, boolean checkable,
			boolean exclusive) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setGroupVisible(int group, boolean visible) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setGroupEnabled(int group, boolean enabled) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean hasVisibleItems() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public MenuItem findItem(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public MenuItem getItem(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean performShortcut(int keyCode, KeyEvent event, int flags) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isShortcutKey(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean performIdentifierAction(int id, int flags) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setQwertyMode(boolean isQwerty) {
		// TODO Auto-generated method stub
		
	}

	
}
