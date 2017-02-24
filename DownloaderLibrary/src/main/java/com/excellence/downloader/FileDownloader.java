package com.excellence.downloader;

/**
 * Created by MK on 2016/10/27.
 */

import java.io.File;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import android.content.Context;
import android.util.Log;

/***
 * 入口 url，文件存储的地址 出口：下载进度，下载状态
 */
public class FileDownloader
{
	private static final String TAG = FileDownloader.class.getSimpleName();
	private static final int THREAD_COUNT = 3;
	private static final int CONNECT_TIME_OUT = 30 * 1000;
	private static final int SO_TIME_OUT = 15 * 1000;
	private Context mContext;
	private List<FileDownloader> mFileDownloaderList = null;
	private DownloadThread[] mDownloadThreads = null;
	private DBHelper mDBHelper = null;
	private boolean isStop = false;
	private boolean isFinished = false;
	private long mDownloadLength = 0;
	private File mStoreFile = null;
	private String mFileUrl;
	private String mFileName;
	private DownloaderListener mDownloaderListener = null;
	private String mDownload_Path;

	public FileDownloader(Context context, File storeFile, String url, DownloaderListener listener)
	{
		mContext = context;
		mStoreFile = storeFile;
		mFileUrl = url;
		mFileName = storeFile.getName();
		mDownload_Path = storeFile.getParent();
		mDownloaderListener = listener;
		mFileDownloaderList = DownloaderUtils.getDownloaderList();
		mDBHelper = DBHelper.getInstance(context);
		mDownloadThreads = new DownloadThread[THREAD_COUNT];
	}

	protected synchronized void append(long size)
	{
		mDownloadLength += size;
		mDBHelper.updateDownloadLength(mFileName, (int) mDownloadLength);
	}

	public void deploy()
	{
		if (isStop)
			return;

		if (!mFileUrl.isEmpty())
		{
			try
			{

				Log.e(TAG, " download url:" + mFileUrl);

				if (mFileUrl != null && mFileUrl.trim().length() > 0)
				{
					URL url = new URL(mFileUrl);
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
					connection.setConnectTimeout(CONNECT_TIME_OUT);
					connection.setReadTimeout(SO_TIME_OUT);
					connection.setRequestMethod("POST");

					if (isStop)
						return;

					if (connection.getResponseCode() == 200)
					{
						int fileLength = connection.getContentLength();
						connection.disconnect();

						if (!mStoreFile.exists())
							mStoreFile.createNewFile();
						RandomAccessFile accessFile = new RandomAccessFile(mStoreFile, "rwd");
						accessFile.setLength(fileLength);
						accessFile.close();
						if (MemorySpaceCheck.hasSDEnoughMemory(mDownload_Path, fileLength))
						{
							mDownloadLength = mDBHelper.queryDownloadedLength(mFileName);
							mDownloaderListener.onDownloadStartListener(mFileName, fileLength);

							int block = fileLength % THREAD_COUNT == 0 ? fileLength / THREAD_COUNT : fileLength / THREAD_COUNT + 1;
							for (int i = 0; i < THREAD_COUNT; i++)
							{
								mDownloadThreads[i] = new DownloadThread(mContext, this, mFileUrl, mStoreFile, block, i, fileLength);
								mDownloadThreads[i].start();
							}

							while (!isStop && !isFinished)
							{
								isFinished = true;
								try
								{
									Thread.sleep(100);
								}
								catch (InterruptedException e)
								{
									e.printStackTrace();
								}
								for (int i = 0; i < THREAD_COUNT; i++)
								{
									if (mDownloadThreads[i] != null && !mDownloadThreads[i].isFinished())
									{
										isFinished = false;
									}
								}
								if (isStop)
									break;
								mDownloaderListener.onDownloadingListener(mFileName, mDownloadLength);
							}

							if (isFinished)
							{
								mDBHelper.updateFlag(mFileName, DownloadConstant.FLAG_DOWNLOAD_FINISHED);
								for (FileDownloader fileDownloader : mFileDownloaderList)
								{
									if (fileDownloader.getDownloaderName().equals(mFileName))
									{
										mFileDownloaderList.remove(fileDownloader);
										break;
									}
								}
								mDownloaderListener.onDownloadFinishListener(mFileName);

								for (int i = 0; i < THREAD_COUNT; i++)
								{
									if (mDownloadThreads[i] != null)
										mDownloadThreads[i].setStop();
								}
							}
						}
						else
						{
							sendErrorMsg(DownloadConstant.SPACE_IS_NOT_ENOUGH);
						}
					}
					else
					{
						Log.e(TAG, "download request fail");
						sendErrorMsg();

					}
				}
				else
				{
					Log.e(TAG, "get download url fail");
					sendErrorMsg();
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
				Log.e(TAG, "download exception");
				sendErrorMsg();
			}
		}else
		{
			//url is empty
		}
	}

	public void sendErrorMsg()
	{
		if (isStop)
			return;
		for (FileDownloader fileDownloader : mFileDownloaderList)
		{
			if (fileDownloader.getDownloaderName().equals(mFileName))
			{
				mFileDownloaderList.remove(fileDownloader);
				break;
			}
		}
		mDBHelper.updateFlag(mFileName, DownloadConstant.FLAG_ERROR);
		isStop = true;
		//mDownloaderListener.onDownloadFailListener(mFileName, BAD_RESPONECODE);
		// send error service
		//mDownloaderListener.onDownloadFailListener(mFileName, DownloadConstant.FLAG_ERROR);
	}

	public void sendErrorMsg(int flag)
	{
		if (isStop)
			return;
		for (FileDownloader fileDownloader : mFileDownloaderList)
		{
			if (fileDownloader.getDownloaderName().equals(mFileName))
			{
				mFileDownloaderList.remove(fileDownloader);
				break;
			}
		}
		mDBHelper.updateFlag(mFileName, DownloadConstant.FLAG_ERROR);
		isStop = true;
		//mDownloaderListener.onDownloadFailListener(mFileName, BAD_RESPONECODE);
		mDownloaderListener.onDownloadFailListener(mFileName, flag);
	}

	public boolean isStop()
	{
		return isStop;
	}

	public void setPause(boolean isStop)
	{
		this.isStop = isStop;
		if (isStop)
		{
			for (int i = 0; i < THREAD_COUNT; i++)
			{
				if (mDownloadThreads[i] != null && !mDownloadThreads[i].isFinished())
					mDownloadThreads[i].setPause();
			}
		}
	}

	public void setStop()
	{
		isStop = true;
		for (int i = 0; i < THREAD_COUNT; i++)
		{
			if (mDownloadThreads[i] != null)
				mDownloadThreads[i].setStop();
		}
	}

	public String getDownloaderName()
	{
		return mFileName;
	}

}
