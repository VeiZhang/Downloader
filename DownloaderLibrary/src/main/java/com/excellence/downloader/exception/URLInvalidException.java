package com.excellence.downloader.exception;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : https://veizhang.github.io/
 *     time   : 2017/3/1
 *     desc   : URL地址异常类
 * </pre>
 */

public class URLInvalidException extends DownloadError
{
	public URLInvalidException()
	{
		super("Download URL is empty or invalid.");
	}

	public URLInvalidException(String detailMessage)
	{
		super(detailMessage);
	}

}
