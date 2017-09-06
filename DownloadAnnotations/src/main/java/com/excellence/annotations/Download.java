package com.excellence.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.excellence.annotations.Constant.NO_URL;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     time   : 2017/8/15
 *     desc   :
 * </pre>
 */

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface Download
{
	/**
	 * 下载开始，获取文件大小
	 * @see com.excellence.downloader.FileDownloader.DownloadTask#getFileSize()
	 *
	 */
	@Retention(RetentionPolicy.CLASS)
	@Target(ElementType.METHOD)
	@interface onPreExecute
	{
		String[] value() default { NO_URL };
	}

	/**
	 * 下载进行中
	 * @see com.excellence.downloader.FileDownloader.DownloadTask#getDownloadLength()
	 */
	@Retention(RetentionPolicy.CLASS)
	@Target(ElementType.METHOD)
	@interface onProgressChange
	{
		String[] value() default { NO_URL };
	}

	/**
	 * 下载进行中，下载速度byte/s
	 * @see com.excellence.downloader.FileDownloader.DownloadTask#getDownloadSpeed()
	 *
	 */
	@Retention(RetentionPolicy.CLASS)
	@Target(ElementType.METHOD)
	@interface onProgressSpeedChange
	{
		String[] value() default { NO_URL };
	}

	/**
	 * 暂停下载
	 */
	@Retention(RetentionPolicy.CLASS)
	@Target(ElementType.METHOD)
	@interface onCancel
	{
		String[] value() default { NO_URL };
	}

	/**
	 * 下载错误
	 */
	@Retention(RetentionPolicy.CLASS)
	@Target(ElementType.METHOD)
	@interface onError
	{
		String[] value() default { NO_URL };
	}

	/**
	 * 下载成功
	 */
	@Retention(RetentionPolicy.CLASS)
	@Target(ElementType.METHOD)
	@interface onSuccess
	{
		String[] value() default { NO_URL };
	}
}
