package com.excellence.downloader;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.excellence.downloader.FileDownloader.DownloadTask;
import com.excellence.downloader.scheduler.DownloadScheduler;
import com.excellence.downloader.utils.IListener;

import java.io.File;
import java.util.LinkedList;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

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

	private static Downloader mInstace = null;
	private FileDownloader mFileDownloader = null;

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
		if (mInstace != null)
		{
			Log.w(TAG, "Downloader initialized!!!");
			return;
		}

		if (parallelTaskCount >= Runtime.getRuntime().availableProcessors())
		{
			Log.w(TAG, "ParallelTaskCount is beyond!!!");
			parallelTaskCount = Runtime.getRuntime().availableProcessors() == 1 ? 1 : Runtime.getRuntime().availableProcessors() - 1;
		}
		mInstace = new Downloader();
		mInstace.mFileDownloader = new FileDownloader(parallelTaskCount, threadCount);
		checkPermission(context);
	}

	/**
	 * Android6.0以后动态申请文件读写权限
	 * 
	 * @param context
	 */
	private static void checkPermission(Context context)
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
		{
			String[] PERMISSIONS_STORAGE = { READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE };
			int permission = ActivityCompat.checkSelfPermission(context, WRITE_EXTERNAL_STORAGE);
			if (permission != PERMISSION_GRANTED)
			{
				if (context instanceof Activity)
					ActivityCompat.requestPermissions((Activity) context, PERMISSIONS_STORAGE, 1);
				else
					throw new RuntimeException("Context should be activity for Android 6.0 or later to request permission!!!");
			}
		}
	}

	/**
	 * 新建下载任务
	 *
	 * @param storeFile 文件
	 * @param url 下载链接
	 * @param listener
	 * @return 下载任务
	 */
	public static DownloadTask addTask(@NonNull File storeFile, @NonNull String url, IListener listener)
	{
		checkDownloader();
		return mInstace.mFileDownloader.addTask(storeFile, url, listener);
	}

	/**
	 * 新建下载任务
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
		return mInstace.mFileDownloader.get(storeFile, url);
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
		return mInstace.mFileDownloader.get(filePath, url);
	}

	/**
	 * 获取下载队列
	 *
	 * @return
	 */
	public static LinkedList<DownloadTask> getTaskQueue()
	{
		checkDownloader();
		return mInstace.mFileDownloader.getTaskQueue();
	}

	private static void checkDownloader()
	{
		if (mInstace == null)
			throw new RuntimeException("Downloader not initialized!!!");
	}

	/**
	 * 关闭下载器
	 */
	public static void destroy()
	{
		if (mInstace.mFileDownloader != null)
			mInstace.mFileDownloader.clearAll();
	}

	public static void register(Object obj)
	{
		DownloadScheduler.getInstance().register(obj);
	}
}
