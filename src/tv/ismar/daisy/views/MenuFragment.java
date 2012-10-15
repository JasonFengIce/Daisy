package tv.ismar.daisy.views;

import java.io.IOException;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParserException;

import tv.ismar.daisy.R;
import android.app.Fragment;
import android.content.Context;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MenuFragment extends Fragment implements OnItemClickListener {
	
	public ArrayList<MenuItem> mMenuList;
	
	private ListView mMenuListView;
	
	private OnMenuItemClickedListener mOnMenuItemClickedListener;
	
	public void setOnMenuItemClickedListener(OnMenuItemClickedListener listener) {
		mOnMenuItemClickedListener = listener;
	}
	
	public interface OnMenuItemClickedListener {
		public void onMenuItemClicked(MenuItem item);
	}
	
	public static MenuFragment newInstance(int layout) {
		MenuFragment fragment = new MenuFragment();
		Bundle args = new Bundle();
		args.putInt("layout", layout);
		fragment.setArguments(args);
		return fragment;
	}

	public interface OnMenuItemClickListener {
		public void onMenuItemClick();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mMenuList = new ArrayList<MenuFragment.MenuItem>();
		MenuItem deleteItem = new MenuItem();
		deleteItem.id = 1;
		deleteItem.isEnable = true;
		deleteItem.title = getResources().getString(R.string.delete_history);
		mMenuList.add(deleteItem);
		MenuItem clearItem = new MenuItem();
		clearItem.id = 2;
		clearItem.isEnable = true;
		clearItem.title = getResources().getString(R.string.clear_history);
		mMenuList.add(clearItem);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Bundle args = getArguments();
		View menuFragment = null;
		if(args!=null) {
			int layoutResId = args.getInt("layout");
			menuFragment = inflater.inflate(layoutResId, container, false);
		} else {
			menuFragment =  inflater.inflate(R.layout.menu_layout, container, false);
		}
		mMenuListView = (ListView) menuFragment.findViewById(R.id.menu_list);
		initLayout();
		return menuFragment;
	}
	
	
	private void initLayout() {
		MenuAdapter adapter = new MenuAdapter(getActivity(), mMenuList);
		mMenuListView.setAdapter(adapter);
	}

	public void inflate(int resId, ArrayList<MenuItem> menuList) {
		XmlResourceParser parser = null;
        try {
            parser = getResources().getLayout(resId);
            AttributeSet attrs = Xml.asAttributeSet(parser);
            
            parseMenu(parser, attrs, menuList);
        } catch (XmlPullParserException e) {
            throw new InflateException("Error inflating menu XML", e);
        } catch (IOException e) {
            throw new InflateException("Error inflating menu XML", e);
        } finally {
            if (parser != null) parser.close();
        }
	}

	private void parseMenu(XmlResourceParser parser, AttributeSet attrs,
			ArrayList<MenuItem> menuList) throws XmlPullParserException, IOException {
		// TODO Auto-generated method stub
		
	}

	public static class MenuItem {
		public int id;
		public boolean isEnable;
		public String title;
		public ArrayList<MenuItem> subMenu;
		public int parentId;
	}
	
	public static class MenuAdapter extends BaseAdapter {

		private ArrayList<MenuItem> mMenuList;
		private Context mContext;
		
		public MenuAdapter(Context context, ArrayList<MenuItem> list) {
			mContext = context;
			mMenuList = list;
		}
		@Override
		public int getCount() {
			return mMenuList.size();
		}

		@Override
		public MenuItem getItem(int position) {
			return mMenuList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return mMenuList.get(position).id;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.menu_layout_item, null);
			TextView title = (TextView)convertView.findViewById(R.id.menu_title);
			title.setText(mMenuList.get(position).title);
			return convertView;
		}
		
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if(mOnMenuItemClickedListener!=null) {
			mOnMenuItemClickedListener.onMenuItemClicked((MenuItem) mMenuListView.getAdapter().getItem(position));
		}
		
	}
	
}
