package com.excellence.downloader;

import static com.excellence.downloader.FileDownloader.CONNECT_TIME_OUT;
import static com.excellence.downloader.FileDownloader.SO_TIME_OUT;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import com.excellence.downloader.exception.DownloadError;

import android.content.Context;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : https://veizhang.github.io/
 *     time   : 2017/2/22
 *     desc   : 单个下载线程
 * </pre>
 */

public class DownloadThread extends Thread
{
	private static final String TAG = DownloadThread.class.getSimpleName();

	private static final int STREAM_LENGTH = 8 * 1024;

	private DBHelper mDBHelper = null;
	private FileDownloader mFileDownloader = null;
	private int mThreadId = 0;
	private long mFileSize = 0;
	private int mDownloadSize = 0;
	private long mStartPosition = 0;
	private long mEndPosition = 0;
	private String mFileName = null;
	private boolean isFinished = false;
	private RandomAccessFile mAccessFile = null;

	protected DownloadThread(Context context, FileDownloader fileDownloader, int threadId, long block, long fileSize)
	{
		mDBHelper = DBHelper.getInstance(context);
		mFileDownloader = fileDownloader;
		mThreadId = threadId;
		mFileSize = fileSize;
		mStartPosition = threadId * block;
		mEndPosition = (threadId + 1) * block - 1;
		mFileName = mFileDownloader.getFileName();
		try
		{
			File storeFile = mFileDownloader.getStoreFile();
			if (!storeFile.exists())
				throw new IllegalStateException("Storage file is not exist.");
			mAccessFile = new RandomAccessFile(storeFile, "rwd");
		}
		catch (Exception e)
		{
			mFileDownloader.sendErrorMsg(new DownloadError(e));
		}
	}

	@Override
	public void run()
	{
		int requestCount = 3;
		do
		{
			try
			{
				mDownloadSize = mDBHelper.queryDownloadSize(mFileName, mThreadId);
				if (mDownloadSize == -1)
				{
					// create once
					mDownloadSize = 0;
					mDBHelper.insertDownloadSize(mFileName, mThreadId, mDownloadSize);
				}
				long tmpStartPosition = mStartPosition + mDownloadSize;
				if (mEndPosition > mFileSize)
					mEndPosition = mFileSize;
				if ((mEndPosition + 1) == tmpStartPosition || mEndPosition == tmpStartPosition)
				{
					isFinished = true;
				}
				else
				{
					URL httpURL = new URL(mFileDownloader.getFileUrl());
					HttpURLConnection connection = (HttpURLConnection) httpURL.openConnection();
					connection.setConnectTimeout(CONNECT_TIME_OUT);
					connection.setReadTimeout(SO_TIME_OUT);
					connection.setRequestMethod("GET");
					connection.setRequestProperty("Accept",
							"image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
					connection.setRequestProperty("Accept-Language", "zh-CN");
					connection.setRequestProperty("Referer", mFileDownloader.getFileUrl());
					connection.setRequestProperty("Charset", "UTF-8");
					connection.setRequestProperty("Range", "bytes=" + tmpStartPosition + "-" + mEndPosition);
					connection.setRequestProperty("User-Agent",
							"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
					connection.setRequestProperty("Connection", "Keep-Alive");
					if (connection.getResponseCode() == HttpURLConnection.HTTP_PARTIAL)
					{
						InputStream inStream = connection.getInputStream();
						BufferedInputStream bufferedInputStream = new BufferedInputStream(inStream);
						byte[] buffer = new byte[STREAM_LENGTH];
						int offset = 0;
						FileChannel outFileChannel = mAccessFile.getChannel();
						outFileChannel.position(tmpStartPosition);
						while (!mFileDownloader.isStop() && (offset = bufferedInputStream.read(buffer)) != -1)
						{
							outFileChannel.write(ByteBuffer.wrap(buffer, 0, offset));
							mDownloadSize += offset;
							mFileDownloader.append(offset);
						}
						updateDatabase();
						inStream.close();
						outFileChannel.close();
						mAccessFile.close();
						tmpStartPosition = mStartPosition + mDownloadSize;
						if ((mEndPosition + 1) == tmpStartPosition || mEndPosition == tmpStartPosition)
						{
							isFinished = true;
						}
					}
				}
				break;
			}
			catch (Exception e)
			{
				if (requestCount == 0 && !mFileDownloader.isStop())
				{
					mFileDownloader.pause();
					mFileDownloader.sendErrorMsg(new DownloadError(e));
					updateDatabase();
				}
			}
		} while (--requestCount > 0);
	}

	/**
	 * 更新单个任务中的数据库 考虑频繁操作数据库会耗内存和影响读写速度，因此分离到下载暂停、结束或异常后更新
	 * 但是，主动销毁app，或Crash异常，则不能保存数据
	 */
	protected void updateDatabase()
	{
		// 更新某线程下载长度
		if (mDBHelper != null && mDownloadSize > 0)
			mDBHelper.updateDownloadSize(mFileName, mThreadId, mDownloadSize);
	}

	/**
	 * 判断当前线程是否成功下载
	 * 
	 * @return
	 */
	protected boolean isFinished()
	{
		return isFinished;
	}

}
