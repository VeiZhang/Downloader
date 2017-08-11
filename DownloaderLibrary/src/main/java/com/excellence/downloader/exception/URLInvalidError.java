package com.excellence.downloader.exception;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     time   : 2017/8/10
 *     desc   : 下载链接异常
 * </pre>
 */

public class URLInvalidError extends DownloadError
{
	public URLInvalidError()
	{
	}

	public URLInvalidError(String message)
	{
		super(message);
	}

	public URLInvalidError(String message, Throwable cause)
	{
		super(message, cause);
	}

	public URLInvalidError(Throwable cause)
	{
		super(cause);
	}
}
