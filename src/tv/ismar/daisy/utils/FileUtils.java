package tv.ismar.daisy.utils;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by huaijie on 7/31/15.
 */
public class FileUtils {

    public static String getFileByUrl(String httpUrl) {
        try {
            URL url = new URL(httpUrl);
            String file = url.getFile();
            return file;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return "";
    }
}
