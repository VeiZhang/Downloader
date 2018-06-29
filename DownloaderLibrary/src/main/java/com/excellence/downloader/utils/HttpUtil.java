package com.excellence.downloader.utils;

import static com.excellence.downloader.utils.CommonUtil.hasDoubleCharacter;

import java.io.IOException;
import java.io.InputStream;
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
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

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
		{
			return url;
		}

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
				{
					url = url.replaceAll(str, URLEncoder.encode(str, "UTF-8"));
				}
			}
			catch (UnsupportedEncodingException e)
			{
				e.printStackTrace();
			}
		}
		return url;
	}

	public static InputStream convertInputStream(HttpURLConnection conn) throws IOException
	{
		InputStream is = conn.getInputStream();
		String encoding = conn.getHeaderField("Content-Encoding");
		Log.i(TAG, "convertInputStream: " + encoding);
		if (TextUtils.isEmpty(encoding))
		{
			return is;
		}

		switch (encoding)
		{
		case "gzip":
			return new GZIPInputStream(is);

		case "deflate":
			return new InflaterInputStream(is);

		default:
			return is;
		}
	}

	public static void setConnectParam(HttpURLConnection conn, String url) throws Exception
	{
		// 设置 HttpURLConnection的请求方式
		// default request : GET
		conn.setRequestMethod("GET");
		// 设置 HttpURLConnection的接收的文件类型
		StringBuilder accept = new StringBuilder();
		accept.append("image/gif, ")
				.append("image/jpeg, ")
				.append("image/pjpeg, ")
				.append("image/webp, ")
				.append("image/apng, ")
				.append("application/xml, ")
				.append("application/xaml+xml, ")
				.append("application/xhtml+xml, ")
				.append("application/x-shockwave-flash, ")
				.append("application/x-ms-xbap, ")
				.append("application/x-ms-application, ")
				.append("application/msword, ")
				.append("application/vnd.ms-excel, ")
				.append("application/vnd.ms-xpsdocument, ")
				.append("application/vnd.ms-powerpoint, ")
				.append("text/plain, ")
				.append("text/html, ")
				.append("*/*");
		conn.setRequestProperty("Accept", accept.toString());
		// 设置接收的压缩格式
		conn.setRequestProperty("Accept-Encoding", "identity");
		// 指定请求uri的源资源地址
		conn.setRequestProperty("Referer", url);
		// 设置 HttpURLConnection的字符编码
		conn.setRequestProperty("Charset", "UTF-8");
		// 检查浏览页面的访问者在用什么操作系统（包括版本号）浏览器（包括版本号）和用户个人偏好
		conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");
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
