package com.excellence.downloader;

import android.util.Log;

import com.excellence.downloader.entity.TaskEntity;
import com.excellence.downloader.exception.DownloadError;
import com.excellence.downloader.exception.FileError;
import com.excellence.downloader.exception.URLInvalidError;
import com.excellence.downloader.utils.IListener;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.zip.GZIPInputStream;

import static com.excellence.downloader.utils.CommonUtil.checkNULL;
import static com.excellence.downloader.utils.HttpUtil.convertUrl;
import static com.excellence.downloader.utils.HttpUtil.isGzipContent;
import static com.excellence.downloader.utils.HttpUtil.printHeader;
import static com.excellence.downloader.utils.HttpUtil.setConnectParam;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     time   : 2017/8/10
 *     desc   : 下载开始
 * </pre>
 */

public class HttpDownloadTask extends HttpTask implements IListener
{
	public static final String TAG = HttpDownloadTask.class.getSimpleName();

	public static final String SUFFIX_TMP = ".tmp";

	private static final int SO_TIME_OUT = 10 * 1000;
	private static final int STREAM_LEN = 8 * 1024;
	private static final int TIMER_SEC = 1000;

	private Executor mResponsePoster = null;
	private TaskEntity mTaskEntity = null;
	private IListener mListener = null;
	private File mTempFile = null;
	private Timer mSpeedTimer = null;
	private long mStartLen = 0;

	public HttpDownloadTask(Executor responsePoster, TaskEntity taskEntity, IListener listener)
	{
		mResponsePoster = responsePoster;
		mTaskEntity = taskEntity;
		mListener = listener;
		mTempFile = new File(mTaskEntity.storeFile + SUFFIX_TMP);
		mTaskEntity.tempFile = mTempFile;
	}

	@Override
	protected boolean buildRequest()
	{
		HttpURLConnection conn = null;
		try
		{
			checkTask();
			Log.e(TAG, "Start Download");
			URL httpURL = new URL(convertUrl(mTaskEntity.url));
			conn = (HttpURLConnection) httpURL.openConnection();
			conn.setConnectTimeout(CONNECT_TIME_OUT);
			conn.setReadTimeout(SO_TIME_OUT);
			conn.setRequestProperty("Range", "bytes=" + mTaskEntity.downloadLen + "-" + (mTaskEntity.fileSize - 1));
			setConnectParam(conn, mTaskEntity.url);
			conn.connect();

			if (mTaskEntity.isCancel)
			{
				onCancel();
				return true;
			}

			printHeader(conn);

			startTimer();
			RandomAccessFile randomAccessFile = new RandomAccessFile(mTempFile, "rwd");
			randomAccessFile.seek(mTaskEntity.downloadLen);
			InputStream is = conn.getInputStream();
			if (isGzipContent(conn) && !(is instanceof GZIPInputStream))
				is = new GZIPInputStream(is);
			BufferedInputStream buffStream = new BufferedInputStream(is);
			byte[] buffer = new byte[STREAM_LEN];
			int read;
			FileChannel outFileChannel = randomAccessFile.getChannel();
			while ((read = buffStream.read(buffer)) != -1)
			{
				outFileChannel.write(ByteBuffer.wrap(buffer, 0, read));
				mTaskEntity.downloadLen += read;
				onProgressChange(mTaskEntity.fileSize, mTaskEntity.downloadLen);

				if (mTaskEntity.isCancel)
				{
					onCancel();
					break;
				}

			}
			is.close();
			outFileChannel.close();
			randomAccessFile.close();

			if (mTaskEntity.isCancel)
				return true;

			if (mTempFile.length() == mTaskEntity.fileSize || mTempFile.length() + 1 == mTaskEntity.fileSize)
			{
				if (!mTempFile.canRead())
					throw new FileError("Download temp file is invalid");
				if (!mTempFile.renameTo(mTaskEntity.storeFile))
					throw new FileError("Can't rename download temp file");
				onSuccess();
			}
		}
		catch (Exception e)
		{
			if (retry())
			{
				onError(new DownloadError(e));
				return true;
			}
			else
				return false;
		}
		finally
		{
			if (conn != null)
				conn.disconnect();
		}
		return true;
	}

	private void startTimer()
	{
		mStartLen = mTaskEntity.downloadLen;
		mSpeedTimer = new Timer(true);
		mSpeedTimer.schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				if (mTaskEntity.isCancel)
				{
					closeTimer();
				}
				else
				{
					mTaskEntity.downloadSpeed = mTaskEntity.downloadLen - mStartLen;
					onProgressChange(mTaskEntity.fileSize, mTaskEntity.downloadLen, mTaskEntity.downloadSpeed);
					mStartLen = mTaskEntity.downloadLen;
				}
			}
		}, 0, TIMER_SEC);
	}

	private void closeTimer()
	{
		if (mSpeedTimer != null)
		{
			mSpeedTimer.purge();
			mSpeedTimer.cancel();
		}
	}

	/**
	 * 检测任务：下载链接、本地文件等等
	 */
	private void checkTask() throws Exception
	{
		if (checkNULL(mTaskEntity.url))
			throw new URLInvalidError("URL is invalid");

		if (!mTempFile.exists())
		{
			if (!mTempFile.getParentFile().exists() && !mTempFile.getParentFile().mkdirs())
				throw new FileError("Failed to open downloader dir");

			if (!mTempFile.createNewFile())
				throw new FileError("Failed to create storage file");
		}

		if (mTempFile.isDirectory())
			throw new FileError("Storage file is a directory");

		if (mTaskEntity.isSupportBP)
		{
			FileInputStream is = new FileInputStream(mTempFile);
			mTaskEntity.downloadLen = is.available();
			is.close();
		}
		else
		{
			mTaskEntity.downloadLen = 0;
			RandomAccessFile randomAccessFile = new RandomAccessFile(mTempFile, "rwd");
			randomAccessFile.setLength(0);
			randomAccessFile.close();
		}

		File parentDir = mTempFile.getParentFile();
		if (parentDir.getFreeSpace() <= mTaskEntity.fileSize - mTaskEntity.downloadLen)
			throw new FileError("Space is not enough");

	}

	@Override
	public void onPreExecute(final long fileSize)
	{
		mResponsePoster.execute(new Runnable()
		{
			@Override
			public void run()
			{
				if (mListener != null)
					mListener.onPreExecute(fileSize);
			}
		});
	}

	@Override
	public void onProgressChange(final long fileSize, final long downloadedSize)
	{
		mResponsePoster.execute(new Runnable()
		{
			@Override
			public void run()
			{
				if (mListener != null && !mTaskEntity.isCancel)
					mListener.onProgressChange(fileSize, downloadedSize);
			}
		});
	}

	@Override
	public void onProgressChange(final long fileSize, final long downloadedSize, final long speed)
	{
		mResponsePoster.execute(new Runnable()
		{
			@Override
			public void run()
			{
				if (mListener != null && !mTaskEntity.isCancel)
					mListener.onProgressChange(fileSize, downloadedSize, speed);
			}
		});
	}

	@Override
	public void onCancel()
	{
		closeTimer();
		mResponsePoster.execute(new Runnable()
		{
			@Override
			public void run()
			{
				if (mListener != null)
					mListener.onCancel();
			}
		});
	}

	@Override
	public void onError(final DownloadError error)
	{
		closeTimer();
		mResponsePoster.execute(new Runnable()
		{
			@Override
			public void run()
			{
				if (mListener != null && !mTaskEntity.isCancel)
					mListener.onError(error);
			}
		});
	}

	@Override
	public void onSuccess()
	{
		closeTimer();
		mResponsePoster.execute(new Runnable()
		{
			@Override
			public void run()
			{
				if (mListener != null && !mTaskEntity.isCancel)
					mListener.onSuccess();
			}
		});
	}
}
