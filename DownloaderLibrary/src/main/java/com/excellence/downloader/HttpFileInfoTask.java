package com.excellence.downloader;

import static com.excellence.downloader.utils.CommonUtil.checkNULL;
import static com.excellence.downloader.utils.HttpUtil.convertUrl;
import static com.excellence.downloader.utils.HttpUtil.formatRequestMsg;
import static com.excellence.downloader.utils.HttpUtil.printHeader;
import static com.excellence.downloader.utils.HttpUtil.setConnectParam;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_PARTIAL;

import java.net.HttpURLConnection;
import java.net.URL;

import com.excellence.downloader.entity.TaskEntity;
import com.excellence.downloader.exception.DownloadError;
import com.excellence.downloader.exception.FileError;
import com.excellence.downloader.exception.URLInvalidError;
import com.excellence.downloader.utils.OnFileInfoCallback;

import android.text.TextUtils;
import android.util.Log;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     time   : 2017/8/10
 *     desc   : 请求头信息
 * </pre>
 */

public class HttpFileInfoTask extends HttpTask
{
	public static final String TAG = HttpFileInfoTask.class.getSimpleName();

	private TaskEntity mTaskEntity = null;
	private OnFileInfoCallback mOnFileInfoCallback = null;

	protected HttpFileInfoTask(TaskEntity taskEntity, OnFileInfoCallback callback)
	{
		mTaskEntity = taskEntity;
		mOnFileInfoCallback = callback;
	}

	@Override
	protected boolean buildRequest()
	{
		HttpURLConnection conn = null;
		try
		{
			Log.e(TAG, "Request file info");
			if (checkNULL(mTaskEntity.url))
			{
				throw new URLInvalidError("URL is invalid");
			}

			URL httpURL = new URL(convertUrl(mTaskEntity.url));
			conn = (HttpURLConnection) httpURL.openConnection();
			conn.setConnectTimeout(CONNECT_TIME_OUT);

			/**
			 * 判断服务器是否支持断点，{@code 206}:支持<br>{@code 200}:不支持
			 * @see #handleHeader(HttpURLConnection)
			 **/
			conn.setRequestProperty("Range", "bytes=" + 0 + "-");
			setConnectParam(conn, mTaskEntity.url);
			conn.connect();

			if (mTaskEntity.isCancel)
			{
				mOnFileInfoCallback.onCancel();
				return true;
			}

			printHeader(conn);
			handleHeader(conn);
			mOnFileInfoCallback.onComplete();
		}
		catch (Exception e)
		{
			if (retry())
			{
				mOnFileInfoCallback.onError(new DownloadError(e));
				return true;
			}
			else
			{
				return false;
			}
		}
		finally
		{
			if (conn != null)
			{
				conn.disconnect();
			}
		}
		return true;
	}

	private void handleHeader(HttpURLConnection conn) throws Exception
	{
		long len = conn.getContentLength();
		if (len < 0)
		{
			String temp = conn.getHeaderField("Content-Length");
			len = TextUtils.isEmpty(temp) ? -1 : Long.parseLong(temp);
		}

		if (len < 0)
		{
			throw new FileError(formatRequestMsg(mTaskEntity, "File length is error"));
		}

		int code = conn.getResponseCode();
		mTaskEntity.fileSize = len;
		mTaskEntity.code = code;
		Log.e(TAG, formatRequestMsg(mTaskEntity));
		switch (code)
		{
		case HTTP_OK:
			mTaskEntity.isSupportBP = false;
			break;

		case HTTP_PARTIAL:
			mTaskEntity.isSupportBP = true;
			break;

		default:
			throw new FileError(formatRequestMsg(mTaskEntity));
		}
	}

}
