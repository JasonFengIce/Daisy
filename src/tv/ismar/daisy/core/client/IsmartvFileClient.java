package tv.ismar.daisy.core.client;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import tv.ismar.daisy.utils.DeviceUtils;
import tv.ismar.daisy.data.HomePagerEntity;

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

    private ArrayList<HomePagerEntity.Carousel> carousels;
    private ArrayList<HashMap<String, String>> files;

    private File parentDir;

    private Context context;

    public IsmartvFileClient(Context context, ArrayList<HomePagerEntity.Carousel> carousels) {
        this.carousels = carousels;
        this.context = context;
        files = new ArrayList<HashMap<String, String>>();
        for (HomePagerEntity.Carousel carousel : carousels) {
            HashMap<String, String> hashMap = new HashMap<String, String>();

            String downloadUrl = carousel.getVideo_url();
            hashMap.put("url", downloadUrl);

            Log.d(TAG, "download url is: " + downloadUrl);

            try {
                URL url = new URL(downloadUrl);
                String downloadPath = DeviceUtils.getCachePath(context);
                String videoName = url.getFile();
                hashMap.put("path", downloadPath + videoName);
                if (!TextUtils.isEmpty(downloadPath)) {
                    files.add(hashMap);
                }
            } catch (MalformedURLException e) {
                Log.e(TAG, e.getMessage());
            }
        }

        parentDir = new File(files.get(0).get("path")).getParentFile();

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
                    String subFileMD5 = DeviceUtils.getMd5ByFile(file);
                    String fileNameWithoutSuffix = DeviceUtils.getFileNameWithoutSuffix(file.getName());
                    Log.i(TAG, "subFileMD5: " + subFileMD5);
                    Log.i(TAG, "fileNameWithoutSuffix: " + fileNameWithoutSuffix);
                    if (subFileMD5.equals(fileNameWithoutSuffix)) {
                        continue;
                    }
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
        }
    }
}
