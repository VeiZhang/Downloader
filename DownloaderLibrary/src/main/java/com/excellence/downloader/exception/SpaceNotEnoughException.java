package com.excellence.downloader.exception;

/**
 * Created by ZhangWei on 2017/3/1.
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
