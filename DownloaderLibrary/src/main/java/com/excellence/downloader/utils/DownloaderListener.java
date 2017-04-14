package com.excellence.downloader.utils;

import com.excellence.downloader.exception.DownloadError;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : https://veizhang.github.io/
 *     time   : 2017/2/16
 *     desc   : 监听接口实现
 * </pre>
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
