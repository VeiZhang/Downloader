package com.excellence.downloader.scheduler;

import com.excellence.downloader.FileDownloader.DownloadTask;

import static com.excellence.compiler.ProxyConstant.COUNT_METHOD_DOWNLOAD;
import static com.excellence.compiler.ProxyConstant.PROXY_SUFFIX_DOWNLOAD;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     time   : 2017/8/30
 *     desc   : 事件调度器，用于处理任务状态的调度
 * </pre>
 */

public class DownloadScheduler extends Scheduler<DownloadTask> {

    private static final String TAG = DownloadScheduler.class.getSimpleName();

    private static DownloadScheduler mInstance = null;

    public static DownloadScheduler getInstance() {
        if (mInstance == null) {
            mInstance = new DownloadScheduler();
        }
        return mInstance;
    }

    private DownloadScheduler() {
        super();
    }

    @Override
    protected String getProxySuffix() {
        return PROXY_SUFFIX_DOWNLOAD;
    }

    @Override
    protected String getMethodCounter() {
        return COUNT_METHOD_DOWNLOAD;
    }

}
