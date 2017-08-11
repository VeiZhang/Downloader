package com.excellence.downloader;

import java.io.File;

import com.excellence.downloader.utils.IListener;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     time   : 2017/8/9
 *     desc   : 下载任务
 * </pre>
 */

public class DownloadTask
{
	private static final int STATUS_WAITING = 0;
	private static final int STATUS_DOWNLOADING = 1;
	private static final int STATUS_PAUSE = 2;
	private static final int STATUS_SUCCESS = 3;
	private static final int STATUS_DISCARD = 4;

	private int mStatus = STATUS_WAITING;
	private File mStoreFile = null;
	private String mUrl = null;

	public DownloadTask(File storeFile, String url, IListener listener)
	{
		mStoreFile = storeFile;
		mUrl = url;
	}

	public boolean deploy()
	{
		// only wait task can deploy
		if (mStatus != STATUS_WAITING)
			return false;

		return true;
	}

	public boolean isDownloading()
	{
		return mStatus == STATUS_DOWNLOADING;
	}

	public boolean check(File storeFile, String url)
	{
		return mStoreFile == storeFile && mUrl.equals(url);
	}
}
