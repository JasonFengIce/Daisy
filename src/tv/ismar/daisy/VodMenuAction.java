package tv.ismar.daisy;

import tv.ismar.daisy.player.ISTVVodMenu;
import android.app.Activity;

public abstract class VodMenuAction extends Activity{
	public abstract boolean onVodMenuClicked(ISTVVodMenu menu, int id);
	public abstract void onVodMenuClosed(ISTVVodMenu menu);
}
