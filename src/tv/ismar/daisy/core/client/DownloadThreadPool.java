package tv.ismar.daisy.core.client;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by huaijie on 6/23/15.
 */
public class DownloadThreadPool {
    private static DownloadThreadPool instance;

    private ExecutorService executorService;

    private DownloadThreadPool() {
        if (executorService == null) {
            executorService = Executors.newSingleThreadExecutor();
        }
    }

    public static DownloadThreadPool getInstance() {
        if (instance == null) {
            instance = new DownloadThreadPool();
        }
        return instance;
    }

    public void add(DownloadClient client) {
        executorService.execute(client);
    }
}
