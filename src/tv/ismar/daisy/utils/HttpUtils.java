package tv.ismar.daisy.utils;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by huaijie on 7/31/15.
 */
public class HttpUtils {

    public static void getFileByUrl(String httpUrl){
        try {
            URL url = new URL(httpUrl);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
