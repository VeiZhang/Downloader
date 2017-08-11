package com.excellence.downloader;

import static com.excellence.downloader.Downloader.DEFAULT_THREAD_COUNT;
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

import com.excellence.downloader.exception.FileError;
import com.excellence.downloader.utils.IListener;

import android.text.TextUtils;

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
	private static final int CONNECT_TIME_OUT = 30 * 1000;
	private static final int SO_TIME_OUT = 10 * 1000;

	public static final int STATUS_WAITING = 0;
	public static final int STATUS_DOWNLOADING = 1;
	public static final int STATUS_PAUSE = 2;
	public static final int STATUS_SUCCESS = 3;
	public static final int STATUS_DISCARD = 4;

	private int mStatus = STATUS_WAITING;
	private File mStoreFile = null;
	private String mUrl = null;
	private IListener mListener = null;
	private int mThreadCount = DEFAULT_THREAD_COUNT;
	private Executor mExecutor = null;

	public DownloadTask(File storeFile, String url, IListener listener)
	{
		mStoreFile = storeFile;
		mUrl = url;
		mListener = listener;
		mExecutor = Executors.newSingleThreadExecutor();
	}

	public DownloadTask(File storeFile, String url, IListener listener, int threadCount)
	{
		this(storeFile, url, listener);
		mThreadCount = threadCount;
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
		URL httpURL = new URL(convertUrl(mUrl));
		HttpURLConnection conn = (HttpURLConnection) httpURL.openConnection();
		conn.setConnectTimeout(CONNECT_TIME_OUT);
		/**
		 * 判断服务器是否支持断点，{@code 206}:支持<br>{@code 200}:不支持
		 * @see #handConnect(HttpURLConnection)
		 **/
		conn.setRequestProperty("Range", "bytes=" + 0 + "-");
		setConnectParam(conn, mUrl);
		conn.connect();
		printHeader(conn);
		handConnect(conn);
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
			throw new FileError(formatErrorMsg("File length is error"));
		}

		int code = conn.getResponseCode();
		switch (code)
		{
		case HTTP_OK:
			mThreadCount = DEFAULT_THREAD_COUNT;
			break;

		case HTTP_PARTIAL:
			break;

		default:
			throw new FileError(formatErrorMsg(code));
		}
	}

	private String formatErrorMsg(int errorCode)
	{
		StringBuilder sp = new StringBuilder();
		sp.append("Task [").append(mUrl).append("]");
		sp.append(" ").append("Error code : ").append(errorCode);
		return sp.toString();
	}

	private String formatErrorMsg(String msg)
	{
		StringBuilder sp = new StringBuilder();
		sp.append("Task [").append(mUrl).append("]");
		sp.append(" ").append("Error msg : ").append(msg);
		return sp.toString();
	}

	private void buildRequest() throws Exception
	{
		URL httpURL = new URL(convertUrl(mUrl));
		HttpURLConnection conn = (HttpURLConnection) httpURL.openConnection();
		conn.setConnectTimeout(CONNECT_TIME_OUT);
		conn.setReadTimeout(SO_TIME_OUT);
		setConnectParam(conn, mUrl);
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
		return mStoreFile == storeFile && mUrl.equals(url);
	}
}
