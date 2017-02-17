package com.zv.downloader.bean;

import android.widget.Button;
import android.widget.ProgressBar;

import com.excellence.downloader.FileDownloader;

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
}
