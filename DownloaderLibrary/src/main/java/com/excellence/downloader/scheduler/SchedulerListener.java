package com.excellence.downloader.scheduler;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     time   : 2017/8/30
 *     desc   :
 * </pre>
 */

public class SchedulerListener<TASK> implements ISchedulerListener<TASK>
{
	@Override
	public void onPreExecute(TASK task)
	{

	}

	@Override
	public void onProgressChange(TASK task)
	{

	}

	@Override
	public void onProgressSpeedChange(TASK task)
	{

	}

	@Override
	public void onCancel(TASK task)
	{

	}

	@Override
	public void onError(TASK task)
	{

	}

	@Override
	public void onSuccess(TASK task)
	{

	}

	public void setListener(Object obj)
	{

	}
}
