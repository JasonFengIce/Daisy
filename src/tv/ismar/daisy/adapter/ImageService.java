package tv.ismar.daisy.adapter;

import tv.ismar.daisy.R;
import tv.ismar.daisy.core.ImageUtils;
import tv.ismar.daisy.core.NetworkUtils;
import tv.ismar.daisy.models.Item;

public class ImageService {

	public static Object[] getImage(Item item) {
		Object[] object = new Object[2];
		int resourceLabel = 0;
		object[0] = ImageUtils.getBitmapFromInputStream(NetworkUtils.getInputStream(item.adlet_url), 282, 158);
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
