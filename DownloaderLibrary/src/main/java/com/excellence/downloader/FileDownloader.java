package com.excellence.downloader;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

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

public class FileDownloader
{
	public static final String TAG = FileDownloader.class.getSimpleName();

	private Executor mResponsePoster = null;
	private final LinkedList<DownloadTask> mTaskQueue;
	private DownloadScheduler<DownloadTask> mDownloadScheduler = null;
	private int mParallelTaskCount;
	private int mThreadCount;

	public FileDownloader(DownloadScheduler<DownloadTask> downloadScheduler, int parallelTaskCount, int threadCount)
	{
		mDownloadScheduler = downloadScheduler;
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

	/**
	 * 新建下载任务
	 *
	 * @param storeFile 保存文件
	 * @param url 下载链接
	 * @param listener
	 * @return
	 */
	public DownloadTask addTask(File storeFile, String url, IListener listener)
	{
		DownloadTask task = get(storeFile, url);
		if (task != null)
		{
			task.resume();
		}
		else
		{
			task = new DownloadTask(storeFile, url, listener);
			synchronized (mTaskQueue)
			{
				mTaskQueue.add(task);
			}
			schedule();
		}
		return task;
	}

	/**
	 * 新建下载任务
	 *
	 * @param filePath 保存路径
	 * @param url 下载链接
	 * @param listener
	 * @return
	 */
	public DownloadTask addTask(String filePath, String url, IListener listener)
	{
		return addTask(new File(filePath), url, listener);
	}

	/**
	 * 刷新任务队列
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

	/**
	 * 关闭所有下载任务
	 */
	public synchronized void clearAll()
	{
		while (!mTaskQueue.isEmpty())
		{
			mTaskQueue.get(0).cancel();
		}
	}

	/**
	 * 获取下载任务
	 *
	 * @param storeFile 保存文件
	 * @param url 下载链接
	 * @return
	 */
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

	/**
	 * 获取下载任务
	 *
	 * @param filePath 保存路径
	 * @param url 下载链接
	 * @return
	 */
	public DownloadTask get(String filePath, String url)
	{
		return get(new File(filePath), url);
	}

	/**
	 * 任务列表
	 *
	 * @return
	 */
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
					mDownloadScheduler.onPreExecute(DownloadTask.this);
					if (listener != null)
						listener.onPreExecute(fileSize);
				}

				@Override
				public void onProgressChange(long fileSize, long downloadedSize)
				{
					mDownloadScheduler.onProgressChange(DownloadTask.this);
					if (listener != null)
						listener.onProgressChange(fileSize, downloadedSize);
				}

				@Override
				public void onProgressChange(long fileSize, long downloadedSize, long speed)
				{
					mDownloadScheduler.onProgressSpeedChange(DownloadTask.this);
					if (listener != null)
						listener.onProgressChange(fileSize, downloadedSize, speed);
				}

				@Override
				public void onCancel()
				{
					mDownloadScheduler.onCancel(DownloadTask.this);
					if (listener != null)
						listener.onCancel();
				}

				@Override
				public void onError(DownloadError error)
				{
					mTaskEntity.setStatus(STATUS_ERROR);
					mDownloadScheduler.onError(DownloadTask.this);
					if (listener != null)
						listener.onError(error);
					schedule();
				}

				@Override
				public void onSuccess()
				{
					mTaskEntity.setStatus(STATUS_SUCCESS);
					mDownloadScheduler.onSuccess(DownloadTask.this);
					if (listener != null)
						listener.onSuccess();
					remove(DownloadTask.this);
				}
			});
		}

		/**
		 * 开始任务
		 *
		 * @return
		 */
		private boolean deploy()
		{
			// only wait task can deploy
			if (mTaskEntity.status != STATUS_WAITING)
				return false;
			mTaskEntity.deploy();
			mRequest.execute();
			return true;
		}

		/**
		 * 是否正在下载
		 *
		 * @return
		 */
		public boolean isDownloading()
		{
			return mTaskEntity.isDownloading();
		}

		private void cancel()
		{
			mTaskEntity.discard();
			mRequest.cancel();
			mTaskQueue.remove(this);
		}

		/**
		 * 完全删除任务
		 */
		public void discard()
		{
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

		/**
		 * 继续任务
		 *
		 * @return
		 */
		public boolean resume()
		{
			switch (mTaskEntity.status)
			{
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
		public int getStatus()
		{
			return mTaskEntity.status;
		}

		/**
		 * 检验是否是当前任务
		 *
		 * @param storeFile 保存文件
		 * @param url 下载链接
		 * @return
		 */
		public boolean check(File storeFile, String url)
		{
			return mTaskEntity.storeFile.equals(storeFile) && mTaskEntity.url.equals(url);
		}

		/**
		 * 获取下载长度
		 *
		 * @return
		 */
		public long getDownloadLength()
		{
			return mTaskEntity.downloadLen;
		}

		/**
		 * 获取文件长度
		 *
		 * @return
		 */
		public long getFileSize()
		{
			return mTaskEntity.fileSize;
		}

		/**
		 * 获取下载速度:byte/s
		 * 
		 * @return
		 */
		public long getDownloadSpeed()
		{
			return mTaskEntity.downloadSpeed;
		}

		/**
		 * 获取下载链接
		 *
		 * @return
		 */
		public String getUrl()
		{
			return mTaskEntity.url;
		}

		/**
		 * 获取下载标识
		 * 
		 * @return
		 */
		public String getKey()
		{
			return mTaskEntity.url;
		}
	}
}
