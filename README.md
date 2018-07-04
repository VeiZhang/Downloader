# Downloader 文件下载器

* [DownloaderLibrary](#DownloaderLibrary)

DownloaderLibrary<a name="DownloaderLibrary">
---------------------------------------------

[![Download][icon_download]][download]


> - HttpURLConnection下载文件依赖库，实现多任务单线程断点下载

> - 临时文件的下载长度作为断点标记

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
    compile 'com.excellence:downloader:_latestVersion'
  }
```
或者直接添加本地Library依赖
```
compile project(':DownloaderLibrary')
```


### 使用示例

* 初始化
    ```java
    // 默认下载选项：任务数2，单任务单线程下载
    Downloader.init(Context context)
    // 设置下载选项
    Downloader.init(Context context, DownloadOptions options)
    ```

* 结束任务
    ```java
    // 暂停所有下载任务，使用文件长度保存断点
    Downloader.destroy();
    ```

* 监听两种方式

    推荐使用**注解方式**监听
    * **注解监听**
        ```java
        // 注册
        Downloader.register(this);

        // 解绑
        Downloader.unregister(this);

        // 监听
        @Download.onPreExecute
        public void onPre(DownloadTask task)
        {
            /**
             * 注解不添加URL，则获取全部任务的下载监听；
             * 加了URL，则过滤出对应的任务的下载监听
             * 如：@Download.onPreExecute({QQ_URL, ANGRYBIRDS_URL})
             */
        }

        @Download.onProgressChange
        public void onProgressChange(DownloadTask task)
        {
            /**
             * @see #onPre(DownloadTask)
             */
        }

        @Download.onProgressSpeedChange
        public void onProgressSpeedChange(DownloadTask task)
        {
            /**
             * @see #onPre(DownloadTask)
             */
        }

        @Download.onCancel
        public void onCancel(DownloadTask task)
        {
            /**
             * @see #onPre(DownloadTask)
             */
        }

        @Download.onError
        public void onError(DownloadTask task)
        {
            /**
             * @see #onPre(DownloadTask)
             */
        }

        @Download.onSuccess
        public void onSuccess(DownloadTask task)
        {
            /**
             * @see #onPre(DownloadTask)
             */
        }
        ```

    * 添加下载任务，并开始下载
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

* 暂停下载任务
    ```java
    DownloadTask.pause();
    ```

* 恢复下载任务
    ```java
    DownloadTask.resume();
    ```

* 删除下载任务
    ```java
    DownloadTask.discard();
    ```

### 修改日志

|         版本         |         描述         |
| ------------------- | ------------------- |
| [1.2.0][DownloadLibrary1.2.0] | 提升下载速度，增加设置项 **2018-7-4** |
| [1.1.0][DownloadLibrary1.1.0] | 注解监听任务 **2017-9-13** |
| [1.0.0][DownloadLibrary1.0.0] | 多任务单线程下载，临时下载文件长度保存断点记录  **2017-8-11** |

### 注意事项

动态申请权限，可以参考[permission][permission]

* 1.1.0以前的版本，**添加了限制：必须在Activity中初始化**，原因是Android6.0+需要动态申请权限
* 1.1.0以前的版本，没有适配**畸形国产机的权限（使用原生方法不能申请权限成功）**，需自己实现
* 1.1.0以后的版本，**去掉在Activity初始化的限制**，但是**畸形国产机权限（使用原生方法不能申请权限成功）** 和Android6.0+需要自己实现动态申请文件读写权限，否则会导致下载失败


<!-- 网站链接 -->

[download]:https://bintray.com/veizhang/maven/downloader/_latestVersion "Latest version"
[permission]:https://github.com/VeiZhang/Permission

<!-- 图片链接 -->

[icon_download]:https://api.bintray.com/packages/veizhang/maven/downloader/images/download.svg

<!-- 版本 -->

[DownloadLibrary1.2.0]:https://bintray.com/veizhang/maven/downloader/1.2.0
[DownloadLibrary1.1.0]:https://bintray.com/veizhang/maven/downloader/1.1.0
[DownloadLibrary1.0.0]:https://bintray.com/veizhang/maven/downloader/1.0.0
