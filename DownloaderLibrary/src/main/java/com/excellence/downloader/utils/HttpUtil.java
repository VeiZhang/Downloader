package com.excellence.downloader.utils;

import static com.excellence.downloader.utils.CommonUtil.hasDoubleCharacter;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.excellence.downloader.entity.TaskEntity;

import android.text.TextUtils;
import android.util.Log;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     time   : 2017/8/9
 *     desc   : 工具类
 * </pre>
 */

public class HttpUtil
{
	public static final String TAG = HttpUtil.class.getSimpleName();

	/**
	 * 转换链接中中文字符
	 *
	 * @param url
	 * @return
	 */
	public static String convertUrl(String url)
	{
		if (TextUtils.isEmpty(url))
			return url;

		if (hasDoubleCharacter(url))
		{
			String regex = "[^\\x00-\\xff]";
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(url);
			Set<String> strs = new HashSet<>();
			while (matcher.find())
			{
				strs.add(matcher.group());
			}

			try
			{
				for (String str : strs)
					url = url.replaceAll(str, URLEncoder.encode(str, "UTF-8"));
			}
			catch (UnsupportedEncodingException e)
			{
				e.printStackTrace();
			}
		}
		return url;
	}

	public static void setConnectParam(HttpURLConnection conn, String url) throws Exception
	{
		// 设置 HttpURLConnection的请求方式
		// default request : GET
		conn.setRequestMethod("GET");
		// 设置 HttpURLConnection的接收的文件类型
		conn.setRequestProperty("Accept",
				"image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
		// 设置 HttpURLConnection的接收语音
		conn.setRequestProperty("Accept-Language", "zh-CN");
		// 指定请求uri的源资源地址
		conn.setRequestProperty("Referer", url);
		// 设置 HttpURLConnection的字符编码
		conn.setRequestProperty("Charset", "UTF-8");
		// 检查浏览页面的访问者在用什么操作系统（包括版本号）浏览器（包括版本号）和用户个人偏好
		conn.setRequestProperty("User-Agent",
				"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
		conn.setRequestProperty("Connection", "Keep-Alive");
	}

	/**
	 * 打印全部请求头信息
	 *
	 * @param conn
	 */
	public static void printHeader(HttpURLConnection conn)
	{
		Map<String, List<String>> headerFields = conn.getHeaderFields();
		for (Entry<String, List<String>> field : headerFields.entrySet())
		{
			Log.i(TAG, "[key : " + field.getKey() + "][value : " + field.getValue() + "]");
		}
	}

	/**
	 * 获取具体的请求头信息
	 *
	 * @param conn
	 * @param key
	 * @return
	 */
	public static List<String> getHeader(HttpURLConnection conn, String key)
	{
		Map<String, List<String>> headerFields = conn.getHeaderFields();
		return headerFields.get(key);
	}

	public static String formatRequestMsg(TaskEntity taskEntity)
	{
		StringBuilder sp = new StringBuilder();
		sp.append("Task [").append(taskEntity.url).append("]");
		sp.append(" ").append("Request code : ").append(taskEntity.code);
		return sp.toString();
	}

	public static String formatRequestMsg(TaskEntity taskEntity, String msg)
	{
		StringBuilder sp = new StringBuilder();
		sp.append("Task [").append(taskEntity.url).append("]");
		sp.append(" ").append("Request msg : ").append(msg);
		return sp.toString();
	}

}
