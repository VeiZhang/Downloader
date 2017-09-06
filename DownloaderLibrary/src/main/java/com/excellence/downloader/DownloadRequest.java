package com.excellence.downloader;

import android.support.annotation.NonNull;

import com.excellence.downloader.entity.TaskEntity;
import com.excellence.downloader.exception.DownloadError;
import com.excellence.downloader.utils.IListener;
import com.excellence.downloader.utils.OnFileInfoCallback;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     time   : 2017/8/11
 *     desc   : 下载请求
 * </pre>
 */

public class DownloadRequest
{
	private TaskEntity mTaskEntity = null;
	private ExecutorService mExecutor = null;
	private HttpDownloadTask mHttpDownloadTask = null;

	public DownloadRequest(@NonNull TaskEntity taskEntity, Executor responsePoster, IListener listener)
	{
		mTaskEntity = taskEntity;
		mExecutor = Executors.newSingleThreadExecutor();
		mHttpDownloadTask = new HttpDownloadTask(responsePoster, mTaskEntity, listener);
	}

	public void execute()
	{
		mExecutor.execute(createFileInfoTask());
	}

	public HttpFileInfoTask createFileInfoTask()
	{
		return new HttpFileInfoTask(mTaskEntity, new OnFileInfoCallback()
		{

			@Override
			public void onComplete()
			{
				mHttpDownloadTask.onPreExecute(mTaskEntity.fileSize);
				File storeFile = mTaskEntity.storeFile;
				if (storeFile.exists() && storeFile.length() == mTaskEntity.fileSize)
				{
					mTaskEntity.downloadLen = mTaskEntity.fileSize;
					mHttpDownloadTask.onProgressChange(mTaskEntity.downloadLen, mTaskEntity.fileSize);
					mHttpDownloadTask.onSuccess();
					return;
				}
				mExecutor.execute(mHttpDownloadTask);
			}

			@Override
			public void onCancel()
			{
				mHttpDownloadTask.onCancel();
			}

			@Override
			public void onError(DownloadError error)
			{
				mHttpDownloadTask.onError(error);
			}

		});
	}

	public void cancel()
	{
		mExecutor.shutdown();
	}
}
