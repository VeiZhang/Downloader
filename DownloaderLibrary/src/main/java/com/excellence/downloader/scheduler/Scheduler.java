package com.excellence.downloader.scheduler;

import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.excellence.compiler.ProxyConstant.CANCEL;
import static com.excellence.compiler.ProxyConstant.ERROR;
import static com.excellence.compiler.ProxyConstant.PRE_EXECUTE;
import static com.excellence.compiler.ProxyConstant.PROGRESS_CHANGE;
import static com.excellence.compiler.ProxyConstant.PROGRESS_SPEED_CHANGE;
import static com.excellence.compiler.ProxyConstant.PROXY_COUNTER_NAME;
import static com.excellence.compiler.ProxyConstant.PROXY_COUNTER_PACKAGE;
import static com.excellence.compiler.ProxyConstant.SUCCESS;
import static java.util.Collections.unmodifiableSet;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     time   : 2017/9/11
 *     desc   :
 * </pre>
 */

public abstract class Scheduler<TASK> implements ISchedulerListener<TASK> {
    private static final String TAG = Scheduler.class.getSimpleName();

    private Set<String> mAnnotationCounter = null;
    private Map<String, ISchedulerListener<TASK>> mSchedulerListeners = new ConcurrentHashMap<>();
    private Map<String, SchedulerListener<TASK>> mObservers = new ConcurrentHashMap<>();

    public Scheduler() {
        initCounter();
    }

    /**
     * 初始化代理参数，获取注解类集合
     */
    private void initCounter() {
        try {
            Class clazz = Class.forName(PROXY_COUNTER_PACKAGE + "." + PROXY_COUNTER_NAME);
            Method download = clazz.getMethod(getMethodCounter());
            Object object = clazz.newInstance();
            Object downloadCounter = download.invoke(object);
            if (downloadCounter != null) {
                mAnnotationCounter = unmodifiableSet((Set<String>) downloadCounter);
            }
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "initCounter: " + e.toString());
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "initCounter: " + e.toString());
        } catch (InstantiationException e) {

            Log.e(TAG, "initCounter: " + e.toString());
        } catch (IllegalAccessException e) {
            Log.e(TAG, "initCounter: " + e.toString());
        } catch (InvocationTargetException e) {
            Log.e(TAG, "initCounter: " + e.toString());
        }
    }

    /**
     * 注册，创建监听类
     *
     * @param obj
     */
    public void register(Object obj) {
        String targetName = obj.getClass().getName();
        if (mAnnotationCounter != null && mAnnotationCounter.contains(targetName)) {
            SchedulerListener<TASK> listener = mObservers.get(targetName);
            if (listener == null) {
                listener = createListener(targetName);
                if (listener != null) {
                    listener.setListener(obj);
                    mObservers.put(targetName, listener);
                } else {
                    Log.e(TAG, "注册失败，没有【" + targetName + "】观察者");
                }
            }
        }
    }

    private SchedulerListener<TASK> createListener(String targetName) {
        SchedulerListener<TASK> listener = null;
        try {
            Class clazz = Class.forName(targetName + getProxySuffix());
            listener = (SchedulerListener<TASK>) clazz.newInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return listener;
    }

    /**
     * 解绑
     *
     * @param obj
     */
    public void unregister(Object obj) {
        for (Iterator<Map.Entry<String, SchedulerListener<TASK>>> iterator = mObservers.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, SchedulerListener<TASK>> entry = iterator.next();
            if (entry.getKey().equals(obj.getClass().getName())) {
                iterator.remove();
            }
        }
    }

    private void handleTask(int status, TASK task) {
        if (mObservers.size() > 0) {
            Set<String> keys = mObservers.keySet();
            for (String key : keys) {
                ISchedulerListener<TASK> listener = mObservers.get(key);
                handleTask(status, listener, task);
            }
        }
    }

    private void handleTask(int status, ISchedulerListener<TASK> listener, TASK task) {
        switch (status) {
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

            default:
                break;
        }
    }

    @Override
    public void onPreExecute(TASK task) {
        handleTask(PRE_EXECUTE, task);
    }

    @Override
    public void onProgressChange(TASK task) {
        handleTask(PROGRESS_CHANGE, task);
    }

    @Override
    public void onProgressSpeedChange(TASK task) {
        handleTask(PROGRESS_SPEED_CHANGE, task);
    }

    @Override
    public void onCancel(TASK task) {
        handleTask(CANCEL, task);
    }

    @Override
    public void onError(TASK task) {
        handleTask(ERROR, task);
    }

    @Override
    public void onSuccess(TASK task) {
        handleTask(SUCCESS, task);
    }

    protected abstract String getProxySuffix();

    protected abstract String getMethodCounter();
}
