package com.excellence.downloader.exception;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     time   : 2017/8/10
 *     desc   : 文件信息异常
 * </pre>
 */

public class FileError extends DownloadError
{
	public FileError()
	{
		super();
	}

	public FileError(String message)
	{
		super(message);
	}

	public FileError(String message, Throwable cause)
	{
		super(message, cause);
	}

	public FileError(Throwable cause)
	{
		super(cause);
	}
}
