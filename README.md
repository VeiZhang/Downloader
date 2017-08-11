# Downloader 文件下载器

* [DownloaderLibrary](#DownloaderLibrary)

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
// 默认任务数2，单任务单线程下载
Downloader.init(Context context)
// 设置最大下载的任务数，单任务下载的线程数
Downloader.init(Context context, int parallelTaskCount, int threadCount)
```
2.finish结束所有任务
```java
// 暂停所有下载任务，使用文件长度保存断点
Downloader.destroy();
```
3.添加下载任务，并开始下载
```java
// 文件路径，下载链接，监听接口可以使用IListener接口，也可以使用Listener监听部分回调
Downloader.addTask(File file, String DownloadURL, new IListener()
{

    @Override
    public void onPreExecute(long fileSize)
    {

    }

    @Override
    public void onProgressChange(long fileSize, long downloadedSize)
    {

    }

    @Override
    public void onProgressChange(long fileSize, long downloadedSize, long speed)
    {

    }

    @Override
    public void onCancel()
    {

    }

    @Override
    public void onError(DownloadError error)
    {

    }

    @Override
    public void onSuccess()
    {

    }

}));
```
4.暂停下载任务
```java
DownloadTask.pause();
```
5.恢复下载任务
```java
DownloadTask.resume();
```
6.删除下载任务
```java
DownloadTask.discard();
```

### 修改日志
|         版本         |         描述         |
| ------------------- | ------------------- |
| [1.0.0][DownloadLibrary1.0.0] | 多任务单线程下载，临时下载文件长度保存断点记录  **2017-8-11** |




<!-- 网站链接 -->
[Bintray]:https://bintray.com/veizhang/maven/downloader "Bintray"
[forks]:https://github.com/VeiZhang/Downloader/network/members
[stars]:https://github.com/VeiZhang/Downloader/stargazers

<!-- 图片链接 -->
[icon_Bintray]:https://img.shields.io/badge/Bintray-v1.0.0-brightgreen.svg
[icon_forks]:https://img.shields.io/github/forks/VeiZhang/Downloader.svg?style=social
[icon_stars]:https://img.shields.io/github/stars/VeiZhang/Downloader.svg?style=social

<!-- 版本 -->
[DownloadLibrary1.0.0]:https://bintray.com/veizhang/maven/downloader/1.0.0
