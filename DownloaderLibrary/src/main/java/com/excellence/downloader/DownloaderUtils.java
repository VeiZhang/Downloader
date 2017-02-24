package com.excellence.downloader;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by ZhangWei on 2017/2/15.
 */

/**
 * 下载器工具
 * 权限
 *     {@link android.Manifest.permission.INTERNET}
 *     {@link android.Manifest.permission.WRITE_EXTERNAL_STORAGE}
 *     {@link android.Manifest.permission.READ_EXTERNAL_STORAGE}
 *
 */
public class DownloaderUtils
{
	private static final String TAG = DownloaderUtils.class.getSimpleName();

	private static DownloaderUtils mInstance = null;
	private List<FileDownloader> mDownloaderList = null;
	private ExecutorService mExecutorService = null;

	private DownloaderUtils()
	{

	}

	public static void init(int parallelTaskCount)
	{
		if (parallelTaskCount >= Runtime.getRuntime().availableProcessors())
		{
			Log.w(TAG, "parallelTaskCount is beyond!!!");
			parallelTaskCount = Runtime.getRuntime().availableProcessors() - 1;
			if (parallelTaskCount == 0)
				parallelTaskCount = 1;
		}
		mInstance = new DownloaderUtils();
		mInstance.mDownloaderList = new ArrayList<>();
		mInstance.mExecutorService = Executors.newFixedThreadPool(parallelTaskCount);
	}

	public static void destroy()
	{
		for (FileDownloader task : mInstance.mDownloaderList)
		{
			task.setPause(true);
		}
	}

	public static List<FileDownloader> getDownloaderList()
	{
		if (mInstance.mDownloaderList != null)
			return mInstance.mDownloaderList;
		else
			throw new IllegalStateException("DownloaderList not initialized");
	}

	public static FileDownloader addTask(Context context, File storeFile, String url, DownloaderListener listener)
	{
		final FileDownloader fileDownloader = new FileDownloader(context, storeFile, url, listener);
		mInstance.mExecutorService.execute(new Runnable()
		{
			@Override
			public void run()
			{
				fileDownloader.deploy();
			}
		});
		return fileDownloader;
	}

	public FileDownloader addTask(Context context, String storeFilePath, String url,DownloaderListener listener)
	{
		return addTask(context, new File(storeFilePath), url, listener);
	}
}
