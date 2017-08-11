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
	public File storeFile = null;

	public String url = null;

	public int threadCount = 0;

	/**
	 * 是否支持断点
	 * {@code true}:支持<br>{@code false}:不支持
	 */
	public boolean isSupportBP = true;

}
