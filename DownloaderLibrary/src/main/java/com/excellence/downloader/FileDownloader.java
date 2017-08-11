package com.excellence.downloader;

import static com.excellence.downloader.entity.TaskEntity.STATUS_DOWNLOADING;
import static com.excellence.downloader.entity.TaskEntity.STATUS_PAUSE;
import static com.excellence.downloader.entity.TaskEntity.STATUS_SUCCESS;
import static com.excellence.downloader.entity.TaskEntity.STATUS_WAITING;
import static com.excellence.downloader.utils.CommonUtil.checkNULL;
import static com.excellence.downloader.utils.CommonUtil.deleteFile;

import java.io.File;
import java.util.LinkedList;
import java.util.concurrent.Executor;

import com.excellence.downloader.entity.TaskEntity;
import com.excellence.downloader.exception.DownloadError;
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

	private Executor mResponsePoster = null;
	private final LinkedList<DownloadTask> mTaskQueue;
	private int mParallelTaskCount;
	private int mThreadCount;

	public FileDownloader(int parallelTaskCount, int threadCount)
	{
		final Handler handler = new Handler(Looper.getMainLooper());
		mResponsePoster = new Executor()
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
		DownloadTask task = new DownloadTask(storeFile, url, listener);
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

	private synchronized void remove(DownloadTask task)
	{
		mTaskQueue.remove(task);
		schedule();
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

	public class DownloadTask
	{
		private TaskEntity mTaskEntity = null;
		private DownloadRequest mRequest = null;

		public DownloadTask(File storeFile, String url, final IListener listener)
		{
			TaskEntity taskEntity = new TaskEntity();
			taskEntity.storeFile = storeFile;
			taskEntity.url = url;
			taskEntity.threadCount = mThreadCount;
			mTaskEntity = taskEntity;
			mRequest = new DownloadRequest(taskEntity, mResponsePoster, new IListener()
			{
				@Override
				public void onPreExecute(long fileSize)
				{
					if (listener != null)
						listener.onPreExecute(fileSize);
				}

				@Override
				public void onProgressChange(long fileSize, long downloadedSize)
				{
					if (listener != null)
						listener.onProgressChange(fileSize, downloadedSize);
				}

				@Override
				public void onCancel()
				{
					if (listener != null)
						listener.onCancel();
				}

				@Override
				public void onError(DownloadError error)
				{
					mTaskEntity.setStatus(STATUS_SUCCESS);
					remove(DownloadTask.this);
					if (listener != null)
						listener.onError(error);
				}

				@Override
				public void onSuccess()
				{
					mTaskEntity.setStatus(STATUS_SUCCESS);
					remove(DownloadTask.this);
					if (listener != null)
						listener.onSuccess();
				}
			});
		}

		private boolean deploy()
		{
			// only wait task can deploy
			if (mTaskEntity.status != STATUS_WAITING)
				return false;
			mTaskEntity.deploy();
			mRequest.execute();
			return true;
		}

		public boolean isDownloading()
		{
			return mTaskEntity.isDownloading();
		}

		public void cancel()
		{
			mTaskEntity.discard();
			mRequest.cancel();
			remove(this);
			deleteFile(mTaskEntity.storeFile);
			deleteFile(mTaskEntity.tempFile);
		}

		public boolean pause()
		{
			switch (mTaskEntity.status)
			{
			case STATUS_DOWNLOADING:
			case STATUS_WAITING:
				mTaskEntity.cancel();
				schedule();
				return true;
			}
			return false;
		}

		public boolean resume()
		{
			switch (mTaskEntity.status)
			{
			case STATUS_PAUSE:
				mTaskEntity.setStatus(STATUS_WAITING);
				schedule();
				return true;
			}
			return false;
		}

		public int getStatus()
		{
			return mTaskEntity.status;
		}

		public boolean check(File storeFile, String url)
		{
			return mTaskEntity.storeFile == storeFile && mTaskEntity.url.equals(url);
		}
	}
}
