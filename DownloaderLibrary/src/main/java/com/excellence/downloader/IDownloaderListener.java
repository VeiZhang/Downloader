package com.excellence.downloader;

/**
 * Created by ZhangWei on 2017/2/16.
 */
public interface IDownloaderListener
{

	void onDownloadStartListener(String filename, int fileLength);

	void onDownloadingListener(String filename, long downloadedLength);

	void onDownloadFinishListener(String filename);

	void onDownloadFailListener(String filename, int result);

}
