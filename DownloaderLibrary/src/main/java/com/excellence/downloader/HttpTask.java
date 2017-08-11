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

public abstract class HttpTask implements Runnable
{
	public static final String TAG = HttpTask.class.getSimpleName();

	private static final int MAX_REQUEST_COUNT = 3;

	private int mRequestCount = 0;

	@Override
	public void run()
	{
		do
		{
			Log.e(TAG, "Retry request " + mRequestCount);
			if (buildRequest())
				break;
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
