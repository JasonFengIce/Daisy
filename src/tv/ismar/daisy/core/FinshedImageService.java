package tv.ismar.daisy.core;

import android.content.Context;
import tv.ismar.daisy.R;
import tv.ismar.daisy.dao.DBHelper;
import tv.ismar.daisy.models.Item;

public class FinshedImageService {

	public static Object[] getImage(Item item,Context context) {
		Object[] object = new Object[2];
		int resourceLabel = 0;
		int H = DaisyUtils.getVodApplication(context).getheightPixels(context);
		if(H==720)
			object[0] = ImageUtils.getBitmapFromInputStream(NetworkUtils.getInputStream(item.adlet_url), (int)(188/DBHelper.rate), (int)(106/DBHelper.rate));
		else
			object[0] = ImageUtils.getBitmapFromInputStream(NetworkUtils.getInputStream(item.adlet_url), (int)(282/DBHelper.rate), (int)(158/DBHelper.rate));
//		object[0] = (BitmapFactory.decodeStream(HttpUtil.getHttpConnectionByGet(item.adlet_url).getInputStream()));
		switch (item.quality) {
		case 3:
			resourceLabel = R.drawable.label_uhd;
			break;
		case 4:
			resourceLabel = R.drawable.label_hd;
			break;
		default:
			resourceLabel = 0;
			break;
		}
		object[1] = resourceLabel;

		return object;
	}
}
