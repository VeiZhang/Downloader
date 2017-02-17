package com.zv.downloader.bean;

/**
 * Created by ZhangWei on 2017/2/15.
 */

import android.widget.Button;
import android.widget.ProgressBar;

import com.vincestyling.netroid.toolbox.FileDownloader.DownloadController;
import com.zv.downloader.R;

public class NetroidTask
{
	private String mFileName = null;
	private String mFileUrl = null;
	private long mDownloadLength;
	private long mFileSize;
	private DownloadController mDownloadController = null;

	private Button mStartBtn = null;
	private Button mDeleteBtn = null;
	private ProgressBar mProgressBar = null;

	public NetroidTask(String fileName, String fileUrl)
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

	public DownloadController getDownloadController()
	{
		return mDownloadController;
	}

	public void setDownloadController(DownloadController downloadController)
	{
		mDownloadController = downloadController;
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
		if (mDownloadController != null)
		{
			switch (mDownloadController.getStatus())
			{
			case DownloadController.STATUS_DOWNLOADING:
			case DownloadController.STATUS_WAITING:
				mProgressBar.setProgress((int) mDownloadLength);
				mProgressBar.setMax((int) mFileSize);
				mStartBtn.setText(R.string.state_pause);
				break;

			case DownloadController.STATUS_PAUSE:
				mStartBtn.setText(R.string.state_continue);
				break;

			case DownloadController.STATUS_DISCARD:
				mProgressBar.setProgress(0);
				mStartBtn.setText(R.string.state_start);
				break;

			case DownloadController.STATUS_SUCCESS:
				if (mFileSize == mDownloadLength)
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
