package com.excellence.downloader;

import android.content.Context;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.util.Log;

import com.excellence.downloader.FileDownloader.DownloadTask;
import com.excellence.downloader.scheduler.DownloadScheduler;
import com.excellence.downloader.utils.IListener;

import java.io.File;
import java.util.LinkedList;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     time   : 2017/8/9
 *     desc   : 下载器初始化
 *              权限
 *                  {@link android.Manifest.permission#INTERNET}
 *                  {@link android.Manifest.permission#WRITE_EXTERNAL_STORAGE}
 *                  {@link android.Manifest.permission#READ_EXTERNAL_STORAGE}
 * </pre>
 */

public class Downloader
{
	public static final String TAG = Downloader.class.getSimpleName();

	public static final int DEFAULT_TASK_COUNT = 2;
	public static final int DEFAULT_THREAD_COUNT = 1;

	private static Downloader mInstance = null;
	private FileDownloader mFileDownloader = null;
	private DownloadScheduler<DownloadTask> mDownloadScheduler = null;

	private Downloader()
	{

	}

	/**
	 * 初始化，默认任务数:2，单线程下载
	 *
	 * @param context
	 */
	public static void init(@NonNull Context context)
	{
		init(context, DEFAULT_TASK_COUNT, DEFAULT_THREAD_COUNT);
	}

	/**
	 * 初始化，设置任务数，单个任务下载线程数
	 *
	 * @param context     上下文
	 * @param parallelTaskCount   任务数
	 * @param threadCount 单个任务下载线程数
	 */
	public static void init(@NonNull Context context, @IntRange(from = 1) int parallelTaskCount, @IntRange(from = 1) int threadCount)
	{
		if (mInstance != null)
		{
			Log.w(TAG, "Downloader initialized!!!");
			return;
		}

		if (parallelTaskCount >= Runtime.getRuntime().availableProcessors())
		{
			Log.w(TAG, "ParallelTaskCount is beyond!!!");
			parallelTaskCount = Runtime.getRuntime().availableProcessors() == 1 ? 1 : Runtime.getRuntime().availableProcessors() - 1;
		}
		mInstance = new Downloader();
		mInstance.mDownloadScheduler = new DownloadScheduler<>();
		mInstance.mFileDownloader = new FileDownloader(mInstance.mDownloadScheduler, parallelTaskCount, threadCount);
	}

	/**
	 * 新建下载任务，搭配注解方式监听
	 *
	 * @param storeFile
	 * @param url
	 * @return
	 */
	public static DownloadTask addTask(@NonNull File storeFile, @NonNull String url)
	{
		return addTask(storeFile, url, null);
	}

	/**
	 * 新建下载任务，搭配注解方式监听
	 *
	 * @param filePath
	 * @param url
	 * @return
	 */
	public static DownloadTask addTask(@NonNull String filePath, @NonNull String url)
	{
		return addTask(filePath, url, null);
	}

	/**
	 * 新建下载任务，推荐注解方式{@link #addTask(File, String)}、{@link #register(Object)}
	 *
	 * @param storeFile 文件
	 * @param url 下载链接
	 * @param listener
	 * @return 下载任务
	 */
	public static DownloadTask addTask(@NonNull File storeFile, @NonNull String url, IListener listener)
	{
		checkDownloader();
		return mInstance.mFileDownloader.addTask(storeFile, url, listener);
	}

	/**
	 * 新建下载任务，推荐注解方式{@link #addTask(String, String)}、{@link #register(Object)}
	 *
	 * @param filePath 文件路径
	 * @param url 下载链接
	 * @param listener
	 * @return 下载任务
	 */
	public static DownloadTask addTask(@NonNull String filePath, @NonNull String url, IListener listener)
	{
		return addTask(new File(filePath), url, listener);
	}

	/**
	 * 获取下载任务
	 *
	 * @param storeFile 保存文件
	 * @param url 下载链接
	 * @return
	 */
	public static DownloadTask get(File storeFile, String url)
	{
		checkDownloader();
		return mInstance.mFileDownloader.get(storeFile, url);
	}

	/**
	 * 获取下载任务
	 *
	 * @param filePath 保存路径
	 * @param url 下载链接
	 * @return
	 */
	public static DownloadTask get(String filePath, String url)
	{
		checkDownloader();
		return mInstance.mFileDownloader.get(filePath, url);
	}

	/**
	 * 获取下载队列
	 *
	 * @return
	 */
	public static LinkedList<DownloadTask> getTaskQueue()
	{
		checkDownloader();
		return mInstance.mFileDownloader.getTaskQueue();
	}

	private static void checkDownloader()
	{
		if (mInstance == null)
			throw new RuntimeException("Downloader not initialized!!!");
	}

	/**
	 * 关闭下载器
	 */
	public static void destroy()
	{
		if (mInstance.mFileDownloader != null)
			mInstance.mFileDownloader.clearAll();
	}

	/**
	 * 注册，创建监听
	 *
	 * @param obj
	 */
	public static void register(Object obj)
	{
		checkDownloader();
		mInstance.mDownloadScheduler.register(obj);
	}

	/**
	 * 解绑
	 *
	 * @param obj
	 */
	public static void unregister(Object obj)
	{
		checkDownloader();
		mInstance.mDownloadScheduler.unregister(obj);
	}
}
