package com.excellence.downloader.utils;

import com.excellence.downloader.exception.DownloadError;

/**
 * Created by ZhangWei on 2017/2/16.
 */

public class DownloaderListener implements IDownloaderListener
{

	@Override
	public void onPreExecute(long fileSize)
	{

	}

	@Override
	public void onProgressChange(long fileSize, long downloadedSize)
	{

	}

	@Override
	public void onCancel()
	{

	}

	@Override
	public void onError(DownloadError error)
	{

	}

	@Override
	public void onSuccess()
	{

	}

}
