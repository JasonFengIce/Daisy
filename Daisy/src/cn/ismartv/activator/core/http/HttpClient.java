package cn.ismartv.activator.core.http;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import cn.ismartv.activator.Activator;
import cn.ismartv.activator.data.Erro;
import com.google.gson.Gson;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Map;

import tv.ismar.daisy.core.NetworkUtils;

public class HttpClient {
    private static final String TAG = "HttpClient";
    public static final int SUCCESS = 0x0000;
    public static final int FAILED = 0x0001;

    public static void postDatas(Handler messageHandler, Context context, String postUrl, Map<String, String> map) {
        HttpURLConnection connection = null;
        URL url;
        StringBuffer sb = new StringBuffer();
        String params = "";
        if (map != null) {
            for (Map.Entry<String, String> e : map.entrySet()) {
                sb.append(e.getKey());
                sb.append("=");
                sb.append(e.getValue());
                sb.append("&");
            }
            params = sb.substring(0, sb.length() - 1);
        }
        try {
            url = new URL(postUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setConnectTimeout(NetworkUtils.CONNET_TIME_OUT);
            connection.setReadTimeout(NetworkUtils.READ_TIME_OUT);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            OutputStreamWriter osw = new OutputStreamWriter(
                    connection.getOutputStream(), "UTF-8");
            osw.write(params);
            osw.flush();
            osw.close();
            int byteread;

            if (200 != connection.getResponseCode()) {
                InputStream einStream = connection.getErrorStream();
                BufferedReader reader3;
                StringBuffer sb3 = new StringBuffer();
                reader3 = new BufferedReader(new InputStreamReader(einStream, "UTF-8"));
                String line3;
                while ((line3 = reader3.readLine()) != null) {
                    sb3.append(line3);
                }
                if (Activator.DEBUG)
                    Log.d(TAG, "erro message is ---> " + sb3.toString());
               Message message = messageHandler.obtainMessage(FAILED, "无法获取证书");
                messageHandler.sendMessage(message);
            } else {
                InputStream inStream = connection.getInputStream();
                String header = connection.getHeaderField("Content-Disposition");
                String headerInfo = URLDecoder.decode(header, "utf-8");
                if (Activator.DEBUG) {
                    Log.d(TAG, "header is  ---> " + headerInfo);
                }
               // AppSharedPreferences.getInstance(context).setPackageInfo(header);
                FileOutputStream fs = context.openFileOutput(
                        "sign1", Context.MODE_WORLD_READABLE);
                byte[] buffer = new byte[1024];
                while ((byteread = inStream.read(buffer)) != -1) {
                    fs.write(buffer, 0, byteread);
                }
                fs.flush();
                fs.close();
                inStream.close();
               messageHandler.sendEmptyMessage(SUCCESS);

            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            Message message = messageHandler.obtainMessage(FAILED, "无法获取证书:请检查网络连接是否正常");
            messageHandler.sendMessage(message);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
