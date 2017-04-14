# Downloader 文件下载器

* [DownloaderLibrary](#DownloaderLibrary)

* [Netroid](#Netroid)

DownloaderLibrary<a name="DownloaderLibrary">
----------------------------
[![Bintray][icon_Bintray]][Bintray]
[![GitHub forks][icon_forks]][forks]
[![GitHub stars][icon_stars]][stars]

> - HttpURLConnection下载文件依赖库，实现多任务多线程断点下载

> - 数据库记录下载长度支持断点标记

### 权限
```
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
```

### 导入Android Studio
添加jCenter远程依赖到module里的build.gradle：
```
dependencies {
    compile 'com.excellence:downloader:1.0.0'
    // 或者直接使用最新版本
    // compile 'com.excellence:downloader:+'
  }
```
或者直接添加本地Library依赖
```
compile project(':DownloaderLibrary')
```


### 使用示例
1.onCreate方法中初始化
```java
// 最大下载的任务数
DownloaderManager.init(int parallelTaskCount);
```
2.finish结束所有任务
```java
// 暂停所有下载任务，保存断点，并关闭数据库
DownloaderManager.destroy(Context context);
```
3.添加下载任务，并开始下载
```java
// 文件路径，下载链接
DownloaderManager.addTask(File file, String DownloadURL, new DownloaderListener()
{

    @Override
    public void onPreExecute(long fileSize)
    {
        super.onPreExecute(fileSize);
    }

    @Override
    public void onProgressChange(long fileSize, long downloadedSize)
    {
        super.onProgressChange(fileSize, downloadedSize);
    }

    @Override
    public void onCancel()
    {
        super.onCancel();
    }

    @Override
    public void onError(DownloadError error)
    {
        super.onError(error);
        error.printStackTrace();
    }

    @Override
    public void onSuccess()
    {
        super.onSuccess();
    }
});
```
4.暂停下载任务
```java
FileDownloader.pause();
```
5.恢复下载任务
```java
FileDownloader.resume();
```
6.删除下载任务
```java
FileDownloader.discard();
```

### 修改日志
|         版本         |         描述         |
| ------------------- | ------------------- |
| [1.0.0][DownloadLibrary1.0.0] | 多线程下载，数据库断点续传  **2017-3-6** |

```
问题：
    1.服务器是否支持断点，支持则多线程断点，否则建议单线程不断点
```


Netroid<a name="Netroid">
-------------------------
> - Netroid是一个基于Volley实现的Android Http库。提供执行网络请求、缓存返回结果、批量图片加载、大文件断点下载的常见Http交互功能

> - 临时文件下载长度支持断点标记

[传送门][NetroidLibrary]


<!-- 网站链接 -->
[Bintray]:https://bintray.com/veizhang/maven/downloader "Bintray"
[forks]:https://github.com/VeiZhang/Downloader/network/members
[stars]:https://github.com/VeiZhang/Downloader/stargazers
[NetroidLibrary]:http://netroid.cn/

<!-- 图片链接 -->
[icon_Bintray]:https://img.shields.io/badge/Bintray-v1.0.0-brightgreen.svg
[icon_forks]:https://img.shields.io/github/forks/VeiZhang/Downloader.svg?style=social
[icon_stars]:https://img.shields.io/github/stars/VeiZhang/Downloader.svg?style=social

<!-- 版本 -->
[DownloadLibrary1.0.0]:https://bintray.com/veizhang/maven/downloader/1.0.0
