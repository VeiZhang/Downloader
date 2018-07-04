package com.zv.downloader.bean;

import static com.excellence.downloader.entity.TaskEntity.STATUS_DISCARD;
import static com.excellence.downloader.entity.TaskEntity.STATUS_DOWNLOADING;
import static com.excellence.downloader.entity.TaskEntity.STATUS_ERROR;
import static com.excellence.downloader.entity.TaskEntity.STATUS_PAUSE;
import static com.excellence.downloader.entity.TaskEntity.STATUS_SUCCESS;

import com.excellence.downloader.FileDownloader.DownloadTask;
import com.zv.downloader.R;

import android.widget.Button;
import android.widget.ProgressBar;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : https://veizhang.github.io/
 *     time   : 2017/2/17
 *     desc   : 下载任务
 * </pre>
 */

public class Task
{
	private String mFileName = null;
	private String mFileUrl = null;
	private long mDownloadLength;
	private long mFileSize;
	private DownloadTask mDownloadTask = null;

	private Button mStartBtn = null;
	private Button mDeleteBtn = null;
	private ProgressBar mProgressBar = null;

	public Task(String fileName, String fileUrl)
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

	public DownloadTask getDownloadTask()
	{
		return mDownloadTask;
	}

	public void setDownloadTask(DownloadTask downloadTask)
	{
		mDownloadTask = downloadTask;
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
		if (mDownloadTask != null)
		{
			switch (mDownloadTask.getStatus())
			{
			case STATUS_DOWNLOADING:
				mProgressBar.setProgress((int) mDownloadLength);
				mProgressBar.setMax((int) mFileSize);
				// mStartBtn.setText(R.string.state_pause);
				break;

			case STATUS_ERROR:
				mStartBtn.setText(R.string.state_error);
				break;

			case STATUS_PAUSE:
				mStartBtn.setText(R.string.state_continue);
				break;

			case STATUS_DISCARD:
				mProgressBar.setProgress(0);
				mStartBtn.setText(R.string.state_start);
				break;

			case STATUS_SUCCESS:
				mProgressBar.setProgress(100);
				mProgressBar.setMax(100);
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

	public void setSpeed(String speed)
	{
		mStartBtn.setText(speed);
	}
}
