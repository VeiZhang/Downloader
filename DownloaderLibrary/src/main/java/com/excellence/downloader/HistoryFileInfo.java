package com.excellence.downloader;

/**
 * Created by ZhangWei on 2016/2/20.
 */
public class HistoryFileInfo
{
	private String name;
	private int flag;
	private int threadId;
	private int downloadLength;

	public HistoryFileInfo()
	{

	}

	public HistoryFileInfo(String name, int threadId, int downloadLength)
	{
		this.name = name;
		this.threadId = threadId;
		this.downloadLength = downloadLength;
	}

	public int getDownloadLength()
	{
		return downloadLength;
	}

	public void setDownloadLength(int downloadLength)
	{
		this.downloadLength = downloadLength;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public int getThreadId()
	{
		return threadId;
	}

	public void setThreadId(int threadId)
	{
		this.threadId = threadId;
	}

	public int getFlag()
	{
		return flag;
	}

	public void setFlag(int flag)
	{
		this.flag = flag;
	}

}
