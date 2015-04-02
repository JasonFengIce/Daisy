package tv.ismar.daisy.core.client;

import android.content.Context;
import android.os.AsyncTask;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.utils.DeviceUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by huaijie on 2015/4/1.
 */

// params, progress, result
public class GuideVideoCacheTask extends AsyncTask<String, String, String> {
    private static final String TAG = "GuideVideoCacheTask";
    private static final int CONNECT_TIME_OUT = 5000;

    private Context context;

    public GuideVideoCacheTask(Context context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(String... strings) {
        String remoteUrl = strings[0];
        try {

            URL url = new URL(remoteUrl + "&device_token=" + SimpleRestClient.device_token);
            String[] array = url.getPath().split("/");
            String cacheFileName = array[array.length - 1];
            File cacheFile = new File(DeviceUtils.getCacheDirectory(context), cacheFileName);
            if (!cacheFile.exists()) {
                if (cacheFile.getParentFile().exists()) {
                    for (File file : cacheFile.getParentFile().listFiles()) {
                        file.delete();
                    }
                }
                cacheFile.createNewFile();
            }

            long cacheSize = cacheFile.length();
            URLConnection urlConnection = url.openConnection();
            urlConnection.setConnectTimeout(CONNECT_TIME_OUT);
//
//            FileOutputStream fileOutputStream = new FileOutputStream(cacheFile, true);
            urlConnection.setRequestProperty("User-Agent", "NetFox");
            urlConnection.setRequestProperty("RANGE", "bytes=" + cacheSize + "-");
            InputStream inputStream = urlConnection.getInputStream();


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return null;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }


    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    @Override
    protected void onCancelled(String s) {
        super.onCancelled(s);
    }

}
