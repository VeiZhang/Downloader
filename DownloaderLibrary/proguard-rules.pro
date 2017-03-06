# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in E:\AndroidTools\AndroidStudio\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-assumenosideeffects class android.util.Log {				#去掉代码里的Log
    public static boolean isLoggable(java.lang.String,int);
    public static *** d(...);
	public static *** v(...);
	public static *** i(...);
	public static *** w(...);
	public static *** e(...);
}

-keep class com.excellence.downloader.** {*;}				#保证源码不混淆
-keep class com.excellence.downloader.exception.** {*;}
-keep class com.excellence.downloader.utils.** {*;}

-renamesourcefileattribute SourceFile     					#保证异常时显示行号
-keepattributes SourceFile,LineNumberTable