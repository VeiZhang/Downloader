package com.excellence.downloader;

import static com.excellence.downloader.utils.CommonUtil.checkNULL;

import java.io.File;
import java.util.LinkedList;
import java.util.concurrent.Executor;

import com.excellence.downloader.entity.TaskEntity;
import com.excellence.downloader.utils.IListener;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     time   : 2017/8/9
 *     desc   : 下载管理
 * </pre>
 */

public class FileDownloader
{
	public static final String TAG = FileDownloader.class.getSimpleName();

	private Executor mExecutor = null;
	private final LinkedList<DownloadTask> mTaskQueue;
	private int mParallelTaskCount;
	private int mThreadCount;

	public FileDownloader(int parallelTaskCount, int threadCount)
	{
		final Handler handler = new Handler(Looper.getMainLooper());
		mExecutor = new Executor()
		{
			@Override
			public void execute(@NonNull Runnable command)
			{
				handler.post(command);
			}
		};
		mTaskQueue = new LinkedList<>();
		mParallelTaskCount = parallelTaskCount;
		mThreadCount = threadCount;
	}

	public DownloadTask addTask(File storeFile, String url, IListener listener)
	{
		TaskEntity taskEntity = new TaskEntity();
		taskEntity.storeFile = storeFile;
		taskEntity.url = url;
		taskEntity.threadCount = mThreadCount;

		DownloadTask task = new DownloadTask(taskEntity, listener);
		synchronized (mTaskQueue)
		{
			mTaskQueue.add(task);
		}
		schedule();
		return task;
	}

	public DownloadTask addTask(String filePath, String url, IListener listener)
	{
		return addTask(new File(filePath), url, listener);
	}

	/**
	 * notify task queue
	 */
	private synchronized void schedule()
	{
		// count run task
		int runTaskCount = 0;
		for (DownloadTask task : mTaskQueue)
		{
			if (task.isDownloading())
				runTaskCount++;
		}

		if (runTaskCount >= mParallelTaskCount)
			return;

		// deploy task to fill parallel task count
		for (DownloadTask task : mTaskQueue)
		{
			if (task.deploy() && ++runTaskCount == mParallelTaskCount)
				return;
		}
	}

	public DownloadTask get(File storeFile, String url)
	{
		if (storeFile == null || checkNULL(url))
			return null;
		for (DownloadTask task : mTaskQueue)
		{
			if (task.check(storeFile, url))
				return task;
		}
		return null;
	}

	public DownloadTask get(String filePath, String url)
	{
		return get(new File(filePath), url);
	}

	public LinkedList<DownloadTask> getTaskQueue()
	{
		return mTaskQueue;
	}
}
