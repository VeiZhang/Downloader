package com.excellence.downloader.exception;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : https://veizhang.github.io/
 *     time   : 2017/3/1
 *     desc   : 储存空间异常类
 * </pre>
 */

public class SpaceNotEnoughException extends DownloadError
{
	public SpaceNotEnoughException()
	{
		super("Download Space is not Enough.");
	}

	public SpaceNotEnoughException(String detailMessage)
	{
		super(detailMessage);
	}
}
