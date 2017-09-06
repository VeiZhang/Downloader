package com.excellence.downloader.scheduler;

import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.excellence.compiler.ProxyConstance.CANCEL;
import static com.excellence.compiler.ProxyConstance.ERROR;
import static com.excellence.compiler.ProxyConstance.PRE_EXECUTE;
import static com.excellence.compiler.ProxyConstance.PROGRESS_CHANGE;
import static com.excellence.compiler.ProxyConstance.PROGRESS_SPEED_CHANGE;
import static com.excellence.compiler.ProxyConstance.PROXY_SUFFIX_DOWNLOADE;
import static com.excellence.compiler.ProxyConstance.SUCCESS;
import static java.util.Collections.unmodifiableSet;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     time   : 2017/8/30
 *     desc   : 事件调度器，用于处理任务状态的调度
 * </pre>
 */

public class DownloadScheduler<TASK> implements ISchedulerListener<TASK>
{
	public static final String TAG = DownloadScheduler.class.getSimpleName();

	private Set<String> mDownloadCounter = null;
	private Map<String, ISchedulerListener<TASK>> mSchedulerListeners = new ConcurrentHashMap<>();
	private Map<String, SchedulerListener<TASK>> mObservers = new ConcurrentHashMap<>();

	public DownloadScheduler()
	{
		initDownloadCounter();
	}

	/**
	 * 代理参数获取
	 */
	private void initDownloadCounter()
	{
		try
		{
			Class clazz = Class.forName("com.excellence.downloader.ProxyClassCounter");
			Method download = clazz.getMethod("getDownloadCounter");
			Object object = clazz.newInstance();
			Object downloadCounter = download.invoke(object);
			if (downloadCounter != null)
				mDownloadCounter = unmodifiableSet((Set<String>) downloadCounter);
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (NoSuchMethodException e)
		{
			e.printStackTrace();
		}
		catch (InstantiationException e)
		{
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
		catch (InvocationTargetException e)
		{
			e.printStackTrace();
		}
	}

	public void register(Object obj)
	{
		String targetName = obj.getClass().getName();
		if (mDownloadCounter != null && mDownloadCounter.contains(targetName))
		{
			SchedulerListener<TASK> listener = mObservers.get(targetName);
			if (listener == null)
			{
				listener = createListener(targetName);
				if (listener != null)
				{
					listener.setListener(obj);
					mObservers.put(targetName, listener);
				}
				else
					Log.e(TAG, "注册失败，没有【" + targetName + "】观察者");
			}
		}
	}

	private SchedulerListener<TASK> createListener(String targetName)
	{
		SchedulerListener<TASK> listener = null;
		try
		{
			Class clazz = Class.forName(targetName + getProxySuffix());
			listener = (SchedulerListener<TASK>) clazz.newInstance();
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (InstantiationException e)
		{
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
		return listener;
	}

	public void unregister(Object obj)
	{
		for (Iterator<Entry<String, SchedulerListener<TASK>>> iterator = mObservers.entrySet().iterator(); iterator.hasNext();)
		{
			Entry<String, SchedulerListener<TASK>> entry = iterator.next();
			if (entry.getKey().equals(obj.getClass().getName()))
				iterator.remove();
		}
	}

	private String getProxySuffix()
	{
		return PROXY_SUFFIX_DOWNLOADE;
	}

	private void handleTask(int status, TASK task)
	{
		if (mObservers.size() > 0)
		{
			Set<String> keys = mObservers.keySet();
			for (String key : keys)
			{
				ISchedulerListener<TASK> listener = mObservers.get(key);
				handleTask(status, listener, task);
			}
		}
	}

	private void handleTask(int status, ISchedulerListener<TASK> listener, TASK task)
	{
		switch (status)
		{
		case PRE_EXECUTE:
			listener.onPreExecute(task);
			break;

		case PROGRESS_CHANGE:
			listener.onProgressChange(task);
			break;

		case PROGRESS_SPEED_CHANGE:
			listener.onProgressSpeedChange(task);
			break;

		case CANCEL:
			listener.onCancel(task);
			break;

		case ERROR:
			listener.onError(task);
			break;

		case SUCCESS:
			listener.onSuccess(task);
			break;
		}
	}

	@Override
	public void onPreExecute(TASK task)
	{
		handleTask(PRE_EXECUTE, task);
	}

	@Override
	public void onProgressChange(TASK task)
	{
		handleTask(PROGRESS_CHANGE, task);
	}

	@Override
	public void onProgressSpeedChange(TASK task)
	{
		handleTask(PROGRESS_SPEED_CHANGE, task);
	}

	@Override
	public void onCancel(TASK task)
	{
		handleTask(CANCEL, task);
	}

	@Override
	public void onError(TASK task)
	{
		handleTask(ERROR, task);
	}

	@Override
	public void onSuccess(TASK task)
	{
		handleTask(SUCCESS, task);
	}

}
