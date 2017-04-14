package com.excellence.downloader.exception;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : https://veizhang.github.io/
 *     time   : 2017/3/1
 *     desc   : 服务器异常类
 * </pre>
 */

public class ServerConnectException extends DownloadError
{
	public ServerConnectException()
	{
		super("Failed to connect server.");
	}

	public ServerConnectException(String detailMessage)
	{
		super(detailMessage);
	}

	public ServerConnectException(int responseCode)
	{
		super(String.format("Failed to connect server, bad code : %1$d.", responseCode));
	}
}
