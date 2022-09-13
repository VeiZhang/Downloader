package com.excellence.downloader;

import android.support.annotation.IntRange;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     time   : 2018/7/3
 *     desc   : 下载选项
 * </pre> 
 */
public class DownloadOptions {

    protected int mParallelTaskCount;
    protected int mThreadCount;
    protected boolean isOpenDynamicFile = true;
    protected long mUpdateInterval = 1000;

    private DownloadOptions(Builder builder) {
        mParallelTaskCount = builder.mParallelTaskCount;
        mThreadCount = builder.mThreadCount;
        isOpenDynamicFile = builder.isOpenDynamicFile;
        mUpdateInterval = builder.mUpdateInterval;
    }

    public static class Builder {

        private int mParallelTaskCount;
        private int mThreadCount;
        private boolean isOpenDynamicFile = true;
        private long mUpdateInterval = 1000;

        /**
         * 设置下载并发任务数
         *
         * @param parallelTaskCount
         * @return
         */
        public Builder parallelTaskCount(@IntRange(from = 1) int parallelTaskCount) {
            mParallelTaskCount = parallelTaskCount;
            return this;
        }

        /**
         * 单个任务下载线程数，目前只支持单线程，默认为1，暂时未使用
         *
         * @param threadCount
         * @return
         */
        public Builder threadCount(@IntRange(from = 1) int threadCount) {
            mThreadCount = threadCount;
            return this;
        }

        /**
         * 是否开启动态文件传输
         *
         * @param isOpenDynamicFile
         * @return
         */
        public Builder isOpenDynamicFile(boolean isOpenDynamicFile) {
            this.isOpenDynamicFile = isOpenDynamicFile;
            return this;
        }

        /**
         * 进度更新频率，默认1s； 0s则表示不限制
         *
         * @param updateInterval
         */
        public Builder updateInterval(@IntRange(from = 0) long updateInterval) {
            this.mUpdateInterval = updateInterval;
            return this;
        }

        public DownloadOptions build() {
            return new DownloadOptions(this);
        }

    }
}
