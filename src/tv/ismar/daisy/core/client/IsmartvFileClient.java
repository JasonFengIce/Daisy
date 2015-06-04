package tv.ismar.daisy.core.client;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import tv.ismar.daisy.utils.DeviceUtils;
import tv.ismar.sakura.data.http.HomePagerEntity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by huaijie on 5/28/15.
 */
public class IsmartvFileClient extends Thread {
    private static final String TAG = "GuideFragment";

    private Context context;
    private ArrayList<HomePagerEntity.Carousel> carousels;
    private ArrayList<HashMap<String, String>> files;

    private File parentDir;


    public IsmartvFileClient(ArrayList<HomePagerEntity.Carousel> carousels) {
        this.carousels = carousels;
        files = new ArrayList<HashMap<String, String>>();
        for (HomePagerEntity.Carousel carousel : carousels) {
            HashMap<String, String> hashMap = new HashMap<String, String>();
            hashMap.put("url", carousel.getVideo_url());
            try {
                URL url = new URL(carousel.getVideo_url());
                hashMap.put("path", DeviceUtils.getCachePath() + url.getFile());
                files.add(hashMap);
            } catch (MalformedURLException e) {
                Log.e(TAG, e.getMessage());
            }
        }

        parentDir = new File(files.get(0).get("path")).getParentFile();
        Log.i(TAG, "parent dir: " + parentDir.getAbsolutePath());

        for (HashMap<String, String> videoFile : files) {
            Log.i(TAG, "video files: " + videoFile.get("path"));
        }

        for (File file : parentDir.listFiles()) {
            Log.i(TAG, "sub files:" + file.getAbsolutePath());
        }
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        } else {
            File[] subFiles = parentDir.listFiles();
            for (File subfile : subFiles) {
                if (!subfile.getName().equals(new File(files.get(0).get("path")).getName())
                        && !subfile.getName().equals(new File(files.get(1).get("path")).getName())
                        && !subfile.getName().equals(new File(files.get(2).get("path")).getName())) {
                    if (subfile.exists()) {
                        subfile.delete();
                        Log.i(TAG, "subfile delete: " + subfile.getName());
                    }
                }
//                for (HashMap<String, String> videoFile : files) {
//                    String videoName = new File(videoFile.get("path")).getName();
//                    if (!subfile.getName().equals(videoName)) {
//                        return;
//                    }
//                    if (subfile.exists()) {
//                        subfile.delete();
//                        Log.i(TAG, "subfile delete: " + subfile.getName());
//                        Log.i(TAG, "video file: " + videoName);
//                    }
//                }
            }
        }
    }


    @Override
    public void run() {
        Response response;
        OkHttpClient client = new OkHttpClient();
        for (HashMap<String, String> hashMap : files) {
            File file = new File(hashMap.get("path"));
            String url = hashMap.get("url");
            FileOutputStream fileOutputStream;
            try {

                if (file.exists()) {
                    long length = file.length();
                    Log.i(TAG, "file last download:" + length);
                    Request request = new Request.Builder()
                            .url(url)
                            .addHeader("RANGE", "bytes=" + length + "-")
                            .build();
                    response = client.newCall(request).execute();
                    fileOutputStream = new FileOutputStream(file, true);
                } else {
                    Request request = new Request.Builder()
                            .url(url)
                            .build();
                    response = client.newCall(request).execute();
                    fileOutputStream = new FileOutputStream(file, false);
                }
                String headerStr = new Gson().toJson(response.headers());
                Log.i(TAG, "--->\n" +
                        "\turl is: " + "\t" + url + "\n" +
                        "\theaders is: " + "\t" + headerStr + "\n");

                InputStream inputStream = response.body().byteStream();
                byte[] buffer = new byte[1024];
                int byteRead = 0;
                while ((byteRead = inputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, byteRead);
                }
                fileOutputStream.flush();
                fileOutputStream.close();
                inputStream.close();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }

            Log.i(TAG, file.getAbsolutePath() + " download complete!!!");

//        File defaultCacheFile = null;
//
//
//        try {
//            Log.i(TAG, "url is: " + url);
//            URL videoUrl = new URL(url);
//            if (DeviceUtils.getCachePath(context) != null) {
//                defaultCacheFile = new File(DeviceUtils.getCachePath(context), videoUrl.getFile());
//                if (!defaultCacheFile.getParentFile().exists()) {
//                    defaultCacheFile.getParentFile().mkdirs();
//                } else {
//                    defaultCacheFile.delete();
//                    Log.i(TAG, "delete cache file");
//                }
//            }
//
////            if (defaultCacheFile.exists()) {
////                long length = defaultCacheFile.length();
////                Log.i(TAG, "file already download:" + length);
////                Request request = new Request.Builder()
////                        .url(url)
////                        .addHeader("RANGE", "bytes=" + length + "-")
////                        .build();
////                response = client.newCall(request).execute();
////            } else
//            {
//                Request request = new Request.Builder()
//                        .url(url)
//                        .build();
//                response = client.newCall(request).execute();
//            }
//
//            String headerStr = new Gson().toJson(response.headers());
//            Log.i(TAG, "--->\n" +
//                    "\turl is: " + "\t" + url + "\n" +
//                    "\theaders is: " + "\t" + headerStr + "\n");
//
//            InputStream inputStream = response.body().byteStream();
//            Log.i(TAG, "cache path is: " + defaultCacheFile.getAbsolutePath());
//            FileOutputStream fileOutputStream = new FileOutputStream(defaultCacheFile);
//            byte[] buffer = new byte[1024];
//            int byteRead = 0;
//            while ((byteRead = inputStream.read(buffer)) != -1) {
//                fileOutputStream.write(buffer, 0, byteRead);
//            }
//            fileOutputStream.flush();
//            fileOutputStream.close();
//            inputStream.close();
//        } catch (IOException e) {
//            Log.e(TAG, e.getMessage());
//        }
        }
    }
}
