package com.excellence.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
	@Retention(RetentionPolicy.CLASS)
	@Target(ElementType.METHOD)
	@interface onPreExecute
	{
		String[] value() default { Constant.NO_URL };
	}
}
