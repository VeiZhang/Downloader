package com.excellence.downloader.utils;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : https://veizhang.github.io/
 *     time   : 2017/2/16
 *     desc   : 监听接口
 * </pre>
 */

public interface IDownloaderListener
{
	/**
	 * 准备下载
	 *
	 * @param fileSize 下载文件长度
	 */
	void onPreExecute(long fileSize);

	/**
	 * 下载中进度刷新
	 *
	 * @param fileSize 文件长度
	 * @param downloadedSize 下载长度
	 */
	void onProgressChange(long fileSize, long downloadedSize);

	/**
	 * 暂停下载
	 */
	void onCancel();

	/**
	 * 下载失败
	 */
	void onError(Exception error);

	/**
	 * 下载成功
	 */
	void onSuccess();

}
