package com.excellence.downloader.utils;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

import com.excellence.downloader.FileDownloader;

import android.util.Log;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : https://veizhang.github.io/
 *     time   : 2017/3/1
 *     desc   : 下载工具类
 * </pre>
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

	/**
	 * 获取头信息
	 *
	 * @param connection http
	 * @param key 键
	 * @return 键值
	 */
	private static List<String> getHeader(HttpURLConnection connection, String key)
	{
		Map<String, List<String>> headerFields = connection.getHeaderFields();
		return headerFields.get(key);
	}
}
