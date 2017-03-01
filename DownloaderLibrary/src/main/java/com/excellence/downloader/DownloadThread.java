package com.excellence.downloader;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;

import com.excellence.downloader.db.DBHelper;
import com.excellence.downloader.entity.HistoryFileInfo;
import com.excellence.downloader.exception.DownloadError;

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

	public DownloadThread(Context context, FileDownloader fileDownloader, int threadId, long block, long fileSize)
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
			mFileDownloader.sendErrorMsg(new DownloadError(e.getMessage()));
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
				HistoryFileInfo historyFileInfo = mDBHelper.queryDownload(mFileName, mThreadId);
				if (historyFileInfo != null)
				{
					mDownloadSize = historyFileInfo.getDownloadLength();
				}
				else
				{
					// create once
					synchronized (DBHelper.lock)
					{
						mDBHelper.insertDownload(mFileName, mThreadId, mDownloadSize);
					}
				}
				mAccessFile.seek(mStartPosition + mDownloadSize);
				if (mEndPosition > mFileSize)
					mEndPosition = mFileSize;
				if ((mEndPosition + 1) == (mStartPosition + mDownloadSize) || mEndPosition == (mStartPosition + mDownloadSize))
				{
					isFinished = true;
				}
				else
				{
					URL url = new URL(mFileDownloader.getFileUrl());
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
					long tmpStartPosition = mStartPosition + mDownloadSize;
					connection.setRequestProperty("Range", "bytes=" + tmpStartPosition + "-" + mEndPosition);
					if (connection.getResponseCode() == 206)
					{
						InputStream inputStream = connection.getInputStream();
						byte[] buffer = new byte[STREAM_LENGTH];
						int len = 0;
						while (!mFileDownloader.isStop() && (len = inputStream.read(buffer)) != -1)
						{
							mAccessFile.write(buffer, 0, len);
							mDownloadSize += len;
							mFileDownloader.append(len);
						}
						mFileDownloader.updateDatabase(mThreadId, mDownloadSize);
						inputStream.close();
						connection.disconnect();
						if ((mEndPosition + 1) == (mStartPosition + mDownloadSize) || mEndPosition == (mStartPosition + mDownloadSize))
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
					mFileDownloader.sendErrorMsg(new DownloadError(e.getMessage()));
					if (mDownloadSize != 0)
						mFileDownloader.updateDatabase(mThreadId, mDownloadSize);
				}
			}
		} while (--requestCount > 0);
	}

	public boolean isFinished()
	{
		return isFinished;
	}

}
