package com.excellence.compiler;

import com.excellence.annotations.Download;
import com.google.auto.service.AutoService;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.annotation.processing.Filer;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     time   : 2017/8/25
 *     desc   : 事件注解扫描器
 *     			{@link com.google.auto.service.AutoService}:编译时，Java程序自动执行，创建类文件等
 * </pre>
 */

@AutoService(Processor.class)
public class DownloadProcessor extends AbstractProcessor
{
	private ElementHandler mElementHandler = null;

	/**
	 * 每一个注解处理器类都必须有一个空的构造函数。然而，这里有一个特殊的{@link #init}方法，它会被注解处理工具调用，并输入{@link ProcessingEnvironment}参数。{@link ProcessingEnvironment}提供很多有用的工具类{@link Elements}、{@link Types}、{@link Filer}。
	 *
	 * @param processingEnvironment
	 */
	@Override
	public synchronized void init(ProcessingEnvironment processingEnvironment)
	{
		super.init(processingEnvironment);
		mElementHandler = new ElementHandler(processingEnvironment.getFiler(), processingEnvironment.getElementUtils());
	}

	/**
	 * 必须指定，这个注解处理器是注册给哪个注解的。注意，它的返回值是一个字符串的集合，包含本处理器想要处理的注解类型的合法全称。换句话说，在这里定义你的注解处理器注册到哪些注解上。
	 *
	 * @return
	 */
	@Override
	public Set<String> getSupportedAnnotationTypes()
	{
		Set<String> annotations = new LinkedHashSet<>();
		annotations.add(Download.onPreExecute.class.getCanonicalName());
		annotations.add(Download.onProgressChange.class.getCanonicalName());
		annotations.add(Download.onProgressSpeedChange.class.getCanonicalName());
		annotations.add(Download.onCancel.class.getCanonicalName());
		annotations.add(Download.onError.class.getCanonicalName());
		annotations.add(Download.onSuccess.class.getCanonicalName());
		return annotations;
	}

	/**
	 * 用来指定你使用的Java版本。
	 *
	 * @return
	 */
	@Override
	public SourceVersion getSupportedSourceVersion()
	{
		return SourceVersion.latestSupported();
	}

	/**
	 * 这相当于每个处理器的主函数main()。 在这里写扫描、评估和处理注解的代码，以及生成Java文件。输入参数{@link RoundEnvironment}，可以让查询出包含特定注解的被注解元素。
	 * 该方法返回ture表示该注解已经被处理, 后续不会再有其他处理器处理; 返回false表示仍可被其他处理器处理
	 *
	 * @param set
	 * @param roundEnv
	 * @return
	 */
	@Override
	public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnv)
	{
		mElementHandler.clean();
		mElementHandler.handleDownload(roundEnv);
		mElementHandler.createProxyFile();
		return true;
	}
}
