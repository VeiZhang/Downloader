package com.excellence.downloader;

import static com.excellence.downloader.entity.TaskEntity.STATUS_WAITING;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.excellence.downloader.entity.TaskEntity;
import com.excellence.downloader.exception.DownloadError;
import com.excellence.downloader.utils.IListener;
import com.excellence.downloader.utils.OnFileInfoCallback;

import android.support.annotation.NonNull;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     time   : 2017/8/9
 *     desc   : 下载任务
 * </pre>
 */

public class DownloadTask implements IListener
{
	public static final String TAG = DownloadTask.class.getSimpleName();

	private TaskEntity mTaskEntity = null;
	private Executor mResponsePoster = null;
	private IListener mListener = null;
	private ExecutorService mExecutor = null;
	private HttpDownloadTask mHttpDownloadTask = null;

	public DownloadTask(@NonNull TaskEntity taskEntity, Executor responsePoster, IListener listener)
	{
		mTaskEntity = taskEntity;
		mResponsePoster = responsePoster;
		mListener = listener;
		mExecutor = Executors.newSingleThreadExecutor();
		mHttpDownloadTask = new HttpDownloadTask(mTaskEntity, this);
	}

	public boolean deploy()
	{
		// only wait task can deploy
		if (mTaskEntity.status != STATUS_WAITING)
			return false;
		mExecutor.execute(createFileInfoTask());
		return true;
	}

	private Runnable createFileInfoTask()
	{
		return new HttpFileInfoTask(mTaskEntity, new OnFileInfoCallback()
		{

			@Override
			public void onComplete()
			{
				mExecutor.execute(mHttpDownloadTask);
			}

			@Override
			public void onError(DownloadError error)
			{
				mHttpDownloadTask.onError(error);
			}

		});
	}

	public boolean isDownloading()
	{
		return mTaskEntity.isDownloading();
	}

	public void cancel()
	{
		mExecutor.shutdown();
	}

	public void pause()
	{

	}

	public void resume()
	{

	}

	public int getStatus()
	{
		return mTaskEntity.status;
	}

	public boolean check(File storeFile, String url)
	{
		return mTaskEntity.storeFile == storeFile && mTaskEntity.url.equals(url);
	}

	@Override
	public void onPreExecute(final long fileSize)
	{
		mResponsePoster.execute(new Runnable()
		{
			@Override
			public void run()
			{
				if (mListener != null)
					mListener.onPreExecute(fileSize);
			}
		});
	}

	@Override
	public void onProgressChange(final long fileSize, final long downloadedSize)
	{
		mResponsePoster.execute(new Runnable()
		{
			@Override
			public void run()
			{
				if (mListener != null)
					mListener.onProgressChange(fileSize, downloadedSize);
			}
		});
	}

	@Override
	public void onCancel()
	{
		mResponsePoster.execute(new Runnable()
		{
			@Override
			public void run()
			{
				if (mListener != null)
					mListener.onCancel();
			}
		});
	}

	@Override
	public void onError(final DownloadError error)
	{
		mResponsePoster.execute(new Runnable()
		{
			@Override
			public void run()
			{
				if (mListener != null)
					mListener.onError(error);
			}
		});
	}

	@Override
	public void onSuccess()
	{
		mResponsePoster.execute(new Runnable()
		{
			@Override
			public void run()
			{
				if (mListener != null)
					mListener.onSuccess();
			}
		});
	}

}
