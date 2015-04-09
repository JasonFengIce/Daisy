package tv.ismar.daisy;

import tv.ismar.daisy.player.ISTVVodMenu;
import android.app.Activity;

public abstract class VodMenuAction extends Activity{
	static final int MSG_SEK_ACTION = 103;
	public abstract boolean onVodMenuClicked(ISTVVodMenu menu, int id);
	public abstract void onVodMenuClosed(ISTVVodMenu menu);
}
