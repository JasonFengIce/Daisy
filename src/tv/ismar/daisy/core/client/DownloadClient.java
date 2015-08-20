package tv.ismar.daisy.core.client;

import android.util.Log;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import tv.ismar.daisy.data.table.DownloadTable;
import tv.ismar.daisy.utils.FileUtils;
import tv.ismar.daisy.utils.HardwareUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by huaijie on 6/19/15.
 */
public class DownloadClient implements Runnable {
    private static final String TAG = "DownloadClient";

    private String url;
    private File downloadFile;
    private String mSavePath;
    private String mServerMD5;
    private String mLocalFileName;


    public DownloadClient(String downloadUrl, String savePath) {
        this.url = downloadUrl;
        String fileName = FileUtils.getFileByUrl(downloadUrl);
        this.mSavePath = savePath;
        this.mServerMD5 = fileName.split("\\.")[0];
        this.mLocalFileName = fileName;
    }


    @Override
    public void run() {

        try {

            downloadFile = new File(mSavePath, mLocalFileName);
            if (!downloadFile.exists()) {
                downloadFile.getParentFile().mkdirs();
                downloadFile.createNewFile();
                Log.d(TAG, "create file: " + downloadFile.getAbsolutePath());
            }
        } catch (IOException e) {
            Log.e(TAG, "create file exception: " + e.getMessage());
            return;
        }

        DownloadTable downloadTable = new DownloadTable();
        downloadTable.file_name = downloadFile.getName();
        downloadTable.download_path = downloadFile.getAbsolutePath();
        downloadTable.url = url;
        downloadTable.server_md5 = mServerMD5;
        downloadTable.save();

        try {


            OkHttpClient client = new OkHttpClient();
            FileOutputStream fileOutputStream;
            Response response;
            Log.d(TAG, "download url is: " + url);
            Log.d(TAG, "save path is : " + mSavePath);
            File localFile = new File(mSavePath, mLocalFileName);
            if (localFile.exists()) {
                String localMD5 = HardwareUtils.getMd5ByFile(localFile);
                if (localMD5.equalsIgnoreCase(mServerMD5)) {
                    return;

                    //not download complete
                } else {
                    Request request = new Request.Builder()
                            .url(url)
                            .addHeader("RANGE", "bytes=" + localFile.length() + "-")
                            .build();
                    response = client.newCall(request).execute();
                    fileOutputStream = new FileOutputStream(localFile, true);

                }
                //local file not exists
            } else {
                Request request = new Request.Builder().url(url).build();
                response = client.newCall(request).execute();
                fileOutputStream = new FileOutputStream(downloadFile, false);
            }
            InputStream inputStream = response.body().byteStream();
            byte[] buffer = new byte[1024];
            int byteRead;
            while ((byteRead = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, byteRead);
            }
            fileOutputStream.flush();
            fileOutputStream.close();
            inputStream.close();
            downloadTable.local_md5 = HardwareUtils.getMd5ByFile(downloadFile);
            downloadTable.save();
            Log.d(TAG, url + " ---> download complete!!!");
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return;
        }


    }
}
