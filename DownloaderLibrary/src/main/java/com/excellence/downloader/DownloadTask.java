package com.excellence.downloader;

import static com.excellence.downloader.utils.HttpUtil.convertUrl;
import static com.excellence.downloader.utils.HttpUtil.printHeader;
import static com.excellence.downloader.utils.HttpUtil.setConnectParam;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_PARTIAL;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.excellence.downloader.entity.TaskEntity;
import com.excellence.downloader.exception.FileError;
import com.excellence.downloader.utils.IListener;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     time   : 2017/8/9
 *     desc   : 下载任务
 * </pre>
 */

public class DownloadTask
{
	public static final String TAG = DownloadTask.class.getSimpleName();

	private static final int CONNECT_TIME_OUT = 30 * 1000;
	private static final int SO_TIME_OUT = 10 * 1000;

	public static final int STATUS_WAITING = 0;
	public static final int STATUS_DOWNLOADING = 1;
	public static final int STATUS_PAUSE = 2;
	public static final int STATUS_SUCCESS = 3;
	public static final int STATUS_DISCARD = 4;

	private int mStatus = STATUS_WAITING;
	private TaskEntity mTaskEntity = null;
	private IListener mListener = null;
	private Executor mExecutor = null;

	public DownloadTask(@NonNull TaskEntity taskEntity, IListener listener)
	{
		mTaskEntity = taskEntity;
		mListener = listener;
		mExecutor = Executors.newSingleThreadExecutor();
	}

	public boolean deploy()
	{
		// only wait task can deploy
		if (mStatus != STATUS_WAITING)
			return false;
		mExecutor.execute(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					checkTask();
					buildFileInfoRequest();
					buildRequest();
				}
				catch (Exception e)
				{

				}
			}
		});
		return true;
	}

	/**
	 * 检测任务：下载链接、本地文件等等
	 */
	private void checkTask()
	{

	}

	private void buildFileInfoRequest() throws Exception
	{
		Log.e(TAG, "Request file info");
		URL httpURL = new URL(convertUrl(mTaskEntity.url));
		HttpURLConnection conn = (HttpURLConnection) httpURL.openConnection();
		conn.setConnectTimeout(CONNECT_TIME_OUT);
		/**
		 * 判断服务器是否支持断点，{@code 206}:支持<br>{@code 200}:不支持
		 * @see #handConnect(HttpURLConnection)
		 **/
		conn.setRequestProperty("Range", "bytes=" + 0 + "-");
		setConnectParam(conn, mTaskEntity.url);
		conn.connect();
		handConnect(conn);
		printHeader(conn);
	}

	private void handConnect(HttpURLConnection conn) throws Exception
	{
		long len = conn.getContentLength();
		if (len < 0)
		{
			String temp = conn.getHeaderField("Content-Length");
			len = TextUtils.isEmpty(temp) ? -1 : Long.parseLong(temp);
		}

		if (len < 0)
		{
			throw new FileError(formatRequestMsg("File length is error"));
		}

		int code = conn.getResponseCode();
		Log.e(TAG, formatRequestMsg(code));
		switch (code)
		{
		case HTTP_OK:
			mTaskEntity.isSupportBP = false;
			break;

		case HTTP_PARTIAL:
			mTaskEntity.isSupportBP = true;
			break;

		default:
			throw new FileError(formatRequestMsg(code));
		}
	}

	private String formatRequestMsg(int code)
	{
		StringBuilder sp = new StringBuilder();
		sp.append("Task [").append(mTaskEntity.url).append("]");
		sp.append(" ").append("Request code : ").append(code);
		return sp.toString();
	}

	private String formatRequestMsg(String msg)
	{
		StringBuilder sp = new StringBuilder();
		sp.append("Task [").append(mTaskEntity.url).append("]");
		sp.append(" ").append("Request msg : ").append(msg);
		return sp.toString();
	}

	private void buildRequest() throws Exception
	{
		Log.e(TAG, "Start Download");
		URL httpURL = new URL(convertUrl(mTaskEntity.url));
		HttpURLConnection conn = (HttpURLConnection) httpURL.openConnection();
		conn.setConnectTimeout(CONNECT_TIME_OUT);
		conn.setReadTimeout(SO_TIME_OUT);
		setConnectParam(conn, mTaskEntity.url);
		conn.connect();
		printHeader(conn);
	}

	public boolean isDownloading()
	{
		return mStatus == STATUS_DOWNLOADING;
	}

	public int getStatus()
	{
		return mStatus;
	}

	public boolean check(File storeFile, String url)
	{
		return mTaskEntity.storeFile == storeFile && mTaskEntity.url.equals(url);
	}
}
