package tv.ismar.daisy.core;

import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ImageUtils {
	public static Bitmap getBitmapFromInputStream(InputStream in, int width, int height) {
		if(in!=null){
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.outWidth = width;
			options.outHeight = height;
			return BitmapFactory.decodeStream(in, null, options);
		} else {
			return null;
		}
	}
}
