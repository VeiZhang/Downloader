package com.excellence.compiler;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     time   : 2017/8/25
 *     desc   : 扫描器常量
 * </pre>
 */

public interface ProxyConstance
{
	/**
	 * 设置观察者的方法
	 */
	String SET_LISTENER = "setListener";

	/**
	 * 代理配置类
	 */
	String PROXY_COUNTER_PACKAGE = "com.excellence.downloader";

	/**
	 * 代理分类统计
	 */
	String PROXY_COUNTER_NAME = "ProxyClassCounter";

	/**
	 * 代理分类统计映射表
	 */
	String COUNT_DOWNLOAD = "download";

	String PROXY_COUNTER_MAP = "typeMapping";

	String COUNT_METHOD_DOWNLOAD = "getDownloadCounter";

	/**
	 * Downloader类名、包名、方法名、代理后缀名
	 */
	String PKG_FILEDOWNLOADER = "com.excellence.downloader.FileDownloader";
	String CLS_DOWNLOADER_TASK = "DownloadTask";
	String PROXY_SUFFIX_DOWNLOAD = "$$DownloadListenerProxy";
	String LISTENER_KEY_MAP = "keyMapping";

	String PKG_SCHEDULER = "com.excellence.downloader.scheduler";
	String CLS_SCHEDULER_LISTENER = "SchedulerListener";

	int PRE_EXECUTE = 0x11;
	int PROGRESS_CHANGE = 0x12;
	int PROGRESS_SPEED_CHANGE = 0x13;
	int CANCEL = 0x14;
	int ERROR = 0x15;
	int SUCCESS = 0x16;

}
