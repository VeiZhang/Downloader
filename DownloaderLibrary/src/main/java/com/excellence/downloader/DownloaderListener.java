package com.excellence.downloader;

/**
 * Created by ZhangWei on 2017/2/16.
 */

public class DownloaderListener implements IDownloaderListener
{
	@Override
	public void onDownloadStartListener(String filename, int fileLength)
	{

	}

	@Override
	public void onDownloadingListener(String filename, long downloadedLength)
	{

	}

	@Override
	public void onDownloadFinishListener(String filename)
	{

	}

	@Override
	public void onDownloadFailListener(String filename, int result)
	{

	}
}
