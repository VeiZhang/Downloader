package com.excellence.downloader.exception;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : https://veizhang.github.io/
 *     time   : 2017/3/1
 *     desc   : 下载异常类
 * </pre>
 */

public class DownloadError extends Exception
{
	public DownloadError()
	{
		super();
	}

	public DownloadError(String detailMessage)
	{
		super(detailMessage);
	}

	public DownloadError(String detailMessage, Throwable throwable)
	{
		super(detailMessage, throwable);
	}

	public DownloadError(Throwable throwable)
	{
		super(throwable);
	}

}
