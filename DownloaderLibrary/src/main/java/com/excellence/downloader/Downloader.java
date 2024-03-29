package com.excellence.downloader;

import android.Manifest;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.excellence.downloader.FileDownloader.DownloadTask;
import com.excellence.downloader.scheduler.DownloadScheduler;
import com.excellence.downloader.utils.IListener;

import java.io.File;
import java.util.LinkedList;

import static com.excellence.downloader.FileDownloader.generateDownloadKey;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     time   : 2017/8/9
 *     desc   : 下载器初始化
 *              权限
 *                  {@link Manifest.permission#INTERNET}
 *                  {@link Manifest.permission#WRITE_EXTERNAL_STORAGE}
 *                  {@link Manifest.permission#READ_EXTERNAL_STORAGE}
 * </pre>
 */

public class Downloader {

    private static final String TAG = Downloader.class.getSimpleName();

    private static final int DEFAULT_TASK_COUNT = 2;
    private static final int DEFAULT_THREAD_COUNT = 1;

    private static Downloader mInstance = null;
    private FileDownloader mFileDownloader = null;
    private DownloadOptions mOptions = null;

    private Downloader() {

    }

    /**
     * 初始化，默认下载选项：任务数:2，单线程下载
     *
     */
    public static void init() {
        init(new DownloadOptions.Builder().parallelTaskCount(DEFAULT_TASK_COUNT).threadCount(DEFAULT_THREAD_COUNT).build());
    }

    /**
     * 初始化，设置下载选项
     *
     * @param options 下载选项设置
     */
    public static void init(@NonNull DownloadOptions options) {
        if (mInstance != null) {
            Log.w(TAG, "Downloader initialized!!!");
            return;
        }

        mInstance = new Downloader();
        mInstance.mOptions = options;
        mInstance.mFileDownloader = new FileDownloader();
    }

    protected static DownloadOptions getOptions() {
        return mInstance.mOptions;
    }

    /**
     * {@link Task}
     *
     * 新建下载任务，搭配注解方式监听
     *
     * @param storeFile
     * @param url
     * @return
     */
    @Deprecated
    public static DownloadTask addTask(@NonNull File storeFile, @NonNull String url) {
        return addTask(storeFile, url, null);
    }

    /**
     * {@link Task}
     *
     * 新建下载任务，搭配注解方式监听
     *
     * @param filePath
     * @param url
     * @return
     */
    @Deprecated
    public static DownloadTask addTask(@NonNull String filePath, @NonNull String url) {
        return addTask(filePath, url, null);
    }

    /**
     * {@link Task}
     *
     * 新建下载任务，推荐注解方式{@link #addTask(String, String)}、{@link #register(Object)}
     *
     * @param filePath 文件路径
     * @param url 下载链接
     * @param listener
     * @return 下载任务
     */
    @Deprecated
    public static DownloadTask addTask(@NonNull String filePath, @NonNull String url, IListener listener) {
        return addTask(new File(filePath), url, listener);
    }

    /**
     * {@link Task}
     *
     * 新建下载任务，推荐注解方式{@link #addTask(File, String)}、{@link #register(Object)}
     *
     * @param storeFile 文件
     * @param url 下载链接
     * @param listener
     * @return 下载任务
     */
    @Deprecated
    public static DownloadTask addTask(@NonNull File storeFile, @NonNull String url, IListener listener) {
        return addTask(storeFile, url, true, listener);
    }

    /**
     * {@link Task}
     *
     * 是否检查头信息，可用于断点下载；不检查头信息，可用于下载媒体流
     *
     * @param storeFile
     * @param url
     * @param checkHeaderInfo
     * @param listener
     * @return
     */
    @Deprecated
    public static DownloadTask addTask(@NonNull File storeFile, @NonNull String url, boolean checkHeaderInfo,
                                       IListener listener) {
        return addTask(generateDownloadKey(storeFile, url), storeFile, url, checkHeaderInfo, listener);
    }

    /**
     * {@link Task}
     *
     * 是否检查头信息，可用于断点下载；不检查头信息，可用于下载媒体流
     *
     * @param key
     * @param storeFile
     * @param url
     * @param checkHeaderInfo
     * @param listener
     * @return
     */
    @Deprecated
    public static DownloadTask addTask(String key, @NonNull File storeFile, @NonNull String url,
                                       boolean checkHeaderInfo, IListener listener) {
        checkDownloader();
        return mInstance.mFileDownloader.addTask(key, storeFile, url, checkHeaderInfo, listener);
    }

    public static DownloadTask addTask(Task task) {
        return addTask(task.key, task.storeFile, task.url, task.checkHeaderInfo, task.listener);
    }

    /**
     * 获取下载任务
     *
     * @param storeFile 保存文件
     * @param url 下载链接
     * @return
     */
    @Nullable
    public static DownloadTask get(File storeFile, String url) {
        checkDownloader();
        return mInstance.mFileDownloader.get(storeFile, url);
    }

    /**
     * 获取下载任务
     *
     * @param filePath 保存路径
     * @param url 下载链接
     * @return
     */
    @Nullable
    public static DownloadTask get(String filePath, String url) {
        checkDownloader();
        return mInstance.mFileDownloader.get(filePath, url);
    }

    /**
     * 获取下载任务
     *
     * @param key 任务标识id
     * @return
     */
    @Nullable
    public static DownloadTask get(String key) {
        checkDownloader();
        return mInstance.mFileDownloader.get(key);
    }

    /**
     * 获取下载队列
     *
     * @return
     */
    public static LinkedList<DownloadTask> getTaskQueue() {
        checkDownloader();
        return mInstance.mFileDownloader.getTaskQueue();
    }

    private static void checkDownloader() {
        if (mInstance == null) {
            throw new RuntimeException("Downloader not initialized!!!");
        }
    }

    /**
     * 关闭下载器
     */
    public static void destroy() {
        if (mInstance.mFileDownloader != null) {
            mInstance.mFileDownloader.clearAll();
        }
    }

    /**
     * 注册，创建监听
     *
     * @param obj
     */
    public static void register(Object obj) {
        checkDownloader();
        DownloadScheduler.getInstance().register(obj);
    }

    /**
     * 解绑
     *
     * @param obj
     */
    public static void unregister(Object obj) {
        checkDownloader();
        DownloadScheduler.getInstance().unregister(obj);
    }

    public static class Task {

        private String key;
        private File storeFile;
        private String url;
        private boolean checkHeaderInfo = true;
        private IListener listener;

        private Task(Builder builder) {
            key = builder.key;
            storeFile = builder.storeFile;
            url = builder.url;
            checkHeaderInfo = builder.checkHeaderInfo;
            listener = builder.listener;
        }

        public static final class Builder {

            private String key;
            private File storeFile;
            private String url;
            private boolean checkHeaderInfo = true;
            private IListener listener;

            /**
             * @param storeFile 存入路径
             * @param url 下载链接
             */
            public Builder(File storeFile, String url) {
                this.storeFile = storeFile;
                this.url = url;
            }

            /**
             * @param key 任务ID
             * @return
             */
            public Builder key(String key) {
                this.key = key;
                return this;
            }

            /**
             * @param checkHeaderInfo 检查头信息，是否断点续传，默认true
             *                        检查头信息，判断是否服务器是否支持断点；当false时，某些视频流下载，有效期限制，因此false时只请求一次
             * @return
             */
            public Builder checkHeaderInfo(boolean checkHeaderInfo) {
                this.checkHeaderInfo = checkHeaderInfo;
                return this;
            }

            /**
             * @param listener 下载监听
             * @return
             */
            public Builder listener(IListener listener) {
                this.listener = listener;
                return this;
            }

            public Task build() {
                return new Task(this);
            }
        }
    }
}
