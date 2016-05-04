package tv.ismar.daisy.utils;

import android.content.Context;
import android.text.TextUtils;
import android.widget.ImageView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import retrofit2.http.Url;
import tv.ismar.daisy.R;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by huaijie on 11/2/15.
 */
public class PicassoUtils {
    public static void load(final Context context, String path, final ImageView target) {
        if (TextUtils.isEmpty(path)) {
            Picasso.with(context).load(R.drawable.default_recommend_bg).memoryPolicy(MemoryPolicy.NO_STORE).into(target);
        } else {
            Picasso.with(context).load(path).error(R.drawable.default_recommend_bg).memoryPolicy(MemoryPolicy.NO_STORE).into(target);
        }

    }

    public static boolean isValidImg(String path){
        try {
            URL url=new URL(path);
            HttpURLConnection conn= (HttpURLConnection) url.openConnection();
            conn.getInputStream();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
