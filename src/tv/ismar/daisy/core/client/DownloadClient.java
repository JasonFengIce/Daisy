package tv.ismar.daisy.core.client;

/**
 * Created by huaijie on 6/19/15.
 */
public class DownloadClient implements Runnable{


    public DownloadClient(String downloadUrl, String savePath){

    }


    @Override
    public void run() {

    }



    public void download(){
        new Thread(this).start();
    }
}
