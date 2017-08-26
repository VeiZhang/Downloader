package com.excellence.compiler;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     time   : 2017/8/25
 *     desc   : 创建代理方法的参数
 * </pre>
 */

class ProxyMethodParam
{
	String packageName;
	String className;
	Set<TaskEnum> taskEnums;
	Map<String, Set<String>> keyMappings = new HashMap<>();
	Map<TaskEnum, Map<Class<? extends Annotation>, String>> methods = new HashMap<>();
}
