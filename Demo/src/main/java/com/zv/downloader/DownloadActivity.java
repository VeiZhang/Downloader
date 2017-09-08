package com.zv.downloader;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import java.io.File;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : https://veizhang.github.io/
 *     time   : 2017/2/15
 *     desc   :
 * </pre>
 */

public abstract class DownloadActivity extends AppCompatActivity
{
	/**
	 * LOCAL_IP是基于本地服务器测试的链接，如果本地服务器关闭，则下载失败
	 */
	private static final String LOCAL_IP = "http://192.168.33.17/";
	protected static final String FILMON_URL = LOCAL_IP + "Filmon.apk";
	protected static final String ROMUPDATE_URL = LOCAL_IP + "RomUpdate_V101_FW101_01_00_20170214.bin";
	protected static final String QQ_URL = "http://gdown.baidu.com/data/wisegame/dc429998555b7d4d/QQ_398.apk";
	protected static final String ANGRYBIRDS_URL = "http://gdown.baidu.com/data/wisegame/9d4083325b73f6d7/fennudexiaoniaozhongwenban_22200603.apk";

	private static final String DOWNLOAD_DIR = "ZVDownloader";
	private static final String MNT_DIR = "/mnt/";
	protected static final String DOWNLOAD_PATH;

	static
	{
		File file = new File(Environment.getExternalStorageDirectory().getPath(), DOWNLOAD_DIR);
		if (file.exists() || file.mkdirs())
			DOWNLOAD_PATH = file.getPath();
		else
		{
			file = new File(MNT_DIR, DOWNLOAD_DIR);
			if (file.exists() || file.mkdirs())
				DOWNLOAD_PATH = file.getPath();
			else
				DOWNLOAD_PATH = MNT_DIR;
		}

	}

	protected ListView mDownloadListView = null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_download);
		initDownloader();
		initView();
	}

	private void initView()
	{
		mDownloadListView = (ListView) findViewById(R.id.download_list_view);
	}

	@Override
	public void finish()
	{
		destroyDownloader();
		super.finish();
	}

	protected abstract void initDownloader();

	protected abstract void destroyDownloader();

}
