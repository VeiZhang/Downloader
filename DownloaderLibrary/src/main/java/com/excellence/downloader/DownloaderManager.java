package com.excellence.downloader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.excellence.downloader.utils.DownloaderListener;
import com.excellence.downloader.utils.IDownloaderListener;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : https://veizhang.github.io/
 *     time   : 2017/2/15
 *     desc   : 下载管理器
 *     			权限
 *     				{@link android.Manifest.permission#INTERNET				 }
 *     				{@link android.Manifest.permission#WRITE_EXTERNAL_STORAGE}
 *     				{@link android.Manifest.permission#READ_EXTERNAL_STORAGE }
 * </pre>
 */

public class DownloaderManager
{
	private static final String TAG = DownloaderManager.class.getSimpleName();

	private static DownloaderManager mInstance = null;
	private List<FileDownloader> mDownloaderList = null;
	private ExecutorService mExecutorService = null;
	private Executor mResponsePoster = null;
	private Context mContext = null;

	private DownloaderManager(Context context)
	{
		mContext = context;
	}

	/**
	 * 初始化下载器
	 *
	 * @param context 上下文
	 * @param parallelTaskCount 最大任务数，最小1个任务
	 */
	public static void init(@NonNull Context context, @IntRange(from = 1) int parallelTaskCount)
	{
		if (parallelTaskCount >= Runtime.getRuntime().availableProcessors())
		{
			Log.w(TAG, "parallelTaskCount is beyond!!!");
			parallelTaskCount = Runtime.getRuntime().availableProcessors() - 1;
		}
		mInstance = new DownloaderManager(context.getApplicationContext());
		mInstance.mDownloaderList = new ArrayList<>();
		mInstance.mExecutorService = Executors.newFixedThreadPool(parallelTaskCount);
		final Handler handler = new Handler(Looper.getMainLooper());
		mInstance.mResponsePoster = new Executor()
		{
			@Override
			public void execute(Runnable command)
			{
				handler.post(command);
			}
		};
	}

	/**
	 * 暂停所有任务
	 *
	 * @param context 上下文
	 */
	public static void destroy(@NonNull Context context)
	{
		for (FileDownloader task : mInstance.mDownloaderList)
		{
			if (task != null)
				task.pause();
		}
		DBHelper.getInstance(context).closeDB();
	}

	/**
	 * 任务队列
	 *
	 * @return 任务队列
	 */
	public static List<FileDownloader> getDownloaderList()
	{
		if (mInstance.mDownloaderList != null)
			return mInstance.mDownloaderList;
		else
			throw new IllegalStateException("DownloaderList not initialized.");
	}

	/**
	 * 新建下载任务
	 *
	 * @param storeFile File类型
	 * @param url 下载链接
	 * @param listener 监听器
	 * @return
	 */
	public static FileDownloader addTask(File storeFile, String url, IDownloaderListener listener)
	{
		return addTask(new FileDownloader(mInstance.mContext, storeFile, url, listener, mInstance.mResponsePoster));
	}

	/**
	 * 新建下载任务
	 *
	 * @param storeFile File类型
	 * @param url 下载链接
	 * @param listener 监听器
	 * @return
	 */
	@Deprecated
	public static FileDownloader addTask(File storeFile, String url, DownloaderListener listener)
	{
		return addTask(new FileDownloader(mInstance.mContext, storeFile, url, listener, mInstance.mResponsePoster));
	}

	/**
	 * 新建下载任务
	 *
	 * @param storeFilePath 文件路径
	 * @param url 下载链接
	 * @param listener 监听器
	 * @return
	 */
	@Deprecated
	public static FileDownloader addTask(String storeFilePath, String url, IDownloaderListener listener)
	{
		return addTask(new File(storeFilePath), url, listener);
	}

	/**
	 * 新建下载任务
	 *
	 * @param storeFilePath 文件路径
	 * @param url 下载链接
	 * @param listener 监听器
	 * @return
	 */
	@Deprecated
	public static FileDownloader addTask(String storeFilePath, String url, DownloaderListener listener)
	{
		return addTask(new File(storeFilePath), url, listener);
	}

	/**
	 * 新建下载任务
	 *
	 * @param fileDownloader 任务
	 * @return
	 */
	public static FileDownloader addTask(final FileDownloader fileDownloader)
	{
		mInstance.mDownloaderList.add(fileDownloader);
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

}
