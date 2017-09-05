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
	 */
	@Retention(RetentionPolicy.CLASS)
	@Target(ElementType.METHOD)
	@interface onPreExecute
	{
		String[] value() default { NO_URL };
	}

	/**
	 * 下载进行中
	 */
	@Retention(RetentionPolicy.CLASS)
	@Target(ElementType.METHOD)
	@interface onProgressChange
	{
		String[] value() default { NO_URL };
	}
}
