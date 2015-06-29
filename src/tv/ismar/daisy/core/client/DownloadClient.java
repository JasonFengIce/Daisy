package tv.ismar.daisy.core.client;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
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
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by huaijie on 6/19/15.
 */
public class DownloadClient implements Runnable {
    private static final String TAG = "DownloadClient";

    private String url;
    private File downloadFile;
    private String savePath;
    private DownloadCallback downloadCallback;

    public DownloadClient(String downloadUrl, String savePath, DownloadCallback callback) {
        this.savePath = savePath;
        this.url = downloadUrl;
        this.downloadCallback = callback;
    }

    public DownloadClient(String downloadUrl, String savePath) {
        this.savePath = savePath;
        this.url = downloadUrl;
    }


    @Override
    public void run() {

        try {
            String videoName = new File(new URL(url).getFile()).getName();
            downloadFile = new File(savePath, videoName);
            if (!downloadFile.exists()) {
                downloadFile.getParentFile().mkdirs();
                downloadFile.createNewFile();
                Log.d(TAG, "create file: " + downloadFile.getAbsolutePath());
            }
        } catch (IOException e) {
            Log.e(TAG, "create file exception: " + e.getMessage());

            if (null != downloadCallback)
                downloadCallback.onCreateFileFailure();
            return;
        }
        if (null != downloadCallback)
            downloadCallback.onCreateFileSuccess();

        DownloadTable downloadTable = new DownloadTable();
        downloadTable.file_name = downloadFile.getName();
        downloadTable.download_path = downloadFile.getAbsolutePath();
        downloadTable.url = url;
        downloadTable.md5 = "";
        downloadTable.save();


        try {
            OkHttpClient client = new OkHttpClient();
            FileOutputStream fileOutputStream;
            Response response;
            Log.d(TAG, "download url is: " + url);
            Request request = new Request.Builder().url(url).build();
            response = client.newCall(request).execute();
            fileOutputStream = new FileOutputStream(downloadFile, false);
            InputStream inputStream = response.body().byteStream();
            byte[] buffer = new byte[1024];
            int byteRead;
            while ((byteRead = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, byteRead);
            }
            fileOutputStream.flush();
            fileOutputStream.close();
            inputStream.close();

            downloadTable.md5 = HardwareUtils.getMd5ByFile(downloadFile);
            downloadTable.save();
            Log.d(TAG, url + " ---> download complete!!!");
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return;
        }

    }

    private void deleteAllFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            if (file.isDirectory()) {
                for (File sub : file.listFiles()) {
                    deleteAllFile(sub.getAbsolutePath());
                }
            } else {
                file.delete();
            }
        }
    }

    public interface DownloadCallback {
        void onCreateFileSuccess();

        void onCreateFileFailure();
    }
}
