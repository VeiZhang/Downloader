package com.excellence.downloader;

import android.util.Log;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     time   : 2017/8/11
 *     desc   :
 * </pre>
 */

abstract class HttpTask implements Runnable
{
	private static final String TAG = HttpTask.class.getSimpleName();

	protected static final int CONNECT_TIME_OUT = 30 * 1000;

	private static final int MAX_REQUEST_COUNT = 3;

	private int mRequestCount = 0;

	@Override
	public void run()
	{
		do
		{
			Log.e(TAG, "Retry request " + mRequestCount);
			if (buildRequest())
			{
				break;
			}
		} while (mRequestCount < MAX_REQUEST_COUNT);
		mRequestCount = 0;
	}

	protected boolean retry()
	{
		mRequestCount++;
		return mRequestCount >= MAX_REQUEST_COUNT;
	}

	protected abstract boolean buildRequest();

}
