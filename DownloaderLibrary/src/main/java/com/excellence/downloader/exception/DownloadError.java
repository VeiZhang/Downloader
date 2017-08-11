package com.excellence.downloader.exception;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     time   : 2017/8/10
 *     desc   : 下载异常
 * </pre>
 */

public class DownloadError extends Exception
{
	public DownloadError()
	{
	}

	public DownloadError(String message)
	{
		super(message);
	}

	public DownloadError(String message, Throwable cause)
	{
		super(message, cause);
	}

	public DownloadError(Throwable cause)
	{
		super(cause);
	}

}
