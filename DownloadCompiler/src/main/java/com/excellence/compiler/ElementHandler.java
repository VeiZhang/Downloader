package com.excellence.compiler;

import static com.excellence.compiler.ProxyConstant.CANCEL;
import static com.excellence.compiler.ProxyConstant.CLS_SCHEDULER_LISTENER;
import static com.excellence.compiler.ProxyConstant.COUNT_DOWNLOAD;
import static com.excellence.compiler.ProxyConstant.COUNT_METHOD_DOWNLOAD;
import static com.excellence.compiler.ProxyConstant.ERROR;
import static com.excellence.compiler.ProxyConstant.LISTENER_KEY_MAP;
import static com.excellence.compiler.ProxyConstant.PKG_SCHEDULER;
import static com.excellence.compiler.ProxyConstant.PRE_EXECUTE;
import static com.excellence.compiler.ProxyConstant.PROGRESS_CHANGE;
import static com.excellence.compiler.ProxyConstant.PROGRESS_SPEED_CHANGE;
import static com.excellence.compiler.ProxyConstant.PROXY_COUNTER_MAP;
import static com.excellence.compiler.ProxyConstant.PROXY_COUNTER_NAME;
import static com.excellence.compiler.ProxyConstant.PROXY_COUNTER_PACKAGE;
import static com.excellence.compiler.ProxyConstant.SET_LISTENER;
import static com.excellence.compiler.ProxyConstant.SUCCESS;
import static com.excellence.compiler.TaskEnum.DOWNLOAD;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;

import com.excellence.annotations.Download;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     time   : 2017/8/25
 *     desc   : 元素处理
 * </pre>
 */

public class ElementHandler
{

	private Filer mFiler = null;
	private Elements mElementUtils = null;
	private Map<String, ProxyMethodParam> mMethods = new HashMap<>();
	private Map<String, Set<String>> mListenerClass = new HashMap<>();

	public ElementHandler(Filer filer, Elements elementUtils)
	{
		mFiler = filer;
		mElementUtils = elementUtils;
	}

	public void clean()
	{
		mMethods.clear();
	}

	/**
	 * VariableElement 一般代表成员变量
	 * ExecutableElement 一般代表类中的方法
	 * TypeElement 一般代表代表类
	 * PackageElement 一般代表Package
	 */
	public void handleDownload(RoundEnvironment roundEnv)
	{
		saveMethod(DOWNLOAD, roundEnv, Download.onPreExecute.class, PRE_EXECUTE);
		saveMethod(DOWNLOAD, roundEnv, Download.onProgressChange.class, PROGRESS_CHANGE);
		saveMethod(DOWNLOAD, roundEnv, Download.onProgressSpeedChange.class, PROGRESS_SPEED_CHANGE);
		saveMethod(DOWNLOAD, roundEnv, Download.onCancel.class, CANCEL);
		saveMethod(DOWNLOAD, roundEnv, Download.onError.class, ERROR);
		saveMethod(DOWNLOAD, roundEnv, Download.onSuccess.class, SUCCESS);
	}

	/**
	 * 查找并且保存扫描到的方法
	 * 如果有相同的注解方法，方法名不一样，后面的方法会覆盖前面，保存的方法是根据扫描到的方法自动创建：方法名、参数
	 *
	 * @param taskEnum
	 * @param roundEnv
	 * @param annotationClazz
	 * @param annotationType
	 */
	private void saveMethod(TaskEnum taskEnum, RoundEnvironment roundEnv, Class<? extends Annotation> annotationClazz, int annotationType)
	{
		for (Element element : roundEnv.getElementsAnnotatedWith(annotationClazz))
		{
			ElementKind kind = element.getKind();
			if (kind == ElementKind.METHOD)
			{
				ExecutableElement method = (ExecutableElement) element;
				TypeElement classElement = (TypeElement) method.getEnclosingElement();
				PackageElement packageElement = mElementUtils.getPackageOf(classElement);
				checkDownloadMethod(taskEnum, method);
				String methodName = method.getSimpleName().toString();
				String className = method.getEnclosingElement().toString();
				ProxyMethodParam proxyEntity = mMethods.get(className);
				if (proxyEntity == null)
				{
					proxyEntity = new ProxyMethodParam();
					proxyEntity.taskEnums = new HashSet<>();
					proxyEntity.packageName = packageElement.getQualifiedName().toString();
					proxyEntity.className = classElement.getSimpleName().toString();
					mMethods.put(className, proxyEntity);
				}
				proxyEntity.taskEnums.add(taskEnum);
				if (proxyEntity.methods.get(taskEnum) == null)
					proxyEntity.methods.put(taskEnum, new HashMap<Class<? extends Annotation>, String>());
				proxyEntity.methods.get(taskEnum).put(annotationClazz, methodName);
				proxyEntity.keyMappings.put(methodName, getValues(taskEnum, method, annotationType));
			}
		}
	}

	/**
	 * 获取注解内容
	 *
	 * @param taskEnum
	 * @param method
	 * @param annotationType
	 * @return
	 */
	private Set<String> getValues(TaskEnum taskEnum, ExecutableElement method, int annotationType)
	{
		String clsName = method.getEnclosingElement().toString();
		String[] keys = null;
		switch (taskEnum)
		{
		case DOWNLOAD:
			keys = getDownloadValues(method, annotationType);
			addListenerMapping(clsName, COUNT_DOWNLOAD);
			break;
		}
		return keys == null ? null : convertSet(keys);
	}

	/**
	 * 添加方法映射
	 *
	 * @param clsName 注解事件的类
	 * @param key {@link ProxyConstant#COUNT_DOWNLOAD}
	 */
	private void addListenerMapping(String clsName, String key)
	{
		Set<String> cls = mListenerClass.get(key);
		if (cls == null)
		{
			cls = new HashSet<>();
			mListenerClass.put(key, cls);
		}
		cls.add(clsName);
	}

	/**
	 * 获取下载的注解数据
	 *
	 * @param method
	 * @param annotationType
	 * @return
	 */
	private String[] getDownloadValues(ExecutableElement method, int annotationType)
	{
		String[] values = null;
		switch (annotationType)
		{
		case PRE_EXECUTE:
			values = method.getAnnotation(Download.onPreExecute.class).value();
			break;

		case PROGRESS_CHANGE:
			values = method.getAnnotation(Download.onProgressChange.class).value();
			break;

		case PROGRESS_SPEED_CHANGE:
			values = method.getAnnotation(Download.onProgressSpeedChange.class).value();
			break;

		case CANCEL:
			values = method.getAnnotation(Download.onCancel.class).value();
			break;

		case ERROR:
			values = method.getAnnotation(Download.onError.class).value();
			break;

		case SUCCESS:
			values = method.getAnnotation(Download.onSuccess.class).value();
			break;
		}
		return values;
	}

	/**
	 * 检查和下载相关的方法，如果被注解的方法为private或参数不合法：唯一参数类型{@link com.excellence.downloader.FileDownloader.DownloadTask}，否则抛出异常
	 *
	 * @param taskEnum
	 * @param method
	 */
	private void checkDownloadMethod(TaskEnum taskEnum, ExecutableElement method)
	{
		String methodName = method.getSimpleName().toString();
		String className = method.getEnclosingElement().toString();
		Set<Modifier> modifiers = method.getModifiers();
		if (modifiers.contains(Modifier.PRIVATE))
			throw new IllegalAccessError(className + "." + methodName + "不能为private方法");

		List<VariableElement> params = (List<VariableElement>) method.getParameters();
		String paramCls = taskEnum.getPkg() + "." + taskEnum.getClassName();
		if (params == null || params.size() != 1)
			throw new IllegalArgumentException(className + "." + methodName + "参数错误，参数只有一个，且参数必须是" + paramCls);
		if (!params.get(0).asType().toString().equals(paramCls))
			throw new IllegalArgumentException(className + "." + methodName + "参数[" + params.get(0).getSimpleName() + "]类型错误，参数必须是" + paramCls);
	}

	public void createProxyFile()
	{
		try
		{
			createProxyListenerFile();
			createProxyClassFile();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	/**
	 * 每一种注解对应的类集合
	 * 生成路径 Demo/build/generated/source/apt/debug/com/excellence/downloader/ProxyClassCounter
	 */
	private void createProxyClassFile() throws Exception
	{
		Set<String> keys = mListenerClass.keySet();
		TypeSpec.Builder builder = TypeSpec.classBuilder(PROXY_COUNTER_NAME).addModifiers(Modifier.PUBLIC, Modifier.FINAL);

		FieldSpec mappingField = FieldSpec
				.builder(ParameterizedTypeName.get(ClassName.get(Map.class), ClassName.get(String.class), ParameterizedTypeName.get(ClassName.get(Set.class), ClassName.get(String.class))),
						PROXY_COUNTER_MAP)
				.addModifiers(Modifier.PRIVATE).initializer("new $T()", HashMap.class).build();
		builder.addField(mappingField);

		// 增加构造函数
		CodeBlock.Builder cb = CodeBlock.builder();
		cb.add("Set<String> set = null;\n");
		for (String key : keys)
			addTypeData(key, mListenerClass.get(key), cb);
		MethodSpec structure = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC).addCode(cb.build()).build();
		builder.addMethod(structure);
		builder.addMethod(createMethod(COUNT_METHOD_DOWNLOAD, COUNT_DOWNLOAD));
		builder.addJavadoc("该文件为自动生成的代理文件，请不要修改该文件的任何代码！\n");
		JavaFile javaFile = JavaFile.builder(PROXY_COUNTER_PACKAGE, builder.build()).build();
		createFile(javaFile);
	}

	/**
	 * 创建不同任务类型的代理类集合
	 *
	 * @param methodName
	 * @param key
	 * @return
	 */
	private MethodSpec createMethod(String methodName, String key)
	{
		MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName);
		ParameterizedTypeName returnName = ParameterizedTypeName.get(ClassName.get(Set.class), ClassName.get(String.class));
		builder.addModifiers(Modifier.PUBLIC, Modifier.FINAL).returns(returnName).addCode("return " + PROXY_COUNTER_MAP + ".get(\"" + key + "\");\n");
		return builder.build();
	}

	/**
	 * 添加每一种注解对应类
	 *
	 * @param type {@link #addListenerMapping(String, String)}
	 * @param clsNames
	 * @param cb
	 */
	private void addTypeData(String type, Set<String> clsNames, CodeBlock.Builder cb)
	{
		if (clsNames == null || clsNames.isEmpty())
			return;
		StringBuilder sb = new StringBuilder();
		sb.append("set = new $T();\n");
		for (String clsName : clsNames)
			sb.append("set.add(\"").append(clsName).append("\");\n");
		sb.append("typeMapping.put(\"").append(type).append("\", ").append("set);\n");
		cb.add(sb.toString(), ClassName.get(HashSet.class));
	}

	/**
	 * 创建事件代理文件
	 * 生成路径 Demo/build/generated/source/apt/debug/com/zv/downloader/downloader/SingleThreadActivity$$DownloadListenerProxy
	 */
	private void createProxyListenerFile() throws Exception
	{
		Set<String> keys = mMethods.keySet();
		for (String key : keys)
		{
			ProxyMethodParam entity = mMethods.get(key);
			for (TaskEnum taskEnum : entity.taskEnums)
			{
				JavaFile javaFile = JavaFile.builder(entity.packageName, createProxyClass(entity, taskEnum)).build();
				createFile(javaFile);
			}
		}
	}

	/**
	 * 创建代理类，代理类中每个注解都是一个方法（实现的接口方法）
	 *
	 * @param entity
	 * @param taskEnum
	 * @return
	 */
	private TypeSpec createProxyClass(ProxyMethodParam entity, TaskEnum taskEnum)
	{
		TypeSpec.Builder builder = TypeSpec.classBuilder(entity.className + taskEnum.getProxySuffix()).addModifiers(Modifier.PUBLIC, Modifier.FINAL);

		// 添加被代理的类的字段
		ClassName obj = ClassName.get(entity.packageName, entity.className);
		FieldSpec observerField = FieldSpec.builder(obj, "obj").addModifiers(Modifier.PRIVATE).build();
		builder.addField(observerField);

		// 添加url映射表
		FieldSpec mappingField = FieldSpec
				.builder(ParameterizedTypeName.get(ClassName.get(Map.class), ClassName.get(String.class), ParameterizedTypeName.get(ClassName.get(Set.class), ClassName.get(String.class))),
						LISTENER_KEY_MAP)
				.addModifiers(Modifier.PRIVATE).initializer("new $T()", HashMap.class).build();
		builder.addField(mappingField);

		// 添加注解方法
		Map<Class<? extends Annotation>, String> temp = entity.methods.get(taskEnum);
		if (temp != null)
		{
			for (Class<? extends Annotation> annotation : temp.keySet())
			{
				MethodSpec method = createProxyMethod(taskEnum, annotation, temp.get(annotation));
				builder.addMethod(method);
			}
		}

		// 增加构造函数
		CodeBlock.Builder cb = CodeBlock.builder();
		cb.add("Set<String> set = null;\n");
		for (String methodName : entity.keyMappings.keySet())
		{
			Set<String> keys = entity.keyMappings.get(methodName);
			if (keys == null || keys.size() == 0)
				continue;
			StringBuilder sb = new StringBuilder();
			sb.append("set = new $T();\n");
			for (String key : keys)
			{
				if (key.isEmpty())
					continue;
				sb.append("set.add(\"").append(key).append("\");\n");
			}
			sb.append("keyMapping.put(\"").append(methodName).append("\", ").append("set);\n");
			cb.add(sb.toString(), ClassName.get(HashSet.class));
		}
		MethodSpec structure = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC).addCode(cb.build()).build();
		builder.addMethod(structure);

		// 添加设置代理的类
		ParameterSpec parameterSpec = ParameterSpec.builder(Object.class, "obj").addModifiers(Modifier.FINAL).build();
		MethodSpec listener = MethodSpec.methodBuilder(SET_LISTENER).addModifiers(Modifier.PUBLIC).returns(void.class).addParameter(parameterSpec).addAnnotation(Override.class)
				.addCode("this.obj = (" + entity.className + ")obj;\n").build();
		builder.addJavadoc("该文件为自动生成的代理文件，请不要修改该文件的任何代码！\n");

		// 创建父类参数
		ClassName superClass = ClassName.get(PKG_SCHEDULER, CLS_SCHEDULER_LISTENER);
		// 创建泛型
		ClassName typeVariableName = ClassName.get(taskEnum.getPkg(), taskEnum.getClassName());
		builder.superclass(ParameterizedTypeName.get(superClass, typeVariableName));
		builder.addMethod(listener);
		return builder.build();
	}

	/**
	 * 创建代理方法
	 *
	 * @param taskEnum 任务类型枚举{@link TaskEnum}
	 * @param annotation 注解 {@link Download}
	 * @param methodName 被代理类注解的方法名
	 * @return
	 */
	private MethodSpec createProxyMethod(TaskEnum taskEnum, Class<? extends Annotation> annotation, String methodName)
	{
		ClassName task = ClassName.get(taskEnum.getPkg(), taskEnum.getClassName());
		ParameterSpec parameterSpec = ParameterSpec.builder(task, "task").addModifiers(Modifier.FINAL).build();
		StringBuilder sb = new StringBuilder();
		sb.append("Set<String> keys = keyMapping.get(\"").append(methodName).append("\");\n");
		sb.append("if (keys != null) {\n\tif (keys.contains(task.getKey())) {\n").append("\t\tobj.").append(methodName).append("((").append(taskEnum.getClassName()).append(")task);\n")
				.append("\t}\n} else {\n").append("\tobj.").append(methodName).append("((").append(taskEnum.getClassName()).append(")task);\n}\n");

		return MethodSpec.methodBuilder(annotation.getSimpleName()).addModifiers(Modifier.PUBLIC).returns(void.class).addParameter(parameterSpec).addAnnotation(Override.class).addCode(sb.toString())
				.build();
	}

	private void createFile(JavaFile javaFile) throws Exception
	{
		javaFile.writeTo(mFiler);
	}

	/**
	 * 字符串数组转set
	 *
	 * @param keys 注解中查到的key
	 * @return
	 */
	private Set<String> convertSet(String[] keys)
	{
		if (keys == null || keys.length == 0)
			return null;
		if (keys[0].isEmpty())
			return null;
		Set<String> set = new HashSet<>();
		Collections.addAll(set, keys);
		return set;
	}
}
