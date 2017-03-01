package com.excellence.downloader;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.util.Log;

import com.excellence.downloader.db.DBHelper;
import com.excellence.downloader.entity.HistoryFileInfo;

/**
 * Created by ZhangWei on 2016/2/22.
 */
public class DownloadThread extends Thread
{
	private static final String TAG = DownloadThread.class.getSimpleName();

	private static final int STREAM_LENGTH = 6 * 1024;

	private DBHelper mDBHelper = null;
	private FileDownloader mFileDownloader = null;
	private String mDownloadUrl = null;
	private int mThreadId = 0;
	private int mFileLength = 0;
	private int mStartPosition = 0;
	private int mDownloadLength = 0;
	private int mEndPosition = 0;
	private String mFileName = null;
	private boolean isFinished = false;
	private RandomAccessFile mAccessFile = null;

	public DownloadThread(Context context, FileDownloader fileDownloader, String downloadUrl, File saveFile, int block, int threadId, int fileLength)
	{
		mDBHelper = DBHelper.getInstance(context);
		mFileDownloader = fileDownloader;
		mDownloadUrl = downloadUrl;
		mThreadId = threadId;
		mFileLength = fileLength;
		mStartPosition = threadId * block;
		mEndPosition = (threadId + 1) * block - 1;
		mFileName = saveFile.getName();

		try
		{
			if (!saveFile.exists())
				throw new IllegalStateException("Storage file is not exist.");
			mAccessFile = new RandomAccessFile(saveFile, "rwd");
		}
		catch (Exception e)
		{
			e.printStackTrace();
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
					mDownloadLength = historyFileInfo.getDownloadLength();
				}
				else
				{
					// create once
					synchronized (DBHelper.lock)
					{
						mDBHelper.insertDownload(mFileName, mThreadId, mDownloadLength);
					}
				}
				mAccessFile.seek(mStartPosition + mDownloadLength);
				if (mEndPosition > mFileLength)
					mEndPosition = mFileLength;
				if ((mEndPosition + 1) == (mStartPosition + mDownloadLength) || mEndPosition == (mStartPosition + mDownloadLength))
				{
					isFinished = true;
				}
				else
				{
					URL url = new URL(mDownloadUrl);
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
					int tmpStartPosition = mStartPosition + mDownloadLength;
					connection.setRequestProperty("Range", "bytes=" + tmpStartPosition + "-" + mEndPosition);
					if (connection.getResponseCode() == 206)
					{
						InputStream inputStream = connection.getInputStream();
						byte[] buffer = new byte[STREAM_LENGTH];
						int len = 0;
						while (!mFileDownloader.isStop() && (len = inputStream.read(buffer)) != -1)
						{
							mAccessFile.write(buffer, 0, len);
							mDownloadLength += len;
							mFileDownloader.append(len);
						}
						mFileDownloader.updateDatabase(mThreadId, mDownloadLength);
						inputStream.close();
						connection.disconnect();
						if ((mEndPosition + 1) == (mStartPosition + mDownloadLength) || mEndPosition == (mStartPosition + mDownloadLength))
						{
							isFinished = true;
						}
					}
				}
				break;
			}
			catch (Exception e)
			{
				if (requestCount == 0)
				{
					e.printStackTrace();
					if (mDownloadLength != 0)
						mFileDownloader.updateDatabase(mThreadId, mDownloadLength);
					Log.e(TAG, requestCount + "download exception ----- download tasks:" + mFileName + "threadId:" + mThreadId);
					mFileDownloader.sendErrorMsg();
				}
			}
		} while (--requestCount > 0);
	}

	public boolean isFinished()
	{
		return isFinished;
	}

}
