package com.excellence.downloader.entity;

import java.io.File;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     time   : 2017/8/10
 *     desc   : 下载任务配置
 * </pre>
 */

public class TaskEntity
{
	public static final int STATUS_WAITING = 0;
	public static final int STATUS_DOWNLOADING = 1;
	public static final int STATUS_PAUSE = 2;
	public static final int STATUS_SUCCESS = 3;
	public static final int STATUS_DISCARD = 4;

	public File storeFile = null;

	/**
	 * 下载链接
	 */
	public String url = null;

	/**
	 * 是否支持断点
	 * {@code true}:支持<br>{@code false}:不支持
	 */
	public boolean isSupportBP = true;

	/**
	 * 下载线程数
	 */
	public int threadCount;

	/**
	 * 文件大小
	 */
	public long fileSize;

	/**
	 * 下载长度
	 */
	public long downloadLen;

	/**
	 * 状态码
	 */
	public int code;

	/**
	 * 下载状态
	 */
	public int status = STATUS_WAITING;

	/**
	 * 是否正在下载
	 * 
	 * @return
	 */
	public boolean isDownloading()
	{
		return status == STATUS_DOWNLOADING;
	}
}
