package com.excellence.downloader.scheduler;

import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.excellence.compiler.ProxyConstance.PROXY_SUFFIX_DOWNLOADE;
import static java.util.Collections.unmodifiableSet;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     time   : 2017/8/30
 *     desc   : 事件调度器，用于处理任务状态的调度
 * </pre>
 */

public class DownloadScheduler<TASK>
{
	public static final String TAG = DownloadScheduler.class.getSimpleName();

	private static DownloadScheduler mInstance = null;

	private Set<String> mDownloadCounter = null;
	private Map<String, ISchedulerListener<TASK>> mSchedulerListeners = new ConcurrentHashMap<>();
	private Map<String, SchedulerListener<TASK>> mObservers = new ConcurrentHashMap<>();

	public static DownloadScheduler getInstance()
	{
		if (mInstance == null)
			mInstance = new DownloadScheduler();
		return mInstance;
	}

	private DownloadScheduler()
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
		String className = obj.getClass().getName();
		if (mDownloadCounter != null && mDownloadCounter.contains(className))
		{
			SchedulerListener<TASK> listener = mObservers.get(className);
			if (listener == null)
			{
				listener = createListener(className);
				if (listener != null)
				{
					listener.setListener(obj);
					mObservers.put(className, listener);
				}
				else
					Log.e(TAG, "注册失败，没有【" + className + "】观察者");
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

	private String getProxySuffix()
	{
		return PROXY_SUFFIX_DOWNLOADE;
	}
}
