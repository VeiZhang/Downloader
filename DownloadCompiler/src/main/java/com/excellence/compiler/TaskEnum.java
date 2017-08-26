package com.excellence.compiler;

import static com.excellence.compiler.ProxyConstance.CLS_DOWNLOADER_TASK;
import static com.excellence.compiler.ProxyConstance.PKG_FILEDOWNLOADER;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     time   : 2017/8/25
 *     desc   : 任务类型枚举
 * </pre>
 */

public enum TaskEnum
{
	DOWNLOAD(PKG_FILEDOWNLOADER, CLS_DOWNLOADER_TASK, "$$DownloadListenerProxy");

	private String pkg;
	private String className;
	private String proxySuffix;

	/**
	 *
	 * @param pkg 包名
	 * @param className 任务完整类名
	 * @param proxySuffix 事件代理后缀
	 */
	TaskEnum(String pkg, String className, String proxySuffix)
	{
		this.pkg = pkg;
		this.className = className;
		this.proxySuffix = proxySuffix;
	}

	public String getPkg()
	{
		return pkg;
	}

	public String getClassName()
	{
		return className;
	}

	public String getProxySuffix()
	{
		return proxySuffix;
	}
}
