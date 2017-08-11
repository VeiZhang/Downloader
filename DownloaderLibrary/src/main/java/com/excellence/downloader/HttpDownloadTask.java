package com.excellence.downloader;

import static com.excellence.downloader.utils.CommonUtil.checkNULL;
import static com.excellence.downloader.utils.HttpUtil.convertUrl;
import static com.excellence.downloader.utils.HttpUtil.printHeader;
import static com.excellence.downloader.utils.HttpUtil.setConnectParam;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import com.excellence.downloader.entity.TaskEntity;
import com.excellence.downloader.exception.DownloadError;
import com.excellence.downloader.exception.FileError;
import com.excellence.downloader.exception.URLInvalidError;
import com.excellence.downloader.utils.IListener;

import android.os.Build;
import android.util.Log;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     time   : 2017/8/10
 *     desc   : 下载开始
 * </pre>
 */

class HttpDownloadTask implements Runnable, IListener
{
	public static final String TAG = HttpDownloadTask.class.getSimpleName();

	private static final int CONNECT_TIME_OUT = 30 * 1000;
	private static final int SO_TIME_OUT = 10 * 1000;
	private static final int STREAM_LEN = 8 * 1024;

	private TaskEntity mTaskEntity = null;
	private IListener mListener = null;

	public HttpDownloadTask(TaskEntity taskEntity, IListener listener)
	{
		mTaskEntity = taskEntity;
		mListener = listener;
	}

	@Override
	public void run()
	{
		onPreExecute(mTaskEntity.fileSize);
		HttpURLConnection conn = null;
		try
		{
			checkTask();
			Log.e(TAG, "Start Download");
			URL httpURL = new URL(convertUrl(mTaskEntity.url));
			conn = (HttpURLConnection) httpURL.openConnection();
			conn.setConnectTimeout(CONNECT_TIME_OUT);
			conn.setReadTimeout(SO_TIME_OUT);
			setConnectParam(conn, mTaskEntity.url);
			conn.connect();
			printHeader(conn);

			RandomAccessFile randomAccessFile = new RandomAccessFile(mTaskEntity.storeFile, "rwd");
			InputStream is = conn.getInputStream();
			BufferedInputStream buffStream = new BufferedInputStream(is);
			byte[] buffer = new byte[STREAM_LEN];
			int read;
			FileChannel outFileChannel = randomAccessFile.getChannel();
			while ((read = buffStream.read(buffer)) != -1)
			{
				outFileChannel.write(ByteBuffer.wrap(buffer, 0, read));
			}
			is.close();
			outFileChannel.close();
			randomAccessFile.close();
		}
		catch (Exception e)
		{
			onError(new DownloadError(e));
		}
		finally
		{
			if (conn != null)
				conn.disconnect();
		}
	}

	/**
	 * 检测任务：下载链接、本地文件等等
	 */
	private void checkTask() throws Exception
	{
		if (checkNULL(mTaskEntity.url))
			throw new URLInvalidError("URL is invalid");

		File storeFile = mTaskEntity.storeFile;
		if (!storeFile.exists())
		{
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
			{

			}

			if (!storeFile.getParentFile().exists() && !storeFile.getParentFile().mkdirs())
				throw new FileError("Failed to open downloader dir");

			if (!storeFile.createNewFile())
				throw new FileError("Failed to create storage file");
		}

		if (storeFile.isDirectory())
			throw new FileError("Storage file is a directory");

		FileInputStream is = new FileInputStream(storeFile);
		mTaskEntity.downloadLen = is.available();
		is.close();

		File parentDir = storeFile.getParentFile();
		if (parentDir.getFreeSpace() <= mTaskEntity.fileSize - mTaskEntity.downloadLen)
			throw new FileError("Space is not enough");

	}

	@Override
	public void onPreExecute(long fileSize)
	{
		mListener.onPreExecute(fileSize);
	}

	@Override
	public void onProgressChange(long fileSize, long downloadedSize)
	{
		mListener.onProgressChange(fileSize, downloadedSize);
	}

	@Override
	public void onCancel()
	{
		mListener.onCancel();
	}

	@Override
	public void onError(DownloadError error)
	{
		mListener.onError(error);
	}

	@Override
	public void onSuccess()
	{
		mListener.onSuccess();
	}
}
