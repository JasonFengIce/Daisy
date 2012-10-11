package tv.ismar.daisy.views;

import java.io.IOException;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParserException;

import tv.ismar.daisy.R;
import android.app.Fragment;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MenuFragment extends Fragment{
	
	public ArrayList<MenuItem> mMenuList;
	
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
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Bundle args = getArguments();
		if(args!=null) {
			int layoutResId = args.getInt("layout");
			return inflater.inflate(layoutResId, container, false);
		}
		return inflater.inflate(R.layout.menu_layout, container, false);
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
	
}
