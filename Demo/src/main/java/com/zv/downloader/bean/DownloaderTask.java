package com.zv.downloader.bean;

import android.widget.Button;
import android.widget.ProgressBar;

import com.excellence.downloader.FileDownloader;
import com.zv.downloader.R;

/**
 * Created by ZhangWei on 2017/2/17.
 */

public class DownloaderTask
{
	private String mFileName = null;
	private String mFileUrl = null;
	private long mDownloadLength;
	private long mFileSize;
	private FileDownloader mFileDownloader = null;

	private Button mStartBtn = null;
	private Button mDeleteBtn = null;
	private ProgressBar mProgressBar = null;

	public DownloaderTask(String fileName, String fileUrl)
	{
		mFileName = fileName;
		mFileUrl = fileUrl;
	}

	public String getFileName()
	{
		return mFileName;
	}

	public void setFileName(String fileName)
	{
		mFileName = fileName;
	}

	public String getFileUrl()
	{
		return mFileUrl;
	}

	public void setFileUrl(String fileUrl)
	{
		mFileUrl = fileUrl;
	}

	public long getDownloadLength()
	{
		return mDownloadLength;
	}

	public void setDownloadLength(long downloadLength)
	{
		mDownloadLength = downloadLength;
	}

	public long getFileSize()
	{
		return mFileSize;
	}

	public void setFileSize(long fileSize)
	{
		mFileSize = fileSize;
	}

	public FileDownloader getFileDownloader()
	{
		return mFileDownloader;
	}

	public void setFileDownloader(FileDownloader fileDownloader)
	{
		mFileDownloader = fileDownloader;
	}

	public Button getStartBtn()
	{
		return mStartBtn;
	}

	public void setStartBtn(Button startBtn)
	{
		mStartBtn = startBtn;
	}

	public Button getDeleteBtn()
	{
		return mDeleteBtn;
	}

	public void setDeleteBtn(Button deleteBtn)
	{
		mDeleteBtn = deleteBtn;
	}

	public ProgressBar getProgressBar()
	{
		return mProgressBar;
	}

	public void setProgressBar(ProgressBar progressBar)
	{
		mProgressBar = progressBar;
	}

	public void invalidateTask()
	{
		if (mFileDownloader != null)
		{
			switch (mFileDownloader.getState())
			{
			case FileDownloader.STATE_DOWNLOADING:
				mProgressBar.setProgress((int) mDownloadLength);
				mProgressBar.setMax((int) mFileSize);
				mStartBtn.setText(R.string.state_pause);
				break;

			case FileDownloader.STATE_ERROR:
				mStartBtn.setText(R.string.state_error);
				break;

			case FileDownloader.STATE_PAUSE:
				mStartBtn.setText(R.string.state_continue);
				break;

			case FileDownloader.STATE_DISCARD:
				mProgressBar.setProgress(0);
				mStartBtn.setText(R.string.state_start);
				break;

			case FileDownloader.STATE_SUCCESS:
				mStartBtn.setText(R.string.state_success);
				break;

			}
		}
		else
		{
			mProgressBar.setProgress(0);
			mStartBtn.setText(R.string.state_start);
		}
	}
}
