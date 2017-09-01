package com.excellence.downloader.scheduler;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     time   : 2017/8/30
 *     desc   : 注解任务监听
 * </pre>
 */

public interface ISchedulerListener<TASK>
{
	void onPre(TASK task);
}
