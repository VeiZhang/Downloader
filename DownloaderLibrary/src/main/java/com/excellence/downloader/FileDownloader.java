package com.excellence.downloader;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.excellence.downloader.entity.TaskEntity;
import com.excellence.downloader.exception.DownloadError;
import com.excellence.downloader.scheduler.DownloadScheduler;
import com.excellence.downloader.utils.IListener;

import java.io.File;
import java.util.LinkedList;
import java.util.concurrent.Executor;

import static com.excellence.downloader.entity.TaskEntity.STATUS_DOWNLOADING;
import static com.excellence.downloader.entity.TaskEntity.STATUS_ERROR;
import static com.excellence.downloader.entity.TaskEntity.STATUS_PAUSE;
import static com.excellence.downloader.entity.TaskEntity.STATUS_SUCCESS;
import static com.excellence.downloader.entity.TaskEntity.STATUS_WAITING;
import static com.excellence.downloader.utils.CommonUtil.checkNULL;
import static com.excellence.downloader.utils.CommonUtil.deleteTmpFile;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     time   : 2017/8/9
 *     desc   : 下载管理
 * </pre>
 */

public class FileDownloader {
    private static final String TAG = FileDownloader.class.getSimpleName();

    private Executor mResponsePoster = null;
    private final LinkedList<DownloadTask> mTaskQueue;
    private DownloadScheduler mDownloadScheduler = null;
    private int mParallelTaskCount;
    private int mThreadCount;

    protected FileDownloader(DownloadScheduler downloadScheduler) {
        DownloadOptions options = Downloader.getOptions();
        mParallelTaskCount = options.mParallelTaskCount;
        mThreadCount = options.mThreadCount;
        if (mParallelTaskCount >= Runtime.getRuntime().availableProcessors()) {
            Log.w(TAG, "ParallelTaskCount is beyond!!!");
            mParallelTaskCount = Runtime.getRuntime().availableProcessors() == 1 ? 1 : Runtime.getRuntime().availableProcessors() - 1;
        }

        mDownloadScheduler = downloadScheduler;
        final Handler handler = new Handler(Looper.getMainLooper());
        mResponsePoster = new Executor() {
            @Override
            public void execute(@NonNull Runnable command) {
                handler.post(command);
            }
        };
        mTaskQueue = new LinkedList<>();
    }

    /**
     * 新建下载任务
     *
     * @param key 任务id
     * @param storeFile 保存文件
     * @param url 下载链接
     * @param listener
     * @return
     */
    public DownloadTask addTask(@Nullable String key, File storeFile, String url, boolean checkHeaderInfo,
                                IListener listener) {
        DownloadTask task = get(storeFile, url);
        if (task != null) {
            /**
             * 重新设置监听器
             */
            task.setListener(listener);
            task.resume();
        } else {
            task = new DownloadTask(key, storeFile, url, checkHeaderInfo, listener);
            synchronized (mTaskQueue) {
                mTaskQueue.add(task);
            }
            schedule();
        }
        return task;
    }

    /**
     * 新建下载任务
     *
     * @param storeFile 保存文件
     * @param url 下载链接
     * @param listener
     * @return
     */
    public DownloadTask addTask(File storeFile, String url, boolean checkHeaderInfo,
                                IListener listener) {
        return addTask(generateDownloadKey(storeFile, url), storeFile, url, checkHeaderInfo, listener);
    }

    /**
     * 新建下载任务
     *
     * @param storeFile 保存路径
     * @param url 下载链接
     * @param listener
     * @return
     */
    public DownloadTask addTask(File storeFile, String url, IListener listener) {
        return addTask(storeFile, url, false, listener);
    }

    /**
     * 新建下载任务
     *
     * @param filePath 保存路径
     * @param url 下载链接
     * @param listener
     * @return
     */
    public DownloadTask addTask(String filePath, String url, IListener listener) {
        return addTask(new File(filePath), url, false, listener);
    }

    /**
     * 刷新任务队列
     */
    private synchronized void schedule() {
        // count run task
        int runTaskCount = 0;
        for (DownloadTask task : mTaskQueue) {
            if (task.isDownloading()) {
                runTaskCount++;
            }
        }

        if (runTaskCount >= mParallelTaskCount) {
            return;
        }

        // deploy task to fill parallel task count
        for (DownloadTask task : mTaskQueue) {
            if (task.deploy() && ++runTaskCount == mParallelTaskCount) {
                return;
            }
        }
    }

    private synchronized void remove(DownloadTask task) {
        mTaskQueue.remove(task);
        schedule();
    }

    /**
     * 关闭所有下载任务
     */
    public synchronized void clearAll() {
        while (!mTaskQueue.isEmpty()) {
            mTaskQueue.get(0).cancel();
        }
    }

    /**
     * 获取下载任务
     *
     * @return
     */
    public DownloadTask get(String key) {
        if (checkNULL(key)) {
            return null;
        }
        for (DownloadTask task : mTaskQueue) {
            if (task.check(key)) {
                return task;
            }
        }
        return null;
    }

    /**
     * 获取下载任务
     *
     * @param filePath 保存路径
     * @param url 下载链接
     * @return
     */
    public DownloadTask get(String filePath, String url) {
        return get(generateDownloadKey(new File(filePath), url));
    }

    /**
     * 获取下载任务
     *
     * @param file 保存路径
     * @param url 下载链接
     * @return
     */
    public DownloadTask get(@NonNull File file, String url) {
        return get(generateDownloadKey(file, url));
    }

    public static String generateDownloadKey(File file, String url) {
        return String.format("%s;%s", file.getPath(), url);
    }

    /**
     * 任务列表
     *
     * @return
     */
    public LinkedList<DownloadTask> getTaskQueue() {
        return mTaskQueue;
    }

    public class DownloadTask {
        private TaskEntity mTaskEntity = null;
        private DownloadRequest mRequest = null;
        private IListener mIListener = null;

        protected DownloadTask(String key, File storeFile, String url, boolean checkHeaderInfo, IListener listener) {
            if (key == null || key.length() == 0) {
                key = generateDownloadKey(storeFile, url);
            }

            TaskEntity taskEntity = new TaskEntity(key);
            taskEntity.storeFile = storeFile;
            taskEntity.url = url;
            taskEntity.threadCount = mThreadCount;
            taskEntity.checkHeaderInfo = checkHeaderInfo;

            mTaskEntity = taskEntity;
            mIListener = listener;
            mRequest = new DownloadRequest(taskEntity, mResponsePoster, new IListener() {
                @Override
                public void onPreExecute(long fileSize) {
                    mDownloadScheduler.onPreExecute(DownloadTask.this);
                    if (mIListener != null) {
                        mIListener.onPreExecute(fileSize);
                    }
                }

                @Override
                public void onProgressChange(long fileSize, long downloadedSize) {
                    mDownloadScheduler.onProgressChange(DownloadTask.this);
                    if (mIListener != null) {
                        mIListener.onProgressChange(fileSize, downloadedSize);
                    }
                }

                @Override
                public void onProgressChange(long fileSize, long downloadedSize, long speed) {
                    mDownloadScheduler.onProgressSpeedChange(DownloadTask.this);
                    if (mIListener != null) {
                        mIListener.onProgressChange(fileSize, downloadedSize, speed);
                    }
                }

                @Override
                public void onCancel() {
                    mDownloadScheduler.onCancel(DownloadTask.this);
                    if (mIListener != null) {
                        mIListener.onCancel();
                    }
                }

                @Override
                public void onError(DownloadError error) {
                    mTaskEntity.setStatus(STATUS_ERROR);
                    mDownloadScheduler.onError(DownloadTask.this);
                    if (mIListener != null) {
                        mIListener.onError(error);
                    }
                    schedule();
                }

                @Override
                public void onSuccess() {
                    mTaskEntity.setStatus(STATUS_SUCCESS);
                    mDownloadScheduler.onSuccess(DownloadTask.this);
                    if (mIListener != null) {
                        mIListener.onSuccess();
                    }
                    remove(DownloadTask.this);
                }
            });
        }

        /**
         * 重新设置下载监听器，解决{@link #addTask}里面，任务不为空时，刷新Listener
         *
         * @param listener
         */
        private void setListener(IListener listener) {
            mIListener = listener;
        }

        /**
         * 开始任务
         *
         * @return
         */
        private boolean deploy() {
            // only wait task can deploy
            if (mTaskEntity.status != STATUS_WAITING) {
                return false;
            }
            mTaskEntity.deploy();
            mRequest.execute();
            return true;
        }

        /**
         * 是否正在下载
         *
         * @return
         */
        public boolean isDownloading() {
            return mTaskEntity.isDownloading();
        }

        private void cancel() {
            mTaskEntity.discard();
            mRequest.cancel();
            mTaskQueue.remove(this);
        }

        /**
         * 完全删除任务
         */
        public void discard() {
            mTaskEntity.discard();
            mRequest.cancel();
            remove(this);
            deleteTmpFile(mTaskEntity.storeFile);
        }

        /**
         * 暂停任务
         *
         * @return
         */
        public boolean pause() {
            switch (mTaskEntity.status) {
                case STATUS_DOWNLOADING:
                case STATUS_WAITING:
                    mTaskEntity.cancel();
                    schedule();
                    return true;
            }
            return false;
        }

        /**
         * 继续任务
         *
         * @return
         */
        public boolean resume() {
            switch (mTaskEntity.status) {
                case STATUS_PAUSE:
                case STATUS_ERROR:
                    mTaskEntity.setStatus(STATUS_WAITING);
                    schedule();
                    return true;
            }
            return false;
        }

        /**
         * 获取下载任务状态
         *
         * @return
         */
        public int getStatus() {
            return mTaskEntity.status;
        }

        /**
         * 检验是否是当前任务
         *
         * @return
         */
        public boolean check(String key) {
            return mTaskEntity.key.equals(key);
        }

        /**
         * 获取下载长度
         *
         * @return
         */
        public long getDownloadLength() {
            return mTaskEntity.downloadLen;
        }

        /**
         * 获取文件长度
         *
         * @return
         */
        public long getFileSize() {
            return mTaskEntity.fileSize;
        }

        /**
         * 获取下载速度:byte/s
         *
         * @return
         */
        public long getDownloadSpeed() {
            return mTaskEntity.downloadSpeed;
        }

        /**
         * 获取下载链接
         *
         * @return
         */
        public String getUrl() {
            return mTaskEntity.url;
        }

        /**
         * 获取下载标识
         *
         * @return
         */
        public String getKey() {
            return mTaskEntity.key;
        }
    }
}
