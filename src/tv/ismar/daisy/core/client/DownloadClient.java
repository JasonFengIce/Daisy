package tv.ismar.daisy.core.client;

import android.nfc.Tag;
import android.util.Log;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import tv.ismar.daisy.data.table.DownloadTable;
import tv.ismar.daisy.utils.HardwareUtils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by huaijie on 6/19/15.
 */
public class DownloadClient implements Runnable {
    private static final String TAG = "DownloadClient";

    private String url;
    private File downloadFile;

    public DownloadClient(String downloadUrl, String savePath) {
        this.url = downloadUrl;

        try {
            String videoName = new File(new URL(downloadUrl).getFile()).getName();
            downloadFile = new File(savePath, videoName);
            if (!downloadFile.getParentFile().exists()) {
                downloadFile.getParentFile().mkdirs();
            }
        } catch (MalformedURLException e) {
            Log.e(TAG, e.getMessage());
        }

    }

    @Override
    public void run() {
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
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        saveToDB();
    }


    private void saveToDB() {
        DownloadTable downloadTable = new DownloadTable();
        downloadTable.file_name = downloadFile.getName();
        downloadTable.download_path = downloadFile.getAbsolutePath();
        downloadTable.url = url;
        downloadTable.md5 = HardwareUtils.getMd5ByFile(downloadFile);
        downloadTable.save();
    }
}
