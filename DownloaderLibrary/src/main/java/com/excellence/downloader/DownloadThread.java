package com.excellence.downloader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.util.Log;

/**
 * Created by ZhangWei on 2016/2/22.
 */
public class DownloadThread extends Thread
{
	private static final String TAG = DownloadThread.class.getSimpleName();

	private DBHelper mDBhelper = null;
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
		mDBhelper = DBHelper.getInstance(context);
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
				saveFile.createNewFile();
			mAccessFile = new RandomAccessFile(saveFile, "rwd");
		}
		catch (IOException e)
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
				HistoryFileInfo historyFileInfo = mDBhelper.queryDownload(mFileName, mThreadId);
				if (historyFileInfo != null)
				{
					mDownloadLength = historyFileInfo.getDownloadLength();
				}
				else
				{
					// create once
					synchronized (DBHelper.lock)
					{
						mDBhelper.insertDownload(mFileName, mThreadId, mDownloadLength);
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
					HttpPost request = new HttpPost(mDownloadUrl);
					int tmpStartPosition = mStartPosition + mDownloadLength;
					request.setHeader("Range", "bytes=" + tmpStartPosition + "-" + mEndPosition);

					HttpResponse response = new DefaultHttpClient().execute(request);

					int resultCode = response.getStatusLine().getStatusCode();
					if (resultCode == 206)
					{
						InputStream inputStream = response.getEntity().getContent();
						byte[] buffer = new byte[1024 * 4];
						int len = 0;
						while (!mFileDownloader.isStop() && (len = inputStream.read(buffer)) != -1)
						{
							mAccessFile.write(buffer, 0, len);
							mFileDownloader.append(len);
							mDownloadLength += len;
							synchronized (DBHelper.lock)
							{
								mDBhelper.updateDownloadId(mFileName, mThreadId, mDownloadLength);
							}

						}
						inputStream.close();
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
					Log.e(TAG, requestCount + "download exception ----- download tasks:" + mFileName + "threadid:" + mThreadId);
					mFileDownloader.sendErrorMsg();
				}
			}
		} while (--requestCount > 0);
	}

	public boolean isFinished()
	{
		return isFinished;
	}

	public void setStop()
	{
		synchronized (DBHelper.lock)
		{
			mDBhelper.delete(mFileName, DBHelper.DOWNLOAD_TBL_NAME);
		}
	}

	public void setPause()
	{
		synchronized (DBHelper.lock)
		{
			Log.d(TAG, "pause mDownloadLength" + mDownloadLength + "::" + mThreadId);
			mDBhelper.updateDownloadId(mFileName, mThreadId, mDownloadLength);
			mDBhelper.updateFlag(mFileName, DownloadConstant.FLAG_PAUSE);
		}
	}
}
