package com.excellence.downloader.utils;

import android.util.Log;

import com.excellence.downloader.FileDownloader;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

/**
 * Created by ZhangWei on 2017/3/1.
 */

public class HttpUtils
{
    private static final String TAG = FileDownloader.class.getSimpleName();

	public HttpUtils()
	{
		super();
	}

    /**
     * Http响应头字段
     *
     * @param connection http
     */
	public static void printHeader(HttpURLConnection connection)
	{
		Map<String, List<String>> headerFields = connection.getHeaderFields();
		for (Map.Entry<String, List<String>> field : headerFields.entrySet())
		{
			Log.i(TAG, "[Key : " + field.getKey() + "][value : " + field.getValue() + "]");
		}
	}

    /**
     * 是否支持断点
     *
     * @param connection http
     */
	public static boolean isSupportRange(HttpURLConnection connection)
	{
		List<String> values = getHeader(connection, "Accept-Ranges");
		return values != null && values.contains("bytes");
	}

	private static List<String> getHeader(HttpURLConnection connection, String key)
	{
		Map<String, List<String>> headerFields = connection.getHeaderFields();
        return headerFields.get(key);
	}
}
