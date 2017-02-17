package com.zv.downloader.bean;

import android.app.Activity;

/**
 * Created by ZhangWei on 2017/2/15.
 */

public class ActivityInfo
{
	private String mActivityName = null;
	private Class<? extends Activity> mActivityCls = null;

	public ActivityInfo(String activityName, Class<? extends Activity> activityCls)
	{
		mActivityName = activityName;
		mActivityCls = activityCls;
	}

	public void setActivityName(String activityName)
	{
		mActivityName = activityName;
	}

	public void setActivityCls(Class<? extends Activity> activityCls)
	{
		mActivityCls = activityCls;
	}

	public String getActivityName()
	{
		return mActivityName;
	}

	public Class<? extends Activity> getActivityCls()
	{
		return mActivityCls;
	}

}
