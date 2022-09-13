package com.excellence.downloader;

import android.text.TextUtils;
import android.util.Log;

import com.excellence.downloader.entity.TaskEntity;
import com.excellence.downloader.exception.DownloadError;
import com.excellence.downloader.exception.FileError;
import com.excellence.downloader.exception.URLInvalidError;
import com.excellence.downloader.utils.BufferedRandomAccessFile;
import com.excellence.downloader.utils.IListener;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.excellence.downloader.utils.CommonUtil.SUFFIX_TMP;
import static com.excellence.downloader.utils.CommonUtil.checkNULL;
import static com.excellence.downloader.utils.HttpUtil.convertInputStream;
import static com.excellence.downloader.utils.HttpUtil.convertUrl;
import static com.excellence.downloader.utils.HttpUtil.printHeader;
import static com.excellence.downloader.utils.HttpUtil.setConnectParam;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     time   : 2017/8/10
 *     desc   : 下载开始
 * </pre>
 */

class HttpDownloadTask extends HttpTask implements IListener {

    private static final String TAG = HttpDownloadTask.class.getSimpleName();

    private static final int SO_TIME_OUT = 10 * 1000;
    private static final int STREAM_LEN = 8 * 1024;

    private Executor mResponsePoster = null;
    private TaskEntity mTaskEntity = null;
    private IListener mListener = null;
    private File mTempFile = null;
    private ScheduledExecutorService mSpeedTimer = null;
    private long mStartLen = 0;
    private boolean isOpenDynamicFile = true;
    private long mLastSendProgressTime = 0;

    protected HttpDownloadTask(Executor responsePoster, TaskEntity taskEntity, IListener listener) {
        mResponsePoster = responsePoster;
        mTaskEntity = taskEntity;
        mListener = listener;
        mTempFile = new File(mTaskEntity.storeFile + SUFFIX_TMP);
        mTaskEntity.tempFile = mTempFile;
        isOpenDynamicFile = Downloader.getOptions().isOpenDynamicFile;
    }

    @Override
    protected boolean buildRequest() {
        HttpURLConnection conn = null;
        try {
            checkTask();
            Log.i(TAG, "Start Download");
            URL httpURL = new URL(convertUrl(mTaskEntity.url));
            conn = (HttpURLConnection) httpURL.openConnection();
            conn.setConnectTimeout(CONNECT_TIME_OUT);
            conn.setReadTimeout(SO_TIME_OUT);

            if (mTaskEntity.fileSize > 0) {
                long range = mTaskEntity.downloadLen;
                conn.setRequestProperty("Range", String.format("bytes=%s-%s", range, mTaskEntity.fileSize - 1));
            }

            setConnectParam(conn, mTaskEntity.url);
            conn.connect();

            BufferedInputStream inputStream = new BufferedInputStream(convertInputStream(conn));

            if (mTaskEntity.isCancel) {
                onCancel();
                return true;
            }

            printHeader(conn);
            handleHeader(conn);
            startTimer();

            if (isOpenDynamicFile) {
                dynamicTransmission(inputStream);
            } else {
                normalTransmission(inputStream);
            }

            inputStream.close();

            if (mTaskEntity.isCancel) {
                return true;
            }

            if (mTaskEntity.checkHeaderInfo) {
                Log.d(TAG, "stream finish : " + mTempFile.length() + ", " + mTaskEntity.fileSize);
                if (mTempFile.length() == mTaskEntity.fileSize
                        || mTempFile.length() + 1 == mTaskEntity.fileSize) {
                    if (!mTempFile.canRead()) {
                        throw new FileError("Download temp file is invalid");
                    }
                    if (!mTempFile.renameTo(mTaskEntity.storeFile)) {
                        throw new FileError("Can't rename download temp file");
                    }
                    onSuccess();
                } else {
                    throw new FileError("Download file size is error");
                }
            } else {
                onSuccess();
            }
        } catch (Exception e) {
            if (retry()) {
                onError(new DownloadError(e));
                return true;
            } else {
                Log.w(TAG, "buildRequest error : ", e);
                return false;
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return true;
    }

    private void handleHeader(HttpURLConnection conn) {
        if (mTaskEntity.checkHeaderInfo) {
            return;
        }

        long len = conn.getContentLength();
        if (len < 0) {
            String temp = conn.getHeaderField("Content-Length");
            len = TextUtils.isEmpty(temp) ? -1 : Long.parseLong(temp);
        }

        mTaskEntity.fileSize = len;
        /**
         * 断点读取时，检查头的时候就读取了长度，断点下载时再次读取，有可能不能读取全部的长度
         */
        onPreExecute(mTaskEntity.fileSize);
    }

    /**
     * 使用块传输，直接通过追加的形式，写入到文件里
     * 单线程文件追加，效果等同 {@link FileChannel#position(long)}
     *
     * @param inputStream
     */
    private void dynamicTransmission(InputStream inputStream) throws Exception {
        FileOutputStream outputStream = new FileOutputStream(mTempFile, true);
        FileChannel channel = outputStream.getChannel();
        channel.position(mTaskEntity.downloadLen);
        ReadableByteChannel readableByteChannel = Channels.newChannel(inputStream);
        ByteBuffer buffer = ByteBuffer.allocate(STREAM_LEN);
        int read;
        while ((read = readableByteChannel.read(buffer)) != -1) {
            buffer.flip();
            channel.write(buffer);
            buffer.compact();

            mTaskEntity.downloadLen += read;
            onIntervalProgressChange(mTaskEntity.fileSize, mTaskEntity.downloadLen);

            if (mTaskEntity.isCancel) {
                onCancel();
                break;
            }
        }
        outputStream.close();
        channel.close();
        readableByteChannel.close();
    }

    /**
     * 普通的文件传输，优化文件写入速度 {@link RandomAccessFile} -> {@link BufferedRandomAccessFile}
     *
     * @param inputStream
     * @throws Exception
     */
    private void normalTransmission(InputStream inputStream) throws Exception {
        BufferedRandomAccessFile randomAccessFile = new BufferedRandomAccessFile(mTempFile, "rwd", STREAM_LEN);
        randomAccessFile.seek(mTaskEntity.downloadLen);

        byte[] buffer = new byte[STREAM_LEN];
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            randomAccessFile.write(buffer, 0, read);
            mTaskEntity.downloadLen += read;
            onIntervalProgressChange(mTaskEntity.fileSize, mTaskEntity.downloadLen);

            if (mTaskEntity.isCancel) {
                onCancel();
                break;
            }
        }
        randomAccessFile.close();
    }

    private void startTimer() {
        mStartLen = mTaskEntity.downloadLen;
        mSpeedTimer = new ScheduledThreadPoolExecutor(1, new BasicThreadFactory.Builder().namingPattern("timer-%d").daemon(true).build());
        mSpeedTimer.scheduleAtFixedRate(mTimerTask, 0, 1, TimeUnit.SECONDS);
    }

    private Runnable mTimerTask = new Runnable() {
        @Override
        public void run() {
            if (mTaskEntity.isCancel) {
                closeTimer();
            } else {
                mTaskEntity.downloadSpeed = mTaskEntity.downloadLen - mStartLen;
                onProgressChange(mTaskEntity.fileSize, mTaskEntity.downloadLen, mTaskEntity.downloadSpeed);
                mStartLen = mTaskEntity.downloadLen;
            }
        }
    };

    private void closeTimer() {
        if (mSpeedTimer != null) {
            mSpeedTimer.shutdownNow();
        }
    }

    /**
     * 检测任务：下载链接、本地文件等等
     */
    private void checkTask() throws Exception {
        if (checkNULL(mTaskEntity.url)) {
            throw new URLInvalidError("URL is invalid");
        }

        if (!mTempFile.exists()) {
            if (!mTempFile.getParentFile().exists() && !mTempFile.getParentFile().mkdirs()) {
                throw new FileError("Failed to open downloader dir");
            }

            if (!mTempFile.createNewFile()) {
                throw new FileError("Failed to create storage file");
            }
        }

        if (mTempFile.isDirectory()) {
            throw new FileError("Storage file is a directory");
        }

        if (mTaskEntity.isSupportBP) {
            FileInputStream is = new FileInputStream(mTempFile);
            mTaskEntity.downloadLen = is.available();
            is.close();
        } else {
            mTaskEntity.downloadLen = 0;
            RandomAccessFile randomAccessFile = new RandomAccessFile(mTempFile, "rwd");
            randomAccessFile.setLength(0);
            randomAccessFile.close();
        }

        File parentDir = mTempFile.getParentFile();
        if (parentDir.getFreeSpace() <= mTaskEntity.fileSize - mTaskEntity.downloadLen) {
            throw new FileError("Space is not enough");
        }

    }

    @Override
    public void onPreExecute(final long fileSize) {
        mResponsePoster.execute(new Runnable() {
            @Override
            public void run() {
                if (mListener != null) {
                    mListener.onPreExecute(fileSize);
                }
            }
        });
    }

    private void onIntervalProgressChange(final long fileSize, final long downloadedSize) {
        long updateInterval = Downloader.getOptions().mUpdateInterval;
        if (updateInterval == 0
                || System.currentTimeMillis() - mLastSendProgressTime >= updateInterval) {
            mLastSendProgressTime = System.currentTimeMillis();
            onProgressChange(fileSize, downloadedSize);
        }
    }

    @Override
    public void onProgressChange(final long fileSize, final long downloadedSize) {
        mResponsePoster.execute(new Runnable() {
            @Override
            public void run() {
                if (mListener != null && !mTaskEntity.isCancel) {
                    mListener.onProgressChange(fileSize, downloadedSize);
                }
            }
        });
    }

    @Override
    public void onProgressChange(final long fileSize, final long downloadedSize, final long speed) {
        mResponsePoster.execute(new Runnable() {
            @Override
            public void run() {
                if (mListener != null && !mTaskEntity.isCancel) {
                    mListener.onProgressChange(fileSize, downloadedSize, speed);
                }
            }
        });
    }

    @Override
    public void onCancel() {
        closeTimer();
        mResponsePoster.execute(new Runnable() {
            @Override
            public void run() {
                if (mListener != null) {
                    mListener.onCancel();
                }
            }
        });
    }

    @Override
    public void onError(final DownloadError error) {
        closeTimer();
        mResponsePoster.execute(new Runnable() {
            @Override
            public void run() {
                if (mListener != null && !mTaskEntity.isCancel) {
                    mListener.onError(error);
                }
            }
        });
    }

    @Override
    public void onSuccess() {
        closeTimer();
        mResponsePoster.execute(new Runnable() {
            @Override
            public void run() {
                if (mListener != null && !mTaskEntity.isCancel) {
                    mListener.onSuccess();
                }
            }
        });
    }
}
