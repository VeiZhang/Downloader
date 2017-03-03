package com.excellence.downloader;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;

import com.excellence.downloader.exception.DownloadError;

import static com.excellence.downloader.FileDownloader.CONNECT_TIME_OUT;
import static com.excellence.downloader.FileDownloader.SO_TIME_OUT;

/**
 * Created by ZhangWei on 2016/2/22.
 */
public class DownloadThread extends Thread
{
	private static final String TAG = DownloadThread.class.getSimpleName();

	private static final int STREAM_LENGTH = 6 * 1024;

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
					URL url = new URL(mFileDownloader.getFileUrl());
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
					connection.setConnectTimeout(CONNECT_TIME_OUT);
					connection.setReadTimeout(SO_TIME_OUT);
					connection.setRequestProperty("Range", "bytes=" + tmpStartPosition + "-" + mEndPosition);
					connection.connect();
					if (connection.getResponseCode() == 206)
					{
						InputStream inStream = connection.getInputStream();
						byte[] buffer = new byte[STREAM_LENGTH];
						int offset = 0;
						mAccessFile.seek(tmpStartPosition);
						while (!mFileDownloader.isStop() && (offset = inStream.read(buffer)) != -1)
						{
							mAccessFile.write(buffer, 0, offset);
							mDownloadSize += offset;
							mFileDownloader.append(offset);
						}
						updateDatabase();
						inStream.close();
						connection.disconnect();
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
	 * 更新单个任务中的数据库
	 * 考虑频繁操作数据库会耗内存和影响读写速度，因此分离到下载暂停、结束或异常后更新
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
	 * @return
     */
	protected boolean isFinished()
	{
		return isFinished;
	}

}
