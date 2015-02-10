package tv.ismar.daisy.player;

import tv.ismar.daisy.R;
import tv.ismar.daisy.VodMenuAction;
import android.graphics.Color;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ISTVVodMenu extends ISTVVodMenuItem{
	private static final String TAG="ISTVVodMenu";
	private VodMenuAction activity;
	private Animation showAnimation;
	private Animation hideAnimation;
	private ListView view;
	private boolean visible=false;
	private boolean created=false;
	@SuppressWarnings("unused")
	private ISTVVodMenuItem currMenu;
	private ISTVVodMenuItem menuStack[]=new ISTVVodMenuItem[10];
	private int menuStackTop=0;

	public ISTVVodMenu(VodMenuAction act){
		super(-1, "");
		activity = act;

		view = (ListView)activity.findViewById(R.id.MenuListView);
		view.setOnKeyListener(new View.OnKeyListener(){
			public boolean onKey(View v, int keyCode, KeyEvent event){
				if(keyCode==KeyEvent.KEYCODE_BACK){
					pop();
					return true;
				}else if(keyCode==KeyEvent.KEYCODE_DPAD_RIGHT 
							|| keyCode == KeyEvent.KEYCODE_DPAD_LEFT){
					return true;
				}else if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
					if(view.getSelectedItemPosition() == (view.getCount() - 1)) {
						return true;
					}
				}
				return false;
			}
		});
		showAnimation = AnimationUtils.loadAnimation(activity, R.layout.fly_left);
		hideAnimation = AnimationUtils.loadAnimation(activity, R.layout.fly_right);
	}

	private ISTVVodMenuItem getCurrMenu(){
		if(menuStackTop==0)
			return null;

		return menuStack[menuStackTop-1];
	}

	private void loadMenu(ISTVVodMenuItem menu, boolean push){
		String titles[] = new String[menu.subItems.size()];
		int sel = -1;

		for(int i=0; i<titles.length; i++){
			titles[i] = menu.subItems.get(i).title;
			if(menu.subItems.get(i).selected){
				sel = i;
			}
		}

		view.setAdapter(new ArrayAdapter<String>(activity, R.layout.menu_item,
			R.id.MenuText,
			titles){
				public View getView(int position, View convertView, ViewGroup parent) {
						convertView = super.getView(position, convertView, parent);
						TextView tv = (TextView)convertView.findViewById(R.id.MenuText);
						ISTVVodMenuItem curr = getCurrMenu();
						int id = position;

						if(!curr.subItems.get(id).enabled){
							tv.setTextColor(Color.rgb(0x40, 0x40, 0x40));
						}else{
							tv.setTextColor(Color.rgb(0xff, 0xff, 0xff));
						}
						if(curr.subItems.get(id).selected){
							convertView.setBackgroundColor(Color.argb(50, 0xe5, 0xaa, 0x50));
						}else{
							convertView.setBackgroundColor(Color.argb(50, 0x0, 0x0, 0x0));
						}
						return convertView;
				}
		});

		if(sel!=-1){
			view.setSelection(sel);
		}

		if(push){
			menuStack[menuStackTop++] = menu;
		}
	}

	private void create(){
		if(!created){
			created=true;
			view.setOnItemClickListener(new OnItemClickListener() {
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						Log.d(TAG, "select "+id);
						ISTVVodMenuItem curr = getCurrMenu();

						if(curr!=null){
							ISTVVodMenuItem item = curr.subItems.get(position);
							if(item.enabled){
								if(item.isSub){
									Log.d(TAG, "load "+item.title);
									loadMenu(item, true);
								}else{
									Log.d(TAG, "click "+item.title);
									if(activity.onVodMenuClicked(ISTVVodMenu.this, item.id)){
										hide();
									}
								}
							}
						}
					}
				}
			);
		}
	}

	public void show(){
		if(!visible && (subItems!=null) && (subItems.size()!=0)){
			visible=true;
			menuStackTop=0;
			create();
			loadMenu(this, true);
			view.setVisibility(View.VISIBLE);
			view.startAnimation(showAnimation);
			view.setItemsCanFocus(true);
			view.requestFocus();
		}
	}

	public void pop(){
		if(!visible)
			return;

		if(menuStackTop>0){
			menuStackTop--;

			ISTVVodMenuItem curr = getCurrMenu();

			if(curr!=null){
				loadMenu(curr, false);
			}else{
				hide();
			}
		}
	}

	public void hide(){
		if(visible){
			visible=false;
			view.startAnimation(hideAnimation);
			view.setVisibility(View.GONE);
			activity.onVodMenuClosed(this);
		}
	}

	public boolean isVisible(){
		return visible;
	}

	public void enable_scroll(boolean enable_scroll){
		view.setVerticalScrollBarEnabled(enable_scroll);
	}
}

