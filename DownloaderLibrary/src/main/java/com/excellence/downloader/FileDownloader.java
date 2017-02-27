package com.excellence.downloader;

/**
 * Created by MK on 2016/10/27.
 */

import java.io.File;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executor;

import android.content.Context;
import android.util.Log;

/***
 * 文件下载器
 */
public class FileDownloader implements IDownloaderListener
{
	private static final String TAG = FileDownloader.class.getSimpleName();

	private static final int THREAD_COUNT = 3;
	private static final int CONNECT_TIME_OUT = 30 * 1000;
	private static final int SO_TIME_OUT = 15 * 1000;
	private static final int REFRESH_TRAVEL_TIME = 100;

	public static final int STATE_DOWNLOADING = 0;
	public static final int STATE_PAUSE = 1;
	public static final int STATE_SUCCESS = 2;
	public static final int STATE_DISCARD = 3;

	private Context mContext;
	private File mStoreFile = null;
	private String mFileUrl;
	private DownloaderListener mDownloaderListener = null;
	private Executor mResponsePoster = null;
	private DownloadThread[] mDownloadThreads = new DownloadThread[THREAD_COUNT];
	private DBHelper mDBHelper = null;
	private boolean isStop = false;
	private boolean isFinished = false;
	private long mDownloadLength = 0;
	private String mFileName;
	private int mState;

	public FileDownloader(Context context, File storeFile, String url, DownloaderListener listener, Executor executor)
	{
		mContext = context;
		mStoreFile = storeFile;
		mFileUrl = url;
		mFileName = storeFile.getName();
		mDownloaderListener = listener;
		mResponsePoster = executor;
		mDBHelper = DBHelper.getInstance(context);
	}

	protected synchronized void append(long size)
	{
		mDownloadLength += size;
		mDBHelper.updateDownloadLength(mFileName, (int) mDownloadLength);
	}

	public void deploy()
	{
		mState = STATE_DOWNLOADING;
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
					// default request : GET
					connection.setRequestMethod("GET");
					int responseCode = connection.getResponseCode();
					Log.e(TAG, "response code : " + responseCode);
					if (responseCode == 200)
					{
						int fileLength = connection.getContentLength();
						connection.disconnect();
						if (!mStoreFile.exists())
							mStoreFile.createNewFile();

						/*
						if (mDBHelper.queryFlag(mFileName) == null)
							mDBHelper.insertFlag(mFileName, 0, fileLength);
						else
							mDBHelper.updateFlag(mFileName, 0);
						*/

						RandomAccessFile accessFile = new RandomAccessFile(mStoreFile, "rwd");
						accessFile.setLength(fileLength);
						accessFile.close();
						if (MemorySpaceCheck.hasSDEnoughMemory(mStoreFile.getParent(), fileLength))
						{
							mDownloadLength = mDBHelper.queryDownloadedLength(mFileName);
							onDownloadStartListener(mFileName, fileLength);

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
									Thread.sleep(REFRESH_TRAVEL_TIME);
								}
								catch (InterruptedException e)
								{
									e.printStackTrace();
								}
								if (isStop)
									break;
								for (int i = 0; i < THREAD_COUNT; i++)
								{
									if (mDownloadThreads[i] != null && !mDownloadThreads[i].isFinished())
									{
										isFinished = false;
									}
								}
								onDownloadingListener(mFileName, mDownloadLength);
							}

							if (isFinished)
							{
								if (fileLength == mDownloadLength)
									mState = STATE_SUCCESS;
								mDBHelper.updateFlag(mFileName, DownloadConstant.FLAG_DOWNLOAD_FINISHED);
								DownloaderManager.getDownloaderList().remove(this);
								onDownloadFinishListener(mFileName);

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
		}
		else
		{
			// url is empty
			Log.e(TAG, "download url is empty");
			sendErrorMsg();
		}
	}

	public void sendErrorMsg()
	{
		sendErrorMsg(DownloadConstant.FLAG_ERROR);
	}

	// 一个下载线程停止，其他线程也停止
	public void sendErrorMsg(int flag)
	{
		if (isStop)
			return;
		mState = STATE_PAUSE;
		DownloaderManager.getDownloaderList().remove(this);
		mDBHelper.updateFlag(mFileName, DownloadConstant.FLAG_ERROR);
		isStop = true;
		onDownloadFailListener(mFileName, flag);
	}

	// 继续任务
	public void schedule()
	{
//		DownloaderManager.addTask(mContext, mStoreFile, mFileUrl, mDownloaderListener);
	}

	// 暂停任务
	public void setPause()
	{
		mState = STATE_PAUSE;
		isStop = true;
		DownloaderManager.getDownloaderList().remove(this);
		for (int i = 0; i < THREAD_COUNT; i++)
		{
			if (mDownloadThreads[i] != null && !mDownloadThreads[i].isFinished())
				mDownloadThreads[i].setPause();
		}
	}

	// 删除任务
	public void setStop()
	{
		mState = STATE_DISCARD;
		isStop = true;
		DownloaderManager.getDownloaderList().remove(this);
		for (int i = 0; i < THREAD_COUNT; i++)
		{
			if (mDownloadThreads[i] != null)
				mDownloadThreads[i].setStop();
		}
	}

	public int getState()
	{
		return mState;
	}

	public boolean isStop()
	{
		return isStop;
	}

	public String getDownloaderName()
	{
		return mFileName;
	}

	public File getStoreFile()
	{
		return mStoreFile;
	}

	@Override
	public void onDownloadStartListener(final String filename, final int fileLength)
	{
		mResponsePoster.execute(new Runnable()
		{
			@Override
			public void run()
			{
				if (mDownloaderListener != null)
					mDownloaderListener.onDownloadStartListener(filename, fileLength);
			}
		});
	}

	@Override
	public void onDownloadingListener(final String filename, final long downloadedLength)
	{
		mResponsePoster.execute(new Runnable()
		{
			@Override
			public void run()
			{
				if (mDownloaderListener != null)
					mDownloaderListener.onDownloadingListener(filename, downloadedLength);
			}
		});
	}

	@Override
	public void onDownloadFinishListener(final String filename)
	{
		mResponsePoster.execute(new Runnable()
		{
			@Override
			public void run()
			{
				if (mDownloaderListener != null)
					mDownloaderListener.onDownloadFinishListener(filename);
			}
		});
	}

	@Override
	public void onDownloadFailListener(final String filename, final int result)
	{
		mResponsePoster.execute(new Runnable()
		{
			@Override
			public void run()
			{
				if (mDownloaderListener != null)
					mDownloaderListener.onDownloadFailListener(filename, result);
			}
		});
	}
}
