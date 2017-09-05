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

	@Override
	public synchronized void init(ProcessingEnvironment processingEnvironment)
	{
		super.init(processingEnvironment);
		mElementHandler = new ElementHandler(processingEnvironment.getFiler(), processingEnvironment.getElementUtils());
	}

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

	@Override
	public SourceVersion getSupportedSourceVersion()
	{
		return SourceVersion.latestSupported();
	}

	@Override
	public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnv)
	{
		mElementHandler.clean();
		mElementHandler.handleDownload(roundEnv);
		mElementHandler.createProxyFile();
		return true;
	}
}
