package com.excellence.downloader.exception;

/**
 * Created by ZhangWei on 2017/3/1.
 */

/**
 * 下载异常类
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
