package tv.ismar.daisy.core.client;

import android.content.Context;
import android.util.Log;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import tv.ismar.daisy.data.table.DownloadTable;
import tv.ismar.daisy.utils.HardwareUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by huaijie on 6/26/15.
 */
public class InternalDownloadClient implements Runnable {
    private static final String TAG = "DownloadClient";

    private String url;
    private String tag;
    private Context context;


    public InternalDownloadClient(Context context, String downloadUrl, String tag) {
        this.tag = tag;
        this.url = downloadUrl;
        this.context = context;
    }

    @Override
    public void run() {
//        try {
//            String videoName = new File(new URL(url).getFile()).getName();
//            downloadFile = new File(savePath, videoName);
//            if (!downloadFile.exists()) {
//                downloadFile.getParentFile().mkdirs();
//                downloadFile.createNewFile();
//                Log.d(TAG, "create file: " + downloadFile.getAbsolutePath());
//            }
//        } catch (IOException e) {
//            Log.e(TAG, "create file exception: " + e.getMessage());
//            return;
//        }
//
//
//        DownloadTable downloadTable = new DownloadTable();
//        downloadTable.file_name = downloadFile.getName();
//        downloadTable.download_path = downloadFile.getAbsolutePath();
//        downloadTable.url = url;
//        downloadTable.save();


        try {
            String videoName = new File(new URL(url).getFile()).getName();
            String downloadPath = tag + "/" + videoName;
            OkHttpClient client = new OkHttpClient();
            FileOutputStream fileOutputStream = context.openFileOutput(downloadPath, Context.MODE_WORLD_WRITEABLE | Context.MODE_WORLD_READABLE);
            Response response;
            Log.d(TAG, "download url is: " + url);
            Request request = new Request.Builder().url(url).build();
            response = client.newCall(request).execute();
            InputStream inputStream = response.body().byteStream();
            byte[] buffer = new byte[1024];
            int byteRead;
            while ((byteRead = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, byteRead);
            }
            fileOutputStream.flush();
            fileOutputStream.close();
            inputStream.close();
            DownloadTable downloadTable = new DownloadTable();
//        downloadTable.download_path = context.getf;
//        downloadTable.url = url;
//        downloadTable.save();
////            downloadTable.md5 = HardwareUtils.getMd5ByFile(downloadFile);
////            downloadTable.save();
            Log.d(TAG, url + " ---> download complete!!!");
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return;
        }

    }
}
