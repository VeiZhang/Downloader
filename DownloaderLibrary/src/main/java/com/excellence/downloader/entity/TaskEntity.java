package com.excellence.downloader.entity;

import java.io.File;

import android.support.annotation.IntRange;

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
	 * 下载取消标记
	 */
	public boolean isCancel = false;

	/**
	 * 取消
	 */
	public void cancel()
	{
		isCancel = true;
		status = STATUS_PAUSE;
	}

	public void discard()
	{
		isCancel = true;
		status = STATUS_DISCARD;
	}

	/**
	 * 继续
	 */
	public void deploy()
	{
		isCancel = false;
		status = STATUS_DOWNLOADING;
	}

	/**
	 * 是否正在下载
	 * 
	 * @return
	 */
	public boolean isDownloading()
	{
		return status == STATUS_DOWNLOADING;
	}

	/**
	 * 设置下载状态
	 *
	 * @param status 下载状态
	 *               @see #STATUS_WAITING
	 *               @see #STATUS_DOWNLOADING
	 *               @see #STATUS_PAUSE
	 *               @see #STATUS_SUCCESS
	 *               @see #STATUS_DISCARD
	 */
	public void setStatus(@IntRange(from = STATUS_WAITING, to = STATUS_DISCARD) int status)
	{
		this.status = status;
	}

}
