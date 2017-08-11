package com.excellence.downloader.utils;

import com.excellence.downloader.exception.DownloadError;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     time   : 2017/8/9
 *     desc   :
 * </pre>
 */

public class Listener implements IListener
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
	public void onProgressChange(long fileSize, long downloadedSize, long speed)
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
